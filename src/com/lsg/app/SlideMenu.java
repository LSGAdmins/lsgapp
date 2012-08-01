package com.lsg.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
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
			public TextView title;
			public ImageView icon;
		}
		static class MenuDesc {
			public int type = Functions.TYPE_PAGE;
			public int icon;
			public String label;
			public String title;
			public Class<?extends Activity> action;
		}
		public SlideMenuAdapter(Activity act, SlideMenu.SlideMenuAdapter.MenuDesc[] items) {
			super(act, R.id.menu_label, items);
			this.act = act;
			this.items = items;
			}
		@Override
        public int getItemViewType(int position) {
            return items[position].type;
        }
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View rowView = convertView;
			if (rowView == null) {
				LayoutInflater inflater = act.getLayoutInflater();
				MenuItem viewHolder = new MenuItem();
				switch(getItemViewType(position)) {
				case Functions.TYPE_PAGE:
					rowView = inflater.inflate(R.layout.menu_listitem, null);
					viewHolder.title = null;
					break;
				case Functions.TYPE_INFO:
					rowView = inflater.inflate(R.layout.menu_info, null);
					viewHolder.title = (TextView) rowView.findViewById(R.id.title);
					break;
				}
				viewHolder.icon = (ImageView) rowView.findViewById(R.id.menu_icon);
				viewHolder.label = (TextView) rowView.findViewById(R.id.menu_label);
				rowView.setTag(viewHolder);
			}

			MenuItem holder = (MenuItem) rowView.getTag();
			String s = items[position].label;
			holder.label.setText(s);
			holder.icon.setImageResource(items[position].icon);
			
			if(holder.title != null) {
				if(items[position].title != null) {
					holder.title.setText(items[position].title);
					holder.title.setVisibility(View.VISIBLE);
				}
				else
					holder.title.setVisibility(View.GONE);
			}

			return rowView;
		}
	}
	
	private static boolean menuShown = false;
	private static boolean menuToHide = false;
	private static View menu;
	private static LinearLayout content;
	private static FrameLayout parent;
	private static int menuSize;
	private static int statusHeight = 0;
	private Activity act;
	private static Class<? extends Activity> curAct;
	private SharedPreferences prefs;
	SlideMenuAdapter.MenuDesc[] items;
	SlideMenu(Activity act, Class<? extends Activity> curAct) {
		this.act = act;
		SlideMenu.curAct = curAct;
		prefs = PreferenceManager.getDefaultSharedPreferences(act);
	}
	public void checkEnabled() {
		if(menuShown)
			this.show(false);
	}/*
	public void checkHide() {
		if(menuToHide) {
			this.hide();
			menuToHide = false;
		}
	}*/
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
    	menu.getViewTreeObserver().addOnGlobalLayoutListener(
				new ViewTreeObserver.OnGlobalLayoutListener() {
					@Override
					public void onGlobalLayout() {
						menu.getViewTreeObserver().removeGlobalOnLayoutListener(this);
						if(menuToHide) {
							hide();
							menuToHide = false;
						}
						}
					});
    	ListView list = (ListView) act.findViewById(R.id.menu_listview);
    	list.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if(!items[position].action.equals(curAct)) {
					Log.d("pos", Long.valueOf(id).toString());
					menuToHide = true;
					Intent intent = new Intent(act, items[position].action);
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
    		//no viewpager to disable :)
    	}
    	menuShown = true;
    	this.fill();
	}
	public void fill() {
		ListView list = (ListView) act.findViewById(R.id.menu_listview);
		if (prefs.getBoolean(Functions.IS_LOGGED_IN, false)) {
			items = new SlideMenuAdapter.MenuDesc[6];
			for (int i = 0; i < 6; i++) {
				items[i] = new SlideMenuAdapter.MenuDesc();
			}
			items[0].icon = R.drawable.ic_launcher;
			items[0].label = "Stundenplan";
			items[0].action = TimeTable.class;
			items[1].icon = R.drawable.ic_launcher;
			items[1].label = "Vertretungsplan";
			items[1].action = VPlan.class;
			items[2].icon = R.drawable.ic_launcher;
			items[2].label = "Termine";
			items[2].action = Events.class;
			items[3].icon = R.drawable.ic_launcher;
			items[3].label = "SMVBlog";
			items[3].action = SMVBlog.class;
			items[4].icon = R.drawable.ic_launcher;
			items[4].label = "Einstellungen";
			items[4].action = (Functions.getSDK() >= 11) ? SettingsAdvanced.class
					: Settings.class;
			items[5].type = Functions.TYPE_INFO;
			items[5].title = "Aktuell";
			items[5].icon = R.drawable.ic_launcher;
			items[5].label = "test";
		} else {

			items = new SlideMenuAdapter.MenuDesc[3];
			for (int i = 0; i < 3; i++) {
				items[i] = new SlideMenuAdapter.MenuDesc();
			}
			items[0].icon = R.drawable.ic_launcher;
			items[0].label = "Setup-Assistent";
			items[0].action = SetupAssistant.class;
			items[1].icon = R.drawable.ic_launcher;
			items[1].label = "Termine";
			items[1].action = Events.class;
			items[2].icon = R.drawable.ic_launcher;
			items[2].label = "SMVBlog";
			items[2].action = SMVBlog.class;/*
			items[4].icon = R.drawable.ic_launcher;
			items[4].label = "Einstellungen";
			items[4].action = (Functions.getSDK() >= 11) ? SettingsAdvanced.class
					: Settings.class;*/
		}
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
