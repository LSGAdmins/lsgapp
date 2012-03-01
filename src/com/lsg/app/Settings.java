package com.lsg.app;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.view.KeyEvent;
import android.widget.Toast;

public class Settings extends PreferenceActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
        
        Preference blacklist = (Preference) findPreference("blacklist");
        blacklist.setOnPreferenceClickListener(new OnPreferenceClickListener() {
        	public boolean onPreferenceClick(Preference preference) {
        				Intent intent = new Intent(Settings.this, BlackWhiteList.class);
        				intent.putExtra(Functions.BLACKWHITELIST, Functions.BLACKLIST);
        				startActivity(intent);
        				return true;
        				}
        	});
        
        Preference whitelist = (Preference) findPreference("whitelist");
        whitelist.setOnPreferenceClickListener(new OnPreferenceClickListener() {
        	public boolean onPreferenceClick(Preference preference) {
        				Intent intent = new Intent(Settings.this, BlackWhiteList.class);
        				intent.putExtra(Functions.BLACKWHITELIST, Functions.WHITELIST);
        				startActivity(intent);
        				return true;
        				}
        	});
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if ((keyCode == KeyEvent.KEYCODE_BACK)) {
	    	Intent intent = new Intent(this, VPlan.class);
	    	intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	    	startActivity(intent);
	        return false;
	    }
	    else
	    	return super.onKeyDown(keyCode, event);
	}
}
