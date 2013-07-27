package com.haleysoft.spanish;

/**
 * Created by Haleysoftware on 5/23/13.
 */

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

public class WordDBCP extends ContentProvider
{
	private String newDb = "0";
	private String updateDb = "0"; //Added
	private ArrayList<Long> markBackup; //Added
	private ArrayList<Long> pointBackup; //Added
	//private static final String TAG = "WordsDBAdapter";

	//Sets up the Database
	private SQLiteDatabase Db;
	private static dBHelper dbHelp;

	//Define the information about the database.
	private static final String DB_NAME = "SpanishDB.db";
	private static final String DB_TABLE = "SpanishWords";
	private static final int DATABASE_VERSION = 3;

	//Define the names of the database columns
	public static final String KEY_ROWID = "_id";
	public static final String KEY_CAT = "Category";
	public static final String KEY_TYPE = "Type";
	public static final String KEY_LEVEL = "Level";
	public static final String KEY_MARK = "Marked";
	public static final String KEY_POINT = "Point";
	public static final String KEY_NOTE = "Note";
	public static final String KEY_ENG = "English";
	public static final String KEY_AENG = "AltEnglish";
	public static final String KEY_SPAN = "Spanish";
	public static final String KEY_ASPAN = "AltSpanish";

	//Content Provider items
	private static final String AUTHORITY = "haleysoft.spanish.provider.words";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + DB_TABLE);

	//Open the database
	public WordDBCP open() throws SQLException
	{
		//Opens the database but if the database named is not found it will create it.
		Db = dbHelp.getWritableDatabase();
		return this;
	}

	//Closes the Database
	public void close()
	{
		if (Db != null)
		{
			Db.close();
		}
	}

	@Override //CP Method
	public boolean onCreate()
	{
		dbHelp = new dBHelper(getContext());
		open();
		if (newDb.contains("1"))
		{
			try
			{
				copyDataBase();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		if (updateDb.contains("1")) //Added
		{
			backupOldDB();
			close();
			open();
			restoreOldDB();
		}
		close();
		return true;
	}

	@Override //CP Method
	public int delete(Uri uri, String selection, String[] selectionArgs)
	{

		return 0;
	}

	@Override //CP Method
	public String getType(Uri uri)
	{

		return null;
	}

	@Override //CP Method
	public Uri insert(Uri uri, ContentValues values)
	{

		return null;
	}

	@Override //CP Method
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder)
	{
		open();
		return Db.query(DB_TABLE, projection, selection, selectionArgs, null, null, sortOrder);
	}

	@Override //CP Method
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs)
	{

		return 0;
	}

	private void backupOldDB() //Added
	{
		//DataBase is already open and will be closed when finished
		if (markBackup != null)
		{
			markBackup.clear();
		}
		Cursor markSave = getAllMarks();
		markBackup = new ArrayList<Long>();
		while (markSave.moveToNext())
		{
			Long markRowId = markSave.getLong(markSave.getColumnIndex(KEY_ROWID));
			markBackup.add(markRowId);
		}
		markSave.close();

		if (pointBackup != null)
		{
			pointBackup.clear();
		}
		Cursor pointSave = getAllPoints();
		pointBackup = new ArrayList<Long>();
		while (pointSave.moveToNext())
		{
			Long pointRowId = pointSave.getLong(pointSave.getColumnIndex(KEY_ROWID));
			pointBackup.add(pointRowId);
		}
		pointSave.close();

		if (Db != null)
		{
			Db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE);
		}
		try
		{
			copyDataBase();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private void restoreOldDB() //Added
	{
		if (markBackup != null)
		{
			int maxMark = markBackup.size();
			int currentMark = 0;
			while (currentMark < maxMark)
			{
				Long markRow = markBackup.get(currentMark);
				ContentValues markNow = new ContentValues();
				markNow.put(KEY_MARK, 1);
				Db.update(DB_TABLE, markNow, KEY_ROWID + " = " + markRow, null);
				currentMark++;
			}
		}
		if (pointBackup != null)
		{
			int maxPoint = pointBackup.size();
			int currentPoint = 0;
			while (currentPoint < maxPoint)
			{
				Long pointRow = pointBackup.get(currentPoint);
				ContentValues pointNow = new ContentValues();
				pointNow.put(KEY_POINT, 1);
				Db.update(DB_TABLE, pointNow, KEY_ROWID + " = " + pointRow, null);
				currentPoint++;
			}
		}
	}

	//Copies the Database from the Assets folder
	public void copyDataBase() throws IOException
	{
		Resources resources = getContext().getResources();
		//This is the path to the empty database.
		String outFileName = Db.getPath();
		//Opens the empty database as the output stream.
		OutputStream myOutput = new FileOutputStream(outFileName);
		//Open the premade assets database as the input stream.
		InputStream myInput = resources.getAssets().open(DB_NAME);
		//This transfers the bytes from the inputfile to the outputfile.
		byte[] buffer = new byte[1024];
		int length;
		while ((length = myInput.read(buffer))>0)
		{
			myOutput.write(buffer, 0, length);
		}
		//Close the streams when finished.
		myOutput.flush();
		myOutput.close();
		myInput.close();
	}

	private Cursor getAllMarks() //Added
	{
		String[] column = {KEY_ROWID};
		String where = KEY_MARK + " = 1";
		return Db.query(DB_TABLE, column, where, null, null, null, null);
	}

	private Cursor getAllPoints() //Added
	{
		String[] column = {KEY_ROWID};
		String where = KEY_POINT + " = 1";
		return Db.query(DB_TABLE, column, where, null, null, null, null);
	}

	//This is where the database helper lives. It helps when the database is created or upgraded.
	private class dBHelper extends SQLiteOpenHelper
	{
		public dBHelper (Context ctx)
		{
			super(ctx, DB_NAME, null, DATABASE_VERSION);
		}

		//This is called when the database is created.
		@Override
		public void onCreate(SQLiteDatabase db)
		{
			newDb = "1";
		}

		//This is called when the database is out of date.
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
		{
			updateDb = "1"; //Added
			//Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will remove all marked words.");
		}
	}
}
