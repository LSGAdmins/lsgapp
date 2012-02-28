package com.lsg.app;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

public class VPlan extends ListActivity implements TextWatcher {
	private ProgressDialog loading;
	private SQLiteDatabase myDB;
	private Cursor c;
	public VertretungCursor vcursor;
	private boolean mine = false;
	private String search = "";

	final Handler handler = new Handler() {
        public void handleMessage(Message msg) {
        	if(msg.arg1 == 1) {
        		loading.cancel();
        		updateCursor(mine);
        	}
        }
    };
    
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Functions.testDB(this);
        
		Functions.setTheme(false, true, this);

		//set header search bar
        LayoutInflater inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
        View search = inflater.inflate(R.layout.search, null);
        EditText searchEdit = (EditText) search.findViewById(R.id.search_edit);
        searchEdit.addTextChangedListener(this);
        getListView().addHeaderView(search);
        
		if(Build.VERSION.SDK_INT >= 11) {
			Advanced adv = new Advanced();
			adv.dropDownNav(this);
		}
		
		myDB = this.openOrCreateDatabase(Functions.DB_NAME, MODE_PRIVATE, null);

		vcursor = new VertretungCursor(this, c);
		getListView().setAdapter(vcursor);
		updateCursor(false);
        Functions.styleListView(getListView(), this);
	}
	public void updateCursor(boolean mine) {
		String where_cond = search;
		if(mine) {
			this.mine = true;
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			String klasse = prefs.getString("class", "");
			if(!where_cond.contains("WHERE"))
				where_cond = " WHERE ";
			else
				where_cond += " OR ";
			where_cond += " klasse LIKE '%" + klasse + "%' OR klasse LIKE 'null' ";
			Log.d("asdf", where_cond);
		}
		else
			this.mine = false;
		if(where_cond.equals(""))
			mine = false;
		else
			mine = true;
		c = myDB.rawQuery("SELECT " + Functions.DB_ROWID + ", " + Functions.DB_KLASSE + ", " + Functions.DB_ART
				+ ", " + Functions.DB_STUNDE + ", " + Functions.DB_LEHRER + ", " + Functions.DB_FACH
				+ ", " + Functions.DB_VERTRETUNGSTEXT + ", " + Functions.DB_VERTRETER + ", " + Functions.DB_RAUM
				+ ", " + Functions.DB_KLASSENSTUFE + ", " + Functions.DB_DATE + " FROM "
        		+ Functions.DB_TABLE + where_cond + ";", null);
		//startManagingCursor(c);
		vcursor.changeCursor(c);
	}
	@Override
	public void onResume() {
		updateCursor(mine);
		super.onResume();
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.vplan, menu);
	    return true;
	}
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.removeItem(R.id.all);
		menu.removeItem(R.id.mine);
	    if(Build.VERSION.SDK_INT < 11) {
	    	if(mine)
	    		menu.add(0, R.id.all, Menu.NONE, R.string.all);
	    	else
	    		menu.add(0, R.id.mine, Menu.NONE, R.string.mine);
	    }
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
			loading = ProgressDialog.show(VPlan.this, "", getString(R.string.loading_vertretungen), true);
			class ProgressThread extends Thread {
				Handler handler;
				ProgressThread(Handler h) {
					handler = h;
				}
				public void run() {
					Looper.prepare();
			    	Functions.refreshVPlan(VPlan.this);
			    	Functions.getClass(VPlan.this);
			    	Message msg = handler.obtainMessage();
			    	msg.arg1 = 1;
			    	handler.sendMessage(msg);
				}
			}
			ProgressThread progress = new ProgressThread(handler);
			progress.start();
	    	return true;
	    case R.id.mine:
	    	updateCursor(true);
	    	return true;
	    case R.id.all:
	    	updateCursor(false);
	    	return true;
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
	public void afterTextChanged (Editable s) {
		
	}
	public void beforeTextChanged (CharSequence s, int start, int count, int after) {
		
	}
	public void onTextChanged (CharSequence s, int start, int before, int count) {
		if(s.length() > 0)
			search = " WHERE klasse LIKE '%" + s + "%' OR fach LIKE '%" + s + "%' OR lehrer LIKE '%" + s + "%' ";
		else
			search = "";
		updateCursor(mine);
	}
	@Override
	public void onDestroy() {
		super.onDestroy();
		myDB.close();
	}
}
