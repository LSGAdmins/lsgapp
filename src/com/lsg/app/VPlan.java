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

public class VPlan extends ListActivity implements TextWatcher, SQLlist  {
	private String[] where_conds = new String[4];
	private ProgressDialog loading;
	private SQLiteDatabase myDB;
	private Cursor c;
	public VertretungCursor vcursor;
	private boolean mine = false;
	private boolean update_locked = false;

	final Handler handler = new Handler() {
        public void handleMessage(Message msg) {
        	Log.d("msg", new Integer(msg.arg1).toString());
        	if(msg.arg1 == 1) {
        		loading.cancel();
        		updateCursor(mine);
        		update_locked = false;
        	}
        	if(msg.arg1 == 2) {
        		loading.cancel();
        		update_locked = false;
        	}
        }
    };
    
	@Override
	public void onCreate(Bundle savedInstanceState) {
		where_conds[0] = "%";
		where_conds[1] = "%";
		where_conds[2] = "%";
		where_conds[3] = "%";
		
		super.onCreate(savedInstanceState);
		Functions.testDB(this);
        
		Functions.setTheme(false, true, this);
		
		getWindow().setBackgroundDrawableResource(R.layout.background);

		//set header search bar
		if(Build.VERSION.SDK_INT < 11) {
			LayoutInflater inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
			View search = inflater.inflate(R.layout.search, null);
			EditText searchEdit = (EditText) search.findViewById(R.id.search_edit);
			searchEdit.addTextChangedListener(this);
			getListView().addHeaderView(search);
		}
        
		if(Build.VERSION.SDK_INT >= 11) {
			Advanced.dropDownNav(this);
		}
		
		myDB = this.openOrCreateDatabase(Functions.DB_NAME, MODE_PRIVATE, null);

		vcursor = new VertretungCursor(this, c);
		getListView().setAdapter(vcursor);
		updateCursor(false);
        Functions.styleListView(getListView(), this);
	}
	public void updateCursor(boolean mine) {
		if(mine) {
			this.mine = true;
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			String klasse = prefs.getString("class", "");
			where_conds[0] =  "%" + klasse + "%";
		}
		else
			where_conds[0] = "%";
		String where_cond = "( " + Functions.DB_KLASSE + " LIKE ? OR " + Functions.DB_KLASSE + " LIKE 'null' ) AND ( " + Functions.DB_KLASSE
				+ " LIKE ? OR " + Functions.DB_FACH + " LIKE ? OR " + Functions.DB_LEHRER + " LIKE ? )";
		c = myDB.query(Functions.DB_TABLE, new String [] {Functions.DB_ROWID, Functions.DB_KLASSE, Functions.DB_ART, Functions.DB_STUNDE,
				Functions.DB_LEHRER, Functions.DB_FACH, Functions.DB_VERTRETUNGSTEXT, Functions.DB_VERTRETER, Functions.DB_RAUM,
				Functions.DB_KLASSENSTUFE, Functions.DB_DATE}, where_cond,
				where_conds, null, null, null);
		if(c.getCount() == 0)
			updateVP();
		vcursor.changeCursor(c);
	}
	public void updateWhereCond(String searchText) {
		/*search = " WHERE " + Functions.DB_KLASSE + " LIKE '%" + searchText + "%' OR " + Functions.DB_FACH + " LIKE '%" + searchText
				+ "%' OR " + Functions.DB_LEHRER + " LIKE '%"
				+ searchText + "%' ";*/
		where_conds[1] = "%" + searchText + "%";
		where_conds[2] = "%" + searchText + "%";
		where_conds[3] = "%" + searchText + "%";
		updateCursor(mine);
	}
	@Override
	public void onResume() {
		updateCursor(mine);
		super.onResume();
	}
	public void updateVP() {
		if(!update_locked) {
			Log.d("asdf", "updatevp");
			loading = ProgressDialog.show(VPlan.this, "", getString(R.string.loading_vertretungen), true);
			update_locked = true;
			class ProgressThread extends Thread {
				Handler handler;
				ProgressThread(Handler h) {
					handler = h;
					}
				public void run() {
					Looper.prepare();
					boolean update = Functions.refreshVPlan(VPlan.this, handler);
					Functions.getClass(VPlan.this);
					Message msg = handler.obtainMessage();
					msg.arg1 = 1;
					if(!update)
						msg.arg1 = 2;
					handler.sendMessage(msg);
					}
				}
			ProgressThread progress = new ProgressThread(handler);
			progress.start();
			}
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.vplan, menu);
	    if(Build.VERSION.SDK_INT >= 11) {
	    	Advanced search = new Advanced();
	    	search.searchBarInit(menu, this);
	    }
	    else
	    	menu.removeItem(R.id.search);
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
	    	updateVP();
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
		String search = s + "";
		updateWhereCond(search);
	}
	@Override
	public void onDestroy() {
		super.onDestroy();
		myDB.close();
	}
}
