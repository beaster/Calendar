package com.ostrovok.calendar;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;

import com.ostrovok.calendar.view.CalendarView;

public class MyCalendarActivity extends Activity {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(new CalendarView(this));
	}
}
