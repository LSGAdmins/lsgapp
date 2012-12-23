package com.lsg.app.tasks;

import java.util.ArrayList;
import java.util.Calendar;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.lsg.app.Functions;
import com.lsg.app.R;
import com.lsg.app.lib.FragmentActivityCallbacks;
import com.lsg.app.timetable.TimeTable;
import com.lsg.app.widget.DateButton;

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
		Log.d("layout", Integer.valueOf(layout).toString());
		return inflater.inflate(layout, null);
	}
	private SQLiteDatabase myDB;
	private Exams.Descriptor eDesc;
	private String[] subjects;
	//the ActionBar default display options, save for reset
	private int displayOptions;
	// TODO remove this
	@TargetApi(11)
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		eDesc = new Exams.Descriptor();
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
						Calendar cal = Calendar.getInstance();
						DatePickerDialog datePicker = new DatePickerDialog(getActivity(),
								CreateEditFragment.this,
								cal.get(Calendar.YEAR),
								cal.get(Calendar.MONTH), cal
										.get(Calendar.DAY_OF_MONTH));
						datePicker.setTitle(
								R.string.pick_date);
						datePicker.show();
				alreadyCalled = false;
			}
		});
		((Spinner) getActivity().findViewById(R.id.subject)).setEnabled(false);
		subjects = new String[1];
		subjects[0] = getActivity().getString(R.string.no_subject);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, subjects);
		((Spinner) getActivity().findViewById(R.id.subject)).setAdapter(adapter);
		((Button) getActivity().findViewById(R.id.type)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				final CharSequence types[]  = new CharSequence[3];
				types[0] = getActivity().getString(R.string.big_test);
				types[1] = getActivity().getString(R.string.small_test);
				types[2] = getActivity().getString(R.string.other_test);
				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
				builder.setTitle(R.string.exam_type);
				builder.setItems(types, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
						case 0:
							eDesc.type = Exams.BIG_TEST;
							break;
						case 1:
							eDesc.type = Exams.SMALL_TEST;
							break;
						case 2:
							eDesc.type = Exams.OTHER_TEST;
							break;
						default:
							break;
						}
						((Button) getActivity().findViewById(R.id.type)).setText(types[which]);
					}
				});
				AlertDialog dialog = builder.create();
				dialog.show();
			}
		});
		myDB = ((FragmentActivityCallbacks) getActivity()).getDB();

        // TODO implement for pre-honeycomb
		LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View customActionBarView = inflater.inflate(
                R.layout.actionbar_buttons, null);
        customActionBarView.findViewById(R.id.actionbar_done).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // TODO save data
                    }
                });
        customActionBarView.findViewById(R.id.actionbar_discard).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // TODO ask user if he really wants to exit
                    }
                });

        final ActionBar actionBar = getActivity().getActionBar();
        /*actionBar.setDisplayOptions(
                ActionBar.DISPLAY_SHOW_CUSTOM,
                ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME
                        | ActionBar.DISPLAY_SHOW_TITLE);*/
        getActivity().getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setCustomView(customActionBarView, new ActionBar.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
		displayOptions = getActivity().getActionBar().getDisplayOptions();
	}
	/*workaround for wrong behaviour on jb
	 * TODO test this on HoneyComb / JellyBean mr1
	 */
	private boolean alreadyCalled = false;
	private Cursor subjectsCursor;
	private int timeTableId;
    public void onDateSet(DatePicker view, int year, int month, int day) {
    	if(Functions.getSDK() == 16 && !alreadyCalled) {
    		alreadyCalled = true;
    		return;
    	}
    	
		DateButton dateButton = ((DateButton) getActivity().findViewById(
				R.id.date));
		dateButton.setYear(year);
		dateButton.setMonth(month);
		dateButton.setDay(day);
		
		((Spinner) getActivity().findViewById(R.id.subject)).setEnabled(true);
		
        boolean weekend = false;
        if(dateButton.getCalendar().get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || dateButton.getCalendar().get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)
        	weekend = true;
        if(weekend)
        	Toast.makeText(getActivity(), "Ausgewähltes Datum ist am Wochenende!", Toast.LENGTH_LONG).show();
		if (((TaskSelected) getActivity()).getCurTask() == TaskSelected.TASK_EDIT_EXAMS) {
			TimeTable timeTable = new TimeTable(getActivity(), myDB);
			timeTable.setClass("", true);
			timeTable.updateAll();
			subjectsCursor = timeTable.getCursor(dateButton.getCalendar().get(Calendar.DAY_OF_WEEK) - 2);
			ArrayList<String> subjects = new ArrayList<String>();
			if (subjectsCursor != null) {
				for (subjectsCursor.moveToFirst(); !subjectsCursor
						.isAfterLast(); subjectsCursor.moveToNext()) {
					subjects.add(subjectsCursor.getString(subjectsCursor
							.getColumnIndex(Functions.DB_FACH)));
				}
			}
			subjects.add(getActivity().getString(R.string.other_subject));
			
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, subjects);
			((Spinner) getActivity().findViewById(R.id.subject)).setAdapter(adapter);
			
			
//			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//		    builder.setTitle(R.string.pick_subjects);
//		    AlertDialog dialog = builder.setItems(subjects, new DialogInterface.OnClickListener() {
//		               public void onClick(DialogInterface dialog, int which) {
//		               if(subjectsCursor != null && which < subjectsCursor.getCount()) {
//		            	   subjectsCursor.moveToPosition(which);
//		            	   Log.d("subject", subjectsCursor.getString(subjectsCursor.getColumnIndex(Functions.DB_FACH)));
//		            	   timeTableId = subjectsCursor.getInt(subjectsCursor.getColumnIndex(Functions.DB_ROWID));
//		               } else {
//		            	   timeTableId = -1;
//		            	   // TODO handle input of not existing subject
//							}
//		           }
//		    }).create();
//		    dialog.show();
        }
    }
	public void save(View v) {
		
	}
	// TODO implement for pre-honeycomb
	@TargetApi(11)
	@Override
	public void onStop() {
		getActivity().getActionBar().setDisplayOptions(displayOptions);
		getActivity().getActionBar().setDisplayOptions(0, ActionBar.DISPLAY_SHOW_CUSTOM);
		getActivity().getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME, ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE);
		getActivity().getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_TITLE);
		Log.d("fragment", "onstop");
		super.onStop();
	}
}
