package com.lsg.app.lib;

import java.io.File;

import com.lsg.app.Functions;
import com.lsg.app.R;
import com.lsg.app.R.string;

import android.annotation.TargetApi;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

@TargetApi(9)
public class Download {
	private DownloadManager downloadManager;
	private SharedPreferences prefs;
	private Context context;
	public Download(Context context) {
		this.context = context;
		prefs = PreferenceManager.getDefaultSharedPreferences(context);
        downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
	}
	public void download() {
		Uri downloadUri = Uri.parse(Functions.UPDATE_URL);
		DownloadManager.Request request = new DownloadManager.Request(downloadUri);
		long downid = downloadManager.enqueue(request);
		SharedPreferences.Editor edit = prefs.edit();
		edit.putLong("downid", downid);
		edit.commit();
	}
    public BroadcastReceiver downloadReceiver = new BroadcastReceiver() {
    	@Override
    	public void onReceive(Context arg0, Intent arg1) {
    		Log.d("asdf", "onreceive");
    		DownloadManager.Query query = new DownloadManager.Query();
    		query.setFilterById(prefs.getLong("downid", 0));
    		Cursor cursor = downloadManager.query(query);
    		if(cursor.moveToFirst()){
    			int columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
    			int status = cursor.getInt(columnIndex);
    			if(status == DownloadManager.STATUS_SUCCESSFUL){
    				//Retrieve the saved request id
    				//long downloadID = prefs.getLong("downid", 0);
    				if(Build.VERSION.SDK_INT >= 11) {
    					String localFileName = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME));
    					//Toast.makeText(context, localFileName, Toast.LENGTH_LONG).show();
    					Intent intent = new Intent(Intent.ACTION_VIEW);
    					intent.setDataAndType(Uri.fromFile(new File(localFileName)), "application/vnd.android.package-archive");
    					context.startActivity(intent);
    				}
    				else {
    					Toast.makeText(context, R.string.please_install, Toast.LENGTH_LONG).show();
    					Intent intent = new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS);
    					context.startActivity(intent);
    				}
    				}
    			}
    		}
    	};
}
