package com.lsg.app.lib;

import android.app.Application;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.lsg.app.Functions;
import com.lsg.app.sqlite.LSGSQliteOpenHelper;

public class LSGApplication extends Application {

	private static SQLiteDatabase sqliteDatabase;
	private static LSGSQliteOpenHelper sqliteOpenHelper;

	@Override
	public void onCreate() {
		ExceptionHandler.init(this);
		super.onCreate();
		// one single database for the entire app

		Context context = getApplicationContext();

		sqliteOpenHelper = new LSGSQliteOpenHelper(context);
		sqliteDatabase = sqliteOpenHelper.getWritableDatabase();
    	Functions.cleanVPlanTable(LSGSQliteOpenHelper.DB_VPLAN_TABLE);
    	Functions.cleanVPlanTable(LSGSQliteOpenHelper.DB_VPLAN_TEACHER);
	}

	public static SQLiteDatabase getSqliteDatabase() {
		return sqliteDatabase;
	}

	public static LSGSQliteOpenHelper getSqliteOpenHelper() {
		return sqliteOpenHelper;
	}

	@Override
	public void onTerminate() {
		if (sqliteDatabase != null) {
			sqliteDatabase.close();
		}

		if (sqliteOpenHelper != null) {
			sqliteOpenHelper.close();
		}

		super.onTerminate();
	}
}
