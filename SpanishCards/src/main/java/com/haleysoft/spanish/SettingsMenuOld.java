package com.haleysoft.spanish;

/**
 * Created by Haleysoftware on 5/23/13.
 */

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.speech.RecognizerIntent;

import java.util.List;

public class SettingsMenuOld extends PreferenceActivity
{
	public static String prefName = "Guest";
	private int mode = 0;


	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle extras = getIntent().getExtras();
		prefName = extras.getString("prefUser");
		mode = extras.getInt("mode");
		SharedPreferences preferences = getSharedPreferences(prefName, MODE_PRIVATE);
		boolean theme = preferences.getBoolean("theme_set", false);
		if (theme)
		{
			setTheme(R.style.ActivityThemeAlt);
		}
		else
		{
			setTheme(R.style.ActivityTheme);
		}
		String orientationTest = preferences.getString("orie_list_set", "0");
		orientation(orientationTest);

		getPreferenceManager().setSharedPreferencesName(prefName);

		switch (mode)
		{
			default:
			case 0:
				addPreferencesFromResource(R.xml.oldmainsettings);
				break;
			case 1:
				addPreferencesFromResource(R.xml.testsettings);
				break;

		}

		if (savedInstanceState != null)
		{
			switch (mode)
			{
				case 0: //This is for the main settings

					break;
				case 1: //This is for the test settings

					break;
				default:
			}
		}

		switch (mode)
		{
			case 0: //This is to setup the main settings

				Preference resetButton = (Preference)findPreference("mark_reset_set");
				resetButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
				{
					@Override
					public boolean onPreferenceClick(Preference arg0) {
						//Code goes here
						Intent reset = new Intent(SettingsMenuOld.this, OldSetDelete.class);
						reset.putExtra("uName", prefName);
						startActivity(reset);
						return true;
					}
				});

				/*
				Preference resetMarkButton = (Preference)findPreference("mark_reset_set");
				resetMarkButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
					{
						@Override
						public boolean onPreferenceClick(Preference arg0)
						{
							String title = getString(R.string.setresetmarksmain);
							String text = getString(R.string.settopresetdialog);
							int id = 0;
							remove.showActionDialog(id, prefName, title, text, null);
							return true;
						}
					});
				Preference resetLevelButton = (Preference)findPreference("level_reset_set");
				resetLevelButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
					{
						@Override
						public boolean onPreferenceClick(Preference arg0)
						{
							String title = getString(R.string.setresetlevelmain);
							String text = getString(R.string.settopresetdialog);
							int id = 1;
							remove.showActionDialog(id, prefName, title, text, null);
							return true;
						}
					});
				Preference clearUserScoreButton = (Preference)findPreference("clear_user_score_set");
				clearUserScoreButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
					{
						@Override
						public boolean onPreferenceClick(Preference arg0)
						{
							String title = getString(R.string.setclearuserscoremain);
							String text = getString(R.string.settopresetdialog);
							int id = 2;
							remove.showActionDialog(id, prefName, title, text, null);
							return true;
						}
					});
				Preference clearAllScoreButton = (Preference)findPreference("clear_all_score_set");
				clearAllScoreButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
					{
						@Override
						public boolean onPreferenceClick(Preference arg0)
						{
							String title = getString(R.string.setclearallscoresmain);
							String text = getString(R.string.settopresetdialog);
							int id = 3;
							remove.showActionDialog(id, prefName, title, text, null);
							return true;
						}
					});

				Preference howToButton = (Preference)findPreference("howto_set");
				howToButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
					{
						@Override
						public boolean onPreferenceClick(Preference arg0)
						{
							//this is for testing
							String title = "Test How To";
							String text = "This will be for the how to activity to be called.";
							remove.showInfoDialog(prefName, title, text);
							return true;
						}
					});
				Preference aboutButton = (Preference)findPreference("about_set");
				aboutButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
					{
						@Override
						public boolean onPreferenceClick(Preference arg0)
						{
							//this is for testing
							String title = "Test about";
							String text = "This will be for the how to activity to be called.";
							remove.showInfoDialog(prefName, title, text);
							return true;
						}
					});
				Preference analyticsCheckButton = (Preference)findPreference("analytics_set");
				analyticsCheckButton.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
				{
					@Override
					public boolean onPreferenceChange(Preference preference, Object newValue)
					{
						String title = getString(R.string.setdatasendmain);
						String text = getString(R.string.setdatasenddialog);
						if (newValue.toString().equals("true"))
						{
							remove.showInfoDialog(prefName, title, text);
						}
						return true;
					}
				});

				Preference userGone = (Preference)findPreference("delete_user_set");
				userGone.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

					@Override
					public boolean onPreferenceChange(Preference preference, Object newValue)
					{
						int arrayRow = rowList.indexOf(newValue.toString());
						String newName = nameList.get(arrayRow);

						String text = getString(R.string.setremovedialogconform);
						int id = 4;
						remove.showActionDialog(id, prefName, newName, text, newValue.toString());
						fillUserList();
						//DialogFragment newDialog = DeleteUserDialog.newInstance(0, prefName, newName, newValue.toString());
						//newDialog.show(((SettingsMenu)getActivity()).theManager, "deleteDialog");
						return true;
					}
				});
				*/
				break;
			case 1: //This is to setup the test settings

				break;
			default:
		}

		//This is where to setup that is common to all settings

		Preference changeOrie = (Preference)findPreference("orie_list_set");
		changeOrie.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
		{
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue)
			{
				orientation(newValue.toString());
				return true;
			}
		});

	}

	@Override
	public void onStart()
	{
		super.onStart();
		switch (mode)
		{
			case 0: //This is for the main settings

				break;
			case 1: //This is for the test settings

				break;
			default:
		}
		voiceCheck();
	}

	@Override
	public void onSaveInstanceState (Bundle savedState)
	{
		switch (mode)
		{
			case 0: //for main settings

				break;
			case 1: //for test settings

				break;
			default:
		}
		super.onSaveInstanceState(savedState);
	}

	@SuppressWarnings("deprecation")
	private void voiceCheck()
	{
		PackageManager pm = getPackageManager();
		List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
		if (activities.size() == 0)
		{
			//Disabled
			Preference voice = (Preference)findPreference("speak_set");
			voice.setEnabled(false);
			//voice.setSummary(R.string.noVoice);
		}
	}

	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	private void orientation(String orientationTest)
	{
		int orieTest = Integer.parseInt(orientationTest);
		if (Build.VERSION.SDK_INT<Build.VERSION_CODES.GINGERBREAD) //For old OS
		{
			switch (orieTest)
			{
				case 0:
					this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
					break;
				case 1:
					this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
					break;
				case 2:
					this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
					break;
				default:
			}
		}
		else
		{
			switch (orieTest)
			{
				case 0:
					this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
					break;
				case 1:
					this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
					break;
				case 2:
					this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
					break;
				default:
			}
		}
	}
}
