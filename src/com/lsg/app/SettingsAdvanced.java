package com.lsg.app;

import java.util.List;

import android.app.ListFragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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
        Functions.setTheme(false, true, this);
        loadHeadersFromResource(R.xml.setting_headers, target);
    }

    public static class LoginFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.login_settings);
        }
    }

    public static class VPlanFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if(Functions.getSDK() < 14)
            	addPreferencesFromResource(R.xml.vplan_settings);
            else
            	addPreferencesFromResource(R.xml.advanced_vplan_settings);
            	
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            int i = 0;
            boolean showonlywhitelist = false;
            while(i < Functions.exclude.length) {
            	if(Functions.exclude[i].equals(prefs.getString(Functions.class_key, "")))
            		showonlywhitelist = true;
            	i++;
            }
            if(!showonlywhitelist) {
            	PreferenceCategory prefCat = (PreferenceCategory) findPreference(getString(R.string.vplan));
            	CheckBoxPreference onlywhitelist = (CheckBoxPreference) findPreference("showonlywhitelist");
            	prefCat.removePreference(onlywhitelist);
            }
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
    		
    		c = myDB.query(table, new String[] {Functions.DB_ROWID, Functions.DB_FACH},
    				null, null, null, null, null);
    		
    		adap = new SimpleCursorAdapter(getActivity(), R.layout.main_listitem, c, new String[] {Functions.DB_FACH},
    				new int[] {R.id.main_textview}, 0);
    		setListAdapter(adap);
    	}
    	@Override
    	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    		return inflater.inflate(R.layout.list, container, false);
    		}
    	@Override
    	public void onStart() {
    		super.onStart();
    		//info if listview empty
    		TextView textv = (TextView) getActivity().findViewById(R.id.list_view_empty);
    		if(table.equals(Functions.EXCLUDE_TABLE))
    			textv.setText(R.string.exclude_empty);
    		else
    			textv.setText(R.string.include_empty);
            getListView().setEmptyView(getActivity().findViewById(R.id.list_view_empty));
    	}
    	@Override
    	public void onResume() {
    		super.onResume();
    		registerForContextMenu(getListView());
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
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
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
}
