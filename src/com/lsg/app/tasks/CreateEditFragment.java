package com.lsg.app.tasks;

import java.util.Calendar;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;

import com.lsg.app.R;

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
	private int year = 2012;
	private int month = 0;
	private int day = 0;
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
				(new DatePickerDialog(getActivity(), CreateEditFragment.this, CreateEditFragment.this.year, CreateEditFragment.this.month, CreateEditFragment.this.day)).show();
			}
		});
	}

    public void onDateSet(DatePicker view, int year, int month, int day) {
        // Do something with the date chosen by the user
    }
	public void save(View v) {
		
	}
}
