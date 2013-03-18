package com.lsg.app.vplan;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
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
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.lsg.app.Functions;
import com.lsg.app.InfoActivity;
import com.lsg.app.R;
import com.lsg.app.ServiceHandler;
import com.lsg.app.SubjectList;
import com.lsg.app.WorkerService;
import com.lsg.app.interfaces.FragmentActivityCallbacks;
import com.lsg.app.lib.AdvancedWrapper;
import com.lsg.app.lib.LSGApplication;
import com.lsg.app.lib.TitleCompat;
import com.lsg.app.lib.TitleCompat.HomeCall;
import com.lsg.app.lib.TitleCompat.RefreshCall;
import com.lsg.app.sqlite.LSGSQliteOpenHelper;

public class VPlan implements WorkerService.WorkerClass {
	public static class VPlanUpdater {
		Context context;

		public VPlanUpdater(Context c) {
			context = c;
		}

		public String[] updatePupils() {
			SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(context);
			String add = "";
			try {
				add = "&"
						+ URLEncoder.encode("date", "UTF-8")
						+ "="
						+ URLEncoder.encode(prefs.getString("vplan_date", ""),
								"UTF-8")
						+ "&"
						+ URLEncoder.encode("time", "UTF-8")
						+ "="
						+ URLEncoder.encode(prefs.getString("vplan_time", ""),
								"UTF-8");
			} catch (UnsupportedEncodingException e) {
				Log.w("encoding", e.getMessage());
			}
			String get = Functions
					.getData(Functions.VP_URL, context, true, add);
			if (!get.equals("networkerror") && !get.equals("loginerror")
					&& !get.equals("noact")) {
				try {
					JSONArray jArray = new JSONArray(get);
					int i = 0;
					SQLiteDatabase myDB = LSGApplication.getSqliteDatabase();
					myDB.delete(LSGSQliteOpenHelper.DB_VPLAN_TABLE, null, null); // clear
																					// vertretungen
					while (i < jArray.length() - 1) {
						JSONObject jObject = jArray.getJSONObject(i);
						ContentValues values = new ContentValues();
						values.put(LSGSQliteOpenHelper.DB_CLASS_LEVEL,
								jObject.getString("klassenstufe"));
						values.put(LSGSQliteOpenHelper.DB_KLASSE,
								jObject.getString("klasse"));
						values.put(LSGSQliteOpenHelper.DB_STUNDE,
								jObject.getString("stunde"));
						values.put(LSGSQliteOpenHelper.DB_VERTRETER,
								jObject.getString("vertreter"));
						values.put(LSGSQliteOpenHelper.DB_RAW_VERTRETER,
								jObject.getString("rawvertreter"));
						values.put(LSGSQliteOpenHelper.DB_LEHRER,
								jObject.getString("lehrer"));
						values.put(LSGSQliteOpenHelper.DB_RAW_LEHRER,
								jObject.getString("rawlehrer"));
						values.put(LSGSQliteOpenHelper.DB_ROOM,
								jObject.getString("raum"));
						values.put(LSGSQliteOpenHelper.DB_TYPE,
								jObject.getString("art"));
						values.put(LSGSQliteOpenHelper.DB_VERTRETUNGSTEXT,
								jObject.getString("vertretungstext"));
						values.put(LSGSQliteOpenHelper.DB_FACH,
								jObject.getString("fach"));
						values.put(LSGSQliteOpenHelper.DB_RAW_FACH,
								jObject.getString("rawfach"));
						values.put(LSGSQliteOpenHelper.DB_DATE,
								jObject.getString("date"));
						values.put(LSGSQliteOpenHelper.DB_LENGTH,
								jObject.getInt("length"));
						values.put(LSGSQliteOpenHelper.DB_DAY_OF_WEEK,
								jObject.getInt("dayofweek"));
						myDB.insert(LSGSQliteOpenHelper.DB_VPLAN_TABLE, null,
								values);
						i++;
					}
					JSONObject jObject = jArray.getJSONObject(i);
					String date = jObject.getString("date");
					String time = jObject.getString("time");
					SharedPreferences.Editor edit = prefs.edit();
					edit.putString("vplan_date", date);
					edit.putString("vplan_time", time);
					edit.commit();
					Functions
							.cleanVPlanTable(LSGSQliteOpenHelper.DB_VPLAN_TABLE);
				} catch (JSONException e) {
					Log.w("jsonerror", e.getMessage());
					return new String[] { "json",
							context.getString(R.string.jsonerror) };
				}
			} else if (get.equals("noact"))
				return new String[] { "noact",
						context.getString(R.string.noact) };
			else if (get.equals("loginerror"))
				return new String[] { "loginerror",
						context.getString(R.string.loginerror) };
			else if (get.equals("networkerror"))
				return new String[] { "networkerror",
						context.getString(R.string.networkerror) };
			else
				return new String[] { "unknownerror",
						context.getString(R.string.unknownerror) };
			return new String[] { "success", " " };
		}

