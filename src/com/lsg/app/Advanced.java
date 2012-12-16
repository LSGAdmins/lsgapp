package com.lsg.app;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ListActivity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.widget.ArrayAdapter;
import android.widget.SearchView;
import android.widget.SpinnerAdapter;

import com.lsg.app.interfaces.SQLlist;
import com.lsg.app.interfaces.SelectedCallback;

@TargetApi(11)
public class Advanced implements SearchView.OnQueryTextListener {
	private SQLlist list;
	public static void homeasup(Activity act) {
	    ActionBar actionBar = act.getActionBar();
	    actionBar.setDisplayHomeAsUpEnabled(true);
	}
	public static void setSelectedItem(int selected, Activity act) {
		ActionBar bar = act.getActionBar();
		bar.setSelectedNavigationItem(selected);
	}
	public static int getSelectedItem(Activity act) {
		ActionBar bar = act.getActionBar();
		return bar.getSelectedNavigationIndex();
	}
	public static void alwaysDisplayFastScroll(ListActivity act) {
		act.getListView().setFastScrollAlwaysVisible(true);
	}
	public static void dropDownNav(Activity context, int actionArrayRes, final SelectedCallback navlistener, int selPosition) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		int spinner = (!prefs.getBoolean("dark_actionbar", false)) ? android.R.layout.simple_spinner_dropdown_item : R.layout.spinner_dropdown_black_actionbar;
		SpinnerAdapter mSpinnerAdapter = ArrayAdapter.createFromResource(context, actionArrayRes, spinner);
		ActionBar.OnNavigationListener navListener = new ActionBar.OnNavigationListener() {
			@Override
			public boolean onNavigationItemSelected(int itemPosition, long itemId) {
				return navlistener.selected(itemPosition, itemId);
			}
		};
		ActionBar actionBar = context.getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		actionBar.setListNavigationCallbacks(mSpinnerAdapter, navListener);
		actionBar.setSelectedNavigationItem(selPosition);
	}
	public void searchBarInit(Menu menu, final SQLlist list) {
		this.list = list;
	    SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
	    searchView.setOnQueryTextListener(this);
	}
	public boolean onQueryTextChange(String text) {
		list.updateWhereCond(text);
		list.updateList();
		return true;
	}
	public boolean onQueryTextSubmit(String text) {
		return true;
	}
}
