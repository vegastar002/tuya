package com.pay.telcel.main;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import xu.ye.bean.CallLogBean;
import xu.ye.view.adapter.HomeDialAdapter;
import xu.ye.view.adapter.T9Adapter;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.AsyncQueryHandler;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.provider.CallLog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;

public class HomeDialActivity extends Activity implements OnClickListener , Callback{
	
	private AsyncQueryHandler asyncQuery;
	private HomeDialAdapter adapter;
	private ListView callLogList;
	
	private List<CallLogBean> list;
	
	private LinearLayout bohaopan;
	private LinearLayout keyboard_show_ll;
	private Button keyboard_show;
	
	private Button phone_view;
	private Button delete;
	private AudioManager am = null;
	
	private CApplication application;
	private ListView listView;
	private T9Adapter t9Adapter;
	private Handler mHandler;
	public final int MSG_Toggle_Show = 1;
	public final int MSG_Toggle_Hide = 2;
	public final int MSG_Show_Update_Dialog = 3;
	private final int DOWNLOAD = 4;
	private final int DOWNLOAD_FINISH = 5;
	private final int Msg_Start_DOWNLOAD = 6;
	public String download_filename = "tuya.apk";
	
	public static final String Set_Control_Bohaopan = "set.control.bohaopan.vigone";
	public static final String Set_Notify_Status = "set.change.tab1.status";
	
	
	
	/* 保存解析的XML信息 */
	HashMap<String, String> mHashMap;
	/* 下载保存路径 */
	private String mSavePath;
	/* 记录进度条数量 */
	private int progress;
	/* 是否取消更新 */
	private boolean cancelUpdate = false;

