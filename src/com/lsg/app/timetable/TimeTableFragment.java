package com.lsg.app.timetable;

import java.util.Calendar;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Messenger;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.lsg.app.Functions;
import com.lsg.app.R;
import com.lsg.app.ServiceHandler;
import com.lsg.app.WorkerService;
import com.lsg.app.interfaces.SelectedCallback;
import com.lsg.app.lib.LSGApplication;
import com.lsg.app.sqlite.LSGSQliteOpenHelper;


 
public class TimeTableFragment extends Fragment implements SelectedCallback,
		WorkerService.WorkerClass {
	private ProgressDialog loading;
	private TimeTableViewPagerAdapter viewpageradap;
	private ViewPager pager;
	private TextView footer;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.viewpager, null);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActivity().setTitle(R.string.timetable);

		viewpageradap = new TimeTableViewPagerAdapter(this);

		pager = (ViewPager) getActivity().findViewById(R.id.viewpager);
		pager.setAdapter(viewpageradap);
		pager.setPageMargin(Functions.dpToPx(40, getActivity()));
		pager.setPageMarginDrawable(R.layout.viewpager_margin);

		// get current day
		Calendar cal = Calendar.getInstance();
		int day;
		switch (cal.get(Calendar.DAY_OF_WEEK)) {
		case Calendar.MONDAY:
			day = 0;
			break;
		case Calendar.TUESDAY:
			day = 1;
			break;
		case Calendar.WEDNESDAY:
			day = 2;
			break;
		case Calendar.THURSDAY:
			day = 3;
			break;
		case Calendar.FRIDAY:
			day = 4;
			break;
		default:
			day = 0;
			break;
		}
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(getActivity());
		pager.setCurrentItem(day, true);
		footer = (TextView) getActivity().findViewById(R.id.footer_text);
		
		// add actions for teachers & admins
		if ((prefs.getBoolean(Functions.RIGHTS_TEACHER, false) || prefs
				.getBoolean(Functions.RIGHTS_ADMIN, false))) {
			SpinnerAdapter mSpinnerAdapter = ArrayAdapter
					.createFromResource(
							getActivity(),
							((prefs.getBoolean(Functions.RIGHTS_ADMIN, false)) ? R.array.timetable_actions
									: R.array.timetable_actions_teachers), (Functions.getSDK() >= 15) ? android.R.layout.simple_spinner_dropdown_item : android.R.layout.simple_spinner_dropdown_item); // TODO with sdk < 15
			ActionBar.OnNavigationListener navListener = new ActionBar.OnNavigationListener() {
				@Override
				public boolean onNavigationItemSelected(int itemPosition,
						long itemId) {
					return TimeTableFragment.this.selected(itemPosition, itemId);
				}
			};
			
			ActionBar actionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
			actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
			actionBar.setListNavigationCallbacks(mSpinnerAdapter, navListener);
			actionBar.setSelectedNavigationItem(0);
		}
		// something to restore...
		if (savedInstanceState != null) {
			if (savedInstanceState.getString("selclass") != null) {
				viewpageradap.setClass(
						(String) savedInstanceState.getString("selclass"),
						savedInstanceState.getBoolean("ownclass", true));
				if (!savedInstanceState.getBoolean("ownclass", true)) {
					footer.setVisibility(View.VISIBLE);
					footer.setText(savedInstanceState.getString("selclass"));
				}
			} else {
				viewpageradap.setTeacher(savedInstanceState
						.getString("selshort"));
				footer.setVisibility(View.VISIBLE);
				footer.setText(savedInstanceState.getString("selshort"));
			}
			viewpageradap.updateList();
			
			// select the item
			ignoreSpinnerSelect = true;
			((ActionBarActivity) getActivity()).getSupportActionBar().setSelectedNavigationItem(savedInstanceState.getInt("navlistselected"));
			isRefreshing = savedInstanceState.getBoolean("refreshing");
		}
		// show help overlays
		Functions.checkMessage(getActivity(), new String[] {
				Functions.OVERLAY_HOMEBUTTON, Functions.OVERLAY_SWIPE });
	}

	public void showMine() {
		((TextView) getActivity().findViewById(R.id.footer_text))
				.setVisibility(View.GONE);
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(getActivity());
		if (prefs.getBoolean(Functions.RIGHTS_TEACHER, false)) {
			viewpageradap.setTeacher(prefs.getString(
					LSGSQliteOpenHelper.TEACHER_SHORT, ""));
			viewpageradap.updateList();
		} else {
			viewpageradap.setClass("", true);
			viewpageradap.updateList();
		}
	}

	public void showClasses() {
		footer.setVisibility(View.VISIBLE);
		final SQLiteDatabase myDB = LSGApplication.getSqliteDatabase();
		final Cursor c = myDB.query(
				LSGSQliteOpenHelper.DB_TIME_TABLE_HEADERS_PUPILS, new String[] {
						LSGSQliteOpenHelper.DB_ROWID,
						LSGSQliteOpenHelper.DB_KLASSE }, null, null, null,
				null, null);
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(R.string.select_class);
		builder.setCursor(c, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				c.moveToPosition(item);
				viewpageradap.setClass(c.getString(c
						.getColumnIndex(LSGSQliteOpenHelper.DB_KLASSE)), false);
				((TextView) getActivity().findViewById(R.id.footer_text)).setText(c
						.getString(c
								.getColumnIndex(LSGSQliteOpenHelper.DB_KLASSE)));
				viewpageradap.updateList();
				c.close();
			}
		}, LSGSQliteOpenHelper.DB_KLASSE);
		builder.setCancelable(false);
		AlertDialog alert = builder.create();
		alert.show();
	}

	public void showTeachers() {
		footer.setVisibility(View.VISIBLE);
		final SQLiteDatabase myDB = LSGApplication.getSqliteDatabase();
		final Cursor c = myDB.query(
				LSGSQliteOpenHelper.DB_TIME_TABLE_HEADERS_TEACHERS,
				new String[] { LSGSQliteOpenHelper.DB_ROWID,
						LSGSQliteOpenHelper.DB_TEACHER,
						LSGSQliteOpenHelper.DB_SHORT }, null, null, null, null,
				null);
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(R.string.select_teacher);
		builder.setCursor(c, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				c.moveToPosition(item);
				viewpageradap.setTeacher(c.getString(c
						.getColumnIndex(LSGSQliteOpenHelper.DB_SHORT)));
				((TextView) getActivity().findViewById(R.id.footer_text)).setText(c.getString(c
						.getColumnIndex(LSGSQliteOpenHelper.DB_TEACHER)));
				viewpageradap.updateList();
				c.close();
			}
		}, LSGSQliteOpenHelper.DB_TEACHER);
		builder.setCancelable(false);
		AlertDialog alert = builder.create();
		alert.show();
	}

	private MenuItem refresh;
	private boolean actionViewSet;

	@TargetApi(11)
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		actionViewSet = false;
		inflater.inflate(R.menu.timetable, menu);
			refresh = menu.findItem(R.id.refresh);
		if(isRefreshing) {
			MenuItemCompat.setActionView(refresh, new ProgressBar(getActivity()));
		}
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.refresh:
			updateTimeTable();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private boolean isRefreshing = false;
	private static ServiceHandler hand;

	public void updateTimeTable() {
		updateTimeTable(false);
	}

	public void updateTimeTable(boolean force) {
		MenuItemCompat.setActionView(refresh, new ProgressBar(getActivity()));
		hand = new ServiceHandler(new ServiceHandler.ServiceHandlerCallback() {
			@Override
			public void onServiceError() {
				// not needed here
			}

			@Override
			public void onFinishedService() {
				Log.d("service", "finished without error");
				MenuItemCompat.collapseActionView(refresh);
				isRefreshing = false;
			}
		});
		Handler handler = hand.getHandler();

		Intent intent = new Intent(getActivity(), WorkerService.class);
		// Create a new Messenger for getting the result
		Messenger messenger = new Messenger(handler);
		intent.putExtra(WorkerService.MESSENGER, messenger);
		intent.putExtra(WorkerService.WORKER_CLASS,
				TimeTableFragment.class.getCanonicalName());
		if (force)
			intent.putExtra(WorkerService.WHAT, WorkerService.UPDATE_ALL_FORCE);
		else
			intent.putExtra(WorkerService.WHAT, WorkerService.UPDATE_ALL);
		getActivity().startService(intent);
	}

	private int selectedPos;
	private boolean ignoreSpinnerSelect = false;

	@Override
	public boolean selected(int position, long id) {
		selectedPos = position;
		switch (position) {
		case 0:
			showMine();
			break;
		case 1:
			footer.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					showClasses();
				}
			});
			if (!ignoreSpinnerSelect)
				showClasses();
			break;
		case 2:
			footer.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					showTeachers();
				}
			});
			if (!ignoreSpinnerSelect)
				showTeachers();
			break;
		default:
			showMine();
			break;
		}
		if (ignoreSpinnerSelect)
			ignoreSpinnerSelect = false;
		return false;
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		try {
			savedInstanceState.putInt("navlistselected", selectedPos);
			savedInstanceState.putBoolean("ownclass",
					viewpageradap.getOwnClass());
			savedInstanceState.putString("selclass", viewpageradap.getKlasse());
			savedInstanceState
					.putString("selshort", viewpageradap.getTeacher());
			savedInstanceState.putBoolean("refreshing", isRefreshing);
		} catch (NullPointerException e) {
			Log.w("LSGäpp", "error saving state");
			e.printStackTrace();
		}
	}

	@Override
	public void onPause() {
		((ActionBarActivity) getActivity()).getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		super.onStop();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (loading != null)
			loading.cancel();
		if (viewpageradap != null)
			viewpageradap.closeCursors();
	}
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		if (selectedPos != 2) // no teacher
			Functions.createContextMenu(menu, v, menuInfo, getActivity(),
					LSGSQliteOpenHelper.DB_TIME_TABLE);
	}

	public boolean onContextItemSelected(final MenuItem item) {
		return Functions.contextMenuSelect(item, getActivity(), viewpageradap,
				LSGSQliteOpenHelper.DB_TIME_TABLE);
	}

	public void update(int what, Context c) {
		TimeTableUpdater udp = new TimeTableUpdater(c);
		switch (what) {
		case WorkerService.UPDATE_ALL_FORCE:
			udp.updatePupils(true);
			udp.updateTeachers(true);
			break;
		case WorkerService.UPDATE_ALL:
			udp.updatePupils();
			udp.updateTeachers();
			break;
		case WorkerService.UPDATE_PUPILS:
			udp.updatePupils();
			break;
		case WorkerService.UPDATE_TEACHERS:
			udp.updateTeachers();
			break;
		}
	}
}