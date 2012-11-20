package com.lsg.app;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.View;

public class OverlayHelp extends Activity {
	String action;
	String[] other;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setBackgroundDrawableResource(R.color.halfTransparent);
		
		other = getIntent().getStringArrayExtra("other");
		
		action = getIntent().getAction();
		if (action.equals(Functions.OVERLAY_HOMEBUTTON))
			setContentView(R.layout.homeasuphelp);
		if (action.equals(Functions.OVERLAY_SWIPE))
			setContentView(R.layout.swipehelp);
	}
	public void done(View v) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor edit = prefs.edit();
		edit.putBoolean(action + "_shown", true);
		edit.commit();
		Functions.checkMessage(this, other);
		finish();
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if ((keyCode == KeyEvent.KEYCODE_BACK)) {
	    	return true;
	    }
	    return super.onKeyDown(keyCode, event);
	}
}
