package com.lsg.app.timetable;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.util.Log;

import com.lsg.app.Functions;
import com.lsg.app.R;

public class TimeTable {
	private String[] excludeSubjects = new String[6];
	private final SQLiteDatabase myDB;
	private Cursor[] timetableCursors = new Cursor[5];
	private final SharedPreferences prefs;
	private String[] days = new String[5];
	private String displayingClass;
	private String displayingTeacher;
	private boolean ownClass = false;
	public TimeTable(Context context, SQLiteDatabase db) {
		myDB = db;
		prefs = PreferenceManager.getDefaultSharedPreferences(context);
		if (prefs.getBoolean(Functions.RIGHTS_TEACHER, false)) {
			setTeacher(prefs.getString(Functions.TEACHER_SHORT, ""));
		} else
			setClass("", true);
		days = context.getResources().getStringArray(R.array.days);
	}
	public String getDay(int day) {
		return days[day];
	}

	public void updateExclude() {
		if (ownClass) {
			excludeSubjects[1] = (prefs.getString(Functions.GENDER, "")
					.equals("m")) ? "Sw" : "Sm";
			if (prefs.getString(Functions.RELIGION, "").equals(
					Functions.KATHOLISCH)) {
				excludeSubjects[2] = Functions.EVANGELISCH;
				excludeSubjects[3] = Functions.ETHIK;
			} else if (prefs.getString(Functions.RELIGION, "").equals(
					Functions.EVANGELISCH)) {
				excludeSubjects[2] = Functions.KATHOLISCH;
				excludeSubjects[3] = Functions.ETHIK;
			} else {
				excludeSubjects[2] = Functions.KATHOLISCH;
				excludeSubjects[3] = Functions.EVANGELISCH;
			}
		} else {
			excludeSubjects[1] = "%";
			excludeSubjects[2] = "%";
			excludeSubjects[3] = "%";
		}
	}
	public void setClass(String klasse, boolean ownClass) {
		this.displayingClass = klasse;
		this.ownClass = ownClass;
		if (ownClass)
			this.displayingClass = prefs.getString(Functions.FULL_CLASS, "null");
		updateExclude();
	}
	public void setTeacher(String teacher) {
		this.displayingTeacher = teacher;
		this.displayingClass = null;
	}
	public void updateAll() {
		updateExclude();
		updateCursor();
	}
	public void updateCursor() {
		if (this.displayingClass == null) {
			for (int i = 0; i < timetableCursors.length; i++) {
				timetableCursors[i] = myDB.query(
						Functions.DB_TIME_TABLE_TEACHERS, new String[] {
								Functions.DB_ROWID,
								Functions.DB_BREAK_SURVEILLANCE,
								Functions.DB_RAW_FACH, Functions.DB_VERTRETUNG,
								Functions.DB_FACH, Functions.DB_ROOM,
								Functions.DB_CLASS, Functions.DB_LENGTH,
								Functions.DB_HOUR, Functions.DB_DAY },
						Functions.DB_SHORT + "=? AND " + Functions.DB_DAY
								+ "=?", new String[] { this.displayingTeacher,
								Integer.valueOf(i).toString() }, null, null,
						null);
			}
		} else {
			excludeSubjects[4] = (this.ownClass) ? "1" : "%";
			if (ownClass)
				excludeSubjects[5] = "%" + displayingClass.substring(0, 2) + "%"
						+ displayingClass.substring(2, 3) + "%";
			else
				excludeSubjects[5] = displayingClass;
			String wherecond = Functions.DB_DAY + "=? AND  "
					+ Functions.DB_RAW_FACH + " != ? AND "
					+ Functions.DB_RAW_FACH + " != ? AND "
					+ Functions.DB_RAW_FACH + " != ? AND "
					+ Functions.DB_DISABLED + " != ? AND " + Functions.DB_CLASS
					+ " LIKE ?";
			for (int i = 0; i < timetableCursors.length; i++) {
				excludeSubjects[0] = Integer.valueOf(i).toString();
				timetableCursors[i] = myDB.query(Functions.DB_TIME_TABLE,
						new String[] { Functions.DB_ROWID, Functions.DB_LEHRER,
								Functions.DB_FACH, Functions.DB_ROOM,
								Functions.DB_VERTRETUNG, Functions.DB_LENGTH,
								Functions.DB_HOUR, Functions.DB_DAY,
								Functions.DB_RAW_FACH }, wherecond,
						excludeSubjects, null, null, null);
				Log.d("where", excludeSubjects[i]);
			}
		}
	}

	public Cursor getCursor(int position) {
		if (position >= 0 && position < timetableCursors.length)
			return timetableCursors[position];
		else
			return null;
	}
	public void closeCursors() {
		for(int i = 0; i < timetableCursors.length; i++)
			timetableCursors[i].close();
	}
	public String getKlasse() {
		return displayingClass;
	}
	public String getTeacher() {
		return displayingTeacher;
	}
	public boolean isOwnClass() {
		return ownClass;
	}
}
