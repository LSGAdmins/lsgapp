package com.lsg.app;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class lsgapp extends Activity /* implements ViewPager.OnPageChangeListener*/ {
	Download down;
	private ViewPagerAdapter adapter;
	private ViewPager pager;
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
	private ProgressDialog loading;
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        Functions.setTheme(false, false, this);
        Functions.testDB(this);
		getWindow().setBackgroundDrawableResource(R.layout.background);
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawableResource(R.layout.background);
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
 		   down = new Download(lsgapp.this);
		}
    /*
	    setContentView(R.layout.viewpager);
	    adapter = new ViewPagerAdapter(this);
	    pager = (ViewPager)findViewById( R.id.viewpager );
	    pager.setOnPageChangeListener(this);
	    /*TitlePageIndicator indicator =
	        (TitlePageIndicator)findViewById( R.id.indicator );*//*
	    pager.setAdapter(adapter);
	    //indicator.setViewPager( pager );
        
        
        UpdateCheck uCheck = new UpdateCheck(handler);
        uCheck.start();
    }
    public void updateVP() {
		UpdateBroadcastReceiver.ProgressThread progress = new UpdateBroadcastReceiver.ProgressThread(handler, this);
		progress.start();
		}
	public void setPage(int page) {
		pager.setCurrentItem(page);
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.main, menu);
	    if(Functions.getSDK() >= 11) {
	    	AdvancedWrapper ahelp = new AdvancedWrapper();
	    	ahelp.searchBar(menu, adapter);
	    }
	    else
	    	menu.removeItem(R.id.search);
	    return true;
	}
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.removeItem(R.id.all);
		menu.removeItem(R.id.mine);
	    if(Functions.getSDK() < 11) {
	    	if(pager.getCurrentItem() == 0)
	    		menu.add(0, R.id.all, Menu.NONE, R.string.all);
	    	else
	    		menu.add(0, R.id.mine, Menu.NONE, R.string.mine);
	    }
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	    case R.id.settings:
	    	Intent settings;
	    	if(Functions.getSDK() >= 11)
	    		settings = new Intent(this, SettingsAdvanced.class);
	    	else
	    		settings = new Intent(this, Settings.class);
	    	startActivity(settings);
	        return true;
	    case R.id.refresh:
	    	updateVP();
	    	return true;
	    case R.id.mine:
	    	setPage(0);
	    	return true;
	    case R.id.all:
	    	setPage(1);
	    	return true;
	    case R.id.subjects:
            Intent subjects = new Intent(this, SubjectList.class);
            startActivity(subjects);
	    	return true;
	    case R.id.timetable:
	    	Intent timetable = new Intent(this, TimeTable.class);
	    	startActivity(timetable);
	    	return true;
	    case R.id.info:
	    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    	builder.setMessage(getString(R.string.number_all) + " " + Integer.valueOf(adapter.second_c.getCount()).toString() + "\n"
	    			+ getString(R.string.number_mine) + " " + Integer.valueOf(adapter.c.getCount()).toString() + "\n"
	    			+ getString(R.string.actdate) + prefs.getString("vplan_date", "") + " / " + prefs.getString("vplan_time", ""))
	    	       .setCancelable(true)
	    	       .setNeutralButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
	    	           public void onClick(DialogInterface dialog, int id) {
	    	                dialog.cancel();
	    	           }
	    	       });
	    	AlertDialog alert = builder.create();
	    	alert.show();
	    	return true;
        case android.R.id.home:
            // app icon in action bar clicked; go home
            Intent intent = new Intent(this, lsgapp.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            return true;
        case R.id.vplan_item:
            Intent vplan_int = new Intent(this, VPlan.class);
            startActivity(vplan_int);
        	return true;
        case R.id.event_item:
            Intent event_int = new Intent(this, Events.class);
            startActivity(event_int);
        	return true;
        case R.id.smv_item:
            Intent smv_int = new Intent(this, SMVBlog.class);
            startActivity(smv_int);
        	return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}
	public void onPageScrollStateChanged (int state) {}
	public void onPageScrolled (int position, float positionOffset, int positionOffsetPixels) {}
	public void onPageSelected (int position) {
		if(Functions.getSDK() >= 11) {
			AdvancedWrapper adv = new AdvancedWrapper();
			adv.selectedItem(position, this);
			}
		if(position == 0)
			setTitle(R.string.timetable);
		else if(position == 1)
			setTitle(R.string.mine);
		else if(position == 2)
			setTitle(R.string.all);
			else if(position == 3)
				setTitle(R.string.events);
			else if(position == 4)
				setTitle(R.string.smvblog);
	}
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		Functions.createContextMenu(menu, v, menuInfo, this, Functions.DB_VPLAN_TABLE);
	}
	@Override
	public boolean onContextItemSelected(final MenuItem item) {
		return Functions.contextMenuSelect(item, this, adapter, Functions.DB_VPLAN_TABLE);
	}
	@Override
	public void onDestroy() {
		super.onDestroy();
		adapter.closeCursorsDB();
	}*/

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
	    inflater.inflate(R.menu.lsgapp, menu);
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
		if(Functions.getSDK() >= 9)
			unregisterReceiver(down.downloadReceiver);
	}
	@Override
	public void onResume() {
		super.onResume();
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
		num_rows.close();
		}
}