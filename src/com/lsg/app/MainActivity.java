package com.lsg.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Toast;

import com.lsg.app.interfaces.FragmentActivityCallbacks;
import com.lsg.app.lib.TitleCompat;
import com.lsg.app.lib.TitleCompat.HomeCall;
import com.lsg.app.settings.Settings;
import com.lsg.app.settings.SettingsAdvanced;
import com.lsg.app.setup.SetupAssistant;
import com.lsg.app.tasks.CreateEditFragment;
import com.lsg.app.tasks.Exams;
import com.lsg.app.tasks.TaskSelected;
import com.lsg.app.timetable.TimeTableFragment;
import com.lsg.lib.slidemenu.SlideMenu;

public class MainActivity extends FragmentActivity implements HomeCall,
		FragmentActivityCallbacks, TaskSelected {
	private TitleCompat titlebar;
	private SlideMenu slidemenu;
	private Class<? extends Fragment> curFrag;
	private Fragment fragment;

	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			task = savedInstanceState.getInt("task");
			id = savedInstanceState.getInt("id");
		}
		if (Functions.getSDK() > 11)
			requestWindowFeature(Window.FEATURE_PROGRESS);

		super.onCreate(savedInstanceState);
		boolean homeAsUp = true;
		if (getResources().getBoolean(R.bool.isTablet))
			homeAsUp = false;
		titlebar = new TitleCompat(this, homeAsUp);

		if (getResources().getBoolean(R.bool.isTablet))
			setContentView(R.layout.fragment_main_tablet);
		else
			setContentView(R.layout.fragment_main);
		titlebar.init(this);
		titlebar.setTitle(getTitle());
		slidemenu = new SlideMenu(this);

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

	@Override
	protected void onResume() {
		slidemenu.checkShown();
		super.onResume();
	}

	@Override
	public void onHomePress() {
		slidemenu.show();
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
		Intent intent;
		// Handle item selection
		switch (item.getItemId()) {
		case android.R.id.home:
			onHomePress();
			return true;
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

	public TitleCompat getTitlebar() {
		return titlebar;
	}

	public SlideMenu getSlideMenu() {
		return slidemenu;
	}

	@Override
	public void changeFragment(Class<? extends Fragment> frag) {
		changeFragment(frag, null);
	}

	public void changeFragment(Class<? extends Fragment> frag, Bundle args) {
		FragmentTransaction fragmentTransaction = getSupportFragmentManager()
				.beginTransaction();
		try {
			fragment = frag.newInstance();
			fragment.setArguments(args);
			fragmentTransaction.replace(R.id.fragmentContainer, fragment);
			fragmentTransaction.addToBackStack(null);
			fragmentTransaction.commit();
		} catch (Exception e) {
			e.printStackTrace();
		}
		curFrag = frag;
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
		switch (taskId) {
		case TaskSelected.TASK_EXAMS:
			frag = Exams.class;
			break;
		case TaskSelected.TASK_EDIT_EXAMS:
			frag = CreateEditFragment.class;
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
		changeFragment(frag, args);
	}

	@Override
	public void onBackPressed() {
		if(curFrag.equals(CreateEditFragment.class))
			((CreateEditFragment) fragment).onCancel();
		else if (!slidemenu.handleBack())
			super.onBackPressed();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putInt("task", task);
		outState.putInt("id", id);
		super.onSaveInstanceState(outState);
		// NOTE restore is done in onCreate
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
}
