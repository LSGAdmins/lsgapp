package com.lsg.app;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class Functions {
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
	
	public static void setTheme(boolean dialog, boolean homeasup, Activity act) {
		int theme = android.R.style.Theme_Black;
		if(Build.VERSION.SDK_INT >= 11) {
			theme = 0x0103006b;  //-> android.R.Theme_Holo, needed, because build target is only 2.3.1
			if(dialog)
				theme = 0x0103006f;  //-> android.R.Theme_Holo_Dialog, needed, because build target is only 2.3.1
		} else {
			if(dialog) {
				theme = android.R.style.Theme_Dialog;
			}
		}
		act.setTheme(theme);
		if(homeasup && Build.VERSION.SDK_INT >= 11)
			Advanced.homeasup(act);
	}
	
	public static void refreshVPlan(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		try {
			Functions.testDB(context);
			String data = URLEncoder.encode("username", "UTF-8") + "=" + URLEncoder.encode(prefs.getString("username", ""), "UTF-8");
        	data       += "&" + URLEncoder.encode("password", "UTF-8") + "=" + URLEncoder.encode(prefs.getString("password", ""), "UTF-8");
        	URL url = new URL("http://linux.lsg.musin.de/cp/vp_app.php");
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
        	try {
        		JSONArray jArray = new JSONArray(get);
        		Toast.makeText(context, new Integer(jArray.length()).toString(), Toast.LENGTH_LONG).show();
        		int i = 0;
    			SQLiteDatabase myDB = context.openOrCreateDatabase(Functions.DB_NAME, context.MODE_PRIVATE, null);
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
        		} catch(JSONException e) {Log.d("json", e.getMessage());}
        	
        }
        catch(Exception e) {
	    	Log.d("except", e.getMessage());
        }
	}
	public static void getClass(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		try {
			Functions.testDB(context);
			String data = URLEncoder.encode("username", "UTF-8") + "=" + URLEncoder.encode(prefs.getString("username", ""), "UTF-8");
        	data       += "&" + URLEncoder.encode("password", "UTF-8") + "=" + URLEncoder.encode(prefs.getString("password", ""), "UTF-8");
        	URL url = new URL("http://linux.lsg.musin.de/cp/getClass.php");
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
    		myDB = context.openOrCreateDatabase(DB_NAME, context.MODE_PRIVATE, null);
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
	public static void refreshEvents(Context context) {
		 SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		try {
			Functions.testDB(context);
        	URL url = new URL("http://linux.lsg.musin.de/cp/termine_app.php");
        	HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        	// If you invoke the method setDoOutput(true) on the URLConnection, it will always use the POST method.
        	conn.setDoOutput(true);
        	conn.setRequestMethod("POST");
        	OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
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
        	try {
        		JSONArray jArray = new JSONArray(get);
        		Toast.makeText(context, new Integer(jArray.length()).toString(), Toast.LENGTH_LONG).show();
        		int i = 0;
    			SQLiteDatabase myDB = context.openOrCreateDatabase(Functions.DB_NAME, context.MODE_PRIVATE, null);
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
        		} catch(JSONException e) {Log.d("json", e.getMessage());}
        	
        }
        catch(Exception e) {
	    	Log.d("except", e.getMessage());
        }
	}
}