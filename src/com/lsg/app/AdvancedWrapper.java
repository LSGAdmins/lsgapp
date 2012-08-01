package com.lsg.app;

import android.annotation.TargetApi;
import android.app.Activity;
import android.view.Menu;
import android.webkit.WebView;

public class AdvancedWrapper {
	public void dropDownNav(Activity context, int actionArrayRes) {
		Advanced.dropDownNav(context, actionArrayRes);
	}

	public void selectedItem(int position, SetupAssistant act) {
		Advanced.selectedItem(position, act);
	}

	public void searchBar(Menu menu, SQLlist list) {
		Advanced adv = new Advanced();
		adv.searchBarInit(menu, list);
	}

	public void homeasup(Activity act) {
		Advanced.homeasup(act);
	}
	@TargetApi(5)
	public void postUrl(WebView webv, String url, byte[] data) {
		webv.postUrl(url, data);
	}
}
