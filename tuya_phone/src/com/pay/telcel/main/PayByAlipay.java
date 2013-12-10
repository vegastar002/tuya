package com.pay.telcel.main;

import java.io.IOException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
import android.app.ProgressDialog;
import android.content.res.XmlResourceParser;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.alipay.android.app.sdk.AliPay;
import com.alipay.android.msp.demo.Keys;
import com.alipay.android.msp.demo.Result;
import com.alipay.android.msp.demo.Rsa;
import com.android.hardcore.crashreport.CrashReportingApplication;

public class PayByAlipay extends Activity implements OnClickListener{

	public static final String TAG = "ExternalPartner";
	private static final int RQF_PAY = 1;
	public final int Msg_Recharge_Finish = 2;
	public final int Msg_Recharge_Failure = 3;
	
	
	public static class Product {
		public String subject;
		public String body;
		public String price;
	}

	public static Product[] sProducts;
	public RelativeLayout recharge_10, recharge_30, recharge_50, recharge_100, name_layout, balance_layout;
	String num = "";
	ProgressDialog pDialog;
	
	CrashReportingApplication cApp;
	Button back;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.pay_by_alipay);
		
		back = (Button) findViewById(R.id.back);
		back.setOnClickListener(this);
		
		cApp = (CrashReportingApplication) getApplication();
		recharge_10 = (RelativeLayout) findViewById(R.id.recharge_10);
		recharge_30 = (RelativeLayout) findViewById(R.id.recharge_30);
		recharge_50 = (RelativeLayout) findViewById(R.id.recharge_50);
		recharge_100 = (RelativeLayout) findViewById(R.id.recharge_100);
		
		recharge_10.setOnClickListener(this);
		recharge_30.setOnClickListener(this);
		recharge_50.setOnClickListener(this);
		recharge_100.setOnClickListener(this);
		
		pDialog = new ProgressDialog(this);
		pDialog.setMessage("请稍候...");
		
		num = getIntent().getStringExtra("phoneNum");
		initProducts();
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
				if (eventType == XmlPullParser.START_TAG && parser.getName().equalsIgnoreCase("product")) {
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
	
	
	public void send121(final int pjs){
		final String host = "http://121.199.3.19:8080/tuya_Serv/p/payForUsers";
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				HttpPost httpRequest = new HttpPost(host);
				List<BasicNameValuePair> Vaparams = new ArrayList<BasicNameValuePair>();
				Vaparams.add(new BasicNameValuePair("command", pjs +"," + num));
				
				try {
					httpRequest.setEntity(new UrlEncodedFormEntity(Vaparams, HTTP.UTF_8));
					HttpResponse httpResponse = new DefaultHttpClient().execute(httpRequest);

					if (httpResponse.getStatusLine().getStatusCode() == 200) {
						String preTel = cApp.retrieveInputStream(httpResponse.getEntity());
						
						Message msg = new Message();
						if ( preTel.equals("success") ){
							msg.what = Msg_Recharge_Finish;
							mHandler.sendMessage(msg);
							
						}else {
							msg.what = Msg_Recharge_Failure;
							mHandler.sendMessage(msg);
						}
						
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			}
		}).start();
	}
	
	
	Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			Result.sResult = (String) msg.obj;
			String str1 = Result.sResult;

			switch (msg.what) {
			case RQF_PAY: {
				String resultStatus = "";
				String src = str1.replace("{", "");
				src = src.replace("}", "");
				String rs = Result.getContent(src, "resultStatus=", ";memo");
				if (Result.sError.containsKey(rs)) {
					resultStatus = Result.sError.get(rs);
				} else {
					resultStatus = "其他错误";
				}
				
				String pjs = "";
				String result = Result.getContent(src, "result=", null);
				try {
					JSONObject json = Result.string2JSON(result, "&");
					pjs = json.getString("total_fee");
					pjs = pjs.substring(1, pjs.length()-1);

				} catch (Exception e) {
					e.printStackTrace();
//					Log.i("Result", "Exception =" + e);
				}
//				Toast.makeText(PayByAlipay.this, resultStatus, Toast.LENGTH_SHORT).show();
				
				if ( Integer.valueOf(rs) == 9000 ){
					pDialog.show();
					send121(Integer.valueOf(pjs));
				}

			}
				break;
				
			case Msg_Recharge_Finish:
				pDialog.dismiss();
				Toast.makeText(PayByAlipay.this, "充值成功", Toast.LENGTH_SHORT).show();
				break;
				
			case Msg_Recharge_Failure:
				pDialog.dismiss();
				Toast.makeText(PayByAlipay.this, "充值失败，请联系服务商", Toast.LENGTH_SHORT).show();
				break;
				
				
			default:
				break;
			}
		};
	};
	
	public void choiceAmount(int position) {
		try {
			String info = getNewOrderInfo(position);
			String sign = Rsa.sign(info, Keys.PRIVATE);
			sign = URLEncoder.encode(sign);
			info += "&sign=\"" + sign + "\"&" + getSignType();
			// start the pay.
			Result.sResult = null;
//			Log.i("", "info = " + info);
			final String orderInfo = info;
			new Thread() {
				public void run() {
					String result = new AliPay(PayByAlipay.this, mHandler).pay(orderInfo);
//					Log.i("", "结果= " + result);
					Message msg = new Message();
					msg.what = RQF_PAY;
					msg.obj = result;
					mHandler.sendMessage(msg);
				}
			}.start();

		} catch (Exception ex) {
			ex.printStackTrace();
			Toast.makeText(PayByAlipay.this, R.string.remote_call_failed, Toast.LENGTH_SHORT).show();
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
//		Log.d("", "outTradeNo: " + key);
		return key;
	}

	private String getSignType() {
		return "sign_type=\"RSA\"";
	}
	
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back:
			finish();
			break;
			
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

		default:
			break;
		}
	}

}
