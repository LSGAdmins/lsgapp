package com.lsg.app.vplan;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lsg.app.Functions;
import com.lsg.app.R;
import com.lsg.app.sqlite.LSGSQliteOpenHelper;

public class VPlanAdapter extends CursorAdapter {
	private Context context;
	private boolean teacher;
	class Standard {
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
	public VPlanAdapter(Context context, Cursor c, boolean teacher) {
		super(context, c, false);
		this.context = context;
		this.teacher = teacher;
		}
	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		LayoutInflater inflater =  (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView;
		if(teacher)
			rowView = inflater.inflate(R.layout.vplan_listitem, null, true);
		else
			rowView = inflater.inflate(R.layout.vplan_listitem, null, true);
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
		String oldvertreter = "";
		int position = cursor.getPosition();
		if(position > 0) {
			cursor.moveToPosition(position-1);
			olddate  = cursor.getString(cursor.getColumnIndex(LSGSQliteOpenHelper.DB_DATE));
			oldclass = cursor.getString(cursor.getColumnIndex(LSGSQliteOpenHelper.DB_CLASS_LEVEL));
			oldvertreter = cursor.getString(cursor.getColumnIndex(LSGSQliteOpenHelper.DB_VERTRETER));
			cursor.moveToPosition(position);
			}
		
		String date = cursor.getString(cursor.getColumnIndex(LSGSQliteOpenHelper.DB_DATE));
		if(date.equals(olddate))
			holder.date.setVisibility(View.GONE);
		else {
			holder.date.setVisibility(View.VISIBLE);
			holder.date.setText(date);
			oldclass = "";
		}

		String klassenstufe = cursor.getString(cursor
				.getColumnIndex(LSGSQliteOpenHelper.DB_CLASS_LEVEL));
		String klasse = cursor.getString(cursor
				.getColumnIndex(LSGSQliteOpenHelper.DB_KLASSE));
		if (!klasse.equals("infotext")) {
			// hide
			holder.webv.setVisibility(View.GONE);
			// show needed views
			holder.title.setVisibility(View.VISIBLE);
			holder.type.setVisibility(View.VISIBLE);
			holder.when.setVisibility(View.VISIBLE);
			holder.bottom.setVisibility(View.VISIBLE);

			if (klassenstufe.equals(oldclass)
					&& (((cursor.getString(cursor.getColumnIndex("type"))
							.equals("teachers") && cursor.getString(
							cursor.getColumnIndex(LSGSQliteOpenHelper.DB_VERTRETER))
							.equals(oldvertreter)) || cursor.getString(
							cursor.getColumnIndex("type")).equals("pupils"))))
				holder.klasse.setVisibility(View.GONE);
			else {
				holder.klasse.setVisibility(View.VISIBLE);
			}
			if (Integer.valueOf(klassenstufe) < 14)
				holder.klasse.setText(klassenstufe + ". "
						+ context.getString(R.string.classes));
			else if (cursor.getString(cursor.getColumnIndex("type"))
					.equals("teachers"))
				holder.klasse.setText(context.getString(R.string.vplan_of)
						+ " "
						+ cursor.getString(cursor
								.getColumnIndex(LSGSQliteOpenHelper.DB_VERTRETER)));
			else
				holder.klasse.setText(context
						.getString(R.string.no_classes));

			String type = cursor.getString(cursor
					.getColumnIndex(LSGSQliteOpenHelper.DB_TYPE));
			holder.type.setText(type);

			holder.type.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
			holder.title.setVisibility(View.VISIBLE);
			String fach = cursor.getString(cursor.getColumnIndex(LSGSQliteOpenHelper.DB_FACH));
			if(fach.equals("null") && klasse.equals("null")) {
				holder.type.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
				holder.title.setVisibility(View.GONE);
			}
			else if (klasse.equals("null"))
				holder.title.setText(fach);
//			else if(fach.equals("null"))
//				holder.title.setText(klasse);	//hypothetic
			else
				holder.title.setText(klasse + " (" + fach + ")");
			
			Integer lesson;
			try {
			lesson = Integer.valueOf(cursor.getString(cursor.getColumnIndex(LSGSQliteOpenHelper.DB_STUNDE)));
			} catch(Exception e) {
				//old db style, do act!!!
				lesson = 0;
				// TODO update vplan
			}
			String when = lesson.toString();
			int i = 0;
			int length = cursor.getInt(cursor.getColumnIndex(LSGSQliteOpenHelper.DB_LENGTH));
			while(i < length) {
				lesson++;
				when += ", " + lesson.toString();
				i++;
				}
			when += ".";
			holder.when.setText(when + context.getString(R.string.hour));
			String vtext = cursor.getString(cursor.getColumnIndex(LSGSQliteOpenHelper.DB_VERTRETUNGSTEXT));
			if(vtext.equals("null"))
				holder.vtext.setVisibility(View.GONE);
			else {
				holder.vtext.setVisibility(View.VISIBLE);
				holder.vtext.setText("[" + vtext + "]");
				}
			String lehrer    = cursor.getString(cursor.getColumnIndex(LSGSQliteOpenHelper.DB_LEHRER));
			if(cursor.getString(cursor.getColumnIndex(LSGSQliteOpenHelper.DB_TYPE)).equals("Entfall")){
				holder.bottom.setText(context.getString(R.string.at) + " " + lehrer);
				} else {
					String vertreter = cursor.getString(cursor.getColumnIndex(LSGSQliteOpenHelper.DB_VERTRETER));
					String raum = cursor.getString(cursor.getColumnIndex(LSGSQliteOpenHelper.DB_ROOM));
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
				String info = cursor.getString(cursor.getColumnIndex(LSGSQliteOpenHelper.DB_VERTRETUNGSTEXT));
				holder.webv.loadData(info, "text/html", null);
				//holder.vtext.setText(Html.fromHtml(cursor.getString(cursor.getColumnIndex(Functions.DB_VERTRETUNGSTEXT))));
				}
		}
}
