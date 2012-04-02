package com.lsg.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.MenuItem;

public class TimeTable extends Activity {
	private TimeTableViewPagerAdapter viewpageradap;
	private ViewPager pager;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Functions.setTheme(false, true, this);
		setContentView(R.layout.viewpager);
		viewpageradap = new TimeTableViewPagerAdapter(this);
	    pager = (ViewPager)findViewById(R.id.viewpager);
	    pager.setAdapter(viewpageradap);
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
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
}
