package com.pay.telcel.main;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;

import com.szy.update.UpdateManager;

public class AboutActivity extends Activity {

	Button check_version;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.about_layout);
		
		check_version = (Button) findViewById(R.id.check_version);
		check_version.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view) {
				UpdateManager manager = new UpdateManager(AboutActivity.this);
				manager.isUpdate("http://oss.aliyuncs.com/bitmapfun/version.xml");
				// 检查软件更新
//				manager.checkUpdate();
			}
		});
	}

}
