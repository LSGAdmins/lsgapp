package com.lsg.app.widget;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

public class CustomFrameLayout extends FrameLayout {
	private View.OnTouchListener interceptListener = null;
	public CustomFrameLayout(Context context) {
		super(context);
	}
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		boolean toReturn = false;
		if(interceptListener != null) {
			//child should return true, if this FrameLayout should handle onTouch
			toReturn = interceptListener.onTouch(this, ev);
		}
		super.onInterceptTouchEvent(ev);
		return toReturn;
	}
	public void setOnTouchIntercept(View.OnTouchListener l) {
		interceptListener = l;
	}
}