	/* 更新进度条 */
	private ProgressBar mProgress;
	private Dialog mDownloadDialog;
	
	
	
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.home_dial_page);
		
		registerBoradcastReceiver();
		mHandler = new Handler(HomeDialActivity.this);
		application = (CApplication)getApplication();
		listView = (ListView) findViewById(R.id.contact_list);
		
		bohaopan = (LinearLayout) findViewById(R.id.bohaopan);
		keyboard_show_ll = (LinearLayout) findViewById(R.id.keyboard_show_ll);
		keyboard_show = (Button) findViewById(R.id.keyboard_show);
		callLogList = (ListView)findViewById(R.id.call_log_list);
		asyncQuery = new MyAsyncQueryHandler(getContentResolver());
		
		keyboard_show.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				dialPadShow();
			}
		});
		
		am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

		phone_view = (Button) findViewById(R.id.phone_view);
		phone_view.setOnClickListener(this);
		phone_view.addTextChangedListener(new TextWatcher() {
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if(null == application.getContactBeanList() || application.getContactBeanList().size()<1 || "".equals(s.toString())){
					listView.setVisibility(View.INVISIBLE);
					callLogList.setVisibility(View.VISIBLE);
				}else{
					if(null == t9Adapter){
						t9Adapter = new T9Adapter(HomeDialActivity.this);
						t9Adapter.assignment(application.getContactBeanList());
//						TextView tv = new TextView(HomeDialActivity.this);
//						tv.setBackgroundResource(R.drawable.dial_input_bg2);
//						listView.addFooterView(tv);
						listView.setAdapter(t9Adapter);
						listView.setTextFilterEnabled(true);
						listView.setOnScrollListener(new OnScrollListener() {
							public void onScrollStateChanged(AbsListView view, int scrollState) {
								if(scrollState == OnScrollListener.SCROLL_STATE_TOUCH_SCROLL){
									if(bohaopan.getVisibility() == View.VISIBLE){
										bohaopan.setVisibility(View.GONE);
//										keyboard_show_ll.setVisibility(View.VISIBLE);
									}
								}
							}
							public void onScroll(AbsListView view, int firstVisibleItem,
									int visibleItemCount, int totalItemCount) {
							}
						});
					}else{
						callLogList.setVisibility(View.INVISIBLE);
						listView.setVisibility(View.VISIBLE);
						t9Adapter.getFilter().filter(s);
					}
				}
			}
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
			public void afterTextChanged(Editable s) {
			}
		});
		
		delete = (Button) findViewById(R.id.delete);
		delete.setOnClickListener(this);
		delete.setOnLongClickListener(new OnLongClickListener() {
			public boolean onLongClick(View v) {
				phone_view.setText("");
				return false;
			}
		});
		
		for (int i = 0; i < 12; i++) {
			View v = findViewById(R.id.dialNum1 + i);
			v.setOnClickListener(this);
		}
		
		init();
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				HttpPost httpRequest = new HttpPost("http://121.199.3.19:8080/tuya_Serv/p/theLastVersion");
				List<BasicNameValuePair> Vaparams = new ArrayList<BasicNameValuePair>();
				try {
					httpRequest.setEntity(new UrlEncodedFormEntity(Vaparams, HTTP.UTF_8));
					HttpResponse httpResponse = new DefaultHttpClient().execute(httpRequest);

					if (httpResponse.getStatusLine().getStatusCode() == 200) {
						String newVersion = application.retrieveInputStream(httpResponse.getEntity());
						
						int curVersion = getVersionCode(HomeDialActivity.this);
						
						if (Integer.valueOf(newVersion) > curVersion){
							Message msg = new Message();
							msg.what = MSG_Show_Update_Dialog;
							mHandler.sendMessage(msg);
						}
//						
//						if ( preTel.equals("{0}") ){
//							msg.what = BACK_SUCCESS;
//							mHandler.sendMessage(msg);
//						}else {
//							preTel = preTel.substring(1, preTel.length()-1);
//							msg.obj = preTel;
//							msg.what = BACK_FAILURE;
//							mHandler.sendMessage(msg);
//						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
		
	}
	
	
	public int getVersionCode(Context context)
	{
		int versionCode = 0;
		try
		{
			// 获取软件版本号，对应AndroidManifest.xml下android:versionCode
			versionCode = context.getPackageManager().getPackageInfo("com.pay.telcel.main", 0).versionCode;
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		return versionCode;
	}
	
	
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		unregisterReceiver(mBroadcastReceiver);
	}


	public void registerBoradcastReceiver(){ 
        IntentFilter myIntentFilter = new IntentFilter(); 
        myIntentFilter.addAction(Set_Control_Bohaopan); 
        //注册广播       
        registerReceiver(mBroadcastReceiver, myIntentFilter); 
    }
	
	BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver(){

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if(action.equals(Set_Control_Bohaopan)){
				
				Message msg = new Message();
				String commString = intent.getStringExtra("show");
				if ( "yes".equals(commString) ){
					msg.what = MSG_Toggle_Show;
					mHandler.sendMessage(msg);
					
				}else if ( "no".equals(commString) ) {
					msg.what = MSG_Toggle_Hide;
					mHandler.sendMessage(msg);
				}
			}
		}
		
	};
	
	
	
	private void init(){
		Uri uri = CallLog.Calls.CONTENT_URI;
		
		String[] projection = { 
				CallLog.Calls.DATE,
				CallLog.Calls.NUMBER,
				CallLog.Calls.TYPE,
				CallLog.Calls.CACHED_NAME,
				CallLog.Calls._ID
		}; // 查询的列
		asyncQuery.startQuery(0, null, uri, projection, null, null, CallLog.Calls.DEFAULT_SORT_ORDER);  
	}
	

	private class MyAsyncQueryHandler extends AsyncQueryHandler {

		public MyAsyncQueryHandler(ContentResolver cr) {
			super(cr);
		}
		protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
			if (cursor != null && cursor.getCount() > 0) {
				list = new ArrayList<CallLogBean>();
				SimpleDateFormat sfd = new SimpleDateFormat("MM-dd hh:mm");
				Date date;
				cursor.moveToFirst();
				for (int i = 0; i < cursor.getCount(); i++) {
					cursor.moveToPosition(i);
					date = new Date(cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DATE)));
//					String date = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DATE));
					String number = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER));
					int type = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.TYPE));
					String cachedName = cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NAME));//缓存的名称与电话号码，如果它的存在
					int id = cursor.getInt(cursor.getColumnIndex(CallLog.Calls._ID));

					CallLogBean clb = new CallLogBean();
					clb.setId(id);
					clb.setNumber(number);
					clb.setName(cachedName);
					if(null == cachedName || "".equals(cachedName)){
						clb.setName(number);
					}
					clb.setType(type);
					clb.setDate(sfd.format(date));
					
					list.add(clb);
				}
				if (list.size() > 0) {
					setAdapter(list);
				}
			}
		}

	}


	private void setAdapter(List<CallLogBean> list) {
		adapter = new HomeDialAdapter(this, list);
//		TextView tv = new TextView(this);
//		tv.setBackgroundResource(R.drawable.dial_input_bg2);
//		callLogList.addFooterView(tv);
		callLogList.setAdapter(adapter);
		callLogList.setOnScrollListener(new OnScrollListener() {

			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if(scrollState == OnScrollListener.SCROLL_STATE_TOUCH_SCROLL){
					if(bohaopan.getVisibility() == View.VISIBLE){
						Intent mIntent = new Intent(Set_Notify_Status);
				        mIntent.putExtra("command", "change");
				        sendBroadcast(mIntent);
				        
						bohaopan.setVisibility(View.GONE);
//						keyboard_show_ll.setVisibility(View.VISIBLE);
					}
				}
			}
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
			}
		});
		callLogList.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				
				
			}
		});
	}
	
	
	
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.dialNum0:
			if (phone_view.getText().length() < 12) {
				input(v.getTag().toString());
			}
			break;
		case R.id.dialNum1:
			if (phone_view.getText().length() < 12) {
				input(v.getTag().toString());
			}
			break;
		case R.id.dialNum2:
			if (phone_view.getText().length() < 12) {
				input(v.getTag().toString());
			}
			break;
		case R.id.dialNum3:
			if (phone_view.getText().length() < 12) {
				input(v.getTag().toString());
			}
			break;
		case R.id.dialNum4:
			if (phone_view.getText().length() < 12) {
				input(v.getTag().toString());
			}
			break;
		case R.id.dialNum5:
			if (phone_view.getText().length() < 12) {
				input(v.getTag().toString());
			}
			break;
		case R.id.dialNum6:
			if (phone_view.getText().length() < 12) {
				input(v.getTag().toString());
			}
			break;
		case R.id.dialNum7:
			if (phone_view.getText().length() < 12) {
				input(v.getTag().toString());
			}
			break;
		case R.id.dialNum8:
			if (phone_view.getText().length() < 12) {
				input(v.getTag().toString());
			}
			break;
		case R.id.dialNum9:
			if (phone_view.getText().length() < 12) {
				input(v.getTag().toString());
			}
			break;
		case R.id.dialx:
			if (phone_view.getText().length() < 12) {
				input(v.getTag().toString());
			}
			break;
		case R.id.dialj:
			if (phone_view.getText().length() < 12) {
				input(v.getTag().toString());
			}
			break;
		case R.id.delete:
			delete();
			break;
		case R.id.phone_view:
			if (phone_view.getText().toString().length() >= 4) {
				Intent intentMainActivity = new Intent(HomeDialActivity.this, OutgoingActivity.class);
				intentMainActivity.putExtra("number", phone_view.getText().toString());
		        intentMainActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);  
		        startActivity(intentMainActivity);
		        
		        phone_view.setText("");
			}
			break;
		default:
			break;
		}
	}
	
	private void input(String str) {
		String p = phone_view.getText().toString();
		phone_view.setText(p + str);
	}
	
	private void delete() {
		String p = phone_view.getText().toString();
		if(p.length()>0){
			phone_view.setText(p.substring(0, p.length()-1));
		}
	}
	
	private void call(String phone) {
		Uri uri = Uri.parse("tel:" + phone);
		Intent it = new Intent(Intent.ACTION_CALL, uri);
		startActivity(it);
	}
	
	public void dialPadShow(){
		if(bohaopan.getVisibility() == View.VISIBLE){
			bohaopan.setVisibility(View.GONE);
			keyboard_show_ll.setVisibility(View.VISIBLE);
		}else{
			bohaopan.setVisibility(View.VISIBLE);
			keyboard_show_ll.setVisibility(View.INVISIBLE);
		}
	}

	
	private void showDownloadDialog(){
		// 构造软件下载对话框
		AlertDialog.Builder builder = new Builder(HomeDialActivity.this);
		builder.setTitle(R.string.soft_updating);
		// 给下载对话框增加进度条
		final LayoutInflater inflater = LayoutInflater.from(HomeDialActivity.this);
		View v = inflater.inflate(R.layout.softupdate_progress, null);
		mProgress = (ProgressBar) v.findViewById(R.id.update_progress);
		builder.setView(v);
		// 取消更新
		builder.setNegativeButton(R.string.soft_update_cancel, new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				dialog.dismiss();
				// 设置取消状态
				cancelUpdate = true;
			}
		});
		
