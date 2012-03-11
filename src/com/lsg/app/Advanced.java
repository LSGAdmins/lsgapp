package com.lsg.app;

import android.app.ActionBar;
import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.widget.ArrayAdapter;
import android.widget.SearchView;
import android.widget.SpinnerAdapter;

public class Advanced implements SearchView.OnQueryTextListener {
	private SQLlist list;
	public static void homeasup(Activity act) {
	    ActionBar actionBar = act.getActionBar();
	    actionBar.setDisplayHomeAsUpEnabled(true);
	}
	public static void selectedItem(int position, Activity act) {
		ActionBar bar = act.getActionBar();
		bar.setSelectedNavigationItem(position);
	}
	public static void dropDownNav(final VPlan act) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(act);
		int spinner = (!prefs.getBoolean("dark_actionbar", false)) ? android.R.layout.simple_spinner_dropdown_item : R.layout.spinner_dropdown_black_actionbar;
		SpinnerAdapter mSpinnerAdapter = ArrayAdapter.createFromResource(act, R.array.action_vertretungen, spinner);
		ActionBar.OnNavigationListener navListener = new ActionBar.OnNavigationListener() {
			@Override
			public boolean onNavigationItemSelected(int itemPosition, long itemId) {
				switch(itemPosition) {
				case 0:
					act.setPage(0);
					break;
				case 1:
					act.setPage(1);
					break;
					default:
						act.setPage(0);
						break;
						}
			return false;
			}
		};
		
		ActionBar actionBar = act.getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		
		actionBar.setListNavigationCallbacks(mSpinnerAdapter, navListener);
	}
	public void searchBarInit(Menu menu, final SQLlist list) {
		this.list = list;
	    SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
	    searchView.setOnQueryTextListener(this);
	}
	public boolean onQueryTextChange(String text) {
		list.updateWhereCond(text);
		return true;
	}
	public boolean onQueryTextSubmit(String text) {
		return true;
	}
}
