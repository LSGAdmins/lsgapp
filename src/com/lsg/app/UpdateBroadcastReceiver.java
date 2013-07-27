package com.lsg.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.lsg.app.vplan.VPlanUpdater;

public class UpdateBroadcastReceiver extends BroadcastReceiver {
	/*public static class ProgressThread extends Thread {
		Handler handler;
		Context context;
		boolean notify;
		ProgressThread(Handler h, Context c) {
			handler = h;
			context = c;
			notify = true;
			}
		ProgressThread(Handler h, Context c, boolean notify) {
			handler = h;
			context = c;
			this.notify = notify;
			}
		public void run() {
			Looper.prepare();
			//boolean update = Functions.refreshVPlan(context, handler, notify);
			
			Message msg = handler.obtainMessage();
			msg.arg1 = 3;
			msg.arg2 = R.string.loading_class;
			handler.sendMessage(msg);
			
			Functions.getClass(context);
			
			msg = handler.obtainMessage();
			msg.arg1 = 3;
			msg.arg2 = R.string.loading_subjects;
			handler.sendMessage(msg);
			
			Functions.updateSubjectList(context, handler, notify);
			
			msg = handler.obtainMessage();
			msg.arg1 = 3;
			msg.arg2 = R.string.loading_timetable;
			handler.sendMessage(msg);
			
			Functions.updateTimetable(context, handler, notify);
			
			msg = handler.obtainMessage();
			msg.arg1 = 3;
			msg.arg2 = R.string.loading_events;
			handler.sendMessage(msg);
			Functions.refreshEvents(context, handler);
			
			msg = handler.obtainMessage();
			msg.arg1 = 1;
			/*if(!update)
				msg.arg1 = 2;
			handler.sendMessage(msg);
			}
		}*/
	public static class VPupdate extends Thread {
		Context context;
		VPupdate(Context c) {
			context = c;
		}
		public void run() {
			VPlanUpdater vpup = new VPlanUpdater(context);
			String res[] = vpup.updatePupils();
			Log.d(res[0], res[1]);
			res = vpup.updateTeachers();
			Log.d(res[0], res[1]);
		}
	}
	private static class IDSender extends Thread {
		Context context;
		String registration_id;
		IDSender(Context c, String id) {
			context = c;
			registration_id = id;
		}
		public void run() {
			Functions.sendClientId(registration_id, context);
		}
	}
	@Override
	public void onReceive(final Context context, Intent intent) {
		try {
			String action = intent.getAction();
			if (action.equals("com.google.android.c2dm.intent.REGISTRATION")) {
				handleRegistration(context, intent);
				} else if (action.equals("com.google.android.c2dm.intent.RECEIVE")) {
					handleMessage(context, intent);
					} else if(action.equals("update_vplan")) {
						updateVP(context);
						}
			Functions.setAlarm(context);
			} catch (Exception e) {
				Log.d("error", e.getMessage());
				e.printStackTrace();
				}
		}
	private void updateVP(Context context) {
		VPupdate vp = new VPupdate(context);
		vp.start();
	}
	private void handleRegistration(Context context, Intent intent) {
		    String registration = intent.getStringExtra("registration_id"); 
		    if (intent.getStringExtra("error") != null) {
		        Log.w(Functions.TAG, "c2dm registration failed");
		    } else if (intent.getStringExtra("unregistered") != null) {
		    	Log.d(Functions.TAG, "c2dm unregistered");
		    } else if (registration != null) {
		    	final String registrationId = intent.getStringExtra("registration_id");
		    	IDSender idsend = new IDSender(context, registrationId);
		    	idsend.start();
		    }
		}
	private void handleMessage(Context context, Intent intent) {
		final String action = intent.getStringExtra("action");
		Log.d("c2dm message", action);
		if(action.equals("update_vplan"))
			updateVP(context);
		}
	}