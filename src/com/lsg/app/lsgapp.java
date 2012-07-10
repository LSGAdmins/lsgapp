package com.lsg.app;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

public class lsgapp extends Activity {
	Download down;
	private SharedPreferences prefs;
	private SharedPreferences.Editor edit;

	class CommonData {
		Context context;
		CommonData(Context c) {
			context = c;
		}
		public String[] updateSubjectList() {
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
			String add = "";
			try {
				add = "&" + URLEncoder.encode("date", "UTF-8") + "=" + URLEncoder.encode(prefs.getString("subject_update_time", ""), "UTF-8");
			} catch(UnsupportedEncodingException e) { Log.d("encoding", e.getMessage()); }
			String get = Functions.getData(Functions.SUBJECT_URL, context, true, add);
			if(!get.equals("networkerror") && !get.equals("loginerror") && !get.equals("noupdate")) {
				try {
	        		JSONArray jArray = new JSONArray(get);
	        		int i = 0;
	    			SQLiteDatabase myDB = context.openOrCreateDatabase(Functions.DB_NAME, Context.MODE_PRIVATE, null);
	    			myDB.delete(Functions.DB_SUBJECT_TABLE, null, null); //clear subjectlist
	        		while(i < jArray.length() - 1) {
	        			JSONObject jObject = jArray.getJSONObject(i);
	        			ContentValues values = new ContentValues();
	        			values.put(Functions.DB_RAW_FACH, jObject.getString("kuerzel"));
	        			values.put(Functions.DB_FACH, jObject.getString("name"));
	            		myDB.insert(Functions.DB_SUBJECT_TABLE, null, values);
	        			i++;
	        			}
	        		JSONObject jObject            = jArray.getJSONObject(i);
	        		String update_time            = jObject.getString("update_time");
	        		SharedPreferences.Editor edit = prefs.edit();
	        		edit.putString("subject_update_time", update_time);
	        		edit.commit();
	        		myDB.close();
	        		} catch(JSONException e) {
	        			Log.d("json", e.getMessage());
	        			return new String[] {"json", context.getString(R.string.jsonerror)};
	        		}
				}
			else if(get.equals("networkerror"))
				return new String[] {"networkerror", context.getString(R.string.networkerror)};
			else if(get.equals("loginerror"))
				return new String[] {"loginerror", context.getString(R.string.loginerror)};
			return new String[] {"success", ""};
		}
		public boolean getClasses() {
			SQLiteDatabase myDB = context.openOrCreateDatabase(Functions.DB_NAME, Context.MODE_PRIVATE, null);
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
			try {
				String get = Functions.getData(Functions.CLASS_URL, context, true, "");
				Log.d("classes", get);
				if(!get.equals("loginerror")) {
					JSONArray jArray = new JSONArray(get);
					int i = 0;
					myDB.delete(Functions.DB_CLASS_TABLE, null, null);
					while(i < jArray.length()-1) {
						ContentValues vals = new ContentValues();
						vals.put(Functions.DB_CLASS, jArray.getString(i));
						myDB.insert(Functions.DB_CLASS_TABLE, null, vals);
						i++;
						}
					SharedPreferences.Editor editor = prefs.edit();
					editor.putString("class", jArray.getString(i));
					editor.commit();
					myDB.close();
					}
				else {
					myDB.close();
					return false;
				}
	        }
	        catch(Exception e) {
	        	myDB.close();
		    	Log.d("getClass()", e.getMessage());
	        }
			return true;
		}
	}
	class CommonDataTask extends AsyncTask<Void, Void, String[]> {
		protected void onPreExecute() {
			super.onPreExecute();
			Functions.lockRotation(lsgapp.this);
			loading = ProgressDialog.show(lsgapp.this, "", lsgapp.this.getString(R.string.loading_common_data));
		}
		@Override
		protected String[] doInBackground(Void... params) {
			CommonData cd = new CommonData(lsgapp.this);
			boolean login = cd.getClasses();
			String res[] = cd.updateSubjectList();
			if(!login) {
				res[0] = "loginerror";
				res[1] = lsgapp.this.getString(R.string.loginerror);
				}
			return res;
			}
		protected void onPostExecute(final String[] res) {
			loading.cancel();
			if(!res[0].equals("success"))
				Toast.makeText(lsgapp.this, res[1], Toast.LENGTH_LONG).show();
			if(res[0].equals("loginerror")) {
				Intent intent;
				if(Functions.getSDK() >= 11)
					intent = new Intent(lsgapp.this, SettingsAdvanced.class);
				else
					intent = new Intent(lsgapp.this, Settings.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
				lsgapp.this.startActivity(intent);
				}
			else if(res[0].equals("success")) {
				if(prefs.getString(Functions.FULL_CLASS, "-1").equals("-1") || !prefs.getString(Functions.FULL_CLASS, "").contains(prefs.getString(Functions.class_key, "-1"))) {
					SQLiteDatabase myDB = openOrCreateDatabase(Functions.DB_NAME, MODE_PRIVATE, null);
					myDB.delete(Functions.DB_TIME_TABLE, null, null);
					SQLiteStatement num_rows = myDB.compileStatement("SELECT COUNT(*) FROM " + Functions.DB_CLASS_TABLE);
					long count = num_rows.simpleQueryForLong();
					num_rows.close();
					Cursor cur = myDB.query(Functions.DB_CLASS_TABLE, new String[] {Functions.DB_CLASS}, Functions.DB_CLASS + " LIKE ?", new String[] {"%"
					+ prefs.getString("class", "").toLowerCase() + "%"},
					null, null, null);
					cur.moveToFirst();
					if(count == 1) {
						edit.putString(Functions.FULL_CLASS, cur.getString(cur.getColumnIndex(Functions.DB_CLASS)));
						edit.commit();
						myDB.close();
						cur.close();
					} else if(count > 1) {
					final CharSequence[] items = new CharSequence[cur.getCount()];
					int i = 0;
					while(i < cur.getCount()) {
						items[i] = cur.getString(cur.getColumnIndex(Functions.DB_CLASS));
						i++;
						cur.moveToNext();
						}
					AlertDialog.Builder builder = new AlertDialog.Builder(lsgapp.this);
					builder.setTitle(R.string.your_class);
					builder.setItems(items, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int item) {
							edit.putString(Functions.FULL_CLASS, (String) items[item]);
							edit.commit();
							}
						});
					AlertDialog alert = builder.create();
					alert.show();
					myDB.close();
					cur.close();
					}
				}
				}
			Functions.unlockRotation(lsgapp.this);
			}
			}
	class UpdateCheckTask extends AsyncTask<Void, Void, String[]> {
		protected void onPreExecute() {
			super.onPreExecute();
		}
		@Override
		protected String[] doInBackground(Void... params) {
			String get = Functions.getData(Functions.UPDATE_CHECK_URL + getString(R.string.versionname), lsgapp.this, false, "");
			try {
				JSONObject jObject = new JSONObject(get);
				if(!jObject.getBoolean("act")) {
					return new String[] {"notact", jObject.getString("actversion"), jObject.getString("changelog")};
					}
				else
					return new String[] {"act"};
				} catch(JSONException e) {Log.d("json", e.getMessage()); Log.d("asdf", e.getMessage());}
			return new String[] {"act"};
			}
		protected void onPostExecute(final String[] data) {
			if(data[0].equals("notact")) {
				AlertDialog.Builder builder = new AlertDialog.Builder(lsgapp.this);
				builder.setMessage(getString(R.string.update_available) + '\n' + data[1] + ": "+ '\n' + data[2])
				.setCancelable(false)
				.setPositiveButton(getString(R.string.update), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						Toast.makeText(lsgapp.this, getString(R.string.downloading), Toast.LENGTH_LONG).show();
						if(Functions.getSDK() >= 11) { //could also be 9, but there are some failed downloads on sgs2
							down.download();
							}
						else {
							Intent intent = new Intent(Intent.ACTION_VIEW);
							intent.setData(Uri.parse(Functions.UPDATE_URL));
							startActivity(intent);
							}
						}
					})
					.setNegativeButton(getString(R.string.no_update), new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.cancel();
							}
						});
				AlertDialog alert = builder.create();
				alert.show();
				}
			}
		}
	
	class LoginTest extends AsyncTask<Void, Void, Boolean> {
		Context context;
		LoginTest(Context c) {
			context = c;
		}
		protected void onPreExecute() {
			super.onPreExecute();
			loading = ProgressDialog.show(context, null, "Teste Login-Daten...");
		}
		@Override
		protected Boolean doInBackground(Void... params) {
			return Functions.testLogin(context);
		}
		@Override
		protected void onPostExecute(Boolean success) {
			super.onPostExecute(success);
			loading.cancel();
			if(success) {
				loginsuccess = true;
				step = 1;
				setup(step, false);
			}
			else
	    		setupUser();
		}
	}
	class PersonData extends AsyncTask<Void, Void, String> {
		Context context;
		PersonData(Context c) {
			context = c;
		}
		protected void onPreExecute() {
			super.onPreExecute();
			loading = ProgressDialog.show(context, null, "Lade Daten...");
		}
		@Override
		protected String doInBackground(Void... params) {
			return Functions.getData(Functions.PERSON_DATA_URL, context, true, "");
		}
		@Override
		protected void onPostExecute(String data) {
			super.onPostExecute(data);
			loading.cancel();
			try {
				JSONObject jarr = new JSONObject(data);
				gender = jarr.getString("gender").charAt(0);
				religion = jarr.getString("religion");
			} catch (Exception e) {}
			dataUser = prefs.getString("username", "");
			setVPlanData();
		}
	}
	private ProgressDialog loading;
	private static int step = 0;
	private static boolean loginsuccess = false;
	private static char gender = '0';
	private static String religion = null;
	private static String dataUser = null;
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	
    	super.onCreate(savedInstanceState);
        Functions.setTheme(false, false, this);
        Functions.testDB(this);
		getWindow().setBackgroundDrawableResource(R.layout.background);
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		edit = prefs.edit();
		if(prefs.getBoolean("firstrun", true)) {
			setup(step, false);
		} else
			startActivity(new Intent(this, TimeTable.class));
		/*
        setContentView(R.layout.main_nav);

	    UpdateCheckTask upcheck = new UpdateCheckTask();
	    upcheck.execute();
        
    	prefs = PreferenceManager.getDefaultSharedPreferences(this);
    	edit = prefs.edit();
    	if(prefs.getBoolean("updatevplanonstart", false)) {
    		UpdateBroadcastReceiver.VPupdate upd = new UpdateBroadcastReceiver.VPupdate(this);
    		upd.start();
    	}
    	Functions.setAlarm(this);
		Intent testAC2DM = new Intent("com.google.android.c2dm.intent.REGISTER");
		if(startService(testAC2DM) == null) {
			Log.i(Functions.TAG, "c2dm not available; disabling");
			edit.putBoolean("disableAC2DM", true);
			edit.putBoolean("useac2dm", false);
			edit.commit();
		}

        if(Functions.getSDK() >= 9)
 		   down = new Download(lsgapp.this);*/
		}
    public void setup(int step, boolean force) {
    	if(!loginsuccess)
    		step = 0;
    	switch(step) {
    	case 0:
    		if(!force)
    			testUser();
    		else
    			setupUser();
    		break;
    	case 1:
    		setupVPlan();
    		break;
    	}
    }
    public void testUser() {
    	setTitle("LSGäpp Setup");
    	if(!prefs.getString("username", "").equals("")) {
    		LoginTest test = new LoginTest(this);
    		test.execute();
    	} else
    		setupUser();
    }
    public void setupUser() {
    	setContentView(R.layout.setup_user);
    	((EditText) findViewById(R.id.username)).setText(prefs.getString("username", ""));
    	((EditText) findViewById(R.id.password)).setText(prefs.getString("password", ""));
    }
    public void setupVPlan() {
    	setContentView(R.layout.setup_vplan);
    	if(dataUser == null || !dataUser.equals(prefs.getString("username", ""))) {
    		PersonData pd = new PersonData(this);
    		pd.execute();
    		} else
    			setVPlanData();
    }
    public void setVPlanData() {
		RadioGroup genderrg = (RadioGroup) findViewById(R.id.gendergroup);
		if(gender == 'm')
			genderrg.check(R.id.male);
		else
			genderrg.check(R.id.female);
		RadioGroup religionrg = (RadioGroup) findViewById(R.id.religiongroup);
		if(religion.equals("K"))
			religionrg.check(R.id.catholic);
		else if(religion.equals("Ev"))
				religionrg.check(R.id.protestant);
		else if(religion.equals("Eth"))
			religionrg.check(R.id.ethnic);
    }
    public void next(View v) {
    	switch(step) {
    	case 0:
    		edit.putString("username", (((EditText) findViewById(R.id.username))).getText().toString());
    		edit.putString("password", (((EditText) findViewById(R.id.password))).getText().toString());
    		testUser();
    		break;
    	case 1:
    		startActivity(new Intent(this, TimeTable.class));
    		break;
    	}
    	//step += 1;
    	//setup(step);
    	//startActivity(new Intent(this, TimeTable.class));
    }
    public void previous(View v) {
    	step -= 1;
    	setup(step, true);
    }
    
    
    public void button_press(View v) {
    	Log.d("text", (String) ((Button) v).getText());
    	if(((String) ((Button) v).getText()).equals(getString(R.string.timetable))) {
    		Intent timetable = new Intent(this, TimeTable.class);
    		startActivity(timetable);
    	}
    	if(((String) ((Button) v).getText()).equals(getString(R.string.vplan_short))) {
    		Intent timetable = new Intent(this, VPlan.class);
    		startActivity(timetable);
    	}
    	if(((String) ((Button) v).getText()).equals(getString(R.string.events))) {
    		Intent timetable = new Intent(this, Events.class);
    		startActivity(timetable);
    	}
    	if(((String) ((Button) v).getText()).equals(getString(R.string.smvblog))) {
    		Intent timetable = new Intent(this, SMVBlog.class);
    		startActivity(timetable);
    	}
    	if(((String) ((Button) v).getText()).equals(getString(R.string.settings))) {
			Intent settings;
			if(Functions.getSDK() >= 11)
				settings = new Intent(this, SettingsAdvanced.class);
			else
				settings = new Intent(this, Settings.class);
    		startActivity(settings);
    	}
    }
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
	    //inflater.inflate(R.menu.lsgapp, menu);
	    return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
	    case R.id.help:
	    	Intent help = new Intent(this, HelpAbout.class);
	    	help.putExtra(Functions.helpabout, Functions.help);
	    	startActivity(help);
	        return true;
	    case R.id.about:
	    	Intent about = new Intent(this, HelpAbout.class);
	    	about.putExtra(Functions.helpabout, Functions.about);
	    	startActivity(about);
	    	return true;
	    case R.id.refresh:
	    	CommonDataTask cdt = new CommonDataTask();
	    	cdt.execute();
	    	return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}
	@Override
	public void onPause() {
		super.onPause();
		/*if(Functions.getSDK() >= 9)
			unregisterReceiver(down.downloadReceiver);*/
	}
	@Override
	public void onResume() {
		super.onResume();/*
		if(Functions.getSDK() >= 9) {
			IntentFilter intentFilter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
			registerReceiver(down.downloadReceiver, intentFilter);
		}
		
		if(!prefs.getString("username", "").equals("")) {
			if(!prefs.getBoolean("disableAC2DM", false) && !prefs.getBoolean("ac2dm_chosen", false)) {
				AlertDialog.Builder builder = new AlertDialog.Builder(lsgapp.this);
				builder.setMessage(getString(R.string.enable_AC2DM))
				.setCancelable(false)
				.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						edit.putBoolean("useac2dm", true);
			       		edit.putBoolean("ac2dm_chosen", true);
			       		edit.commit();
			       		Functions.registerAC2DM(lsgapp.this);
			       		dialog.cancel();
			       		}
					})
			       .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
				       		edit.putBoolean("useac2dm", false);
				       		edit.putBoolean("ac2dm_chosen", true);
				       		edit.commit();
			                dialog.cancel();
			           }
			       });
				AlertDialog alert = builder.create();
				builder.setCancelable(false);
				alert.show();
				}
			if(prefs.getString(Functions.RELIGION, "-1").equals("-1")) {
				final CharSequence[] items = getResources().getStringArray(R.array.religion);
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle(R.string.choose_religion);
				builder.setItems(items, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {
						switch(item) {
						case 0:
							edit.putString(Functions.RELIGION, Functions.KATHOLISCH);
							break;
						case 1:
							edit.putString(Functions.RELIGION, Functions.EVANGELISCH);
							break;
							default:
								edit.putString(Functions.RELIGION, Functions.ETHIK);
								break;
								}
						edit.commit();
						dialog.cancel();
						}
					});
				AlertDialog alert = builder.create();
				builder.setCancelable(false);
				alert.show();
				}
			if(prefs.getString(Functions.GENDER, "-1").equals("-1")) {
				final CharSequence[] items = getResources().getStringArray(R.array.gender);
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle(R.string.your_gender);
				builder.setItems(items, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {
						switch(item) {
						case 0:
							edit.putString(Functions.GENDER, "m");
							break;
						case 1:
							edit.putString(Functions.GENDER, "w");
							break;
							default:
								edit.putString(Functions.GENDER, "-1");
								break;
								}
						edit.commit();
						dialog.cancel();
						}
					});
				builder.setCancelable(false);
				AlertDialog alert = builder.create();
				builder.setCancelable(false);
				alert.show();
				}
	        }
		SQLiteDatabase myDB = openOrCreateDatabase(Functions.DB_NAME, Context.MODE_PRIVATE, null);
		SQLiteStatement num_rows = myDB.compileStatement("SELECT COUNT(*) FROM " + Functions.DB_CLASS_TABLE);
		long count = num_rows.simpleQueryForLong();
		if(count == 0) {
			CommonDataTask cdt = new CommonDataTask();
			cdt.execute();
		}
		myDB.close();
		num_rows.close();*/
		}
	}