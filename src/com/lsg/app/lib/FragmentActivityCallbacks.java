package com.lsg.app.lib;

import android.database.sqlite.SQLiteDatabase;
import android.support.v4.app.Fragment;

public interface FragmentActivityCallbacks {
	public TitleCompat getTitlebar();
	public SlideMenu getSlideMenu();
	public void changeFragment(Class<?extends Fragment> frag);
	public SQLiteDatabase getDB();
}
