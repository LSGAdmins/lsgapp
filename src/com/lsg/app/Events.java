package com.lsg.app;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class Events extends ListActivity implements SQLlist {
	private SQLiteDatabase myDB;
	private Cursor d;
	public EventCursor ecursor;
	private ProgressDialog loading;

	

	final Handler handler = new Handler() {
        public void handleMessage(Message msg) {
        	if(msg.arg1 == 1) {
        		loading.cancel();
        		updateCursor("");
        	}
        }
    };
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Functions.testDB(this);
		
		Functions.setTheme(false, true, this);
		
		getWindow().setBackgroundDrawableResource(R.layout.background);
		
		myDB = this.openOrCreateDatabase(Functions.DB_NAME, MODE_PRIVATE, null);
		
		
	
	ecursor = new EventCursor(this, d);
	getListView().setAdapter(ecursor);
	updateCursor("");

    Functions.styleListView(getListView(), this);
	
	
	}
	public void updateCursor(String where_cond) {
		d = myDB.rawQuery("SELECT " + Functions.DB_ROWID + ", " + Functions.DB_DATES + ", " + Functions.DB_ENDDATES
				+ ", " + Functions.DB_TIMES + ", " + Functions.DB_ENDTIMES + ", " + Functions.DB_TITLE + ", " + Functions.DB_VENUE + " FROM "
        		+ Functions.DB_EVENTS_TABLE + where_cond + ";", null);
		startManagingCursor(d);
		ecursor.changeCursor(d);
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.events, menu);
	    if(Build.VERSION.SDK_INT >= 11) {
	    	Advanced search = new Advanced();
	    	search.searchBarInit(menu, this);
	    }
	    return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	    case R.id.refresh:
			loading = ProgressDialog.show(Events.this, "", getString(R.string.loading_events), true);
			class ProgressThread extends Thread {
				Handler handler;
				ProgressThread(Handler h) {
					handler = h;
				}
				public void run() {
					Looper.prepare();
			    	Functions.refreshEvents(Events.this);
			    	Functions.getClass(Events.this);
			    	Message msg = handler.obtainMessage();
			    	msg.arg1 = 1;
			    	handler.sendMessage(msg);
				}
			}
			ProgressThread progress = new ProgressThread(handler);
			progress.start();
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
	public void updateWhereCond(String searchText) {
		String where_cond;

		/*myDB.execSQL("CREATE TABLE IF NOT EXISTS " + Functions.DB_EVENTS_TABLE
				+ " (" + Functions.DB_ROWID       + " integer primary key autoincrement,"
	    	    + Functions.DB_DATES              + " text,"
	     	    + Functions.DB_ENDDATES           + " text,"
	    	    + Functions.DB_TIMES              + " text,"
	    	    + Functions.DB_ENDTIMES           + " text,"
	    	    + Functions.DB_TITLE              + " text,"
	    	    + Functions.DB_VENUE              + " text"*/
		if(searchText.length() > 0)
			where_cond = " WHERE " + Functions.DB_DATES + " LIKE ''"; // TODO fix where cond
		else
			where_cond = "";
		updateCursor(where_cond);
		Log.d("asdf", "where");
	}
	public void onDestroy() {
		super.onDestroy();
		myDB.close();
	}
}
