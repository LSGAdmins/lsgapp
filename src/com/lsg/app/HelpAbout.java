package com.lsg.app;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

public class HelpAbout extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Functions.setTheme(false, true, this);
		Bundle data = getIntent().getExtras();
		String type = data.getString(Functions.helpabout);
		if(type.equals(Functions.help)) {
			setContentView(R.layout.help);
			setTitle(getString(R.string.help));
		}
		else {
			setContentView(R.layout.about);
			setTitle(getString(R.string.about));
		}
		if(Build.VERSION.SDK_INT >=11) {
			ActionBar actionBar = getActionBar();
		    actionBar.setDisplayHomeAsUpEnabled(true); //click on logo should go back to device selection
		}
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
        case android.R.id.home:
            // app icon in action bar clicked; change mode
            Intent intent = new Intent(this, lsgapp.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
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

