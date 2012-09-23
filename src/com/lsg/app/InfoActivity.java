package com.lsg.app;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

public class InfoActivity extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle extras = getIntent().getExtras();
		Log.d("type", extras.getString("type"));
		if(extras.getString("type").equals("vplan")) {
			setTitle(R.string.vplan);
			setContentView(R.layout.info_vplan);
			((TextView) findViewById(R.id.vplan_num)).setText(extras.getString("vplan_num"));
			((TextView) findViewById(R.id.mine_num)).setText(extras.getString("mine_num"));
			((TextView) findViewById(R.id.date)).setText(extras.getString("date"));
			((TextView) findViewById(R.id.vplan_num_teachers)).setText(extras.getString("vplan_num_teachers"));
			((TextView) findViewById(R.id.date_teachers)).setText(extras.getString("date_teachers"));
			if(!extras.getBoolean("teacher"))
				(findViewById(R.id.teachers_container)).setVisibility(View.GONE);
		} else if(extras.getString("type").equals("info")) {
			requestWindowFeature(Window.FEATURE_NO_TITLE);
			setContentView(R.layout.news);
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			String text;
			if(extras.getString("info_type").equals("pupils"))
				text = prefs.getString(Functions.NEWS_PUPILS, "");
			else
				text = prefs.getString(Functions.NEWS_TEACHERS, "");
			((TextView) findViewById(R.id.news_content)).setText(text);
		}
	}
}
