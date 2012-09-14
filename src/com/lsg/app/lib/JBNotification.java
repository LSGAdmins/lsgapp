package com.lsg.app.lib;

import com.lsg.app.DownloadService;
import com.lsg.app.R;
import com.lsg.app.UpdateActivity;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

@TargetApi(16)
public class JBNotification {
	public static void makeJBUpdateNotification(Context context, String actVersion, String changelog) {

		Intent notificationIntent = new Intent(context, UpdateActivity.class);
		notificationIntent.putExtra("version", actVersion);
		notificationIntent.putExtra("changelog",changelog);
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
		
		Intent downIntent = new Intent(context, DownloadService.class);
		downIntent.putExtra("started", "notification");
		PendingIntent downLoad = PendingIntent.getService(context, 0, downIntent, 0);
		Notification.Builder builder = new Notification.Builder(context);
		builder.setSmallIcon(R.drawable.ic_launcher);
		builder.setTicker(context.getString(R.string.update_available));
		builder.setContentTitle(context.getString(R.string.update_available));
		builder.setContentText(context.getString(R.string.update_available_content) + " " + actVersion);
		builder.setAutoCancel(true);
		builder.setContentIntent(contentIntent);
		builder.addAction(android.R.drawable.ic_menu_rotate, context.getString(R.string.update), downLoad);
		((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).notify(DownloadService.NOTIFICATION_ID, builder.build());
	}
}
