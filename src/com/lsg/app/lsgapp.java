package com.lsg.app;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class lsgapp extends ListActivity {
    /** Called when the activity is first created. Test */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Functions.setTheme(false, false, this);
        String[] actions = {getString(R.string.vplan), getString(R.string.events), getString(R.string.smvblog)};
        setListAdapter(new ArrayAdapter<String>(this, R.layout.main_listitem, actions));
    }
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		String listtext = (String) ((TextView) v).getText();
		if(listtext.equals(getString(R.string.vplan))) {
			Intent intent = new Intent(this, VPlan.class);
			startActivity(intent);
		}
		if(listtext.equals(getString(R.string.events))) {
			Intent intent = new Intent(this, Events.class);
			startActivity(intent);
		}
		if(listtext.equals(getString(R.string.smvblog))) {
			Intent intent = new Intent(this, SMVBlog.class);
			startActivity(intent);
		}
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.lsgapp, menu);
	    return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    case R.id.help:
	    	Intent help = new Intent(this, HelpAbout.class);
	    	help.putExtra(Functions.helpabout, Functions.help);
	    	startActivity(help);
	        return true;
	    case R.id.about:
	    	Intent about = new Intent(this, HelpAbout.class);
	    	about.putExtra(Functions.helpabout, Functions.about);
	    	startActivity(about);
	    	return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}
}