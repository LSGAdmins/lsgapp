package com.lsg.app.tasks;

import java.util.Calendar;

import android.app.DatePickerDialog;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import com.lsg.app.Functions;
import com.lsg.app.R;
import com.lsg.app.lib.FragmentActivityCallbacks;

public class CreateEditFragment extends Fragment implements DatePickerDialog.OnDateSetListener{
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup root,
			Bundle bundle) {
		int layout;
		switch(((TaskSelected) getActivity()).getCurTask()) {
		case TaskSelected.TASK_EDIT_EXAMS:
			layout = R.layout.edit_exams;
			break;
			default:
				return null;
		}
		return inflater.inflate(layout, null);
	}
	private SQLiteDatabase myDB;
	private Calendar cal;
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		switch(((TaskSelected) getActivity()).getCurTask()) {
		case TaskSelected.TASK_EDIT_EXAMS:
			if(((TaskSelected) getActivity()).getCurId() != -1) {
				((TextView) getActivity().findViewById(R.id.title)).setText(R.string.editExam);
			}
		}
		((Button) getActivity().findViewById(R.id.date)).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				cal = Calendar.getInstance();
				(new DatePickerDialog(getActivity(), CreateEditFragment.this, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))).show();
			}
		});
		myDB = ((FragmentActivityCallbacks) getActivity()).getDB();
	}

    public void onDateSet(DatePicker view, int year, int month, int day) {
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, day);
        boolean weekend = false;
        if(cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)
        	weekend = true;
        if(weekend)
        	Toast.makeText(getActivity(), "Ausgewähltes Datum ist am Wochenende!", Toast.LENGTH_LONG).show();
		if (((TaskSelected) getActivity()).getCurTask() == TaskSelected.TASK_EDIT_EXAMS) {
			Cursor res = myDB.query(
					Functions.DB_TIME_TABLE,
					new String[] { Functions.DB_ROWID, Functions.DB_RAW_FACH,
							Functions.DB_FACH },
					Functions.DB_DAY + "=?",
					new String[] { Integer.valueOf(
							cal.get(Calendar.DAY_OF_WEEK) - 2).toString() },
					null, null, null);
			for(res.moveToFirst(); !res.isAfterLast(); res.moveToNext()) {
				Log.d("subject", res.getString(res.getColumnIndex(Functions.DB_FACH)));
			}
        }
        Log.d("dayofweek", Integer.valueOf(cal.get(Calendar.DAY_OF_WEEK)).toString());
    }
	public void save(View v) {
		
	}
}
