package com.pay.telcel.main;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.provider.ContactsContract;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class OutgoingActivity extends Activity implements OnClickListener, Callback{

	TextView info, status;
	Button cancel, remotedial, localdial;
	String number = "";
	CApplication cApp;
	Handler mHandler;
	ProgressBar pbar1;
	public final static String B_PHONE_STATE = TelephonyManager.ACTION_PHONE_STATE_CHANGED;
	private OutgoingCallReceiver mBroadcastReceiver;
	String account = "", password = "";
	private SharedPreferences perference;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.outgoing_layout);
		
		cancel = (Button) findViewById(R.id.cancel);
		remotedial = (Button) findViewById(R.id.remotedial);
		localdial = (Button) findViewById(R.id.localdial);
		pbar1 = (ProgressBar) findViewById(R.id.pbar1);
		info = (TextView) findViewById(R.id.info);
		status = (TextView) findViewById(R.id.status);
		
		
		cancel.setOnClickListener(this);
		remotedial.setOnClickListener(this);
		localdial.setOnClickListener(this);
		
		mHandler = new Handler(this);
		cApp = (CApplication) getApplication();
		number = getIntent().getStringExtra("number");
		String contact = getPeople(number);
		if ( !"".equals(contact) ){
			info.setText(contact + " " + number);
		}else {
			info.setText(number);
		}
		
		
		
//		registerThis();
		
//		Intent intent = getIntent();
//		if (intent != null) {
//			number = PhoneNumberUtils.getNumberFromIntent(intent, this);
//			String contact = getPeople(number);
//			if ( !"".equals(contact) ){
//				info.setText(contact + " " + number);
//			}else {
//				info.setText(number);
//			}
//			
//			dial();
//		}else {
//			return;
//		}
		
		
	}

	
	public void registerThis() {
        mBroadcastReceiver = new OutgoingCallReceiver();
        IntentFilter intentFilter = new IntentFilter();  
        intentFilter.addAction(B_PHONE_STATE);
        intentFilter.setPriority(Integer.MAX_VALUE);
        registerReceiver(mBroadcastReceiver, intentFilter);
    }
	
	public void unregisterThis() {  
        unregisterReceiver(mBroadcastReceiver);  
    }
	

	public String getPeople(String mNumber) {
		String[] projection = { ContactsContract.PhoneLookup.DISPLAY_NAME,
				ContactsContract.CommonDataKinds.Phone.NUMBER };

		Cursor cursor = this.getContentResolver().query(
				ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
				projection, // Which columns to return.
				ContactsContract.CommonDataKinds.Phone.NUMBER + " like '%"	+ mNumber + "%' ", null, null);

		if (cursor == null) {
			return "";
		}
		
		String name = "";
		for (int i = 0; i < cursor.getCount(); i++) {
			cursor.moveToPosition(i);
			int nameFieldColumnIndex = cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME);
			name = cursor.getString(nameFieldColumnIndex);
		}
		
		return name;
	} 
	
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
//		unregisterThis();
		finish();
	}


	@Override
	public void onClick(View view) {
		
		switch (view.getId()) {
		case R.id.cancel:
			finish();
			android.os.Process.killProcess(android.os.Process.myPid());
			System.exit(0);
			break;
			
		case R.id.remotedial:
			if ( false == cApp.isConnectingToInternet() ){
				Toast.makeText(OutgoingActivity.this, getString(R.string.network_disconnet), Toast.LENGTH_SHORT).show();
			}else {
				dial();
			}
			
			break;
			
		case R.id.localdial:
			cApp.inner = true;
			Intent intent=new Intent("android.intent.action.CALL", Uri.parse("tel:"+number));
			startActivity(intent);
			finish();
			break;

		default:
			break;
		}
	}


	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
		case 1:
			cancel.setVisibility(View.GONE);
			status.setText("发送成功, 等待回复");
			break;
			
		case 2:
			pbar1.setVisibility(View.GONE);
			Toast.makeText(OutgoingActivity.this, "请求失败,请确认网络是否正常或号码是否填写正确", Toast.LENGTH_SHORT).show();
			break;

		default:
			break;
		}
		return false;
	}
	
	public void dial(){
		if ( number.startsWith("+86") || number.startsWith("0086") || number.startsWith("086") ){
			Toast.makeText(OutgoingActivity.this, "请不要在号码前加86", Toast.LENGTH_SHORT).show();
			return;
		}
		
		SharedPreferences perference = getSharedPreferences("user_profile", Context.MODE_PRIVATE);
		if ( !perference.getString("account", "").equals("")  ||   !perference.getString("password", "").equals("")) {
			account = perference.getString("account", "");
			password = perference.getString("password", "");
		}else {
			Toast.makeText(OutgoingActivity.this, "请先注册或登陆", Toast.LENGTH_SHORT).show();
			return;
		}
		
		int bb = Integer.valueOf(perference.getString("balance", ""));
		if ( bb == 0 ){
			Toast.makeText(OutgoingActivity.this, "余额不足，请充值", Toast.LENGTH_SHORT).show();
			return;
		}
		
		cApp.inner = true;
		status.setText("发送呼叫请求");
		localdial.setVisibility(View.GONE);
		remotedial.setVisibility(View.GONE);
		cancel.setVisibility(View.VISIBLE);
		pbar1.setVisibility(View.VISIBLE);
		
		Thread mThread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				String host = "http://da.bigo.me/interface/call/mobile/" + account + "/pwd/" + password + "/phone/" +number;
				HttpPost httpRequest = new HttpPost(host);
				List<BasicNameValuePair> Vaparams = new ArrayList<BasicNameValuePair>();
				
				try {
					httpRequest.setEntity(new UrlEncodedFormEntity(Vaparams, HTTP.UTF_8));
					HttpResponse httpResponse = new DefaultHttpClient().execute(httpRequest);

					if (httpResponse.getStatusLine().getStatusCode() == 200) {
						String preTel = cApp.retrieveInputStream(httpResponse.getEntity());
						Log.i("", preTel);
						
						Message msg = new Message();
						if ( preTel.equals("{0}") ){
							msg.what = 1;
							mHandler.sendMessage(msg);
						}else {
							
							msg.what = 2;
							mHandler.sendMessage(msg);
						}
						
						
					}
				} catch (Exception e) {
					Message msg = new Message();
					msg.what = 2;
					mHandler.sendMessage(msg);
					
					e.printStackTrace();
				}
				
			}
		});
		
		mThread.start();
	}

}
