package com.lsg.app;

import java.net.URLEncoder;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.lsg.app.TimeTable.TimeTableUpdater;
import com.lsg.app.lib.ExceptionHandler;

public class SetupAssistant extends Activity {
	private SharedPreferences prefs;
	private SharedPreferences.Editor edit;

	class LoginTest extends AsyncTask<Void, Void, String> {
		Context context;

		LoginTest(Context c) {
			context = c;
		}

		protected void onPreExecute() {
			super.onPreExecute();
			loading = ProgressDialog
					.show(context, null, getString(R.string.check_login)); 
		}

		@Override
		protected String doInBackground(Void... params) {
			return Functions.getData(Functions.LOGIN_TEST_URL, context, true,
					"");
		}

		@Override
		protected void onPostExecute(String success) {
			super.onPostExecute(success);
			loading.cancel();
			if (success.equals("true")) {
				loginsuccess = true;
				step = 1;
				setup(step, false);
			} else if (success.equals("false"))
				setupUser();
			else {
				AlertDialog.Builder builder = new AlertDialog.Builder(
						SetupAssistant.this);
				builder.setMessage(
						getString(R.string.network_conn_req))
						.setCancelable(true)
						.setNeutralButton(getString(R.string.ok),
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										SetupAssistant.this.finish();
									}
								});
				AlertDialog alert = builder.create();
				alert.show();
			}
		}
	}

	class PersonData extends AsyncTask<Void, Void, String> {
		Context context;

		PersonData(Context c) {
			context = c;
		}

		protected void onPreExecute() {
			super.onPreExecute();
			loading = ProgressDialog.show(context, null, getString(R.string.loading_data));
		}

		@Override
		protected String doInBackground(Void... params) {
			return Functions.getData(Functions.PERSON_DATA_URL, context, true,
					"");
		}

		@Override
		protected void onPostExecute(String data) {
			super.onPostExecute(data);
			try {
				Log.d("data", data);
				JSONObject jarr = new JSONObject(data);
				gender = jarr.getString("gender").charAt(0);
				religion = jarr.getString("religion");
				usr_class = jarr.getString("class");
				pupil = jarr.getBoolean("pupil");
				teacher = jarr.getBoolean("teacher");
				admin = jarr.getBoolean("admin");
				try {
				teacher_short = jarr.getString("short");
				} catch(JSONException e) {
					Log.v(SetupAssistant.class.getName(), "seems like you're not a teacher :)");
					e.printStackTrace();
				}
				JSONArray json_cls = jarr.getJSONArray("classes");
				classes = new String[json_cls.length()];
				for (int i = 0; i < json_cls.length(); i++)
					classes[i] = json_cls.getString(i);
			} catch (Exception e) {
				Log.d("e", e.getMessage());
				e.printStackTrace();
			}
			dataUser = prefs.getString("username", "");
			if (!teacher) {
				loading.cancel();
				setVPlanData();
			} else {
				RetrieveTeacherVPlan rtv = new RetrieveTeacherVPlan();
				rtv.execute();
			}
		}
	}
	
	class RetrieveTeacherVPlan extends AsyncTask <Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... params) {
			TimeTable.TimeTableUpdater upd = new TimeTable.TimeTableUpdater(getApplicationContext());
			upd.updateTeachers();
			return null;
		}
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			loading.cancel();
			setVPlanTeacherData();
		}
		
	}

	class SendData extends AsyncTask<Void, Void, Boolean> {
		Context context;
		SharedPreferences prefs;

		SendData(Context c, SharedPreferences p) {
			context = c;
			prefs = p;
		}

		protected void onPreExecute() {
			super.onPreExecute();
			loading = ProgressDialog.show(context, null, getString(R.string.loading_data));
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			String res = "";
			try {
				res = Functions
						.getData(
								Functions.PERSON_DATA_SEND_URL,
								context,
								true,
								"&"
										+ URLEncoder.encode("full_class",
												"UTF-8")
										+ "="
										+ URLEncoder.encode(prefs.getString(
												Functions.FULL_CLASS, ""),
												"UTF-8")
										+ "&"
										+ URLEncoder.encode("gender", "UTF-8")
										+ "="
										+ URLEncoder.encode(prefs.getString(
												Functions.GENDER, ""), "UTF-8")
										+ "&"
										+ URLEncoder
												.encode("religion", "UTF-8")
										+ "="
										+ URLEncoder.encode(prefs.getString(
												Functions.RELIGION, ""),
												"UTF-8") + "&" + URLEncoder.encode("short", "UTF-8") + "=" + URLEncoder.encode(prefs.getString(Functions.TEACHER_SHORT, ""), "UTF-8"));
			} catch (Exception e) {
			}
			return res.equals("success");
		}

		@Override
		protected void onPostExecute(Boolean parm) {
			super.onPostExecute(parm);
			loading.cancel();
			if (!prefs.getString(Functions.FULL_CLASS, "null").equals("null"))
				step += 1;
			else {

				edit.putBoolean(Functions.IS_LOGGED_IN, true);
				edit.commit();
				startActivity(new Intent(SetupAssistant.this, TimeTable.class));
			}
			setup(step, false);
		}
	}

	class TimeTableData extends AsyncTask<Void, Void, ArrayList<Integer[]>> {
		Context context;

		TimeTableData(Context c) {
			context = c;
		}

		protected void onPreExecute() {
			super.onPreExecute();
			loading = ProgressDialog.show(context, null, getString(R.string.loading_data));
		}

		@Override
		protected ArrayList<Integer[]> doInBackground(Void... params) {
			SubjectList.SubjectListUpdater supdater = new SubjectList.SubjectListUpdater(
					context);
			supdater.updateSubjectList();
			ArrayList<Integer[]> conflicts = new ArrayList<Integer[]>();
			TimeTableUpdater upd = new TimeTableUpdater(SetupAssistant.this);
			upd.updatePupils();
			if (teacher || admin)
				upd.updateTeachers();
			SQLiteDatabase myDB = context.openOrCreateDatabase(
					Functions.DB_NAME, Context.MODE_PRIVATE, null);
			SQLiteStatement stmt = myDB.compileStatement("DELETE FROM " + Functions.DB_EXCLUDE_TABLE + " WHERE " + Functions.DB_TYPE + "=?");
			stmt.bindString(1, "newstyle");
			stmt.execute();
			for (int day = 0; day < 7; day++) {
				for (int hour = 0; hour < 12; hour++) {
					stmt = myDB
							.compileStatement("SELECT COUNT(*) FROM "
									+ Functions.DB_TIME_TABLE + " WHERE "
									+ Functions.DB_DAY + "=? AND "
									+ Functions.DB_HOUR + "=? AND "
									+ Functions.DB_RAW_FACH + "!=? AND "
									+ Functions.DB_RAW_FACH + "!=? AND "
									+ Functions.DB_RAW_FACH + "!=? AND "
									+ Functions.DB_CLASS + " LIKE ?");
					stmt.bindString(1, Integer.valueOf(day).toString());
					stmt.bindString(2, Integer.valueOf(hour).toString());
					stmt.bindString(3, prefs.getString(Functions.GENDER, "m")
							.equals("m") ? "Sw" : "Sm");
					stmt.bindString(6,
							"%"
									+ prefs.getString(Functions.FULL_CLASS, "")
											.substring(0, 2)
									+ "%"
									+ prefs.getString(Functions.FULL_CLASS, "")
											.substring(2, 3) + "%");
					if (prefs.getString(Functions.RELIGION, "").equals(
							Functions.KATHOLISCH)) {
						stmt.bindString(4, Functions.EVANGELISCH);
						stmt.bindString(5, Functions.ETHIK);
					} else if (prefs.getString(Functions.RELIGION, "").equals(
							Functions.EVANGELISCH)) {
						stmt.bindString(4, Functions.KATHOLISCH);
						stmt.bindString(5, Functions.ETHIK);
					} else {
						stmt.bindString(4, Functions.EVANGELISCH);
						stmt.bindString(5, Functions.KATHOLISCH);
					}
					long count = stmt.simpleQueryForLong();
					if (count > 1 || ((prefs.getString(Functions.FULL_CLASS, "").equals("Q11") || prefs.getString(Functions.FULL_CLASS, "").equals("Q12")) && count > 0)) {
						String selectionArgs[] = new String[6];
						selectionArgs[0] = Integer.valueOf(day).toString();
						selectionArgs[1] = Integer.valueOf(hour).toString();
						selectionArgs[2] = prefs.getString(Functions.GENDER,
								"m").equals("m") ? "Sw" : "Sm";
						if (prefs.getString(Functions.RELIGION, "").equals(
								Functions.KATHOLISCH)) {
							selectionArgs[3] = Functions.EVANGELISCH;
							selectionArgs[4] = Functions.ETHIK;
						} else if (prefs.getString(Functions.RELIGION, "")
								.equals(Functions.EVANGELISCH)) {
							selectionArgs[3] = Functions.KATHOLISCH;
							selectionArgs[4] = Functions.ETHIK;
						} else {
							selectionArgs[3] = Functions.EVANGELISCH;
							selectionArgs[4] = Functions.KATHOLISCH;
						}
						selectionArgs[5] = "%"
								+ prefs.getString(Functions.FULL_CLASS, "")
										.substring(0, 2)
								+ "%"
								+ prefs.getString(Functions.FULL_CLASS, "")
										.substring(2, 3) + "%";
						Cursor c = myDB.query(Functions.DB_TIME_TABLE,
								new String[] { Functions.DB_RAW_FACH,
										Functions.DB_ROWID, Functions.DB_FACH,
										Functions.DB_LEHRER,
										Functions.DB_RAW_LEHRER },
								Functions.DB_DAY + "=? AND "
										+ Functions.DB_HOUR + "=? AND "
										+ Functions.DB_RAW_FACH + "!=? AND "
										+ Functions.DB_RAW_FACH + "!=? AND "
										+ Functions.DB_RAW_FACH + "!=? AND "
										+ Functions.DB_CLASS + " LIKE ?",
								selectionArgs, null, null, Functions.DB_FACH);
						if (c.moveToFirst())
							do {
								Log.d("pos", Integer.valueOf(c.getPosition())
										.toString());
								stmt = myDB.compileStatement("INSERT INTO "
										+ Functions.DB_EXCLUDE_TABLE + " ("
										+ Functions.DB_TEACHER + ", "
										+ Functions.DB_RAW_FACH + ", "
										+ Functions.DB_FACH + ", "
										+ Functions.DB_HOUR + ", "
										+ Functions.DB_DAY + ", "
										+ Functions.DB_TYPE
										+ ") VALUES (?, ?, ?, ?, ?, ?)");
								stmt.bindString(
										1,
										c.getString(c
												.getColumnIndex(Functions.DB_RAW_LEHRER)));
								stmt.bindString(2, c.getString(c
										.getColumnIndex(Functions.DB_RAW_FACH)));
								stmt.bindString(3, c.getString(c
										.getColumnIndex(Functions.DB_FACH)));
								stmt.bindLong(4, hour);
								stmt.bindLong(5, day);
								stmt.bindString(6, "newstyle");
								stmt.execute();
								Log.d("stmt", stmt.toString());
								stmt.close();
								Log.d("rawfach", c.getString(c
										.getColumnIndex(Functions.DB_RAW_FACH)));
							} while (c.moveToNext());
						c.close();
						conflicts.add(new Integer[] { day, hour });
					}
				}
			}
			myDB.close();
			return conflicts;
		}

		@Override
		protected void onPostExecute(ArrayList<Integer[]> conflicts) {
			loading.cancel();
			super.onPostExecute(conflicts);
			setupTimeTableConflicts(conflicts);
		}
	}

	private ProgressDialog loading;
	private static int step = 0;
	private static boolean loginsuccess = false;
	private static char gender = '0';
	private static boolean pupil = false;
	private static boolean teacher = false;
	private static boolean admin = false;
	private static String religion = null;
	private static String dataUser = null;
	private static String[] classes;
	private static String usr_class;
	private static String teacher_short;
	private static ArrayList<RadioGroup> timetable_conflicts_rg;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Functions.init(this);
		Functions.setTheme(false, false, this);
		Functions.setupDB(this);
		getWindow().setBackgroundDrawableResource(R.layout.background);
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		edit = prefs.edit();
		if (!prefs.getBoolean(Functions.IS_LOGGED_IN, false))
			setup(step, false);
		else {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(
					getString(R.string.rerun_setup_assistant))
					.setCancelable(false)
					.setPositiveButton(getString(R.string.yes),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									dialog.cancel();
									setup(step, false);
								}
							})
					.setNegativeButton(getString(R.string.no),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									dialog.cancel();
									finish();
								}
							});
			AlertDialog alert = builder.create();
			alert.show();
		}
		Functions.setAlarm(this);
		Intent testAC2DM = new Intent("com.google.android.c2dm.intent.REGISTER");
		if (startService(testAC2DM) == null) {
			Log.i(Functions.TAG, "c2dm not available; disabling");
			edit.putBoolean("disableAC2DM", true);
			edit.putBoolean("useac2dm", false);
			edit.commit();
		}
	}

	public void setup(int step, boolean force) {
		if (!loginsuccess)
			step = 0;
		switch (step) {
		case 0:
			if (!force)
				testUser();
			else
				setupUser();
			break;
		case 1:
			setupVPlan();
			break;
		case 2:
			setupTimeTable();
			break;
		case 3:
		}
	}

	public void testUser() {
		setTitle("LSGäpp Setup");
		if (!prefs.getString("username", "").equals("")) {
			LoginTest test = new LoginTest(this);
			test.execute();
		} else
			setupUser();
	}

	public void setupUser() {
		setContentView(R.layout.setup_user);
		((EditText) findViewById(R.id.username)).setText(prefs.getString(
				"username", ""));
		((EditText) findViewById(R.id.password)).setText(prefs.getString(
				"password", ""));
		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
		((EditText) findViewById(R.id.password))
				.setOnEditorActionListener(new OnEditorActionListener() {
					@Override
					public boolean onEditorAction(TextView v, int actionId,
							KeyEvent event) {
						if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER))
								|| (actionId == EditorInfo.IME_ACTION_DONE)) {
							next(null);
						}
						return false;
					}

				});
	}

	public void setupVPlan() {
		if (dataUser == null
				|| !dataUser.equals(prefs.getString("username", ""))) {
			PersonData pd = new PersonData(this);
			pd.execute();
		} else {
			if (!teacher)
				setVPlanData();
			else
				setVPlanTeacherData();
		}
	}

	public void setVPlanTeacherData() {
		setContentView(R.layout.setup_vplan_teacher);
		((Button) findViewById(R.id.teacher_short)).setText(teacher_short);
		setPermissions();
	}

	public void selectShort(View v) {
		final SQLiteDatabase myDB = openOrCreateDatabase(Functions.DB_NAME, MODE_PRIVATE, null);
		final Cursor allShorts = myDB.query(Functions.DB_TIME_TABLE_HEADERS_TEACHERS, new String[] {Functions.DB_SHORT, Functions.DB_ROWID}, null, null, null, null, null);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.select_short);
		builder.setCursor(allShorts, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				allShorts.moveToPosition(which);
				teacher_short = allShorts.getString(allShorts.getColumnIndex(Functions.DB_SHORT));
				allShorts.close();
				myDB.close();
				setVPlanTeacherData();
			}}, Functions.DB_SHORT);
		AlertDialog alert = builder.create();
		alert.show();
	}

	public void setVPlanData() {
		setContentView(R.layout.setup_vplan);
		if (prefs.getBoolean("disableAC2DM", false)) {
			CheckBox chk = (CheckBox) findViewById(R.id.push_check);
			chk.setChecked(false);
			chk.setVisibility(View.GONE);
		}
		RadioGroup genderrg = (RadioGroup) findViewById(R.id.gendergroup);
		if (gender == 'm')
			genderrg.check(R.id.male);
		else
			genderrg.check(R.id.female);
		RadioGroup religionrg = (RadioGroup) findViewById(R.id.religiongroup);
		Log.d("religion", religion);
		if (religion.equals("K"))
			religionrg.check(R.id.catholic);
		else if (religion.equals("Ev"))
			religionrg.check(R.id.protestant);
		else if (religion.equals("Eth"))
			religionrg.check(R.id.ethics);

		RadioGroup classrg = (RadioGroup) findViewById(R.id.classgroup);
		if (classes != null) {
			RadioButton[] rb = new RadioButton[classes.length];
			for (int i = 0; i < classes.length; i++) {
				rb[i] = new RadioButton(this);
				rb[i].setText(classes[i]);
				rb[i].setId(i);
				if (classes[i].equals(usr_class) || classes.length == 1)
					rb[i].toggle();
			}
			for (int i = 0; i < classes.length; i++)
				classrg.addView(rb[i], i);
		} else {
			classrg.setVisibility(View.GONE);
			findViewById(R.id.classtext).setVisibility(View.GONE);
		}
		setPermissions();
	}
	public void setPermissions() {
		// strike through non-available permissions
		TextView tv = ((TextView) findViewById(R.id.pupil));
		tv.setPaintFlags(tv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
		if (pupil)
			tv.setPaintFlags(tv.getPaintFlags() ^ Paint.STRIKE_THRU_TEXT_FLAG);
		tv = ((TextView) findViewById(R.id.teacher));
		tv.setPaintFlags(tv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
		if (teacher)
			tv.setPaintFlags(tv.getPaintFlags() ^ Paint.STRIKE_THRU_TEXT_FLAG);
		tv = ((TextView) findViewById(R.id.admin));
		tv.setPaintFlags(tv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
		if (admin)
			tv.setPaintFlags(tv.getPaintFlags() ^ Paint.STRIKE_THRU_TEXT_FLAG);
	}

	public void setupTimeTable() {
		setContentView(R.layout.setup_timetable);
		TimeTableData ttd = new TimeTableData(this);
		ttd.execute();
	}

	public void setupTimeTableConflicts(ArrayList<Integer[]> conflicts) {
		Integer[] conflict = new Integer[2];
		timetable_conflicts_rg = new ArrayList<RadioGroup>();
		SQLiteDatabase myDB = openOrCreateDatabase(Functions.DB_NAME,
				Context.MODE_PRIVATE, null);
		for (int i = 0; i < conflicts.size(); i++) {
			conflict = conflicts.get(i);

			String[] selectionArgs = new String[6];
			selectionArgs[0] = Integer.valueOf(conflict[0]).toString();
			selectionArgs[1] = Integer.valueOf(conflict[1]).toString();
			selectionArgs[2] = prefs.getString(Functions.GENDER, "m").equals(
					"m") ? "Sw" : "Sm";
			if (prefs.getString(Functions.RELIGION, "").equals(
					Functions.KATHOLISCH)) {
				selectionArgs[3] = Functions.EVANGELISCH;
				selectionArgs[4] = Functions.ETHIK;
			} else if (prefs.getString(Functions.RELIGION, "").equals(
					Functions.EVANGELISCH)) {
				selectionArgs[3] = Functions.KATHOLISCH;
				selectionArgs[4] = Functions.ETHIK;
			} else {
				selectionArgs[3] = Functions.EVANGELISCH;
				selectionArgs[4] = Functions.KATHOLISCH;
			}
			selectionArgs[5] = "%"
					+ prefs.getString(Functions.FULL_CLASS, "").substring(0, 2)
					+ "%"
					+ prefs.getString(Functions.FULL_CLASS, "").substring(2, 3)
					+ "%";
			Cursor c = myDB.query(Functions.DB_TIME_TABLE, new String[] {
					Functions.DB_RAW_FACH, Functions.DB_ROWID,
					Functions.DB_FACH, Functions.DB_LEHRER }, Functions.DB_DAY
					+ "=? AND " + Functions.DB_HOUR + "=? AND "
					+ Functions.DB_RAW_FACH + "!=? AND "
					+ Functions.DB_RAW_FACH + "!=? AND "
					+ Functions.DB_RAW_FACH + "!=? AND " + Functions.DB_CLASS
					+ " LIKE ?", selectionArgs, null, null, null);
			c.moveToFirst();
			RadioGroup rg = new RadioGroup(this);
			TextView tv = new TextView(this);
			tv.setText(getResources().getStringArray(R.array.days)[conflict[0]]
					+ ", " + Integer.valueOf(conflict[1] + 1).toString()
					+ ". Stunde");
			tv.setTextAppearance(this, android.R.attr.textAppearanceMedium);
			((LinearLayout) findViewById(R.id.timetable_conflicts_placeholder))
					.addView(tv);
			boolean showNone;
			if (conflict[1] + 1 < 7)
				showNone = false;
			else
				showNone = true;
			for (int ii = 0; ii < c.getCount(); ii++) {
				if (showNone) {
					try {
						if (!c.getString(
								c.getColumnIndex(Functions.DB_RAW_FACH))
								.substring(0, 3).equals("INT"))
							showNone = false;
					} catch (IndexOutOfBoundsException e) {
						showNone = false;
					}
				}
				RadioButton option = new RadioButton(this);
				option.setText(c.getString(c.getColumnIndex(Functions.DB_FACH))
						+ " bei "
						+ c.getString(c.getColumnIndex(Functions.DB_LEHRER)));
				option.setId(c.getInt(c.getColumnIndex(Functions.DB_ROWID)));
				if (ii == 0)
					option.setChecked(true);
				rg.addView(option);
				c.moveToNext();
				if (ii + 1 == c.getCount() && (showNone || prefs.getString(Functions.FULL_CLASS, "").equals("Q12") || prefs.getString(Functions.FULL_CLASS, "").equals("Q11"))) {
					option = new RadioButton(this);
					option.setText("Freistunde!");
					option.setId(0);
					rg.addView(option);
				}
			}
			c.close();
			((LinearLayout) findViewById(R.id.timetable_conflicts_placeholder))
					.addView(rg);
			timetable_conflicts_rg.add(rg);
		}
		myDB.close();
	}

	public void next(View v) {
		switch (step) {
		case 0:
			edit.putString("username",
					(((EditText) findViewById(R.id.username))).getText()
							.toString());
			edit.putString("password",
					(((EditText) findViewById(R.id.password))).getText()
							.toString());
			edit.commit();
			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(
					((EditText) findViewById(R.id.password)).getWindowToken(),
					0);
			testUser();
			break;
		case 1:
			if (!teacher) {
				RadioGroup religionrg = (RadioGroup) findViewById(R.id.religiongroup);
				String religion;
				switch (religionrg.getCheckedRadioButtonId()) {
				case R.id.catholic:
					religion = Functions.KATHOLISCH;
					break;
				case R.id.protestant:
					religion = Functions.EVANGELISCH;
					break;
				case R.id.ethics:
				default:
					religion = Functions.ETHIK;
					break;
				}
				edit.putString(Functions.RELIGION, religion);
				RadioGroup genderrg = (RadioGroup) findViewById(R.id.gendergroup);
				edit.putString(Functions.GENDER, (genderrg
						.getCheckedRadioButtonId() == R.id.female) ? "w" : "m");
				RadioGroup classrg = (RadioGroup) findViewById(R.id.classgroup);
				try {
					edit.putString(Functions.FULL_CLASS,
							classes[classrg.getCheckedRadioButtonId()]);
				} catch (NullPointerException e) {
					// user doesn't have a class
					edit.putString(Functions.FULL_CLASS, "null");
				}
			} else {
				edit.putString(Functions.TEACHER_SHORT, teacher_short);
			}
			CheckBox chk = (CheckBox) findViewById(R.id.push_check);
			edit.putBoolean("useac2dm", chk.isChecked());
			if (chk.isChecked())
				Functions.registerGCM(this);
			edit.putBoolean(Functions.RIGHTS_PUPIL, pupil);
			edit.putBoolean(Functions.RIGHTS_TEACHER, teacher);
			edit.putBoolean(Functions.RIGHTS_ADMIN, admin);
			edit.commit();
			SendData sd = new SendData(this, prefs);
			sd.execute();
			break;
		case 2:
			SQLiteDatabase myDB = openOrCreateDatabase(Functions.DB_NAME,
					MODE_PRIVATE, null);
			for (int i = 0; i < timetable_conflicts_rg.size(); i++) {
				RadioGroup rg = timetable_conflicts_rg.get(i);
				Log.d("id", Integer.valueOf(rg.getCheckedRadioButtonId())
						.toString());
				if (rg.getCheckedRadioButtonId() != 0) {
					Cursor c = myDB.query(Functions.DB_TIME_TABLE,
							new String[] { Functions.DB_HOUR, Functions.DB_DAY,
									Functions.DB_RAW_FACH,
									Functions.DB_RAW_LEHRER },
							Functions.DB_ROWID + "=?", new String[] { Integer
									.valueOf(rg.getCheckedRadioButtonId())
									.toString() }, null, null, null);
					c.moveToFirst();
					Log.d("count", Integer.valueOf(c.getCount()).toString());
					SQLiteStatement stmt = myDB.compileStatement("DELETE FROM "
							+ Functions.DB_EXCLUDE_TABLE + " WHERE "
							+ Functions.DB_HOUR + "=? AND " + Functions.DB_DAY
							+ "=? AND " + Functions.DB_RAW_FACH + "=? AND "
							+ Functions.DB_TEACHER + "=?");
					stmt.bindString(1,
							c.getString(c.getColumnIndex(Functions.DB_HOUR)));
					stmt.bindString(2,
							c.getString(c.getColumnIndex(Functions.DB_DAY)));
					stmt.bindString(3, c.getString(c
							.getColumnIndex(Functions.DB_RAW_FACH)));
					stmt.bindString(4, c.getString(c
							.getColumnIndex(Functions.DB_RAW_LEHRER)));
					stmt.execute();
					Log.d("hour",
							c.getString(c.getColumnIndex(Functions.DB_HOUR)));
					Log.d("day",
							c.getString(c.getColumnIndex(Functions.DB_DAY)));
					Log.d("subject", c.getString(c
							.getColumnIndex(Functions.DB_RAW_FACH)));
					Log.d("teacher", c.getString(c
							.getColumnIndex(Functions.DB_RAW_LEHRER)));
				}
			}
			myDB.close();
			edit.putBoolean(Functions.IS_LOGGED_IN, true);
			edit.commit();
			startActivity(new Intent(this, TimeTable.class));
			TimeTable.blacklistTimeTable(this);
			break;
		}
		// step += 1;
		// setup(step);
		// startActivity(new Intent(this, TimeTable.class));
	}

	public void previous(View v) {
		step -= 1;
		setup(step, true);
	}

	public void button_press(View v) {
		Log.d("text", (String) ((Button) v).getText());
		if (((String) ((Button) v).getText())
				.equals(getString(R.string.timetable))) {
			Intent timetable = new Intent(this, TimeTable.class);
			startActivity(timetable);
		}
		if (((String) ((Button) v).getText())
				.equals(getString(R.string.vplan_short))) {
			Intent timetable = new Intent(this, VPlan.class);
			startActivity(timetable);
		}
		if (((String) ((Button) v).getText())
				.equals(getString(R.string.events))) {
			Intent timetable = new Intent(this, Events.class);
			startActivity(timetable);
		}
		if (((String) ((Button) v).getText())
				.equals(getString(R.string.smvblog))) {
			Intent timetable = new Intent(this, SMVBlog.class);
			startActivity(timetable);
		}
		if (((String) ((Button) v).getText())
				.equals(getString(R.string.settings))) {
			Intent settings;
			if (Functions.getSDK() >= 11)
				settings = new Intent(this, SettingsAdvanced.class);
			else
				settings = new Intent(this, Settings.class);
			startActivity(settings);
		}
	}
}