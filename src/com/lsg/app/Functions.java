package com.lsg.app;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.Toast;

public class Functions {
	public static final String   BLACKWHITELIST = "blackwhitelist";
	public static final String   BLACKLIST      = "blacklist";
	public static final String   WHITELIST      = "whitelist";
	
	public static final String   UPDATE_URL  = "http://linux.lsg.musin.de/cp/downloads/lsgapp.apk";
	public static final String   VP_URL      = "http://linux.lsg.musin.de/cp/vp_app.php";
	public static final String   EVENT_URL   = "http://linux.lsg.musin.de/cp/termine_app.php";
	public static final String   CLASS_URL   = "http://linux.lsg.musin.de/cp/getClass.php";
	public static final String   API_VERSION = "2";
	
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
	public static final String DB_RAW_FACH        = "rawfach";
	public static final String DB_DATE            = "date";
	//exclude & include
	public static final String EXCLUDE_TABLE      = "exclude";
	public static final String INCLUDE_TABLE      = "include";
	public static final String DB_NEEDS_SYNC      = "needssync";
	
	public static void setTheme(boolean dialog, boolean homeasup, Activity act) {
		int theme = android.R.style.Theme_Light;
		if(Functions.getSDK() >= 11) {
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
			try {
				AdvancedWrapper advWrapper = new AdvancedWrapper();
				advWrapper.homeasup(act);
			} catch (Exception e) {
			}
		}
	}
	public static void styleListView(ListView lv, Context context) {
		if(Functions.getSDK() >= 11) {
			ColorDrawable sage = new ColorDrawable(context.getResources().getColor(R.color.seperatorgrey));
			lv.setDivider(sage);
			lv.setDividerHeight(2);
		}
	}
	
