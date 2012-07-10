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
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
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
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class Functions {
	public static final String   TAG            = "LSGäpp";
	
	public static final String   BLACKWHITELIST = "blackwhitelist";
	public static final String   BLACKLIST      = "blacklist";
	public static final String   WHITELIST      = "whitelist";
	
	//c2dm
	public static final String   EMAIL       = "noreply.lsg@googlemail.com";
	
	public static final String   UPDATE_URL       = "http://linux.lsg.musin.de/cp/downloads/lsgapp.apk";
	public static final String   UPDATE_CHECK_URL = "http://linux.lsg.musin.de/cp/checkUpdate.php?version=";
	public static final String   VP_URL           = "http://linux.lsg.musin.de/cp/vp_app.php";
	public static final String   EVENT_URL        = "http://linux.lsg.musin.de/cp/termine_app.php";
	public static final String   CLASS_URL        = "http://linux.lsg.musin.de/cp/getClass.php?all=true";
	public static final String   SUBJECT_URL      = "http://linux.lsg.musin.de/cp/fach_kuerzel.php";
	public static final String   REGISTRATION_URL = "http://linux.lsg.musin.de/cp/register_client.php";
	public static final String   TIMETABLE_URL    = "http://linux.lsg.musin.de/cp/timetable.php";
	public static final String   LOGIN_TEST_URL   = "http://linux.lsg.musin.de/cp/setup.php?act=checklogin";
	public static final String   PERSON_DATA_URL  = "http://linux.lsg.musin.de/cp/setup.php?act=getdata";
	public static final String   API_VERSION      = "3";
	
	public static final String   class_key  = "class";
	public static final String[] exclude    = {"Q11", "Q12"};
	
	public static final String helpabout = "helpabout";
	public static final String help      = "help";
	public static final String about     = "about";
	
	public static final String DB_ROWID           = "_id";
	public static final String DB_NAME            = "lsgapp";
	//VPlan
	public static final String DB_VPLAN_TABLE     = "vertretungen";
	public static final String DB_VPLAN_TEACHER   = "lehrervertretungen";
	public static final String DB_KLASSENSTUFE    = "klassenstufe";
	public static final String DB_KLASSE          = "klasse";
	public static final String DB_STUNDE          = "stunde";
	public static final String DB_VERTRETER       = "vertreter";
	public static final String DB_RAW_VERTRETER   = "rawvertreter";
	public static final String DB_LEHRER          = "lehrer";
	public static final String DB_RAW_LEHRER      = "rawlehrer";
	public static final String DB_RAUM            = "raum";
	public static final String DB_ART             = "art";
	public static final String DB_VERTRETUNGSTEXT = "vertretungstext";
	public static final String DB_FACH            = "fach";
	public static final String DB_RAW_FACH        = "rawfach";
	public static final String DB_DATE            = "date";
	public static final String DB_LENGTH          = "length";
	//Termine
	public static final String DB_EVENTS_TABLE    = "events";
	public static final String DB_DATES    		  = "dates";
	public static final String DB_ENDDATES        = "enddates";
	public static final String DB_TIMES           = "times";
	public static final String DB_ENDTIMES        = "endtimes";
	public static final String DB_TITLE           = "title";
	public static final String DB_VENUE           = "venue";
	//exclude & include
	public static final String EXCLUDE_TABLE      = "exclude";
	public static final String INCLUDE_TABLE      = "include";
	public static final String DB_NEEDS_SYNC      = "needssync";
	//subjects
	public static final String DB_SUBJECT_TABLE   = "subjects";
	//timetable
	public static final String DB_TIME_TABLE      = "timetable";
	public static final String DB_DAY             = "day";
	public static final String DB_HOUR            = "hour";
	//classes
	public static final String DB_CLASS_TABLE     = "classes";
	public static final String DB_CLASS           = "class";
	
	public static final String RELIGION           = "religion";
	public static final String KATHOLISCH         = "K";
	public static final String EVANGELISCH        = "Ev";
	public static final String ETHIK              = "Eth";
	
	public static final String GENDER             = "gender";
	
	public static final String FULL_CLASS         = "full_class";
	//slidemenu
	public static final int TYPE_PAGE             = 0;
	public static final int TYPE_INFO             = 1;
	
	public static void setTheme(boolean dialog, boolean homeasup, Activity act) {
		int theme = android.R.style.Theme_Light;
		if(Functions.getSDK() >= 11) {
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(act);
			if(Functions.getSDK() >= 14 && prefs.getBoolean("dark_actionbar", false))
				theme = android.R.style.Theme_Holo_Light_DarkActionBar;
			else
				theme = android.R.style.Theme_Holo_Light;
			if(dialog)
				theme = android.R.style.Theme_Holo_Light_Dialog;
		} else {
			if(dialog) {
				theme = android.R.style.Theme_Dialog;
			}
		}
		act.setTheme(theme);
		
		if(homeasup && Functions.getSDK() >= 11) {
			homeUp(act);
		}
	}
	public static void homeUp(Activity act) {
		try {
			AdvancedWrapper advWrapper = new AdvancedWrapper();
			advWrapper.homeasup(act);
		} catch (Exception e) {
		}
	}
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
	
	public static String getData(String urlString, Context context, boolean login, String add) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		try {
			String data = URLEncoder.encode("username", "UTF-8") + "=" + URLEncoder.encode(prefs.getString("username", ""), "UTF-8")
					+ "&" + URLEncoder.encode("password", "UTF-8") + "=" + URLEncoder.encode(prefs.getString("password", ""), "UTF-8")
					+ "&" + URLEncoder.encode("api", "UTF-8") + "=" + URLEncoder.encode(Functions.API_VERSION, "UTF-8") + add;
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
		} catch(Exception e) { Log.d("except in fetching data: ", e.getMessage() + " "); e.printStackTrace(); return "networkerror";}
	}
	public static boolean testLogin(Context context) {
		return (getData(Functions.LOGIN_TEST_URL, context, true, "").equals("true"));
	}
	public static Runnable getErrorRunnable(String error, final Context context) {
		Log.d("asdf", "errorrunnable");
		if(error.equals("jsonerror")) {
			Runnable r = new Runnable (){
				public void run() {
					Toast.makeText(context, context.getString(R.string.jsonerror), Toast.LENGTH_LONG).show();
					}
				};
				return r;
				}
		else if(error.equals("loginerror")) {
			Runnable r = new Runnable (){
					public void run() {
						Toast.makeText(context, context.getString(R.string.loginerror), Toast.LENGTH_LONG).show();
						Intent intent;
						if(Functions.getSDK() >= 11)
							intent = new Intent(context, SettingsAdvanced.class);
						else
							intent = new Intent(context, Settings.class);
						intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
						context.startActivity(intent);
					}
					};
					return r;
		}
		else {
			Runnable r = new Runnable (){
					public void run() {
						Toast.makeText(context, context.getString(R.string.networkerror), Toast.LENGTH_LONG).show();
					}
					};
					return r;
					}
	}
	public static void cleanDB(Context context) {
		Calendar now  = Calendar.getInstance();
		int year_now  = now.get(Calendar.YEAR);
		int month_now = now.get(Calendar.MONTH)+1;
		int day_now   = now.get(Calendar.DAY_OF_MONTH);
		SQLiteDatabase myDB;
		myDB = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
		Cursor result = myDB.query(Functions.DB_VPLAN_TABLE, new String[] {Functions.DB_DATE}, null, null, null, null, null);
		result.moveToFirst();
		int i = 0;
		while(i < result.getCount()) {
			String date = result.getString(result.getColumnIndex(Functions.DB_DATE));
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
				myDB.execSQL("DELETE FROM " + Functions.DB_VPLAN_TABLE + " WHERE " + Functions.DB_DATE + " = '" + date + "'");
				}
			i++;
		}
		result.close();
		myDB.close();
	}
	public static void testDB(Context context) {
		SQLiteDatabase myDB;
		myDB = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
    	try {
    		//vertretungen
    		myDB.execSQL("CREATE TABLE IF NOT EXISTS " + Functions.DB_VPLAN_TABLE
    				+ " (" + Functions.DB_ROWID       + " INTEGER primary key autoincrement,"
    	    		+ Functions.DB_KLASSENSTUFE       + " INTEGER,"
    	    	    + Functions.DB_KLASSE	          + " TEXT,"
    	    	    + Functions.DB_STUNDE             + " INTEGER,"
    	    	    + Functions.DB_VERTRETER          + " TEXT,"
    	     	    + Functions.DB_LEHRER             + " TEXT,"
    	    	    + Functions.DB_RAUM               + " TEXT,"
    	    	    + Functions.DB_ART                + " TEXT,"
    	    	    + Functions.DB_VERTRETUNGSTEXT    + " TEXT,"
    	    	    + Functions.DB_FACH               + " TEXT,"
    	    	    + Functions.DB_DATE               + " TEXT"
    				+");");
    		myDB.execSQL("CREATE TABLE IF NOT EXISTS " + Functions.DB_VPLAN_TEACHER
    				+ " (" + Functions.DB_ROWID       + " INTEGER primary key autoincrement,"
    	    		+ Functions.DB_KLASSENSTUFE       + " INTEGER,"
    	    	    + Functions.DB_KLASSE	          + " TEXT,"
    	    	    + Functions.DB_STUNDE             + " INTEGER,"
    	    	    + Functions.DB_VERTRETER          + " TEXT,"
    	     	    + Functions.DB_LEHRER             + " TEXT,"
    	    	    + Functions.DB_RAUM               + " TEXT,"
    	    	    + Functions.DB_ART                + " TEXT,"
    	    	    + Functions.DB_VERTRETUNGSTEXT    + " TEXT,"
    	    	    + Functions.DB_FACH               + " TEXT,"
    	    	    + Functions.DB_DATE               + " TEXT,"
    	    	    + Functions.DB_RAW_FACH           + " TEXT,"
    	    	    + Functions.DB_LENGTH             + " INTEGER"
    				+");");
    		//blacklist
    		myDB.execSQL("CREATE TABLE IF NOT EXISTS " + Functions.EXCLUDE_TABLE + " ("
    				+ Functions.DB_ROWID + " INTEGER primary key autoincrement,"
    				+ Functions.DB_FACH + " TEXT,"
    				+ Functions.DB_NEEDS_SYNC + " TEXT"
    				+ ");");
    		//whitelist
    		myDB.execSQL("CREATE TABLE IF NOT EXISTS " + Functions.INCLUDE_TABLE + " ("
    				+ Functions.DB_ROWID + " INTEGER primary key autoincrement,"
    				+ Functions.DB_FACH + " TEXT,"
    				+ Functions.DB_NEEDS_SYNC + " TEXT"
    				+ ");");
    		//subjects
    		myDB.execSQL("CREATE TABLE IF NOT EXISTS " + Functions.DB_SUBJECT_TABLE
    				+ " (" + Functions.DB_ROWID       + " INTEGER primary key autoincrement,"
    	    		+ Functions.DB_RAW_FACH + " TEXT,"
    	    	    + Functions.DB_FACH + " TEXT"
    				+");");
    		//events
    		myDB.execSQL("CREATE TABLE IF NOT EXISTS " + Functions.DB_EVENTS_TABLE
    				+ " (" + Functions.DB_ROWID       + " INTEGER primary key autoincrement,"
    	    	    + Functions.DB_DATES              + " TEXT,"
    	     	    + Functions.DB_ENDDATES           + " TEXT,"
    	    	    + Functions.DB_TIMES              + " TEXT,"
    	    	    + Functions.DB_ENDTIMES           + " TEXT,"
    	    	    + Functions.DB_TITLE              + " TEXT,"
    	    	    + Functions.DB_VENUE              + " TEXT"
    				+");");
    		myDB.execSQL("CREATE TABLE IF NOT EXISTS " + Functions.DB_TIME_TABLE
    				+ " (" + Functions.DB_ROWID        + " INTEGER primary key autoincrement,"
    	    	    + Functions.DB_LEHRER              + " TEXT,"
    	     	    + Functions.DB_FACH                + " TEXT,"
    	    	    + Functions.DB_RAUM                + " TEXT,"
    	    	    + Functions.DB_LENGTH              + " INTEGER,"
    	    	    + Functions.DB_DAY                 + " INTEGER,"
    	    	    + Functions.DB_HOUR                + " INTEGER"
    				+");");
    		myDB.execSQL("CREATE TABLE IF NOT EXISTS " + Functions.DB_CLASS_TABLE
    				+ " (" + Functions.DB_ROWID        + " INTEGER primary key autoincrement,"
    	    	    + Functions.DB_CLASS              + " TEXT"
    				+");");
    		//upgrades for table
    		if(myDB.getVersion() == 0) {
    			Log.d(Functions.DB_VPLAN_TABLE, "adding column " + Functions.DB_RAW_FACH);
    			myDB.execSQL("ALTER TABLE " + Functions.DB_VPLAN_TABLE + " ADD COLUMN " + Functions.DB_RAW_FACH + " TEXT");
    			myDB.setVersion(1);
    		}
    		if(myDB.getVersion() == 1) {
    			Log.d(Functions.EXCLUDE_TABLE, "adding column " + Functions.DB_RAW_FACH);
    			myDB.execSQL("ALTER TABLE " + Functions.EXCLUDE_TABLE + " ADD COLUMN " + Functions.DB_RAW_FACH + " TEXT");
    			Log.d(Functions.INCLUDE_TABLE, "adding column " + Functions.DB_RAW_FACH);
    			myDB.execSQL("ALTER TABLE " + Functions.INCLUDE_TABLE + " ADD COLUMN " + Functions.DB_RAW_FACH + " TEXT");
    			myDB.setVersion(2);
    		}
    		if(myDB.getVersion() == 2) {
    			Log.d(Functions.DB_VPLAN_TABLE, "adding column " + Functions.DB_LENGTH);
    			myDB.execSQL("ALTER TABLE " + Functions.DB_VPLAN_TABLE + " ADD COLUMN " + Functions.DB_LENGTH + " INTEGER");
    			myDB.setVersion(3);
    		}
    		if(myDB.getVersion() == 3) {
    			Log.d(Functions.DB_TIME_TABLE, "adding column " + Functions.DB_RAW_FACH);
    			myDB.execSQL("ALTER TABLE " + Functions.DB_TIME_TABLE + " ADD COLUMN " + Functions.DB_RAW_FACH + " TEXT");
    			myDB.setVersion(4);
    		}
    		if(myDB.getVersion() == 4) {
    			Log.d(Functions.DB_VPLAN_TABLE, "adding column " + Functions.DB_RAW_LEHRER);
    			myDB.execSQL("ALTER TABLE " + Functions.DB_VPLAN_TABLE + " ADD COLUMN " + Functions.DB_RAW_VERTRETER + " TEXT");
    			Log.d(Functions.DB_VPLAN_TABLE, "adding column " + Functions.DB_RAW_LEHRER);
    			myDB.execSQL("ALTER TABLE " + Functions.DB_VPLAN_TABLE + " ADD COLUMN " + Functions.DB_RAW_LEHRER + " TEXT");
    			myDB.setVersion(5);
    		}
    		if(myDB.getVersion() == 5) {
    			Log.d(Functions.DB_VPLAN_TEACHER, "adding column " + Functions.DB_RAW_LEHRER);
    			myDB.execSQL("ALTER TABLE " + Functions.DB_VPLAN_TEACHER + " ADD COLUMN " + Functions.DB_RAW_VERTRETER + " TEXT");
    			Log.d(Functions.DB_VPLAN_TEACHER, "adding column " + Functions.DB_RAW_LEHRER);
    			myDB.execSQL("ALTER TABLE " + Functions.DB_VPLAN_TEACHER + " ADD COLUMN " + Functions.DB_RAW_LEHRER + " TEXT");
    			myDB.setVersion(6);
    		}
    		myDB.close();
        } catch (Exception e) {
        	myDB.close();
        	Log.d("db", e.getMessage());
        	}
    	Functions.cleanDB(context);
	}
	
	//handlers for adding / removing items to / from blacklist
	public static void createContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo, Context context, String table) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
		String klasse  = "";
		String rawfach = "";
		String fach    = "";
		int conmenu = 0;
		SQLiteDatabase myDB = context.openOrCreateDatabase(Functions.DB_NAME, Context.MODE_PRIVATE, null);
		if(table.equals(Functions.DB_VPLAN_TABLE)) {
			Cursor cur = myDB.query(Functions.DB_VPLAN_TABLE, new String[] {Functions.DB_KLASSE, Functions.DB_FACH, Functions.DB_RAW_FACH}, Functions.DB_ROWID + " = ?",
					new String[] {new Long(info.id).toString()}, null, null, null);
			cur.moveToFirst();
			
			klasse  = cur.getString(cur.getColumnIndex(Functions.DB_KLASSE));
			rawfach = cur.getString(cur.getColumnIndex(Functions.DB_RAW_FACH));
			fach    = cur.getString(cur.getColumnIndex(Functions.DB_FACH));
			cur.close();
			
			int i = 0;
			while(i < Functions.exclude.length) {
				if(klasse.contains(Functions.exclude[i]))
					conmenu = 1;
				i++;
				}
			if(klasse.equals("null"))
				conmenu = 1;
			} else if(table.equals(Functions.DB_SUBJECT_TABLE)) {
				Cursor cur = myDB.query(Functions.DB_SUBJECT_TABLE, new String[] {Functions.DB_FACH, Functions.DB_RAW_FACH}, Functions.DB_ROWID + " = ?",
						new String[] {new Long(info.id).toString()}, null, null, null);
				cur.moveToFirst();
				
				rawfach = cur.getString(cur.getColumnIndex(Functions.DB_RAW_FACH));
				fach    = cur.getString(cur.getColumnIndex(Functions.DB_FACH));
				cur.close();
				conmenu = 1;
				}
		
		Cursor exclude = myDB.query(Functions.EXCLUDE_TABLE, new String[] {Functions.DB_FACH}, Functions.DB_RAW_FACH + " LIKE ?",
				new String[] {rawfach}, null, null, null);
		if(exclude.getCount() > 0)
			conmenu = 2;

		Cursor include = myDB.query(Functions.INCLUDE_TABLE, new String[] {Functions.DB_FACH}, Functions.DB_RAW_FACH + " LIKE ?",
				new String[] {rawfach}, null, null, null);
		if(include.getCount() > 0)
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
	public static boolean contextMenuSelect(MenuItem item, Context context, final SQLlist list, String table) {
		final SQLiteDatabase myDB = context.openOrCreateDatabase(Functions.DB_NAME, Context.MODE_PRIVATE, null);
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
		  
		  Cursor cur;
		  if(table.equals(Functions.DB_VPLAN_TABLE)) {
			  cur = myDB.query(Functions.DB_VPLAN_TABLE, new String[] {Functions.DB_FACH, Functions.DB_RAW_FACH}, Functions.DB_ROWID + " = ?",
					  new String[] {new Long(info.id).toString()}, null, null, null);
		  } else { //table.equals(Functions.DB_SUBJECT_TABLE)
			  cur = myDB.query(Functions.DB_SUBJECT_TABLE, new String[] {Functions.DB_FACH, Functions.DB_RAW_FACH}, Functions.DB_ROWID + " = ?",
					  new String[] {new Long(info.id).toString()}, null, null, null);
		  }
		  cur.moveToFirst();
		  
		  //final String klasse  = cur.getString(cur.getColumnIndex(Functions.DB_KLASSE));
		  final String rawfach = cur.getString(cur.getColumnIndex(Functions.DB_RAW_FACH));
		  final String fach    = cur.getString(cur.getColumnIndex(Functions.DB_FACH));
		  cur.close();
		  final String prompt;
		  final String listtable;
			
		  //final CharSequence title = ((TextView) info.targetView.findViewById(R.id.vertretung_title)).getText();
		  int menuItemIndex = item.getItemId();
		  if(menuItemIndex == 0) {
			  prompt = context.getString(R.string.really_exclude);
			  listtable  = Functions.EXCLUDE_TABLE;
		  }
		  else if(menuItemIndex == 1) {
			  prompt = context.getString(R.string.really_include);
			  listtable  = Functions.INCLUDE_TABLE;
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
				        	vals.put(Functions.DB_FACH, fach);
				        	vals.put(Functions.DB_RAW_FACH, rawfach);
				        	vals.put(Functions.DB_NEEDS_SYNC, "true");
				        	myDB.insert(listtable, null, vals);
				        	list.updateList();
				            break;
				        }
				    }
				};
			  AlertDialog.Builder builder = new AlertDialog.Builder(context);
				builder.setMessage(prompt)
				.setPositiveButton(context.getString(R.string.yes), dialogClickListener)
				.setNegativeButton(context.getString(R.string.no), dialogClickListener).show();
		  }
		  if(menuItemIndex == 2) {
			  myDB.delete(Functions.EXCLUDE_TABLE, Functions.DB_RAW_FACH + " = ?", new String[] {rawfach});
			  list.updateList();
		  }
		  if(menuItemIndex == 3) {
			  myDB.delete(Functions.INCLUDE_TABLE, Functions.DB_RAW_FACH + " = ?", new String[] {rawfach});
			  list.updateList();
		  }
		  return true;
	}
	public static void sendClientId(String id, Context context) {
		String add = "";
		try {
			add = "&" + URLEncoder.encode("client_id", "UTF-8") + "=" + URLEncoder.encode(id, "UTF-8");
		} catch(UnsupportedEncodingException e) { Log.d("encoding", e.getMessage()); }
		String get = Functions.getData(Functions.REGISTRATION_URL, context, true, add);
		if(!get.equals("networkerror")) {
			Log.d("c2dm", get);
			}
		else {
			Log.d("sendId", "networkerror");
		}
	}
	public static void registerAC2DM(Context context) {
		Intent registrationIntent = new Intent("com.google.android.c2dm.intent.REGISTER");
		registrationIntent.putExtra("app", PendingIntent.getBroadcast(context, 0, new Intent(), 0));
		registrationIntent.putExtra("sender", Functions.EMAIL);
		context.startService(registrationIntent);
	}
	public static void unregisterAC2DM(Context context) {
		Intent unregIntent = new Intent("com.google.android.c2dm.intent.UNREGISTER");
		unregIntent.putExtra("app", PendingIntent.getBroadcast(context, 0, new Intent(), 0));
		context.startService(unregIntent);
	}
	public static int dpToPx(int dp, Context ctx) {
	    Resources r = ctx.getResources();
	    return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
	}
	//source: http://stackoverflow.com/questions/5418510/disable-the-touch-events-for-all-the-views
	public static void enableDisableViewGroup(ViewGroup viewGroup, boolean enabled) {
		int childCount = viewGroup.getChildCount();
		for (int i = 0; i < childCount; i++) {
			View view = viewGroup.getChildAt(i);
			if(view.isFocusable())
				view.setEnabled(enabled);
			if (view instanceof ViewGroup) {
				enableDisableViewGroup((ViewGroup) view, enabled);
				} else if (view instanceof ListView) {
					if(view.isFocusable())
						view.setEnabled(enabled);
					ListView listView = (ListView) view;
					int listChildCount = listView.getChildCount();
					for (int j = 0; j < listChildCount; j++) {
						if(view.isFocusable())
							listView.getChildAt(j).setEnabled(false);
						}
					}
			}
		}
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
	public static void unlockRotation(Activity ctx) {
		ctx.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
	}
	}