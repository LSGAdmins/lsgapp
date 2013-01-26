package com.lsg.app.settings;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.view.KeyEvent;

import com.lsg.app.Functions;
import com.lsg.app.R;
import com.lsg.app.lib.TitleCompat;
import com.lsg.app.lib.TitleCompat.HomeCall;
import com.lsg.app.setup.SetupAssistant;
import com.lsg.lib.slidemenu.SlideMenu;

//code for old devices -> deprecated
@SuppressWarnings("deprecation")
public class Settings extends PreferenceActivity implements OnSharedPreferenceChangeListener, HomeCall {
	private TitleCompat titlebar;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		titlebar = new TitleCompat(this, true);
		super.onCreate(savedInstanceState);
		
		Functions.setTheme(false, false, this);
		getWindow().setBackgroundDrawableResource(R.layout.background);
		
        addPreferencesFromResource(R.xml.login_settings);
        addPreferencesFromResource(R.xml.timetable_settings);
        addPreferencesFromResource(R.xml.vplan_settings);
        addPreferencesFromResource(R.xml.general_settings);
        addPreferencesFromResource(R.xml.list_settings);
        
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        int i = 0;
        boolean showonlywhitelist = false;
        while(i < Functions.exclude.length) {
        	if(Functions.exclude[i].equals(prefs.getString(Functions.class_key, "")))
        		showonlywhitelist = true;
        	i++;
        }
    	PreferenceCategory prefCat = (PreferenceCategory) findPreference(getString(R.string.vplan));
        if(!showonlywhitelist) {
        	CheckBoxPreference onlywhitelist = (CheckBoxPreference) findPreference("showonlywhitelist");
        	prefCat.removePreference(onlywhitelist);
        }
        
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
        Preference loginsettings = (Preference) findPreference("loginSettings");
        loginsettings.setOnPreferenceClickListener(new OnPreferenceClickListener() {
        	public boolean onPreferenceClick(Preference preference) {
        				Intent intent = new Intent(Settings.this, SetupAssistant.class);
        				intent.setAction("changeUser");
        				startActivity(intent);
        				return true;
        				}
        	});
        Preference ttsettings = (Preference) findPreference("timetableSettings");
        ttsettings.setOnPreferenceClickListener(new OnPreferenceClickListener() {
        	public boolean onPreferenceClick(Preference preference) {
        				Intent intent = new Intent(Settings.this, SetupAssistant.class);
        				intent.setAction("changeTimeTable");
        				startActivity(intent);
        				return true;
        				}
        	});
        prefs.registerOnSharedPreferenceChangeListener(this);
    	push(!(prefs.getBoolean("autopullvplan", false) || prefs.getBoolean("updatevplanonstart", false)));
    	pull(!prefs.getBoolean("useac2dm", false));
        if(prefs.getBoolean("disableAC2DM", false)) {
        	Preference ac2dm = (Preference) findPreference("useac2dm");
        	prefCat.removePreference(ac2dm);
        }
        titlebar.init(this);
        titlebar.setTitle(getTitle());
	}
    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        if(key.equals("useac2dm")) {
        	boolean useac2dm = prefs.getBoolean("useac2dm", false);
        	pull(!useac2dm);
        	if(useac2dm)
        		Functions.registerGCM(this);
        	else
        		Functions.unregisterGCM(this);
        }
        if((key.equals("updatevplanonstart") || key.equals("autopullvplan")) && !prefs.getBoolean("disableAC2DM", false)) {
        	push(!(prefs.getBoolean("autopullvplan", false) || prefs.getBoolean("updatevplanonstart", false)));
        }
    }
    private void push(boolean enabled) {
    	(findPreference("useac2dm")).setEnabled(enabled);
    }
    private void pull(boolean enabled) {
    	((CheckBoxPreference) findPreference("updatevplanonstart")).setEnabled(enabled);
    	((CheckBoxPreference) findPreference("autopullvplan")).setEnabled(enabled);
    	((EditTextPreference) findPreference("autopull_intervall")).setEnabled(enabled);
    }
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if ((keyCode == KeyEvent.KEYCODE_BACK)) {
	    	Intent intent = new Intent(this, SetupAssistant.class);
	    	intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	    	startActivity(intent);
	        return false;
	    }
	    else
	    	return super.onKeyDown(keyCode, event);
	}
	@Override
	public void onHomePress() {
		finish();
	}
}
