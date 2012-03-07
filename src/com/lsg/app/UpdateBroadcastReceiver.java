package com.lsg.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

public class UpdateBroadcastReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		try {
			String action = intent.getAction();
			Log.d("action", action);
			Bundle bundle = intent.getExtras();
			String message = bundle.getString("action");
			Log.d("broadcast", message);
			Handler h = new Handler();
			if(message.equals("update")) {
				Functions.refreshVPlan(context, h);
				Functions.getClass(context);
				Functions.updateSubjectList(context, h);
			}
			Functions.setAlarm(context);
			} catch (Exception e) {
				Log.d(this.toString(), "error in getting data");
				e.printStackTrace();
				}
		}
	}
