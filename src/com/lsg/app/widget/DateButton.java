package com.lsg.app.widget;

import java.util.Calendar;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;

public class DateButton extends Button {
	private Calendar cal = Calendar.getInstance();

	public DateButton(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void setYear(int year) {
		cal.set(Calendar.YEAR, year);
		updateText();
	}

	public void setMonth(int month) {
		cal.set(Calendar.MONTH, month);
		updateText();
	}

	public void setDay(int dayOfMonth) {
		cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
		updateText();
	}

	private void updateText() {
		setText(cal.get(Calendar.DAY_OF_MONTH) + "." + cal.get(Calendar.MONTH)
				+ "." + cal.get(Calendar.YEAR));
	}

	public Calendar getCalendar() {
		return cal;
	}
}
