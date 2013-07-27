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
	//private String licenseFree = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAhf7caMdtA37LLLSVLOrH4DbBU/8VpODxmetrLvVbimpZ7pzadIw/8UcZW9LFSihOcHQkel5Y3qu9QAFH/t87fwAYRbKeRY9nyZ8pNS4eqB+JpKKbI+jUm2aKZ4DYkTG8E8NP4w7FtFioH7+QTGBbLlZh0xz8mHNmHRPm50TIauyDq8x5ULh+me7XbJyfis2m3rzWAQIe9d2U51yMw51DEYN0+yccMKHTrhiA/72veinMfd6WBs3dGNT2jaVzUZ74Sr0iSPNSbuNftQBTwqI7ICFrDXsy5eUz6OsMfiQXKq+9HtgQAAlUuZZnVvGZUCCVY129Tlw6nNt0sODHOfOjmwIDAQAB";
	//private String licensePaid;

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
