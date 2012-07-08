package com.lsg.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class SlideMenu {
	public static class SlideMenuAdapter extends ArrayAdapter<SlideMenu.SlideMenuAdapter.MenuDesc> {
		Activity act;
		SlideMenu.SlideMenuAdapter.MenuDesc[] items;
		class MenuItem {
			public TextView label;
			public ImageView icon;
		}
		static class MenuDesc {
			public int icon;
			public String label;
		}
		public SlideMenuAdapter(Activity act, SlideMenu.SlideMenuAdapter.MenuDesc[] items) {
			super(act, R.id.menu_label, items);
			this.act = act;
			this.items = items;
			}
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View rowView = convertView;
			if (rowView == null) {
				LayoutInflater inflater = act.getLayoutInflater();
				rowView = inflater.inflate(R.layout.menu_listitem, null);
				MenuItem viewHolder = new MenuItem();
				viewHolder.label = (TextView) rowView.findViewById(R.id.menu_label);
				viewHolder.icon = (ImageView) rowView.findViewById(R.id.menu_icon);
				rowView.setTag(viewHolder);
			}

			MenuItem holder = (MenuItem) rowView.getTag();
			String s = items[position].label;
			holder.label.setText(s);
			holder.icon.setImageResource(items[position].icon);

			return rowView;
		}
	}
	
	private static boolean menuShown = false;
	private static View menu;
	private static LinearLayout content;
	private static FrameLayout parent;
	private static int menuSize;
	private static int statusHeight = 0;
	private Activity act;
	private static int curAct;
	SlideMenu(Activity act, int curAct) {
		this.act = act;
		this.curAct = curAct;
	}
	public void checkEnabled() {
		if(menuShown)
			this.show(false);
	}
	public void show() {
		if(statusHeight == 0) {
			Rect rectgle = new Rect();
			Window window = act.getWindow();
			window.getDecorView().getWindowVisibleDisplayFrame(rectgle);
			statusHeight = rectgle.top;
			}
		this.show(true);
	}
	public void show(boolean animate) {
    	menuSize = Functions.dpToPx(250, act);
    	content = ((LinearLayout) act.findViewById(android.R.id.content).getParent());
    	FrameLayout.LayoutParams parm = (FrameLayout.LayoutParams) content.getLayoutParams();
    	parm.setMargins(menuSize, 0, -menuSize, 0);
    	content.setLayoutParams(parm);
    	TranslateAnimation ta = new TranslateAnimation(-menuSize, 0, 0, 0);
    	ta.setDuration(500);
    	if(animate)
    		content.startAnimation(ta);
    	parent = (FrameLayout) content.getParent();
    	LayoutInflater inflater = (LayoutInflater) act.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    	menu = inflater.inflate(R.layout.menu, null);
    	FrameLayout.LayoutParams lays = new FrameLayout.LayoutParams(-1, -1, 3);
    	lays.setMargins(0,statusHeight, 0, 0);
    	menu.setLayoutParams(lays);
    	parent.addView(menu);
    	ListView list = (ListView) act.findViewById(R.id.menu_listview);
    	list.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if(position != curAct) {
					hide();
					Intent intent;
					switch(position) {
					case 0:
						intent = new Intent(act, TimeTable.class);
						break;
					case 1:
						intent = new Intent(act, VPlan.class);
						break;
					case 2:
						intent = new Intent(act, Events.class);
						break;
					case 3:
						intent = new Intent(act, SMVBlog.class);
						break;
					case 4:
						if(Functions.getSDK() >= 11)
							intent = new Intent(act, SettingsAdvanced.class);
						else
							intent = new Intent(act, Settings.class);
						break;
					default:
						intent = new Intent(act, TimeTable.class);
						break;
						}
					menuShown = false;
					intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
					act.startActivity(intent);
					} else {
						hide();
					}
				}
			});
    	if(animate)
    		menu.startAnimation(ta);
    	menu.findViewById(R.id.overlay).setOnClickListener(new OnClickListener() {
    		@Override
    		public void onClick(View v) {
    			SlideMenu.this.hide();
    		}
    	});
    	
    	Functions.enableDisableViewGroup((LinearLayout) parent.findViewById(android.R.id.content).getParent(), false);
    	try {    		
    		((ExtendedViewPager) act.findViewById(R.id.viewpager)).setPagingEnabled(false);
    		((ExtendedPagerTabStrip) act.findViewById(R.id.viewpager_tabs)).setNavEnabled(false);
    	} catch(Exception e) {
    		//no viewpager :)
    	}
    	menuShown = true;
    	this.fill();
	}
	public void fill() {
		ListView list = (ListView) act.findViewById(R.id.menu_listview);
		SlideMenuAdapter.MenuDesc[] items = new SlideMenuAdapter.MenuDesc[5];
		for(int i = 0; i < 5; i++) {
			items[i] = new SlideMenuAdapter.MenuDesc();
		}
		items[0].icon = R.drawable.ic_launcher;
		items[0].label = "Stundenplan";
		items[1].icon = R.drawable.ic_launcher;
		items[1].label = "Vertretungsplan";
		items[2].icon = R.drawable.ic_launcher;
		items[2].label = "Termine";
		items[3].icon = R.drawable.ic_launcher;
		items[3].label = "SMVBlog";
		items[4].icon = R.drawable.ic_launcher;
		items[4].label = "Einstellungen";
		SlideMenuAdapter adap = new SlideMenuAdapter(act, items);
		list.setAdapter(adap);
	}
	public void hide() {
		TranslateAnimation ta = new TranslateAnimation(0, -menuSize, 0, 0);
		ta.setDuration(500);
		menu.startAnimation(ta);
		parent.removeView(menu);
		
		TranslateAnimation tra = new TranslateAnimation(menuSize, 0, 0, 0);
		tra.setDuration(500);
		content.startAnimation(tra);
		FrameLayout.LayoutParams parm = (FrameLayout.LayoutParams) content.getLayoutParams();
    	parm.setMargins(0, 0, 0, 0);
    	content.setLayoutParams(parm);
    	Functions.enableDisableViewGroup((LinearLayout) parent.findViewById(android.R.id.content).getParent(), true);
    	try {
    		((ExtendedViewPager) act.findViewById(R.id.viewpager)).setPagingEnabled(true);
    		((ExtendedPagerTabStrip) act.findViewById(R.id.viewpager_tabs)).setNavEnabled(true);
    	} catch(Exception e) {
    		//no viewpager :)
    	}
    	menuShown = false;
	}
}
