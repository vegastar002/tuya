package com.pay.telcel.main;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Toast;

public class Address_KaMi extends Activity implements OnClickListener, Callback{

	CApplication cApp;
	Button back, submitBtn;
	EditText cardID, cardPW;
	ViewPager viewPager;
	RadioButton directBtn, addressBtn;
	List<View> lists;
	ListView newListAdd;
	ProgressDialog pDialog;
	public WebView webview2;
	Handler mHandler;
	String phoneNum = "";
	final int Msg_Back_Success = 1;
	final int Msg_Back_Failure = 2;
	final int BACK_CHECK_BALANCE = 3;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.address_recharge);
		
		cApp = (CApplication) getApplication();
		mHandler = new Handler(this);
		back = (Button) findViewById(R.id.back);
		directBtn = (RadioButton) findViewById(R.id.direct);
		addressBtn = (RadioButton) findViewById(R.id.address);
		pDialog = new ProgressDialog(this);
		pDialog.setMessage("请稍候...");
		phoneNum = getIntent().getStringExtra("phoneNum");
		
		back.setOnClickListener(this);
		directBtn.setOnClickListener(this);
		addressBtn.setOnClickListener(this);
		
		
		
		viewPager = (ViewPager) findViewById(R.id.address_vPager);
		lists = new ArrayList<View>();
		LayoutInflater mInflater = getLayoutInflater();
		View kamiView = mInflater.inflate(R.layout.dilog_common, null);
		View addressView = mInflater.inflate(R.layout.webview_address, null);
		lists.add(kamiView);
		lists.add(addressView);
		viewPager.setAdapter(new MyPagerAdapter(lists));
		viewPager.setCurrentItem(0);
		viewPager.setOnPageChangeListener(new XTOnPageChangeListener());
		viewPager.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View view, MotionEvent motionevent) {
				// TODO Auto-generated method stub
				return false;
			}
		});
		
		
		submitBtn = (Button) kamiView.findViewById(R.id.submit);
		cardID = (EditText) kamiView.findViewById(R.id.cardID);
		cardPW = (EditText) kamiView.findViewById(R.id.cardPW);
		submitBtn.setOnClickListener(this);
		

		webview2 = (WebView) addressView.findViewById(R.id.webview2);
		WebSettings webSettings = webview2.getSettings();
		webSettings.setDefaultTextEncodingName("utf-8");
		webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
		webview2.loadUrl("http://oss.aliyuncs.com/tuya/address.html");
		
	}

	
	class MyPagerAdapter extends PagerAdapter{
		public List<View> mListViews;

		public MyPagerAdapter(List<View> mListViews) {
			this.mListViews = mListViews;
		}

		@Override
		public void destroyItem(View arg0, int arg1, Object arg2) {
			((ViewPager) arg0).removeView(mListViews.get(arg1));
		}
		
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return mListViews.size();
		}
		
		@Override
		public Object instantiateItem(View arg0, int arg1) {
			((ViewPager) arg0).addView(mListViews.get(arg1), 0);
			return mListViews.get(arg1);
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			// TODO Auto-generated method stub
			return arg0 == (arg1);
		}
		
	}
	
	
	
	class XTOnPageChangeListener implements OnPageChangeListener{

		@Override
		public void onPageScrollStateChanged(int page) {
			
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onPageSelected(int page) {
			switch (page) {
			case 0:
				directBtn.setChecked(true);
				addressBtn.setChecked(false);
				break;
			case 1:
				directBtn.setChecked(false);
				addressBtn.setChecked(true);
				break;
			default:
				break;
			}
		}
		
	}
	
	
	
	public void paybyMiMa(String cardId, String cardPW){
		final String host = "http://da.bigo.me/interface/pay/mobile/" +phoneNum+"/cardid/"+ cardId+"/cardpwd/"+cardPW;
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
	
	
	
	
	public void showDialog() {
		if ( "".equals(cardID.getText().toString()) || "".equals(cardPW.getText().toString()) ){
			Toast.makeText(Address_KaMi.this, "不能为空", Toast.LENGTH_SHORT).show();
		}else {
			paybyMiMa(cardID.getText().toString(), cardPW.getText().toString());
		}
	}
	
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back:
			finish();
			break;
			
		case R.id.direct:
			viewPager.setCurrentItem(0);
			break;
		case R.id.address:
			viewPager.setCurrentItem(1);
			break;
			
		case R.id.submit:
			showDialog();
			break;

		default:
			break;
		}
	}




	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
		case Msg_Back_Success:
			pDialog.dismiss();
			int amount = Integer.valueOf(msg.obj.toString());
			Toast.makeText(Address_KaMi.this, "充值成功，充值"+ amount+ "元", Toast.LENGTH_LONG).show();
			finish();
			break;
			
		case Msg_Back_Failure:
			pDialog.dismiss();
			Toast.makeText(Address_KaMi.this, msg.obj.toString(), Toast.LENGTH_SHORT).show();
			break;

		default:
			break;
		}
		return false;
	}

}
