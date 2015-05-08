package com.haleysoft.spanish.databases;

/**
 * Created by Haleysoftware on 5/23/13.
 * Cleaned by Mike Haley on 9/6/13.
 */

import android.app.Fragment;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;

import com.haleysoft.spanish.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

//This class is no longer being used. Please use UserDBCP for now on.
public class UserDBFragment extends Fragment {
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

    //private static final String TAG = "UserAdapter";
    private DatabaseHelper dbHelper;
    private SQLiteDatabase db;

    public final static String DB_CREATEA = "create table " + DB_TABLEA + " (" + KEY_ROWA + " integer primary key autoincrement, " + KEY_USER + " text, " + KEY_LAST + " integer);";
    public final static String DB_CREATEB = "create table " + DB_TABLEB + " (" + KEY_ROWB + " integer primary key autoincrement, " + KEY_NAME + " text, " + KEY_DATE + " text, " + KEY_SCORE + " integer, " + KEY_MODE + " text, " + KEY_HSCORE + " integer);";


    public UserDBFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        dbHelper = new DatabaseHelper(getActivity());
    }

    public UserDBFragment open() throws SQLException {
        db = dbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        if (db != null) {
            db.close();
        }
    }

    public long addScore(String name, int score, String show, String hide) {
        SharedPreferences preferences = getActivity().getSharedPreferences(name, 0);
        boolean pointTest = preferences.getBoolean("point_set", false);
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy", Locale.US);
        String currentDate = dateFormat.format(new Date());
        String type;
        String userName;
        if (name.matches("Guest")) {
            userName = getString(R.string.gName);
        } else {
            userName = name;
        }
        open();
        if (pointTest) { //User wants one point for each word
            type = String.format(getString(R.string.scoreNameLimited), show, hide);
            //type = "Limited " + show + " to " + hide;
        } else { //User wants a point for each word
            type = String.format(getString(R.string.scoreNameUnlimited), show, hide);
            //type = "Unlimited " + show + " to " + hide;
        }
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_NAME, userName);
        initialValues.put(KEY_DATE, currentDate);
        initialValues.put(KEY_SCORE, score);
        initialValues.put(KEY_MODE, type);
        initialValues.put(KEY_HSCORE, 0);
        long row = db.insert(DB_TABLEB, null, initialValues);
        close();
        return row;
    }

    public void updateScore(long id, int score) {
        open();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy", Locale.US);
        String currentDate = dateFormat.format(new Date());
        ContentValues scoreUp = new ContentValues();
        scoreUp.put(KEY_DATE, currentDate);
        scoreUp.put(KEY_SCORE, score);
        db.update(DB_TABLEB, scoreUp, KEY_ROWB + "=" + id, null);
        close();
    }

    public void cleanScore() {
        //resets the High Score to zero
        ContentValues cleanUp = new ContentValues();
        cleanUp.put(KEY_HSCORE, "0");
        open();
        db.update(DB_TABLEB, cleanUp, null, null);

        //Guest top scores
        SharedPreferences guestPreferences = getActivity().getSharedPreferences("Guest", 0);
        String keepGuestScore = guestPreferences.getString("score_list_set", "15");
        String gWhere = KEY_NAME + " LIKE ?";
        String[] key1 = {getString(R.string.gName)};
        Cursor gTop = db.query(DB_TABLEB, null, gWhere, key1, null, null, KEY_SCORE + " DESC", keepGuestScore);
        while (gTop.moveToNext()) {
            Long gID = gTop.getLong(gTop.getColumnIndex(KEY_ROWB));
            ContentValues gScore = new ContentValues();
            gScore.put(KEY_HSCORE, "1");
            db.update(DB_TABLEB, gScore, KEY_ROWB + "=" + gID, null);
        }
        gTop.close();

        //Users top scores
        Cursor names = db.query(DB_TABLEA, null, null, null, null, null, KEY_USER + " COLLATE NOCASE ASC");
        while (names.moveToNext()) { //Loop to get the next user name
            String user = names.getString(names.getColumnIndex(KEY_USER));
            SharedPreferences userPreferences = getActivity().getSharedPreferences(user, 0);
            String keepUserScore = userPreferences.getString("score_list_set", "15");
            String userWhere = KEY_NAME + " LIKE ?";
            String[] key2 = {user};
            Cursor top = db.query(DB_TABLEB, null, userWhere, key2, null, null, KEY_SCORE + " DESC", keepUserScore);
            while (top.moveToNext()) { //ID of top scores for each user
                Long ID = top.getLong(top.getColumnIndex(KEY_ROWB));
                ContentValues markScore = new ContentValues();
                markScore.put(KEY_HSCORE, "1");
                db.update(DB_TABLEB, markScore, KEY_ROWB + "=" + ID, null);
            }
            top.close();
        }
        names.close();

        //Remove unwanted scores
        db.delete(DB_TABLEB, KEY_HSCORE + "<?", new String[]{"1"});
        close();
    }

    public Cursor topScore(String show, String hide, String userName) {
        open();
        String topWhere;
        if (show == null || hide == null) {
            topWhere = null;
        } else {
            SharedPreferences preferences = getActivity().getSharedPreferences(userName, 0);
            boolean pointTest = preferences.getBoolean("point_set", false);
            String type;
            String showText;
            String hideText;
            if (show.matches("Spanish")) {
                showText = getString(R.string.langSpanish);
            } else { //English
                showText = getString(R.string.langEnglish);
            }
            if (hide.matches("Spanish")) {
                hideText = getString(R.string.langSpanish);
            } else { //English
                hideText = getString(R.string.langEnglish);
            }
            if (pointTest) { //User wants one point for each word
                type = String.format(getString(R.string.scoreNameLimited), showText, hideText);
            } else { //User wants a point for each word
                type = String.format(getString(R.string.scoreNameUnlimited), showText, hideText);
            }
            topWhere = KEY_MODE + " LIKE '" + type + "%'";
        }
        //DB needs to be closed
        return db.query(DB_TABLEB, null, topWhere, null, null, null, KEY_SCORE + " DESC", "1");
    }

    public void removeScore(String name) {
        db.delete(DB_TABLEB, KEY_NAME + "=?", new String[]{name});
    }

    public void emptyScore() {
        open();
        db.delete(DB_TABLEB, KEY_HSCORE + "<?", new String[]{"2"});
        close();
    }

    public boolean addUser(String name) {
        open();
        boolean check;
        String where = KEY_USER + " LIKE '" + name + "%'";
        Cursor cursor = db.query(DB_TABLEA, null, where, null, null, null, null, "1");
        if (cursor.moveToFirst()) {
            check = false;
        } else {
            check = true;
            //Adds the new user
            ContentValues initialValues = new ContentValues();
            initialValues.put(KEY_USER, name);
            db.insert(DB_TABLEA, null, initialValues);
        }
        cursor.close();
        close();
        return check;
    }

    public boolean deleteUser(String row) {
        return db.delete(DB_TABLEA, KEY_ROWA + "=?", new String[]{row}) > 0;
    }

    public Cursor getUsers() {
        open();
        //DB needs to be closed
        return db.query(DB_TABLEA, new String[]{KEY_ROWA, KEY_USER}, null, null, null, null, KEY_USER + " COLLATE NOCASE ASC");
    }

    //Not yet used
    /*
	public boolean updateUser(String row, String newName)
	{
		//open();
		//Database needs to be opened and closed
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_USER, newName);
		return db.update(DB_TABLE, initialValues, KEY_ROW + "=?", new String[] {row}) > 0;
	}
	*/

    private class DatabaseHelper extends SQLiteOpenHelper {
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
