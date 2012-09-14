package com.lsg.app;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class UpdateActivity extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.updatecheck);
		((Button) findViewById(R.id.finish)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		Bundle extras = getIntent().getExtras();
		TextView version = (TextView) findViewById(R.id.versionview);
		version.setText(version.getText() + " " + extras.getString("version"));
		((TextView) findViewById(R.id.changelog)).setText(extras.getString("changelog"));
	}
	public void update(View v) {
		finish();
		if(Functions.getSDK() < 11) {
			String url = "http://www.example.com";
			Intent i = new Intent(Intent.ACTION_VIEW);
			i.setData(Uri.parse(Functions.UPDATE_URL));
			startActivity(i);
		} else {
			Log.d("update", "down");
			Intent intent = new Intent(this, DownloadService.class);
			startService(intent);
		}
	}
}
