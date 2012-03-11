package com.lsg.app;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class SubjectList extends ListActivity implements SQLlist, TextWatcher {
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
		
		myDB = openOrCreateDatabase(Functions.DB_NAME, MODE_PRIVATE, null);
		
		updateCursor();
		adap = new SimpleCursorAdapter(this, R.layout.main_listitem, c, new String[] {Functions.DB_FACH},
				new int[] {R.id.main_textview});
		setListAdapter(adap);
		
		registerForContextMenu(getListView());
		
		//info if listview empty
        getListView().setEmptyView(findViewById(R.id.list_view_empty));
        TextView textv = (TextView) findViewById(R.id.list_view_empty);
        textv.setText(R.string.subjectlist_empty);
        
        where_cond = Functions.DB_FACH + " LIKE ?";
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
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		Functions.createContextMenu(menu, v, menuInfo, this, Functions.DB_SUBJECT_TABLE);
	}
	@Override
	public boolean onContextItemSelected(final MenuItem item) {
		return Functions.contextMenuSelect(item, this, this, Functions.DB_SUBJECT_TABLE);
	}
	public void updateCursor() {
		c = myDB.query(Functions.DB_SUBJECT_TABLE, new String [] {Functions.DB_ROWID, Functions.DB_FACH}, where_cond, where_conds, null, null, null);
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
	public void onDestroy() {
		super.onDestroy();
		myDB.close();
	}
}
