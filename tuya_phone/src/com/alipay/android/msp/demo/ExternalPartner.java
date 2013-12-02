package com.alipay.android.msp.demo;

import java.io.IOException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.app.Activity;
import android.content.res.XmlResourceParser;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.alipay.android.app.sdk.AliPay;
import com.pay.telcel.main.R;

public class ExternalPartner extends Activity implements OnItemClickListener {
	public static final String TAG = "ExternalPartner";

	private static final int RQF_PAY = 1;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.external_partner);

		initProducts();
		initListView();
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

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position,long arg3) {
		try {
//			Log.i("ExternalPartner", "onItemClick");
			String info = getNewOrderInfo(position);
			String sign = Rsa.sign(info, Keys.PRIVATE);
			sign = URLEncoder.encode(sign);
			info += "&sign=\"" + sign + "\"&" + getSignType();
			Log.i("ExternalPartner", "start pay");
			// start the pay.
			Result.sResult = null;
			Log.i(TAG, "info = " + info);
			final String orderInfo = info;
			new Thread() {
				public void run() {
					String result = new AliPay(ExternalPartner.this, mHandler).pay(orderInfo);

					Log.i(TAG, "结果= " + result);
					Message msg = new Message();
					msg.what = RQF_PAY;
					msg.obj = result;
					mHandler.sendMessage(msg);
				}
			}.start();

		} catch (Exception ex) {
			ex.printStackTrace();
			Toast.makeText(ExternalPartner.this, R.string.remote_call_failed, Toast.LENGTH_SHORT).show();
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
		sb.append(sProducts[position].price.replace("一口价:", ""));
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
		Log.d(TAG, "outTradeNo: " + key);
		return key;
	}

	private String getSignType() {
		return "sign_type=\"RSA\"";
	}

	private void initListView() {
		ListView lv = (ListView) findViewById(R.id.list_view);
		lv.setAdapter(new ExternalPartnerAdapter());
		lv.setOnItemClickListener(this);
	}

	private class ExternalPartnerAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return sProducts.length;
		}

		@Override
		public Object getItem(int arg0) {
			return sProducts[arg0];
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				LayoutInflater factory = LayoutInflater
						.from(ExternalPartner.this);
				convertView = factory.inflate(R.layout.product_item, null);
			}

			Product product = (Product) getItem(position);
			TextView tv = (TextView) convertView.findViewById(R.id.subject);
			tv.setText(product.subject);

			tv = (TextView) convertView.findViewById(R.id.body);
			tv.setText(product.body);

			tv = (TextView) convertView.findViewById(R.id.price);
			tv.setText(product.price);

			return convertView;
		}

	}

	Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			Result.sResult = (String) msg.obj;

			switch (msg.what) {
			case RQF_PAY: {
				Toast.makeText(ExternalPartner.this, Result.getResult(), Toast.LENGTH_SHORT).show();

			}
				break;
			default:
				break;
			}
		};
	};

	public static class Product {
		public String subject;
		public String body;
		public String price;
	}

	public static Product[] sProducts;
}