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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

public class Events extends ListActivity implements SQLlist, TextWatcher{
	private SQLiteDatabase myDB;
	private Cursor d;
	public EventCursor ecursor;
	private ProgressDialog loading;
	private String[] where_conds = new String[6];
	
	final Handler handler = new Handler() {
		public void handleMessage(Message msg) {
        	if(msg.arg1 == 1) {
        		loading.cancel();
        		updateCursor();
        	}
        }
    };
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Functions.testDB(this);
		
		Functions.setTheme(false, true, this);
		
		getWindow().setBackgroundDrawableResource(R.layout.background);
		
		//set header search bar
		if(Build.VERSION.SDK_INT < 11) {
			LayoutInflater inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
			View search = inflater.inflate(R.layout.search, null);
			EditText searchEdit = (EditText) search.findViewById(R.id.search_edit);
			searchEdit.addTextChangedListener(this);
			getListView().addHeaderView(search);
			}
		
		myDB = this.openOrCreateDatabase(Functions.DB_NAME, MODE_PRIVATE, null);
		
		ecursor = new EventCursor(this, d);
		getListView().setAdapter(ecursor);
		updateWhereCond("");
		updateCursor();
		
		Functions.styleListView(getListView(), this);
	}
	public void updateCursor() {
		String where_cond = " " + Functions.DB_DATES + " LIKE ? OR " + Functions.DB_ENDDATES + " LIKE ? OR "
		+ Functions.DB_TIMES + " LIKE ? OR " + Functions.DB_ENDTIMES + " LIKE ? OR " + Functions.DB_TITLE + " LIKE ? OR "
				+ Functions.DB_VENUE + " LIKE ? ";
		d = myDB.query(Functions.DB_EVENTS_TABLE, new String [] {Functions.DB_ROWID, Functions.DB_DATES, Functions.DB_ENDDATES,
				Functions.DB_TIMES,	Functions.DB_ENDTIMES, Functions.DB_TITLE, Functions.DB_VENUE}, where_cond,
				where_conds, null, null, null);
		ecursor.changeCursor(d);
		Log.d("asdf", "where");
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.events, menu);
	    if(Build.VERSION.SDK_INT >= 11) {
	    	Advanced search = new Advanced();
	    	search.searchBarInit(menu, this);
	    }
	    else
	    	menu.removeItem(R.id.search);
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
			    	Functions.refreshEvents(Events.this, handler);
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
		where_conds[0] = "%" + searchText + "%";
		where_conds[1] = "%" + searchText + "%";
		where_conds[2] = "%" + searchText + "%";
		where_conds[3] = "%" + searchText + "%";
		where_conds[4] = "%" + searchText + "%";
		where_conds[5] = "%" + searchText + "%";
		updateCursor();
	}
	public void afterTextChanged (Editable s) {
	}
	public void beforeTextChanged (CharSequence s, int start, int count, int after) {
		
	}
	public void onTextChanged (CharSequence s, int start, int before, int count) {
		String search = s + "";
		updateWhereCond(search);
	}
	public void onDestroy() {
		super.onDestroy();
		myDB.close();
	}
}
