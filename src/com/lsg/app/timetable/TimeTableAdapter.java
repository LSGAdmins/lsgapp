package com.lsg.app.timetable;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lsg.app.Functions;
import com.lsg.app.R;
import com.lsg.app.sqlite.LSGSQliteOpenHelper;

public class TimeTableAdapter extends CursorAdapter {

	class TimetableItem {
		public LinearLayout lay;
		public TextView break_surveillance;
		public TextView timetable_affection_what;
		public TextView timetable_hour;
		public TextView header;
		public TextView subtitle;
		public TextView timetable_room;
	}

	public TimeTableAdapter(Context context, Cursor c) {
		super(context, c, false);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.timetable_item, null, true);
		TimetableItem holder = new TimetableItem();
		holder.lay = (LinearLayout) rowView.findViewById(R.id.timetable_lay);
		holder.break_surveillance = (TextView) rowView
				.findViewById(R.id.break_surveillance);
		holder.timetable_affection_what = (TextView) rowView
				.findViewById(R.id.timetable_affection_what);
		holder.timetable_hour = (TextView) rowView
				.findViewById(R.id.timetable_hour);
		holder.header = (TextView) rowView.findViewById(R.id.timetable_subject);
		holder.subtitle = (TextView) rowView
				.findViewById(R.id.timetable_teacher);
		holder.timetable_room = (TextView) rowView
				.findViewById(R.id.timetable_room);
		if (Functions.getSDK() < 11)
			holder.timetable_hour
					.setBackgroundResource(R.layout.divider_gradient);
		rowView.setTag(holder);
		return rowView;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		TimetableItem holder = (TimetableItem) view.getTag();
		int hour = cursor.getInt(cursor
				.getColumnIndex(LSGSQliteOpenHelper.DB_HOUR)) + 1;
		String when = Integer.valueOf(hour).toString();
		int i = 1;
		int length = cursor.getInt(cursor
				.getColumnIndex(LSGSQliteOpenHelper.DB_LENGTH));
		while (i < length) {
			when += ", " + Integer.valueOf(hour + i).toString();
			i++;
		}
		if (cursor.getString(cursor
				.getColumnIndex(LSGSQliteOpenHelper.DB_VERTRETUNG)) != null
				&& cursor
						.getString(
								cursor.getColumnIndex(LSGSQliteOpenHelper.DB_VERTRETUNG))
						.equals("true")) {
			holder.lay.setBackgroundResource(R.layout.background_info);

			holder.timetable_affection_what.setVisibility(View.VISIBLE);
			int type = cursor.getInt(cursor.getColumnIndex(LSGSQliteOpenHelper.DB_MATCHING_TYPE));
			if (type == 0)
				holder.timetable_affection_what.setBackgroundColor(context
						.getResources().getColor(R.color.lightgreen));
			else if (type == 1)
				holder.timetable_affection_what.setBackgroundColor(context
						.getResources().getColor(R.color.lightorange));
			else if (type == 2)
				holder.timetable_affection_what.setBackgroundColor(context
						.getResources().getColor(R.color.lightred));
			holder.timetable_affection_what.setText(cursor.getString(cursor.getColumnIndex(LSGSQliteOpenHelper.DB_MATCHING_TYPE_RAW)));
		} else {
			holder.lay.setBackgroundResource(R.layout.background);
			holder.timetable_affection_what.setVisibility(View.GONE);
		}
		
		
		
		holder.timetable_hour.setText(when + ". "
				+ context.getString(R.string.hour));
		holder.timetable_room.setText(context.getString(R.string.room)
				+ " "
				+ cursor.getString(cursor
						.getColumnIndex(LSGSQliteOpenHelper.DB_ROOM)));
		String subtitle;
		if (cursor.getColumnIndex(LSGSQliteOpenHelper.DB_BREAK_SURVEILLANCE) != -1) {
			if (!cursor
					.getString(
							cursor.getColumnIndex(LSGSQliteOpenHelper.DB_BREAK_SURVEILLANCE))
					.equals("null")) {
				holder.break_surveillance.setVisibility(View.VISIBLE);
				holder.break_surveillance
						.setText(context.getString(R.string.break_surveillance)
								+ " "
								+ cursor.getString(cursor
										.getColumnIndex(LSGSQliteOpenHelper.DB_BREAK_SURVEILLANCE)));
			} else
				holder.break_surveillance.setVisibility(View.GONE);
			subtitle = LSGSQliteOpenHelper.DB_CLASS;
		} else {
			holder.break_surveillance.setVisibility(View.GONE);
			subtitle = LSGSQliteOpenHelper.DB_LEHRER;
		}
		if (cursor
				.getString(cursor.getColumnIndex(LSGSQliteOpenHelper.DB_ROOM))
				.equals("null"))
			holder.timetable_room.setVisibility(View.GONE);
		else
			holder.timetable_room.setVisibility(View.VISIBLE);
		holder.header.setText(cursor.getString(cursor
				.getColumnIndex(LSGSQliteOpenHelper.DB_FACH)));
		holder.subtitle.setText(cursor.getString(cursor
				.getColumnIndex(subtitle)));
		if (holder.subtitle.getText().equals("null"))
			holder.subtitle.setVisibility(View.GONE);
		else
			holder.subtitle.setVisibility(View.VISIBLE);
	}
}