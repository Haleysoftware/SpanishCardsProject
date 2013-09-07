package com.haleysoft.spanish;

/**
 * Created by Haleysoftware on 5/23/13.
 * Cleaned by Mike Haley on 9/6/13.
 */

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

public class UserDBCP extends ContentProvider {
	//User Name Database column names
	public static final String KEY_ROWA = "_id";
	public static final String KEY_USER = "user";
	public static final String KEY_LAST = "last";

	//Score Database column names
	public static final String KEY_ROWB = "_id";
	public static final String KEY_NAME = "name";
	public static final String KEY_DATE = "date";
	public static final String KEY_SCORE = "score";
	public static final String KEY_MODE = "mode";
	public static final String KEY_HSCORE = "highscore";

	//Database and Table names
	private static final String DB_NAME = "userdb";
	private static final String DB_TABLEA = "nametable";
	private static final String DB_TABLEB = "scoretable";
	private static final int DB_VERSION = 1;

	private DatabaseHelper dbHelper;
	private SQLiteDatabase db;

	public final static String DB_CREATEA = "create table " + DB_TABLEA + " (" + KEY_ROWA + " integer primary key autoincrement, " + KEY_USER + " text, " + KEY_LAST + " integer);";
	public final static String DB_CREATEB = "create table " + DB_TABLEB + " (" + KEY_ROWB + " integer primary key autoincrement, " + KEY_NAME + " text, " + KEY_DATE + " text, " + KEY_SCORE + " integer, " + KEY_MODE + " text, " + KEY_HSCORE + " integer);";

	//Content Provider items
	private static final String AUTHORITY = "haleysoft.spanish.provider.scores";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + DB_TABLEB);

	public UserDBCP open() throws SQLException {
		db = dbHelper.getWritableDatabase();
		return this;
	}

	public void close() {
		if (db != null) {
			db.close();
		}
	}

	@Override //CP Method
	public boolean onCreate() {
		dbHelper = new DatabaseHelper(getContext());
		return true;
	}

	@Override //CP Method
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		//open(); //needs to be closed

		return 0;
	}

	@Override //CP Method
	public String getType(Uri uri) {

		return null;
	}

	@Override //CP Method
	public Uri insert(Uri uri, ContentValues values) {
		//open(); //needs to be closed

		return null;
	}

	@Override //CP Method
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		open();
		return db.query(DB_TABLEB, projection, selection, selectionArgs, null, null, sortOrder);
	}

	@Override //CP Method
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		return 0;
	}

	//Database helper (Open, Close, Create, Upgrade)
	private static class DatabaseHelper extends SQLiteOpenHelper {
		DatabaseHelper(Context ctx) {
			super(ctx, DB_NAME, null, DB_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(DB_CREATEA);
			db.execSQL(DB_CREATEB);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			//Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS nametable");
			db.execSQL("DROP TABLE IF EXISTS scoretable");
			onCreate(db);
		}
	}
}
