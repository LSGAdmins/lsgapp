package com.lsg.app.settings;

import java.util.List;

import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.view.MenuItem;

import com.lsg.app.Functions;
import com.lsg.app.R;

@TargetApi(11)
public class SettingsAdvanced extends PreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Functions.homeUp(this);
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

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			break;
		}
		return super.onOptionsItemSelected(item);
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
}