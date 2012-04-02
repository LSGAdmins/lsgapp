package com.lsg.app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;

public class TimeTableViewPagerAdapter extends PagerAdapter {
	private String[] exclude_subjects = new String[4];
	private final SQLiteDatabase myDB;
	public Cursor timetable_monday;
	public Cursor timetable_tuesday;
	public Cursor timetable_wednesday;
	public Cursor timetable_thursday;
	public Cursor timetable_friday;
	private TimetableAdapter timetableadap_monday;
	private TimetableAdapter timetableadap_tuesday;
	private TimetableAdapter timetableadap_wednesday;
	private TimetableAdapter timetableadap_thursday;
	private TimetableAdapter timetableadap_friday;
	private final Context context;
	private final SharedPreferences prefs;
	
	public TimeTableViewPagerAdapter(TimeTable act) {
		prefs = PreferenceManager.getDefaultSharedPreferences(act);
		exclude_subjects[1] = (prefs.getString(Functions.GENDER, "").equals("m")) ? "Sw" : "Sm";
		if(prefs.getString(Functions.RELIGION, "").equals(Functions.KATHOLISCH)) {
			exclude_subjects[2] = Functions.EVANGELISCH;
			exclude_subjects[3] = Functions.ETHIK;
		} else if(prefs.getString(Functions.RELIGION, "").equals(Functions.EVANGELISCH)) {
			exclude_subjects[2] = Functions.KATHOLISCH;
			exclude_subjects[3] = Functions.ETHIK;
		} else {
			exclude_subjects[2] = Functions.KATHOLISCH;
			exclude_subjects[3] = Functions.EVANGELISCH;
		}
		context = (Context) act;
		
		myDB = context.openOrCreateDatabase(Functions.DB_NAME, Context.MODE_PRIVATE, null);
		timetableadap_monday    = new TimetableAdapter(context, timetable_monday);
		timetableadap_tuesday   = new TimetableAdapter(context, timetable_tuesday);
		timetableadap_wednesday = new TimetableAdapter(context, timetable_wednesday);
		timetableadap_thursday  = new TimetableAdapter(context, timetable_thursday);
		timetableadap_friday    = new TimetableAdapter(context, timetable_friday);
		updateCursor();
		}
	@Override
	public int getCount() {
		return 5;
		}
	@Override
	public Object instantiateItem(View pager, int position) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		LinearLayout lay = (LinearLayout) inflater.inflate(R.layout.list, null);
		ListView lv = (ListView) lay.findViewById(android.R.id.list);
		switch(position) {
		case 0:
			lv.setAdapter(timetableadap_monday);
			break;
		case 1:
			lv.setAdapter(timetableadap_tuesday);
			break;
		case 2:
			lv.setAdapter(timetableadap_wednesday);
			break;
		case 3:
			lv.setAdapter(timetableadap_thursday);
			break;
		case 4:
			lv.setAdapter(timetableadap_friday);
			break;
			default:
				lv.setAdapter(timetableadap_monday);
				break;
		}
		lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Cursor c = myDB.query(Functions.DB_TIME_TABLE, new String[] {Functions.DB_RAW_FACH, Functions.DB_HOUR, Functions.DB_LEHRER},
						Functions.DB_ROWID + "=?", new String[] {new Long(id).toString()}, null, null, null);
				c.moveToFirst();
				String hour = Integer.valueOf(c.getInt(c.getColumnIndex(Functions.DB_HOUR)) + 1).toString();
				Cursor d = myDB.query(Functions.DB_VPLAN_TABLE, new String[] {Functions.DB_KLASSE, Functions.DB_STUNDE, Functions.DB_VERTRETUNGSTEXT, 
						Functions.DB_LEHRER, Functions.DB_FACH, Functions.DB_ART}, Functions.DB_RAW_FACH + "=? AND " + Functions.DB_STUNDE + "=? AND "
				+ Functions.DB_LEHRER + "=?",
				new String[] {c.getString(c.getColumnIndex(Functions.DB_RAW_FACH)), hour, c.getString(c.getColumnIndex(Functions.DB_LEHRER))}, null, null, null);
				d.moveToFirst();
				if(d.getCount() > 0) {
					String vtext = (!(d.getString(d.getColumnIndex(Functions.DB_VERTRETUNGSTEXT))).equals("null")) ? d.getString(
							d.getColumnIndex(Functions.DB_VERTRETUNGSTEXT)) + "\n" : "";
					AlertDialog.Builder builder = new AlertDialog.Builder(context);
					builder.setTitle(d.getString(d.getColumnIndex(Functions.DB_KLASSE)))
					.setMessage(d.getString(d.getColumnIndex(Functions.DB_FACH)) + " / " + d.getString(d.getColumnIndex(Functions.DB_STUNDE)) + ". "
					+ context.getString(R.string.hour)+ "\n" + vtext + d.getString(d.getColumnIndex(Functions.DB_ART)) + " " +context.getString(R.string.at)
					+ " " + d.getString(d.getColumnIndex(Functions.DB_LEHRER)))
					.setCancelable(true)
					.setNeutralButton(context.getString(R.string.ok), new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.cancel();
							}
						});
					AlertDialog alert = builder.create();
					alert.show();
					}
				}
			});
		((ViewPager)pager).addView(lay, 0);
		return lay;
		}
	public void updateCursor() {
		exclude_subjects[0] = "0";
		timetable_monday = myDB.query(Functions.DB_TIME_TABLE, new String[] {Functions.DB_ROWID, Functions.DB_LEHRER, Functions.DB_FACH, Functions.DB_RAUM, Functions.DB_LENGTH,
				Functions.DB_HOUR, Functions.DB_DAY, Functions.DB_RAW_FACH}, Functions.DB_DAY + "=? AND  " + Functions.DB_RAW_FACH + " != ? AND " + Functions.DB_RAW_FACH
				+ " != ? AND " +  Functions.DB_RAW_FACH + " != ?", exclude_subjects, null, null, null);
		timetableadap_monday.changeCursor(timetable_monday);
		

		exclude_subjects[0] = "1";
		timetable_tuesday = myDB.query(Functions.DB_TIME_TABLE, new String[] {Functions.DB_ROWID, Functions.DB_LEHRER, Functions.DB_FACH, Functions.DB_RAUM, Functions.DB_LENGTH,
				Functions.DB_HOUR, Functions.DB_DAY, Functions.DB_RAW_FACH}, Functions.DB_DAY + "=? AND  " + Functions.DB_RAW_FACH + " != ? AND " + Functions.DB_RAW_FACH
				+ " != ? AND " +  Functions.DB_RAW_FACH + " != ?", exclude_subjects, null, null, null);
		timetableadap_tuesday.changeCursor(timetable_tuesday);
		
		exclude_subjects[0] = "2";
		timetable_wednesday = myDB.query(Functions.DB_TIME_TABLE, new String[] {Functions.DB_ROWID, Functions.DB_LEHRER, Functions.DB_FACH, Functions.DB_RAUM, Functions.DB_LENGTH,
				Functions.DB_HOUR, Functions.DB_DAY, Functions.DB_RAW_FACH}, Functions.DB_DAY + "=? AND  " + Functions.DB_RAW_FACH + " != ? AND " + Functions.DB_RAW_FACH
				+ " != ? AND " +  Functions.DB_RAW_FACH + " != ?", exclude_subjects, null, null, null);
		timetableadap_wednesday.changeCursor(timetable_wednesday);
		
		exclude_subjects[0] = "3";
		timetable_thursday = myDB.query(Functions.DB_TIME_TABLE, new String[] {Functions.DB_ROWID, Functions.DB_LEHRER, Functions.DB_FACH, Functions.DB_RAUM, Functions.DB_LENGTH,
				Functions.DB_HOUR, Functions.DB_DAY, Functions.DB_RAW_FACH}, Functions.DB_DAY + "=? AND  " + Functions.DB_RAW_FACH + " != ? AND " + Functions.DB_RAW_FACH
				+ " != ? AND " +  Functions.DB_RAW_FACH + " != ?", exclude_subjects, null, null, null);
		timetableadap_thursday.changeCursor(timetable_thursday);
		
		exclude_subjects[0] = "4";
		timetable_friday = myDB.query(Functions.DB_TIME_TABLE, new String[] {Functions.DB_ROWID, Functions.DB_LEHRER, Functions.DB_FACH, Functions.DB_RAUM, Functions.DB_LENGTH,
				Functions.DB_HOUR, Functions.DB_DAY, Functions.DB_RAW_FACH}, Functions.DB_DAY + "=? AND  " + Functions.DB_RAW_FACH + " != ? AND " + Functions.DB_RAW_FACH
				+ " != ? AND " +  Functions.DB_RAW_FACH + " != ?", exclude_subjects, null, null, null);
		timetableadap_friday.changeCursor(timetable_friday);
	}
	public void closeCursorsDB() {
		timetable_monday.close();
		timetable_tuesday.close();
		timetable_wednesday.close();
		timetable_thursday.close();
		timetable_friday.close();
		myDB.close();
	}
	@Override
	public void destroyItem( View pager, int position, Object view ) {
		((ViewPager)pager).removeView((View) view);
		}
	
	@Override
	public boolean isViewFromObject( View view, Object object ) {
		return view.equals( object );
		}
	
	@Override
	public void finishUpdate( View view ) {}
	
	@Override
	public void restoreState( Parcelable p, ClassLoader c ) {}
	
	@Override
	public Parcelable saveState() {
		return null;
		}
	@Override
	public void startUpdate( View view ) {}
}
