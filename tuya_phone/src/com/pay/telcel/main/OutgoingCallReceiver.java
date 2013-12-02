package com.pay.telcel.main;

import java.lang.reflect.Method;

import com.android.internal.telephony.ITelephony;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.RemoteException;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;

public class OutgoingCallReceiver extends BroadcastReceiver {

	private String incoming_number = "";
	private ITelephony iTelephony = null;
	private CApplication cAPP;
	private AudioManager mAudioManager;
	
	@Override
	public void onReceive(Context context, Intent intent) {
//		String number = getResultData();
//		Log.i("", "拨打电话: "+ number);
        
		cAPP = (CApplication) context.getApplicationContext();
		mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		
		if (intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
			String phoneNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);         
	        Log.i("", "呼出: "+phoneNumber);
	        
	        if ( cAPP.inner ){
	        	cAPP.inner = false;
	        	
	        }else {
	        	setResultData(null); 
				Intent intentMainActivity = new Intent(context, OutgoingActivity.class);
				intentMainActivity.putExtra("number", phoneNumber);
		        intentMainActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);  
		        context.startActivity(intentMainActivity);
			}
			
            
		} else {
			TelephonyManager tm = (TelephonyManager) context.getSystemService(Service.TELEPHONY_SERVICE);
			switch (tm.getCallState()) {
			case TelephonyManager.CALL_STATE_RINGING:
				incoming_number = intent.getStringExtra("incoming_number");
				Log.i("", "来电 " + incoming_number);
				
				if ( mAudioManager != null && incoming_number == null){
					mAudioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);//静音处理
				}
				
				iTelephony = getITelephony(context); //获取电话接口
				
				//自动接听
				if ( cAPP.inner ){
		        	cAPP.inner = false;
		        	answerRingingCall(context);
		        }

				break;
			case TelephonyManager.CALL_STATE_OFFHOOK:
				if ( mAudioManager != null ){
					mAudioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
				}
				
				Log.i("", "挂断 ");
				break;

			case TelephonyManager.CALL_STATE_IDLE:
				Log.i("", "空闲 ");
				break;
			}
		}
//		
//		
	}
//	
	private ITelephony getITelephony(Context context) {
		TelephonyManager mTelephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		Class<TelephonyManager> c = TelephonyManager.class;
		Method getITelephonyMethod = null;
		try {
			getITelephonyMethod = c.getDeclaredMethod("getITelephony",	(Class[]) null); // 获取声明的方法
			getITelephonyMethod.setAccessible(true);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}

		try {
			iTelephony = (ITelephony) getITelephonyMethod.invoke(mTelephonyManager, (Object[]) null); // 获取实例
			return iTelephony;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return iTelephony;
	}
//	
//	
	public synchronized void answerRingingCall(Context context) { 
        //插耳机
           Intent localIntent1 = new Intent(Intent.ACTION_HEADSET_PLUG);
           localIntent1.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
           localIntent1.putExtra("state", 1);
           localIntent1.putExtra("microphone", 1);
           localIntent1.putExtra("name", "Headset");
           context.sendOrderedBroadcast(localIntent1,
             "android.permission.CALL_PRIVILEGED");
        //按下耳机按钮
           Intent localIntent2 = new Intent(Intent.ACTION_MEDIA_BUTTON);
           KeyEvent localKeyEvent1 = new KeyEvent(KeyEvent.ACTION_DOWN,
             KeyEvent.KEYCODE_HEADSETHOOK);
           localIntent2.putExtra("android.intent.extra.KEY_EVENT",
             localKeyEvent1);
           context.sendOrderedBroadcast(localIntent2,
             "android.permission.CALL_PRIVILEGED");
        //放开耳机按钮
           Intent localIntent3 = new Intent(Intent.ACTION_MEDIA_BUTTON);
           KeyEvent localKeyEvent2 = new KeyEvent(KeyEvent.ACTION_UP,
             KeyEvent.KEYCODE_HEADSETHOOK);
           localIntent3.putExtra("android.intent.extra.KEY_EVENT",
             localKeyEvent2);
           context.sendOrderedBroadcast(localIntent3,
             "android.permission.CALL_PRIVILEGED");
        //拔出耳机
           Intent localIntent4 = new Intent(Intent.ACTION_HEADSET_PLUG);
           localIntent4.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
           localIntent4.putExtra("state", 0);
           localIntent4.putExtra("microphone", 1);
           localIntent4.putExtra("name", "Headset");
           context.sendOrderedBroadcast(localIntent4, "android.permission.CALL_PRIVILEGED");
    }
	

}
