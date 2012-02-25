package com.lsg.app;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class VPlan extends ListActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Functions.setTheme(false, this);
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
