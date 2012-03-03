package com.lsg.app;

import android.app.Activity;
import android.view.Menu;

public class AdvancedWrapper {
	public void dropDownNav(VPlan vplan) {
		Advanced.dropDownNav(vplan);
		}
	public void searchBar(Menu menu, SQLlist list) {
		Advanced adv = new Advanced();
		adv.searchBarInit(menu, list);
		}
	public void homeasup(Activity act) {
		Advanced.homeasup(act);
	}
	}
