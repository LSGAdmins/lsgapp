package com.lsg.app;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;

public class OverlayHelp extends Activity {
	String action;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setBackgroundDrawableResource(R.color.halfTransparent);
		
		action = getIntent().getAction();
		if (action.equals("homeasuphelp"))
			setContentView(R.layout.homeasuphelp);
		if (action.equals("swipehelp"))
			setContentView(R.layout.swipehelp);
	}
	public void done(View v) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor edit = prefs.edit();
		edit.putBoolean(action + "_shown", true);
		edit.commit();
		finish();
	}
}
