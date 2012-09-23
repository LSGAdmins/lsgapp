package com.lsg.app;

import java.io.File;

import android.annotation.TargetApi;
import android.app.DownloadManager;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.widget.Toast;

@TargetApi(11)
public class DownloadService extends Service {
	public static final int NOTIFICATION_ID = 1;
	BroadcastReceiver downloadReceiver;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Toast.makeText(getApplicationContext(),
				getString(R.string.downloading_update), Toast.LENGTH_LONG)
				.show();
		if (intent.getExtras().getString("started").equals("notification"))
			((NotificationManager) getSystemService(NOTIFICATION_SERVICE))
					.cancel(NOTIFICATION_ID);
		final SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		final DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
		Uri downloadUri = Uri.parse(Functions.UPDATE_URL);
		DownloadManager.Request request = new DownloadManager.Request(
				downloadUri);
		long downid = downloadManager.enqueue(request);
		SharedPreferences.Editor edit = prefs.edit();
		edit.putLong("downid", downid);
		edit.commit();
		downloadReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context arg0, Intent arg1) {
				DownloadManager.Query query = new DownloadManager.Query();
				query.setFilterById(prefs.getLong("downid", 0));
				Cursor cursor = downloadManager.query(query);
				if (cursor.moveToFirst()) {
					int columnIndex = cursor
							.getColumnIndex(DownloadManager.COLUMN_STATUS);
					int status = cursor.getInt(columnIndex);
					if (status == DownloadManager.STATUS_SUCCESSFUL) {
						// Retrieve the saved request id
						String localFileName = cursor
								.getString(cursor
										.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME));
						Intent intent = new Intent(Intent.ACTION_VIEW);
						intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						intent.setDataAndType(
								Uri.fromFile(new File(localFileName)),
								"application/vnd.android.package-archive");
						startActivity(intent);
						stopSelf();
					}
				}
			}
		};
		registerReceiver(downloadReceiver, new IntentFilter(
				DownloadManager.ACTION_DOWNLOAD_COMPLETE));
		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		// We don't provide binding, so return null
		return null;
	}

	@Override
	public void onDestroy() {
		unregisterReceiver(downloadReceiver);
	}
}
