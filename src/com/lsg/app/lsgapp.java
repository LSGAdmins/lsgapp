package com.lsg.app;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class lsgapp extends ListActivity {
    /** Called when the activity is first created. Test */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Functions.setTheme(false, this);
        String[] actions = {getString(R.string.vplan), getString(R.string.events)};
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
	}
}