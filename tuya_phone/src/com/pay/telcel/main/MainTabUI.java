package com.pay.telcel.main;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.Toast;

public class MainTabUI extends TabActivity {
	
	private TabHost mTabHost;
	private RadioGroup mRadioGroup;
	private static final int INTERVAL_TIME = 5;
	private static final int MILLISECOND_TO_SECOND = 1000;
	private boolean mIsExit;
	private static long sFirstTimePressBackBtn;
	public CApplication mApp;
	public RadioButton main_tab_dialhistory;
	public String mTextviewArray[] = {"呼叫", "联系人", "充值", "设置"};
	 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.tab_main_activity);
		
		mApp = (CApplication) getApplication();
        init_view();
	}
	
	
	private void init_view() {
		mTabHost = getTabHost();
		 
		int count = mTabClassArray.length;		
		for(int i = 0; i < count; i++) {	
				TabSpec tabSpec = mTabHost.newTabSpec(
						mTextviewArray[i]).setIndicator(mTextviewArray[i]).setContent(getTabItemIntent(i));
				mTabHost.addTab(tabSpec);
		}
		
		mRadioGroup = (RadioGroup) findViewById(R.id.main_radiogroup);
		mRadioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				switch(checkedId){
				case R.id.main_tab_dialhistory:
					mTabHost.setCurrentTabByTag(mTextviewArray[0]);
					break;
				case R.id.main_tab_address:
					mTabHost.setCurrentTabByTag(mTextviewArray[1]);
					break;
				case R.id.main_tab_recharge:
					mTabHost.setCurrentTabByTag(mTextviewArray[2]);
					break;
				case R.id.main_tab_settings:
					mTabHost.setCurrentTabByTag(mTextviewArray[3]);
					break;
				}
			}
		});
		
		 ((RadioButton) mRadioGroup.getChildAt(0)).toggle();
		 
		 main_tab_dialhistory = (RadioButton) findViewById(R.id.main_tab_dialhistory);
		 main_tab_dialhistory.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view) {
				Log.i("", "abc");
			}
		});
		 
	 }
	 
	 
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			if (!mIsExit) {
				Toast.makeText(this, getText(R.string.str_again_exit), Toast.LENGTH_SHORT).show();
				mIsExit = true;
				sFirstTimePressBackBtn = System.currentTimeMillis();
			} else {
				long interval = (System.currentTimeMillis() - sFirstTimePressBackBtn) / MILLISECOND_TO_SECOND;
				if (interval < INTERVAL_TIME) {
					finish();
					android.os.Process.killProcess(android.os.Process.myPid());
					System.exit(0);

				} else {
					mIsExit = false;
					Toast.makeText(this, getText(R.string.str_again_exit), Toast.LENGTH_SHORT).show();
				}
			}

			return false;
		}
		return super.dispatchKeyEvent(event);
	};
		
		
		
	 private Intent getTabItemIntent(int index) {
		Intent intent = new Intent(this, mTabClassArray[index]);
		return intent;
	 }
	 
	 public Class mTabClassArray[] = {
		 		HomeDialActivity.class,
//		 		MainActivity.class,
//		 		Activity_3.class,
//		 		Activity_4.class
		 	};
	 

}
