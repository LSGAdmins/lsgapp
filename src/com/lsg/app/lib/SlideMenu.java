package com.lsg.app.lib;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.lsg.app.Events;
import com.lsg.app.ExtendedPagerTabStrip;
import com.lsg.app.ExtendedViewPager;
import com.lsg.app.Functions;
import com.lsg.app.HelpAbout;
import com.lsg.app.InfoActivity;
import com.lsg.app.R;
import com.lsg.app.SMVBlog;
import com.lsg.app.Settings;
import com.lsg.app.SettingsAdvanced;
import com.lsg.app.SetupAssistant;
import com.lsg.app.TimeTable;
import com.lsg.app.VPlan;

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
			public boolean useSlideMenu = true;
			public int type = Functions.TYPE_PAGE;
			public int icon;
			public String label;
			public String title;
			public Class<?extends Activity> openActivity;
			public Intent openIntent;
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
			if (rowView == null
					|| (items[position].type == Functions.TYPE_INFO && rowView
							.findViewById(R.id.title) == null)
					|| (items[position].type == Functions.TYPE_PAGE && rowView
							.findViewById(R.id.title) != null)) {
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
	private static LinearLayout contentContainer;
	private static FrameLayout containerParent;
	private static int menuSize;
	private static int statusBarHeight = 0;
	private Activity act;
	private static Class<? extends Activity> curAct;
	private SharedPreferences prefs;
	SlideMenuAdapter.MenuDesc[] items;
	public SlideMenu(Activity act, Class<? extends Activity> curAct) {
		this.act = act;
		SlideMenu.curAct = curAct;
		prefs = PreferenceManager.getDefaultSharedPreferences(act);
    	contentContainer = ((LinearLayout) act.findViewById(android.R.id.content).getParent());
    	(act.findViewById(android.R.id.content)).setBackgroundResource(R.layout.background);
		
		containerParent = (FrameLayout) contentContainer.getParent();
    	LayoutInflater inflater = (LayoutInflater) act.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    	menu = inflater.inflate(R.layout.menu, null);
    	FrameLayout.LayoutParams lays = new FrameLayout.LayoutParams(-1, -1, 3);
    	lays.setMargins(20000, 20000, 0, 0);
    	menu.setLayoutParams(lays);
    	contentContainer.bringToFront();
    	containerParent.addView(menu);
    	menu.findViewById(R.id.overlay).bringToFront();
    	
    	menu.getViewTreeObserver().addOnGlobalLayoutListener(
				new ViewTreeObserver.OnGlobalLayoutListener() {
					@SuppressWarnings("deprecation")
					@Override
					public void onGlobalLayout() {
						menu.getViewTreeObserver().removeGlobalOnLayoutListener(this);
						if(menuToHide) {
							hide();
							menuToHide = false;
						}
						}
					});
    	fill();
		if(menuShown)
			this.show(false);
	}
	public void checkEnabled() {
	}
	public void show() {
		if(statusBarHeight == 0) {
			Rect rectgle = new Rect();
			Window window = act.getWindow();
			window.getDecorView().getWindowVisibleDisplayFrame(rectgle);
			statusBarHeight = rectgle.top;
			}
		this.show(true, 0);
	}

	public void show(boolean animate) {
		show(animate, 0);
	}
	//LayoutParams to move content & menu around
	private FrameLayout.LayoutParams contentContainerLayoutParams;
	private FrameLayout.LayoutParams menuLayoutParams;
	//store motion events
	private float motionStartX;
	private float lastX;
	private float previousX;
	private int maxDiff = 0;
	private int lastDiff;
	public void show(boolean animate, int offset) {
		contentContainer.bringToFront();
    	menuSize = Functions.dpToPx(250, act);
    	if(offset == 0)
    		offset = menuSize;
    	
    	//move content & ActionBar out to right
    	contentContainerLayoutParams = (FrameLayout.LayoutParams) contentContainer.getLayoutParams();
    	contentContainerLayoutParams.setMargins(menuSize, 0, -menuSize, 0);
    	contentContainer.setLayoutParams(contentContainerLayoutParams);
    	
    	//set menu to left side
    	menuLayoutParams = new FrameLayout.LayoutParams(-1, -1, 3);
    	menuLayoutParams.setMargins(0, statusBarHeight, 0, 0);
    	menu.setLayoutParams(menuLayoutParams);
    	
    	//onClick management for menu ListView
    	ListView list = (ListView) act.findViewById(R.id.menu_listview);
    	list.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// if not clicked item for current Activity
				if (items[position].openActivity == null
						|| !items[position].openActivity.equals(curAct)) {
					// mark this menu to be hidden
					if (items[position].useSlideMenu)
						menuToHide = true;
					//start new activity / intent
					if (items[position].openActivity != null) {
						Intent intent = new Intent(act, items[position].openActivity);
						intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
						act.startActivity(intent);
					} else
						act.startActivity(items[position].openIntent);
				} else {
					hide();
				}
			}
		});
    	
    	//disable views in normal content
    	Functions.enableDisableViewGroup((LinearLayout) contentContainer, false);
    	
		if (animate) {
			// slide out content
			TranslateAnimation contentSlideOut = new TranslateAnimation(
					-offset, 0, 0, 0);
			contentSlideOut.setDuration(Math.abs(offset) * 500 / menuSize);
			contentContainer.startAnimation(contentSlideOut);

			// slide in menu
			TranslateAnimation menuSlideIn = new TranslateAnimation(
					-(offset / 2), 0, 0, 0);
			menuSlideIn.setDuration(Math.abs(offset) * 500 / menuSize);
			menu.startAnimation(menuSlideIn);

			menuSlideIn.setAnimationListener(new AnimationListener() {
				@Override
				public void onAnimationEnd(Animation animation) {
					// to enable content overlay view for slide-back
					menu.bringToFront();
				}
				@Override
				public void onAnimationRepeat(Animation animation) {
					// not needed here
				}
				@Override
				public void onAnimationStart(Animation animation) {
					// not needed here
				}
			});
		} else
			menu.bringToFront();
		
		menuShown = true;
    	(menu.findViewById(R.id.overlay)).setOnClickListener(new OnClickListener() {
    		@Override
    		public void onClick(View v) {
    			//need this to get onTouch to work, don't know why
    			Log.d("asdf", "onclick");
    		}
    	});
    	(menu.findViewById(R.id.overlay)).setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO nicer slides
				switch(event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					/*if(event.getX() < menuSize)
						return false;*/
					motionStartX = event.getX();
					contentContainerLayoutParams = (FrameLayout.LayoutParams) contentContainer.getLayoutParams();
					contentContainerLayoutParams.setMargins(0, 0, 0, 0);
					contentContainer.setLayoutParams(contentContainerLayoutParams);
					
					menuLayoutParams = (FrameLayout.LayoutParams) menu.getLayoutParams();
			    	contentContainer.bringToFront();
			    	//contentContainer.scrollBy(menuSize, 0);
					break;
				case MotionEvent.ACTION_MOVE:
			    	previousX = lastX;
					lastX = event.getX();
					int positionDiff = Float.valueOf(motionStartX - lastX).intValue();
					if(positionDiff < 0)
						positionDiff = 0;
					if(lastDiff == positionDiff || lastDiff -1 == positionDiff)
						break;
					lastDiff = positionDiff;
					if(lastDiff < maxDiff)
						maxDiff = lastDiff;

			    	/*contentContainerLayoutParams.setMargins(menuSize - positionDiff, 0, -menuSize + positionDiff, 0);
			    	contentContainer.setLayoutParams(contentContainerLayoutParams);
					Log.d("menuSize", Integer.valueOf(menuSize).toString());*/
					contentContainer.scrollTo(-menuSize + positionDiff, 0);
			    	
			    	/*menuLayoutParams.setMargins(-positionDiff / 2, statusBarHeight, positionDiff / 2, 0);
			    	menu.setLayoutParams(menuLayoutParams);*/
					menu.scrollTo(positionDiff / 2, 0);
					break;
				case MotionEvent.ACTION_UP:
					contentContainer.scrollTo(0, 0);
					menu.scrollTo(0, 0);
					if(previousX < event.getX() && lastDiff > Functions.dpToPx(5, act)) {
						show(true, lastDiff);
					} else
						hide(lastDiff);
					break;
				}
				return true;
			}
		});
	}
	public void hide() {
		hide(0);
	}
	public void hide(int offset) {
		contentContainer.bringToFront();
		//slide out menu to left
		TranslateAnimation menuSlideOut = new TranslateAnimation(0, -((menuSize - offset) / 3), 0, 0);
		menuSlideOut.setDuration(Math.abs(menuSize - offset) *500 / menuSize);
		menu.startAnimation(menuSlideOut);
		
		//slide in content from right
		TranslateAnimation contentSlideIn = new TranslateAnimation(menuSize - offset, 0, 0, 0);
		contentSlideIn.setDuration(Math.abs(menuSize - offset) *500 / menuSize);
		contentContainer.startAnimation(contentSlideIn);
		
		contentContainerLayoutParams = (FrameLayout.LayoutParams) contentContainer.getLayoutParams();
    	contentContainerLayoutParams.setMargins(0, 0, 0, 0);
    	contentContainer.setLayoutParams(contentContainerLayoutParams);

    	//re-enable all Views
    	Functions.enableDisableViewGroup((LinearLayout) containerParent.findViewById(android.R.id.content).getParent(), true);
    	
    	menuShown = false;
		
		menuSlideOut.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
				// not needed here
			}
			@Override
			public void onAnimationRepeat(Animation animation) {
				// not needed here	
			}
			@Override
			public void onAnimationEnd(Animation animation) {
				// move menu out of visible scope
				menuLayoutParams = (FrameLayout.LayoutParams) menu.getLayoutParams();
				menuLayoutParams.setMargins(20000, 20000, 0, 0);
				menu.setLayoutParams(menuLayoutParams);
			}
		});
	}
	public void fill() {
		ListView list = (ListView) act.findViewById(R.id.menu_listview);
		if (prefs.getBoolean(Functions.IS_LOGGED_IN, false)) {
			if (prefs.getBoolean(Functions.RIGHTS_TEACHER, false)
					|| prefs.getBoolean(Functions.RIGHTS_ADMIN, false))
				items = new SlideMenuAdapter.MenuDesc[9];
			else
				items = new SlideMenuAdapter.MenuDesc[8];
			for (int i = 0; i < items.length; i++) {
				items[i] = new SlideMenuAdapter.MenuDesc();
			}
			items[0].icon = R.drawable.ic_timetable;
			items[0].label = "Stundenplan";
			items[0].openActivity = TimeTable.class;
			items[1].icon = R.drawable.ic_vplan_green;
			items[1].label = "Vertretungsplan";
			items[1].openActivity = VPlan.class;
			items[2].icon = R.drawable.ic_events;
			items[2].label = "Termine";
			items[2].openActivity = Events.class;
			items[3].icon = R.drawable.ic_smv;
			items[3].label = "SMVBlog";
			items[3].openActivity = SMVBlog.class;
			items[4].icon = R.drawable.ic_settings;
			items[4].label = "Einstellungen";
			items[4].openActivity = (Functions.getSDK() >= 11) ? SettingsAdvanced.class
					: Settings.class;
			items[5].icon = R.drawable.ic_help;
			items[5].label = "Hilfe";
			items[5].openActivity = null;
			items[5].openIntent = new Intent(act, HelpAbout.class);
			items[5].openIntent.putExtra(Functions.HELPABOUT, Functions.help);
			items[5].useSlideMenu = false;
			items[6].icon = R.drawable.ic_about;
			items[6].label = "Über";
			items[6].openActivity = null;
			items[6].openIntent = new Intent(act, HelpAbout.class);
			items[6].openIntent.putExtra(Functions.HELPABOUT, Functions.about);
			items[6].useSlideMenu = false;
			String news_pupils = prefs.getString(Functions.NEWS_PUPILS, "");
			items[7].type = Functions.TYPE_INFO;
			items[7].title = "Aktuell";
			items[7].icon = R.drawable.ic_launcher;
			items[7].label = news_pupils.substring(0,
					((news_pupils.length() > 60) ? 60 : news_pupils.length()))
					+ ((news_pupils.length() > 60) ? "..." : "");
			items[7].openActivity = null;
			items[7].openIntent = new Intent(act, InfoActivity.class);
			items[7].openIntent.putExtra("type", "info");
			items[7].openIntent.putExtra("info_type", "pupils");
			items[7].useSlideMenu = false;
			if (prefs.getBoolean(Functions.RIGHTS_TEACHER, false)
					|| prefs.getBoolean(Functions.RIGHTS_ADMIN, false)) {
				String news_teachers = prefs.getString(Functions.NEWS_TEACHERS,
						"");
				items[8].type = Functions.TYPE_INFO;
				items[8].title = "Lehrerinfo";
				items[8].icon = R.drawable.ic_launcher;
				items[8].label = news_teachers.substring(0, ((news_teachers
						.length() > 60) ? 60 : news_teachers.length()))
						+ ((news_teachers.length() > 60) ? "..." : "");
				items[8].openIntent = new Intent(act, InfoActivity.class);
				items[8].openIntent.putExtra("type", "info");
				items[8].openIntent.putExtra("info_type", "teachers");
				items[8].useSlideMenu = false;
			}
		} else {

			items = new SlideMenuAdapter.MenuDesc[5];
			for (int i = 0; i < items.length; i++) {
				items[i] = new SlideMenuAdapter.MenuDesc();
			}
			items[0].icon = R.drawable.ic_settings;
			items[0].label = "Setup-Assistent";
			items[0].openActivity = SetupAssistant.class;
			items[1].icon = R.drawable.ic_events;
			items[1].label = "Termine";
			items[1].openActivity = Events.class;
			items[2].icon = R.drawable.ic_smv;
			items[2].label = "SMVBlog";
			items[2].openActivity = SMVBlog.class;
			items[3].icon = R.drawable.ic_help;
			items[3].label = "Hilfe";
			items[3].openActivity = null;
			items[3].openIntent = new Intent(act, HelpAbout.class);
			items[3].openIntent.putExtra(Functions.HELPABOUT, Functions.help);
			items[3].useSlideMenu = false;
			items[4].icon = R.drawable.ic_about;
			items[4].label = "Über";
			items[4].openActivity = null;
			items[4].openIntent = new Intent(act, HelpAbout.class);
			items[4].openIntent.putExtra(Functions.HELPABOUT, Functions.about);
			items[4].useSlideMenu = false;
		}
		SlideMenuAdapter adap = new SlideMenuAdapter(act, items);
		list.setAdapter(adap);
	}
}
