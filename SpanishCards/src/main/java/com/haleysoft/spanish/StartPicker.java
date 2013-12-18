package com.haleysoft.spanish;

/**
 * Created by Haleysoftware on 05/23/13.
 * Cleaned by Mike Haley on 08/29/13.
 * Removed IAP on 12/18/13
 */

import android.app.Activity;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.content.Intent;
import android.content.SharedPreferences;

import com.google.analytics.tracking.android.EasyTracker;

public class StartPicker extends Activity {
	private static final String MASTER_SETTINGS = "haley_master_set";
	private SharedPreferences masterPref;
	private SharedPreferences preferences;
	private boolean analytics = false;
	private String userName = "Guest";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setupPref();
		//Check to go right to test mode
		masterPref = getSharedPreferences(MASTER_SETTINGS, MODE_PRIVATE);
		userName = masterPref.getString("last_user_set", "Guest");
		preferences = getSharedPreferences(userName, MODE_PRIVATE);
	}

	@Override
	public void onStart() {
		super.onStart();
		boolean testing = preferences.getBoolean("return_test_set", false);
		analytics = preferences.getBoolean("analytics_set", false);
		if (analytics) {
			EasyTracker.getInstance().activityStart(this);
		}
		if (testing) {
			goTest();
		} else {
			goSelect();
		}
	}

	@Override
	public void onStop() {
		super.onStop();
		if (analytics) {
			EasyTracker.getInstance().activityStop(this);
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
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
