package com.lsg.app;

import java.util.List;

import android.app.ListFragment;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class SettingsAdvanced extends PreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Functions.setTheme(false, true, this);
    }

    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.setting_headers, target);
    }

    public static class LoginFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            
            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.login_settings);
        }
    }

    public static class VPlanFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.vplan_settings);
        }
    }

    public static class BlackWhiteListFragment extends ListFragment {
    	private SQLiteDatabase myDB;
    	private String table;
    	private SimpleCursorAdapter adap;
    	private Cursor c;
    	@Override
    	public void onCreate(Bundle savedInstanceState) {
    		super.onCreate(savedInstanceState);
    		
    		myDB = getActivity().openOrCreateDatabase(Functions.DB_NAME, MODE_PRIVATE, null);
    		
    		Bundle data = getArguments();
    		String type = data.getString("list");
    		if(type.equals("blacklist")) {
    			table = new String(Functions.EXCLUDE_TABLE);
    		}
    		else {
    			table = new String(Functions.INCLUDE_TABLE);
    		}
    		
    		c = myDB.query(table, new String[] {Functions.DB_ROWID, Functions.DB_FACH, Functions.DB_NEEDS_SYNC},
    				null, null, null, null, null);
    		
    		adap = new SimpleCursorAdapter(getActivity(), R.layout.main_listitem, c, new String[] {Functions.DB_FACH},
    				new int[] {R.id.main_textview}, 0);
    		setListAdapter(adap);
    	}
    	@Override
    	public void onResume() {
    		super.onResume();
    		registerForContextMenu(getListView());
    	}
    	public void updateList() {
    		c = myDB.query(table, new String[] {Functions.DB_ROWID, Functions.DB_FACH, Functions.DB_NEEDS_SYNC},
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
    		final CharSequence title = ((TextView) info.targetView.findViewById(R.id.main_textview)).getText();
    		int menuItemIndex = item.getItemId();
    		if(menuItemIndex == 0) {
    			myDB.delete(table, Functions.DB_FACH + " LIKE ?", new String[] {(String) title});
    			updateList();
    		}
    		return true;
    	}
    	@Override
    	public boolean onOptionsItemSelected(MenuItem item) {
    	    // Handle item selection
    	    switch (item.getItemId()) {
            case android.R.id.home:
                return true;
    	    default:
    	        return super.onOptionsItemSelected(item);
    	    }
    	}
    }

}
