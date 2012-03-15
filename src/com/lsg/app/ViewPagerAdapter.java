package com.lsg.app;

import org.apache.http.util.EncodingUtils;
import android.content.Context;
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
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ViewPagerAdapter extends PagerAdapter implements SQLlist, TextWatcher {
	private String[] where_conds = new String[4];
	private String[] where_conds_events = new String[6];
	private SQLiteDatabase myDB;
	public Cursor c;
	public Cursor second_c;
	public Cursor events;
	private VertretungCursor vcursor_second;
	private VertretungCursor vcursor;
	private EventCursor evcursor;
	private String exclude_cond;
	private String include_cond;
	private lsgapp act;
	private final Context context;
	
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
		context = (Context) act;
		this.act = act;
		
		myDB = context.openOrCreateDatabase(Functions.DB_NAME, Context.MODE_PRIVATE, null);
		updateCondLists();
		
		SQLiteStatement num_rows = myDB.compileStatement("SELECT COUNT(*) FROM " + Functions.DB_VPLAN_TABLE);
		long count = num_rows.simpleQueryForLong();
		if(count == 0)
			act.updateVP();
		vcursor = new VertretungCursor(context, c);
		vcursor_second = new VertretungCursor(context, second_c);
		evcursor = new EventCursor(context, events);
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
		return 4;
		}
	
	@Override
	public Object instantiateItem(View pager, int position) {
		if(position < 3) {
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
			if(position == 0)
				lv.setAdapter(vcursor);
			if(position == 1)
				lv.setAdapter(vcursor_second);
			if(position == 2)
				lv.setAdapter(evcursor);
			else
				act.registerForContextMenu(lv);
			lv.setEmptyView(lay.findViewById(R.id.list_view_empty));
			((TextView) lay.findViewById(R.id.list_view_empty)).setText(R.string.vplan_empty);
			((ViewPager)pager).addView(lay, position);
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
				Functions.DB_KLASSENSTUFE, Functions.DB_DATE}, mine_cond, where_conds, null, null, null);
		where_conds[0] = "%";
		second_c = myDB.query(Functions.DB_VPLAN_TABLE, new String [] {Functions.DB_ROWID, Functions.DB_KLASSE, Functions.DB_ART, Functions.DB_STUNDE,
				Functions.DB_LEHRER, Functions.DB_FACH, Functions.DB_VERTRETUNGSTEXT, Functions.DB_VERTRETER, Functions.DB_RAUM,
				Functions.DB_KLASSENSTUFE, Functions.DB_DATE}, all_cond, where_conds, null, null, null);
		vcursor.changeCursor(c);
		vcursor_second.changeCursor(second_c);
		
		String where_cond = " " + Functions.DB_DATES + " LIKE ? OR " + Functions.DB_ENDDATES + " LIKE ? OR "
		+ Functions.DB_TIMES + " LIKE ? OR " + Functions.DB_ENDTIMES + " LIKE ? OR " + Functions.DB_TITLE + " LIKE ? OR "
				+ Functions.DB_VENUE + " LIKE ? ";
		events = myDB.query(Functions.DB_EVENTS_TABLE, new String [] {Functions.DB_ROWID, Functions.DB_DATES, Functions.DB_ENDDATES,
				Functions.DB_TIMES,	Functions.DB_ENDTIMES, Functions.DB_TITLE, Functions.DB_VENUE}, where_cond,
				where_conds_events, null, null, null);
		evcursor.changeCursor(events);
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