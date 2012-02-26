package com.lsg.app;

import android.app.ActionBar;
import android.app.Activity;
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
				switch(itemPosition) {
				case 0:
					act.updateCursor(false);
					break;
				case 1:
					act.updateCursor(true);
					break;
					default:
						act.updateCursor(false);
						break;
						}
			return false;
			}
		};
		
		ActionBar actionBar = act.getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		
		actionBar.setListNavigationCallbacks(mSpinnerAdapter, navListener);
	}
}
