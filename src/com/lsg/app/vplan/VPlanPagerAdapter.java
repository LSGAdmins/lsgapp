package com.lsg.app.vplan;

import com.lsg.app.Functions;
import com.lsg.app.R;
import com.lsg.app.interfaces.SQLlist;
import com.lsg.app.lib.LSGApplication;
import com.lsg.app.sqlite.LSGSQliteOpenHelper;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class VPlanPagerAdapter extends PagerAdapter implements SQLlist,
		TextWatcher {
	private String[] where_conds = new String[4];
	private String[] where_conds_events = new String[6];
	private String[] exclude_subjects = new String[4];
	private String[] titles = new String[3];
	private final SQLiteDatabase myDB;
	public Cursor cursor_all;
	public Cursor cursor_mine;
	public Cursor cursor_teachers;
	private VPlanAdapter vadapter_all;
	private VPlanAdapter vadapter_mine;
	private VPlanAdapter vadapter_teachers;
	private String exclude_cond;
	private String include_cond;
	private VPlanFragment act;
	private final Context context;
	private final SharedPreferences prefs;

	public VPlanPagerAdapter(VPlanFragment act) {
		where_conds[0] = "%";
		where_conds[1] = "%";
		where_conds[2] = "%";
		where_conds[3] = "%";
		where_conds_events[0] = "%";
		where_conds_events[1] = "%";
		where_conds_events[2] = "%";
		where_conds_events[3] = "%";
		where_conds_events[4] = "%";
		where_conds_events[5] = "%";
		titles[0] = act.getString(R.string.vplan_mine);
		titles[1] = act.getString(R.string.vplan_pupils);
		titles[2] = act.getString(R.string.vplan_teachers);
		prefs = PreferenceManager
				.getDefaultSharedPreferences(act.getActivity());
		exclude_subjects[1] = (prefs.getString(Functions.GENDER, "")
				.equals("m")) ? "Sw" : "Sm";
		if (prefs.getString(Functions.RELIGION, "")
				.equals(Functions.KATHOLISCH)) {
			exclude_subjects[2] = Functions.EVANGELISCH;
			exclude_subjects[3] = Functions.ETHIK;
		} else if (prefs.getString(Functions.RELIGION, "").equals(
				Functions.EVANGELISCH)) {
			exclude_subjects[2] = Functions.KATHOLISCH;
			exclude_subjects[3] = Functions.ETHIK;
		} else {
			exclude_subjects[2] = Functions.KATHOLISCH;
			exclude_subjects[3] = Functions.EVANGELISCH;
		}
		context = (Context) act.getActivity();
		this.act = act;

		myDB = LSGApplication.getSqliteDatabase();
		updateCondLists();

		SQLiteStatement num_rows = myDB
				.compileStatement("SELECT COUNT(*) FROM "
						+ LSGSQliteOpenHelper.DB_VPLAN_TABLE);
		long count = num_rows.simpleQueryForLong();
		SQLiteStatement num_rows_2 = myDB
				.compileStatement("SELECT COUNT(*) FROM "
						+ LSGSQliteOpenHelper.DB_VPLAN_TEACHER);
		long count2 = num_rows_2.simpleQueryForLong();
		if (count == 0 && count2 == 0)
			act.updateVP();
		num_rows.close();
		vadapter_mine = new VPlanAdapter(context, cursor_mine,
				prefs.getBoolean(Functions.RIGHTS_TEACHER, false));
		vadapter_all = new VPlanAdapter(context, cursor_all, false);
		vadapter_teachers = new VPlanAdapter(context, cursor_teachers, true);
		updateCursor();
	}

	@Override
	public int getCount() {
		if (prefs.getBoolean(Functions.RIGHTS_TEACHER, false)
				|| prefs.getBoolean(Functions.RIGHTS_ADMIN, false))
			return 3;
		else
			return 2;
	}

	public String getTitle(int pos) {
		return titles[pos];
	}

	@Override
	public CharSequence getPageTitle(int position) {
		return titles[position];
	}

	@Override
	public Object instantiateItem(View pager, int position) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		LinearLayout lay = (LinearLayout) inflater.inflate(R.layout.list, null);
		ListView lv = (ListView) lay.findViewById(android.R.id.list);
		// set header search bar
		if (Functions.getSDK() < 11) {
			View search = inflater.inflate(R.layout.search, null);
			EditText searchEdit = (EditText) search
					.findViewById(R.id.search_edit);
			searchEdit.addTextChangedListener(this);
			lv.addHeaderView(search);
		}
		if (position == 0)
			lv.setAdapter(vadapter_mine);
		if (position == 1)
			lv.setAdapter(vadapter_all);
		if (position == 2)
			lv.setAdapter(vadapter_teachers);
		Functions.styleListView(lv, context);
		act.registerForContextMenu(lv);
		lv.setEmptyView(lay.findViewById(R.id.list_view_empty));
		if (position == 0)
			((TextView) lay.findViewById(R.id.list_view_empty))
					.setText(R.string.vplan_mine_empty);
		if (position == 1)
			((TextView) lay.findViewById(R.id.list_view_empty))
					.setText(R.string.vplan_empty);
		if (position == 2)
			((TextView) lay.findViewById(R.id.list_view_empty))
					.setText(R.string.vplan_empty);
		((ViewPager) pager).addView(lay, 0);
		return lay;
	}

	public void updateCondLists() {
		exclude_cond = new String();
		Cursor exclude = myDB.query(LSGSQliteOpenHelper.DB_EXCLUDE_TABLE,
				new String[] { LSGSQliteOpenHelper.DB_RAW_FACH }, null, null,
				null, null, null);
		exclude.moveToFirst();
		int i = 0;
		while (i < exclude.getCount()) {
			String fach = exclude.getString(exclude
					.getColumnIndex(LSGSQliteOpenHelper.DB_RAW_FACH));
			exclude_cond += " AND " + LSGSQliteOpenHelper.DB_RAW_FACH + " != '"
					+ fach + "' ";
			exclude.moveToNext();
			i++;
		}
		exclude.close();
		include_cond = new String();
		Cursor include = myDB.query(LSGSQliteOpenHelper.DB_INCLUDE_TABLE,
				new String[] { LSGSQliteOpenHelper.DB_FACH }, null, null, null,
				null, null);
		include.moveToFirst();
		i = 0;
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		String connector = "";
		while (i < include.getCount()) {
			String fach = include.getString(include
					.getColumnIndex(LSGSQliteOpenHelper.DB_FACH));
			include_cond += connector + LSGSQliteOpenHelper.DB_FACH
					+ " LIKE '%" + fach + "%' ";
			connector = " OR ";
			include.moveToNext();
			i++;
		}
		include.close();
		if (include_cond.length() == 0)
			include_cond = " 0 ";
		if (prefs.getBoolean("showonlywhitelist", false))
			include_cond = "AND (" + include_cond + " ) OR ( " + include_cond
					+ " )";
		else
			include_cond = " OR ( " + include_cond + " ) ";
	}

	public void updateCursor() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		String klasse = prefs.getString("full_class", "");
		where_conds[0] = "%" + klasse + "%";
		String first = "( " + LSGSQliteOpenHelper.DB_KLASSE + " LIKE ? ";
		String sec = "";
		if (prefs.getBoolean("showwithoutclass", true))
			sec = "OR " + LSGSQliteOpenHelper.DB_KLASSE + " LIKE 'null'";
		sec += " OR " + LSGSQliteOpenHelper.DB_KLASSE
				+ " LIKE 'infotext') AND ( " + LSGSQliteOpenHelper.DB_KLASSE
				+ " LIKE ? OR " + LSGSQliteOpenHelper.DB_FACH + " LIKE ? OR "
				+ LSGSQliteOpenHelper.DB_LEHRER + " LIKE ? )";
		String mine_cond = first + include_cond + sec + exclude_cond;
		String all_cond = first + sec;
		if (prefs.getBoolean(Functions.RIGHTS_TEACHER, false)) {
			cursor_mine = myDB
					.query(LSGSQliteOpenHelper.DB_VPLAN_TEACHER,
							new String[] { LSGSQliteOpenHelper.DB_ROWID,
									LSGSQliteOpenHelper.DB_KLASSE,
									LSGSQliteOpenHelper.DB_TYPE,
									LSGSQliteOpenHelper.DB_STUNDE,
									LSGSQliteOpenHelper.DB_LEHRER,
									LSGSQliteOpenHelper.DB_FACH,
									LSGSQliteOpenHelper.DB_VERTRETUNGSTEXT,
									LSGSQliteOpenHelper.DB_VERTRETER,
									LSGSQliteOpenHelper.DB_ROOM,
									LSGSQliteOpenHelper.DB_CLASS_LEVEL,
									LSGSQliteOpenHelper.DB_DATE,
									LSGSQliteOpenHelper.DB_LENGTH,
									"'teachers' AS type" },
							LSGSQliteOpenHelper.DB_RAW_VERTRETER + "=? OR "
									+ LSGSQliteOpenHelper.DB_RAW_LEHRER + "=?",
							new String[] {
									prefs.getString(
											LSGSQliteOpenHelper.TEACHER_SHORT,
											""),
									prefs.getString(
											LSGSQliteOpenHelper.TEACHER_SHORT,
											"") }, null, null, null);
		} else {
			cursor_mine = myDB
					.query(LSGSQliteOpenHelper.DB_VPLAN_TABLE,
							new String[] { LSGSQliteOpenHelper.DB_ROWID,
									LSGSQliteOpenHelper.DB_KLASSE,
									LSGSQliteOpenHelper.DB_TYPE,
									LSGSQliteOpenHelper.DB_STUNDE,
									LSGSQliteOpenHelper.DB_LEHRER,
									LSGSQliteOpenHelper.DB_FACH,
									LSGSQliteOpenHelper.DB_VERTRETUNGSTEXT,
									LSGSQliteOpenHelper.DB_VERTRETER,
									LSGSQliteOpenHelper.DB_ROOM,
									LSGSQliteOpenHelper.DB_CLASS_LEVEL,
									LSGSQliteOpenHelper.DB_DATE,
									LSGSQliteOpenHelper.DB_LENGTH,
									"'pupils' AS type" }, mine_cond,
							where_conds, null, null, null);
		}
		where_conds[0] = "%";
		cursor_all = myDB.query(LSGSQliteOpenHelper.DB_VPLAN_TABLE,
				new String[] { LSGSQliteOpenHelper.DB_ROWID,
						LSGSQliteOpenHelper.DB_KLASSE,
						LSGSQliteOpenHelper.DB_TYPE,
						LSGSQliteOpenHelper.DB_STUNDE,
						LSGSQliteOpenHelper.DB_LEHRER,
						LSGSQliteOpenHelper.DB_FACH,
						LSGSQliteOpenHelper.DB_VERTRETUNGSTEXT,
						LSGSQliteOpenHelper.DB_VERTRETER,
						LSGSQliteOpenHelper.DB_ROOM,
						LSGSQliteOpenHelper.DB_CLASS_LEVEL,
						LSGSQliteOpenHelper.DB_DATE,
						LSGSQliteOpenHelper.DB_LENGTH, "'pupils' AS type" },
				all_cond, where_conds, null, null, null);
		cursor_teachers = myDB.query(LSGSQliteOpenHelper.DB_VPLAN_TEACHER,
				new String[] { LSGSQliteOpenHelper.DB_ROWID,
						LSGSQliteOpenHelper.DB_KLASSE,
						LSGSQliteOpenHelper.DB_TYPE,
						LSGSQliteOpenHelper.DB_STUNDE,
						LSGSQliteOpenHelper.DB_LEHRER,
						LSGSQliteOpenHelper.DB_FACH,
						LSGSQliteOpenHelper.DB_VERTRETUNGSTEXT,
						LSGSQliteOpenHelper.DB_VERTRETER,
						LSGSQliteOpenHelper.DB_ROOM,
						LSGSQliteOpenHelper.DB_CLASS_LEVEL,
						LSGSQliteOpenHelper.DB_DATE,
						LSGSQliteOpenHelper.DB_LENGTH, "'teachers' AS type" },
				all_cond, where_conds, null, null, LSGSQliteOpenHelper.DB_ROWID
						+ ", CASE " + LSGSQliteOpenHelper.DB_VERTRETER
						+ " WHEN 'null' THEN 0 ELSE 1 END, "
						+ LSGSQliteOpenHelper.DB_VERTRETER + ", "
						+ LSGSQliteOpenHelper.DB_STUNDE);
		vadapter_mine.changeCursor(cursor_mine);
		vadapter_all.changeCursor(cursor_all);
		vadapter_teachers.changeCursor(cursor_teachers);
	}

	public void updateWhereCond(String searchText) {
		where_conds[1] = "%" + searchText + "%";
		where_conds[2] = "%" + searchText + "%";
		where_conds[3] = "%" + searchText + "%";

		where_conds_events[0] = "%" + searchText + "%";
		where_conds_events[1] = "%" + searchText + "%";
		where_conds_events[2] = "%" + searchText + "%";
		where_conds_events[3] = "%" + searchText + "%";
		where_conds_events[4] = "%" + searchText + "%";
		where_conds_events[5] = "%" + searchText + "%";
		updateCursor();
	}

	@Override
	public void updateList() {
		updateCondLists();
		updateCursor();
	}

	public void afterTextChanged(Editable s) {
	}

	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
	}

	public void onTextChanged(CharSequence s, int start, int before, int count) {
		String search = s + "";
		updateWhereCond(search);
	}

	public void closeCursorsDB() {
		cursor_mine.close();
		cursor_all.close();
	}

	@Override
	public void destroyItem(View pager, int position, Object view) {
		((ViewPager) pager).removeView((View) view);
	}

	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view.equals(object);
	}

	@Override
	public void finishUpdate(View view) {
	}

	@Override
	public void restoreState(Parcelable p, ClassLoader c) {
	}

	@Override
	public Parcelable saveState() {
		return null;
	}

	@Override
	public void startUpdate(View view) {
	}
}
