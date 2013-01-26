package com.lsg.app.lib;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SpinnerAdapter;

import com.lsg.app.R;
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

	public static void alwaysDisplayFastScroll(ListView lv) {
		lv.setFastScrollAlwaysVisible(true);
	}

	public static void dropDownNav(Activity context, int actionArrayRes,
			final SelectedCallback navlistener, int selPosition) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		int spinner = (!prefs.getBoolean("dark_actionbar", false)) ? android.R.layout.simple_spinner_dropdown_item
				: R.layout.spinner_dropdown_black_actionbar;
		SpinnerAdapter mSpinnerAdapter = ArrayAdapter.createFromResource(
				context, actionArrayRes, spinner);
		ActionBar.OnNavigationListener navListener = new ActionBar.OnNavigationListener() {
			@Override
			public boolean onNavigationItemSelected(int itemPosition,
					long itemId) {
				return navlistener.selected(itemPosition, itemId);
			}
		};
		ActionBar actionBar = context.getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		actionBar.setListNavigationCallbacks(mSpinnerAdapter, navListener);
		actionBar.setSelectedNavigationItem(selPosition);
	}

	public static void standardNavigation(Activity act) {
		act.getActionBar()
				.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
	}

	public void searchBarInit(Menu menu, final SQLlist list) {
		this.list = list;
		SearchView searchView = (SearchView) menu.findItem(R.id.search)
				.getActionView();
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

	public static void setActionBarCustomView(Activity act,
			View customActionBarView) {
		final ActionBar actionBar = act.getActionBar();
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		actionBar.setCustomView(customActionBarView,
				new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.MATCH_PARENT));
	}

	public static void removeActionBarCustomView(Activity act, boolean homeAsUp) {
		act.getActionBar().setDisplayOptions(0, ActionBar.DISPLAY_SHOW_CUSTOM);
		if (!homeAsUp)
			act.getActionBar().setDisplayOptions(
					ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE);
		else
			act.getActionBar().setDisplayOptions(
					ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE
							| ActionBar.DISPLAY_HOME_AS_UP);
	}

	public static View setMenuActionView(MenuItem item, View actionView) {
		View v = item.getActionView();
		item.setActionView(actionView);
		item.getActionView().setSaveEnabled(false);
		return v;
	}
}