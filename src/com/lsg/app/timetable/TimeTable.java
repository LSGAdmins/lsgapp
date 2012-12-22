package com.lsg.app.timetable;

import java.util.Calendar;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
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
import android.support.v4.view.ViewPager;
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
import android.widget.ProgressBar;
import android.widget.TextView;

import com.lsg.app.AdvancedWrapper;
import com.lsg.app.Functions;
import com.lsg.app.R;
import com.lsg.app.ServiceHandler;
import com.lsg.app.WorkerService;
import com.lsg.app.interfaces.SelectedCallback;
import com.lsg.app.lib.FragmentActivityCallbacks;
import com.lsg.app.lib.SlideMenu;
import com.lsg.app.lib.TitleCompat;
import com.lsg.app.lib.TitleCompat.HomeCall;
import com.lsg.app.lib.TitleCompat.RefreshCall;

public class TimeTable extends Fragment implements SelectedCallback, HomeCall, RefreshCall, WorkerService.WorkerClass {
	public static void blacklistTimeTable(Context context) {
		SQLiteDatabase myDB = context.openOrCreateDatabase(Functions.DB_NAME,
				Context.MODE_PRIVATE, null);
		Cursor allSubjects = myDB.query(Functions.DB_TIME_TABLE, new String[] { Functions.DB_ROWID,
				Functions.DB_DAY, Functions.DB_HOUR, Functions.DB_RAW_FACH,
				Functions.DB_RAW_LEHRER }, null, null, null, null, null);
		allSubjects.moveToFirst();
		ContentValues vals = new ContentValues();
		vals.put(Functions.DB_DISABLED, 2);
		myDB.update(Functions.DB_TIME_TABLE, vals, null, null);
		if(allSubjects.getCount() > 0)
		do {
			Cursor exclude = myDB.query(
					Functions.DB_EXCLUDE_TABLE,
					new String[] { Functions.DB_ROWID },
					Functions.DB_TEACHER + "=? AND " + Functions.DB_RAW_FACH
 + "=? AND "
										+ Functions.DB_HOUR + "=? AND "
										+ Functions.DB_DAY + "=?",
								new String[] {
										allSubjects.getString(allSubjects
												.getColumnIndex(Functions.DB_RAW_LEHRER)),
										allSubjects.getString(allSubjects
												.getColumnIndex(Functions.DB_RAW_FACH)),
										allSubjects.getString(allSubjects
												.getColumnIndex(Functions.DB_HOUR)),
										allSubjects.getString(allSubjects
												.getColumnIndex(Functions.DB_DAY)) },
								null, null, null);
				Cursor exclude_oldstyle = myDB
						.query(Functions.DB_EXCLUDE_TABLE,
								new String[] { Functions.DB_ROWID },
								Functions.DB_RAW_FACH + "=? AND "
										+ Functions.DB_TYPE + "=?",
								new String[] {
										allSubjects.getString(allSubjects
												.getColumnIndex(Functions.DB_RAW_FACH)),
										"oldstyle" }, null, null, null);
				if (exclude.getCount() > 0 || exclude_oldstyle.getCount() > 0) {
					myDB.execSQL(
							"UPDATE " + Functions.DB_TIME_TABLE + " SET "
									+ Functions.DB_DISABLED + "=? WHERE "
									+ Functions.DB_ROWID + "=?",
							new String[] {
									"1",
									allSubjects.getString(allSubjects
											.getColumnIndex(Functions.DB_ROWID)) });
				}
				exclude.close();
				exclude_oldstyle.close();
			} while (allSubjects.moveToNext());
		allSubjects.close();
		try {
			myDB.close();
		} catch (Exception e) {

		}
	}
	private ProgressDialog loading;
	private TimeTableViewPagerAdapter viewpageradap;
	private ViewPager pager;
	private SlideMenu slidemenu;
	private TextView footer;
	private TitleCompat titlebar;

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
		((FragmentActivityCallbacks) getActivity()).getSlideMenu().setFragment(TimeTable.class);
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
		titlebar = ((FragmentActivityCallbacks) getActivity()).getTitlebar();
		titlebar.addRefresh(this);
		titlebar.setTitle(getActivity().getTitle());
		if ((prefs.getBoolean(Functions.RIGHTS_TEACHER, false) || prefs
				.getBoolean(Functions.RIGHTS_ADMIN, false))) {
			titlebar.addSpinnerNavigation(this, (prefs.getBoolean(
					Functions.RIGHTS_ADMIN, false)) ? R.array.timetable_actions
					: R.array.timetable_actions_teachers);
		}
		// let service check for update
		Intent intent = new Intent(getActivity(), WorkerService.class);
		intent.putExtra(WorkerService.WHAT, 100);
		getActivity().startService(intent);
		// Log.d("encrypted", LSGappAuth.encrypt("passwort", "aaaa"));
		Functions.checkMessage(getActivity(), new String[] {
				Functions.OVERLAY_HOMEBUTTON, Functions.OVERLAY_SWIPE });
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
			viewpageradap.updateCursor();
			if (Functions.getSDK() >= 11
					&& (prefs.getBoolean(Functions.RIGHTS_ADMIN, false) || prefs
							.getBoolean(Functions.RIGHTS_TEACHER, false))) {
				suppressSelect = true;
				AdvancedWrapper adv = new AdvancedWrapper();
				// TODO selected item also for pre-ics
				adv.setSelectedItem(
						savedInstanceState.getInt("navlistselected"), getActivity());
			}
			refreshing = savedInstanceState.getBoolean("refreshing");
		}
	}
	public void showMine() {
		((TextView) getActivity().findViewById(R.id.footer_text)).setVisibility(View.GONE);
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		if (prefs.getBoolean(Functions.RIGHTS_TEACHER, false)) {
			viewpageradap.setTeacher(prefs.getString(Functions.TEACHER_SHORT, ""));
			viewpageradap.updateCursor();
		} else {
			viewpageradap.setClass("", true);
			viewpageradap.updateCursor();
		}
	}

	public void showClasses() {
		footer.setVisibility(View.VISIBLE);
		final SQLiteDatabase myDB = getActivity().openOrCreateDatabase(Functions.DB_NAME,
				Context.MODE_PRIVATE, null);
		final Cursor c = myDB.query(Functions.DB_TIME_TABLE_HEADERS_PUPILS,
				new String[] { Functions.DB_ROWID, Functions.DB_KLASSE }, null,
				null, null, null, null);
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(R.string.select_class);
		builder.setCursor(c, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				c.moveToPosition(item);
				viewpageradap.setClass(
						c.getString(c.getColumnIndex(Functions.DB_KLASSE)),
						false);
				((TextView) getActivity().findViewById(R.id.footer_text)).setText(c
						.getString(c.getColumnIndex(Functions.DB_KLASSE)));
				viewpageradap.updateCursor();
				c.close();
				myDB.close();
			}
		}, Functions.DB_KLASSE);
		builder.setCancelable(false);
		AlertDialog alert = builder.create();
		alert.show();
	}

	public void showTeachers() {
		footer.setVisibility(View.VISIBLE);
		final SQLiteDatabase myDB = getActivity().openOrCreateDatabase(Functions.DB_NAME,
				Context.MODE_PRIVATE, null);
		final Cursor c = myDB.query(Functions.DB_TIME_TABLE_HEADERS_TEACHERS,
				new String[] { Functions.DB_ROWID, Functions.DB_TEACHER,
						Functions.DB_SHORT }, null, null, null, null, null);
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(R.string.select_teacher);
		builder.setCursor(c, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				c.moveToPosition(item);
				viewpageradap.setTeacher(c.getString(c
						.getColumnIndex(Functions.DB_SHORT)));
				((TextView) getActivity().findViewById(R.id.footer_text)).setText(c
						.getString(c.getColumnIndex(Functions.DB_TEACHER)));
				viewpageradap.updateCursor();
				c.close();
				myDB.close();
			}
		}, Functions.DB_TEACHER);
		builder.setCancelable(false);
		AlertDialog alert = builder.create();
		alert.show();
	}
	private MenuItem refresh;
	@TargetApi(11)
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.timetable, menu);
		if(Functions.getSDK() < 11)
			menu.removeItem(R.id.refresh);
		else
			refresh = menu.findItem(R.id.refresh);
		if(refreshing && Functions.getSDK() >= 11)
				refresh.setActionView(new ProgressBar(getActivity()));
		else if(refreshing)
			loading = ProgressDialog.show(getActivity(), null, getString(R.string.loading_timetable));
		super.onCreateOptionsMenu(menu, inflater);
		//return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.refresh:
			onRefreshPress();
			return true;
		case android.R.id.home:
			onHomePress();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	private boolean refreshing = false;
	private static ServiceHandler hand;
	private View actionView;
	@TargetApi(11)
	public void updateTimeTable() {
		refreshing = true;
		View v;
		if (Functions.getSDK() >= 11) {
			try {
				v = refresh.getActionView();
				refresh.setActionView(new ProgressBar(getActivity()));
				refresh.getActionView().setSaveEnabled(false);
			} catch (NullPointerException e) {
				loading = ProgressDialog.show(getActivity(), null, getString(R.string.loading_timetable));
				v = null;
			}
			actionView = v;
		} else {
			actionView = null;
			loading = ProgressDialog.show(getActivity(), null,
					getString(R.string.loading_timetable));
		}
		hand = new ServiceHandler(new ServiceHandler.ServiceHandlerCallback() {
			@Override
			public void onServiceError() {
				// not needed here
			}

			@Override
			public void onFinishedService() {
				Log.d("service", "finished without error");
				try {
				if (Functions.getSDK() >= 11 && actionView != null)
					refresh.setActionView(actionView);
				else
					loading.cancel();
				} catch(Exception e) {
					Log.w("LSGäpp", "Error hiding loading");
					e.printStackTrace();
				}
				refreshing = false;
			}
		});
		Handler handler = hand.getHandler();
		
		Intent intent = new Intent(getActivity(), WorkerService.class);
	    // Create a new Messenger for the communication back
	    Messenger messenger = new Messenger(handler);
	    intent.putExtra(WorkerService.MESSENGER, messenger);
	    intent.putExtra(WorkerService.WORKER_CLASS, TimeTable.class.getCanonicalName());
	    intent.putExtra(WorkerService.WHAT, WorkerService.UPDATE_ALL);
	    getActivity().startService(intent);
	}
	@Override
	public void onDestroy() {
		super.onDestroy();
		try {
		loading.cancel();
		} catch(NullPointerException e) {
			//no dialog
		}
		try {
			viewpageradap.closeCursorsDB();
		} catch(NullPointerException e) {
			//viewpager not yet initialized
		}
	}
	private int selPos;
	private boolean suppressSelect = false;
	@Override
	public boolean selected(int position, long id) {
		selPos = position;
		switch(position) {
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
			if (!suppressSelect)
				showClasses();
			break;
		case 2:
			footer.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					showTeachers();
				}
			});
			if (!suppressSelect)
				showTeachers();
			break;
		default:
			showMine();
			break;
		}
		if (suppressSelect)
			suppressSelect = false;
		return false;
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		try {
			savedInstanceState.putInt("navlistselected", selPos);
			savedInstanceState.putBoolean("ownclass", viewpageradap.getOwnClass());
			savedInstanceState.putString("selclass", viewpageradap.getKlasse());
			savedInstanceState.putString("selshort", viewpageradap.getTeacher());
			savedInstanceState.putBoolean("refreshing", refreshing);
		} catch (NullPointerException e) {
		  e.printStackTrace();
	  }
	}
	@Override
	public void onHomePress() {
		slidemenu.show();
	}
	@Override
	public void onStop() {
		titlebar.removeSpinnerNavigation();
		super.onStop();
	}
	@Override
	public void onRefreshPress() {
		updateTimeTable();
	}
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		Functions.createContextMenu(menu, v, menuInfo, getActivity(), Functions.DB_TIME_TABLE);
	}
	@Override
	public boolean onContextItemSelected(final MenuItem item) {
		return Functions.contextMenuSelect(item, getActivity(), viewpageradap, Functions.DB_TIME_TABLE);
	}
	public void update(int what, Context c) {
		TimeTableUpdater udp = new TimeTableUpdater(c);
		switch(what) {
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