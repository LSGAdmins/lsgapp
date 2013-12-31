package com.lsg.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.lsg.app.drawer.DrawerAdapter;
import com.lsg.app.interfaces.FragmentActivityCallbacks;
import com.lsg.app.settings.Settings;
import com.lsg.app.settings.SettingsAdvanced;
import com.lsg.app.setup.SetupAssistant;
import com.lsg.app.tasks.CreateEditFragment;
import com.lsg.app.tasks.Exams;
import com.lsg.app.tasks.TaskSelected;
import com.lsg.app.tasks.TasksOverView;
import com.lsg.app.timetable.TimeTableFragment;
import com.lsg.app.vplan.VPlanFragment;


public class MainActivity extends ActionBarActivity implements FragmentActivityCallbacks, TaskSelected {
	private Class<? extends Fragment> curFrag;
	private Fragment fragment;

	private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private ListView mDrawerList;
    private DrawerAdapter mSlideMenuAdapter;

	private DrawerAdapter.MenuDesc[] mSlideMenuItems;
	
	private int transId;

	private class DrawerItemClickListener implements ListView.OnItemClickListener {
	    @Override
	    public void onItemClick(AdapterView parent, View view, int position, long id) {
			if (mSlideMenuItems[position].openFragment != null) {
				changeFragment(mSlideMenuItems[position].openFragment);
				mSlideMenuAdapter.clearSelection();
				mSlideMenuItems[position].selected = true;
				mSlideMenuAdapter.notifyDataSetChanged();
			} else if (mSlideMenuItems[position].openIntent != null) {
				startActivity(mSlideMenuItems[position].openIntent);
			} else {
				Intent intent = new Intent(getApplicationContext(), mSlideMenuItems[position].openActivity);
				intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
				startActivity(intent);
			}
			mDrawerLayout.closeDrawers();
		}
	
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_PROGRESS);
		super.onCreate(savedInstanceState);
		
		if (savedInstanceState != null) {
			task = savedInstanceState.getInt("task");
			id = savedInstanceState.getInt("id");
		}

