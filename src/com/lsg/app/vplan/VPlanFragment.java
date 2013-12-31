package com.lsg.app.vplan;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Messenger;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.lsg.app.Functions;
import com.lsg.app.InfoActivity;
import com.lsg.app.R;
import com.lsg.app.ServiceHandler;
import com.lsg.app.SubjectList;
import com.lsg.app.WorkerService;
import com.lsg.app.sqlite.LSGSQliteOpenHelper;




public class VPlanFragment extends Fragment implements OnQueryTextListener {
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.viewpager, null);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		adapter = new VPlanPagerAdapter(this);

		prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		setHasOptionsMenu(true);
	}

	private VPlanPagerAdapter adapter;
	private ViewPager pager;
	private SharedPreferences prefs;
	private boolean vplanEmpty;

	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		pager = (ViewPager) getActivity().findViewById(R.id.viewpager);
		pager.setAdapter(adapter);
		pager.setPageMargin(Functions.dpToPx(40, getActivity()));
		pager.setPageMarginDrawable(R.layout.viewpager_margin);

		getActivity().setTitle(R.string.vplan);
	}

	private MenuItem refresh;
	private boolean refreshing;

	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.vplan, menu);

		refresh = menu.findItem(R.id.refresh);
		if (vplanEmpty)
			updateVP();

		SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.search));
		searchView.setOnQueryTextListener(this);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.refresh:
			updateVP();
			return true;
		case R.id.subjects:
			Toast.makeText(getActivity(), getString(R.string.subjectlist_info),
					Toast.LENGTH_LONG).show();
			Intent subjects = new Intent(getActivity(), SubjectList.class);
			startActivity(subjects);
			return true;
		case R.id.info:
			Intent intent = new Intent(getActivity(), InfoActivity.class);
			intent.putExtra("type", "vplan");
			intent.putExtra("vplan_num",
					Integer.valueOf(adapter.cursor_all.getCount()).toString());
			intent.putExtra("mine_num",
					Integer.valueOf(adapter.cursor_mine.getCount()).toString());
			intent.putExtra("date", prefs.getString("vplan_date", "") + " / "
					+ prefs.getString("vplan_time", ""));
			intent.putExtra("vplan_num_teachers",
					Integer.valueOf(adapter.cursor_teachers.getCount())
							.toString());
			intent.putExtra(
					"date_teachers",
					prefs.getString("vplan_teacher_date", "") + " / "
							+ prefs.getString("vplan_teacher_time", ""));
			intent.putExtra("teacher", (prefs.getBoolean(
					Functions.RIGHTS_TEACHER, false) || prefs.getBoolean(
					Functions.RIGHTS_ADMIN, false)));
			startActivity(intent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		savedInstanceState.putBoolean("refreshing", refreshing);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		String table = (pager.getCurrentItem() != 2) ? LSGSQliteOpenHelper.DB_VPLAN_TABLE
				: LSGSQliteOpenHelper.DB_VPLAN_TEACHER;
		Functions.createContextMenu(menu, v, menuInfo, getActivity(), table);
	}

	public boolean onContextItemSelected(final MenuItem item) {
		String table = (pager.getCurrentItem() != 2) ? LSGSQliteOpenHelper.DB_VPLAN_TABLE
				: LSGSQliteOpenHelper.DB_VPLAN_TEACHER;
		return Functions.contextMenuSelect(item, getActivity(), adapter, table);
	}

	private static ServiceHandler hand;

	public void updateVP(boolean wait) {
		vplanEmpty = true;
	}

	@TargetApi(11)
	public void updateVP() {
		refreshing = true;
		MenuItemCompat.expandActionView(refresh);

		hand = new ServiceHandler(new ServiceHandler.ServiceHandlerCallback() {
			@Override
			public void onServiceError() {
				// TODO Auto-generated method stub
			}

			@Override
			public void onFinishedService() {
				MenuItemCompat.collapseActionView(refresh);
				refreshing = false;
				adapter.updateList();
			}
		});
		Handler handler = hand.getHandler();

		Intent intent = new Intent(getActivity(), WorkerService.class);
		// Create a new Messenger for the communication back
		Messenger messenger = new Messenger(handler);
		intent.putExtra(WorkerService.MESSENGER, messenger);
		intent.putExtra(WorkerService.WORKER_CLASS,
				VPlanUpdater.class.getCanonicalName());
		intent.putExtra(WorkerService.WHAT, WorkerService.UPDATE_ALL);
		getActivity().startService(intent);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		adapter.closeCursorsDB();
	}

	@Override
	public boolean onQueryTextChange(String arg0) {
		adapter.updateWhereCond(arg0);
		return false;
	}

	@Override
	public boolean onQueryTextSubmit(String arg0) {
		// TODO Auto-generated method stub
		return false;
	}
}
