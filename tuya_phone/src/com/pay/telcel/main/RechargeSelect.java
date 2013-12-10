package com.pay.telcel.main;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.hardcore.crashreport.CrashReportingApplication;

public class RechargeSelect extends Activity implements OnClickListener, Callback{

	public RelativeLayout way_pay_layout_direct, way_pay_layout_kami, balance_layout;
	private SharedPreferences perference;
	public TextView account, balanceTV;
	public String accountInit = "", balance="", host="";
	CrashReportingApplication cApp;
	Handler mHandler;
	final int Msg_Back_Success = 1;
	final int Msg_Back_Failure = 2;
	final int BACK_CHECK_BALANCE = 3;
	ProgressDialog pDialog;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.recharge_select);
		
		cApp = (CrashReportingApplication) getApplication();
		mHandler = new Handler(this);
		perference = getSharedPreferences("user_profile", Context.MODE_PRIVATE);
		accountInit = perference.getString("account", "");
		balance = perference.getString("balance", "");
		
		account = (TextView) findViewById(R.id.account);
		balanceTV = (TextView) findViewById(R.id.balance);
		
		account.setText(accountInit);
		balanceTV.setText(balance + " 元");
		
		way_pay_layout_kami = (RelativeLayout) findViewById(R.id.way_pay_layout_kami);
		way_pay_layout_direct = (RelativeLayout) findViewById(R.id.way_pay_layout_direct);
		balance_layout = (RelativeLayout) findViewById(R.id.balance_layout);
		
		way_pay_layout_kami.setOnClickListener(this);
		way_pay_layout_direct.setOnClickListener(this);
		balance_layout.setOnClickListener(this);
		
		pDialog = new ProgressDialog(this);
		pDialog.setMessage("请稍候...");
	}


	public void paybyMiMa(String cardId, String cardPW){
		final String host = "http://da.bigo.me/interface/pay/mobile/" +accountInit+"/cardid/"+ cardId+"/cardpwd/"+cardPW;
		pDialog.show();
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				HttpPost httpRequest = new HttpPost(host);
				List<BasicNameValuePair> Vaparams = new ArrayList<BasicNameValuePair>();
				
				try {
					httpRequest.setEntity(new UrlEncodedFormEntity(Vaparams, HTTP.UTF_8));
					HttpResponse httpResponse = new DefaultHttpClient().execute(httpRequest);

					if (httpResponse.getStatusLine().getStatusCode() == 200) {
						String preTel = cApp.retrieveInputStream(httpResponse.getEntity());
						Log.i("", "返回:" + preTel);
						
						Message msg = new Message();
						if ( preTel.contains("ERR1") ){
							msg.what = Msg_Back_Failure;
							msg.obj = "手机号码格式不正确";
							mHandler.sendMessage(msg);
						}
						else if ( preTel.contains("ERR2") ) {
							msg.what = Msg_Back_Failure;
							msg.obj = "不存在此绑定号码";
							mHandler.sendMessage(msg);
						}
						else if ( preTel.contains("ERR3") ) {
							msg.what = Msg_Back_Failure;
							msg.obj = "卡密错误";
							mHandler.sendMessage(msg);
						}
						else {
							preTel = preTel.substring(1, preTel.length()-1);
							
							msg.what = Msg_Back_Success;
							msg.obj = preTel;
							mHandler.sendMessage(msg);
						}
						
					}
				} catch (Exception e) {
					
					e.printStackTrace();
				}
				
			}
		}).start();
	}
	
	
	//查余额
	public void doCheckBalance(){
		host = "http://da.bigo.me/interface/BalanceNoPwd/mobile/" + accountInit;
		pDialog.show();
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				HttpPost httpRequest = new HttpPost(host);
				List<BasicNameValuePair> Vaparams = new ArrayList<BasicNameValuePair>();
				
				try {
					httpRequest.setEntity(new UrlEncodedFormEntity(Vaparams, HTTP.UTF_8));
					HttpResponse httpResponse = new DefaultHttpClient().execute(httpRequest);

					if (httpResponse.getStatusLine().getStatusCode() == 200) {
						String preTel = cApp.retrieveInputStream(httpResponse.getEntity());
//						Log.i("", preTel);
						
						JSONObject json = new JSONObject(preTel);
						JSONObject userinfo = json.getJSONObject("userinfo");
						String balance = userinfo.getString("balance");
						int errcode = json.getInt("errcode");
						if ( errcode == 0 ){
							//成功
							Message msg = new Message();
							msg.what = BACK_CHECK_BALANCE;
							msg.obj = balance;
							mHandler.sendMessage(msg);
						}
						
						
					}
				} catch (Exception e) {
					
					e.printStackTrace();
				}
				
			}
		}).start();
	}
	
	
	public void showDialog() {
		LayoutInflater inflater = getLayoutInflater();
		final View layout = inflater.inflate(R.layout.dilog_common, (ViewGroup) findViewById(R.id.dialog_comm));
		new AlertDialog.Builder(this)
		.setTitle("请输入卡密")
		.setView(layout)
		.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				EditText cardID = (EditText) layout.findViewById(R.id.cardID);
				EditText cardPW = (EditText) layout.findViewById(R.id.cardPW);
				
				if ( "".equals(cardID.getText().toString()) || "".equals(cardPW.getText().toString()) ){
					Toast.makeText(RechargeSelect.this, "不能为空", Toast.LENGTH_SHORT).show();
				}else {
					paybyMiMa(cardID.getText().toString(), cardPW.getText().toString());
				}
				
			}
		})
		.setNegativeButton("取消", null).show();
	}
	
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.way_pay_layout_kami:
//			showDialog();
			
			Intent mIntent = new Intent(RechargeSelect.this, Address_KaMi.class);
			mIntent.putExtra("phoneNum", accountInit);
			startActivity(mIntent);
			break;
			
		case R.id.way_pay_layout_direct:
			mIntent = new Intent(RechargeSelect.this, PayByAlipay.class);
			mIntent.putExtra("phoneNum", accountInit);
			startActivity(mIntent);
			break;
			
		case R.id.balance_layout:
			doCheckBalance();
			break;

		default:
			break;
		}
	}


	@Override
	public boolean handleMessage(Message msg) {
		pDialog.dismiss();
		
		switch (msg.what) {
		case Msg_Back_Failure:
			Toast.makeText(RechargeSelect.this, msg.obj.toString(), Toast.LENGTH_SHORT).show();
			break;
			
		case Msg_Back_Success:
			int amount = Integer.valueOf(msg.obj.toString());
			String ss = perference.getString("balance", "");
			Float fl = Float.valueOf(ss);
			fl = fl + amount;
			
			Editor editor = perference.edit();
			editor.putString("balance", ""+ fl);
			editor.commit();
			
			balanceTV.setText(fl + " 元");
			Toast.makeText(RechargeSelect.this, "充值成功，充值"+ amount+ "元", Toast.LENGTH_LONG).show();
			break;
			
		case BACK_CHECK_BALANCE:
			pDialog.dismiss();
			String amounttt = msg.obj.toString();
			
			editor = perference.edit();
			editor.putString("balance", ""+ amounttt);
			editor.commit();
			balanceTV.setText(amounttt + " 元");
			
			break;

		default:
			break;
		}
		return false;
	}

}
