package com.lsg.app.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.lsg.app.Functions;

public class LSGSQliteOpenHelper extends SQLiteOpenHelper {
	public static final String DB_ROWID = "_id";
	public static final String DB_NAME = "lsgapp";
	public static final String DB_REMOTE_ID = "remote_id";
	// VPlan
	public static final String DB_VPLAN_TABLE = "vertretungen";
	public static final String DB_VPLAN_TEACHER = "lehrervertretungen";
	public static final String DB_CLASS_LEVEL = "klassenstufe";
	public static final String DB_KLASSE = "klasse";
	public static final String DB_STUNDE = "stunde";
	public static final String DB_VERTRETER = "vertreter";
	public static final String DB_RAW_VERTRETER = "rawvertreter";
	public static final String DB_LEHRER = "lehrer";
	public static final String DB_RAW_LEHRER = "rawlehrer";
	public static final String DB_ROOM = "raum";
	public static final String DB_TYPE = "art";
	public static final String DB_VERTRETUNGSTEXT = "vertretungstext";
	public static final String DB_FACH = "fach";
	public static final String DB_RAW_FACH = "rawfach";
	public static final String DB_DATE = "date";
	public static final String DB_LENGTH = "length";
	public static final String DB_DAY_OF_WEEK = "dayofweek";
	// Termine
	public static final String DB_EVENTS_TABLE = "events";
	public static final String DB_DATES = "dates";
	public static final String DB_ENDDATES = "enddates";
	public static final String DB_TIMES = "times";
	public static final String DB_ENDTIMES = "endtimes";
	public static final String DB_TITLE = "title";
	public static final String DB_VENUE = "venue";
	// exclude & include
	public static final String DB_EXCLUDE_TABLE = "exclude";
	public static final String INCLUDE_TABLE = "include";
	public static final String DB_NEEDS_SYNC = "needssync";
	// subjects
	public static final String DB_SUBJECT_TABLE = "subjects";
	// timetable
	public static final String DB_TIME_TABLE = "timetable";
	public static final String DB_DAY = "day";
	public static final String DB_HOUR = "hour";
	public static final String DB_DISABLED = "disabled";
	public static final String DB_VERTRETUNG = "vertretung";
	// for teachers
	public static final String DB_TIME_TABLE_TEACHERS = "timetable_teachers";
	public static final String DB_BREAK_SURVEILLANCE = "pausenaufsicht";
	// timetable headers
	public static final String DB_TIME_TABLE_HEADERS_PUPILS = "tt_headers";
	public static final String DB_TEACHER = "teacher";
	public static final String DB_SECOND_TEACHER = "secteacher";
	// for teachers
	public static final String DB_TIME_TABLE_HEADERS_TEACHERS = "tt_headers_teacher";
	public static final String DB_SHORT = "short";
	public static final String TEACHER_SHORT = DB_SHORT;
	// Exams & Homework
	public static final String DB_EXAMS_TABLE = "exams";
	public static final String DB_HOMEWORK_TABLE = "homework";
	public static final String DB_YEAR = "year";
	public static final String DB_MONTH = "month";
	public static final String DB_DAYOFMONTH = "dayofmonth";
	public static final String DB_LEARNING_MATTER = "learning_matter";
	public static final String DB_NOTES = "notes";
	public static final String DB_CONTENT = "content";
	public static final String DB_LOCKED = "locked";
	// classes
	public static final String DB_CLASS_TABLE = "classes";
	public static final String DB_CLASS = "class";
	
	public static final int DB_VERSION = 16;

