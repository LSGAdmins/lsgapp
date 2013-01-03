package com.lsg.app.settings;


import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.TextView;

import com.lsg.app.R;
import com.lsg.app.lib.LSGApplication;
import com.lsg.app.sqlite.LSGSQliteOpenHelper;

public class BlackWhiteListFragment extends ListFragment {
	private SQLiteDatabase myDB;
	private String table;
	private SimpleCursorAdapter adap;
	private Cursor c;

	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		myDB = LSGApplication.getSqliteDatabase();

		Bundle data = getArguments();
		String type = data.getString("list");
		if (type.equals("blacklist")) {
			table = new String(LSGSQliteOpenHelper.DB_EXCLUDE_TABLE);
			wherecond = LSGSQliteOpenHelper.DB_TYPE + "='oldstyle'";
		} else {
			table = new String(LSGSQliteOpenHelper.INCLUDE_TABLE);
		}

		c = myDB.query(table, new String[] { LSGSQliteOpenHelper.DB_ROWID,
				LSGSQliteOpenHelper.DB_FACH }, wherecond, null, null, null,
				null);

		adap = new SimpleCursorAdapter(getActivity(), R.layout.main_listitem,
				c, new String[] { LSGSQliteOpenHelper.DB_FACH },
				new int[] { R.id.main_textview }, 0);
		setListAdapter(adap);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.list, container, false);
	}

	String wherecond = "";

	@Override
	public void onStart() {
		super.onStart();
		// info if listview empty
		TextView textv = (TextView) getActivity().findViewById(
				R.id.list_view_empty);
		if (table.equals(LSGSQliteOpenHelper.DB_EXCLUDE_TABLE))
			textv.setText(R.string.exclude_empty);
		else
			textv.setText(R.string.include_empty);
		getListView().setEmptyView(
				getActivity().findViewById(R.id.list_view_empty));
	}

	@Override
	public void onResume() {
		super.onResume();
		registerForContextMenu(getListView());
	}

	public void updateList() {
		c = myDB.query(table, new String[] { LSGSQliteOpenHelper.DB_ROWID,
				LSGSQliteOpenHelper.DB_FACH }, wherecond, null, null, null,
				null);
		Log.d("where", wherecond);
		adap.changeCursor(c);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
		TextView title_text_view = (TextView) info.targetView
				.findViewById(R.id.main_textview);
		String title = new StringBuffer(title_text_view.getText()).toString();
		menu.setHeaderTitle(title);
		menu.add(0, 0, Menu.NONE, R.string.list_remove);
	}

	@Override
	public boolean onContextItemSelected(final MenuItem item) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
				.getMenuInfo();
		long _id = info.id;
		int menuItemIndex = item.getItemId();
		if (menuItemIndex == 0) {
			myDB.delete(table, LSGSQliteOpenHelper.DB_ROWID + " = ?",
					new String[] { Long.valueOf(_id).toString() });
			updateList();
		}
		return true;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		c.close();
	}
}
