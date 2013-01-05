package com.lsg.app.lib;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.lsg.app.Events;
import com.lsg.app.Functions;
import com.lsg.app.HelpAbout;
import com.lsg.app.InfoActivity;
import com.lsg.app.MainActivity;
import com.lsg.app.R;
import com.lsg.app.SMVBlog;
import com.lsg.app.VPlan;
import com.lsg.app.interfaces.FragmentActivityCallbacks;
import com.lsg.app.setup.SetupAssistant;
import com.lsg.app.tasks.TasksOverView;
import com.lsg.app.timetable.TimeTableFragment;

public class SlideMenuFragment extends ListFragment {
	public static class SlideMenuAdapter extends
			ArrayAdapter<SlideMenuFragment.SlideMenuAdapter.MenuDesc> {
		Activity act;
		SlideMenuFragment.SlideMenuAdapter.MenuDesc[] items;

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
			public Class<? extends Activity> openActivity;
			public Class<? extends Fragment> openFragment;
			public Class<? extends Activity> containerActivity;
			public Intent openIntent;
		}

		public SlideMenuAdapter(Activity act,
				SlideMenuFragment.SlideMenuAdapter.MenuDesc[] items) {
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
				switch (getItemViewType(position)) {
				case Functions.TYPE_PAGE:
					rowView = inflater.inflate(R.layout.menu_listitem, null);
					viewHolder.title = null;
					break;
				case Functions.TYPE_INFO:
					rowView = inflater.inflate(R.layout.menu_info, null);
					viewHolder.title = (TextView) rowView
							.findViewById(R.id.title);
					break;
				}
				viewHolder.icon = (ImageView) rowView
						.findViewById(R.id.menu_icon);
				viewHolder.label = (TextView) rowView
						.findViewById(R.id.menu_label);
				rowView.setTag(viewHolder);
			}

			MenuItem holder = (MenuItem) rowView.getTag();
			String s = items[position].label;
			holder.label.setText(s);
			holder.icon.setImageResource(items[position].icon);

			if (holder.title != null) {
				if (items[position].title != null) {
					holder.title.setText(items[position].title);
					holder.title.setVisibility(View.VISIBLE);
				} else
					holder.title.setVisibility(View.GONE);
			}

