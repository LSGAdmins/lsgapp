package com.lsg.app;

import android.app.Activity;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

public class TitleCompat {
	public interface HomeCall {
		public void onHomePress();
	}

	private Activity activity;
	private boolean homeasup;

	TitleCompat(Activity act, boolean homeasup) {
		activity = act;
		this.homeasup = homeasup;
		if (Functions.getSDK() < 11) {
			act.requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
			act.getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
					R.layout.window_title);
		} else if (homeasup) {
			Advanced.homeasup(act);
		}
	}

	public void init(final HomeCall homecall) {
		if (Functions.getSDK() < 11) {
			activity.getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
					R.layout.window_title);
			(activity.findViewById(R.id.header))
					.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							homecall.onHomePress();
						}
					});
		}
	}
	public void setTitle(CharSequence title) {
		setTitle((String) title);
	}
	public void setTitle(String title) {
		((TextView) activity.findViewById(R.id.title)).setText(title);
	}
}
