package com.lsg.app;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.lsg.app.interfaces.SQLlist;
import com.lsg.app.lib.LSGApplication;
import com.lsg.app.sqlite.LSGSQliteOpenHelper;


public class SubjectList extends ActionBarActivity implements SQLlist, TextWatcher, SearchView.OnQueryTextListener {
	public static class SubjectListUpdater {
		Context context;
		public SubjectListUpdater(Context c) {
			context = c;
		}
		public String[] updateSubjectList() {
			SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(context);
			String add = "";
			try {
				add = "&"
						+ URLEncoder.encode("date", "UTF-8")
						+ "="
						+ URLEncoder.encode(
								prefs.getString("subject_update_time", ""),
								"UTF-8");
			} catch (UnsupportedEncodingException e) {
				Log.d("encoding", e.getMessage());
			}
			String get = Functions.getData(Functions.SUBJECT_URL, context,
					true, add);
			if (!get.equals("networkerror") && !get.equals("loginerror")
					&& !get.equals("noupdate")) {
				try {
					JSONArray jArray = new JSONArray(get);
					int i = 0;
					SQLiteDatabase myDB = LSGApplication.getSqliteDatabase();
					myDB.delete(LSGSQliteOpenHelper.DB_SUBJECT_TABLE, null, null); // clear
																			// subjectlist
					while (i < jArray.length() - 1) {
						JSONObject jObject = jArray.getJSONObject(i);
						ContentValues values = new ContentValues();
						values.put(LSGSQliteOpenHelper.DB_RAW_FACH,
								jObject.getString("kuerzel"));
						values.put(LSGSQliteOpenHelper.DB_FACH, jObject.getString("name"));
						myDB.insert(LSGSQliteOpenHelper.DB_SUBJECT_TABLE, null, values);
						i++;
					}
					JSONObject jObject = jArray.getJSONObject(i);
					String update_time = jObject.getString("update_time");
					SharedPreferences.Editor edit = prefs.edit();
					edit.putString("subject_update_time", update_time);
					edit.commit();
				} catch (JSONException e) {
					Log.d("json", e.getMessage());
					return new String[] { "json",
							context.getString(R.string.jsonerror) };
				}
			} else if (get.equals("networkerror"))
				return new String[] { "networkerror",
						context.getString(R.string.networkerror) };
			else if (get.equals("loginerror"))
				return new String[] { "loginerror",
						context.getString(R.string.loginerror) };
			return new String[] { "success", "" };
		}
	}
	private Cursor c;
	private SimpleCursorAdapter adap;
	private SQLiteDatabase myDB;
	private String   where_cond;
	private String[] where_conds;
	private ListView listView;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list);
		
		listView = ((ListView) findViewById(android.R.id.list));
		
		myDB = LSGApplication.getSqliteDatabase();
		
		updateCursor();
		adap = new SimpleCursorAdapter(this, R.layout.main_listitem, c, new String[] {LSGSQliteOpenHelper.DB_FACH},
				new int[] {R.id.main_textview}, 0);
		listView.setAdapter(adap);
		
		registerForContextMenu(listView);
		
		//info if listview empty
        listView.setEmptyView(findViewById(R.id.list_view_empty));
        TextView textv = (TextView) findViewById(R.id.list_view_empty);
        textv.setText(R.string.subjectlist_empty);
        
        where_cond = LSGSQliteOpenHelper.DB_FACH + " LIKE ?";
        where_conds = new String[1];
        where_conds[0] = "%";
        
        // set home as up
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}
	
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.subjectlist, menu);
	    ((SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.search))).setOnQueryTextListener(this);
//	    if(Functions.getSDK() >= 11) {
//	    	AdvancedWrapper ahelp = new AdvancedWrapper();
////	    	ahelp.searchBar(menu, this);
//	    }
//	    else
//	    	menu.removeItem(R.id.search);
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
			finish();
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		Functions.createContextMenu(menu, v, menuInfo, this, LSGSQliteOpenHelper.DB_SUBJECT_TABLE);
	}
	
	public boolean onContextItemSelected(final MenuItem item) {
		return Functions.contextMenuSelect(item, this, this, LSGSQliteOpenHelper.DB_SUBJECT_TABLE);
	}
	public void updateCursor() {
		c = myDB.query(LSGSQliteOpenHelper.DB_SUBJECT_TABLE, new String [] {LSGSQliteOpenHelper.DB_ROWID, LSGSQliteOpenHelper.DB_FACH}, where_cond, where_conds, null, null, null);
	}
	public void updateList() {
		updateCursor();
		adap.changeCursor(c);
	}
	public void updateWhereCond(String cond) {
		where_conds[0] = "%" + cond + "%";
		updateList();
	}
	public void updateSubjects() {
		
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
	public boolean onQueryTextChange(String arg0) {
		String search = arg0 + "";
		updateWhereCond(search);
		return true;
	}

	@Override
	public boolean onQueryTextSubmit(String arg0) {
		// TODO Auto-generated method stub
		return false;
	}
}
