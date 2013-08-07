package com.haleysoft.spanish;

/**
 * Created by Haleysoftware on 5/23/13.
 */

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.FragmentActivity;

public class StartPicker extends FragmentActivity
{
	private static final String MASTER_SETTINGS = "haley_master_set";
	private String userName = "Guest";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setupPref();
		//Check to go right to test mode
		SharedPreferences masterPref = getSharedPreferences(MASTER_SETTINGS, MODE_PRIVATE);
		userName = masterPref.getString("last_user_set", "Guest");
		SharedPreferences preferences = getSharedPreferences(userName, MODE_PRIVATE);
		boolean testing = preferences.getBoolean("return_test_set", false);
		if (testing)
		{
			goTest();
		}
		else
		{
			goSelect();
		}

	}

	private void setupPref()
	{
		PreferenceManager.setDefaultValues(this, MASTER_SETTINGS, MODE_PRIVATE, R.xml.programsettings, false);
		PreferenceManager.setDefaultValues(this, userName, MODE_PRIVATE, R.xml.mastersettings, false);
	}


	public void goTest()
	{
		Intent test = new Intent(this, TestMain.class);
		test.putExtra("user", userName);
		startActivity(test);
		finish();
	}

	public void goSelect()
	{
		Intent select = new Intent(this, TestSelect.class);
		startActivity(select);
		finish();
	}
}