		if (getResources().getBoolean(R.bool.isTablet))
			setContentView(R.layout.fragment_main_tablet);
		else
			setContentView(R.layout.fragment_main);
		
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);

		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close) {
			public void onDrawerClosed(android.view.View drawerView) {}
			public void onDrawerOpened(android.view.View drawerView) {}
		};
		
		mDrawerLayout.setDrawerListener(mDrawerToggle);
		
		mDrawerList = (ListView) findViewById(R.id.left_drawer);
		fillDrawer();
		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
		

		Class<? extends Fragment> frag = null;
		// usually open TimeTable
		if (savedInstanceState == null)
			frag = TimeTableFragment.class;
		if (!((SharedPreferences) PreferenceManager
				.getDefaultSharedPreferences(this)).getBoolean(
				Functions.IS_LOGGED_IN, false)) {
			if (!((SharedPreferences) PreferenceManager
					.getDefaultSharedPreferences(this)).getString("username",
					"null").equals("null")) {
				Toast.makeText(this,
						getString(R.string.setup_assistant_opening),
						Toast.LENGTH_LONG).show();
				startActivity(new Intent(this, SetupAssistant.class));
				this.finish();
				return;
			} else {
				Toast.makeText(this, getString(R.string.run_setup_assistant),
						Toast.LENGTH_LONG).show();
				frag = Events.class;
			}
			Functions.init(this);
		}

		Bundle extras = getIntent().getExtras();
		if (extras != null && extras.containsKey("fragment"))
			frag = (Class<? extends Fragment>) extras
					.getSerializable("fragment");
		FragmentTransaction fragmentTransaction = getSupportFragmentManager()
				.beginTransaction();
		try {
			fragment = frag.newInstance();
			fragmentTransaction.add(R.id.fragmentContainer, fragment);
			fragmentTransaction.commit();
		} catch (Exception e) {
			e.printStackTrace();
		}

		// let service check for update
		Intent intent = new Intent(this, WorkerService.class);
		intent.putExtra(WorkerService.WHAT, 100);
		startService(intent);

		// show slidemenu help overlay
		Functions.checkMessage(this,
				new String[] { Functions.OVERLAY_HOMEBUTTON });
	}
	
	private void fillDrawer() {
		SharedPreferences prefs = (SharedPreferences) PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		if (prefs.getBoolean(Functions.IS_LOGGED_IN, false)) {
			if (prefs.getBoolean(Functions.RIGHTS_TEACHER, false)
					|| prefs.getBoolean(Functions.RIGHTS_ADMIN, false))
				mSlideMenuItems = new DrawerAdapter.MenuDesc[7];
			else
				mSlideMenuItems = new DrawerAdapter.MenuDesc[6];
			for (int i = 0; i < mSlideMenuItems.length; i++) {
				mSlideMenuItems[i] = new DrawerAdapter.MenuDesc();
			}
			// TimeTable
			mSlideMenuItems[0].icon = R.drawable.ic_timetable;
			mSlideMenuItems[0].label = "Stundenplan";
			mSlideMenuItems[0].openFragment = TimeTableFragment.class;
			mSlideMenuItems[0].containerActivity = MainActivity.class;
			mSlideMenuItems[0].selected = true;
			// VPlan
			mSlideMenuItems[1].icon = R.drawable.ic_vplan_green;
			mSlideMenuItems[1].label = "Vertretungsplan";
			mSlideMenuItems[1].openFragment = VPlanFragment.class;
			mSlideMenuItems[1].containerActivity = MainActivity.class;
			// Tasks
			mSlideMenuItems[2].icon = R.drawable.ic_tasks;
			mSlideMenuItems[2].label = "Aufgaben";
			mSlideMenuItems[2].containerActivity = MainActivity.class;
			mSlideMenuItems[2].openFragment = TasksOverView.class;
			// Events
			mSlideMenuItems[3].icon = R.drawable.ic_events;
			mSlideMenuItems[3].label = "Termine";
			mSlideMenuItems[3].openFragment = Events.class;
			mSlideMenuItems[3].containerActivity = MainActivity.class;
			// SMVBlog
			mSlideMenuItems[4].icon = R.drawable.ic_smv;
			mSlideMenuItems[4].label = "SMVBlog";
			mSlideMenuItems[4].openFragment = SMVBlog.class;
			mSlideMenuItems[4].containerActivity = MainActivity.class;
			// News 4 Pupils
			String news_pupils = prefs.getString(Functions.NEWS_PUPILS, "");
			mSlideMenuItems[5].type = Functions.TYPE_INFO;
			mSlideMenuItems[5].title = "Aktuell";
			mSlideMenuItems[5].icon = R.drawable.ic_launcher;
			mSlideMenuItems[5].label = news_pupils.substring(0,
					((news_pupils.length() > 60) ? 60 : news_pupils.length()))
					+ ((news_pupils.length() > 60) ? "..." : "");
			mSlideMenuItems[5].openActivity = null;
			mSlideMenuItems[5].openIntent = new Intent(this, InfoActivity.class);
			mSlideMenuItems[5].openIntent.putExtra("type", "info");
			mSlideMenuItems[5].openIntent.putExtra("info_type", "pupils");
			mSlideMenuItems[5].useSlideMenu = false;
			if (prefs.getBoolean(Functions.RIGHTS_TEACHER, false)
					|| prefs.getBoolean(Functions.RIGHTS_ADMIN, false)) {
				String news_teachers = prefs.getString(Functions.NEWS_TEACHERS,
						"");
				mSlideMenuItems[6].type = Functions.TYPE_INFO;
				mSlideMenuItems[6].title = "Lehrerinfo";
				mSlideMenuItems[6].icon = R.drawable.ic_launcher;
				mSlideMenuItems[6].label = news_teachers.substring(0, ((news_teachers
						.length() > 60) ? 60 : news_teachers.length()))
						+ ((news_teachers.length() > 60) ? "..." : "");
				mSlideMenuItems[6].openIntent = new Intent(this,
						InfoActivity.class);
				mSlideMenuItems[6].openIntent.putExtra("type", "info");
				mSlideMenuItems[6].openIntent.putExtra("info_type", "teachers");
				mSlideMenuItems[6].useSlideMenu = false;
			}
		} else {

			mSlideMenuItems = new DrawerAdapter.MenuDesc[5];
			for (int i = 0; i < mSlideMenuItems.length; i++) {
				mSlideMenuItems[i] = new DrawerAdapter.MenuDesc();
			}
			mSlideMenuItems[0].icon = R.drawable.ic_settings;
			mSlideMenuItems[0].label = "Setup-Assistent";
			mSlideMenuItems[0].openActivity = SetupAssistant.class;
			mSlideMenuItems[1].icon = R.drawable.ic_events;
			mSlideMenuItems[1].label = "Termine";
			mSlideMenuItems[1].openFragment = Events.class;
			mSlideMenuItems[1].containerActivity = MainActivity.class;
			mSlideMenuItems[1].selected = true;
			mSlideMenuItems[2].icon = R.drawable.ic_smv;
			mSlideMenuItems[2].label = "SMVBlog";
			mSlideMenuItems[2].openFragment = SMVBlog.class;
			mSlideMenuItems[2].containerActivity = MainActivity.class;
			mSlideMenuItems[3].icon = R.drawable.ic_help;
			mSlideMenuItems[3].label = "Hilfe";
			mSlideMenuItems[3].openActivity = null;
			mSlideMenuItems[3].openIntent = new Intent(this, HelpAbout.class);
			mSlideMenuItems[3].openIntent.putExtra(Functions.HELPABOUT, Functions.help);
			mSlideMenuItems[3].useSlideMenu = false;
			mSlideMenuItems[4].icon = R.drawable.ic_about;
			mSlideMenuItems[4].label = "Über";
			mSlideMenuItems[4].openActivity = null;
			mSlideMenuItems[4].openIntent = new Intent(this, HelpAbout.class);
			mSlideMenuItems[4].openIntent.putExtra(Functions.HELPABOUT, Functions.about);
			mSlideMenuItems[4].useSlideMenu = false;
		}
		mSlideMenuAdapter = new DrawerAdapter(this, mSlideMenuItems);
		mDrawerList.setAdapter(mSlideMenuAdapter);
	}
	
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		mDrawerToggle.syncState();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.base_menu, menu);
		if (!((SharedPreferences) PreferenceManager
				.getDefaultSharedPreferences(this)).getBoolean(
				Functions.IS_LOGGED_IN, false))
			menu.removeItem(R.id.settings);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			// pass to drawer
	          return true;
	        }
		Intent intent;
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.settings:
			if (Functions.getSDK() < 11)
				intent = new Intent(this, Settings.class);
			else
				intent = new Intent(this, SettingsAdvanced.class);
			startActivity(intent);
			return true;
		case R.id.help:
			intent = new Intent(this, HelpAbout.class);
			intent.putExtra(Functions.HELPABOUT, Functions.help);
			startActivity(intent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	@Override
	public void changeFragment(Class<? extends Fragment> frag) {
		changeFragment(frag, null);
	}
	public void changeFragment(Class<? extends Fragment> frag, Bundle args) {
		changeFragment(frag, args, true);
	}
	public void changeFragment(Class<? extends Fragment> frag, Bundle args, boolean addToBackStack) {
		FragmentTransaction fragmentTransaction = getSupportFragmentManager()
				.beginTransaction();
		try {
			fragment = frag.newInstance();
			fragment.setArguments(args);
			fragmentTransaction.replace(R.id.fragmentContainer, fragment);
			if (addToBackStack)
				fragmentTransaction.addToBackStack(null);
			transId = fragmentTransaction.commit();
		} catch (Exception e) {
			e.printStackTrace();
		}
		curFrag = frag;
	}
	
	public int getTransId() {
		return transId;
	}

	private int task, id;

	@Override
	public int getCurId() {
		return id;
	}

	@Override
	public int getCurTask() {
		return task;
	}

	@Override
	public void onTaskSelected(int taskId) {
		onTaskSelected(taskId, -1);
	}

	@Override
	public void onTaskSelected(int taskId, int rowId) {
		task = taskId;
		id = rowId;
		Class<? extends Fragment> frag = null;
		boolean addToBackStack = true;
		switch (taskId) {
		case TaskSelected.TASK_EXAMS:
			frag = Exams.class;
			break;
		case TaskSelected.TASK_EDIT_EXAMS:
			frag = CreateEditFragment.class;
			addToBackStack = false;
			break;
		case TaskSelected.TASK_HOMEWORK:
			break;
		case TaskSelected.TASK_GRADES:
			break;
		default:
			return;
		}
		Bundle args = new Bundle();
		args.putLong(TaskSelected.ID, rowId);
		changeFragment(frag, args, addToBackStack);
	}

	@Override
	public void onBackPressed() {
		if (curFrag.equals(CreateEditFragment.class))
			((CreateEditFragment) fragment).onCancel();
		else
			super.onBackPressed();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putInt("task", task);
		outState.putInt("id", id);
		super.onSaveInstanceState(outState);
		// NOTE restore is done in onCreate
	}
}
