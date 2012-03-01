package com.lsg.app;

import android.app.ListActivity;
import android.os.Bundle;
import android.util.Log;

public class BlackWhiteList extends ListActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Functions.setTheme(false, true, this);
		
		Bundle data = getIntent().getExtras();
		String type = data.getString(Functions.BLACKWHITELIST);
		if(type.equals(Functions.BLACKLIST))
			getWindow().setTitle(getString(R.string.blacklist));
		if(type.equals(Functions.WHITELIST))
			getWindow().setTitle(getString(R.string.whitelist));
		Log.d("asdf", type);
	}
}
