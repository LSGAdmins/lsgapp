package com.lsg.app;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

public class VertretungCursor extends CursorAdapter {
	static class Standard {
		public TextView date;
		public TextView klasse;
		public TextView title;
		public TextView type;
		public TextView when;
		public TextView vtext;
		public TextView bottom;
	}
	public VertretungCursor(Context context, Cursor c) {
		super(context, c);
		}
	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		LayoutInflater inflater =  (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View rowView = inflater.inflate(R.layout.standard, null, true);
			Standard holder = new Standard();
			holder.date = (TextView) rowView.findViewById(R.id.vertretung_date);
			holder.klasse = (TextView) rowView.findViewById(R.id.vertretung_class);
			holder.title = (TextView) rowView.findViewById(R.id.vertretung_title);
			holder.type = (TextView) rowView.findViewById(R.id.vertretung_type);
			holder.when = (TextView) rowView.findViewById(R.id.vertretung_when);
			holder.vtext = (TextView) rowView.findViewById(R.id.vertretung_text);
			holder.bottom = (TextView) rowView.findViewById(R.id.vertretung_bottom);
			rowView.setTag(holder);
			return rowView;
			}
	
	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		Standard holder = (Standard) view.getTag();
		
		String olddate  = "";
		String oldclass = "";
		if(cursor.getPosition() > 0) {
			int position = cursor.getPosition();
			cursor.moveToPosition(position-1);
			olddate  = cursor.getString(cursor.getColumnIndex(Functions.DB_DATE));
			oldclass = cursor.getString(cursor.getColumnIndex(Functions.DB_KLASSENSTUFE));
			cursor.moveToPosition(position);
		}
		
		String date = cursor.getString(cursor.getColumnIndex(Functions.DB_DATE));
		if(date.equals(olddate))
			holder.date.setVisibility(View.GONE);
		else {
			holder.date.setVisibility(View.VISIBLE);
			holder.date.setText(date);
			oldclass = "";
		}

		String klassä = cursor.getString(cursor.getColumnIndex(Functions.DB_KLASSENSTUFE));
		if(klassä.equals(oldclass))
			holder.klasse.setVisibility(View.GONE);
		else {
			holder.klasse.setVisibility(View.VISIBLE);
			if(new Integer(klassä) < 14)
				holder.klasse.setText(klassä + ". " + context.getString(R.string.classes));
			else
				holder.klasse.setText(context.getString(R.string.no_classes));
		}
		
		String klasse = cursor.getString(cursor.getColumnIndex(Functions.DB_KLASSE));
		if(klasse.equals("null")) {
			klasse = context.getString(R.string.no_class);
		}
		
		String fach = cursor.getString(cursor.getColumnIndex(Functions.DB_FACH));
		holder.title.setText(klasse + " (" + fach + ")");
		String type = cursor.getString(cursor.getColumnIndex(Functions.DB_ART));
		holder.type.setText(type);
		
		/*Log.d("type", "'" + type +"'");
		if(type.equals("Entfall"))
			holder.type.setTextColor(R.color.darkorange);*/
		
		String when = cursor.getString(cursor.getColumnIndex(Functions.DB_STUNDE));
		holder.when.setText(when + ". " +context.getString(R.string.hour));
		String vtext = cursor.getString(cursor.getColumnIndex(Functions.DB_VERTRETUNGSTEXT));
		if(vtext.equals("null"))
			holder.vtext.setVisibility(View.GONE);
		else {
			/*
			 * android recycles Views
			 * -> it happens, that a view with invisible vtext is reused
			 * -> make visible in ANY CASE */
			holder.vtext.setVisibility(View.VISIBLE);
			holder.vtext.setText("[" + vtext + "]");
		}
		
		String lehrer    = cursor.getString(cursor.getColumnIndex(Functions.DB_LEHRER));
		if(cursor.getString(cursor.getColumnIndex(Functions.DB_ART)).equals("Entfall")){
			holder.bottom.setText(context.getString(R.string.at) + " " + lehrer);
		} else {
			String vertreter = cursor.getString(cursor.getColumnIndex(Functions.DB_VERTRETER));
			String raum = cursor.getString(cursor.getColumnIndex(Functions.DB_RAUM));
			holder.bottom.setText(lehrer + " → " + vertreter + '\n' + context.getString(R.string.room) + " " + raum);
		}
		}
}
