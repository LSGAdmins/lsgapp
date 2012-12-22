package com.lsg.app.timetable;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.lsg.app.Functions;
import com.lsg.app.InfoActivity;
import com.lsg.app.PagerTitles;
import com.lsg.app.R;
import com.lsg.app.interfaces.SQLlist;


public class TimeTableViewPagerAdapter extends PagerAdapter implements
		PagerTitles, SQLlist {
	private String[] exclude_subjects = new String[6];
	private final SQLiteDatabase myDB;
	private Cursor[] timetable_cursors = new Cursor[5];
	private TimeTableAdapter[] TimeTableAdapters = new TimeTableAdapter[5];
	private final Context context;
	private final SharedPreferences prefs;
	private String[] titles = new String[5];
	private String klasse;
	private String teacher;
	private boolean ownClass = false;
	private TimeTable timetableFragment;

	public TimeTableViewPagerAdapter(TimeTable timetableFragment) {
		this.timetableFragment = timetableFragment;
		prefs = PreferenceManager.getDefaultSharedPreferences(timetableFragment.getActivity());
		if (prefs.getBoolean(Functions.RIGHTS_TEACHER, false)) {
			setTeacher(prefs.getString(Functions.TEACHER_SHORT, ""));
		}
		else
			setClass("", true);
		context = (Context) timetableFragment.getActivity();
		titles = timetableFragment.getResources().getStringArray(R.array.days);

		myDB = context.openOrCreateDatabase(Functions.DB_NAME,
				Context.MODE_PRIVATE, null);
		for(int i = 0; i < 5; i++)
			TimeTableAdapters[i] =  new TimeTableAdapter(context, timetable_cursors[i]);
		
		SQLiteStatement num_rows = myDB
				.compileStatement("SELECT COUNT(*) FROM "
						+ Functions.DB_TIME_TABLE);
		long count = num_rows.simpleQueryForLong();
		if (count == 0)
			timetableFragment.updateTimeTable();
		num_rows.close();
		updateCursor();
	}

	@Override
	public int getCount() {
		return 5;
	}

	@Override
	public String getTitle(int pos) {
		return titles[pos];
	}

	@Override
	public CharSequence getPageTitle(int position) {
		return titles[position];
	}

	@Override
	public Object instantiateItem(View pager, int position) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		LinearLayout lay = (LinearLayout) inflater.inflate(R.layout.list,
				null);
		ListView lv = (ListView) lay.findViewById(android.R.id.list);
		lv.setAdapter(TimeTableAdapters[position]);
		lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Cursor c = myDB
						.query(Functions.DB_TIME_TABLE,
								new String[] { Functions.DB_REMOTE_ID,
										Functions.DB_VERTRETUNG },
								Functions.DB_ROWID + "=?",
								new String[] { Long.valueOf(id).toString() },
								null, null, null);
				if (c.moveToFirst())
					if (c.getString(c.getColumnIndex(Functions.DB_VERTRETUNG)) != null && c.getString(
							c.getColumnIndex(Functions.DB_VERTRETUNG))
							.equals("true")) {
						Intent intent = new Intent(timetableFragment.getActivity(), InfoActivity.class);
						intent.putExtra("type", "timetable_popup");
						intent.putExtra("id", c.getString(c.getColumnIndex(Functions.DB_REMOTE_ID)));
						timetableFragment.getActivity().startActivity(intent);
					}
			}
		});
		lv.setEmptyView(lay.findViewById(R.id.list_view_empty));
		timetableFragment.registerForContextMenu(lv);
		((TextView) lay.findViewById(R.id.list_view_empty))
				.setText(R.string.timetable_empty);
		((ViewPager) pager).addView(lay, 0);
		return lay;
	}

	public void updateExclude() {
		if (ownClass) {
			exclude_subjects[1] = (prefs.getString(Functions.GENDER, "")
					.equals("m")) ? "Sw" : "Sm";
			if (prefs.getString(Functions.RELIGION, "").equals(
					Functions.KATHOLISCH)) {
				exclude_subjects[2] = Functions.EVANGELISCH;
				exclude_subjects[3] = Functions.ETHIK;
			} else if (prefs.getString(Functions.RELIGION, "").equals(
					Functions.EVANGELISCH)) {
				exclude_subjects[2] = Functions.KATHOLISCH;
				exclude_subjects[3] = Functions.ETHIK;
			} else {
				exclude_subjects[2] = Functions.KATHOLISCH;
				exclude_subjects[3] = Functions.EVANGELISCH;
			}
		}
		else {
			exclude_subjects[1] = "%";
			exclude_subjects[2] = "%";
			exclude_subjects[3] = "%";
		}
	}

	public void setClass(String klasse, boolean ownClass) {
		this.klasse = klasse;
		this.ownClass = ownClass;
		if(ownClass)
			this.klasse = prefs.getString(Functions.FULL_CLASS, "null");
		updateExclude();
	}
	public void setTeacher(String teacher) {
		this.teacher = teacher;
		this.klasse = null;
	}

	public void updateCursor() {
		if (this.klasse == null) {
			for (int i = 0; i < getCount(); i++) {
				timetable_cursors[i] = myDB.query(
						Functions.DB_TIME_TABLE_TEACHERS, new String[] {
								Functions.DB_ROWID,
								Functions.DB_BREAK_SURVEILLANCE, Functions.DB_RAW_FACH, Functions.DB_VERTRETUNG,
								Functions.DB_FACH, Functions.DB_ROOM,
								Functions.DB_CLASS, Functions.DB_LENGTH,
								Functions.DB_HOUR, Functions.DB_DAY },
						Functions.DB_SHORT + "=? AND " + Functions.DB_DAY + "=?",
						new String[] { this.teacher, Integer.valueOf(i).toString() }, null, null, null);
				TimeTableAdapters[i].changeCursor(timetable_cursors[i]);
			}
		} else {
			exclude_subjects[4] = (this.ownClass) ? "1" : "%";
			if (ownClass)
				exclude_subjects[5] = "%" + klasse.substring(0, 2) + "%"
						+ klasse.substring(2, 3) + "%";
			else
				exclude_subjects[5] = klasse;
			String wherecond = Functions.DB_DAY + "=? AND  "
					+ Functions.DB_RAW_FACH + " != ? AND "
					+ Functions.DB_RAW_FACH + " != ? AND "
					+ Functions.DB_RAW_FACH + " != ? AND "
					+ Functions.DB_DISABLED + " != ? AND "
					+ Functions.DB_CLASS + " LIKE ?";
			for (int i = 0; i < getCount(); i++) {
				exclude_subjects[0] = Integer.valueOf(i).toString();
				timetable_cursors[i] = myDB.query(Functions.DB_TIME_TABLE,
						new String[] { Functions.DB_ROWID,
								Functions.DB_LEHRER, Functions.DB_FACH,
								Functions.DB_ROOM, Functions.DB_VERTRETUNG,
								Functions.DB_LENGTH, Functions.DB_HOUR,
								Functions.DB_DAY, Functions.DB_RAW_FACH },
						wherecond, exclude_subjects, null, null, null);
				TimeTableAdapters[i].changeCursor(timetable_cursors[i]);
			}
		}
	}

	public void closeCursorsDB() {
		for(int i = 0; i < getCount(); i++)
			timetable_cursors[i].close();
		myDB.close();
	}

	@Override
	public void destroyItem(View pager, int position, Object view) {
		((ViewPager) pager).removeView((View) view);
	}

	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view.equals(object);
	}

	@Override
	public void finishUpdate(View view) {
	}

	@Override
	public void restoreState(Parcelable p, ClassLoader c) {
	}

	@Override
	public Parcelable saveState() {
		return null;
	}

	@Override
	public void startUpdate(View view) {
	}

	@Override
	public void updateWhereCond(String searchText) {
		//don't need this
	}

	@Override
	public void updateList() {
		updateCursor();
	}
	public String getKlasse() {
		return this.klasse;
	}
	public String getTeacher() {
		return this.teacher;
	}
	public boolean getOwnClass() {
		return this.ownClass;
	}
}
