package com.lsg.app.interfaces;

import com.lsg.app.lib.TitleCompat;
import com.lsg.lib.slidemenu.SlideMenu;

import android.database.sqlite.SQLiteDatabase;
import android.support.v4.app.Fragment;

public interface FragmentActivityCallbacks {
	public TitleCompat getTitlebar();
	public SlideMenu getSlideMenu();
	public void changeFragment(Class<?extends Fragment> frag);
}
