package com.lsg.app.settings;

import java.util.List;

import android.annotation.TargetApi;
import android.app.ListFragment;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
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
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.lsg.app.Functions;
import com.lsg.app.R;
import com.lsg.app.lib.LSGApplication;
import com.lsg.app.lib.SlideMenu;
import com.lsg.app.sqlite.LSGSQliteOpenHelper;

@TargetApi(11)
public class SettingsAdvanced extends PreferenceActivity {
	private SlideMenu slidemenu;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Functions.homeUp(this);
	}

	@Override
	protected void onResume() {
		slidemenu = new SlideMenu(this, SettingsAdvanced.class);
		super.onResume();
	}

	@Override
	public void onBuildHeaders(List<Header> target) {
		loadHeadersFromResource(R.xml.setting_headers, target);
	}

	public static class LoginFragment extends PreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.login_settings);
		}
	}

	public static class VPlanFragment extends PreferenceFragment implements
			OnSharedPreferenceChangeListener {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			if (Functions.getSDK() < 14)
				addPreferencesFromResource(R.xml.vplan_settings);
			else
				addPreferencesFromResource(R.xml.advanced_vplan_settings);

			SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(getActivity());
			int i = 0;
			boolean showonlywhitelist = false;
			while (i < Functions.exclude.length) {
				if (Functions.exclude[i].equals(prefs.getString(
						Functions.class_key, "")))
					showonlywhitelist = true;
				i++;
			}
			PreferenceCategory prefCat = (PreferenceCategory) findPreference(getString(R.string.vplan));
			if (!showonlywhitelist) {
				Preference onlywhitelist = (Preference) findPreference("showonlywhitelist");
				prefCat.removePreference(onlywhitelist);
			}
			prefs.registerOnSharedPreferenceChangeListener(this);
			push(!(prefs.getBoolean("autopullvplan", false) || prefs
					.getBoolean("updatevplanonstart", false)));
			pull(!prefs.getBoolean("useac2dm", false));
			if (prefs.getBoolean("disableAC2DM", false)) {
				Preference ac2dm = (Preference) findPreference("useac2dm");
				prefCat.removePreference(ac2dm);
			}
		}

		@Override
		public void onSharedPreferenceChanged(SharedPreferences prefs,
				String key) {
			if (key.equals("useac2dm")) {
				boolean useac2dm = prefs.getBoolean("useac2dm", false);
				pull(!useac2dm);
				if (useac2dm)
					Functions.registerGCM(getActivity());
				else
					Functions.unregisterGCM(getActivity());
			}
			if ((key.equals("updatevplanonstart") || key
					.equals("autopullvplan"))
					&& !prefs.getBoolean("disableAC2DM", false)) {
				push(!(prefs.getBoolean("autopullvplan", false) || prefs
						.getBoolean("updatevplanonstart", false)));
			}
		}

		private void push(boolean enabled) {
			(findPreference("useac2dm")).setEnabled(enabled);
		}

		private void pull(boolean enabled) {
			((CheckBoxPreference) findPreference("updatevplanonstart"))
					.setEnabled(enabled);
			((CheckBoxPreference) findPreference("autopullvplan"))
					.setEnabled(enabled);
			((EditTextPreference) findPreference("autopull_intervall"))
					.setEnabled(enabled);
		}
	}

	public static class GeneralFragment extends PreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.general_settings);
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

			adap = new SimpleCursorAdapter(getActivity(),
					R.layout.main_listitem, c,
					new String[] { LSGSQliteOpenHelper.DB_FACH },
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
			String title = new StringBuffer(title_text_view.getText())
					.toString();
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

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case android.R.id.home:
			slidemenu.show();
			findViewById(android.R.id.content).invalidate();
			Log.d("menu", "home");
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}