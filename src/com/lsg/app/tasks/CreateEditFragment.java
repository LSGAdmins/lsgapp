package com.lsg.app.tasks;

import java.util.ArrayList;
import java.util.Calendar;

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
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.lsg.app.Functions;
import com.lsg.app.R;
import com.lsg.app.interfaces.FragmentActivityCallbacks;
import com.lsg.app.lib.AdvancedWrapper;
import com.lsg.app.lib.LSGApplication;
import com.lsg.app.sqlite.LSGSQliteOpenHelper;
import com.lsg.app.timetable.TimeTable;
import com.lsg.app.widget.DateButton;

public class CreateEditFragment extends Fragment implements
		DatePickerDialog.OnDateSetListener {
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup root,
			Bundle bundle) {
		int layout;
		switch (((TaskSelected) getActivity()).getCurTask()) {
		case TaskSelected.TASK_EDIT_EXAMS:
			layout = R.layout.edit_exams;
			break;
		default:
			return null;
		}
		return inflater.inflate(layout, null);
	}

	private SQLiteDatabase myDB;
	private ArrayList<String> subjects = new ArrayList<String>();

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		((FragmentActivityCallbacks) getActivity()).getSlideMenu().tabletHide();

		switch (((TaskSelected) getActivity()).getCurTask()) {
		case TaskSelected.TASK_EDIT_EXAMS:
			if (((TaskSelected) getActivity()).getCurId() != -1) {
				((TextView) getActivity().findViewById(R.id.title))
						.setText(R.string.editExam);
			}
		}
		((Button) getActivity().findViewById(R.id.date))
				.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						Calendar cal = Calendar.getInstance();
						DatePickerDialog datePicker = new DatePickerDialog(
								getActivity(), CreateEditFragment.this, cal
										.get(Calendar.YEAR), cal
										.get(Calendar.MONTH), cal
										.get(Calendar.DAY_OF_MONTH));
						datePicker.setTitle(R.string.pick_date);
						datePicker.show();
						alreadyCalled = false;
					}
				});
		((Spinner) getActivity().findViewById(R.id.subject)).setEnabled(false);
		subjects.clear();
		subjects.add(getActivity().getString(R.string.no_subject));
		ArrayAdapter<String> dateAdapter = new ArrayAdapter<String>(
				getActivity(), android.R.layout.simple_spinner_dropdown_item,
				subjects);
		((Spinner) getActivity().findViewById(R.id.subject))
				.setAdapter(dateAdapter);
		((Spinner) getActivity().findViewById(R.id.subject)).setEnabled(false);

		String[] types = new String[3];
		types[0] = getActivity().getString(R.string.big_test);
		types[1] = getActivity().getString(R.string.small_test);
		types[2] = getActivity().getString(R.string.other_test);
		ArrayAdapter<String> typeAdapter = new ArrayAdapter<String>(
				getActivity(), android.R.layout.simple_spinner_dropdown_item,
				types);
		((Spinner) getActivity().findViewById(R.id.type))
				.setAdapter(typeAdapter);
		myDB = LSGApplication.getSqliteDatabase();
		if (savedInstanceState != null) {
			if (savedInstanceState.getSerializable("date") != null) {
				// get DateButton & set date
				DateButton dateButton = ((DateButton) getActivity()
						.findViewById(R.id.date));
				dateButton.setCalendar((Calendar) savedInstanceState
						.getSerializable("date"));
				// set Adapter for subjects spinner / enable
				subjects = getSubjectSpinnerData(dateButton.getCalendar());
				dateAdapter = new ArrayAdapter<String>(getActivity(),
						android.R.layout.simple_spinner_dropdown_item, subjects);
				((Spinner) getActivity().findViewById(R.id.subject))
						.setAdapter(dateAdapter);
				((Spinner) getActivity().findViewById(R.id.subject))
						.setEnabled(true);
				// set selected subject
				((Spinner) getActivity().findViewById(R.id.subject))
						.setSelection(savedInstanceState
								.getInt("selectedSubject"));
			}
		}
	}

	@Override
	public void onResume() {
		LayoutInflater inflater = (LayoutInflater) getActivity()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View customActionBarView = inflater.inflate(
				R.layout.actionbar_buttons, null);
		customActionBarView.findViewById(R.id.actionbar_done)
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						String title = ((EditText) getActivity().findViewById(
								R.id.editTitle)).getText().toString();
						DateButton dateButton = ((DateButton) getActivity()
								.findViewById(R.id.date));
						if (!dateButton.getModified()) {
							Toast.makeText(
									getActivity(),
									getActivity().getString(
											R.string.exam_not_complete),
									Toast.LENGTH_LONG).show();
							return;
						}
						Calendar date = dateButton.getCalendar();

						subjectsCursor.moveToPosition(((Spinner) getActivity()
								.findViewById(R.id.subject))
								.getSelectedItemPosition());
						String subject = subjectsCursor.getString(subjectsCursor
								.getColumnIndex(LSGSQliteOpenHelper.DB_RAW_FACH));
						String type;
						switch (((Spinner) getActivity()
								.findViewById(R.id.type))
								.getSelectedItemPosition()) {
						case 0:
							type = Exams.BIG_TEST;
							break;
						case 1:
							type = Exams.SMALL_TEST;
							break;
						case 2:
						default:
							type = Exams.OTHER_TEST;
							break;
						}
						String learningMatters = ((EditText) getActivity()
								.findViewById(R.id.editLearningMatter))
								.getText().toString();
						boolean locked = ((CheckBox) getActivity()
								.findViewById(R.id.checkbox_lock_exam))
								.isChecked();
						String notes = ((EditText) getActivity().findViewById(
								R.id.editNotes)).getText().toString();

						Log.d("title", title);
						Log.d("date", date.toString());
						Log.d("subject", subject);
						Log.d("type", type);
						Log.d("locked", Boolean.valueOf(locked).toString());
						Log.d("learningMatters", learningMatters);
						Log.d("notes", notes);
					}
				});
		customActionBarView.findViewById(R.id.actionbar_discard)
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						onCancel();
					}
				});
		if (Functions.getSDK() < 11)
			((LinearLayout) getActivity().findViewById(R.id.actionButtons))
					.addView(customActionBarView,
							new LinearLayout.LayoutParams(
									ViewGroup.LayoutParams.MATCH_PARENT,
									ViewGroup.LayoutParams.MATCH_PARENT));
		else {
			AdvancedWrapper adv = new AdvancedWrapper();
			adv.setActionBarCustomView(getActivity(), customActionBarView);
		}
		super.onResume();
	}

	public void onCancel() {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setMessage("Wirklich abbrechen?");
		builder.setPositiveButton(R.string.ok,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						getActivity().getSupportFragmentManager()
								.popBackStack();
					}
				});
		builder.setNegativeButton(R.string.no, null);
		AlertDialog alert = builder.create();
		alert.show();
	}

	private Cursor subjectsCursor;

	public ArrayList<String> getSubjectSpinnerData(Calendar cal) {
		TimeTable timeTable = new TimeTable(getActivity(), myDB);
		timeTable.setClass("", true);
		timeTable.updateAll();
		subjectsCursor = timeTable.getCursor(cal.get(Calendar.DAY_OF_WEEK) - 2);
		ArrayList<String> subjects = new ArrayList<String>();
		if (subjectsCursor != null) {
			for (subjectsCursor.moveToFirst(); !subjectsCursor.isAfterLast(); subjectsCursor
					.moveToNext()) {
				subjects.add(subjectsCursor.getString(subjectsCursor
						.getColumnIndex(LSGSQliteOpenHelper.DB_FACH)));
			}
		}
		subjects.add(getActivity().getString(R.string.other_subject));
		return subjects;
	}

	/*
	 * workaround for wrong behaviour on jb TODO test this on HoneyComb /
	 * JellyBean mr1
	 */
	private boolean alreadyCalled = false;

	public void onDateSet(DatePicker view, int year, int month, int day) {
		if (Functions.getSDK() == 16 && !alreadyCalled) {
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
		if (dateButton.getCalendar().get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY
				|| dateButton.getCalendar().get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)
			weekend = true;
		if (weekend)
			Toast.makeText(getActivity(),
					"Ausgewähltes Datum ist am Wochenende!", Toast.LENGTH_LONG)
					.show();
		if (((TaskSelected) getActivity()).getCurTask() == TaskSelected.TASK_EDIT_EXAMS) {
			subjects.clear();
			subjects = getSubjectSpinnerData(dateButton.getCalendar());

			ArrayAdapter<String> adapter = new ArrayAdapter<String>(
					getActivity(),
					android.R.layout.simple_spinner_dropdown_item, subjects);
			((Spinner) getActivity().findViewById(R.id.subject))
					.setAdapter(adapter);
			// TODO handle selection of non-existing subject -> EditText for
			// name
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		DateButton dateButton = ((DateButton) getActivity().findViewById(
				R.id.date));
		if (dateButton.getModified()) {
			outState.putSerializable("date", dateButton.getCalendar());
			outState.putInt("selectedSubject", ((Spinner) getActivity()
					.findViewById(R.id.subject)).getSelectedItemPosition());
		} else {
			outState.putSerializable("date", null);
			outState.putInt("selectedSubject", 0);
		}

		super.onSaveInstanceState(outState);
	}

	@Override
	public void onStop() {
		if (Functions.getSDK() > 11) {
			AdvancedWrapper adv = new AdvancedWrapper();
			adv.removeActionBarCustomView(getActivity(), !getActivity()
					.getResources().getBoolean(R.bool.isTablet));
		}
		((FragmentActivityCallbacks) getActivity()).getSlideMenu().tabletShow();
		super.onStop();
	}
}