	public LSGSQliteOpenHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		createCommands(database);
		onUpgrade(database, 0, DB_VERSION);
		}
	private void createCommands(SQLiteDatabase database) {
		//vertretungen
		database.execSQL("CREATE TABLE IF NOT EXISTS " + DB_VPLAN_TABLE
				+ " (" + DB_ROWID       + " INTEGER primary key autoincrement,"
	    		+ DB_CLASS_LEVEL       + " INTEGER,"
	    	    + DB_KLASSE	          + " TEXT,"
	    	    + DB_STUNDE             + " INTEGER,"
	    	    + DB_VERTRETER          + " TEXT,"
	     	    + DB_LEHRER             + " TEXT,"
	    	    + DB_ROOM               + " TEXT,"
	    	    + DB_TYPE                + " TEXT,"
	    	    + DB_VERTRETUNGSTEXT    + " TEXT,"
	    	    + DB_FACH               + " TEXT,"
	    	    + DB_DATE               + " TEXT"
				+");");
		database.execSQL("CREATE TABLE IF NOT EXISTS " + DB_VPLAN_TEACHER
				+ " (" + DB_ROWID       + " INTEGER primary key autoincrement,"
	    		+ DB_CLASS_LEVEL       + " INTEGER,"
	    	    + DB_KLASSE	          + " TEXT,"
	    	    + DB_STUNDE             + " INTEGER,"
	    	    + DB_VERTRETER          + " TEXT,"
	     	    + DB_LEHRER             + " TEXT,"
	    	    + DB_ROOM               + " TEXT,"
	    	    + DB_TYPE                + " TEXT,"
	    	    + DB_VERTRETUNGSTEXT    + " TEXT,"
	    	    + DB_FACH               + " TEXT,"
	    	    + DB_DATE               + " TEXT,"
	    	    + DB_RAW_FACH           + " TEXT,"
	    	    + DB_LENGTH             + " INTEGER"
				+");");
		//blacklist
		database.execSQL("CREATE TABLE IF NOT EXISTS " + DB_EXCLUDE_TABLE + " ("
				+ DB_ROWID + " INTEGER primary key autoincrement,"
				+ DB_FACH + " TEXT,"
				+ DB_NEEDS_SYNC + " TEXT"
				+ ");");
		//whitelist
		database.execSQL("CREATE TABLE IF NOT EXISTS " + INCLUDE_TABLE + " ("
				+ DB_ROWID + " INTEGER primary key autoincrement,"
				+ DB_FACH + " TEXT,"
				+ DB_NEEDS_SYNC + " TEXT"
				+ ");");
		//subjects
		database.execSQL("CREATE TABLE IF NOT EXISTS " + DB_SUBJECT_TABLE
				+ " (" + DB_ROWID       + " INTEGER primary key autoincrement,"
	    		+ DB_RAW_FACH + " TEXT,"
	    	    + DB_FACH + " TEXT"
				+");");
		//events
		database.execSQL("CREATE TABLE IF NOT EXISTS " + DB_EVENTS_TABLE
				+ " (" + DB_ROWID       + " INTEGER primary key autoincrement,"
	    	    + DB_DATES              + " TEXT,"
	     	    + DB_ENDDATES           + " TEXT,"
	    	    + DB_TIMES              + " TEXT,"
	    	    + DB_ENDTIMES           + " TEXT,"
	    	    + DB_TITLE              + " TEXT,"
	    	    + DB_VENUE              + " TEXT"
				+");");
		database.execSQL("CREATE TABLE IF NOT EXISTS " + DB_TIME_TABLE
				+ " (" + DB_ROWID        + " INTEGER primary key autoincrement,"
	    	    + DB_LEHRER              + " TEXT,"
	     	    + DB_FACH                + " TEXT,"
	    	    + DB_ROOM                + " TEXT,"
	    	    + DB_LENGTH              + " INTEGER,"
	    	    + DB_DAY                 + " INTEGER,"
	    	    + DB_HOUR                + " INTEGER"
				+");");
		database.execSQL("CREATE TABLE IF NOT EXISTS " + DB_TIME_TABLE_HEADERS_PUPILS
				+ " (" + DB_ROWID + " INTEGER primary key autoincrement,"
				+ DB_TEACHER + " TEXT,"
				+ DB_SECOND_TEACHER + " TEXT,"
				+ DB_KLASSE + " TEXT"
				+ ");");
		database.execSQL("CREATE TABLE IF NOT EXISTS " + DB_TIME_TABLE_TEACHERS
				+ " (" + DB_ROWID        + " INTEGER primary key autoincrement,"
				+ DB_SHORT               + " TEXT,"
	    	    + DB_BREAK_SURVEILLANCE  + " TEXT,"
	    	    + DB_RAW_FACH            + " TEXT,"
	     	    + DB_FACH                + " TEXT,"
	    	    + DB_ROOM                + " TEXT,"
	    	    + DB_CLASS               + " TEXT,"
	    	    + DB_LENGTH              + " INTEGER,"
	    	    + DB_DAY                 + " INTEGER,"
	    	    + DB_HOUR                + " INTEGER"
				+");");
		database.execSQL("CREATE TABLE IF NOT EXISTS " + DB_TIME_TABLE_HEADERS_TEACHERS
				+ " (" + DB_ROWID + " INTEGER primary key autoincrement,"
				+ DB_SHORT + " TEXT,"
				+ DB_TEACHER + " TEXT"
				+ ");");
		database.execSQL("CREATE TABLE IF NOT EXISTS " + DB_EXAMS_TABLE
				+ " (" + DB_ROWID + " INTEGER primary key autoincrement,"
				+ DB_DATE + " STRING,"
				+ DB_YEAR + " INTEGER,"
				+ DB_MONTH + " INTEGER,"
				+ DB_DAYOFMONTH + " INTEGER,"
				+ DB_TYPE + " STRING,"
				+ DB_RAW_FACH + " STRING,"
				+ DB_FACH + " STRING,"
				+ DB_TITLE + " STRING,"
				+ DB_LEARNING_MATTER + " STRING,"
	    		+ DB_NOTES + " STRING,"
	    		+ DB_LOCKED + " INTEGER"
				+ ");");
		database.execSQL("CREATE TABLE IF NOT EXISTS " + DB_HOMEWORK_TABLE
				+ " (" + DB_ROWID + " INTEGER primary key autoincrement,"
	    		+ DB_DATE + " STRING,"
				+ DB_YEAR + " INTEGER,"
				+ DB_MONTH + " INTEGER,"
				+ DB_DAYOFMONTH + " INTEGER,"
				+ DB_RAW_FACH + " STRING,"
				+ DB_FACH + " STRING,"
				+ DB_TITLE + " STRING,"
				+ DB_CONTENT + " CONTENT"
				+ ");");
	}

	@Override
	public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
		createCommands(database);
		//upgrades for table
		switch (newVersion) {
	 case DB_VERSION:
		 switch (oldVersion) {
		 case 0:
			Log.d(DB_VPLAN_TABLE, "adding column " + DB_RAW_FACH);
			database.execSQL("ALTER TABLE " + DB_VPLAN_TABLE + " ADD COLUMN " + DB_RAW_FACH + " TEXT");
		 case 1:
			Log.d(DB_EXCLUDE_TABLE, "adding column " + DB_RAW_FACH);
			database.execSQL("ALTER TABLE " + DB_EXCLUDE_TABLE + " ADD COLUMN " + DB_RAW_FACH + " TEXT");
			Log.d(INCLUDE_TABLE, "adding column " + DB_RAW_FACH);
			database.execSQL("ALTER TABLE " + INCLUDE_TABLE + " ADD COLUMN " + DB_RAW_FACH + " TEXT");
		 case 2:
			Log.d(DB_VPLAN_TABLE, "adding column " + DB_LENGTH);
			database.execSQL("ALTER TABLE " + DB_VPLAN_TABLE + " ADD COLUMN " + DB_LENGTH + " INTEGER");
		 case 3:
			Log.d(DB_TIME_TABLE, "adding column " + DB_RAW_FACH);
			database.execSQL("ALTER TABLE " + DB_TIME_TABLE + " ADD COLUMN " + DB_RAW_FACH + " TEXT");
		 case 4:
			Log.d(DB_VPLAN_TABLE, "adding column " + DB_RAW_LEHRER);
			database.execSQL("ALTER TABLE " + DB_VPLAN_TABLE + " ADD COLUMN " + DB_RAW_VERTRETER + " TEXT");
			Log.d(DB_VPLAN_TABLE, "adding column " + DB_RAW_LEHRER);
			database.execSQL("ALTER TABLE " + DB_VPLAN_TABLE + " ADD COLUMN " + DB_RAW_LEHRER + " TEXT");
		 case 5:
			Log.d(DB_VPLAN_TEACHER, "adding column " + DB_RAW_LEHRER);
			database.execSQL("ALTER TABLE " + DB_VPLAN_TEACHER + " ADD COLUMN " + DB_RAW_VERTRETER + " TEXT");
			Log.d(DB_VPLAN_TEACHER, "adding column " + DB_RAW_LEHRER);
			database.execSQL("ALTER TABLE " + DB_VPLAN_TEACHER + " ADD COLUMN " + DB_RAW_LEHRER + " TEXT");
		 case 6:
			Log.d(DB_TIME_TABLE, "adding column " + DB_DISABLED);
			database.execSQL("ALTER TABLE " + DB_TIME_TABLE + " ADD COLUMN " + DB_DISABLED + " INT");
		 case 7:
			Log.d(DB_TIME_TABLE, "adding column " + DB_CLASS);
			database.execSQL("ALTER TABLE " + DB_TIME_TABLE + " ADD COLUMN " + DB_CLASS + " TEXT");
		 case 8: 
			Log.d(DB_EXCLUDE_TABLE, "adding column " + DB_TEACHER);
			database.execSQL("ALTER TABLE " + DB_EXCLUDE_TABLE + " ADD COLUMN " + DB_TEACHER + " TEXT");
			Log.d(DB_EXCLUDE_TABLE, "adding column " + DB_HOUR);
			database.execSQL("ALTER TABLE " + DB_EXCLUDE_TABLE + " ADD COLUMN " + DB_HOUR + " TEXT");
			Log.d(DB_EXCLUDE_TABLE, "adding column " + DB_DAY);
			database.execSQL("ALTER TABLE " + DB_EXCLUDE_TABLE + " ADD COLUMN " + DB_DAY + " TEXT");
		 case 9:
			Log.d(DB_TIME_TABLE, "adding column " + DB_RAW_LEHRER);
			database.execSQL("ALTER TABLE " + DB_TIME_TABLE + " ADD COLUMN " + DB_RAW_LEHRER + " TEXT");
		 case 10:
			Log.d(DB_TIME_TABLE, "adding column " + DB_VERTRETUNG);
			database.execSQL("ALTER TABLE " + DB_TIME_TABLE + " ADD COLUMN " + DB_VERTRETUNG + " TEXT");
			Log.d(DB_TIME_TABLE_TEACHERS, "adding column " + DB_VERTRETUNG);
			database.execSQL("ALTER TABLE " + DB_TIME_TABLE_TEACHERS + " ADD COLUMN " + DB_VERTRETUNG + " TEXT");
		 case 11:
			Log.d(DB_EXCLUDE_TABLE, "adding column " + DB_TYPE);
			database.execSQL("ALTER TABLE " + DB_EXCLUDE_TABLE + " ADD COLUMN " + DB_TYPE + " TEXT");
		 case 12:
			Log.d(DB_VPLAN_TABLE, "adding column " + DB_DISABLED);
			database.execSQL("ALTER TABLE " + DB_VPLAN_TABLE + " ADD COLUMN " + DB_DISABLED + " INTEGER");
		 case 13:
			Log.d(DB_TIME_TABLE, "adding column " + DB_REMOTE_ID);
			database.execSQL("ALTER TABLE " + DB_TIME_TABLE + " ADD COLUMN " + DB_REMOTE_ID + " INTEGER");
		 case 14:
			Log.d(DB_VPLAN_TABLE, "adding column " + DB_DAY_OF_WEEK);
			database.execSQL("ALTER TABLE " + DB_VPLAN_TABLE + " ADD COLUMN " + DB_DAY_OF_WEEK + " INTEGER");
		 case 15:
			 Log.d(DB_EXAMS_TABLE, "adding column " + DB_NEEDS_SYNC);
			 database.execSQL("ALTER TABLE " + DB_EXAMS_TABLE + " ADD COLUMN " + DB_NEEDS_SYNC + " INTEGER");
		}
		}
	}

}
