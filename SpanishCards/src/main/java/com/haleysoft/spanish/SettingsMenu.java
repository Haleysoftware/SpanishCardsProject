package com.haleysoft.spanish;

/**
 * Created by Haleysoftware on 5/23/13.
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

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class SettingsMenu extends FragmentActivity
{
	FragmentManager theManager = getSupportFragmentManager();
	SettingsMenuFragment setting;
	public UserDBFragment userdb;
	private String prefName = "Guest";
	private int mode = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		if (savedInstanceState == null)
		{
			addFragments();
		}
		prefName = getIntent().getExtras().getString("prefUser");
		mode = getIntent().getExtras().getInt("mode");
		SharedPreferences preferences = getSharedPreferences(prefName, MODE_PRIVATE);
		boolean theme = preferences.getBoolean("theme_set", false);
		String orientationTest = preferences.getString("orie_list_set", "0");

		if (theme)
		{
			setTheme(R.style.ActivityThemeAlt);
		}
		else
		{
			setTheme(R.style.ActivityTheme);
		}
		updateOrie(orientationTest);

	}

	@Override
	public void onStart()
	{
		super.onStart();
		userdb = (UserDBFragment) theManager.findFragmentByTag("userFragment");
	}

	private void addFragments()
	{

		FragmentTransaction theTransaction = theManager.beginTransaction();

		//I don't know why the wordDB is added
		WordDBFragment worddb = new WordDBFragment();
		theTransaction.add(worddb, "wordFragment");

		UserDBFragment userdb = new UserDBFragment();
		theTransaction.add(userdb, "userFragment");

		theTransaction.commit();
	}

	public void updateOrie(String orientationTest)
	{
		int orieTest = Integer.parseInt(orientationTest);
		switch (orieTest)
		{
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

	public void onDialogOkay(int actionID, String dTitle, String extra)
	{
		switch (actionID)
		{
			case 0: //Reset marked words
				WordDBFragment worddb = (WordDBFragment) theManager.findFragmentByTag("wordFragment");
				worddb.resetMark();
				break;
			case 1: //Reset user level
				SharedPreferences pref = getSharedPreferences(prefName, MODE_PRIVATE);
				pref.edit().putInt("user_level", 1).commit();
				pref.edit().putInt("level_points", 0).commit();
				break;
			case 2: //Clear user scores
				userdb.open();
				userdb.removeScore(prefName);
				userdb.close();
				break;
			case 3: //Clear all scores
				userdb.emptyScore();
				break;
			case 4: //Delete user verify
				DialogFragment newDialog = DeleteUserDialog.newInstance(0, prefName, dTitle, extra);
				newDialog.show(theManager, "deleteDialog");
				break;
			default:
				//nothing
		}
	}

	public void removeUser(String noName, String noRow, boolean clearScores)
	{
		userdb.open();

		if (clearScores)
		{
			userdb.removeScore(noName);
		}
		if (userdb.deleteUser(noRow))
		{
			Toast.makeText(this, noName  + " was removed!", Toast.LENGTH_SHORT).show();
		}
		else
		{
			Toast.makeText(this, noName + " at " + noRow + " was not removed!", Toast.LENGTH_SHORT).show();
		}
		userdb.close();
		setting.fillUserList();
	}
}
