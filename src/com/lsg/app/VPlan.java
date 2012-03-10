package com.lsg.app;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewFlipper;

public class VPlan extends ListActivity implements TextWatcher, SQLlist, OnTouchListener  {
	private String[] where_conds = new String[4];
	private ProgressDialog loading;
	private SQLiteDatabase myDB;
	private Cursor c;
	private Cursor second_c;
	private VertretungCursor vcursor_second;
	private VertretungCursor vcursor;
	private boolean mine = true;
	private String exclude_cond;
	private String include_cond;
	private float lastX, lastY, moveX, moveY;
	private ViewFlipper vFlip;
	private LinearLayout left, right;
	private boolean noAnim;
	private boolean lock;
    DisplayMetrics metrics = new DisplayMetrics();

	final Handler handler = new Handler() {
        public void handleMessage(Message msg) {
        	if(msg.arg1 == 1) {
        		loading.cancel();
        		updateCursor(mine);
        	}
        	if(msg.arg1 == 2) {
        		loading.cancel();
        	}
        	if(msg.arg1 == 3) {
        		loading.cancel();
    			loading = ProgressDialog.show(VPlan.this, "", getString(msg.arg2), true);
        	}
        }
    };
    
	@Override
	public void onCreate(Bundle savedInstanceState) {
		where_conds[0] = "%";
		where_conds[1] = "%";
		where_conds[2] = "%";
		where_conds[3] = "%";
		
		super.onCreate(savedInstanceState);
		Functions.testDB(this);
        
		Functions.setTheme(false, true, this);
		
		setContentView(R.layout.list_viewflipper);
		findViewById(R.id.second_list).setOnTouchListener(this);
		getListView().setOnTouchListener(this);
		vFlip = (ViewFlipper) findViewById(R.id.viewflipper);
		left  = (LinearLayout) findViewById(R.id.first);
		right = (LinearLayout) findViewById(R.id.second);//vFlip.getChildAt(vFlip.getDisplayedChild() + 1);
	    getWindowManager().getDefaultDisplay().getMetrics(metrics);
		
		getWindow().setBackgroundDrawableResource(R.layout.background);
		
		//set header search bar
		if(Functions.getSDK() < 11) {
			LayoutInflater inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
			View search = inflater.inflate(R.layout.search, null);
			EditText searchEdit = (EditText) search.findViewById(R.id.search_edit);
			searchEdit.addTextChangedListener(this);
			getListView().addHeaderView(search);
			ListView sec = (ListView) findViewById(R.id.second_list);
			sec.addHeaderView(search);
		}
        
		if(Functions.getSDK() >= 11) {
			try {
				AdvancedWrapper actHelper = new AdvancedWrapper();
				actHelper.dropDownNav(this);
				} catch (Exception e) {
					Log.d("error", e.getMessage());
					}
		}
		
		myDB = this.openOrCreateDatabase(Functions.DB_NAME, MODE_PRIVATE, null);
		updateCondLists();
		
		SQLiteStatement num_rows = myDB.compileStatement("SELECT COUNT(*) FROM " + Functions.DB_VPLAN_TABLE);
		long count = num_rows.simpleQueryForLong();
		if(count == 0)
			updateVP();
		
		vcursor = new VertretungCursor(this, c);
		getListView().setAdapter(vcursor);
        Functions.styleListView(getListView(), this);
        registerForContextMenu(getListView());
        
        //info if listview empty
        getListView().setEmptyView(findViewById(R.id.list_view_empty));
        TextView textv = (TextView) findViewById(R.id.list_view_empty);
        textv.setText(R.string.vplan_empty);
        
        //second listview, with all
        ListView all = (ListView) findViewById(R.id.second_list);
        vcursor_second = new VertretungCursor(this, second_c);
        all.setAdapter(vcursor_second);
        Functions.styleListView(all, this);
        registerForContextMenu(all);
		updateCursor(mine);
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
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
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
	public void getCursor() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
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
		second_c = myDB.query(Functions.DB_VPLAN_TABLE, new String [] {Functions.DB_ROWID, Functions.DB_KLASSE, Functions.DB_ART, Functions.DB_STUNDE,
				Functions.DB_LEHRER, Functions.DB_FACH, Functions.DB_VERTRETUNGSTEXT, Functions.DB_VERTRETER, Functions.DB_RAUM,
				Functions.DB_KLASSENSTUFE, Functions.DB_DATE}, mine_cond, where_conds, null, null, null);
		where_conds[0] = "%";
		c = myDB.query(Functions.DB_VPLAN_TABLE, new String [] {Functions.DB_ROWID, Functions.DB_KLASSE, Functions.DB_ART, Functions.DB_STUNDE,
				Functions.DB_LEHRER, Functions.DB_FACH, Functions.DB_VERTRETUNGSTEXT, Functions.DB_VERTRETER, Functions.DB_RAUM,
				Functions.DB_KLASSENSTUFE, Functions.DB_DATE}, all_cond, where_conds, null, null, null);
	}
	public void updateCursor(boolean mine) {
		ViewFlipper vFlip = (ViewFlipper) findViewById(R.id.viewflipper);
		if(this.mine != mine) {
			vFlip.setInAnimation(this, android.R.anim.slide_in_left);
			vFlip.setOutAnimation(this, android.R.anim.slide_out_right);
			if(vFlip.getDisplayedChild() != 0) {
				vFlip.showPrevious();
				mine = true;
			}
			else {
				vFlip.showNext();
				mine = false;
			}
			Log.d("flip", new Integer(vFlip.getDisplayedChild()).toString());
		}
        
		this.mine = mine;
		getCursor();
		vcursor.changeCursor(c);
		vcursor_second.changeCursor(second_c);
	}
	@Override
	public boolean onTouch(View v, MotionEvent motionEvent) {
		switch (motionEvent.getAction()) {
		case MotionEvent.ACTION_DOWN:
			lastX = motionEvent.getX();
			lastY = motionEvent.getY();
			break;
		case MotionEvent.ACTION_UP:
			if(!noAnim) {
				float movez;
				if(lastX-motionEvent.getX() > (metrics.widthPixels / 2))
					movez = metrics.widthPixels - (lastX - motionEvent.getX());
				else
					movez = (lastX - motionEvent.getX());
				TranslateAnimation ta = new TranslateAnimation(Animation.ABSOLUTE, movez, Animation.ABSOLUTE, 0, Animation.ABSOLUTE, 0, Animation.ABSOLUTE, 0);
				ta.setDuration(100);
				FrameLayout.LayoutParams lp_up = new FrameLayout.LayoutParams(left.getLayoutParams());
				lp_up.setMargins(0, 0, 0, 0);
				if((lastX-motionEvent.getX() > (metrics.widthPixels / 5) && mine) || (!mine && lastX+motionEvent.getX() < (metrics.widthPixels / 4))) {
					right.startAnimation(ta);
					right.invalidate();
					left.setVisibility(View.GONE);
					mine = false;
					if(Functions.getSDK() >= 11) {
						AdvancedWrapper adv = new AdvancedWrapper();
						adv.selectedItem(1, this);
					}
					} else {
						left.startAnimation(ta);
						left.invalidate();
						right.setVisibility(View.GONE);
						mine = true;
						if(Functions.getSDK() >= 11) {
							AdvancedWrapper adv = new AdvancedWrapper();
							adv.selectedItem(0, this);
						}
						}
				left.setLayoutParams(lp_up);
				right.setLayoutParams(lp_up);
			}
			lock = false;
			break;
	    case MotionEvent.ACTION_MOVE:
	    	if((moveX - motionEvent.getX() > 5 || moveX - motionEvent.getX() < -5) && !lock) {
	            left.setVisibility(View.VISIBLE);
	            right.setVisibility(View.VISIBLE);
	            
	    		int move = (int)(lastX - motionEvent.getX());
	    	    FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(left.getLayoutParams());
	            lp.setMargins(-move, 0, move, 0);
	            
	    	    FrameLayout.LayoutParams lp_sec = new FrameLayout.LayoutParams(right.getLayoutParams());
	            lp_sec.setMargins(((int) metrics.widthPixels) - move, 0, move - ((int) metrics.widthPixels), 0);
	            if(!mine) {
	            	lp_sec.setMargins((-move - metrics.widthPixels), 0, metrics.widthPixels + move, 0);
	            }
	            
	            
	            if((mine && move < 0) || (!mine && move > 0)) {
	            	lp_sec.setMargins(metrics.widthPixels, 0, 0, 0);
	            	lp.setMargins(0, 0, 0, 0);
	            	noAnim = true;
	            }
	            else
	            	noAnim = false;
	            if(mine) {
	            	right.setLayoutParams(lp_sec);
	            	left.setLayoutParams(lp);
	            }
	            else {
	            	left.setLayoutParams(lp_sec);
	            	right.setLayoutParams(lp);
	            }
	            left.invalidate();
	            right.invalidate();
	    	}
	    	else if(lastY - motionEvent.getY() > 3) {
	    		lock = true;
	    		noAnim = true;
	    	}
	    	moveX = motionEvent.getX();
	    	moveY = motionEvent.getY();
	    	break;
	  }
	  return false;
	}
	public void updateWhereCond(String searchText) {
		where_conds[1] = "%" + searchText + "%";
		where_conds[2] = "%" + searchText + "%";
		where_conds[3] = "%" + searchText + "%";
		updateCursor(mine);
	}
	public void updateList() {
		updateCondLists();
		updateCursor(this.mine);
	}
	@Override
	public void onResume() {
		updateCursor(mine);
		super.onResume();
	}
	public void updateVP() {
		loading = ProgressDialog.show(VPlan.this, "", getString(R.string.loading_vertretungen), true);
		UpdateBroadcastReceiver.ProgressThread progress = new UpdateBroadcastReceiver.ProgressThread(handler, this);
		progress.start();
		}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.vplan, menu);
	    if(Functions.getSDK() >= 11) {
	    	AdvancedWrapper ahelp = new AdvancedWrapper();
	    	ahelp.searchBar(menu, this);
	    }
	    else
	    	menu.removeItem(R.id.search);
	    return true;
	}
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.removeItem(R.id.all);
		menu.removeItem(R.id.mine);
	    if(Functions.getSDK() < 11) {
	    	if(mine)
	    		menu.add(0, R.id.all, Menu.NONE, R.string.all);
	    	else
	    		menu.add(0, R.id.mine, Menu.NONE, R.string.mine);
	    }
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	    case R.id.settings:
	    	Intent settings;
	    	if(Functions.getSDK() >= 11)
	    		settings = new Intent(this, SettingsAdvanced.class);
	    	else
	    		settings = new Intent(this, Settings.class);
	    	startActivity(settings);
	        return true;
	    case R.id.refresh:
	    	updateVP();
	    	return true;
	    case R.id.mine:
	    	updateCursor(true);
	    	return true;
	    case R.id.all:
	    	updateCursor(false);
	    	return true;
	    case R.id.subjects:
            Intent subjects = new Intent(this, SubjectList.class);
            startActivity(subjects);
	    	return true;
        case android.R.id.home:
            // app icon in action bar clicked; go home
            Intent intent = new Intent(this, lsgapp.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}
	public void afterTextChanged (Editable s) {
	}
	public void beforeTextChanged (CharSequence s, int start, int count, int after) {
		
	}
	public void onTextChanged (CharSequence s, int start, int before, int count) {
		String search = s + "";
		updateWhereCond(search);
	}
	@Override
	public void onDestroy() {
		super.onDestroy();
		myDB.close();
	}
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		Functions.createContextMenu(menu, v, menuInfo, myDB, this, Functions.DB_VPLAN_TABLE);
	}
	@Override
	public boolean onContextItemSelected(final MenuItem item) {
		return Functions.contextMenuSelect(item, myDB, this, this, Functions.DB_VPLAN_TABLE);
	}
}