			return rowView;
		}
	}

	SlideMenuAdapter.MenuDesc[] items;
	SharedPreferences prefs;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.menu, null);
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
	}
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		fill();
	}
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Class<? extends Fragment> fragment = ((FragmentActivityCallbacks) getActivity()).getSlideMenu().getFragment();
		// if not clicked item for current Activity
		if (fragment != null && fragment.equals(items[position].openFragment))
			((FragmentActivityCallbacks) getActivity()).getSlideMenu().hide();
		else if ((items[position].openActivity == null || !items[position].openActivity
				.equals(getActivity().getClass()))) {
			// mark this menu to be hidden
			if (items[position].useSlideMenu)
				SlideMenu.menuToHide = true;
			// start new activity / intent
			if (items[position].openActivity != null) {
				Intent intent = new Intent(getActivity(), items[position].openActivity);
				intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
				getActivity().startActivity(intent);

			} else if (items[position].containerActivity != null) {
				if (!items[position].containerActivity.equals(getActivity().getClass())) {
					Intent intent = new Intent(getActivity(),
							items[position].containerActivity);
					intent.putExtra("fragment", items[position].openFragment);
					getActivity().startActivity(intent);
				} else {
					Log.d("slide", "change fragment");
					((FragmentActivityCallbacks) getActivity())
							.changeFragment(items[position].openFragment);
					((FragmentActivityCallbacks) getActivity()).getSlideMenu().hide();
				}
			} else
				getActivity().startActivity(items[position].openIntent);
		} else {
			((FragmentActivityCallbacks) getActivity()).getSlideMenu().hide();
		}
	}

	public void fill() {
		ListView list = getListView();
		if (prefs.getBoolean(Functions.IS_LOGGED_IN, false)) {
			if (prefs.getBoolean(Functions.RIGHTS_TEACHER, false)
					|| prefs.getBoolean(Functions.RIGHTS_ADMIN, false))
				items = new SlideMenuAdapter.MenuDesc[7];
			else
				items = new SlideMenuAdapter.MenuDesc[6];
			for (int i = 0; i < items.length; i++) {
				items[i] = new SlideMenuAdapter.MenuDesc();
			}
			// TimeTable
			items[0].icon = R.drawable.ic_timetable;
			items[0].label = "Stundenplan";
			items[0].openFragment = TimeTableFragment.class;
			items[0].containerActivity = MainActivity.class;
			// VPlan
			items[1].icon = R.drawable.ic_vplan_green;
			items[1].label = "Vertretungsplan";
			items[1].openFragment = VPlan.class;
			items[1].containerActivity = MainActivity.class;
			// Tasks
			items[2].icon = R.drawable.ic_tasks;
			items[2].label = "Aufgaben";
			items[2].containerActivity = MainActivity.class;
			items[2].openFragment = TasksOverView.class;
			// Events
			items[3].icon = R.drawable.ic_events;
			items[3].label = "Termine";
			items[3].openFragment = Events.class;
			items[3].containerActivity = MainActivity.class;
			// SMVBlog
			items[4].icon = R.drawable.ic_smv;
			items[4].label = "SMVBlog";
			items[4].openFragment = SMVBlog.class;
			items[4].containerActivity = MainActivity.class;
			// News 4 Pupils
			String news_pupils = prefs.getString(Functions.NEWS_PUPILS, "");
			items[5].type = Functions.TYPE_INFO;
			items[5].title = "Aktuell";
			items[5].icon = R.drawable.ic_launcher;
			items[5].label = news_pupils.substring(0,
					((news_pupils.length() > 60) ? 60 : news_pupils.length()))
					+ ((news_pupils.length() > 60) ? "..." : "");
			items[5].openActivity = null;
			items[5].openIntent = new Intent(getActivity(), InfoActivity.class);
			items[5].openIntent.putExtra("type", "info");
			items[5].openIntent.putExtra("info_type", "pupils");
			items[5].useSlideMenu = false;
			if (prefs.getBoolean(Functions.RIGHTS_TEACHER, false)
					|| prefs.getBoolean(Functions.RIGHTS_ADMIN, false)) {
				String news_teachers = prefs.getString(Functions.NEWS_TEACHERS,
						"");
				items[6].type = Functions.TYPE_INFO;
				items[6].title = "Lehrerinfo";
				items[6].icon = R.drawable.ic_launcher;
				items[6].label = news_teachers.substring(0, ((news_teachers
						.length() > 60) ? 60 : news_teachers.length()))
						+ ((news_teachers.length() > 60) ? "..." : "");
				items[6].openIntent = new Intent(getActivity(), InfoActivity.class);
				items[6].openIntent.putExtra("type", "info");
				items[6].openIntent.putExtra("info_type", "teachers");
				items[6].useSlideMenu = false;
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
			items[1].openFragment = Events.class;
			items[1].containerActivity = MainActivity.class;
			items[2].icon = R.drawable.ic_smv;
			items[2].label = "SMVBlog";
			items[2].openFragment = SMVBlog.class;
			items[2].containerActivity = MainActivity.class;
			items[3].icon = R.drawable.ic_help;
			items[3].label = "Hilfe";
			items[3].openActivity = null;
			items[3].openIntent = new Intent(getActivity(), HelpAbout.class);
			items[3].openIntent.putExtra(Functions.HELPABOUT, Functions.help);
			items[3].useSlideMenu = false;
			items[4].icon = R.drawable.ic_about;
			items[4].label = "Über";
			items[4].openActivity = null;
			items[4].openIntent = new Intent(getActivity(), HelpAbout.class);
			items[4].openIntent.putExtra(Functions.HELPABOUT, Functions.about);
			items[4].useSlideMenu = false;
		}
		SlideMenuAdapter adap = new SlideMenuAdapter(getActivity(), items);
		list.setAdapter(adap);
	}

}
