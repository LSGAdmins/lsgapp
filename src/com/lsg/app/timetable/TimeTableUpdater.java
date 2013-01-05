package com.lsg.app.timetable;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.util.Log;

import com.lsg.app.Functions;
import com.lsg.app.R;
import com.lsg.app.lib.LSGApplication;
import com.lsg.app.sqlite.LSGSQliteOpenHelper;

public class TimeTableUpdater {
	private Context context;

	public TimeTableUpdater(Context c) {
		context = c;
		TimeTableUpdater.blacklistTimeTable(context);
	}

	public String[] updatePupils() {
		return updatePupils(false);
	}

	public String[] updatePupils(boolean force) {
		Log.d("asdf", "updatepupils");
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		String add = "";
		try {
			add = "&"
					+ URLEncoder.encode("date", "UTF-8")
					+ "="
					+ URLEncoder.encode(prefs.getString("timetable_date", ""),
							"UTF-8")
					+ "&"
					+ URLEncoder.encode("time", "UTF-8")
					+ "="
					+ URLEncoder.encode(prefs.getString("timetable_time", ""),
							"UTF-8")
					+ "&"
					+ URLEncoder.encode("class", "UTF-8")
					+ "="
					+ URLEncoder.encode(
							prefs.getString(Functions.FULL_CLASS, ""), "UTF-8");
			if (force)
				add += "&" + URLEncoder.encode("force", "UTF-8") + "="
						+ URLEncoder.encode("true", "UTF-8");
		} catch (UnsupportedEncodingException e) {
			Log.d("encoding", e.getMessage());
		}
		String get = Functions.getData(Functions.TIMETABLE_URL, context, true,
				add);
		Log.d("get", get);
		if (!get.equals("networkerror") && !get.equals("loginerror")
				&& !get.equals("noact")) {
			try {
				JSONArray classes = new JSONArray(get);
				SQLiteDatabase myDB = LSGApplication.getSqliteDatabase();
				// clear timetable
				myDB.delete(LSGSQliteOpenHelper.DB_TIME_TABLE, null, null);
				// clear headers
				myDB.delete(LSGSQliteOpenHelper.DB_TIME_TABLE_HEADERS_PUPILS,
						null, null);
				for (int i = 0; i < classes.length(); i++) {
					JSONArray one_class = classes.getJSONArray(i);
					JSONObject class_info = one_class.getJSONObject(0);
					String date = class_info.getString("date");
					String time = class_info.getString("time");
					String one = class_info.getString("one");
					String two = class_info.getString("two");
					String klasse = class_info.getString("klasse");
					SharedPreferences.Editor edit = prefs.edit();
					edit.putString("timetable_date", date);
					edit.putString("timetable_time", time);
					ContentValues headerval = new ContentValues();
					headerval.put(LSGSQliteOpenHelper.DB_TEACHER, one);
					headerval.put(LSGSQliteOpenHelper.DB_SECOND_TEACHER, two);
					headerval.put(LSGSQliteOpenHelper.DB_KLASSE, klasse);
					myDB.insert(
							LSGSQliteOpenHelper.DB_TIME_TABLE_HEADERS_PUPILS,
							null, headerval);
					edit.commit();
					for (int ii = 1; ii < one_class.length(); ii++) {
						JSONObject jObject = one_class.getJSONObject(ii);
						ContentValues values = new ContentValues();
						values.put(LSGSQliteOpenHelper.DB_LEHRER,
								jObject.getString("teacher"));
						values.put(LSGSQliteOpenHelper.DB_FACH,
								jObject.getString("subject"));
						values.put(LSGSQliteOpenHelper.DB_RAW_FACH,
								jObject.getString("rawsubject"));
						values.put(LSGSQliteOpenHelper.DB_ROOM,
								jObject.getString("room"));
						values.put(LSGSQliteOpenHelper.DB_LENGTH,
								jObject.getInt("length"));
						values.put(LSGSQliteOpenHelper.DB_DAY,
								jObject.getInt("day"));
						values.put(LSGSQliteOpenHelper.DB_HOUR,
								jObject.getInt("hour"));
						values.put(LSGSQliteOpenHelper.DB_CLASS,
								jObject.getString("class"));
						values.put(LSGSQliteOpenHelper.DB_RAW_LEHRER,
								jObject.getString("rawteacher"));
						Cursor c = myDB
								.query(LSGSQliteOpenHelper.DB_EXCLUDE_TABLE,
										new String[] { LSGSQliteOpenHelper.DB_ROWID },
										LSGSQliteOpenHelper.DB_TEACHER
												+ "=? AND "
												+ LSGSQliteOpenHelper.DB_RAW_FACH
												+ "=? AND "
												+ LSGSQliteOpenHelper.DB_HOUR
												+ "=? AND "
												+ LSGSQliteOpenHelper.DB_DAY
												+ "=?",
										new String[] {
												values.getAsString(LSGSQliteOpenHelper.DB_RAW_LEHRER),
												values.getAsString(LSGSQliteOpenHelper.DB_RAW_FACH),
												values.getAsString(LSGSQliteOpenHelper.DB_HOUR),
												values.getAsString(LSGSQliteOpenHelper.DB_DAY) },
										null, null, null);
						// c.moveToFirst();
						if (c.getCount() > 0) {
							values.put(LSGSQliteOpenHelper.DB_DISABLED, 1);
						} else
							values.put(LSGSQliteOpenHelper.DB_DISABLED, 2);
						c.close();
						myDB.insert(LSGSQliteOpenHelper.DB_TIME_TABLE, null,
								values);
					}
				}
			} catch (JSONException e) {
				Log.d("json", e.getMessage());
				return new String[] { "json",
						context.getString(R.string.jsonerror) };
			}
		} else if (get.equals("networkerror")) {
			return new String[] { "networkerror",
					context.getString(R.string.networkerror) };
		} else if (get.equals("loginerror"))
			return new String[] { "loginerror",
					context.getString(R.string.loginerror) };
		return new String[] { "success", "" };
	}

