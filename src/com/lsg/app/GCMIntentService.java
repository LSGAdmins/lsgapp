package com.lsg.app;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gcm.GCMBaseIntentService;

public class GCMIntentService extends GCMBaseIntentService {

	@Override
	protected void onError(Context arg0, String arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onMessage(Context arg0, Intent arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onRegistered(Context context, String regId) {
		Functions.sendClientId(regId, context);
	}

	@Override
	protected void onUnregistered(Context context, String regId) {
		Log.d("unregister", regId);
		Functions.sendClientId(regId, context, true);
	}

}
