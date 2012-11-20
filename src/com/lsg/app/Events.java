package com.lsg.app;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.TargetApi;
import android.app.ListActivity;
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
import android.widget.ProgressBar;
import android.widget.TextView;

import com.lsg.app.interfaces.SQLlist;
import com.lsg.app.lib.SlideMenu;
import com.lsg.app.lib.TitleCompat;
import com.lsg.app.lib.TitleCompat.HomeCall;
import com.lsg.app.lib.TitleCompat.RefreshCall;

public class Events extends ListActivity implements SQLlist, HomeCall, RefreshCall, TextWatcher, WorkerService.WorkerClass {
	public static class EventAdapter extends CursorAdapter {
		class Standard {
			public TextView month;
			public TextView title;
			public TextView date;
			public TextView place;
		}
		public EventAdapter(Context context, Cursor d) {
			super(context, d, false);
			}
		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			LayoutInflater inflater =  (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				View rowView = inflater.inflate(R.layout.events_item, null, true);
				Standard holder = new Standard();
				holder.month = (TextView) rowView.findViewById(R.id.event_month);
				holder.title = (TextView) rowView.findViewById(R.id.event_title);
				holder.date = (TextView) rowView.findViewById(R.id.event_date);
				holder.place = (TextView) rowView.findViewById(R.id.event_place);
				

				if(Functions.getSDK() < 11)
					holder.month.setBackgroundResource(R.layout.divider_gradient);
				rowView.setTag(holder);
				return rowView;
				}
		
		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			String olddate = "e.e";
			int position = cursor.getPosition();
			if(position > 0) {
				cursor.moveToPosition(position-1);
				olddate = cursor.getString(cursor.getColumnIndex(Functions.DB_DATES));
				cursor.moveToNext();
			}
			Standard holder = (Standard) view.getTag();	
			String title = cursor.getString(cursor.getColumnIndex(Functions.DB_TITLE));
			holder.title.setText(title);
			String datebeginning = cursor.getString(cursor.getColumnIndex(Functions.DB_DATES));
			String timebeginning = cursor.getString(cursor.getColumnIndex(Functions.DB_TIMES));
			String dateending = cursor.getString(cursor.getColumnIndex(Functions.DB_ENDDATES));
			String timeending = cursor.getString(cursor.getColumnIndex(Functions.DB_ENDTIMES));
			if (datebeginning.equals("null") && timebeginning.equals("null") && dateending.equals("null") && timeending.equals("null"))
				holder.date.setText("Keine Zeit angegeben");
			else if (timebeginning.equals("null") && dateending.equals("null") && timeending.equals("null"))
				holder.date.setText("am " + datebeginning);
			else if (timebeginning.equals("null") && timeending.equals("null"))
				holder.date.setText("vom " + datebeginning + " bis zum " + dateending);
			else if (dateending.equals("null") && timeending.equals("null"))
				holder.date.setText("am " + datebeginning + " um " + timebeginning);
			else if (dateending.equals("null"))
				holder.date.setText("am " + datebeginning + " von " + timebeginning + " bis " + timeending);
			else
				holder.date.setText("von " + datebeginning + " " + timebeginning + " bis " + dateending + " " + timeending);
			String place = cursor.getString(cursor.getColumnIndex(Functions.DB_VENUE));
			if (place.equals("null")) {
				holder.place.setText("");
				holder.place.setVisibility(View.GONE);
				holder.date.setPadding(10,0,10,10);
			} else {
				holder.place.setVisibility(View.VISIBLE);
				holder.place.setText("Ort: " + place);
				holder.date.setPadding(10,0,10,0);
			}
			String[] oldmonth = olddate.split("\\.");
			String[] month    = datebeginning.split("\\.");
			if(!oldmonth[1].equals(month[1])) {
				holder.month.setVisibility(View.VISIBLE);
				holder.month.setText(context.getResources().getStringArray(R.array.months)[Integer.valueOf(month[1])-1] + " '" + month[2]);
			}
			else
				holder.month.setVisibility(View.GONE);
		}
	}
	public static class EventUpdate {
		private Context context;
		EventUpdate(Context c) {
			context = c;
		}
		public String[] refreshEvents() {
			String get = Functions.getData(Functions.EVENT_URL, context, false, "");
			if(!get.equals("networkerror")) {
				try {
					JSONArray jArray = new JSONArray(get);
					int i = 0;
					SQLiteDatabase myDB = context.openOrCreateDatabase(Functions.DB_NAME, Context.MODE_PRIVATE, null);
					myDB.delete(Functions.DB_EVENTS_TABLE, null, null); //clear termine
					while(i < jArray.length()) {
						JSONObject jObject = jArray.getJSONObject(i);
						ContentValues values = new ContentValues();
						values.put(Functions.DB_DATES, jObject.getString("dates"));
						values.put(Functions.DB_ENDDATES, jObject.getString("enddates"));
						values.put(Functions.DB_TIMES, jObject.getString("times"));
						values.put(Functions.DB_ENDTIMES, jObject.getString("endtimes"));
						values.put(Functions.DB_TITLE, jObject.getString("title"));
						values.put(Functions.DB_VENUE, jObject.getString("venue"));
						myDB.insert(Functions.DB_EVENTS_TABLE, null, values);
						i++;
						}
					myDB.close();
					} catch(JSONException e) {
						Log.d("json", e.getMessage());
						return new String[] {"json", context.getString(R.string.jsonerror)};
						}
				}
			else {
				return new String[] {"networkerror", context.getString(R.string.networkerror)};
				}
			return new String[] {"success", ""};
			}
		}
	private ProgressDialog loading;
	private EventAdapter evadap;
	private String where_cond = " " + Functions.DB_DATES + " LIKE ? OR " + Functions.DB_ENDDATES + " LIKE ? OR "
			+ Functions.DB_TIMES + " LIKE ? OR " + Functions.DB_ENDTIMES + " LIKE ? OR " + Functions.DB_TITLE + " LIKE ? OR "
			+ Functions.DB_VENUE + " LIKE ? ";
	private String[] where_conds_events = new String[6];
	private Cursor events;
	private SQLiteDatabase myDB;
	private SlideMenu slidemenu;
	private TitleCompat titlebar;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		titlebar = new TitleCompat(this, true);
		myDB = openOrCreateDatabase(Functions.DB_NAME, Context.MODE_PRIVATE, null);
		updateWhereCond("%");
		events = myDB.query(Functions.DB_EVENTS_TABLE, new String [] {Functions.DB_ROWID, Functions.DB_DATES, Functions.DB_ENDDATES,
				Functions.DB_TIMES,	Functions.DB_ENDTIMES, Functions.DB_TITLE, Functions.DB_VENUE}, where_cond,
				where_conds_events, null, null, null);
		evadap = new EventAdapter(this, events);
		setContentView(R.layout.list);
		//set header search bar
		if(Functions.getSDK() < 11) {
			LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
			View search = inflater.inflate(R.layout.search, null);
			EditText searchEdit = (EditText) search.findViewById(R.id.search_edit);
			searchEdit.addTextChangedListener(this);
			getListView().addHeaderView(search);
			}
		getListView().setAdapter(evadap);
		getListView().setEmptyView(getListView().findViewById(R.id.list_view_empty));
		((TextView) findViewById(R.id.list_view_empty)).setText(R.string.events_empty);
		
		SQLiteStatement num_rows = myDB.compileStatement("SELECT COUNT(*) FROM " + Functions.DB_EVENTS_TABLE);
		long count = num_rows.simpleQueryForLong();
		if(count == 0)
			updateEvents();
		num_rows.close();
		
		slidemenu = new SlideMenu(this, Events.class);
		slidemenu.checkEnabled();
		titlebar.init(this);
		titlebar.addRefresh(this);
		titlebar.setTitle(getTitle());
		
		Functions.checkMessage(this, new String[] {Functions.OVERLAY_HOMEBUTTON});
	}
	private boolean refreshing = false;
	private MenuItem refresh;
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.events, menu);
	    if(Functions.getSDK() >= 11) {
			AdvancedWrapper ahelp = new AdvancedWrapper();
			ahelp.searchBar(menu, this);
			refresh = menu.findItem(R.id.refresh);
		} else {
			menu.removeItem(R.id.search);
			menu.removeItem(R.id.refresh);
		}
	    return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	    case R.id.refresh:
	    	onRefreshPress();
	    	return true;
        case android.R.id.home:
        	onHomePress();
            return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}
	private static ServiceHandler hand;
	@TargetApi(11)
	public void updateEvents() {
		refreshing = true;
		final View actionView;
		View v;
		if (Functions.getSDK() >= 11) {
			try {
				v = refresh.getActionView();
				refresh.setActionView(new ProgressBar(this));
			} catch (NullPointerException e) {
				loading = ProgressDialog.show(this, null, getString(R.string.loading_events));
				v = null;
			}
			actionView = v;
		} else {
			actionView = null;
			loading = ProgressDialog.show(this, null,
					"Lade...");
		}
		hand = new ServiceHandler(new ServiceHandler.ServiceHandlerCallback() {
			@Override
			public void onServiceError() {
				// TODO Auto-generated method stub
			}

			@Override
			public void onFinishedService() {
				Log.d("service", "finished without error");
				try {
					if (Functions.getSDK() >= 11 && actionView != null)
						refresh.setActionView(actionView);
					else
						loading.cancel();
				} catch (Exception e) {
				}
				updateList();
				refreshing = false;
			}
		});
		Handler handler = hand.getHandler();
		
		Intent intent = new Intent(this, WorkerService.class);
	    // Create a new Messenger for the communication back
	    Messenger messenger = new Messenger(handler);
	    intent.putExtra(WorkerService.MESSENGER, messenger);
	    intent.putExtra(WorkerService.WORKER_CLASS, Events.class.getCanonicalName());
	    intent.putExtra(WorkerService.WHAT, WorkerService.UPDATE_ALL);
	    startService(intent);
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
		events = myDB.query(Functions.DB_EVENTS_TABLE, new String [] {Functions.DB_ROWID, Functions.DB_DATES, Functions.DB_ENDDATES,
				Functions.DB_TIMES,	Functions.DB_ENDTIMES, Functions.DB_TITLE, Functions.DB_VENUE}, where_cond,
				where_conds_events, null, null, null);
		evadap.changeCursor(events);
	}
	@Override
	public void onDestroy() {
		super.onDestroy();
		events.close();
		myDB.close();
	}

	@Override
	public void onHomePress() {
		slidemenu.show();
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
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		refreshing = savedInstanceState.getBoolean("refreshing");
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