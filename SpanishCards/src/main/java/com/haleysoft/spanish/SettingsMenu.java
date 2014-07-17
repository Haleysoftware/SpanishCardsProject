package com.haleysoft.spanish;

/**
 * Created by Haleysoftware on 5/23/13.
 * Cleaned by Mike Haley on 8/27/13.
 */

import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.widget.Toast;

import com.haleysoft.spanish.databases.UserDBFragment;
import com.haleysoft.spanish.databases.WordDBFragment;
import com.haleysoft.spanish.dialogs.DeleteUserDialog;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class SettingsMenu extends FragmentActivity {
    FragmentManager theManager = getSupportFragmentManager();
    SettingsMenuFragment setting;
    public UserDBFragment userDB;
    private String prefName = "Guest";
    private int mode = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            addFragments();
        }
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            prefName = extras.getString("prefUser");
            mode = extras.getInt("mode");
        }
        SharedPreferences preferences = getSharedPreferences(prefName, MODE_PRIVATE);
        boolean theme = preferences.getBoolean("theme_set", false);
        String orientationTest = preferences.getString("orie_list_set", "0");
        setTheme(theme ? R.style.ActivityThemeAlt : R.style.ActivityTheme);
        updateOrie(orientationTest);
    }

    @Override
    public void onStart() {
        super.onStart();
        userDB = (UserDBFragment) theManager.findFragmentByTag("userFragment");
    }

    private void addFragments() {
        FragmentTransaction theTransaction = theManager.beginTransaction();
        //Word DB is for resetting marked words
        WordDBFragment words = new WordDBFragment();
        UserDBFragment users = new UserDBFragment();
        theTransaction.add(words, "wordFragment");
        theTransaction.add(users, "userFragment");
        theTransaction.commit();
    }

    public void updateOrie(String orientationTest) {
        int orieTest = Integer.parseInt(orientationTest);
        switch (orieTest) {
            case 0:
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                break;
            case 1:
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
                break;
            case 2:
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
                break;
            default:
        }

        Bundle arg = new Bundle();
        arg.putCharSequence("prefUser", prefName);
        arg.putInt("mode", mode);
        setting = new SettingsMenuFragment();
        setting.setArguments(arg);
        getFragmentManager().beginTransaction().replace(android.R.id.content, setting).commit();
    }

    public void onDialogOkay(int actionID, String dTitle, String extra) {
        switch (actionID) {
            case 0: //Reset marked words
                WordDBFragment wordDB = (WordDBFragment) theManager.findFragmentByTag("wordFragment");
                wordDB.resetMark();
                break;
            case 1: //Reset user level
                SharedPreferences pref = getSharedPreferences(prefName, MODE_PRIVATE);
                pref.edit().putInt("user_level", 1).commit();
                pref.edit().putInt("level_points", 0).commit();
                break;
            case 2: //Clear user scores
                userDB.open();
                userDB.removeScore(prefName);
                userDB.close();
                break;
            case 3: //Clear all scores
                userDB.emptyScore();
                break;
            case 4: //Delete user verify
                DialogFragment newDialog = DeleteUserDialog.newInstance(0, prefName, dTitle, extra);
                newDialog.show(theManager, "deleteDialog");
                break;
            default:
                //nothing
        }
    }

    public void removeUser(String noName, String noRow, boolean clearScores) {
        userDB.open();
        if (clearScores) {
            userDB.removeScore(noName);
        }
        if (userDB.deleteUser(noRow)) {
            Toast.makeText(this, noName + " was removed!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, noName + " at " + noRow + " was not removed!", Toast.LENGTH_SHORT).show();
        }
        userDB.close();
        setting.fillUserList();
    }

    public void updateScreen() {
        setting.updateTts();
    }
}
