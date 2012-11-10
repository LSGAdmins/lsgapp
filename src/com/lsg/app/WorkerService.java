package com.lsg.app;

import java.net.URLEncoder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.preference.PreferenceManager;
import android.util.Log;

import com.lsg.app.lib.JBNotification;

public class WorkerService extends IntentService {
	public interface WorkerClass {
		public void update(int what, Context c);
	}

	public static int RESULT_OK = 0;
	public static int RESULT_ERROR = 1;
	public static final int UPDATE_ALL = 0;
	public static final int UPDATE_PUPILS = 1;
	public static final int UPDATE_TEACHERS = 2;
	public static String WHAT = "what";
	public static String WORKER_CLASS = "workerclass";
	public static String ACTION = "action";
	public static String MESSENGER = "messenger";
	public static String TIMETABLE = "timetable";

	public WorkerService() {
		super("WorkerService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Bundle extras = intent.getExtras();
		if (extras.getInt(WHAT) == 100) {
			checkUpdate();
			loadNews();
		} else if (extras.getInt(WHAT) == 200) {
			try {
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			
			PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);

				String add = "&exception="
						+ URLEncoder.encode(prefs.getString("exception", ""),
								"UTF-8") + "&appversion="
						+ URLEncoder.encode(pInfo.versionName, "UTF-8")
						+ "&appcode="
						+ URLEncoder.encode(Integer.valueOf(pInfo.versionCode).toString(), "UTF-8");
			String data = Functions.getData(Functions.ERROR_URL, this, false, add);
			if(data.equals("success")) {
				SharedPreferences.Editor edit = prefs.edit();
				edit.remove("exception");
				edit.commit();
			}
			} catch(Exception e) {
			}
		} else {
			ClassLoader loader = WorkerClass.class.getClassLoader();
			try {
				Class<?> class_ = loader.loadClass(extras
						.getString(WORKER_CLASS));
				WorkerClass object = (WorkerClass) class_.newInstance();
				object.update(extras.getInt(WHAT), getApplicationContext());
			} catch (Exception e) {
				e.printStackTrace();
			}
			Log.d("service", "handling intent");
			if (extras != null) {
				Messenger messenger = (Messenger) extras.get(MESSENGER);
				Message msg = Message.obtain();
				msg.arg1 = RESULT_OK;
				try {
					messenger.send(msg);
				} catch (Exception e) {
					Log.w(getClass().getName(), "Exception sending message", e);
				}
			}
		}
	}

	@SuppressWarnings("deprecation")
	public void checkUpdate() {
		String get = Functions.getData(Functions.UPDATE_CHECK_URL
				+ getString(R.string.versionname), getApplicationContext(),
				false, "");
		try {
			JSONObject jObject = new JSONObject(get);
			if (!jObject.getBoolean("act")) {
				if (Functions.getSDK() < 16) {
					String ns = Context.NOTIFICATION_SERVICE;
					NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);

					int icon = R.drawable.ic_launcher;
					CharSequence tickerText = getString(R.string.update_available);
					long when = System.currentTimeMillis();

					Notification notification = new Notification(icon,
							tickerText, when);
					Context context = getApplicationContext();
					CharSequence contentTitle = getString(R.string.update_available);
					CharSequence contentText = getString(R.string.update_available_content)
							+ " " + jObject.getString("actversion");
					Intent notificationIntent = new Intent(this,
							UpdateActivity.class);
					notificationIntent.putExtra("version",
							jObject.getString("actversion"));
					notificationIntent.putExtra("changelog",
							jObject.getString("changelog"));
					PendingIntent contentIntent = PendingIntent.getActivity(
							this, 0, notificationIntent, 0);

					notification.setLatestEventInfo(context, contentTitle,
							contentText, contentIntent);
					final int NOTIFICATION_ID = 1;

					mNotificationManager.notify(NOTIFICATION_ID, notification);
				} else
					JBNotification.makeJBUpdateNotification(this,
							jObject.getString("actversion"),
							jObject.getString("changelog"));
			}
		} catch (JSONException e) {
			Log.d("json", e.getMessage());
		}
	}

	public void loadNews() {
		String get = Functions.getData(Functions.NEWS_URL,
				getApplicationContext(), true, "");
		try {
			JSONArray contents = new JSONArray(get);
			String allContents = "";
			for (int i = 0; i < contents.length(); i++) {
				allContents = allContents + ((i != 0) ? "\n" : "")
						+ contents.getString(i);
			}
			SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(getApplicationContext());
			SharedPreferences.Editor edit = prefs.edit();
			edit.putString(Functions.NEWS_PUPILS, allContents);
			edit.commit();
		} catch (JSONException e) {
			Log.d("json", e.getMessage());
		}
		get = Functions.getData(Functions.NEWS_URL + "?type=teachers",
				getApplicationContext(), true, "");
		try {
			JSONArray contents = new JSONArray(get);
			String allContents = "";
			for (int i = 0; i < contents.length(); i++) {
				allContents = allContents + ((i != 0) ? "\n\n" : "")
						+ contents.getString(i);
			}
			SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(getApplicationContext());
			SharedPreferences.Editor edit = prefs.edit();
			edit.putString(Functions.NEWS_TEACHERS, allContents);
			edit.commit();
		} catch (JSONException e) {
			Log.d("json", e.getMessage());
		}
	}
}
