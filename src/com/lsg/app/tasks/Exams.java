package com.lsg.app.tasks;

import java.util.Calendar;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.lsg.app.R;
import com.lsg.app.interfaces.FragmentActivityCallbacks;
import com.lsg.app.lib.LSGApplication;
import com.lsg.app.lib.TitleCompat;
import com.lsg.app.sqlite.LSGSQliteOpenHelper;

public class Exams extends ListFragment {

	public static class Descriptor {
		public String title;
		public Calendar date;
		public String rawSubject;
		public String type;
		public String learningMatters;
		public boolean isLocked;
		public boolean notes;
	}

	public static final String BIG_TEST = "big_test";
	public static final String SMALL_TEST = "small_test";
	public static final String OTHER_TEST = "other_test";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup root,
			Bundle bundle) {
		return inflater.inflate(android.R.layout.list_content, null);
	}

	private SQLiteDatabase myDB;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		//set Title
		getActivity().setTitle(R.string.exams);
		TitleCompat titleCompat = ((FragmentActivityCallbacks) getActivity()).getTitlebar();
		titleCompat.setTitle(getActivity().getTitle());

		// header view, to add new exam
		LayoutInflater inflater = getLayoutInflater(savedInstanceState);
		View addItem = inflater.inflate(R.layout.tasks_listitem, null);
		((TextView) addItem.findViewById(R.id.mainText))
				.setText(R.string.newExam);
		getListView().addHeaderView(addItem);

		// open the database
		myDB = LSGApplication.getSqliteDatabase();
		// TODO select only those with valid date
		Cursor c = myDB.query(LSGSQliteOpenHelper.DB_EXAMS_TABLE, new String[] {
				LSGSQliteOpenHelper.DB_ROWID, LSGSQliteOpenHelper.DB_DATE,
				LSGSQliteOpenHelper.DB_TYPE, LSGSQliteOpenHelper.DB_TITLE,
				LSGSQliteOpenHelper.DB_LEARNING_MATTER,
				LSGSQliteOpenHelper.DB_FACH }, "", new String[] {}, null, null,
				null);
		getListView().setAdapter(
				new SimpleCursorAdapter(getActivity().getApplicationContext(),
						R.layout.exams_listitem, c, new String[] {
								LSGSQliteOpenHelper.DB_TITLE,
								LSGSQliteOpenHelper.DB_TYPE,
								LSGSQliteOpenHelper.DB_DATE,
								LSGSQliteOpenHelper.DB_FACH,
								LSGSQliteOpenHelper.DB_LEARNING_MATTER },
						new int[] { R.id.title, R.id.type, R.id.date,
								R.id.subjects, R.id.content }, 0));
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		if (position == 0)
			((TaskSelected) getActivity()).onTaskSelected(
					TaskSelected.TASK_EDIT_EXAMS, -1);
	}
}