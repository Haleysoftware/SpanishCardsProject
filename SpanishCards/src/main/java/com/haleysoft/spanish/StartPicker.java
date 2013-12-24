package com.haleysoft.spanish;

/**
 * Created by Haleysoftware on 05/23/13.
 * Cleaned by Mike Haley on 08/29/13.
 * Removed IAP on 12/18/13
 */

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

public class StartPicker extends Activity {
	private static final String MASTER_SETTINGS = "haley_master_set";
	//private SharedPreferences masterPref;
	private SharedPreferences preferences;
	private String userName = "Guest";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setupPref();
		//Check to go right to test mode
		SharedPreferences masterPref = getSharedPreferences(MASTER_SETTINGS, MODE_PRIVATE);
		userName = masterPref.getString("last_user_set", "Guest");
		preferences = getSharedPreferences(userName, MODE_PRIVATE);
	}

	@Override
	public void onStart() {
		super.onStart();
		boolean testing = preferences.getBoolean("return_test_set", false);
		if (testing) {
			goTest();
		} else {
			goSelect();
		}
	}

	private void setupPref() {
		PreferenceManager.setDefaultValues(this, MASTER_SETTINGS, MODE_PRIVATE, R.xml.programsettings, false);
		PreferenceManager.setDefaultValues(this, userName, MODE_PRIVATE, R.xml.mastersettings, false);
	}

	public void goTest() {
		Intent test = new Intent(this, TestMain.class);
		test.putExtra("user", userName);
		startActivity(test);
		finish();
	}

	public void goSelect() {
		Intent select = new Intent(this, TestSelect.class);
		startActivity(select);
		finish();
	}
}
