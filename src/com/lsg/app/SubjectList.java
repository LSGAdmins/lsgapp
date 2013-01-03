package com.lsg.app;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ListActivity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.SimpleCursorAdapter;
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
import android.widget.EditText;
import android.widget.TextView;

import com.lsg.app.interfaces.SQLlist;
import com.lsg.app.lib.AdvancedWrapper;
import com.lsg.app.lib.LSGApplication;
import com.lsg.app.sqlite.LSGSQliteOpenHelper;

public class SubjectList extends ListActivity implements SQLlist, TextWatcher {
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
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
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
		
		myDB = LSGApplication.getSqliteDatabase();
		
		updateCursor();
		adap = new SimpleCursorAdapter(this, R.layout.main_listitem, c, new String[] {LSGSQliteOpenHelper.DB_FACH},
				new int[] {R.id.main_textview}, 0);
		setListAdapter(adap);
		
		registerForContextMenu(getListView());
		
		//info if listview empty
        getListView().setEmptyView(findViewById(R.id.list_view_empty));
        TextView textv = (TextView) findViewById(R.id.list_view_empty);
        textv.setText(R.string.subjectlist_empty);
        
        where_cond = LSGSQliteOpenHelper.DB_FACH + " LIKE ?";
        where_conds = new String[1];
        where_conds[0] = "%";
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
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("fragment", VPlan.class);
            startActivity(intent);
            return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		Functions.createContextMenu(menu, v, menuInfo, this, LSGSQliteOpenHelper.DB_SUBJECT_TABLE);
	}
	@Override
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
}