//		builder.show();
		mDownloadDialog = builder.create();
		mDownloadDialog.show();
		// 现在文件
		downloadApk();
	}
	
	
	private void downloadApk(){
		// 启动新线程下载软件
		new downloadApkThread().start();
	}
	
	
	private class downloadApkThread extends Thread
	{
		@Override
		public void run()
		{
			try
			{
				// 判断SD卡是否存在，并且是否具有读写权限
				if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
				{
					// 获得存储卡的路径
					String sdpath = Environment.getExternalStorageDirectory() + "/";
					mSavePath = sdpath + "download";
					URL url = new URL("http://oss.aliyuncs.com/bitmapfun/new version/tuya.apk");
					// 创建连接
					HttpURLConnection conn = (HttpURLConnection) url.openConnection();
					conn.connect();
					// 获取文件大小
					int length = conn.getContentLength();
					// 创建输入流
					InputStream is = conn.getInputStream();

					File file = new File(mSavePath);
					// 判断文件目录是否存在
					if (!file.exists())
					{
						file.mkdir();
					}
					File apkFile = new File(mSavePath, download_filename);
					FileOutputStream fos = new FileOutputStream(apkFile);
					int count = 0;
					// 缓存
					byte buf[] = new byte[1024];
					// 写入到文件中
					do
					{
						int numread = is.read(buf);
						count += numread;
						// 计算进度条位置
						progress = (int) (((float) count / length) * 100);
						// 更新进度
						mHandler.sendEmptyMessage(DOWNLOAD);
						if (numread <= 0)
						{
							// 下载完成
							mHandler.sendEmptyMessage(DOWNLOAD_FINISH);
							break;
						}
						// 写入文件
						fos.write(buf, 0, numread);
					} while (!cancelUpdate);// 点击取消就停止下载.
					fos.close();
					is.close();
				}
			} catch (MalformedURLException e)
			{
				e.printStackTrace();
			} catch (IOException e)
			{
				e.printStackTrace();
			}
			// 取消下载对话框显示
			mDownloadDialog.dismiss();
		}
	};
	
	
	private void showNoticeDialog(){
		// 构造对话框
		AlertDialog.Builder builder = new Builder(HomeDialActivity.this);
		builder.setTitle(R.string.soft_update_title);
		builder.setMessage(R.string.soft_update_info);
		// 更新
		builder.setPositiveButton(R.string.soft_update_updatebtn, new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				dialog.dismiss();
				// 显示下载对话框
				Message msg = new Message();
				msg.what = Msg_Start_DOWNLOAD;
				mHandler.sendMessage(msg);
				
			}
		});
		// 稍后更新
		builder.setNegativeButton(R.string.soft_update_later, new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				dialog.dismiss();
			}
		});
		Dialog noticeDialog = builder.create();
		noticeDialog.show();
	}
	
	
	private void installApk(){
		File apkfile = new File(mSavePath, download_filename);
		if (!apkfile.exists())
		{
			return;
		}
		// 通过Intent安装APK文件
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setDataAndType(Uri.parse("file://" + apkfile.toString()), "application/vnd.android.package-archive");
		startActivity(i);
	}
	
	

	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
		case MSG_Toggle_Show:
			bohaopan.setVisibility(View.VISIBLE);
			break;
			
		case MSG_Toggle_Hide:
			bohaopan.setVisibility(View.GONE);
			break;
			
		case MSG_Show_Update_Dialog:
			showNoticeDialog();
			break;
			
		case DOWNLOAD:
			// 设置进度条位置
			mProgress.setProgress(progress);
			break;
		case DOWNLOAD_FINISH:
			// 安装文件
			installApk();
			break;
			
		case Msg_Start_DOWNLOAD:
			showDownloadDialog();
			break;

		default:
			break;
		}
		return false;
	}
	
	
	
}
