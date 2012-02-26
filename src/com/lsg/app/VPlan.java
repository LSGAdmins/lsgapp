package com.lsg.app;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class VPlan extends ListActivity {
	private ProgressDialog loading;
	private SQLiteDatabase myDB;
	private Cursor c;
	public VertretungCursor vcursor;
	

	final Handler handler = new Handler() {
        public void handleMessage(Message msg) {
        	if(msg.arg1 == 1) {
        		loading.cancel();
        		updateCursor("");
        	}
        }
    };
    
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Functions.testDB(this);
		
		Functions.setTheme(false, true, this);
		Advanced adv = new Advanced();
		adv.dropDownNav(this);
		
		myDB = this.openOrCreateDatabase(Functions.DB_NAME, MODE_PRIVATE, null);
		
		/*c = myDB.rawQuery("SELECT " + Functions.DB_ROWID + ", " + Functions.DB_KLASSE + ", " + Functions.DB_ART
				+ ", " + Functions.DB_STUNDE + ", " + Functions.DB_LEHRER + ", " + Functions.DB_FACH
				+ ", " + Functions.DB_VERTRETUNGSTEXT + ", " + Functions.DB_VERTRETER + ", " + Functions.DB_RAUM
				+ " FROM "
        		+ Functions.DB_TABLE + ";", null);
		startManagingCursor(c);*/

		vcursor = new VertretungCursor(this, c);
		getListView().setAdapter(vcursor);
		updateCursor("");
	}
	public void updateCursor(String where_cond) {
		c = myDB.rawQuery("SELECT " + Functions.DB_ROWID + ", " + Functions.DB_KLASSE + ", " + Functions.DB_ART
				+ ", " + Functions.DB_STUNDE + ", " + Functions.DB_LEHRER + ", " + Functions.DB_FACH
				+ ", " + Functions.DB_VERTRETUNGSTEXT + ", " + Functions.DB_VERTRETER + ", " + Functions.DB_RAUM
				+ ", " + Functions.DB_KLASSENSTUFE + ", " + Functions.DB_DATE + " FROM "
        		+ Functions.DB_TABLE + where_cond + ";", null);
		startManagingCursor(c);
		vcursor.changeCursor(c);
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.vplan, menu);
	    return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	    case R.id.settings:
	    	Intent settings = new Intent(this, Settings.class);
	    	startActivity(settings);
	        return true;
	    case R.id.refresh:
			loading = ProgressDialog.show(VPlan.this, "", getString(R.string.loading_vertretungen), true);
			class ProgressThread extends Thread {
				Handler handler;
				ProgressThread(Handler h) {
					handler = h;
				}
				public void run() {
					Looper.prepare();
			    	Functions.refreshVPlan(VPlan.this);
			    	Functions.getClass(VPlan.this);
			    	Message msg = handler.obtainMessage();
			    	msg.arg1 = 1;
			    	handler.sendMessage(msg);
				}
			}
			ProgressThread progress = new ProgressThread(handler);
			progress.start();
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
	@Override
	public void onDestroy() {
		super.onDestroy();
		myDB.close();
	}
}
