package com.pay.telcel.main;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.RelativeLayout;

public class SettingActivity extends Activity implements OnClickListener{
	
	RelativeLayout feedback, about_layout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.setting_layout);
		
		feedback = (RelativeLayout) findViewById(R.id.feedback);
		about_layout = (RelativeLayout) findViewById(R.id.about_layout);
		
		feedback.setOnClickListener(this);
		about_layout.setOnClickListener(this);
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

		default:
			break;
		}
	}

}