		public String[] updateTeachers() {
			SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(context);
			String add = "";
			try {
				add = "&"
						+ URLEncoder.encode("date", "UTF-8")
						+ "="
						+ URLEncoder.encode(
								prefs.getString("vplan_teacher_date", ""),
								"UTF-8")
						+ "&"
						+ URLEncoder.encode("time", "UTF-8")
						+ "="
						+ URLEncoder.encode(
								prefs.getString("vplan_teacher_time", ""),
								"UTF-8") + "&"
						+ URLEncoder.encode("type", "UTF-8") + "="
						+ URLEncoder.encode("teachers", "UTF-8");
			} catch (UnsupportedEncodingException e) {
				Log.w("encoding", e.getMessage());
			}
			String get = Functions
					.getData(Functions.VP_URL, context, true, add);
			if (!get.equals("networkerror") && !get.equals("loginerror")
					&& !get.equals("noact") && !get.equals("rights")) {
				try {
					JSONArray jArray = new JSONArray(get);
					int i = 0;
					SQLiteDatabase myDB = LSGApplication.getSqliteDatabase();
					myDB.delete(LSGSQliteOpenHelper.DB_VPLAN_TEACHER, null,
							null); // clear vertretungen
					while (i < jArray.length() - 1) {
						JSONObject jObject = jArray.getJSONObject(i);
						ContentValues values = new ContentValues();
						values.put(LSGSQliteOpenHelper.DB_CLASS_LEVEL,
								jObject.getString("klassenstufe"));
						values.put(LSGSQliteOpenHelper.DB_KLASSE,
								jObject.getString("klasse"));
						values.put(LSGSQliteOpenHelper.DB_STUNDE,
								jObject.getString("stunde"));
						values.put(LSGSQliteOpenHelper.DB_VERTRETER,
								jObject.getString("vertreter"));
						values.put(LSGSQliteOpenHelper.DB_RAW_VERTRETER,
								jObject.getString("rawvertreter"));
						values.put(LSGSQliteOpenHelper.DB_LEHRER,
								jObject.getString("lehrer"));
						values.put(LSGSQliteOpenHelper.DB_RAW_LEHRER,
								jObject.getString("rawlehrer"));
						values.put(LSGSQliteOpenHelper.DB_ROOM,
								jObject.getString("raum"));
						values.put(LSGSQliteOpenHelper.DB_TYPE,
								jObject.getString("art"));
						values.put(LSGSQliteOpenHelper.DB_VERTRETUNGSTEXT,
								jObject.getString("vertretungstext"));
						values.put(LSGSQliteOpenHelper.DB_FACH,
								jObject.getString("fach"));
						values.put(LSGSQliteOpenHelper.DB_RAW_FACH,
								jObject.getString("rawfach"));
						values.put(LSGSQliteOpenHelper.DB_DATE,
								jObject.getString("date"));
						values.put(LSGSQliteOpenHelper.DB_LENGTH,
								jObject.getInt("length"));
						myDB.insert(LSGSQliteOpenHelper.DB_VPLAN_TEACHER, null,
								values);
						i++;
					}
					JSONObject jObject = jArray.getJSONObject(i);
					String date = jObject.getString("date");
					String time = jObject.getString("time");
					SharedPreferences.Editor edit = prefs.edit();
					edit.putString("vplan_teacher_date", date);
					edit.putString("vplan_teacher_time", time);
					edit.commit();
				} catch (JSONException e) {
					Log.w("jsonerror", e.getMessage());
					return new String[] { "json",
							context.getString(R.string.jsonerror) };
				}
			} else if (get.equals("noact"))
				return new String[] { "noact",
						context.getString(R.string.noact) };
			else if (get.equals("loginerror"))
				return new String[] { "loginerror",
						context.getString(R.string.loginerror) };
			else if (get.equals("networkerror"))
				return new String[] { "networkerror",
						context.getString(R.string.networkerror) };
			else
				return new String[] { "unknownerror",
						context.getString(R.string.unknownerror) };
			return new String[] { "success", " " };
		}
	}

	public void update(int what, Context c) {
		VPlanUpdater udp = new VPlanUpdater(c);
		switch (what) {
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