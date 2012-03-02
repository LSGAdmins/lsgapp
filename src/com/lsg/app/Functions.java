package com.lsg.app;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

public class Functions {
	public static final String   BLACKWHITELIST = "blackwhitelist";
	public static final String   BLACKLIST      = "blacklist";
	public static final String   WHITELIST      = "whitelist";
	
	public static final String   UPDATE_URL = "http://linux.lsg.musin.de/cp/downloads/lsgapp.apk";
	public static final String   VP_URL     = "http://linux.lsg.musin.de/cp/vp_app.php";
	public static final String   EVENT_URL  = "http://linux.lsg.musin.de/cp/termine_app.php";
	public static final String   CLASS_URL  = "http://linux.lsg.musin.de/cp/getClass.php";
	
	public static final String   class_key  = "class";
	public static final String[] exclude    = {"Q11", "Q12"};
	
	public static final String helpabout = "helpabout";
	public static final String help      = "help";
	public static final String about     = "about";
	
	public static final String DB_ROWID           = "_id";
	public static final String DB_NAME            = "lsgapp";
	//VPlan
	public static final String DB_TABLE           = "vertretungen";
	public static final String DB_KLASSENSTUFE    = "klassenstufe";
	public static final String DB_KLASSE          = "klasse";
	public static final String DB_STUNDE          = "stunde";
	public static final String DB_VERTRETER       = "vertreter";
	public static final String DB_LEHRER          = "lehrer";
	public static final String DB_RAUM            = "raum";
	public static final String DB_ART             = "art";
	public static final String DB_VERTRETUNGSTEXT = "vertretungstext";
	public static final String DB_FACH            = "fach";
	public static final String DB_DATE            = "date";
	//exclude & include
	public static final String EXCLUDE_TABLE      = "exclude";
	public static final String INCLUDE_TABLE      = "include";
	public static final String DB_NEEDS_SYNC      = "needssync";
	
	public static void setTheme(boolean dialog, boolean homeasup, Activity act) {
		int theme = android.R.style.Theme_Light;
		if(Build.VERSION.SDK_INT >= 11) {
			theme = android.R.style.Theme_Holo_Light;
			if(dialog)
				theme = android.R.style.Theme_Holo_Light_Dialog;
		} else {
			if(dialog) {
				theme = android.R.style.Theme_Dialog;
			}
		}
		act.setTheme(theme);
		if(homeasup && Build.VERSION.SDK_INT >= 11) {
			try {
				Advanced.homeasup(act);
			} catch (Exception e) {
			}
		}
	}
	public static void styleListView(ListView lv, Context context) {
		if(Build.VERSION.SDK_INT >= 11) {
			ColorDrawable sage = new ColorDrawable(context.getResources().getColor(R.color.seperatorgrey));
			lv.setDivider(sage);
			lv.setDividerHeight(2);
		}
	}
	
	public static String getFach(String title) {
		String[] split = title.split("\\(");
		return split[1].split("\\)")[0];
	}
	
	public static String getData(String urlString, Context context, boolean login) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		try {
			String data = URLEncoder.encode("username", "UTF-8") + "=" + URLEncoder.encode(prefs.getString("username", ""), "UTF-8");
        	data       += "&" + URLEncoder.encode("password", "UTF-8") + "=" + URLEncoder.encode(prefs.getString("password", ""), "UTF-8");
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
        	BufferedReader reader = new BufferedReader(new InputStreamReader(response));
        	String line;
        	String get = "";
        	while ((line = reader.readLine()) != null) {
        		get += line;
        		}
        	return get;
		} catch(Exception e) { Log.d("except in fetching data: ", e.getMessage()); return "networkerror";}
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
						Intent intent = new Intent(context, Settings.class);
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
	
