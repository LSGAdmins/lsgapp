package com.lsg.app;

import android.os.Handler;
import android.os.Message;

public class ServiceHandler {
	public static interface ServiceHandlerCallback {
		public void onFinishedService();
		public void onServiceError();
	}

	private final ServiceHandlerCallback callback;
	private Handler handler;

	public ServiceHandler(ServiceHandlerCallback callback) {
		this.callback = callback;
		handler = new Handler() {
			public void handleMessage(Message message) {
				if(message.arg1 == WorkerService.RESULT_OK)
					ServiceHandler.this.callback.onFinishedService();
				else
					ServiceHandler.this.callback.onServiceError();
			}
		};
	}
	public Handler getHandler() {
		return handler;
	}
}
