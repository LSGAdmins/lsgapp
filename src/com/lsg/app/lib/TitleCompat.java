package com.lsg.app.lib;

import android.app.Activity;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.lsg.app.Functions;
import com.lsg.app.R;
import com.lsg.app.interfaces.SelectedCallback;

public class TitleCompat {
	public interface HomeCall {
		public void onHomePress();
	}

	public interface RefreshCall {
		public void onRefreshPress();
	}

	private Activity activity;

	public TitleCompat(Activity act, boolean homeasup) {
		activity = act;
		if (Functions.getSDK() < 11) {
			act.requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
			act.getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
					R.layout.window_title);
		} else if (homeasup) {
			AdvancedWrapper adv = new AdvancedWrapper();
			adv.homeasup(act);
		}
	}

	public void init(final HomeCall homecall) {
		if (Functions.getSDK() < 11) {
			activity.getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
					R.layout.window_title);
			(activity.findViewById(R.id.logo))
					.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							homecall.onHomePress();
						}
					});
			(activity.findViewById(R.id.search_item)).setVisibility(View.GONE);
			(activity.findViewById(R.id.titlebar_spinner))
					.setVisibility(View.GONE);
		}
	}

	public void setTitle(CharSequence title) {
		setTitle((String) title);
	}

	public void setTitle(int title) {
		setTitle(activity.getString(title));
	}

	public void setTitle(String title) {
		activity.setTitle(title);
		if (Functions.getSDK() < 11)
			((TextView) activity.findViewById(R.id.title)).setText(title);
	}

	public void addRefresh(final RefreshCall sc) {
		if (Functions.getSDK() < 11) {
			(activity.findViewById(R.id.search_item))
					.setVisibility(View.VISIBLE);
			(activity.findViewById(R.id.search_item))
					.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							sc.onRefreshPress();
						}
					});
		}
	}

	public void addSpinnerNavigation(final SelectedCallback callback, int listId) {
		if (Functions.getSDK() < 11) {
			(activity.findViewById(R.id.title)).setVisibility(View.GONE);
			Spinner spinner = (Spinner) activity
					.findViewById(R.id.titlebar_spinner);
			spinner.setVisibility(View.VISIBLE);
			int spinnerType = android.R.layout.simple_spinner_dropdown_item;
			SpinnerAdapter mSpinnerAdapter = ArrayAdapter.createFromResource(
					activity, listId, spinnerType);
			spinner.setAdapter(mSpinnerAdapter);
			spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> parent, View view,
						int pos, long id) {
					callback.selected(pos, id);
				}

				@Override
				public void onNothingSelected(AdapterView<?> parent) {
				}
			});
		} else {
			AdvancedWrapper adv = new AdvancedWrapper();
			adv.dropDownNav(activity, R.array.timetable_actions, callback, 0);
		}
	}
	public void removeSpinnerNavigation() {
		// TODO test on pre-honeycomb
		if(Functions.getSDK() < 11) {
			(activity.findViewById(R.id.title)).setVisibility(View.VISIBLE);
			Spinner spinner = (Spinner) activity
					.findViewById(R.id.titlebar_spinner);
			spinner.setVisibility(View.GONE);
		} else {
			AdvancedWrapper adv = new AdvancedWrapper();
			adv.standardNavigation(activity);
		}
	}
}
