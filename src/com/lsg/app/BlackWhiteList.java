package com.lsg.app;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class BlackWhiteList extends ListActivity {
	private SQLiteDatabase myDB;
	private String table;
	private SimpleCursorAdapter adap;
	private Cursor c;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list);
		
		Functions.setTheme(false, true, this);
		Functions.styleListView(getListView(), this);
		getWindow().setBackgroundDrawableResource(R.layout.background);
		
		myDB = openOrCreateDatabase(Functions.DB_NAME, MODE_PRIVATE, null);
		
		Bundle data = getIntent().getExtras();
		String type = data.getString(Functions.BLACKWHITELIST);
		if(type.equals(Functions.BLACKLIST)) {
			setTitle(getString(R.string.blacklist));
			table = new String(Functions.EXCLUDE_TABLE);
		}
		else {
			setTitle(getString(R.string.whitelist));
			table = new String(Functions.INCLUDE_TABLE);
		}
		
		c = myDB.query(table, new String[] {Functions.DB_ROWID, Functions.DB_FACH},
				null, null, null, null, null);
		
		adap = new SimpleCursorAdapter(this, R.layout.main_listitem, c, new String[] {Functions.DB_FACH},
				new int[] {R.id.main_textview});
		setListAdapter(adap);
		
		registerForContextMenu(getListView());
		
		//info if listview empty
        getListView().setEmptyView(findViewById(R.id.list_view_empty));
        TextView textv = (TextView) findViewById(R.id.list_view_empty);
        if(table.equals(Functions.EXCLUDE_TABLE))
        	textv.setText(R.string.exclude_empty);
        else
        	textv.setText(R.string.include_empty);
	}
	public void updateList() {
		c = myDB.query(table, new String[] {Functions.DB_ROWID, Functions.DB_FACH},
				null, null, null, null, null);
		adap.changeCursor(c);
	}
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
		TextView title_text_view = (TextView) info.targetView.findViewById(R.id.main_textview);
		String title = new StringBuffer(title_text_view.getText()).toString();
		menu.setHeaderTitle(title);
		menu.add(0, 0, Menu.NONE, R.string.list_remove);
	}
	@Override
	public boolean onContextItemSelected(final MenuItem item) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
		long _id          = info.id;
		int menuItemIndex = item.getItemId();
		if(menuItemIndex == 0) {
			myDB.delete(table, Functions.DB_ROWID + " = ?", new String[] {new Long(_id).toString()});
			updateList();
		}
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
        case android.R.id.home:
            // app icon in action bar clicked; go home
            Intent intent = new Intent(this, Settings.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}
	@Override
	public void onDestroy() {
		super.onDestroy();
		c.close();
	}
}
