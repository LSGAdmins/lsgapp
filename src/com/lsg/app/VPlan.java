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

public class VPlan extends ListActivity {
	Cursor c;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Functions.refreshVPlan(this);
		
		Functions.setTheme(false, this);
		
		SQLiteDatabase myDB;
		myDB = this.openOrCreateDatabase(Functions.DB_NAME, MODE_PRIVATE, null);
		
		c = myDB.rawQuery("SELECT " + Functions.DB_ROWID + ", " + Functions.DB_KLASSE + ", " + Functions.DB_ART
				+ ", " + Functions.DB_STUNDE + ", " + Functions.DB_VERTRETER + " FROM "
        		+ Functions.DB_TABLE + ";", null);
		startManagingCursor(c);
		
		SimpleCursorAdapter device_adapter = new SimpleCursorAdapter(this,
				R.layout.vertretung_item,
				c,
				new String[] { Functions.DB_KLASSE, Functions.DB_ART, Functions.DB_STUNDE, Functions.DB_VERTRETER },
				new int[] { R.id.vertretung_title, R.id.vertretung_type, R.id.vertretung_when, R.id.vertretung_bottom });
		getListView().setAdapter(device_adapter);
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.vplan, menu);
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
	    	Functions.refreshVPlan(this);
	    	return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}
}
