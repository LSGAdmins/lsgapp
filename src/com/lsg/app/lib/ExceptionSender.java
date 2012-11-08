package com.lsg.app.lib;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class ExceptionSender extends IntentService {
	public static final String STACKTRACE = "stacktrace";
	public static final String INTENT     = "com.lsg.app.Exception";
	
	public ExceptionSender() {
		super("ExceptionSender");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Log.d("except", "hanlder");
		Bundle extras = intent.getExtras();
		Log.d("error", extras.getString(STACKTRACE));
		Log.d("asdf", "exceptionsender");
	}
}
