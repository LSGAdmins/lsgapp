package com.lsg.app.timetable;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.preference.PreferenceManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.lsg.app.Functions;
import com.lsg.app.InfoActivity;
import com.lsg.app.PagerTitles;
import com.lsg.app.R;
import com.lsg.app.interfaces.SQLlist;
import com.lsg.app.lib.FragmentActivityCallbacks;

public class TimeTableViewPagerAdapter extends PagerAdapter implements
		PagerTitles, SQLlist {
	private TimeTableAdapter[] timeTableAdapters = new TimeTableAdapter[5];
	private SQLiteDatabase myDB;
	private TimeTableFragment timetableFragment;
	
	private TimeTable timeTable;
	
	public TimeTableViewPagerAdapter(TimeTableFragment timetableFragment) {
		myDB = ((FragmentActivityCallbacks) timetableFragment.getActivity()).getDB();
		this.timetableFragment = timetableFragment;
		for (int i = 0; i < 5; i++)
			timeTableAdapters[i] = new TimeTableAdapter(timetableFragment.getActivity(),
					null);

		SQLiteStatement num_rows = myDB
				.compileStatement("SELECT COUNT(*) FROM "
						+ Functions.DB_TIME_TABLE);
		long count = num_rows.simpleQueryForLong();
		if (count == 0)
			timetableFragment.updateTimeTable();
		num_rows.close();
		timeTable = new TimeTable(timetableFragment.getActivity(), myDB);
		timeTable.updateCursor();
		changeCursors();
		}

	@Override
	public int getCount() {
		return 5;
	}

	@Override
	public String getTitle(int pos) {
		return timeTable.getDay(pos);
	}

	@Override
	public CharSequence getPageTitle(int position) {
		return timeTable.getDay(position);
	}

	@Override
	public Object instantiateItem(View pager, int position) {
		LayoutInflater inflater = (LayoutInflater) timetableFragment.getActivity()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		LinearLayout lay = (LinearLayout) inflater.inflate(R.layout.list, null);
		ListView lv = (ListView) lay.findViewById(android.R.id.list);
		lv.setAdapter(timeTableAdapters[position]);
		//vplan matching
		lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Cursor c = myDB.query(Functions.DB_TIME_TABLE, new String[] {
						Functions.DB_REMOTE_ID, Functions.DB_VERTRETUNG },
						Functions.DB_ROWID + "=?",
						new String[] { Long.valueOf(id).toString() }, null,
						null, null);
				if (c.moveToFirst())
					if (c.getString(c.getColumnIndex(Functions.DB_VERTRETUNG)) != null
							&& c.getString(
									c.getColumnIndex(Functions.DB_VERTRETUNG))
									.equals("true")) {
						Intent intent = new Intent(timetableFragment
								.getActivity(), InfoActivity.class);
						intent.putExtra("type", "timetable_popup");
						intent.putExtra("id", c.getString(c
								.getColumnIndex(Functions.DB_REMOTE_ID)));
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


	public void setClass(String klasse, boolean ownClass) {
		timeTable.setClass(klasse, ownClass);
	}

	public void setTeacher(String teacher) {
		timeTable.setTeacher(teacher);
	}

	public void closeCursors() {
		for (int i = 0; i < getCount(); i++)
			timeTable.closeCursors();
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
	public void updateWhereCond(String searchText) {
		// don't need this
	}

	@Override
	public void updateList() {
		timeTable.updateCursor();
		changeCursors();
	}

	public void changeCursors() {
		for (int i = 0; i < getCount(); i++)
			timeTableAdapters[i].changeCursor(timeTable.getCursor(i));
	}
	public String getKlasse() {
		return timeTable.getKlasse();
	}

	public String getTeacher() {
		return timeTable.getTeacher();
	}

	public boolean getOwnClass() {
		return timeTable.isOwnClass();
	}
}
