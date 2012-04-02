package com.lsg.app;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class lsgapp extends Activity  implements ViewPager.OnPageChangeListener {
	Download down;
	private ViewPagerAdapter adapter;
	private ViewPager pager;
	private SharedPreferences prefs;
	private SharedPreferences.Editor edit;

	final Handler handler = new Handler() {
        public void handleMessage(Message msg) {
        	if(msg.arg1 == 1) {
//        		loading.cancel();
        		//adapter.updateCursor();
        	}
        	if(msg.arg1 == 2) {
//        		loading.cancel();
        	}
        	if(msg.arg1 == 3) {
//        		loading.cancel();
//    			loading = ProgressDialog.show(lsgapp.this, "", getString(msg.arg2), true);
        	}
        }
    };
    class UpdateCheck extends Thread {
    	Handler handler;
    	public UpdateCheck(Handler h) {
    		handler = h;
    	}
    	public void run() {
    		Looper.prepare();
        	String get = "";
    		try {
    			get = Functions.getData(Functions.UPDATE_CHECK_URL + getString(R.string.versionname), lsgapp.this, false, "");
            	try {
            		JSONObject jObject = new JSONObject(get);
            		if(!jObject.getBoolean("act")) {
            			Log.d("asdf", "notact");
            			final String actVersion = jObject.getString("actversion");
            			final String changelog  = jObject.getString("changelog");
            			Runnable notify = new Runnable() {
            				public void run() {
            					AlertDialog.Builder builder = new AlertDialog.Builder(lsgapp.this);
            					builder.setMessage(getString(R.string.update_available) + '\n' + actVersion + ": "+ '\n' + changelog)
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
            			};
            			handler.post(notify);
            		}
            		} catch(JSONException e) {Log.d("json", e.getMessage()); Log.d("asdf", e.getMessage());}
            }
            catch(Exception e) {
    	    	Log.d("except", e.getMessage());
            }
    	}
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        Functions.setTheme(false, false, this);
        Functions.testDB(this);
		getWindow().setBackgroundDrawableResource(R.layout.background);
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawableResource(R.layout.background);
        
    	prefs = PreferenceManager.getDefaultSharedPreferences(this);
    	edit = prefs.edit();
    	if(prefs.getBoolean("updatevplanonstart", false)) {
    		UpdateBroadcastReceiver.ProgressThread upd = new UpdateBroadcastReceiver.ProgressThread(new Handler(), this);
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
			if(prefs.getString(Functions.FULL_CLASS, "-1").equals("-1")) {
				SQLiteDatabase myDB = openOrCreateDatabase(Functions.DB_NAME, MODE_PRIVATE, null);
				Cursor cur = myDB.query(Functions.DB_CLASS_TABLE, new String[] {Functions.DB_CLASS}, Functions.DB_CLASS + " LIKE ?", new String[] {"%"
				+ prefs.getString("class", "").toLowerCase() + "%"},
				null, null, null);
				cur.moveToFirst();
				final CharSequence[] items = new CharSequence[cur.getCount()];
				int i = 0;
				while(i < cur.getCount()) {
					items[i] = cur.getString(cur.getColumnIndex(Functions.DB_CLASS));
					i++;
					cur.moveToNext();
					}
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
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
	    setContentView(R.layout.viewpager);
	    adapter = new ViewPagerAdapter(this);
	    pager = (ViewPager)findViewById( R.id.viewpager );
	    pager.setOnPageChangeListener(this);
	    /*TitlePageIndicator indicator =
	        (TitlePageIndicator)findViewById( R.id.indicator );*/
	    pager.setAdapter(adapter);
	    //indicator.setViewPager( pager );
        
        if(Functions.getSDK() >= 9)
 		   down = new Download(lsgapp.this);
        
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
	    inflater.inflate(R.menu.vplan, menu);
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
	}
}