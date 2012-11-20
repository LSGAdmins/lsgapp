package com.lsg.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.MenuItem;
import android.view.View;

public class HelpAbout extends FragmentActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Functions.setTheme(false, true, this);
		getWindow().setBackgroundDrawableResource(R.layout.background);
		Bundle data = getIntent().getExtras();
		String type = data.getString(Functions.HELPABOUT);
		if(type.equals(Functions.help)) {
			setContentView(R.layout.help);
			setTitle(getString(R.string.help));
		}
		else {
			setContentView(R.layout.about);
			setTitle(getString(R.string.about));
		}
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
        case android.R.id.home:
            // app icon in action bar clicked; change mode
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

