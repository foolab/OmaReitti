package com.omareitti;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.Button;
import android.widget.EditText;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.view.View;
import android.app.TimePickerDialog;
import android.app.DatePickerDialog;
import java.util.Calendar;
import android.text.format.DateFormat;

public class DateTimeSelector extends LinearLayout
    implements TimePickerDialog.OnTimeSetListener,
	       DatePickerDialog.OnDateSetListener {

    private EditText mTimeEdit;
    private EditText mDateEdit;
    private Context mContext;
    private String mMinute, mHour, mDay, mMonth, mYear;

    public DateTimeSelector(Context context, AttributeSet attrs) {
	super(context, attrs);
	mContext = context;
    }

    public String getMinute() {
	return mMinute;
    }

    public String getHour() {
	return mHour;
    }

    public String getDay() {
	return mDay;
    }

    public String getMonth() {
	return mMonth;
    }

    public String getYear() {
	return mYear;
    }

    @Override
    protected void onFinishInflate() {
	super.onFinishInflate();
	((Activity)getContext()).getLayoutInflater().inflate(R.layout.date_time_selector, this);
	Button button = (Button)findViewById(R.id.nowButton);
	button.setOnClickListener(new View.OnClickListener() {
		public void onClick(View v) {
		    reset();
		}
	    });

	mTimeEdit = (EditText)findViewById(R.id.editTime);
	mTimeEdit.setOnClickListener(new OnClickListener() {
		public void onClick(View v) {
		    Calendar c = Calendar.getInstance();
		    int hour = c.get(Calendar.HOUR_OF_DAY);
		    int minute = c.get(Calendar.MINUTE);

		    TimePickerDialog dlg =
			new TimePickerDialog(mContext, DateTimeSelector.this, hour,
					     minute, true
					     /* DateFormat.is24HourFormat(mContext) */);
		    dlg.show();
		}
	    });

	mDateEdit = (EditText)findViewById(R.id.editDate);
	mDateEdit.setOnClickListener(new OnClickListener() {
		public void onClick(View v) {
		    Calendar c = Calendar.getInstance();
		    int year = c.get(Calendar.YEAR);
		    int month = c.get(Calendar.MONTH);
		    int day = c.get(Calendar.DAY_OF_MONTH);
		    DatePickerDialog dlg =
			new DatePickerDialog(mContext, DateTimeSelector.this, year, month, day);
		    dlg.show();
		}
	    });

	reset();
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
	mHour = String.format("%02d", hourOfDay);
	mMinute = String.format("%02d", minute);
	mTimeEdit.setText(String.format("%s:%s", mHour, mMinute));
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
	    // Do something with the date chosen by the user
	mYear = String.format("%d", year);
	mMonth = String.format("%02d", month + 1);
	mDay = String.format("%02d", day);
	mDateEdit.setText(String.format("%s.%s.%s", mDay, mMonth, mYear));
    }

    private void reset() {
	Calendar c = Calendar.getInstance();
	onTimeSet(null, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));
	onDateSet(null, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
    }
}
