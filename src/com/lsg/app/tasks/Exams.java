package com.lsg.app.tasks;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lsg.app.R;

class Exams extends ListFragment {
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup root,
			Bundle bundle) {
		return inflater.inflate(android.R.layout.list_content, null);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		// header view, to add new exam
		LayoutInflater inflater = getLayoutInflater(savedInstanceState);
		View addItem = inflater.inflate(R.layout.tasks_listitem, null);
		((TextView) addItem.findViewById(R.id.mainText))
				.setText(R.string.newExam);
		getListView().addHeaderView(addItem);

		Cursor c = new TaskButtonCursor();
		getListView().setAdapter(
				new SimpleCursorAdapter(getActivity().getApplicationContext(),
						R.layout.tasks_listitem, c, new String[] { "text" },
						new int[] { R.id.mainText }));
	}
}