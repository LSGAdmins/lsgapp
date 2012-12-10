package com.lsg.app;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gcm.GCMBaseIntentService;

public class GCMIntentService extends GCMBaseIntentService {
	public GCMIntentService() {
		super(Functions.GCM_ID);
	}
	@Override
	protected void onError(Context context, String error) {
		Log.w("LSGäpp", "gcm error: " + error);
	}

	@Override
	protected void onMessage(Context context, Intent data) {
		Log.d("data", data.getExtras().toString());
		Bundle extras = data.getExtras();
		String action = extras.getString("action");
		if (action.equals("timetable")) {
			int what = Integer.valueOf(extras.getString("what")).intValue();
			Intent intent = new Intent(this, WorkerService.class);
			// Create a new Messenger for the communication back
			intent.putExtra(WorkerService.WORKER_CLASS,
					TimeTable.class.getCanonicalName());
			intent.putExtra(WorkerService.WHAT, what);
			startService(intent);
		} else if (action.equals("vplan")) {
			int what = Integer.valueOf(extras.getString("what")).intValue();
			Intent intent = new Intent(this, WorkerService.class);
			// Create a new Messenger for the communication back
			intent.putExtra(WorkerService.WORKER_CLASS,
					VPlan.class.getCanonicalName());
			intent.putExtra(WorkerService.WHAT, what);
			startService(intent);
		} else if (action.equals("events")) {
			int what = Integer.valueOf(extras.getString("what")).intValue();
			Intent intent = new Intent(this, WorkerService.class);
			// Create a new Messenger for the communication back
			intent.putExtra(WorkerService.WORKER_CLASS,
					Events.class.getCanonicalName());
			intent.putExtra(WorkerService.WHAT, what);
			startService(intent);
		}
	}

	@Override
	protected void onRegistered(Context context, String regId) {
		Functions.sendClientId(regId, context);
	}

	@Override
	protected void onUnregistered(Context context, String regId) {
		Functions.sendClientId(regId, context, false);
	}

}
