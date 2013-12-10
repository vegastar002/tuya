package com.pay.telcel.main;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

import org.apache.http.HttpResponse;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.android.hardcore.crashreport.CrashReportingApplication;

public class Login extends Activity implements OnClickListener, Callback{

	Button register, login_login_btn;
	EditText login_user_edit, login_passwd_edit;
	Handler mHandler;
	CrashReportingApplication cApp;
	ProgressDialog pDialog;
	String host = "", password = "", phoneNum = "";
	private static final int BACK_CHECK_LOGIN = 1;
	private static final int BACK_PASSWORD_ERROR = 2;
	private static final int BACK_NO_ACCOUNT = 3;
	private static final int ASK_CHECK_BALANCE = 4;
	private static final int BACK_CHECK_BALANCE = 5;
	public PopupWindow m_popupWindow;
	public Timer timer;
	View subview2;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.loginnew2);
		
		mHandler = new Handler(this);
		cApp = (CrashReportingApplication) getApplication();
		timer = new Timer(true);
		register = (Button) findViewById(R.id.register);
		login_login_btn = (Button) findViewById(R.id.login_login_btn);
		login_user_edit = (EditText) findViewById(R.id.login_user_edit);
		login_passwd_edit = (EditText) findViewById(R.id.login_passwd_edit);
		
		register.setOnClickListener(this);
		login_login_btn.setOnClickListener(this);
		
		pDialog = new ProgressDialog(this);
		pDialog.setMessage("登陆...");
		
		SharedPreferences perference = getSharedPreferences("user_profile", Context.MODE_PRIVATE);
		if ( !"".equals(perference.getString("account", "")) && !"".equals(perference.getString("password", "")) ){
			Intent mIntent = new Intent(Login.this, MainTabUI.class);
			startActivity(mIntent);
			finish();
		}
//		if ( !"".equals(perference.getString("account", "")) && !"".equals(perference.getString("password", "")) ){
//			//account和password匀不能为空
//			login_user_edit.setText(perference.getString("account", ""));
//			login_passwd_edit.setText(perference.getString("password", ""));
//			pDialog.show();
//			doLogin();
//		}
		
	}

	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ( keyCode == KeyEvent.KEYCODE_BACK){
			finish();
			android.os.Process.killProcess(android.os.Process.myPid());
			System.exit(0);
		}
		
		return super.onKeyDown(keyCode, event);
	}


	public void doLogin(){
		phoneNum = login_user_edit.getText().toString();
		password = login_passwd_edit.getText().toString();
		host = "http://da.bigo.me/interface/checklogin/mobile/"+ phoneNum+ "/pwd/" + password;
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				HttpPost httpRequest = new HttpPost(host);
				List<BasicNameValuePair> Vaparams = new ArrayList<BasicNameValuePair>();
				
				//验证账户密码是否正确
				try {
					httpRequest.setEntity(new UrlEncodedFormEntity(Vaparams, HTTP.UTF_8));
					HttpResponse httpResponse = new DefaultHttpClient().execute(httpRequest);

					if (httpResponse.getStatusLine().getStatusCode() == 200) {
						String preTel = cApp.retrieveInputStream(httpResponse.getEntity());
//						Log.i("", preTel);
						
						Message msg = new Message();
						if ( "{0}".equals(preTel) ){
							msg.what = BACK_CHECK_LOGIN;
							mHandler.sendMessage(msg);
							
						}else if ( "{1}".equals(preTel) ) {
							msg.what = BACK_PASSWORD_ERROR;
							mHandler.sendMessage(msg);
							
						}else if ( "{2}".equals(preTel) ) {
							msg.what = BACK_NO_ACCOUNT;
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
		host = "http://da.bigo.me/interface/BalanceNoPwd/mobile/" + phoneNum;
		
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
	
	
	public boolean check(){
		if(login_passwd_edit.getText().toString().equals("")) {
			Toast.makeText(Login.this, "请输入密码", Toast.LENGTH_SHORT).show();
			login_passwd_edit.requestFocus();
			return false;
		} else if(login_user_edit.getText().toString().equals("")) {
			Toast.makeText(Login.this, "请输入手机号 ", Toast.LENGTH_SHORT).show();
			login_user_edit.requestFocus();
			return false;
		}
		
		return true;
	}
	
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.register:
			Intent mIntent = new Intent(Login.this, register.class);
			startActivity(mIntent);
			break;
			
		case R.id.login_login_btn:
			if ( !check() ){
				return;
			}
			
			if ( !cApp.isConnectingToInternet() ){
				Toast.makeText(Login.this, getString(R.string.network_disconnet), Toast.LENGTH_SHORT).show();
				return;
			}
			
			subview2 = getLayoutInflater().inflate(R.layout.loading, null);
			m_popupWindow = new PopupWindow(subview2, LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT,true);
			m_popupWindow.setFocusable(true);
			m_popupWindow.setOutsideTouchable(true);
			m_popupWindow.setBackgroundDrawable(new BitmapDrawable());
			m_popupWindow.showAtLocation((View) login_login_btn, Gravity.TOP, 0, 0);
			
			doLogin();
			break;

		default:
			break;
		}
	}

	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
		case BACK_CHECK_LOGIN:
			mHandler.sendEmptyMessage(ASK_CHECK_BALANCE);
			break;
			
		case BACK_PASSWORD_ERROR:
			if ( m_popupWindow != null ){
				m_popupWindow.dismiss();
			}
			Toast.makeText(Login.this, "手机号或密码错误 ", Toast.LENGTH_SHORT).show();
			break;
			
		case BACK_NO_ACCOUNT:
			if ( m_popupWindow != null ){
				m_popupWindow.dismiss();
			}
			Toast.makeText(Login.this, "账户不存在，请注册 ", Toast.LENGTH_SHORT).show();
			break;
			
		case ASK_CHECK_BALANCE:
			doCheckBalance();
			break;
			
		case BACK_CHECK_BALANCE:
			if ( m_popupWindow != null ){
				m_popupWindow.dismiss();
			}
			pDialog.dismiss();
			
			SharedPreferences perference = getSharedPreferences("user_profile", Context.MODE_PRIVATE);
			Editor editor = perference.edit();
			editor.putString("account", phoneNum);
			editor.putString("password", password);
			editor.putString("balance", msg.obj.toString());
			editor.commit();
			
			Intent mIntent = new Intent(Login.this, MainTabUI.class);
			startActivity(mIntent);
			finish();
			break;
			
		default:
			break;
		}
		return false;
	}

}