	public static int getSDK() {
		return new Integer(Build.VERSION.SDK);
		/*if(Build.VERSION.SDK.equals("3"))
			return 3;
		else
			return Build.VERSION.SDK_INT;*/
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
        	BufferedReader reader = new BufferedReader(new InputStreamReader(response));
        	String line;
        	String get = "";
        	while ((line = reader.readLine()) != null) {
        		get += line;
        		}
        	Log.d("get", get);
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
	
	public static boolean refreshVPlan(final Context context, Handler h) {
		Functions.testDB(context);
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		String add = "";
		try {
			add = "&" + URLEncoder.encode("date", "UTF-8") + "=" + URLEncoder.encode(prefs.getString("date", ""), "UTF-8")
					+ "&" + URLEncoder.encode("time", "UTF-8") + "=" + URLEncoder.encode(prefs.getString("time", ""), "UTF-8");
		} catch(UnsupportedEncodingException e) { Log.d("encoding", e.getMessage()); }
		String get = Functions.getData(Functions.VP_URL, context, true, add);
		if(!get.equals("networkerror") && !get.equals("loginerror") && !get.equals("noact")) {
			try {
        		JSONArray jArray = new JSONArray(get);
        		Toast.makeText(context, new Integer(jArray.length()).toString(), Toast.LENGTH_LONG).show();
        		int i = 0;
    			SQLiteDatabase myDB = context.openOrCreateDatabase(Functions.DB_NAME, Context.MODE_PRIVATE, null);
    			myDB.delete(Functions.DB_TABLE, null, null); //clear vertretungen
        		while(i < jArray.length() - 1) {
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
        			values.put(Functions.DB_RAW_FACH, jObject.getString("rawfach"));
        			values.put(Functions.DB_DATE, jObject.getString("date"));
            		myDB.insert(Functions.DB_TABLE, null, values);
        			i++;
        			}
        		JSONObject jObject            = jArray.getJSONObject(i);
        		String date                   = jObject.getString("date");
        		String time                   = jObject.getString("time");
        		SharedPreferences.Editor edit = prefs.edit();
        		edit.putString("date", date);
        		edit.putString("time", time);
        		edit.commit();
        		myDB.close();
        		} catch(JSONException e) {
        			Log.d("json", e.getMessage());
        			h.post(getErrorRunnable("jsonerror", context));
        			return false;
        		}
			}
		else if(get.equals("noact")) {
			Runnable r = new Runnable() {
				public void run() {
					Toast.makeText(context, context.getString(R.string.noact), Toast.LENGTH_SHORT).show();
				}
			};
			h.post(r);
			return true;
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
			String get = Functions.getData(Functions.CLASS_URL, context, true, "");
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
    		/*Cursor c = myDB.query(Functions.DB_TABLE, new String[] {Functions.DB_RAW_FACH}, null, null, null, null, null);
    		if(c.getColumnIndex(Functions.DB_RAW_FACH) != -1)
    			myDB.setVersion(1);*/
    		//upgrades for table
    		if(myDB.getVersion() == 0) {
    			Log.d(Functions.DB_TABLE, "adding column " + Functions.DB_RAW_FACH);
    			myDB.execSQL("ALTER TABLE " + Functions.DB_TABLE + " ADD COLUMN " + Functions.DB_RAW_FACH + " text");
    			myDB.setVersion(1);
    		}
    		if(myDB.getVersion() == 1) {
    			Log.d(Functions.EXCLUDE_TABLE, "adding column " + Functions.DB_RAW_FACH);
    			myDB.execSQL("ALTER TABLE " + Functions.EXCLUDE_TABLE + " ADD COLUMN " + Functions.DB_RAW_FACH + " text");
    			Log.d(Functions.INCLUDE_TABLE, "adding column " + Functions.DB_RAW_FACH);
    			myDB.execSQL("ALTER TABLE " + Functions.INCLUDE_TABLE + " ADD COLUMN " + Functions.DB_RAW_FACH + " text");
    			myDB.setVersion(2);
    		}
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
		String get = Functions.getData(Functions.EVENT_URL, context, false, "");
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
	//handlers for adding / removing items to / from blacklist
	public static void createContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo, SQLiteDatabase myDB, Context context) {

		AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
		Cursor cur = myDB.query(Functions.DB_TABLE, new String[] {Functions.DB_KLASSE, Functions.DB_FACH, Functions.DB_RAW_FACH}, Functions.DB_ROWID + " = ?",
				new String[] {new Long(info.id).toString()}, null, null, null);
		cur.moveToFirst();
		
		String klasse  = cur.getString(cur.getColumnIndex(Functions.DB_KLASSE));
		String rawfach = cur.getString(cur.getColumnIndex(Functions.DB_RAW_FACH));
		String fach    = cur.getString(cur.getColumnIndex(Functions.DB_FACH));
		cur.close();
		
		int conmenu = 0;
		int i = 0;
		while(i < Functions.exclude.length) {
			if(klasse.contains(Functions.exclude[i]))
				conmenu = 1;
			i++;
			}
		if(klasse.equals("null"))
			conmenu = 1;
		
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
	public static boolean contextMenuSelect(MenuItem item, final SQLiteDatabase myDB, Context context, final SQLlist list) {
		  AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
		  
		  Cursor cur = myDB.query(Functions.DB_TABLE, new String[] {Functions.DB_KLASSE, Functions.DB_FACH, Functions.DB_RAW_FACH}, Functions.DB_ROWID + " = ?",
				  new String[] {new Long(info.id).toString()}, null, null, null);
		  cur.moveToFirst();
		  
		  //final String klasse  = cur.getString(cur.getColumnIndex(Functions.DB_KLASSE));
		  final String rawfach = cur.getString(cur.getColumnIndex(Functions.DB_RAW_FACH));
		  final String fach    = cur.getString(cur.getColumnIndex(Functions.DB_FACH));
		  cur.close();
		  final String prompt;
		  final String table;
			
		  //final CharSequence title = ((TextView) info.targetView.findViewById(R.id.vertretung_title)).getText();
		  int menuItemIndex = item.getItemId();
		  if(menuItemIndex == 0) {
			  prompt = context.getString(R.string.really_exclude);
			  table  = Functions.EXCLUDE_TABLE;
		  }
		  else if(menuItemIndex == 1) {
			  prompt = context.getString(R.string.really_include);
			  table  = Functions.INCLUDE_TABLE;
		  }
		  else {
			  //this code shouldnt be executed, its just for the compiler not to complain :-)
			  prompt = "";
			  table = "";
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
				        	myDB.insert(table, null, vals);
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
}