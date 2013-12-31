package com.lsg.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;

public class HelpAbout extends ActionBarActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle data = getIntent().getExtras();
		String type = data.getString(Functions.HELPABOUT);
		if(type.equals(Functions.help)) {
			setContentView(R.layout.help);
			setTitle(getString(R.string.help));
		}
		else {
			setContentView(R.layout.activity_about);
			setTitle(getString(R.string.about));
		}
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
        case android.R.id.home:
            finish();
            return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}
	public void hp(View v) {
		Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://launchpad.net/bluetoothcarcontroller"));
		startActivity(browserIntent);
	}
}

