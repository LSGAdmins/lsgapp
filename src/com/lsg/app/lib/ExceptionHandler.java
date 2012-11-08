package com.lsg.app.lib;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.Thread.UncaughtExceptionHandler;
import java.net.URLEncoder;

import com.lsg.app.Functions;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class ExceptionHandler implements UncaughtExceptionHandler {
	Context context;
	SharedPreferences.Editor edit;
	Thread.UncaughtExceptionHandler defaultUncaughtHandler;
	public static final String ERROR_URL = "http://linux.lsg.musin.de/cp/error.php";

	public ExceptionHandler(final Context context) {
		this.context = context;
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		edit = prefs.edit();
		
		defaultUncaughtHandler = Thread.getDefaultUncaughtExceptionHandler();
	}

	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		try {
			final StringWriter sw = new StringWriter();
			ex.printStackTrace(new PrintWriter(sw));
			edit.putString("exception", sw.toString());
			edit.commit();
		} catch (Exception e) {

		} finally {
			defaultUncaughtHandler.uncaughtException(thread, ex);
		}
	}
}
