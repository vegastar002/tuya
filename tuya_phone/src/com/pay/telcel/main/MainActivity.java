package com.pay.telcel.main;

import java.io.IOException;
import java.net.URLEncoder;
import java.text.BreakIterator;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.HttpResponse;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnKeyListener;
import android.content.SharedPreferences.Editor;
import android.content.res.XmlResourceParser;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alipay.android.app.sdk.AliPay;
import com.alipay.android.msp.demo.Keys;
import com.alipay.android.msp.demo.Result;
import com.alipay.android.msp.demo.Rsa;

public class MainActivity extends Activity implements OnClickListener{

	public RelativeLayout recharge_10, recharge_30, recharge_50, recharge_100, name_layout, balance_layout;
	private static final int RQF_PAY = 1;
	private static final int BACK_SUCCESS = 2;
	private static final int BACK_FAILURE = 3;
	private static final int BACK_Account_Exited = 4;
	private static final int BACK_BalanceNoPwd_Success = 5;
	private static final int BACK_Update_Balance = 6;
	public Button register;
	public ImageView balance_fresh;
	CApplication cApp;
	ProgressDialog pDialog;
	TextView account, balance;
	String regPhoneNumber = "";
	Timer timer = new Timer();
	private SharedPreferences perference;
	
	
	
	public static class Product {
		public String subject;
		public String body;
		public String price;
	}

	public static Product[] sProducts;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		
		cApp = (CApplication) getApplication();
		recharge_10 = (RelativeLayout) findViewById(R.id.recharge_10);
		recharge_30 = (RelativeLayout) findViewById(R.id.recharge_30);
		recharge_50 = (RelativeLayout) findViewById(R.id.recharge_50);
		recharge_100 = (RelativeLayout) findViewById(R.id.recharge_100);
		name_layout = (RelativeLayout) findViewById(R.id.name_layout);
		balance_layout = (RelativeLayout) findViewById(R.id.balance_layout);
		register = (Button) findViewById(R.id.register);
		balance_fresh = (ImageView) findViewById(R.id.balance_fresh);
		account = (TextView) findViewById(R.id.account);
		balance = (TextView) findViewById(R.id.balance);
		
		recharge_10.setOnClickListener(this);
		recharge_30.setOnClickListener(this);
		recharge_50.setOnClickListener(this);
		recharge_100.setOnClickListener(this);
		name_layout.setOnClickListener(this);
		register.setOnClickListener(this);
		balance_fresh.setOnClickListener(this);
		balance_layout.setOnClickListener(this);
		
		pDialog = new ProgressDialog(this);
		pDialog.setMessage("请稍候...");
		
		
		initProducts();
		
