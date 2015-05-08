
package com.haleysoft.spanish.databases;

/**
 * Created by Haleysoftware on 5/23/13.
 * Cleaned by Mike Haley on 9/6/13.
 */

import android.app.Fragment;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;

import com.haleysoft.spanish.R;
import com.haleysoft.spanish.WordSwapHelper;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Random;

//This class is no longer being used. Please use WordDBCP for now on.
public class WordDBFragment extends Fragment {
    private static final int demoLevel = 30;
    //private static final int maxLevel = 150;
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


    public WordDBFragment() {

    }

    //Start of the Fragments LifeCycle
    //Called when the Fragment is created
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        dbHelp = new dBHelper(getActivity());
        open();
        if (newDb.contains("1")) {
            try {
                copyDataBase();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (updateDb.contains("1")) {
            backupOldDB();
            close();
            open();
            restoreOldDB();
        }
        close();
    }
    //End of Fragment LifeCycle

    private boolean checkPaid() {
        String mainAppPkg = "com.haleysoft.spanish";
        String keyPkg = "com.haleysoft.spanish.key";
        PackageManager manager = getActivity().getPackageManager();
        int sigMatch = 22;
        if (manager != null) {
            sigMatch = manager.checkSignatures(mainAppPkg, keyPkg);
        }
        return sigMatch == PackageManager.SIGNATURE_MATCH;
    }

    //Open the database
    public WordDBFragment open() throws SQLException {
        //Opens the database but if the database named is not found it will create it.
        Db = dbHelp.getWritableDatabase();
        return this;
    }

    //Closes the Database
    public void close() {
        if (Db != null) {
            Db.close();
        }
    }

    private void backupOldDB() {
        //DataBase is already open and will be closed when finished
        if (markBackup != null) {
            markBackup.clear();
        }
        Cursor markSave = getAllMarks();
        markBackup = new ArrayList<Long>();
        while (markSave.moveToNext()) {
            Long markRowId = markSave.getLong(markSave.getColumnIndex(KEY_ROWID));
            markBackup.add(markRowId);
        }
        markSave.close();

        if (pointBackup != null) {
            pointBackup.clear();
        }
        Cursor pointSave = getAllPoints();
        pointBackup = new ArrayList<Long>();
        while (pointSave.moveToNext()) {
            Long pointRowId = pointSave.getLong(pointSave.getColumnIndex(KEY_ROWID));
            pointBackup.add(pointRowId);
        }
        pointSave.close();

        if (Db != null) {
            Db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE);
        }
        try {
            copyDataBase();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void restoreOldDB() {
        if (markBackup != null) {
            int maxMark = markBackup.size();
            int currentMark = 0;
            while (currentMark < maxMark) {
                Long markRow = markBackup.get(currentMark);
                ContentValues markNow = new ContentValues();
                markNow.put(KEY_MARK, 1);
                Db.update(DB_TABLE, markNow, KEY_ROWID + " = " + markRow, null);
                currentMark++;
            }
        }
        if (pointBackup != null) {
            int maxPoint = pointBackup.size();
            int currentPoint = 0;
            while (currentPoint < maxPoint) {
                Long pointRow = pointBackup.get(currentPoint);
                ContentValues pointNow = new ContentValues();
                pointNow.put(KEY_POINT, 1);
                Db.update(DB_TABLE, pointNow, KEY_ROWID + " = " + pointRow, null);
                currentPoint++;
            }
        }
    }

    //Copies the Database from the Assets folder
    public void copyDataBase() throws IOException {
        Resources resources = getResources();
        //This is the path to the empty database.
        String outFileName = Db.getPath();
        //Opens the empty database as the output stream.
        OutputStream myOutput = new FileOutputStream(outFileName);
        //Open the premade assets database as the input stream.
        InputStream myInput = resources.getAssets().open(DB_NAME);
        //This transfers the bytes from the inputfile to the outputfile.
        byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer)) > 0) {
            myOutput.write(buffer, 0, length);
        }
        //Close the streams when finished.
        myOutput.flush();
        myOutput.close();
        myInput.close();
    }

    //Counts the number of rows in the words database so random will not pick something that is not there
    public int countEntries() {
        int rows = 1;
        open();
        Cursor cursor = Db.rawQuery("SELECT COUNT(_id) FROM SpanishWords", null);
        if (cursor.moveToFirst()) {
            rows = cursor.getInt(0);
        }
        close();
        cursor.close();
        return rows;
    }

    //Updates the mark column of the current displayed row
    public void updateMark(long rowId, int changeMark) {
        open();
        ContentValues markUp = new ContentValues();
        markUp.put(KEY_MARK, changeMark);
        Db.update(DB_TABLE, markUp, KEY_ROWID + "=" + rowId, null);
        close();
    }

    public void resetMark() {
        open();
        ContentValues markUp = new ContentValues();
        markUp.put(KEY_MARK, 0);
        Db.update(DB_TABLE, markUp, null, null);
        close();
    }

    private Cursor getAllMarks() {
        String[] column = {KEY_ROWID};
        String where = KEY_MARK + " = 1";
        return Db.query(DB_TABLE, column, where, null, null, null, null);
    }

    //Updates the point column of the current displayed row
    public void updatePoint(long rowId) {
        open();
        ContentValues pointUp = new ContentValues();
        pointUp.put(KEY_POINT, 1);
        Db.update(DB_TABLE, pointUp, KEY_ROWID + "=" + rowId, null);
        close();
    }

    public void resetPoint() {
        open();
        ContentValues pointUp = new ContentValues();
        pointUp.put(KEY_POINT, 0);
        Db.update(DB_TABLE, pointUp, null, null);
        close();
    }

    private Cursor getAllPoints() {
        String[] column = {KEY_ROWID};
        String where = KEY_POINT + " = 1";
        return Db.query(DB_TABLE, column, where, null, null, null, null);
    }

    //This is called to pull a random row from the database.
    public Cursor getRandomWord(String select, boolean freePlay, int userLevel) throws IOException {
        if (select == null) {
            throw new IOException("Word DB select is null");
        }
        //This is used to change the sorting options.
        String sort;
        //Creates the variable that will be used to pick what row to return.
        String where;
        String[] key;
        //Sets the random number
        Random random = new Random();
        //Sets id to the total number of rows in the database.
        int id = countEntries();
        //pick a random number that is equal or between 1 and the number of rows.
        int rand = random.nextInt(id) + 1;
        //This will run if All is selected on the spinner.
        if (select.matches(getString(R.string.arrayAll))) { //contains
            if (freePlay) {
                if (checkPaid()) {
                    where = KEY_ROWID + " = ?";
                    key = new String[]{String.valueOf(rand)};
                    sort = "null";
                } else {
                    where = KEY_LEVEL + " <= ?";
                    key = new String[]{String.valueOf(demoLevel)};
                    sort = "Random()";
                }
            } else {
                where = KEY_LEVEL + " <= ?";
                key = new String[]{String.valueOf(userLevel)};
                sort = "Random()";
            }
        } else if (select.matches(getString(R.string.arrayMarked))) { //contains
            if (freePlay) {
                if (checkPaid()) {
                    where = KEY_MARK + " = ?";
                    key = new String[]{"1"};
                } else {
                    where = KEY_MARK + " = ? AND " + KEY_LEVEL + " <= ?";
                    key = new String[]{"1", String.valueOf(demoLevel)};
                }
            } else {
                where = KEY_LEVEL + " <= ? AND " + KEY_MARK + " = ?";
                key = new String[]{String.valueOf(userLevel), "1"};
            }
            sort = "Random()";
        } else if (select.matches(getString(R.string.arrayLevel))) { //contains
            where = KEY_LEVEL + " <= ?";
            key = new String[]{String.valueOf(userLevel)};
            sort = "Random()";
        } else if (select.matches(getString(R.string.arrayPoints))) { //contains
            if (freePlay) {
                if (checkPaid()) {
                    where = KEY_POINT + " = ?";
                    key = new String[]{"0"};
                } else {
                    where = KEY_POINT + " = ? AND " + KEY_LEVEL + " <= ?";
                    key = new String[]{"0", String.valueOf(demoLevel)};
                }
            } else {
                where = KEY_LEVEL + " <= ? AND " + KEY_POINT + " = ?";
                key = new String[]{String.valueOf(userLevel), "0"};
            }
            sort = "Random()";
        } else {
            String search = WordSwapHelper.cateStringToCode(getActivity(), select);
            if (freePlay) {
                if (checkPaid()) {
                    where = KEY_CAT + " LIKE ?";
                    key = new String[]{search};
                } else {
                    where = KEY_CAT + " LIKE ? AND " + KEY_LEVEL + " <= ?";
                    key = new String[]{search, String.valueOf(demoLevel)};
                }
            } else {
                where = KEY_LEVEL + " <= ? AND " + KEY_CAT + " = ?";
                key = new String[]{String.valueOf(userLevel), search};
            }
            sort = "Random()";
        }
        //open the database.
        open();
        return Db.query(DB_TABLE, null, where, key, null, null, sort, "1");
    }

    //This is where the database helper lives. It helps when the database is created or upgraded.
    private class dBHelper extends SQLiteOpenHelper {
        public dBHelper(Context ctx) {
            super(ctx, DB_NAME, null, DATABASE_VERSION);
        }

        //This is called when the database is created.
        @Override
        public void onCreate(SQLiteDatabase db) {
            newDb = "1";
        }

        //This is called when the database is out of date.
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            updateDb = "1"; //Added
            //Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will remove all marked words.");
        }
    }
}