	public static boolean refreshVPlan(final Context context, Handler h) {
		Functions.testDB(context);
		String get = Functions.getData(Functions.VP_URL, context, true);
		if(!get.equals("networkerror") && !get.equals("loginerror")) {
			try {
        		JSONArray jArray = new JSONArray(get);
        		Toast.makeText(context, new Integer(jArray.length()).toString(), Toast.LENGTH_LONG).show();
        		int i = 0;
    			SQLiteDatabase myDB = context.openOrCreateDatabase(Functions.DB_NAME, Context.MODE_PRIVATE, null);
    			myDB.delete(Functions.DB_TABLE, null, null); //clear vertretungen
        		while(i < jArray.length()) {
        			JSONObject jObject = jArray.getJSONObject(i);
        			ContentValues values = new ContentValues();
        			values.put(Functions.DB_KLASSENSTUFE, jObject.getString("klassenstufe"));
        			values.put(Functions.DB_KLASSE, jObject.getString("klasse"));
        			values.put(Functions.DB_STUNDE, jObject.getString("stunde"));
        			values.put(Functions.DB_VERTRETER, jObject.getString("vertreter"));
        			values.put(Functions.DB_LEHRER, jObject.getString("lehrer"));
        			values.put(Functions.DB_RAUM, jObject.getString("raum"));
        			values.put(Functions.DB_ART, jObject.getString("art"));
        			values.put(Functions.DB_VERTRETUNGSTEXT, jObject.getString("vertretungstext"));
        			values.put(Functions.DB_FACH, jObject.getString("fach"));
        			values.put(Functions.DB_DATE, jObject.getString("date"));
            		myDB.insert(Functions.DB_TABLE, null, values);
        			i++;
        			}
        		myDB.close();
        		} catch(JSONException e) {
        			Log.d("json", e.getMessage());
        			h.post(getErrorRunnable("jsonerror", context));
        			return false;
        		}
			}
		else {
			h.post(getErrorRunnable(get, context));
			return false;
		}
		return true;
		}
	public static void getClass(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		try {
			Functions.testDB(context);
			String get = Functions.getData(Functions.CLASS_URL, context, true);
        	SharedPreferences.Editor editor = prefs.edit();
        	editor.putString("class", get);
        	editor.commit();
        }
        catch(Exception e) {
	    	Log.d("except", e.getMessage());
        }
	}
	public static void testDB(Context context) {
    	try {
    		SQLiteDatabase myDB;
    		myDB = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
    		//vertretungen
    		myDB.execSQL("CREATE TABLE IF NOT EXISTS " + Functions.DB_TABLE
    				+ " (" + Functions.DB_ROWID       + " integer primary key autoincrement,"
    	    		+ Functions.DB_KLASSENSTUFE       + " integer,"
    	    	    + Functions.DB_KLASSE	          + " text,"
    	    	    + Functions.DB_STUNDE             + " integer,"
    	    	    + Functions.DB_VERTRETER          + " text,"
    	     	    + Functions.DB_LEHRER             + " text,"
    	    	    + Functions.DB_RAUM               + " text,"
    	    	    + Functions.DB_ART                + " text,"
    	    	    + Functions.DB_VERTRETUNGSTEXT    + " text,"
    	    	    + Functions.DB_FACH               + " text,"
    	    	    + Functions.DB_DATE               + " text"
    				+");");
    		myDB.execSQL("CREATE TABLE IF NOT EXISTS " + Functions.EXCLUDE_TABLE + " ("
    				+ Functions.DB_ROWID + " integer primary key autoincrement,"
    				+ Functions.DB_FACH + " text,"
    				+ Functions.DB_NEEDS_SYNC + " text"
    				+ ");");
    		myDB.execSQL("CREATE TABLE IF NOT EXISTS " + Functions.INCLUDE_TABLE + " ("
    				+ Functions.DB_ROWID + " integer primary key autoincrement,"
    				+ Functions.DB_FACH + " text,"
    				+ Functions.DB_NEEDS_SYNC + " text"
    				+ ");");
    		//events
    		myDB.execSQL("CREATE TABLE IF NOT EXISTS " + Functions.DB_EVENTS_TABLE
    				+ " (" + Functions.DB_ROWID       + " integer primary key autoincrement,"
    	    	    + Functions.DB_DATES              + " text,"
    	     	    + Functions.DB_ENDDATES           + " text,"
    	    	    + Functions.DB_TIMES              + " text,"
    	    	    + Functions.DB_ENDTIMES           + " text,"
    	    	    + Functions.DB_TITLE              + " text,"
    	    	    + Functions.DB_VENUE              + " text"
    				+");");
    		//upgrades for table
    		/*if(myDB.getVersion() == 0) {
    			myDB.setVersion(1);
    		}*/
    		myDB.close();
        } catch (Exception e) { Log.d("db", e.getMessage());}
	}
	
	//Termine
	public static final String DB_EVENTS_TABLE    = "events";
	public static final String DB_DATES    		  = "dates";
	public static final String DB_ENDDATES        = "enddates";
	public static final String DB_TIMES           = "times";
	public static final String DB_ENDTIMES        = "endtimes";
	public static final String DB_TITLE           = "title";
	public static final String DB_VENUE           = "venue";
	public static boolean refreshEvents(Context context, Handler h) {
		Functions.testDB(context);
		String get = Functions.getData(Functions.EVENT_URL, context, false);
		if(!get.equals("networkerror")) {
			try {
        		JSONArray jArray = new JSONArray(get);
        		Toast.makeText(context, new Integer(jArray.length()).toString(), Toast.LENGTH_LONG).show();
        		int i = 0;
    			SQLiteDatabase myDB = context.openOrCreateDatabase(Functions.DB_NAME, Context.MODE_PRIVATE, null);
    			myDB.delete(Functions.DB_EVENTS_TABLE, null, null); //clear termine
        		while(i < jArray.length()) {
        			JSONObject jObject = jArray.getJSONObject(i);
        			ContentValues values = new ContentValues();
        			values.put(Functions.DB_DATES, jObject.getString("dates"));
        			values.put(Functions.DB_ENDDATES, jObject.getString("enddates"));
        			values.put(Functions.DB_TIMES, jObject.getString("times"));
        			values.put(Functions.DB_ENDTIMES, jObject.getString("endtimes"));
        			values.put(Functions.DB_TITLE, jObject.getString("title"));
        			values.put(Functions.DB_VENUE, jObject.getString("venue"));
            		myDB.insert(Functions.DB_EVENTS_TABLE, null, values);
        			i++;
        			}
        		myDB.close();
        		} catch(JSONException e) {
        			Log.d("json", e.getMessage());
        			h.post(getErrorRunnable("jsonerror", context));
        			return false;
        		}	
        }
		else {
			h.post(getErrorRunnable(get, context));
			return false;
		}
		return true;
	}
}