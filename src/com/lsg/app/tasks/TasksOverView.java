package com.lsg.app.tasks;

import android.content.Context;
import android.database.Cursor;
import android.database.MergeCursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.lsg.app.R;

public class TasksOverView extends ListFragment {
	class TasksOverViewAdapter extends CursorAdapter {
		class TasksHolder {
			public TextView mainText;
		}

		public TasksOverViewAdapter(Context context, Cursor c) {
			super(context, c, false);
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View rowView;
			rowView = inflater.inflate(R.layout.tasks_listitem, null, true);
			TasksHolder holder = new TasksHolder();
			holder.mainText = (TextView) rowView.findViewById(R.id.mainText);
			rowView.setTag(holder);
			return rowView;
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			TasksHolder holder = (TasksHolder) view.getTag();
			if(cursor.getString(cursor.getColumnIndex("type")).equals(TaskButtonCursor.type)) {
				holder.mainText.setText(cursor.getString(cursor.getColumnIndex(TaskButtonCursor.columns[2])));
			} else {
				
			}
		}
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup root, Bundle bundle) {
		return inflater.inflate(android.R.layout.list_content, null);
	}
	private MergeCursor mergedCursor;
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		mergedCursor = new MergeCursor(new Cursor[] {new TaskButtonCursor()});

		setListAdapter(new TasksOverViewAdapter(getActivity().getApplicationContext(), mergedCursor));
	}
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		if(position < 3) {
			int task = 0;
			switch(position) {
			case 0:
				task = TaskSelected.TASK_EXAMS;
				break;
			case 1:
				task = TaskSelected.TASK_HOMEWORK;
				break;
			case 2:
				task = TaskSelected.TASK_GRADES;
				break;
			}
			((TaskSelected) getActivity()).onTaskSelected(task);
		}
	}
}