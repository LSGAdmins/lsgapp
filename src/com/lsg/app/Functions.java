package com.lsg.app;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Calendar;

import org.apache.http.util.EncodingUtils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;

import com.google.android.gcm.GCMRegistrar;
import com.lsg.app.interfaces.SQLlist;
import com.lsg.app.lib.LSGApplication;
import com.lsg.app.sqlite.LSGSQliteOpenHelper;

public class Functions {
	public static final String   TAG            = "LSGäpp";
	
	public static final String   BLACKWHITELIST = "blackwhitelist";
	public static final String   BLACKLIST      = "blacklist";
	public static final String   WHITELIST      = "whitelist";
	
	//c2dm
	public static final String   GCM_ID = "521051077972";
	
	public static final String   UPDATE_URL           = "http://linux.lsg.musin.de/cp/downloads/lsgapp.apk";
	public static final String   UPDATE_CHECK_URL     = "http://linux.lsg.musin.de/cp/checkUpdate.php?version=";
	public static final String   VP_URL               = "http://linux.lsg.musin.de/cp/vp_app.php";
	public static final String   EVENT_URL            = "http://linux.lsg.musin.de/cp/termine_app.php";
	public static final String   CLASS_URL            = "http://linux.lsg.musin.de/cp/getClass.php?all=true";
	public static final String   SUBJECT_URL          = "http://linux.lsg.musin.de/cp/fach_kuerzel.php";
	public static final String   REGISTRATION_URL     = "http://linux.lsg.musin.de/cp/register_client.php";
	public static final String   TIMETABLE_URL        = "http://linux.lsg.musin.de/cp/timetable.php";
	public static final String   TIMETABLE_TEACHERS_URL        = "http://linux.lsg.musin.de/cp/timetable_teachers.php";
	public static final String   LOGIN_TEST_URL       = "http://linux.lsg.musin.de/cp/setup.php?act=checklogin";
	public static final String   PERSON_DATA_URL      = "http://linux.lsg.musin.de/cp/setup.php?act=getdata";
	public static final String   PERSON_DATA_SEND_URL = "http://linux.lsg.musin.de/cp/setup.php?act=setdata";
	public static final String   NEWS_URL             = "http://linux.lsg.musin.de/cp/news_app.php";
	public static final String   ERROR_URL            = "http://linux.lsg.musin.de/cp/error.php";
	public static final String   API_VERSION          = "3";
	
	public static final String   class_key  = "class";
	public static final String[] exclude    = {"Q11", "Q12"};
	
	public static final String HELPABOUT = "helpabout";
	public static final String help      = "help";
	public static final String about     = "about";
	
	public static final String NEWS_PUPILS = "news_pupils";
	public static final String NEWS_TEACHERS = "news_teachers";
	
	public static final String RELIGION = "religion";
	public static final String KATHOLISCH = "K";
	public static final String EVANGELISCH = "Ev";
	public static final String ETHIK = "Eth";

	public static final String GENDER             = "gender";
	
	public static final String FULL_CLASS         = "full_class";
	//slidemenu
	public static final int TYPE_PAGE             = 0;
	public static final int TYPE_INFO             = 1;
	public static final String IS_LOGGED_IN       = "isloggedin";
	//rights
	public static final String RIGHTS_PUPIL = "pupil";
	public static final String RIGHTS_TEACHER = "teacher";
	public static final String RIGHTS_ADMIN = "admin";
	
	//overlay
	public static final String SHOWN              = "_shown";
	public static final String OVERLAY_HOMEBUTTON = "home";
	public static final String OVERLAY_SWIPE      = "swipe";
	
