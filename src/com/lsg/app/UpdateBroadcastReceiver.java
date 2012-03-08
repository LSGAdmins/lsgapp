package com.lsg.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class UpdateBroadcastReceiver extends BroadcastReceiver {
	public static class ProgressThread extends Thread {
		Handler handler;
		Context context;
		ProgressThread(Handler h, Context c) {
			handler = h;
			context = c;
			}
		public void run() {
			Looper.prepare();
			boolean update = Functions.refreshVPlan(context, handler);
			
			Message msg = handler.obtainMessage();
			msg.arg1 = 3;
			msg.arg2 = R.string.loading_class;
			handler.sendMessage(msg);
			
			Functions.getClass(context);
			
			msg = handler.obtainMessage();
			msg.arg1 = 3;
			msg.arg2 = R.string.loading_subjects;
			handler.sendMessage(msg);
			
			Functions.updateSubjectList(context, handler);
			
			msg = handler.obtainMessage();
			msg.arg1 = 1;
			if(!update)
				msg.arg1 = 2;
			handler.sendMessage(msg);
			}
		}
	@Override
	public void onReceive(final Context context, Intent intent) {
		try {
			Log.d("asdf", "UpdateBroadcastReceiver");
			String action = intent.getAction();
			Log.d("asdfaction", action);
			Handler h = new Handler();
			if(action.equals("update_vplan")) {
				ProgressThread progress = new ProgressThread(h, context);
				progress.start();
			}
			Functions.setAlarm(context);
			} catch (Exception e) {
				Log.d("error", e.getMessage());
				e.printStackTrace();
				}
		}
	}
