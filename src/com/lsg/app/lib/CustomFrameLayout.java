package com.lsg.app.lib;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

public class CustomFrameLayout extends FrameLayout {
	public static final int INTERCEPT_SOURCE = 10807823;
	private View.OnTouchListener listener = null;
	public CustomFrameLayout(Context context) {
		super(context);
	}
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		boolean toReturn = true;
		if(listener != null)
			toReturn = listener.onTouch(this, ev);
		super.onInterceptTouchEvent(ev);
		return toReturn;
	}
	public void setOnTouchIntercept(View.OnTouchListener l) {
		listener = l;
	}
}
