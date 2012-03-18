package com.lsg.app;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.CursorAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

public class VertretungAdapter extends CursorAdapter {
	static class Standard {
		public LinearLayout standard;
		public TextView date;
		public TextView klasse;
		public TextView title;
		public TextView type;
		public TextView when;
		public TextView vtext;
		public TextView bottom;
		public WebView webv;
	}
	public VertretungAdapter(Context context, Cursor c) {
		super(context, c);
		}
	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		LayoutInflater inflater =  (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View rowView = inflater.inflate(R.layout.standard, null, true);
			Standard holder = new Standard();
			holder.standard = (LinearLayout) rowView.findViewById(R.id.standard_rellayout);
			holder.date = (TextView) rowView.findViewById(R.id.vertretung_date);
			holder.klasse = (TextView) rowView.findViewById(R.id.vertretung_class);
			holder.title = (TextView) rowView.findViewById(R.id.vertretung_title);
			holder.type = (TextView) rowView.findViewById(R.id.vertretung_type);
			holder.when = (TextView) rowView.findViewById(R.id.vertretung_when);
			holder.vtext = (TextView) rowView.findViewById(R.id.vertretung_text);
			holder.bottom = (TextView) rowView.findViewById(R.id.vertretung_bottom);
			holder.webv = (WebView) rowView.findViewById(R.id.standard_webview);
			if(Functions.getSDK() < 11)
				holder.klasse.setBackgroundResource(R.layout.divider_gradient);
			rowView.setTag(holder);
			return rowView;
			}
	
	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		Standard holder = (Standard) view.getTag();
		
		String olddate  = "";
		String oldclass = "";
		int position = cursor.getPosition();
		if(position > 0) {
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

		String klassenstufe = cursor.getString(cursor.getColumnIndex(Functions.DB_KLASSENSTUFE));
		String klasse = cursor.getString(cursor.getColumnIndex(Functions.DB_KLASSE));
		if(!klasse.equals("infotext")) {
			//hide
			holder.webv.setVisibility(View.GONE);
			//show needed views
			holder.title.setVisibility(View.VISIBLE);
			holder.type.setVisibility(View.VISIBLE);
			holder.when.setVisibility(View.VISIBLE);
			holder.bottom.setVisibility(View.VISIBLE);
			
			if(klassenstufe.equals(oldclass))
				holder.klasse.setVisibility(View.GONE);
			else {
				holder.klasse.setVisibility(View.VISIBLE);
				}
			if(new Integer(klassenstufe) < 14)
				holder.klasse.setText(klassenstufe + ". " + context.getString(R.string.classes));
			else
				holder.klasse.setText(context.getString(R.string.no_classes));
			if(klasse.equals("null")) {
				klasse = context.getString(R.string.no_class);
				}
			
			String fach = cursor.getString(cursor.getColumnIndex(Functions.DB_FACH));
			holder.title.setText(klasse + " (" + fach + ")");
			String type = cursor.getString(cursor.getColumnIndex(Functions.DB_ART));
			holder.type.setText(type);
			
			Integer lesson = new Integer(cursor.getString(cursor.getColumnIndex(Functions.DB_STUNDE)));
			String when = lesson.toString();
			int i = 0;
			int length = cursor.getInt(cursor.getColumnIndex(Functions.DB_LENGTH));
			while(i < length) {
				lesson++;
				when += ", " + lesson.toString();
				i++;
			}
			when += ".";
			holder.when.setText(when + context.getString(R.string.hour));
			String vtext = cursor.getString(cursor.getColumnIndex(Functions.DB_VERTRETUNGSTEXT));
			if(vtext.equals("null"))
				holder.vtext.setVisibility(View.GONE);
			else {
				/*
				 * 			 * android recycles Views
				 * 			 * -> it happens, that a view with invisible vtext is reused
				 * 			 * -> make visible in ANY CASE */
				holder.vtext.setVisibility(View.VISIBLE);
				holder.vtext.setText("[" + vtext + "]");
				}
			String lehrer    = cursor.getString(cursor.getColumnIndex(Functions.DB_LEHRER));
			if(cursor.getString(cursor.getColumnIndex(Functions.DB_ART)).equals("Entfall")){
				holder.bottom.setText(context.getString(R.string.at) + " " + lehrer);
				} else {
				String vertreter = cursor.getString(cursor.getColumnIndex(Functions.DB_VERTRETER));
				String raum = cursor.getString(cursor.getColumnIndex(Functions.DB_RAUM));
				String raumInsert = "";
				if(!raum.equals("null"))
					raumInsert = '\n' + context.getString(R.string.room) + " " + raum;
				holder.bottom.setText(lehrer + " → " + vertreter + raumInsert);
			}
		} else {
			holder.klasse.setText(context.getString(R.string.info));
			//hide views not needed
			holder.title.setVisibility(View.GONE);
			holder.type.setVisibility(View.GONE);
			holder.when.setVisibility(View.GONE);
			holder.bottom.setVisibility(View.GONE);
			holder.vtext.setVisibility(View.GONE);
			
			//unhide needed views that could be hidden
			holder.klasse.setVisibility(View.VISIBLE);
			holder.webv.setVisibility(View.VISIBLE);
			String info = cursor.getString(cursor.getColumnIndex(Functions.DB_VERTRETUNGSTEXT));
			holder.webv.loadData(info, "text/html", null);
			//holder.vtext.setText(Html.fromHtml(cursor.getString(cursor.getColumnIndex(Functions.DB_VERTRETUNGSTEXT))));
		}
		}
}
