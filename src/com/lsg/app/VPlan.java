package com.lsg.app;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.EditText;
import android.widget.TextView;

public class VPlan extends ListActivity implements TextWatcher, SQLlist  {
	private String[] where_conds = new String[4];
	private ProgressDialog loading;
	private SQLiteDatabase myDB;
	private Cursor c;
	public VertretungCursor vcursor;
	private boolean mine = true;
	private boolean update_locked = false;
	private String exclude_cond;
	private String include_cond;

	final Handler handler = new Handler() {
        public void handleMessage(Message msg) {
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
		
		setContentView(R.layout.list);
		
		getWindow().setBackgroundDrawableResource(R.layout.background);
		
		//set header search bar
		if(Functions.getSDK() < 11) {
			LayoutInflater inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
			View search = inflater.inflate(R.layout.search, null);
			EditText searchEdit = (EditText) search.findViewById(R.id.search_edit);
			searchEdit.addTextChangedListener(this);
			getListView().addHeaderView(search);
		}
        
		if(Functions.getSDK() >= 11) {
			try {
				AdvancedWrapper actHelper = new AdvancedWrapper();
				actHelper.dropDownNav(this);
			} catch (Exception e) { Log.d("error", e.getMessage()); }
			
		}
		
		myDB = this.openOrCreateDatabase(Functions.DB_NAME, MODE_PRIVATE, null);
		updateCondLists();
		
		SQLiteStatement num_rows = myDB.compileStatement("SELECT COUNT(*) FROM " + Functions.DB_TABLE);
		long count = num_rows.simpleQueryForLong();
		if(count == 0)
			updateVP();
		
		vcursor = new VertretungCursor(this, c);
		getListView().setAdapter(vcursor);
		updateCursor(mine);
        Functions.styleListView(getListView(), this);
        registerForContextMenu(getListView());
        
        //info if listview empty
        getListView().setEmptyView(findViewById(R.id.list_view_empty));
        TextView textv = (TextView) findViewById(R.id.list_view_empty);
        textv.setText(R.string.vplan_empty);
	}
	public void updateCondLists() {
		exclude_cond = new String();
		Cursor exclude = myDB.query(Functions.EXCLUDE_TABLE, new String[] {Functions.DB_FACH, Functions.DB_NEEDS_SYNC},
				null, null, null, null, null);
		exclude.moveToFirst();
		int i = 0;
		while(i < exclude.getCount()) {
			String fach = exclude.getString(exclude.getColumnIndex(Functions.DB_FACH));
			exclude_cond += " AND " + Functions.DB_FACH + " != '" + fach + "' ";
			exclude.moveToNext();
			i++;
		}
		exclude.close();
		include_cond = new String();
		Cursor include = myDB.query(Functions.INCLUDE_TABLE, new String[] {Functions.DB_FACH, Functions.DB_NEEDS_SYNC},
				null, null, null, null, null);
		include.moveToFirst();
		i = 0;
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		String connector = "";
		while(i < include.getCount()) {
			String fach = include.getString(include.getColumnIndex(Functions.DB_FACH));
			include_cond += connector + Functions.DB_FACH + " LIKE '%" + fach + "%' ";
			connector = "OR";
			include.moveToNext();
			i++;
		}
		include.close();
		if(include_cond.length() == 0)
			include_cond = " 0 ";
		if(prefs.getBoolean("showonlywhitelist", false))
			include_cond = "AND (" + include_cond + " ) OR ( " + include_cond + " )";
		else
			include_cond = " OR ( " + include_cond + " )";
	}
	public void updateCursor(boolean mine) {
		this.mine = mine;
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		if(mine) {
			String klasse = prefs.getString("class", "");
			where_conds[0] =  "%" + klasse + "%";
		}
		else
			where_conds[0] = "%";
		String where_cond = "( " + Functions.DB_KLASSE + " LIKE ? ";
		if(mine)
			where_cond += include_cond;
		if(prefs.getBoolean("showwithoutclass", true))
			where_cond += "OR " + Functions.DB_KLASSE + " LIKE 'null'";
		where_cond += " OR " + Functions.DB_KLASSE + " LIKE 'infotext') AND ( " + Functions.DB_KLASSE
				+ " LIKE ? OR " + Functions.DB_FACH + " LIKE ? OR " + Functions.DB_LEHRER + " LIKE ? )";
		if(mine)
			where_cond += exclude_cond;
		c = myDB.query(Functions.DB_TABLE, new String [] {Functions.DB_ROWID, Functions.DB_KLASSE, Functions.DB_ART, Functions.DB_STUNDE,
				Functions.DB_LEHRER, Functions.DB_FACH, Functions.DB_VERTRETUNGSTEXT, Functions.DB_VERTRETER, Functions.DB_RAUM,
				Functions.DB_KLASSENSTUFE, Functions.DB_DATE}, where_cond,
				where_conds, null, null, null);
		vcursor.changeCursor(c);
	}
	public void updateWhereCond(String searchText) {
		where_conds[1] = "%" + searchText + "%";
		where_conds[2] = "%" + searchText + "%";
		where_conds[3] = "%" + searchText + "%";
		updateCursor(mine);
	}
	public void updateList() {
		updateCursor(this.mine);
	}
	@Override
	public void onResume() {
		updateCursor(mine);
		super.onResume();
	}
	public void updateVP() {
		if(!update_locked) {
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
	    if(Functions.getSDK() >= 11) {
	    	AdvancedWrapper ahelp = new AdvancedWrapper();
	    	ahelp.searchBar(menu, this);
	    }
	    else
	    	menu.removeItem(R.id.search);
	    return true;
	}
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.removeItem(R.id.all);
		menu.removeItem(R.id.mine);
	    if(Functions.getSDK() < 11) {
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
	    	Intent settings;
	    	if(Functions.getSDK() >= 11)
	    		settings = new Intent(this, SettingsAdvanced.class);
	    	else
	    		settings = new Intent(this, Settings.class);
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
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
		Cursor cur = myDB.query(Functions.DB_TABLE, new String[] {Functions.DB_KLASSE, Functions.DB_FACH, Functions.DB_RAW_FACH}, Functions.DB_ROWID + " = ?",
				new String[] {new Long(info.id).toString()}, null, null, null);
		cur.moveToFirst();
		
		String klasse  = cur.getString(cur.getColumnIndex(Functions.DB_KLASSE));
		String rawfach = cur.getString(cur.getColumnIndex(Functions.DB_RAW_FACH));
		String fach    = cur.getString(cur.getColumnIndex(Functions.DB_FACH));
		cur.close();
		
		int conmenu = 0;
		int i = 0;
		while(i < Functions.exclude.length) {
			if(klasse.contains(Functions.exclude[i]))
				conmenu = 1;
			i++;
			}
		if(klasse.equals("null"))
			conmenu = 1;
		
		Cursor exclude = myDB.query(Functions.EXCLUDE_TABLE, new String[] {Functions.DB_FACH}, Functions.DB_FACH + " LIKE ?",
				new String[] {rawfach}, null, null, null);
		if(exclude.getCount() > 0)
			conmenu = 2;

		Cursor include = myDB.query(Functions.INCLUDE_TABLE, new String[] {Functions.DB_FACH}, Functions.DB_FACH + " LIKE ?",
				new String[] {rawfach}, null, null, null);
		if(include.getCount() > 0)
			conmenu = 3;
		
		if(conmenu == 1) {
			menu.setHeaderTitle(fach + " (" + rawfach + ")");
			menu.add(Menu.NONE, 0, 0, this.getString(R.string.excludesubject));
			menu.add(Menu.NONE, 1, 0, this.getString(R.string.includesubject));
		}
		else if(conmenu == 2) {
			menu.setHeaderTitle(fach + " (" + rawfach + ")");
			menu.add(Menu.NONE, 2, 0, this.getString(R.string.no_excludesubject));
		}
		else if(conmenu == 3) {
			menu.setHeaderTitle(fach + " (" + rawfach + ")");
			menu.add(Menu.NONE, 3, 0, this.getString(R.string.no_includesubject));
		}
	}
	@Override
	public boolean onContextItemSelected(final MenuItem item) {
	  AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
	  
	  Cursor cur = myDB.query(Functions.DB_TABLE, new String[] {Functions.DB_KLASSE, Functions.DB_FACH, Functions.DB_RAW_FACH}, Functions.DB_ROWID + " = ?",
			  new String[] {new Long(info.id).toString()}, null, null, null);
	  cur.moveToFirst();
	  
	  //final String klasse  = cur.getString(cur.getColumnIndex(Functions.DB_KLASSE));
	  final String rawfach = cur.getString(cur.getColumnIndex(Functions.DB_RAW_FACH));
	  final String fach    = cur.getString(cur.getColumnIndex(Functions.DB_FACH));
	  cur.close();
	  final String prompt;
	  final String table;
		
	  //final CharSequence title = ((TextView) info.targetView.findViewById(R.id.vertretung_title)).getText();
	  int menuItemIndex = item.getItemId();
	  if(menuItemIndex == 0) {
		  prompt = getString(R.string.really_exclude);
		  table  = Functions.EXCLUDE_TABLE;
	  }
	  else if(menuItemIndex == 1) {
		  prompt = getString(R.string.really_include);
		  table  = Functions.INCLUDE_TABLE;
	  }
	  else {
		  //this code shouldnt be executed, its just for the compiler not to complain :-)
		  prompt = "";
		  table = "";
	  }
	  if(menuItemIndex == 0 || menuItemIndex == 1) {
		  DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			    @Override
			    public void onClick(DialogInterface dialog, int which) {
			        switch (which){
			        case DialogInterface.BUTTON_POSITIVE:
			        	ContentValues vals = new ContentValues();
			        	vals.put(Functions.DB_FACH, fach);
			        	vals.put(Functions.DB_RAW_FACH, rawfach);
			        	vals.put(Functions.DB_NEEDS_SYNC, "true");
			        	VPlan.this.myDB.insert(table, null, vals);
			        	updateCondLists();
			        	updateList();
			            break;
			        }
			    }
			};
		  AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(prompt)
			.setPositiveButton(this.getString(R.string.yes), dialogClickListener)
			.setNegativeButton(this.getString(R.string.no), dialogClickListener).show();
	  }
	  if(menuItemIndex == 2) {
		  myDB.delete(Functions.EXCLUDE_TABLE, Functions.DB_RAW_FACH + " = ?", new String[] {rawfach});
		  updateCondLists();
		  updateList();
	  }
	  if(menuItemIndex == 3) {
		  myDB.delete(Functions.INCLUDE_TABLE, Functions.DB_RAW_FACH + " = ?", new String[] {rawfach});
		  updateCondLists();
		  updateList();
	  }
	  return true;
	}
}