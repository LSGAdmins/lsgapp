package com.lsg.app;

import java.util.Calendar;

import org.apache.http.util.EncodingUtils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ViewPagerAdapter extends PagerAdapter implements SQLlist, TextWatcher {
	private String[] where_conds = new String[4];
	private String[] where_conds_events = new String[6];
	private String[] exclude_subjects = new String[4];
	private final SQLiteDatabase myDB;
	public Cursor c;
	public Cursor second_c;
	public Cursor events;
	public Cursor timetable_c;
	private VPlan.VertretungAdapter vadapter_second;
	private VPlan.VertretungAdapter vadapter;
	private TimeTable.TimetableAdapter timetableadap;
	private Events.EventAdapter evadapter;
	private String exclude_cond;
	private String include_cond;
	private lsgapp act;
	private final Context context;
	private final SharedPreferences prefs;
	
	public ViewPagerAdapter(lsgapp act) {
		where_conds[0] = "%";
		where_conds[1] = "%";
		where_conds[2] = "%";
		where_conds[3] = "%";
		where_conds_events[0] = "%";
		where_conds_events[1] = "%";
		where_conds_events[2] = "%";
		where_conds_events[3] = "%";
		where_conds_events[4] = "%";
		where_conds_events[5] = "%";
		prefs = PreferenceManager.getDefaultSharedPreferences(act);
		exclude_subjects[1] = (prefs.getString(Functions.GENDER, "").equals("m")) ? "Sw" : "Sm";
		if(prefs.getString(Functions.RELIGION, "").equals(Functions.KATHOLISCH)) {
			exclude_subjects[2] = Functions.EVANGELISCH;
			exclude_subjects[3] = Functions.ETHIK;
		} else if(prefs.getString(Functions.RELIGION, "").equals(Functions.EVANGELISCH)) {
			exclude_subjects[2] = Functions.KATHOLISCH;
			exclude_subjects[3] = Functions.ETHIK;
		} else {
			exclude_subjects[2] = Functions.KATHOLISCH;
			exclude_subjects[3] = Functions.EVANGELISCH;
		}
		Log.d("gender", prefs.getString(Functions.GENDER, ""));
		Log.d("religion", prefs.getString(Functions.RELIGION, ""));
		Log.d("exclude_subjects[]", exclude_subjects[0] + " asdf");
		Log.d("exclude_subjects[]", exclude_subjects[1] + " asdf");
		Log.d("exclude_subjects[]", exclude_subjects[2] + " asdf");
		Log.d("exclude_subjects[]", exclude_subjects[3] + " asdf");
		context = (Context) act;
		this.act = act;
		
		myDB = context.openOrCreateDatabase(Functions.DB_NAME, Context.MODE_PRIVATE, null);
		updateCondLists();
		
		SQLiteStatement num_rows = myDB.compileStatement("SELECT COUNT(*) FROM " + Functions.DB_VPLAN_TABLE);
		long count = num_rows.simpleQueryForLong();
		/*if(count == 0)
			act.updateVP();*/
		vadapter = new VPlan.VertretungAdapter(context, c, false);
		vadapter_second = new VPlan.VertretungAdapter(context, second_c, false);
		evadapter = new Events.EventAdapter(context, events);
		timetableadap = new TimeTable.TimetableAdapter(context, timetable_c);
		updateCursor();
		}
	
/*	@Override
	public String getTitle( int position )
	{
		super.get
		return titles[ position ];
		}*/
	
	@Override
	public int getCount() {
		return 5;
		}
	
	@Override
	public Object instantiateItem(View pager, int position) {
		if(position == 0) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			LinearLayout lay = (LinearLayout) inflater.inflate(R.layout.list, null);
			ListView lv = (ListView) lay.findViewById(android.R.id.list);
			lv.setAdapter(timetableadap);
			lv.setOnItemClickListener(new OnItemClickListener() {
				     @Override
				     public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				          Cursor c = myDB.query(Functions.DB_TIME_TABLE, new String[] {Functions.DB_RAW_FACH, Functions.DB_HOUR, Functions.DB_LEHRER},
				        		  Functions.DB_ROWID + "=?", new String[] {new Long(id).toString()}, null, null, null);
				          c.moveToFirst();
				          String hour = Integer.valueOf(c.getInt(c.getColumnIndex(Functions.DB_HOUR)) + 1).toString();
				          Cursor d = myDB.query(Functions.DB_VPLAN_TABLE, new String[] {Functions.DB_KLASSE, Functions.DB_STUNDE, Functions.DB_VERTRETUNGSTEXT, 
				        		  Functions.DB_LEHRER, Functions.DB_FACH, Functions.DB_ART}, Functions.DB_RAW_FACH + "=? AND " + Functions.DB_STUNDE + "=? AND "
				          + Functions.DB_LEHRER + "=?",
				        		  new String[] {c.getString(c.getColumnIndex(Functions.DB_RAW_FACH)), hour, c.getString(c.getColumnIndex(Functions.DB_LEHRER))}, null, null, null);
				          d.moveToFirst();
				          if(d.getCount() > 0) {
				        	  String vtext = (!(d.getString(d.getColumnIndex(Functions.DB_VERTRETUNGSTEXT))).equals("null")) ? d.getString(
				        			  d.getColumnIndex(Functions.DB_VERTRETUNGSTEXT)) + "\n" : "";
				  	    	AlertDialog.Builder builder = new AlertDialog.Builder(context);
				  	    	builder.setTitle(d.getString(d.getColumnIndex(Functions.DB_KLASSE)))
				  	    		   .setMessage(d.getString(d.getColumnIndex(Functions.DB_FACH)) + " / " + d.getString(d.getColumnIndex(Functions.DB_STUNDE)) + ". "
				  	    				   + context.getString(R.string.hour)+ "\n" + vtext + d.getString(d.getColumnIndex(Functions.DB_ART)) + " " +context.getString(R.string.at)
				  	    				   + " " + d.getString(d.getColumnIndex(Functions.DB_LEHRER)))
				  	    	       .setCancelable(true)
				  	    	       .setNeutralButton(context.getString(R.string.ok), new DialogInterface.OnClickListener() {
				  	    	           public void onClick(DialogInterface dialog, int id) {
				  	    	                dialog.cancel();
				  	    	           }
				  	    	       });
				  	    	AlertDialog alert = builder.create();
				  	    	alert.show();
				          }
				         }
				});
			((ViewPager)pager).addView(lay, position);
			return lay;
		}
		else if(position < 4) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			LinearLayout lay = (LinearLayout) inflater.inflate(R.layout.list, null);
			ListView lv = (ListView) lay.findViewById(android.R.id.list);
			//set header search bar
			if(Functions.getSDK() < 11) {
				View search = inflater.inflate(R.layout.search, null);
				EditText searchEdit = (EditText) search.findViewById(R.id.search_edit);
				searchEdit.addTextChangedListener(this);
				lv.addHeaderView(search);
				}
			if(position == 1)
				lv.setAdapter(vadapter);
			if(position == 2)
				lv.setAdapter(vadapter_second);
			if(position == 3)
				lv.setAdapter(evadapter);
			else
				act.registerForContextMenu(lv);
			lv.setEmptyView(lay.findViewById(R.id.list_view_empty));
			((TextView) lay.findViewById(R.id.list_view_empty)).setText(R.string.vplan_empty);
			((ViewPager)pager).addView(lay, 0);
			return lay;
			}
		else {
			WebView webview = new WebView(context);
			webview.getSettings().setJavaScriptEnabled(true);
			/*webview.setWebChromeClient(new WebChromeClient() {
				public void onProgressChanged(WebView view, int progress) {
					activity.setProgress(progress*1000);
					}
				});*/
			webview.setWebViewClient(new WebViewClient() {
				public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
					Toast.makeText(context, context.getString(R.string.oops) + " " + description, Toast.LENGTH_SHORT).show();
					}
				});
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
			String postData = "log=" + prefs.getString("username", "")
					+ "&pwd=" + prefs.getString("password", "") + "&redirect_to=http://www.lsg.musin.de/smv/aktuelles/";
			if(Functions.getSDK() >= 5) {
				AdvancedWrapper advWrapper = new AdvancedWrapper();
				advWrapper.postUrl(webview, "http://www.lsg.musin.de/smv/login/?action=login", EncodingUtils.getBytes(postData, "BASE64"));
				advWrapper = null;
				//webview.postUrl("http://www.lsg.musin.de/smv/login/?action=login", EncodingUtils.getBytes(postData, "BASE64"));
			}
			else
				webview.loadUrl("http://www.lsg.musin.de/smv/login/?action=login");
			return webview;
			}
		}
	

	public void updateCondLists() {
		exclude_cond = new String();
		Cursor exclude = myDB.query(Functions.EXCLUDE_TABLE, new String[] {Functions.DB_FACH, Functions.DB_NEEDS_SYNC},
				null, null, null, null, null);
		exclude.moveToFirst();
		int i = 0;
		while(i < exclude.getCount()) {
			String fach = exclude.getString(exclude.getColumnIndex(Functions.DB_FACH));
			exclude_cond += " AND " + Functions.DB_FACH + " != '" + fach + "' ";
			exclude.moveToNext();
			i++;
		}
		exclude.close();
		include_cond = new String();
		Cursor include = myDB.query(Functions.INCLUDE_TABLE, new String[] {Functions.DB_FACH, Functions.DB_NEEDS_SYNC},
				null, null, null, null, null);
		include.moveToFirst();
		i = 0;
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		String connector = "";
		while(i < include.getCount()) {
			String fach = include.getString(include.getColumnIndex(Functions.DB_FACH));
			include_cond += connector + Functions.DB_FACH + " LIKE '%" + fach + "%' ";
			connector = " OR ";
			include.moveToNext();
			i++;
		}
		include.close();
		if(include_cond.length() == 0)
			include_cond = " 0 ";
		if(prefs.getBoolean("showonlywhitelist", false))
			include_cond = "AND (" + include_cond + " ) OR ( " + include_cond + " )";
		else
			include_cond = " OR ( " + include_cond + " )";
	}
	public void updateCursor() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		String klasse = prefs.getString("class", "");
		where_conds[0] =  "%" + klasse + "%";
		String first = "( " + Functions.DB_KLASSE + " LIKE ? ";
		String sec = "";
		if(prefs.getBoolean("showwithoutclass", true))
			sec = "OR " + Functions.DB_KLASSE + " LIKE 'null'";
		sec += " OR " + Functions.DB_KLASSE + " LIKE 'infotext') AND ( " + Functions.DB_KLASSE
				+ " LIKE ? OR " + Functions.DB_FACH + " LIKE ? OR " + Functions.DB_LEHRER + " LIKE ? )";
		String mine_cond = first + include_cond +  sec + exclude_cond;
		String all_cond = first + sec;
		c = myDB.query(Functions.DB_VPLAN_TABLE, new String [] {Functions.DB_ROWID, Functions.DB_KLASSE, Functions.DB_ART, Functions.DB_STUNDE,
				Functions.DB_LEHRER, Functions.DB_FACH, Functions.DB_VERTRETUNGSTEXT, Functions.DB_VERTRETER, Functions.DB_RAUM,
				Functions.DB_KLASSENSTUFE, Functions.DB_DATE, Functions.DB_LENGTH}, mine_cond, where_conds, null, null, null);
		where_conds[0] = "%";
		second_c = myDB.query(Functions.DB_VPLAN_TABLE, new String [] {Functions.DB_ROWID, Functions.DB_KLASSE, Functions.DB_ART, Functions.DB_STUNDE,
				Functions.DB_LEHRER, Functions.DB_FACH, Functions.DB_VERTRETUNGSTEXT, Functions.DB_VERTRETER, Functions.DB_RAUM,
				Functions.DB_KLASSENSTUFE, Functions.DB_DATE, Functions.DB_LENGTH}, all_cond, where_conds, null, null, null);
		vadapter.changeCursor(c);
		vadapter_second.changeCursor(second_c);
		
		String where_cond = " " + Functions.DB_DATES + " LIKE ? OR " + Functions.DB_ENDDATES + " LIKE ? OR "
		+ Functions.DB_TIMES + " LIKE ? OR " + Functions.DB_ENDTIMES + " LIKE ? OR " + Functions.DB_TITLE + " LIKE ? OR "
				+ Functions.DB_VENUE + " LIKE ? ";
		events = myDB.query(Functions.DB_EVENTS_TABLE, new String [] {Functions.DB_ROWID, Functions.DB_DATES, Functions.DB_ENDDATES,
				Functions.DB_TIMES,	Functions.DB_ENDTIMES, Functions.DB_TITLE, Functions.DB_VENUE}, where_cond,
				where_conds_events, null, null, null);
		evadapter.changeCursor(events);
		
		
		Calendar cal = Calendar.getInstance();
		int day;
		switch(cal.get(Calendar.DAY_OF_WEEK)) {
		case Calendar.MONDAY:
			day = 0;
			break;
		case Calendar.TUESDAY:
			day = 1;
			break;
		case Calendar.WEDNESDAY:
			day = 2;
			break;
		case Calendar.THURSDAY:
			day = 3;
			break;
		case Calendar.FRIDAY:
			day = 4;
			break;
			default:
				day = 0;
				break;
		}
		exclude_subjects[0] = Integer.valueOf(day).toString();
		timetable_c = myDB.query(Functions.DB_TIME_TABLE, new String[] {Functions.DB_ROWID, Functions.DB_LEHRER, Functions.DB_FACH, Functions.DB_RAUM, Functions.DB_LENGTH,
				Functions.DB_HOUR, Functions.DB_DAY, Functions.DB_RAW_FACH}, Functions.DB_DAY + "=? AND  " + Functions.DB_RAW_FACH + " != ? AND " + Functions.DB_RAW_FACH
				+ " != ? AND " +  Functions.DB_RAW_FACH + " != ?", exclude_subjects, null, null, null);
		timetableadap.changeCursor(timetable_c);
	}
	public void updateWhereCond(String searchText) {
		where_conds[1] = "%" + searchText + "%";
		where_conds[2] = "%" + searchText + "%";
		where_conds[3] = "%" + searchText + "%";
		
		where_conds_events[0] = "%" + searchText + "%";
		where_conds_events[1] = "%" + searchText + "%";
		where_conds_events[2] = "%" + searchText + "%";
		where_conds_events[3] = "%" + searchText + "%";
		where_conds_events[4] = "%" + searchText + "%";
		where_conds_events[5] = "%" + searchText + "%";
		updateCursor();
	}
	public void updateList() {
		updateCondLists();
		updateCursor();
	}
	public void afterTextChanged (Editable s) { }
	public void beforeTextChanged (CharSequence s, int start, int count, int after) { }
	public void onTextChanged (CharSequence s, int start, int before, int count) {
		String search = s + "";
		updateWhereCond(search);
	}
	public void closeCursorsDB() {
		c.close();
		second_c.close();
		events.close();
		myDB.close();
		timetableadap.close();
	}
	@Override
	public void destroyItem( View pager, int position, Object view ) {
		((ViewPager)pager).removeView((View) view);
		}
	
	@Override
	public boolean isViewFromObject( View view, Object object ) {
		return view.equals( object );
		}
	
	@Override
	public void finishUpdate( View view ) {}
	
	@Override
	public void restoreState( Parcelable p, ClassLoader c ) {}
	
	@Override
	public Parcelable saveState() {
		return null;
		}
	@Override
	public void startUpdate( View view ) {}
}