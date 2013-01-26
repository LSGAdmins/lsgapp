package com.lsg.app;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.Bundle;
import android.os.Handler;
import android.os.Messenger;
import android.support.v4.app.ListFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.lsg.app.interfaces.FragmentActivityCallbacks;
import com.lsg.app.interfaces.SQLlist;
import com.lsg.app.lib.AdvancedWrapper;
import com.lsg.app.lib.LSGApplication;
import com.lsg.app.lib.TitleCompat;
import com.lsg.app.lib.TitleCompat.RefreshCall;
import com.lsg.app.sqlite.LSGSQliteOpenHelper;

public class Events extends ListFragment implements SQLlist, RefreshCall,
		TextWatcher, WorkerService.WorkerClass {
	public static class EventAdapter extends CursorAdapter implements
			SectionIndexer {
		Cursor cursor;
		Context context;

		class Standard {
			public TextView month;
			public TextView title;
			public TextView date;
			public TextView place;
		}

		private ArrayList<String[]> headers = new ArrayList<String[]>();
		private ArrayList<Integer> headerPositions = new ArrayList<Integer>();

		public EventAdapter(Context context, Cursor cursor) {
			super(context, cursor, false);
			this.cursor = cursor;
			this.context = context;
			updateHeaders();
		}

		@Override
		public void changeCursor(Cursor cursor) {
			super.changeCursor(cursor);
			this.cursor = cursor;
			updateHeaders();
		}

		public void updateHeaders() {
			for (cursor.moveToFirst(); cursor.getPosition() < cursor.getCount(); cursor
					.moveToNext()) {
				String olddate = "e.e";
				if (cursor.getPosition() > 0) {
					cursor.moveToPosition(cursor.getPosition() - 1);
					olddate = cursor.getString(cursor
							.getColumnIndex(LSGSQliteOpenHelper.DB_DATES));
					cursor.moveToNext();
				}
				String datebeginning = cursor.getString(cursor
						.getColumnIndex(LSGSQliteOpenHelper.DB_DATES));
				String[] oldmonth = olddate.split("\\.");
				String[] month = datebeginning.split("\\.");
				String[] monthsshort = context.getResources().getStringArray(
						R.array.monthsshort);
				if (!oldmonth[1].equals(month[1])) {
					Log.d("month", ((String[]) context.getResources()
							.getStringArray(R.array.monthsshort))[Integer
							.valueOf(month[1]) - 1]);
					headers.add(new String[] {
							monthsshort[Integer.valueOf(month[1]) - 1],
							Integer.valueOf(cursor.getPosition()).toString() });
					headerPositions.add(cursor.getPosition());
				}
			}
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View rowView = inflater.inflate(R.layout.events_item, null, true);
			Standard holder = new Standard();
			holder.month = (TextView) rowView.findViewById(R.id.event_month);
			holder.title = (TextView) rowView.findViewById(R.id.event_title);
			holder.date = (TextView) rowView.findViewById(R.id.event_date);
			holder.place = (TextView) rowView.findViewById(R.id.event_place);

			if (Functions.getSDK() < 11)
				holder.month.setBackgroundResource(R.layout.divider_gradient);
			rowView.setTag(holder);
			return rowView;
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			String olddate = "e.e";
			int position = cursor.getPosition();
			if (position > 0) {
				cursor.moveToPosition(position - 1);
				olddate = cursor.getString(cursor
						.getColumnIndex(LSGSQliteOpenHelper.DB_DATES));
				cursor.moveToNext();
			}
			Standard holder = (Standard) view.getTag();
			String title = cursor.getString(cursor
					.getColumnIndex(LSGSQliteOpenHelper.DB_TITLE));
			holder.title.setText(title);
			String datebeginning = cursor.getString(cursor
					.getColumnIndex(LSGSQliteOpenHelper.DB_DATES));
			String timebeginning = cursor.getString(cursor
					.getColumnIndex(LSGSQliteOpenHelper.DB_TIMES));
			String dateending = cursor.getString(cursor
					.getColumnIndex(LSGSQliteOpenHelper.DB_ENDDATES));
			String timeending = cursor.getString(cursor
					.getColumnIndex(LSGSQliteOpenHelper.DB_ENDTIMES));
			if (datebeginning.equals("null") && timebeginning.equals("null")
					&& dateending.equals("null") && timeending.equals("null"))
				holder.date.setText("Keine Zeit angegeben");
			else if (timebeginning.equals("null") && dateending.equals("null")
					&& timeending.equals("null"))
				holder.date.setText("am " + datebeginning);
			else if (timebeginning.equals("null") && timeending.equals("null"))
				holder.date.setText("vom " + datebeginning + " bis zum "
						+ dateending);
			else if (dateending.equals("null") && timeending.equals("null"))
				holder.date.setText("am " + datebeginning + " um "
						+ timebeginning);
			else if (dateending.equals("null"))
				holder.date.setText("am " + datebeginning + " von "
						+ timebeginning + " bis " + timeending);
			else
				holder.date.setText("von " + datebeginning + " "
						+ timebeginning + " bis " + dateending + " "
						+ timeending);
			String place = cursor.getString(cursor
					.getColumnIndex(LSGSQliteOpenHelper.DB_VENUE));
			if (place.equals("null")) {
				holder.place.setText("");
				holder.place.setVisibility(View.GONE);
				holder.date.setPadding(10, 0, 10, 10);
			} else {
				holder.place.setVisibility(View.VISIBLE);
				holder.place.setText("Ort: " + place);
				holder.date.setPadding(10, 0, 10, 0);
			}
			String[] oldmonth = olddate.split("\\.");
			String[] month = datebeginning.split("\\.");
			if (!oldmonth[1].equals(month[1])) {
				holder.month.setVisibility(View.VISIBLE);
				holder.month.setText(context.getResources().getStringArray(
						R.array.months)[Integer.valueOf(month[1]) - 1]
						+ " '" + month[2]);
			} else
				holder.month.setVisibility(View.GONE);
		}

		@Override
		public int getPositionForSection(int section) {
			try {
				return Integer.valueOf(headers.get(section)[1]);
			} catch (Exception e) {
				return 0;
			}
		}

		@Override
		public int getSectionForPosition(int position) {
			int prevPos = 0;
			for (int i = 0; i < headerPositions.size(); i++) {
				if (position > prevPos && position <= headerPositions.get(i))
					return i;
				prevPos = Integer.valueOf(headerPositions.get(i));
			}
			return 0;
		}

		@Override
		public Object[] getSections() {
			String[] toReturn = new String[headers.size()];
			for (int i = 0; i < headers.size(); i++) {
				toReturn[i] = headers.get(i)[0];
			}
			return toReturn;
		}
	}

	public static class EventUpdate {
		private Context context;

		EventUpdate(Context c) {
			context = c;
		}

		public String[] refreshEvents() {
			String get = Functions.getData(Functions.EVENT_URL, context, false,
					"");
			if (!get.equals("networkerror")) {
				try {
					JSONArray jArray = new JSONArray(get);
					int i = 0;
					SQLiteDatabase myDB = LSGApplication.getSqliteDatabase();
					myDB.delete(LSGSQliteOpenHelper.DB_EVENTS_TABLE, null, null); // clear
																					// termine
					while (i < jArray.length()) {
						JSONObject jObject = jArray.getJSONObject(i);
						ContentValues values = new ContentValues();
						values.put(LSGSQliteOpenHelper.DB_DATES,
								jObject.getString("dates"));
						values.put(LSGSQliteOpenHelper.DB_ENDDATES,
								jObject.getString("enddates"));
						values.put(LSGSQliteOpenHelper.DB_TIMES,
								jObject.getString("times"));
						values.put(LSGSQliteOpenHelper.DB_ENDTIMES,
								jObject.getString("endtimes"));
						values.put(LSGSQliteOpenHelper.DB_TITLE,
								jObject.getString("title"));
						values.put(LSGSQliteOpenHelper.DB_VENUE,
								jObject.getString("venue"));
						myDB.insert(LSGSQliteOpenHelper.DB_EVENTS_TABLE, null,
								values);
						i++;
					}
				} catch (JSONException e) {
					Log.d("json", e.getMessage());
					return new String[] { "json",
							context.getString(R.string.jsonerror) };
				}
			} else {
				return new String[] { "networkerror",
						context.getString(R.string.networkerror) };
			}
			return new String[] { "success", "" };
		}
	}

	private ProgressDialog loading;
	private EventAdapter evadap;
	private String where_cond = " " + LSGSQliteOpenHelper.DB_DATES
			+ " LIKE ? OR " + LSGSQliteOpenHelper.DB_ENDDATES + " LIKE ? OR "
			+ LSGSQliteOpenHelper.DB_TIMES + " LIKE ? OR "
			+ LSGSQliteOpenHelper.DB_ENDTIMES + " LIKE ? OR "
			+ LSGSQliteOpenHelper.DB_TITLE + " LIKE ? OR "
			+ LSGSQliteOpenHelper.DB_VENUE + " LIKE ? ";
	private String[] where_conds_events = new String[6];
	private Cursor events;
	private SQLiteDatabase myDB;
	private TitleCompat titlebar;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.list, null);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Functions.checkMessage(getActivity(),
				new String[] { Functions.OVERLAY_HOMEBUTTON });
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		if (Functions.getSDK() < 11) {
			View search = ((LayoutInflater) getActivity().getSystemService(
					Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.search,
					null);
			EditText searchEdit = (EditText) search
					.findViewById(R.id.search_edit);
			searchEdit.addTextChangedListener(this);
			getListView().addHeaderView(search);
		}
		super.onCreate(savedInstanceState);
		myDB = LSGApplication.getSqliteDatabase();
		updateWhereCond("%");
		events = myDB.query(LSGSQliteOpenHelper.DB_EVENTS_TABLE, new String[] {
				LSGSQliteOpenHelper.DB_ROWID, LSGSQliteOpenHelper.DB_DATES,
				LSGSQliteOpenHelper.DB_ENDDATES, LSGSQliteOpenHelper.DB_TIMES,
				LSGSQliteOpenHelper.DB_ENDTIMES, LSGSQliteOpenHelper.DB_TITLE,
				LSGSQliteOpenHelper.DB_VENUE }, where_cond, where_conds_events,
				null, null, null);
		evadap = new EventAdapter(getActivity(), events);
		// setContentView(R.layout.list);

		// set header search bar
		getListView().setAdapter(evadap);
		getListView().setEmptyView(
				getListView().findViewById(R.id.list_view_empty));
		((TextView) getActivity().findViewById(R.id.list_view_empty))
				.setText(R.string.events_empty);
		getListView().setFastScrollEnabled(true);

		SQLiteStatement num_rows = myDB
				.compileStatement("SELECT COUNT(*) FROM "
						+ LSGSQliteOpenHelper.DB_EVENTS_TABLE);
		long count = num_rows.simpleQueryForLong();
		if (count == 0)
			updateEvents();
		num_rows.close();

		titlebar = ((FragmentActivityCallbacks) getActivity()).getTitlebar();
		titlebar.addRefresh(this);
		getActivity().setTitle(R.string.events);
		titlebar.setTitle(getActivity().getTitle());
		Functions.alwaysDisplayFastScroll(getListView());
		setHasOptionsMenu(true);
		if (savedInstanceState != null)
			refreshing = savedInstanceState.getBoolean("refreshing");

		((FragmentActivityCallbacks) getActivity()).getSlideMenu().setFragment(
				Events.class);
	}

	private boolean refreshing = false;
	private MenuItem refresh;

	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.events, menu);
		if (Functions.getSDK() >= 11) {
			AdvancedWrapper ahelp = new AdvancedWrapper();
			ahelp.searchBar(menu, this);
			refresh = menu.findItem(R.id.refresh);
		} else {
			menu.removeItem(R.id.search);
			menu.removeItem(R.id.refresh);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.refresh:
			onRefreshPress();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		// TODO insert selected Event into Calendar
	}
	
	private static ServiceHandler hand;
	private boolean actionViewSet = false;
	@TargetApi(11)
	public void updateEvents() {
		refreshing = true;
		if (Functions.getSDK() >= 11) {
			try {
				AdvancedWrapper adv = new AdvancedWrapper();
				adv.setMenuActionView(refresh, new ProgressBar(
						getActivity()));
				actionViewSet = true;
			} catch (NullPointerException e) {
				loading = ProgressDialog.show(getActivity(), null,
						getString(R.string.loading_events));
			}
		} else
			loading = ProgressDialog.show(getActivity(), null, "Lade...");
		hand = new ServiceHandler(new ServiceHandler.ServiceHandlerCallback() {
			@Override
			public void onServiceError() {
				Log.d("service", "finished with error");
			}

			@Override
			public void onFinishedService() {
				Log.d("service", "finished without error");
				try {
					if (Functions.getSDK() >= 11 && actionViewSet) {
						AdvancedWrapper adv = new AdvancedWrapper();
						adv.setMenuActionView(refresh, null);
					} else
						loading.cancel();
				} catch (Exception e) {
					e.printStackTrace();
				}
				updateList();
				refreshing = false;
			}
		});
		Handler handler = hand.getHandler();

		Intent intent = new Intent(getActivity(), WorkerService.class);
		// Create a new Messenger for the communication back
		Messenger messenger = new Messenger(handler);
		intent.putExtra(WorkerService.MESSENGER, messenger);
		intent.putExtra(WorkerService.WORKER_CLASS,
				Events.class.getCanonicalName());
		intent.putExtra(WorkerService.WHAT, WorkerService.UPDATE_ALL);
		getActivity().startService(intent);
	}

	@Override
	public void updateWhereCond(String searchText) {
		where_conds_events[0] = "%" + searchText + "%";
		where_conds_events[1] = "%" + searchText + "%";
		where_conds_events[2] = "%" + searchText + "%";
		where_conds_events[3] = "%" + searchText + "%";
		where_conds_events[4] = "%" + searchText + "%";
		where_conds_events[5] = "%" + searchText + "%";
	}

	@Override
	public void updateList() {
		if (myDB.isOpen()) {
			events = myDB.query(LSGSQliteOpenHelper.DB_EVENTS_TABLE,
					new String[] { LSGSQliteOpenHelper.DB_ROWID,
							LSGSQliteOpenHelper.DB_DATES,
							LSGSQliteOpenHelper.DB_ENDDATES,
							LSGSQliteOpenHelper.DB_TIMES,
							LSGSQliteOpenHelper.DB_ENDTIMES,
							LSGSQliteOpenHelper.DB_TITLE,
							LSGSQliteOpenHelper.DB_VENUE }, where_cond,
					where_conds_events, null, null, null);
			evadap.changeCursor(events);
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (events != null)
			events.close();
	}

	@Override
	public void onRefreshPress() {
		updateEvents();
	}

	@Override
	public void afterTextChanged(Editable s) {
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		updateWhereCond(s.toString());
		updateList();
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		savedInstanceState.putBoolean("refreshing", refreshing);
	}

	@Override
	public void update(int what, Context c) {
		EventUpdate udp = new EventUpdate(c);
		switch (what) {
		case WorkerService.UPDATE_ALL:
			udp.refreshEvents();
			break;
		}
	}
}