package com.lsg.app;

import com.lsg.app.lib.LSGApplication;
import com.lsg.app.sqlite.LSGSQliteOpenHelper;

import android.app.Activity;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

public class InfoActivity extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle extras = getIntent().getExtras();
		Log.d("type", extras.getString("type"));
		if (extras.getString("type").equals("vplan")) {
			setTitle(R.string.vplan);
			setContentView(R.layout.info_vplan);
			((TextView) findViewById(R.id.vplan_num)).setText(extras
					.getString("vplan_num"));
			((TextView) findViewById(R.id.mine_num)).setText(extras
					.getString("mine_num"));
			((TextView) findViewById(R.id.date)).setText(extras
					.getString("date"));
			((TextView) findViewById(R.id.vplan_num_teachers)).setText(extras
					.getString("vplan_num_teachers"));
			((TextView) findViewById(R.id.date_teachers)).setText(extras
					.getString("date_teachers"));
			if (!extras.getBoolean("teacher"))
				(findViewById(R.id.teachers_container))
						.setVisibility(View.GONE);
		} else if (extras.getString("type").equals("info")) {
			requestWindowFeature(Window.FEATURE_NO_TITLE);
			setContentView(R.layout.news);
			SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(this);
			String text;
			if (extras.getString("info_type").equals("pupils"))
				text = prefs.getString(Functions.NEWS_PUPILS, "");
			else
				text = prefs.getString(Functions.NEWS_TEACHERS, "");
			((TextView) findViewById(R.id.news_content)).setText(text);
		} else if (extras.getString("type").equals("timetable_popup")) {
			setContentView(R.layout.vplan_listitem);
			SQLiteDatabase myDB = LSGApplication.getSqliteDatabase();
			String id = extras.getString("id");

			Cursor d = myDB.query(LSGSQliteOpenHelper.DB_VPLAN_TABLE,
					new String[] { LSGSQliteOpenHelper.DB_VERTRETUNGSTEXT,
					LSGSQliteOpenHelper.DB_KLASSE, LSGSQliteOpenHelper.DB_FACH,
					LSGSQliteOpenHelper.DB_STUNDE, LSGSQliteOpenHelper.DB_TYPE,
					LSGSQliteOpenHelper.DB_LEHRER, LSGSQliteOpenHelper.DB_ROOM,
					LSGSQliteOpenHelper.DB_LENGTH, LSGSQliteOpenHelper.DB_VERTRETER }, LSGSQliteOpenHelper.DB_ROWID + "=?",
					new String[] { id }, null, null, null);
			d.moveToFirst();
			findViewById(R.id.standard_webview).setVisibility(View.GONE);
			findViewById(R.id.vertretung_date).setVisibility(View.GONE);
			findViewById(R.id.vertretung_class).setVisibility(View.GONE);
			((TextView) findViewById(R.id.vertretung_title)).setText(d
					.getString(d.getColumnIndex(LSGSQliteOpenHelper.DB_KLASSE))
					+ " ("
					+ d.getString(d.getColumnIndex(LSGSQliteOpenHelper.DB_FACH)) + ")");
			((TextView) findViewById(R.id.vertretung_type)).setText(d
					.getString(d.getColumnIndex(LSGSQliteOpenHelper.DB_TYPE)));

			String when = d.getString(d.getColumnIndex(LSGSQliteOpenHelper.DB_STUNDE));
			Integer lesson = d.getInt(d.getColumnIndex(LSGSQliteOpenHelper.DB_STUNDE));
			int i = 0;
			int length = d.getInt(d.getColumnIndex(LSGSQliteOpenHelper.DB_LENGTH));
			while (i < length) {
				lesson++;
				when += ", " + lesson.toString();
				i++;
			}
			when += "." + getString(R.string.hour);
			((TextView) findViewById(R.id.vertretung_when)).setText(when);

			String vtext = d.getString(d
					.getColumnIndex(LSGSQliteOpenHelper.DB_VERTRETUNGSTEXT));
			if (vtext.equals("null"))
				((TextView) findViewById(R.id.vertretung_text))
						.setVisibility(View.GONE);
			else {
				((TextView) findViewById(R.id.vertretung_text))
						.setVisibility(View.VISIBLE);
				((TextView) findViewById(R.id.vertretung_text)).setText("["
						+ vtext + "]");
			}

			String lehrer = d.getString(d.getColumnIndex(LSGSQliteOpenHelper.DB_LEHRER));
			if (d.getString(d.getColumnIndex(LSGSQliteOpenHelper.DB_TYPE)).equals(
					"Entfall")) {
				((TextView) findViewById(R.id.vertretung_bottom)).setText(getString(R.string.at) + " " + lehrer);
			} else {
				String vertreter = d.getString(d
						.getColumnIndex(LSGSQliteOpenHelper.DB_VERTRETER));
				String raum = d.getString(d.getColumnIndex(LSGSQliteOpenHelper.DB_ROOM));
				String raumInsert = "";
				if (!raum.equals("null"))
					raumInsert = '\n' + getString(R.string.room) + " " + raum;
				((TextView) findViewById(R.id.vertretung_text)).setText(lehrer
						+ " → " + vertreter + raumInsert);
				((TextView) findViewById(R.id.vertretung_text)).setVisibility(View.VISIBLE);
			}
		}
	}
}
