package com.lsg.app;


import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

public class EventAdapter extends CursorAdapter {
	static class Standard {
		public TextView month;
		public TextView title;
		public TextView date;
		public TextView place;
	}
	public EventAdapter(Context context, Cursor d) {
		super(context, d);
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
			holder.month.setText(context.getResources().getStringArray(R.array.months)[new Integer(month[1])-1] + " '" + month[2]);
		}
		else
			holder.month.setVisibility(View.GONE);
	}
}