		perference = getSharedPreferences("user_profile", Context.MODE_PRIVATE);
		if ( !perference.getString("account", "").equals("")  &&   !perference.getString("password", "").equals("")) {
			account.setText(perference.getString("account", ""));
			balance.setText(perference.getInt("balance", 0) + " 元");
		}
		
	}

	private void initProducts() {
		if (sProducts != null)
			return;

		XmlResourceParser parser = getResources().getXml(R.xml.products);
		ArrayList<Product> products = new ArrayList<Product>();
		Product product = null;

		try {
			int eventType = parser.getEventType();

			while (eventType != XmlPullParser.END_DOCUMENT) {
				if (eventType == XmlPullParser.START_TAG
						&& parser.getName().equalsIgnoreCase("product")) {
					product = new Product();
					product.subject = parser.getAttributeValue(0);
					product.body = parser.getAttributeValue(1);
					product.price = parser.getAttributeValue(2);
					products.add(product);
				}
				eventType = parser.next();
			}

			sProducts = new Product[products.size()];
			products.toArray(sProducts);

		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {

			switch (msg.what) {
			case RQF_PAY: {
				Result.sResult = (String) msg.obj;
				Toast.makeText(MainActivity.this, Result.getResult(), Toast.LENGTH_SHORT).show();
			}
				break;
				
			case BACK_SUCCESS:
				pDialog.dismiss();
				account.setText(regPhoneNumber);
				balance.setText("1 元");
				Toast.makeText(MainActivity.this, "注册成功", Toast.LENGTH_SHORT).show();
				
//				SharedPreferences perference = getSharedPreferences("user_profile", Context.MODE_PRIVATE);
				Editor editor = perference.edit();
				editor.putString("account", regPhoneNumber);
				editor.putString("password", "123456");
				editor.putString("balance", "1 元");
				editor.commit();
				break;
				
			case BACK_FAILURE:
				pDialog.dismiss();
				String feedb = msg.obj.toString();
				if ( "failure".equals(feedb) ){
					Toast.makeText(MainActivity.this, "失败,请重试", Toast.LENGTH_SHORT).show();
				}else {
					Toast.makeText(MainActivity.this, feedb, Toast.LENGTH_SHORT).show();
				}
				break;
				
			case BACK_Account_Exited:
				pDialog.dismiss();
				final String regNumber = msg.obj.toString();
				AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
				builder.setTitle("注册");
				builder.setMessage("用户不存在,是否现在注册?(注册即送1元)");
				builder.setPositiveButton(getString(android.R.string.yes), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						final String host = "http://da.bigo.me/interface/acctwap/t/3/mobile/" + regNumber + "/pwd/123456/acctid/10672";
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
//										Log.i("", preTel);
										
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
				});
				builder.setNegativeButton(getString(android.R.string.no), null);
				builder.show();
				
				break;
				
			case BACK_BalanceNoPwd_Success:
				String balStr = msg.obj.toString();
				pDialog.dismiss();
				account.setText(regPhoneNumber);
				balance.setText(balStr + " 元");
				
				Editor editor2 = perference.edit();
				editor2.putString("account", regPhoneNumber);
				editor2.putString("password", "123456");
				editor2.putInt("balance", Integer.valueOf(balStr));
				editor2.commit();
				break;
				
			case BACK_Update_Balance:
				String balStr2 = msg.obj.toString();
				pDialog.dismiss();
				balance.setText(balStr2 + " 元");
				
				Editor editor3 = perference.edit();
				editor3.putInt("balance", Integer.valueOf(balStr2));
				editor3.commit();
				break;
				
				
			default:
				break;
			}
		};
	};
	
	
	//选择面值
	public void choiceAmount(int position) {
		try {
			String info = getNewOrderInfo(position);
			String sign = Rsa.sign(info, Keys.PRIVATE);
			sign = URLEncoder.encode(sign);
			info += "&sign=\"" + sign + "\"&" + getSignType();
			// start the pay.
			Result.sResult = null;
			Log.i("", "info = " + info);
			final String orderInfo = info;
			new Thread() {
				public void run() {
					String result = new AliPay(MainActivity.this, mHandler).pay(orderInfo);
					Log.i("", "结果= " + result);
					Message msg = new Message();
					msg.what = RQF_PAY;
					msg.obj = result;
					mHandler.sendMessage(msg);
				}
			}.start();

		} catch (Exception ex) {
			ex.printStackTrace();
			Toast.makeText(MainActivity.this, R.string.remote_call_failed, Toast.LENGTH_SHORT).show();
		}
	}
	
	
	private String getNewOrderInfo(int position) {
		StringBuilder sb = new StringBuilder();
		sb.append("partner=\"");
		sb.append(Keys.DEFAULT_PARTNER);
		sb.append("\"&out_trade_no=\"");
		sb.append(getOutTradeNo());
		sb.append("\"&subject=\"");
		sb.append(sProducts[position].subject);
		sb.append("\"&body=\"");
		sb.append(sProducts[position].body);
		sb.append("\"&total_fee=\"");
		sb.append(sProducts[position].price.replace("RMB:", ""));
		sb.append("\"&notify_url=\"");

		// 网址需要做URL编码
		sb.append(URLEncoder.encode("http://121.199.3.19:8080/webpay/notify_url.jsp"));//http://notify.java.jpxx.org/index.jsp
		sb.append("\"&service=\"mobile.securitypay.pay");
		sb.append("\"&_input_charset=\"UTF-8");
		sb.append("\"&return_url=\"");
		sb.append(URLEncoder.encode("http://m.alipay.com"));
		sb.append("\"&payment_type=\"1");
		sb.append("\"&seller_id=\"");
		sb.append(Keys.DEFAULT_SELLER);

		// 如果show_url值为空，可不传
		// sb.append("\"&show_url=\"");
		sb.append("\"&it_b_pay=\"1m");
		sb.append("\"");

		return new String(sb);
	}
	
	
	private String getOutTradeNo() {
		SimpleDateFormat format = new SimpleDateFormat("MMddHHmmss");
		Date date = new Date();
		String key = format.format(date);

		java.util.Random r = new java.util.Random();
		key += r.nextInt();
		key = key.substring(0, 15);
		Log.d("", "outTradeNo: " + key);
		return key;
	}

	private String getSignType() {
		return "sign_type=\"RSA\"";
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.recharge_10:
			choiceAmount(0);
			break;
			
		case R.id.recharge_30:
			choiceAmount(1);
			break;
			
		case R.id.recharge_50:
			choiceAmount(2);
			break;
			
		case R.id.recharge_100:
			choiceAmount(3);
			break;
			
		case R.id.register:
			final EditText et1 = new EditText(this);
			AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
			builder.setTitle("请输入你的手机号");
			builder.setMessage("注册即送1元话费");
			builder.setView(et1);
			builder.setPositiveButton(getString(android.R.string.yes), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					regPhoneNumber = et1.getText().toString();
					if ( "".equals(regPhoneNumber) ){
						Toast.makeText(MainActivity.this, "手机号码不能为空", Toast.LENGTH_SHORT).show();
						return;
					}
					final String host = "http://da.bigo.me/interface/acctwap/t/3/mobile/" + regPhoneNumber + "/pwd/123456/acctid/10672";
					pDialog.show();
					
					timer.schedule(new TimerTask() {
						@Override
						public void run() {
							InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
							imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
						}
					}, 500);
					
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
//									Log.i("", preTel);
									
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
			});
			builder.setNegativeButton(getString(android.R.string.no), null);
			builder.show();
			
			break;
			
		case R.id.balance_fresh:
			Log.i("", "haha");
			break;
			
		case R.id.name_layout:
			//登录
			if ( !perference.getString("account", "").equals("")  ||   !perference.getString("password", "").equals("")) {
				return;
			}
			
			final EditText et2 = new EditText(this);
			AlertDialog.Builder builder2 = new AlertDialog.Builder(MainActivity.this);
			builder2.setMessage("请输入你的手机号");
			builder2.setView(et2);
			builder2.setPositiveButton(getString(android.R.string.yes), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					regPhoneNumber = et2.getText().toString();
					if ( "".equals(regPhoneNumber) ){
						Toast.makeText(MainActivity.this, "手机号码不能为空", Toast.LENGTH_SHORT).show();
						return;
					}
					
					//查询此手机用户
					final String host = "http://da.bigo.me/interface/BalanceNoPwd/mobile/" + regPhoneNumber;
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
//									Log.i("", preTel);
									
									JSONObject json = new JSONObject(preTel);
									JSONObject userinfo = json.getJSONObject("userinfo");
									int balance = userinfo.getInt("balance");
									int errcode = json.getInt("errcode");
									if ( errcode == 3 ){
										//用户不存在
										Message msg = new Message();
										msg.what = BACK_Account_Exited;
										msg.obj = regPhoneNumber;
										mHandler.sendMessage(msg);
									}else if ( errcode == 0 ) {
										//成功
										Message msg = new Message();
										msg.what = BACK_BalanceNoPwd_Success;
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
			});
			builder2.setNegativeButton(getString(android.R.string.no), new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					pDialog.dismiss();
				}
			});
			builder2.show();
			break;
			
		case R.id.balance_layout:
			String accID = "";
			if ( !"".equals(perference.getString("account", "")) ) {
				accID = perference.getString("account", "");
			}else {
				return;
			}
			Log.i("", "查询账户: " + accID);
			
			final String host = "http://da.bigo.me/interface/BalanceNoPwd/mobile/" + accID;
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
//							Log.i("", preTel);
							
							JSONObject json = new JSONObject(preTel);
							JSONObject userinfo = json.getJSONObject("userinfo");
							int balance = userinfo.getInt("balance");
							int errcode = json.getInt("errcode");
							if ( errcode == 0 ){
								//成功
								Message msg = new Message();
								msg.what = BACK_Update_Balance;
								msg.obj = balance;
								mHandler.sendMessage(msg);
							}
						}
					} catch (Exception e) {
						
						e.printStackTrace();
					}
					
				}
			}).start();
			
			
			break;

		default:
			break;
		}
	}
	
	private Dialog setNegativeButton(String string, Object object) {
		// TODO Auto-generated method stub
		return null;
	}

	public class SIMCardInfo {  
	    /** 
	     * TelephonyManager提供设备上获取通讯服务信息的入口。 应用程序可以使用这个类方法确定的电信服务商和国家 以及某些类型的用户访问信息。 
	     * 应用程序也可以注册一个监听器到电话收状态的变化。不需要直接实例化这个类 
	     * 使用Context.getSystemService(Context.TELEPHONY_SERVICE)来获取这个类的实例。 
	     */  
	    private TelephonyManager telephonyManager;  
	    /** 
	     * 国际移动用户识别码 
	     */  
	    private String IMSI;  
	  
	    public SIMCardInfo(Context context) {  
	        telephonyManager = (TelephonyManager) context  
	                .getSystemService(Context.TELEPHONY_SERVICE);  
	    }  
	  
	    /** 
	     * Role:获取当前设置的电话号码 
	     * <BR>Date:2012-3-12 
	     * <BR>@author CODYY)peijiangping 
	     */  
	    public String getNativePhoneNumber() {  
	        String NativePhoneNumber=null;  
	        NativePhoneNumber=telephonyManager.getLine1Number();
	        return NativePhoneNumber;  
	    }  
	  
	    /** 
	     * Role:Telecom service providers获取手机服务商信息 <BR> 
	     * 需要加入权限<uses-permission 
	     * android:name="android.permission.READ_PHONE_STATE"/> <BR> 
	     * Date:2012-3-12 <BR> 
	     *  
	     * @author CODYY)peijiangping 
	     */  
	    public String getProvidersName() {  
	        String ProvidersName = null;  
	        // 返回唯一的用户ID;就是这张卡的编号神马的  
	        IMSI = telephonyManager.getSubscriberId();  
	        // IMSI号前面3位460是国家，紧接着后面2位00 02是中国移动，01是中国联通，03是中国电信。  
	        System.out.println(IMSI);  
	        if (IMSI.startsWith("46000") || IMSI.startsWith("46002")) {  
	            ProvidersName = "中国移动";  
	        } else if (IMSI.startsWith("46001")) {  
	            ProvidersName = "中国联通";  
	        } else if (IMSI.startsWith("46003")) {  
	            ProvidersName = "中国电信";  
	        }  
	        return ProvidersName;  
	    }
	}

}
