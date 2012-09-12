package com.lsg.app;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gcm.GCMBaseIntentService;

public class GCMIntentService extends GCMBaseIntentService {

	@Override
	protected void onError(Context context, String error) {
		Log.w("LSGäpp", "gcm error: " + error);
	}

	@Override
	protected void onMessage(Context context, Intent data) {
		Log.d("data", data.getExtras().toString());
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
