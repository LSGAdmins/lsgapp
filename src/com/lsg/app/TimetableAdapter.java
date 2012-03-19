package com.lsg.app;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TimetableAdapter extends CursorAdapter {
	private SQLiteDatabase myDB;
	static class TimetableItem {
		public LinearLayout lay;
		public TextView timetable_day;
		public TextView timetable_hour;
		public TextView timetable_subject;
		public TextView timetable_teacher;
		public TextView timetable_room;
	}
	public TimetableAdapter(Context context, Cursor c) {
		super(context, c);
		myDB = context.openOrCreateDatabase(Functions.DB_NAME, Context.MODE_PRIVATE, null);
		}
	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		LayoutInflater inflater =  (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View rowView = inflater.inflate(R.layout.timetable_item, null, true);
			TimetableItem holder     = new TimetableItem();
			holder.lay               = (LinearLayout) rowView.findViewById(R.id.timetable_lay);
			holder.timetable_day     = (TextView) rowView.findViewById(R.id.timetable_day);
			holder.timetable_hour    = (TextView) rowView.findViewById(R.id.timetable_hour);
			holder.timetable_subject = (TextView) rowView.findViewById(R.id.timetable_subject);
			holder.timetable_teacher = (TextView) rowView.findViewById(R.id.timetable_teacher);
			holder.timetable_room    = (TextView) rowView.findViewById(R.id.timetable_room);
			if(Functions.getSDK() < 11)
				holder.timetable_hour.setBackgroundResource(R.layout.divider_gradient);
			rowView.setTag(holder);
			return rowView;
			}
	
	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		TimetableItem holder = (TimetableItem) view.getTag();
		int position = cursor.getPosition();
		if(position == 0) {
			holder.timetable_day.setVisibility(View.VISIBLE);
			holder.timetable_day.setText(context.getResources().getStringArray(R.array.days)[cursor.getInt(cursor.getColumnIndex(Functions.DB_DAY))]);
		}
		else
			holder.timetable_day.setVisibility(View.GONE);
		int hour = cursor.getInt(cursor.getColumnIndex(Functions.DB_HOUR)) + 1;
		String when = new Integer(hour).toString();
		int i = 1;
		int length = cursor.getInt(cursor.getColumnIndex(Functions.DB_LENGTH));
		while(i < length) {
			when += ", " + new Integer(hour + i).toString();
			i++;
		}
		String rawfach = cursor.getString(cursor.getColumnIndex(Functions.DB_RAW_FACH));
		String lehrer = cursor.getString(cursor.getColumnIndex(Functions.DB_LEHRER));
		Cursor c = myDB.query(Functions.DB_VPLAN_TABLE, new String[] {}, Functions.DB_STUNDE + "=? AND " + Functions.DB_RAW_FACH + "=? AND " + Functions.DB_LEHRER + "=?",
				new String[] {new Integer(hour).toString(), rawfach, lehrer}, null, null, null);
		if(c.getCount() > 0)
			holder.lay.setBackgroundResource(R.layout.background_info);
		else
			holder.lay.setBackgroundResource(R.layout.background);
		Log.d("laenge " + rawfach, new Integer(c.getCount()).toString());
		
		holder.timetable_hour.setText(when + ". " + context.getString(R.string.hour));
		holder.timetable_subject.setText(cursor.getString(cursor.getColumnIndex(Functions.DB_FACH)));
		holder.timetable_teacher.setText(cursor.getString(cursor.getColumnIndex(Functions.DB_LEHRER)));
		holder.timetable_room.setText(context.getString(R.string.room) + " " + cursor.getString(cursor.getColumnIndex(Functions.DB_RAUM)));
		}
}
