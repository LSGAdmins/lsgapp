package com.lsg.app;

import android.app.Activity;
import android.view.Menu;
import android.webkit.WebView;

public class AdvancedWrapper {
	public void dropDownNav(lsgapp act) {
		Advanced.dropDownNav(act);
		}
	public void selectedItem(int position, lsgapp act) {
		Advanced.selectedItem(position, act);
	}
	public void searchBar(Menu menu, SQLlist list) {
		Advanced adv = new Advanced();
		adv.searchBarInit(menu, list);
		}
	public void homeasup(Activity act) {
		Advanced.homeasup(act);
	}
	public void postUrl(WebView webv, String url, byte[] data) {
		webv.postUrl(url, data);
	}
	}
