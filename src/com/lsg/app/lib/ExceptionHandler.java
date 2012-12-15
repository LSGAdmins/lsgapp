package com.lsg.app.lib;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.Thread.UncaughtExceptionHandler;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.lsg.app.WorkerService;

public class ExceptionHandler implements UncaughtExceptionHandler {
	Context context;
	private boolean disabled = false;
	SharedPreferences.Editor edit;
	Thread.UncaughtExceptionHandler defaultUncaughtHandler;

	public ExceptionHandler(Application context) {
		this.context = context;
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		if (!prefs.getBoolean("sendcrashes", true))
			disabled = true;
		
		edit = prefs.edit();

		defaultUncaughtHandler = Thread.getDefaultUncaughtExceptionHandler();
	}
	
	public static void init (Application context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		if(prefs.getString("exception", "") != "") {
			Intent intent = new Intent(context, WorkerService.class);
		    intent.putExtra(WorkerService.WHAT, 200);
		    context.startService(intent);
		}
		Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(context));
	}

	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		try {
			if (!disabled) {
				final StringWriter sw = new StringWriter();
				ex.printStackTrace(new PrintWriter(sw));
				edit.putString("exception", sw.toString());
				edit.commit();
			}
		} catch (Exception e) {
		} finally {
			defaultUncaughtHandler.uncaughtException(thread, ex);
		}
	}
}
