package com.pay.telcel.main;

import java.util.List;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class FeedbackActivity extends Activity implements OnClickListener{

	Button send_content, back;
	EditText contentT;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.feedback_layout);
		
		send_content = (Button) findViewById(R.id.send_content);
		back = (Button) findViewById(R.id.back);
		contentT = (EditText) findViewById(R.id.contentT);
		
		back.setOnClickListener(this);
		send_content.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.send_content:
			String content = contentT.getText().toString();
			if ( "".equals(content) ){
				return;
			}
			String mobile = "15120004523";

			SmsManager smsManager = SmsManager.getDefault();
			PendingIntent sentIntent = PendingIntent.getBroadcast(FeedbackActivity.this, 0, new Intent(), 0);

			if (content.length() >= 70) {
				// 短信字数大于70，自动分条
				List<String> ms = smsManager.divideMessage(content);

				for (String str : ms) {
					// 短信发送
					smsManager.sendTextMessage(mobile, null, str, sentIntent,null);
				}
			} else {
				smsManager.sendTextMessage(mobile, null, content, sentIntent,null);
			}

			Toast.makeText(FeedbackActivity.this, "发送成功！", Toast.LENGTH_LONG).show();
			finish();
			break;
			
		case R.id.back:
			finish();
			break;

		default:
			break;
		}
	}

}
