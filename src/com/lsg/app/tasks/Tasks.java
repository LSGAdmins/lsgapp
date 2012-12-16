package com.lsg.app.tasks;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.MenuItem;

import com.lsg.app.Functions;
import com.lsg.app.R;
import com.lsg.app.lib.SlideMenu;
import com.lsg.app.lib.TitleCompat;
import com.lsg.app.lib.TitleCompat.HomeCall;

public class Tasks extends FragmentActivity implements TaskSelected, HomeCall {
	class HomeWork extends Fragment {

	}

	class Grades extends Fragment {

	}

	private SlideMenu slidemenu;
	private TitleCompat titlebar;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		titlebar = new TitleCompat(this, true);
		titlebar.init(this);
		titlebar.setTitle(getTitle());
		setContentView(R.layout.fragment_main);
		slidemenu = new SlideMenu(this, Tasks.class);
		FragmentTransaction fragmentTransaction = getSupportFragmentManager()
				.beginTransaction();
		Fragment fragment = new TasksOverView();
		fragmentTransaction.add(R.id.fragmentContainer, fragment);
		fragmentTransaction.commit();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case android.R.id.home:
			onHomePress();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onTaskSelected(int taskId) {
		Log.d("task", Integer.valueOf(taskId).toString());
		Fragment fragment;
		switch (taskId) {
		case TaskSelected.TASK_EXAMS:
			fragment = new Exams();
			break;
		case TaskSelected.TASK_GRADES:
			fragment = new Grades();
			break;
		case TaskSelected.TASK_HOMEWORK:
			fragment = new HomeWork();
			break;
		default:
			fragment = new TasksOverView();
			break;
		}
		FragmentTransaction fragmentTransaction = getSupportFragmentManager()
				.beginTransaction();
		fragmentTransaction.replace(R.id.fragmentContainer, fragment);
	    fragmentTransaction.addToBackStack(null); 
		fragmentTransaction.commit();
	}

	@Override
	public void onHomePress() {
		slidemenu.show();
	}
}