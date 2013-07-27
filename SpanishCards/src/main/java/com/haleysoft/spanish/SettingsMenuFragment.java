package com.haleysoft.spanish;

/**
 * Created by Haleysoftware on 5/23/13.
 */

import java.util.ArrayList;
import java.util.List;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.speech.RecognizerIntent;
import android.support.v4.app.DialogFragment;

@TargetApi(12)
public class SettingsMenuFragment extends PreferenceFragment
{
	private String prefName = "Guest";
	private int mode = 0;
	private CharSequence[] userNames;
	private CharSequence[] userRows;
	private ArrayList<String> nameList;
	private ArrayList<String> rowList;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		//setRetainInstance(true);
		prefName = getArguments().getString("prefUser", prefName);
		mode = getArguments().getInt("mode", mode);
		getPreferenceManager().setSharedPreferencesName(prefName);
		switch (mode)
		{
			default:
			case 0: //This is to show the main settings
				addPreferencesFromResource(R.xml.mainsettings);
				break;
			case 1: //This is to show the test settings
				addPreferencesFromResource(R.xml.testsettings);
				break;
		}
		if (savedInstanceState != null)
		{
			switch (mode)
			{
				case 0: //This is for the main settings
					rowList = new ArrayList<String>();
					nameList = new ArrayList<String>();
					rowList = savedInstanceState.getStringArrayList("listRow");
					nameList = savedInstanceState.getStringArrayList("listName");
					recoverUserList();
					break;
				case 1: //This is for the test settings

					break;
				default:
			}
		}

		switch (mode)
		{
			case 0: //This is to setup the main settings
				Preference resetMarkButton = (Preference)findPreference("mark_reset_set");
				resetMarkButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
				{
					@Override
					public boolean onPreferenceClick(Preference arg0)
					{
						String title = getString(R.string.setresetmarksmain);
						String text = getString(R.string.settopresetdialog);
						int id = 0;
						showActionDialog(id, prefName, title, text, null);
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
						showActionDialog(id, prefName, title, text, null);
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
						showActionDialog(id, prefName, title, text, null);
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
						showActionDialog(id, prefName, title, text, null);
						return true;
					}
				});
				/*
				Preference howToButton = (Preference)findPreference("howto_set");
				howToButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
					{
						@Override
						public boolean onPreferenceClick(Preference arg0)
						{
							//this is for testing
							String title = "Test How To";
							String text = "This will be for the how to activity to be called.";
							showInfoDialog(prefName, title, text);
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
							showInfoDialog(prefName, title, text);
							return true;
						}
					});

				Preference analyticsCheckButton = (Preference)findPreference("analytics_set");
				analyticsCheckButton.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
				{
					@Override
					public boolean onPreferenceChange(Preference preference, Object newValue)
					{
						GoogleAnalytics myInstance = GoogleAnalytics.getInstance(getActivity());
						myInstance.requestAppOptOut(new AppOptOutCallback()
						{
							   @Override
							   public void reportAppOptOut(boolean optOut)
							   {
							     if (optOut)
							     {
							     // Alert the user that they've opted out.
							     }
							   });
						}
						return true;
					}
				});
				*/
				Preference userGone = (Preference)findPreference("delete_user_set");
				userGone.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

					@Override
					public boolean onPreferenceChange(Preference preference, Object newValue)
					{
						int arrayRow = rowList.indexOf(newValue.toString());
						String newName = nameList.get(arrayRow);

						String text = getString(R.string.setremovedialogconform);
						int id = 4;
						showActionDialog(id, prefName, newName, text, newValue.toString());
						//DialogFragment newDialog = DeleteUserDialog.newInstance(0, prefName, newName, newValue.toString());
						//newDialog.show(((SettingsMenu)getActivity()).theManager, "deleteDialog");
						return true;
					}
				});
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
				((SettingsMenu)getActivity()).updateOrie(newValue.toString());
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
				fillUserList();
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
				savedState.putStringArrayList("listName", nameList);
				savedState.putStringArrayList("listRow", rowList);
				break;
			case 1: //for test settings

				break;
			default:
		}
		super.onSaveInstanceState(savedState);
	}

	private void recoverUserList()
	{
		ListPreference deleteUser = (ListPreference) findPreference("delete_user_set");
		if (nameList.size()>0)
		{
			deleteUser.setEnabled(true);
			userNames = nameList.toArray(new CharSequence[nameList.size()]);
			userRows = rowList.toArray(new CharSequence[rowList.size()]);
			deleteUser.setEntries(userNames);
			deleteUser.setEntryValues(userRows);
		}
		else
		{
			deleteUser.setEnabled(false);
		}
	}

	public void fillUserList()
	{
		ListPreference deleteUser = (ListPreference) findPreference("delete_user_set");
		Cursor cNames = ((SettingsMenu)getActivity()).userdb.getUsers();
		rowList = new ArrayList<String>();
		nameList = new ArrayList<String>();
		while (cNames.moveToNext())
		{
			Long row = cNames.getLong(cNames.getColumnIndex(UserDBFragment.KEY_ROWA));
			String name = cNames.getString(cNames.getColumnIndex(UserDBFragment.KEY_USER));
			if (!name.matches(prefName))
			{
				rowList.add(row.toString());
				nameList.add(name);
			}
		}
		cNames.close();
		((SettingsMenu)getActivity()).userdb.close();
		if (nameList.size()>0)
		{
			deleteUser.setEnabled(true);
			userNames = nameList.toArray(new CharSequence[nameList.size()]);
			userRows = rowList.toArray(new CharSequence[rowList.size()]);
			deleteUser.setEntries(userNames);
			deleteUser.setEntryValues(userRows);
		}
		else
		{
			deleteUser.setEnabled(false);
		}
	}

	private void voiceCheck()
	{
		PackageManager pm = getActivity().getPackageManager();
		List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
		if (activities.size() == 0)
		{
			//Disabled
			Preference voice = (Preference)findPreference("speak_set");
			voice.setEnabled(false);
		}
	}

	/*
	private void showInfoDialog(String uName, String dTitle, String dText)
	{
		DialogFragment newDialog = InfoDialog.newInstance(uName, dTitle, dText);
		newDialog.show(((SettingsMenu)getActivity()).theManager, "infoDialog");
	}
	*/

	private void showActionDialog(int action, String userN, String title, String text, String extra)
	{
		int pref = 0; //settings for 3.0 and up
		DialogFragment newDialog = ActionDialog.newInstance(pref, action, userN, title, text, extra);
		newDialog.show(((SettingsMenu)getActivity()).theManager, "actionDialog");
	}
}
