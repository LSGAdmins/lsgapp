package com.lsg.app;

import android.annotation.TargetApi;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.webkit.WebView;
import android.widget.ListView;

import com.lsg.app.interfaces.SQLlist;
import com.lsg.app.interfaces.SelectedCallback;

public class AdvancedWrapper {
	public void dropDownNav(Activity context, int actionArrayRes, SelectedCallback navlistener, int selPosition) {
		Advanced.dropDownNav(context, actionArrayRes, navlistener, selPosition);
	}

	public void setSelectedItem(int position, Activity act) {
		Advanced.setSelectedItem(position, act);
	}

	public void getSelectedItem(Activity act) {
		Advanced.getSelectedItem(act);
	}
	public void searchBar(Menu menu, SQLlist list) {
		Advanced adv = new Advanced();
		adv.searchBarInit(menu, list);
	}

	public void homeasup(Activity act) {
		Advanced.homeasup(act);
	}
	public void alwaysDisplayFastScroll(ListView lv) {
		Advanced.alwaysDisplayFastScroll(lv);
	}
	public void standardNavigation(Activity act) {
		Advanced.standardNavigation(act);
	}
	public void setActionBarCustomView(Activity act, View customActionBarView) {
		Advanced.setActionBarCustomView(act, customActionBarView);
	}
	public void removeActionBarCustomView(Activity act) {
		Advanced.removeActionBarCustomView(act);
	}
	@TargetApi(5)
	public void postUrl(WebView webv, String url, byte[] data) {
		webv.postUrl(url, data);
	}
}
