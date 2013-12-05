package com.pay.telcel.main;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.RelativeLayout;

public class SettingActivity extends Activity implements OnClickListener{
	
	RelativeLayout feedback, about_layout, logout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.setting_layout);
		
		feedback = (RelativeLayout) findViewById(R.id.feedback);
		about_layout = (RelativeLayout) findViewById(R.id.about_layout);
		logout = (RelativeLayout) findViewById(R.id.logout);
		
		feedback.setOnClickListener(this);
		about_layout.setOnClickListener(this);
		logout.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.feedback:
			Intent mIntent = new Intent(SettingActivity.this, FeedbackActivity.class);
			startActivity(mIntent);
			break;
			
		case R.id.about_layout:
			mIntent = new Intent(SettingActivity.this, AboutActivity.class);
			startActivity(mIntent);
			break;
			
		case R.id.logout:
			SharedPreferences perference = getSharedPreferences("user_profile", Context.MODE_PRIVATE);
			Editor editor = perference.edit();
			editor.putString("account", "");
			editor.putString("password", "");
			editor.putString("balance", "");
			editor.commit();
			
			finish();
			android.os.Process.killProcess(android.os.Process.myPid());
			System.exit(0);
			
			break;

		default:
			break;
		}
	}

}
