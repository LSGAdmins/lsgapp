package com.lsg.app.tasks;

import android.content.Context;
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

import com.lsg.app.Functions;
import com.lsg.app.R;

public class Exams extends ListFragment {
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup root,
			Bundle bundle) {
		return inflater.inflate(android.R.layout.list_content, null);
	}

	private SQLiteDatabase myDB;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		// header view, to add new exam
		LayoutInflater inflater = getLayoutInflater(savedInstanceState);
		View addItem = inflater.inflate(R.layout.tasks_listitem, null);
		((TextView) addItem.findViewById(R.id.mainText))
				.setText(R.string.newExam);
		getListView().addHeaderView(addItem);

		// open the database
		myDB = getActivity().getApplicationContext().openOrCreateDatabase(
				Functions.DB_NAME, Context.MODE_PRIVATE, null);
		// TODO select only those with valid date
		Cursor c = myDB.query(Functions.DB_EXAMS_TABLE, new String[] {
				Functions.DB_ROWID, Functions.DB_DATE, Functions.DB_TYPE,
				Functions.DB_TITLE, Functions.DB_CONTENT, Functions.DB_FACH },
				"", new String[] {}, null, null, null);
		getListView().setAdapter(
				new SimpleCursorAdapter(getActivity().getApplicationContext(),
						R.layout.exams_listitem, c, new String[] {
								Functions.DB_TITLE, Functions.DB_TYPE,
								Functions.DB_DATE, Functions.DB_FACH,
								Functions.DB_CONTENT }, new int[] { R.id.title,
								R.id.type, R.id.date, R.id.subjects,
								R.id.content }, 0));
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		if (position == 0)
			((TaskSelected) getActivity()).onTaskSelected(
					TaskSelected.TASK_EDIT_EXAMS, -1);
	}
}