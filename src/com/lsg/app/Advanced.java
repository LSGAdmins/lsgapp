package com.lsg.app;

import android.app.ActionBar;
import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;

public class Advanced {
	public static void homeasup(Activity act) {
	    ActionBar actionBar = act.getActionBar();
	    actionBar.setDisplayHomeAsUpEnabled(true);
	}
	public static void dropDownNav(final VPlan act) {
		SpinnerAdapter mSpinnerAdapter = ArrayAdapter.createFromResource(act, R.array.action_vertretungen,
		          android.R.layout.simple_spinner_dropdown_item);
		ActionBar.OnNavigationListener navListener = new ActionBar.OnNavigationListener() {
			@Override
			public boolean onNavigationItemSelected(int itemPosition, long itemId) {
				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(act);
				String klasse = prefs.getString("class", "");
				String where_cond;
				switch(itemPosition) {
				case 0:
					where_cond = "";
					break;
				case 1:
					where_cond = " WHERE klasse LIKE '%" + klasse + "%' OR klasse LIKE 'null' ";
					break;
					default:
						where_cond = "";
						break;
						}
				Log.d("asdf", where_cond);
				act.updateCursor(where_cond);
			return false;
			}
		};
		
		ActionBar actionBar = act.getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		
		actionBar.setListNavigationCallbacks(mSpinnerAdapter, navListener);
	}
}
