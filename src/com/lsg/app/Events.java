package com.lsg.app;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListAdapter;
import android.widget.SimpleCursorAdapter;

public class Events extends ListActivity {
	Cursor d;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Functions.refreshEvents(this);
		
		Functions.setTheme(false, this);
		
		SQLiteDatabase myDB;
		myDB = this.openOrCreateDatabase(Functions.DB_NAME, MODE_PRIVATE, null);
		
		d = myDB.rawQuery("SELECT " + Functions.DB_ROWID + ", " + Functions.DB_DATES + ", " + Functions.DB_ENDDATES
				+ ", " + Functions.DB_TIMES + ", " + Functions.DB_ENDTIMES + ", " + Functions.DB_TITLE + ", " + Functions.DB_VENUE + " FROM "
        		+ Functions.DB_EVENTS_TABLE + ";", null);
		startManagingCursor(d);
		
		SimpleCursorAdapter device_event_adapter = new SimpleCursorAdapter(this,
				R.layout.events_item,
				d,
				new String[] { Functions.DB_TITLE, Functions.DB_VENUE, Functions.DB_DATES, Functions.DB_ENDDATES },
				new int[] { R.id.event_title, R.id.event_venue, R.id.event_date, R.id.event_enddate });
		getListView().setAdapter(device_event_adapter);
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.events, menu);
	    return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	    case R.id.settings:
	    	Intent settings = new Intent(this, Settings.class);
	    	startActivity(settings);
	        return true;
	    case R.id.refresh:
	    	Functions.refreshEvents(this);
	    	return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}
}
