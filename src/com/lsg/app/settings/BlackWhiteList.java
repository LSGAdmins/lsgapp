package com.lsg.app.settings;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

import com.lsg.app.Functions;
import com.lsg.app.R;

public class BlackWhiteList extends FragmentActivity {
//	private SQLiteDatabase myDB;
//	private String table;
//	private SimpleCursorAdapter adap;
//	private Cursor c;
	//TODO test if this works on pre-honeycomb
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_main);

		FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

		Bundle data = getIntent().getExtras();
		String type = data.getString(Functions.BLACKWHITELIST);
		
		Bundle arguments = new Bundle();
		arguments.putString("list", type);
		BlackWhiteListFragment blackWhiteList = new BlackWhiteListFragment();
		blackWhiteList.setArguments(arguments);
		
		fragmentTransaction.add(R.id.fragmentContainer, blackWhiteList);
		fragmentTransaction.commit();
//		
//		Functions.setTheme(false, true, this);
//		Functions.styleListView(getListView(), this);
//		getWindow().setBackgroundDrawableResource(R.layout.background);
//		
//		myDB = LSGApplication.getSqliteDatabase();
//		
//		Bundle data = getIntent().getExtras();
//		String type = data.getString(Functions.BLACKWHITELIST);
//		
//		String wherecond = "";
//		if(type.equals(Functions.BLACKLIST)) {
//			setTitle(getString(R.string.blacklist));
//			table = new String(LSGSQliteOpenHelper.DB_EXCLUDE_TABLE);
//			wherecond = LSGSQliteOpenHelper.DB_TYPE + "='oldstyle'";
//		}
//		else {
//			setTitle(getString(R.string.whitelist));
//			table = new String(LSGSQliteOpenHelper.INCLUDE_TABLE);
//		}
//		
//		c = myDB.query(table, new String[] {LSGSQliteOpenHelper.DB_ROWID, LSGSQliteOpenHelper.DB_FACH},
//				wherecond, null, null, null, null);
//		
//		adap = new SimpleCursorAdapter(this, R.layout.main_listitem, c, new String[] {LSGSQliteOpenHelper.DB_FACH},
//				new int[] {R.id.main_textview}, 0);
//		setListAdapter(adap);
//		
//		registerForContextMenu(getListView());
//		
//		//info if listview empty
//        getListView().setEmptyView(findViewById(R.id.list_view_empty));
//        TextView textv = (TextView) findViewById(R.id.list_view_empty);
//        if(table.equals(LSGSQliteOpenHelper.DB_EXCLUDE_TABLE))
//        	textv.setText(R.string.exclude_empty);
//        else
//        	textv.setText(R.string.include_empty);
	}
//	public void updateList() {
//		c = myDB.query(table, new String[] {LSGSQliteOpenHelper.DB_ROWID, LSGSQliteOpenHelper.DB_FACH},
//				null, null, null, null, null);
//		adap.changeCursor(c);
//	}
//	@Override
//	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
//		super.onCreateContextMenu(menu, v, menuInfo);
//		AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
//		TextView title_text_view = (TextView) info.targetView.findViewById(R.id.main_textview);
//		String title = new StringBuffer(title_text_view.getText()).toString();
//		menu.setHeaderTitle(title);
//		menu.add(0, 0, Menu.NONE, R.string.list_remove);
//	}
//	@Override
//	public boolean onContextItemSelected(final MenuItem item) {
//		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
//		long _id          = info.id;
//		int menuItemIndex = item.getItemId();
//		if(menuItemIndex == 0) {
//			myDB.delete(table, LSGSQliteOpenHelper.DB_ROWID + " = ?", new String[] {Long.valueOf(_id).toString()});
//			updateList();
//		}
//		return true;
//	}
//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//	    // Handle item selection
//	    switch (item.getItemId()) {
//        case android.R.id.home:
//            // app icon in action bar clicked; go home
//            Intent intent = new Intent(this, Settings.class);
//            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//            startActivity(intent);
//            return true;
//	    default:
//	        return super.onOptionsItemSelected(item);
//	    }
//	}
//	@Override
//	public void onDestroy() {
//		super.onDestroy();
//		c.close();
//	}
}