	public String[] updateTeachers() {
		return updatePupils(false);
	}

	public String[] updateTeachers(boolean force) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		String add = "";
		try {
			add = "&"
					+ URLEncoder.encode("date", "UTF-8")
					+ "="
					+ URLEncoder.encode(
							prefs.getString("timetable_teachers_date", ""),
							"UTF-8")
					+ "&"
					+ URLEncoder.encode("time", "UTF-8")
					+ "="
					+ URLEncoder.encode(
							prefs.getString("timetable_teachers_time", ""),
							"UTF-8");
			if (force)
				add += "&" + URLEncoder.encode("force", "UTF-8") + "="
						+ URLEncoder.encode("true", "UTF-8");
		} catch (UnsupportedEncodingException e) {
			Log.d("encoding", e.getMessage());
		}
		String get = Functions.getData(Functions.TIMETABLE_TEACHERS_URL,
				context, true, add);
		
		if (!get.equals("networkerror") && !get.equals("loginerror")
				&& !get.equals("noact")) {
			try {
				JSONArray classes = new JSONArray(get);
				SQLiteDatabase myDB = LSGApplication.getSqliteDatabase();
				// clear timetable
				myDB.delete(LSGSQliteOpenHelper.DB_TIME_TABLE_TEACHERS, null,
						null);
				// clear headers
				myDB.delete(LSGSQliteOpenHelper.DB_TIME_TABLE_HEADERS_TEACHERS,
						null, null);
				for (int i = 0; i < classes.length(); i++) {
					JSONArray one_teacher = classes.getJSONArray(i);
					JSONObject teacher_info = one_teacher.getJSONObject(0);
					String date = teacher_info.getString("date");
					String time = teacher_info.getString("time");
					String name = teacher_info.getString("name");
					String short_ = teacher_info.getString("short");
					SharedPreferences.Editor edit = prefs.edit();
					edit.putString("timetable_teachers_date", date);
					edit.putString("timetable_teachers_time", time);
					ContentValues headerval = new ContentValues();
					headerval.put(LSGSQliteOpenHelper.DB_TEACHER, name);
					headerval.put(LSGSQliteOpenHelper.DB_SHORT, short_);
					myDB.insert(
							LSGSQliteOpenHelper.DB_TIME_TABLE_HEADERS_TEACHERS,
							null, headerval);
					edit.commit();
					for (int ii = 1; ii < one_teacher.length(); ii++) {
						JSONObject jObject = one_teacher.getJSONObject(ii);
						ContentValues values = new ContentValues();
						values.put(LSGSQliteOpenHelper.DB_SHORT, short_);
						values.put(LSGSQliteOpenHelper.DB_BREAK_SURVEILLANCE,
								jObject.getString("pausenaufsicht"));
						values.put(LSGSQliteOpenHelper.DB_RAW_FACH,
								jObject.getString("rawfach"));
						values.put(LSGSQliteOpenHelper.DB_FACH,
								jObject.getString("fach"));
						values.put(LSGSQliteOpenHelper.DB_ROOM,
								jObject.getString("room"));
						values.put(LSGSQliteOpenHelper.DB_CLASS,
								jObject.getString("class"));
						values.put(LSGSQliteOpenHelper.DB_LENGTH,
								jObject.getInt("length"));
						values.put(LSGSQliteOpenHelper.DB_DAY,
								jObject.getInt("day"));
						values.put(LSGSQliteOpenHelper.DB_HOUR,
								jObject.getInt("hour"));
						myDB.insert(LSGSQliteOpenHelper.DB_TIME_TABLE_TEACHERS,
								null, values);
					}
				}
			} catch (JSONException e) {
				Log.d("json", e.getMessage());
				return new String[] { "json",
						context.getString(R.string.jsonerror) };
			}
		} else if (get.equals("networkerror")) {
			return new String[] { "networkerror",
					context.getString(R.string.networkerror) };
		} else if (get.equals("loginerror"))
			return new String[] { "loginerror",
					context.getString(R.string.loginerror) };
		return new String[] { "success", "" };
	}

	public static void blacklistTimeTable(Context context) {
		SQLiteDatabase myDB = LSGApplication.getSqliteDatabase();
		Cursor allSubjects = myDB.query(LSGSQliteOpenHelper.DB_TIME_TABLE,
				new String[] { LSGSQliteOpenHelper.DB_ROWID,
						LSGSQliteOpenHelper.DB_DAY,
						LSGSQliteOpenHelper.DB_HOUR,
						LSGSQliteOpenHelper.DB_RAW_FACH,
						LSGSQliteOpenHelper.DB_RAW_LEHRER }, null, null, null,
				null, null);
		allSubjects.moveToFirst();
		ContentValues vals = new ContentValues();
		vals.put(LSGSQliteOpenHelper.DB_DISABLED, 2);
		myDB.update(LSGSQliteOpenHelper.DB_TIME_TABLE, vals, null, null);
		if (allSubjects.getCount() > 0)
			do {
				Cursor exclude = myDB
						.query(LSGSQliteOpenHelper.DB_EXCLUDE_TABLE,
								new String[] { LSGSQliteOpenHelper.DB_ROWID },
								LSGSQliteOpenHelper.DB_TEACHER + "=? AND "
										+ LSGSQliteOpenHelper.DB_RAW_FACH
										+ "=? AND "
										+ LSGSQliteOpenHelper.DB_HOUR
										+ "=? AND "
										+ LSGSQliteOpenHelper.DB_DAY + "=?",
								new String[] {
										allSubjects.getString(allSubjects
												.getColumnIndex(LSGSQliteOpenHelper.DB_RAW_LEHRER)),
										allSubjects.getString(allSubjects
												.getColumnIndex(LSGSQliteOpenHelper.DB_RAW_FACH)),
										allSubjects.getString(allSubjects
												.getColumnIndex(LSGSQliteOpenHelper.DB_HOUR)),
										allSubjects.getString(allSubjects
												.getColumnIndex(LSGSQliteOpenHelper.DB_DAY)) },
								null, null, null);
				Cursor exclude_oldstyle = myDB
						.query(LSGSQliteOpenHelper.DB_EXCLUDE_TABLE,
								new String[] { LSGSQliteOpenHelper.DB_ROWID },
								LSGSQliteOpenHelper.DB_RAW_FACH + "=? AND "
										+ LSGSQliteOpenHelper.DB_TYPE + "=?",
								new String[] {
										allSubjects.getString(allSubjects
												.getColumnIndex(LSGSQliteOpenHelper.DB_RAW_FACH)),
										"oldstyle" }, null, null, null);
				if (exclude.getCount() > 0 || exclude_oldstyle.getCount() > 0) {
					myDB.execSQL(
							"UPDATE " + LSGSQliteOpenHelper.DB_TIME_TABLE
									+ " SET " + LSGSQliteOpenHelper.DB_DISABLED
									+ "=? WHERE "
									+ LSGSQliteOpenHelper.DB_ROWID + "=?",
							new String[] {
									"1",
									allSubjects.getString(allSubjects
											.getColumnIndex(LSGSQliteOpenHelper.DB_ROWID)) });
				}
				exclude.close();
				exclude_oldstyle.close();
			} while (allSubjects.moveToNext());
		allSubjects.close();
		try {
		} catch (Exception e) {

		}
	}
}
