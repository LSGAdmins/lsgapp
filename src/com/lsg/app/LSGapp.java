package com.lsg.app;

import android.app.Application;
import android.util.Log;

import com.lsg.app.lib.ExceptionHandler;

public class LSGapp extends Application {
	@Override
	public void onCreate() {
		ExceptionHandler.init(this);
		Log.d("lsagpp", "LSGApp init");
		super.onCreate();
	}
}
