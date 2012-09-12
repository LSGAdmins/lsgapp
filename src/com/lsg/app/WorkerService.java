package com.lsg.app;

import com.lsg.app.TimeTable.TimeTableUpdater;

import android.app.IntentService;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

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
	public static String ACTION    = "action";
	public static String MESSENGER = "messenger";
	public static String TIMETABLE = "timetable";

	public WorkerService() {
		super("WorkerService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Bundle extras = intent.getExtras();
		String action = extras.getString(ACTION);
		ClassLoader loader = WorkerClass.class.getClassLoader();
		try {
		Class class_ = loader.loadClass(extras.getString(WORKER_CLASS));
		WorkerClass object = (WorkerClass) class_.newInstance();
		object.update(extras.getInt(WHAT), getApplicationContext());
		} catch(Exception e) {
			e.printStackTrace();
		}
		Log.d("service", "handling intent");
		if (extras != null) {
			Messenger messenger = (Messenger) extras.get(MESSENGER);
			Message msg = Message.obtain();
			msg.arg1 = RESULT_OK;
			try {
				messenger.send(msg);
			} catch (android.os.RemoteException e1) {
				Log.w(getClass().getName(), "Exception sending message", e1);
			}

		}
	}
}
