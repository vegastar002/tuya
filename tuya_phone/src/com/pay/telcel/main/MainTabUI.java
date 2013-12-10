package com.pay.telcel.main;

import android.app.TabActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
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

import com.android.hardcore.crashreport.CrashReportingApplication;

public class MainTabUI extends TabActivity implements Callback{
	
	private TabHost mTabHost;
	private RadioGroup mRadioGroup;
	private static final int INTERVAL_TIME = 5;
	private static final int MILLISECOND_TO_SECOND = 1000;
	private boolean mIsExit;
	private static long sFirstTimePressBackBtn;
	public CrashReportingApplication mApp;
	public RadioButton main_tab_dialhistory;
	public String mTextviewArray[] = {"呼叫", "联系人", "充值", "设置"};
	public int dial_toggle = 0;
	public Handler mHandler;
	public final static int MSG_Change_Show = 1;
	
	 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.tab_main_activity);
		
		registerBoradcastReceiver();
		mApp = (CrashReportingApplication) getApplication();
		mHandler = new Handler(MainTabUI.this);
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
					setTab1Draw();
					mTabHost.setCurrentTabByTag(mTextviewArray[1]);
					break;
				case R.id.main_tab_recharge:
					setTab1Draw();
					mTabHost.setCurrentTabByTag(mTextviewArray[2]);
					break;
				case R.id.main_tab_settings:
					setTab1Draw();
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
				if ( dial_toggle == 0 ){
					dial_toggle = 1;
					Drawable drawableTop = getResources().getDrawable(R.drawable.icon_tabbar_keyborddown);
					main_tab_dialhistory.setCompoundDrawablesWithIntrinsicBounds(null, drawableTop, null, null);
					Intent mIntent = new Intent(HomeDialActivity.Set_Control_Bohaopan);
			        mIntent.putExtra("show", "yes");
			        sendBroadcast(mIntent);
			        
			        
				}else if ( dial_toggle == 1 ) {
					dial_toggle = 0;
					Drawable drawableTop = getResources().getDrawable(R.drawable.icon_tabbar_keybordup);
					main_tab_dialhistory.setCompoundDrawablesWithIntrinsicBounds(null, drawableTop, null, null);
					Intent mIntent = new Intent(HomeDialActivity.Set_Control_Bohaopan);
			        mIntent.putExtra("show", "no");
			        sendBroadcast(mIntent);
			        
				}
				
			}
		});
		 
	 }
	
	public void setTab1Draw(){
		Drawable drawableTop = getResources().getDrawable(R.drawable.tab_dia_history);
		main_tab_dialhistory.setCompoundDrawablesWithIntrinsicBounds(null, drawableTop, null, null);
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
	
	public void registerBoradcastReceiver(){ 
        IntentFilter myIntentFilter = new IntentFilter(); 
        myIntentFilter.addAction(HomeDialActivity.Set_Notify_Status); 
        registerReceiver(mBroadcastReceiver, myIntentFilter); 
    }
	
	BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver(){

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if(action.equals(HomeDialActivity.Set_Notify_Status)){
				
				Message msg = new Message();
				String commString = intent.getStringExtra("command");
				if ( "change".equals(commString) ){
					msg.what = MSG_Change_Show;
					mHandler.sendMessage(msg);
					
				}
			}
		}
		
	};
	
	
	 private Intent getTabItemIntent(int index) {
		Intent intent = new Intent(this, mTabClassArray[index]);
		return intent;
	 }
	 
	 public Class mTabClassArray[] = {
		 		HomeDialActivity.class,
		 		HomeContactActivity.class,
		 		RechargeSelect.class,
		 		SettingActivity.class
		 	};

	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
		case MSG_Change_Show:
			dial_toggle = 0;
			Drawable drawableTop = getResources().getDrawable(R.drawable.icon_tabbar_keybordup);
			main_tab_dialhistory.setCompoundDrawablesWithIntrinsicBounds(null, drawableTop, null, null);
			break;

		default:
			break;
		}
		return false;
	}
	 

}