	/**
	 * set holo theme in android 4
	 * @param dialog if it should be a dialog
	 * @param homeasup if there should be a "homeasup" navigation
	 * @param act the calling activity
	 */
	public static void setTheme(boolean dialog, boolean homeasup, Activity act) {
		if(homeasup && Functions.getSDK() >= 11) {
			homeUp(act);
		}
	}
	/**
	 * method to set home as up
	 * @param act the calling activity
	 */
	public static void homeUp(Activity act) {
		try {
			AdvancedWrapper advWrapper = new AdvancedWrapper();
			advWrapper.homeasup(act);
		} catch (Exception e) {
		}
	}
	public static void alwaysDisplayFastScroll(ListView lv) {
		if (Functions.getSDK() >= 11) {
			try {
				AdvancedWrapper advWrapper = new AdvancedWrapper();
				advWrapper.alwaysDisplayFastScroll(lv);
			} catch (Exception e) {
			}
		}
	}
	/**
	 * initialize the vplan-pull
	 * @param context the app context
	 */
	public static void setAlarm(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		if(!prefs.getBoolean("autopullvplan", false)) {
			Log.d("Functions.java", "no auto pull");
			return;
		}
		else
			Log.d("Functions.java", "setAlarm");
		int time_add = 30;
		try {
			time_add = Integer.valueOf(prefs.getString("autopull_intervall", "60"));
		} catch(NumberFormatException e) {
			Log.d("NumberFormatException", e.getMessage());
		}
		if(time_add == 0)
			time_add = 1;
		Functions.setAlarm(context, time_add);
	}
	/**
	 * set an alarm to pull the vplan
	 * @param context the app context
	 * @param time_add the time to wait
	 */
	public static void setAlarm(Context context, int time_add) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		if(!prefs.getBoolean("autopullvplan", false)) {
			Log.d("Functions.java", "no auto pull");
			return;
		}
		Log.d("time", Integer.valueOf((time_add*1000*60)).toString());
		Intent intent = new Intent(context, UpdateBroadcastReceiver.class);
		intent.setAction("update_vplan");
		boolean alarmUp = (PendingIntent.getBroadcast(context, 192837, intent, PendingIntent.FLAG_NO_CREATE) != null);
		Log.d("alarmUp", Boolean.valueOf(alarmUp).toString());
		PendingIntent sender = PendingIntent.getBroadcast(context, 192837, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + (time_add*1000*60), sender);
		}
	/**
	 * style a listview, to get more beautiful dividers
	 * @param lv the listview
	 * @param context the context
	 */
	public static void styleListView(ListView lv, Context context) {
		if(Functions.getSDK() >= 11) {
			ColorDrawable sage = new ColorDrawable(context.getResources().getColor(R.color.seperatorgrey));
			lv.setDivider(sage);
			lv.setDividerHeight(2);
		}
	}
	
	public static int getSDK() { //needed for compat with 1.5, no longer supported
		return Build.VERSION.SDK_INT;
	}
	/**
	 * a helper method to connect to the api
	 * @param urlString the url to open
	 * @param context the app context
	 * @param login if the login params are needed
	 * @param add add this to the query string in post
	 * @return the fetched data
	 */
	public static String getData(String urlString, Context context, boolean login, String add) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		try {
			String data = "";
			if (login)
				data = URLEncoder.encode("username", "UTF-8")
						+ "="
						+ URLEncoder.encode(prefs.getString("username", ""),
								"UTF-8")
						+ "&"
						+ URLEncoder.encode("password", "UTF-8")
						+ "="
						+ URLEncoder.encode(prefs.getString("password", ""),
								"UTF-8");
			data = data + "&" + URLEncoder.encode("api", "UTF-8") + "="
					+ URLEncoder.encode(Functions.API_VERSION, "UTF-8") + add;
			URL url = new URL(urlString);
        	HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        	// If you invoke the method setDoOutput(true) on the URLConnection, it will always use the POST method.
        	conn.setDoOutput(true);
        	conn.setRequestMethod("POST");
        	OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
        	wr.write(data);
        	wr.flush();
        	wr.close();
        	//get response
        	InputStream response = conn.getInputStream();
        	BufferedReader reader = new BufferedReader(new InputStreamReader(response), 8 * 1024);
        	String line;
        	String get = "";
        	while ((line = reader.readLine()) != null) {
        		get += line;
        		}
        	return get;
		} catch(Exception e) {
			Log.d("except in fetching data: ", e.getMessage() + " ");
			e.printStackTrace();
			return "networkerror";
			}
	}
	/**
	 * Clean database of vplan - used when the vplan is one day old, but there's no new version
	 * @param act the app context to access the database
	 */
	public static void cleanVPlanTable(String table) {
		Calendar now  = Calendar.getInstance();
		int year_now  = now.get(Calendar.YEAR);
		int month_now = now.get(Calendar.MONTH)+1;
		int day_now   = now.get(Calendar.DAY_OF_MONTH);
		SQLiteDatabase myDB;
		myDB = LSGApplication.getSqliteDatabase();
		
		Cursor result = myDB.query(table, new String[] {LSGSQliteOpenHelper.DB_DATE}, null, null, null, null, null);
		result.moveToFirst();
		int i = 0;
		while(i < result.getCount()) {
			String date = result.getString(result.getColumnIndex(LSGSQliteOpenHelper.DB_DATE));
			String[] splitdate = date.split("\\.");
			int year  = Integer.valueOf(splitdate[2]);
			int month = Integer.valueOf(splitdate[1]);
			int day   = Integer.valueOf(splitdate[0]);
			
			boolean isvalid = false;
			if(!(year < year_now+1)) {
				isvalid = true;
				}
			else if(year == year_now) {
				if(!(month < month_now+2)) {
					isvalid = true;
					}
				else if(month == month_now) {
					if(!(day < day_now)) {
						isvalid = true;
						}
					}
				}
			if(!isvalid) {
				myDB.execSQL("DELETE FROM " + table + " WHERE " + LSGSQliteOpenHelper.DB_DATE + " = '" + date + "'");
				}
			i++;
		}
		result.close();
		try {
		matchVPlanTimeTable();
		} catch(Exception e) {
			Log.e("LSGäpp", "exception in matching vplan");
			e.printStackTrace();
		}
	}
	public static void matchVPlanTimeTable() {
		SQLiteDatabase myDB = LSGApplication.getSqliteDatabase();
		ContentValues vals = new ContentValues();
		vals.put(LSGSQliteOpenHelper.DB_VERTRETUNG, "false");
		myDB.update(LSGSQliteOpenHelper.DB_TIME_TABLE, vals, null, null);
		vals.put(LSGSQliteOpenHelper.DB_VERTRETUNG, "true");

		Cursor c = myDB
				.query(LSGSQliteOpenHelper.DB_VPLAN_TABLE, new String[] {
						LSGSQliteOpenHelper.DB_ROWID,
						LSGSQliteOpenHelper.DB_DAY_OF_WEEK,
						LSGSQliteOpenHelper.DB_STUNDE,
						LSGSQliteOpenHelper.DB_RAW_LEHRER,
						LSGSQliteOpenHelper.DB_RAW_FACH }, null, null, null,
						null, null);
		c.moveToFirst();
		if (c.getCount() > 0)
			do {
				vals.put(LSGSQliteOpenHelper.DB_REMOTE_ID,
						c.getString(c.getColumnIndex(LSGSQliteOpenHelper.DB_ROWID)));
				long num_rows = myDB
						.update(LSGSQliteOpenHelper.DB_TIME_TABLE,
								vals,
								LSGSQliteOpenHelper.DB_DAY + "=? AND "
										+ LSGSQliteOpenHelper.DB_HOUR + "=? AND "
										+ LSGSQliteOpenHelper.DB_RAW_LEHRER
										+ " LIKE ? AND "
										+ LSGSQliteOpenHelper.DB_RAW_FACH + "=?",
								new String[] {
										c.getString(c
												.getColumnIndex(LSGSQliteOpenHelper.DB_DAY_OF_WEEK)),
										Integer.valueOf(
												c.getInt(c
														.getColumnIndex(LSGSQliteOpenHelper.DB_STUNDE)) - 1)
												.toString(),
										"%"
												+ c.getString(c
														.getColumnIndex(LSGSQliteOpenHelper.DB_RAW_LEHRER))
												+ "%",
										c.getString(c
												.getColumnIndex(LSGSQliteOpenHelper.DB_RAW_FACH)) });
				int ii = 1;

				while (num_rows == 0
						&& c.getInt(c.getColumnIndex(LSGSQliteOpenHelper.DB_STUNDE)) - ii >= 0) {
					num_rows = myDB
							.update(LSGSQliteOpenHelper.DB_TIME_TABLE,
									vals,
									LSGSQliteOpenHelper.DB_DAY + "=? AND "
											+ LSGSQliteOpenHelper.DB_HOUR + "=? AND "
											+ LSGSQliteOpenHelper.DB_RAW_LEHRER
											+ " LIKE ? AND "
											+ LSGSQliteOpenHelper.DB_RAW_FACH + "=? AND "
											+ LSGSQliteOpenHelper.DB_LENGTH + "=?",
									new String[] {
											c.getString(c
													.getColumnIndex(LSGSQliteOpenHelper.DB_DAY_OF_WEEK)),
											Integer.valueOf(
													c.getInt(c
															.getColumnIndex(LSGSQliteOpenHelper.DB_STUNDE))
															- 1 - ii)
													.toString(),
											"%"
													+ c.getString(c
															.getColumnIndex(LSGSQliteOpenHelper.DB_RAW_LEHRER))
													+ "%",
											c.getString(c
													.getColumnIndex(LSGSQliteOpenHelper.DB_RAW_FACH)),
											Integer.valueOf(1 + ii).toString() });
					ii++;
				}
			} while (c.moveToNext());
	}

	/**
	 * add items to blacklist / remove items from blacklist
	 * @param menu the menu to show
	 * @param v the calling view
	 * @param menuInfo information about the calling view, eg. id
	 * @param context the app context
	 * @param table the table to use, either Functions.DB_VPLAN_TABLE or Functions.DB_SUBJECT_TABLE
	 */
	public static void createContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo, Context context, String table) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
		String klasse  = "";
		String rawfach = "";
		String fach    = "";
		int conmenu = 0;
		SQLiteDatabase myDB = LSGApplication.getSqliteDatabase();
		if (table.equals(LSGSQliteOpenHelper.DB_VPLAN_TABLE)) {
			Cursor cur = myDB.query(LSGSQliteOpenHelper.DB_VPLAN_TABLE,
					new String[] { LSGSQliteOpenHelper.DB_KLASSE,
							LSGSQliteOpenHelper.DB_FACH,
							LSGSQliteOpenHelper.DB_RAW_FACH },
					LSGSQliteOpenHelper.DB_ROWID + " = ?", new String[] { Long
							.valueOf(info.id).toString() }, null, null, null);
			cur.moveToFirst();
			
			klasse  = cur.getString(cur.getColumnIndex(LSGSQliteOpenHelper.DB_KLASSE));
			rawfach = cur.getString(cur.getColumnIndex(LSGSQliteOpenHelper.DB_RAW_FACH));
			fach    = cur.getString(cur.getColumnIndex(LSGSQliteOpenHelper.DB_FACH));
			cur.close();
			
			int i = 0;
			while(i < Functions.exclude.length) {
				if(klasse.contains(Functions.exclude[i]))
					conmenu = 1;
				i++;
				}
			if(klasse.equals("null"))
				conmenu = 1;
			} else if(table.equals(LSGSQliteOpenHelper.DB_TIME_TABLE)) {
			Log.d("create", "context4timetable");
			Cursor cur = myDB.query(LSGSQliteOpenHelper.DB_TIME_TABLE,
					new String[] { LSGSQliteOpenHelper.DB_CLASS,
							LSGSQliteOpenHelper.DB_FACH,
							LSGSQliteOpenHelper.DB_RAW_FACH },
					LSGSQliteOpenHelper.DB_ROWID + " = ?", new String[] { Long
							.valueOf(info.id).toString() }, null, null, null);
				cur.moveToFirst();
				
				klasse  = cur.getString(cur.getColumnIndex(LSGSQliteOpenHelper.DB_CLASS));
				rawfach = cur.getString(cur.getColumnIndex(LSGSQliteOpenHelper.DB_RAW_FACH));
				fach    = cur.getString(cur.getColumnIndex(LSGSQliteOpenHelper.DB_FACH));
				cur.close();
				
				int i = 0;
				while(i < Functions.exclude.length) {
					if(klasse.contains(Functions.exclude[i]))
						conmenu = 1;
					i++;
					}
				if(klasse.equals("null"))
					conmenu = 1;
		} else if (table.equals(LSGSQliteOpenHelper.DB_SUBJECT_TABLE)) {
			Cursor cur = myDB.query(LSGSQliteOpenHelper.DB_SUBJECT_TABLE,
					new String[] { LSGSQliteOpenHelper.DB_FACH,
							LSGSQliteOpenHelper.DB_RAW_FACH },
					LSGSQliteOpenHelper.DB_ROWID + " = ?", new String[] { Long
							.valueOf(info.id).toString() }, null, null, null);
				cur.moveToFirst();
				
				rawfach = cur.getString(cur.getColumnIndex(LSGSQliteOpenHelper.DB_RAW_FACH));
				fach    = cur.getString(cur.getColumnIndex(LSGSQliteOpenHelper.DB_FACH));
				cur.close();
			conmenu = 1;
		}

		Cursor exclude = myDB.query(LSGSQliteOpenHelper.DB_EXCLUDE_TABLE,
				new String[] { LSGSQliteOpenHelper.DB_FACH },
				LSGSQliteOpenHelper.DB_RAW_FACH + " LIKE ?",
				new String[] { rawfach }, null, null, null);
		if (exclude.getCount() > 0)
			conmenu = 2;

		Cursor include = myDB.query(LSGSQliteOpenHelper.INCLUDE_TABLE,
				new String[] { LSGSQliteOpenHelper.DB_FACH },
				LSGSQliteOpenHelper.DB_RAW_FACH + " LIKE ?",
				new String[] { rawfach }, null, null, null);
		if (include.getCount() > 0)
			conmenu = 3;

		if(conmenu == 1) {
			menu.setHeaderTitle(fach + " (" + rawfach + ")");
			menu.add(Menu.NONE, 0, 0, context.getString(R.string.excludesubject));
			menu.add(Menu.NONE, 1, 0, context.getString(R.string.includesubject));
		}
		else if(conmenu == 2) {
			menu.setHeaderTitle(fach + " (" + rawfach + ")");
			menu.add(Menu.NONE, 2, 0, context.getString(R.string.no_excludesubject));
		}
		else if(conmenu == 3) {
			menu.setHeaderTitle(fach + " (" + rawfach + ")");
			menu.add(Menu.NONE, 3, 0, context.getString(R.string.no_includesubject));
		}
	}
	/**
	 * Callback for the above method
	 * @param item the clicked MenuItem
	 * @param context the app context
	 * @param list the calling Activity, has to extend SQLlist
	 * @param table the table
	 * @return always true
	 */
	public static boolean contextMenuSelect(MenuItem item, final Context context,
			final SQLlist list, String table) {
		final SQLiteDatabase myDB = LSGApplication.getSqliteDatabase();
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
				.getMenuInfo();

		Cursor cur;
		if (table.equals(LSGSQliteOpenHelper.DB_VPLAN_TABLE)) {
			cur = myDB.query(LSGSQliteOpenHelper.DB_VPLAN_TABLE, new String[] {
					LSGSQliteOpenHelper.DB_FACH,
					LSGSQliteOpenHelper.DB_RAW_FACH },
					LSGSQliteOpenHelper.DB_ROWID + " = ?", new String[] { Long
							.valueOf(info.id).toString() }, null, null, null);
		} else if (table.equals(LSGSQliteOpenHelper.DB_TIME_TABLE)) {
			cur = myDB.query(LSGSQliteOpenHelper.DB_TIME_TABLE, new String[] {
					LSGSQliteOpenHelper.DB_FACH,
					LSGSQliteOpenHelper.DB_RAW_FACH },
					LSGSQliteOpenHelper.DB_ROWID + " = ?", new String[] { Long
							.valueOf(info.id).toString() }, null, null, null);
		} else { // table.equals(Functions.DB_SUBJECT_TABLE)
			cur = myDB.query(LSGSQliteOpenHelper.DB_SUBJECT_TABLE,
					new String[] { LSGSQliteOpenHelper.DB_FACH,
							LSGSQliteOpenHelper.DB_RAW_FACH },
					LSGSQliteOpenHelper.DB_ROWID + " = ?", new String[] { Long
							.valueOf(info.id).toString() }, null, null, null);
		}
		cur.moveToFirst();
		  
		  //final String klasse  = cur.getString(cur.getColumnIndex(Functions.DB_KLASSE));
		  final String rawfach = cur.getString(cur.getColumnIndex(LSGSQliteOpenHelper.DB_RAW_FACH));
		  final String fach    = cur.getString(cur.getColumnIndex(LSGSQliteOpenHelper.DB_FACH));
		  cur.close();
		  final String prompt;
		  final String listtable;
			
		  //final CharSequence title = ((TextView) info.targetView.findViewById(R.id.vertretung_title)).getText();
		  int menuItemIndex = item.getItemId();
		  if(menuItemIndex == 0) {
			  prompt = context.getString(R.string.really_exclude);
			  listtable  = LSGSQliteOpenHelper.DB_EXCLUDE_TABLE;
		  }
		  else if(menuItemIndex == 1) {
			  prompt = context.getString(R.string.really_include);
			  listtable  = LSGSQliteOpenHelper.INCLUDE_TABLE;
		  }
		  else {
			  //this code should never be executed, its just for the compiler not to complain :-)
			  prompt = "";
			  listtable = "";
		  }
		  if(menuItemIndex == 0 || menuItemIndex == 1) {
			  DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
				    @Override
				    public void onClick(DialogInterface dialog, int which) {
				        switch (which){
				        case DialogInterface.BUTTON_POSITIVE:
				        	ContentValues vals = new ContentValues();
				        	vals.put(LSGSQliteOpenHelper.DB_FACH, fach);
				        	vals.put(LSGSQliteOpenHelper.DB_RAW_FACH, rawfach);
				        	vals.put(LSGSQliteOpenHelper.DB_NEEDS_SYNC, "true");
				        	vals.put(LSGSQliteOpenHelper.DB_TYPE, "oldstyle");
				        	myDB.insert(listtable, null, vals);
//				        	VPlan.blacklistVPlan(context);
//				        	TimeTable.blacklistTimeTable(context);
				        	list.updateList();
				            break;
				        }
				    }
				};
			  AlertDialog.Builder builder = new AlertDialog.Builder(context);
				builder.setMessage(prompt)
				.setPositiveButton(context.getString(R.string.yes), dialogClickListener)
					.setNegativeButton(context.getString(R.string.no),
							dialogClickListener).show();
		}
		if (menuItemIndex == 2) {
			myDB.delete(LSGSQliteOpenHelper.DB_EXCLUDE_TABLE,
					LSGSQliteOpenHelper.DB_RAW_FACH + " = ?",
					new String[] { rawfach });
			list.updateList();
		}
		if (menuItemIndex == 3) {
			myDB.delete(LSGSQliteOpenHelper.INCLUDE_TABLE,
					LSGSQliteOpenHelper.DB_RAW_FACH + " = ?",
					new String[] { rawfach });
			list.updateList();
		}
		  return true;
	}
	public static void sendClientId(String id, Context context) {
		sendClientId(id, context, true);
	}
	public static void sendClientId(String id, Context context, boolean enabled) {
		String add = "";
		try {
			add = "&" + URLEncoder.encode("client_id", "UTF-8") + "="
					+ URLEncoder.encode(id, "UTF-8") + "&"
					+ URLEncoder.encode("type", "UTF-8") + "="
					+ URLEncoder.encode("gcm", "UTF-8") + "&" + URLEncoder.encode("enabled", "UTF-8") + "="
					+ URLEncoder.encode(Boolean.valueOf(enabled).toString(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			Log.d("encoding", e.getMessage());
		}
		String get = Functions.getData(Functions.REGISTRATION_URL, context,
				true, add);
		if(!get.equals("networkerror")) {
			Log.d("c2dm", get);
			}
		else {
			Log.d("sendId", "networkerror");
		}
	}
	public static void registerGCM(Context context) {
		try {
		GCMRegistrar.checkDevice(context);
		GCMRegistrar.checkManifest(context);
		final String regId = GCMRegistrar.getRegistrationId(context);
		Log.v("regId", regId);
		if (regId.equals("")) {
		  GCMRegistrar.register(context, Functions.GCM_ID);
		} else {
		  Log.v(TAG, "Already registered");
		}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void unregisterGCM(Context context) {
		try {
		GCMRegistrar.checkDevice(context);
		GCMRegistrar.checkManifest(context);
		  GCMRegistrar.unregister(context);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * a little helper function to convert dp units to pixel on the specific device
	 * @param dp the value in dp
	 * @param ctx the app context
	 * @return an intent containing the size in pixels
	 */
	public static int dpToPx(int dp, Context ctx) {
	    Resources r = ctx.getResources();
	    return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
	}
	public static float percentWidth(int px, Activity ctx) {
		Display display = ctx.getWindowManager().getDefaultDisplay();
		return (float) px / display.getWidth();
	}
	/**
	 * lock the rotation for asynctask
	 * @param ctx the app context
	 */
	@SuppressWarnings("deprecation")
	public static void lockRotation(Activity ctx) {
		WindowManager wm =  (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
	    Display dp = wm.getDefaultDisplay();
		switch(dp.getOrientation()) {
		case Surface.ROTATION_0:
			ctx.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			break;
		case Surface.ROTATION_90:
			ctx.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			break;
		case Surface.ROTATION_180:
			ctx.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
			break;
		case Surface.ROTATION_270:
			ctx.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
			break;
		}
	}
	/**
	 * unlock the rotation change
	 * @param ctx the app context
	 */
	public static void unlockRotation(Activity ctx) {
		ctx.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
	}
	public static WebView webv = null;
	public static Bundle webvSave;
	@SuppressLint("SetJavaScriptEnabled")
	public static void init(Activity act) {
		webv = new WebView(act);
		webv.getSettings().setJavaScriptEnabled(true);
		webv.setWebChromeClient(new WebChromeClient() {
			public void onProgressChanged(WebView view, int progress) {
				//actactivity.setProgress(progress*1000);
				if(progress == 100)
					webv.saveState(Functions.webvSave);
				}
			});
		webv.setWebViewClient(new WebViewClient() {
			public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
				//Toast.makeText(act, getString(R.string.oops) + " " + description, Toast.LENGTH_SHORT).show();
				}
			});
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(act);
		String postData = "log=" + prefs.getString("username", "")
				+ "&pwd=" + prefs.getString("password", "") + "&redirect_to=http://www.lsg.musin.de/smv/aktuelles/";
		if(Functions.getSDK() >= 5) {
			AdvancedWrapper advWrapper = new AdvancedWrapper();
			advWrapper.postUrl(webv, "http://www.lsg.musin.de/smv/login/?action=login", EncodingUtils.getBytes(postData, "BASE64"));
			advWrapper = null;
		} else
			webv.loadUrl("http://www.lsg.musin.de/smv/login/?action=login");
		if (prefs.getBoolean("useac2dm", false))
			Functions.registerGCM(act);
	}
	public static void checkMessage(Context context, String[] action) {
		if(action.length == 0)
			return;
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		String[] addActions = new String[action.length - 1];
		if (action.length > 1) {
			int i = 0;
			for (String item : action) {
				if(i == 0){
					i++;
					continue;
				}
				addActions[i-1] = item;
				i++;
			}
		}
		for (int ii = 0; ii < action.length; ii++) {
			if (!prefs.getBoolean(action[ii] + SHOWN, false)) {
				Intent intent = new Intent(context, OverlayHelp.class);
				intent.setAction(action[0]);
				if (addActions != null)
					intent.putExtra("other", addActions);
				context.startActivity(intent);
				return;
			}
		}
	}
}