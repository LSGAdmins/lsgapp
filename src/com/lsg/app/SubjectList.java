package com.lsg.app;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class SubjectList extends ListActivity implements SQLlist{
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Functions.setTheme(false, true, this);
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.subjectlist, menu);
	    if(Functions.getSDK() >= 11) {
	    	AdvancedWrapper ahelp = new AdvancedWrapper();
	    	ahelp.searchBar(menu, this);
	    }
	    else
	    	menu.removeItem(R.id.search);
	    menu.removeItem(R.id.settings);
	    return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	    case R.id.refresh:
	    	updateSubjects();
	    	return true;
        case android.R.id.home:
            // app icon in action bar clicked; go home
            Intent intent = new Intent(this, VPlan.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}
	public void updateList() {
		
	}
	public void updateWhereCond(String cond) {
		
	}
	public void updateSubjects() {
		
	}
}
