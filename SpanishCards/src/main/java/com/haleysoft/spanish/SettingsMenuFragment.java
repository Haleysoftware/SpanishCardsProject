package com.haleysoft.spanish;

/**
 * Created by Haleysoftware on 5/23/13.
 * Cleaned by Mike Haley on 8/28/13.
 */

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.support.v4.app.DialogFragment;

import java.util.ArrayList;
import java.util.List;

@TargetApi(12)
public class SettingsMenuFragment extends PreferenceFragment implements OnPreferenceChangeListener, OnPreferenceClickListener {
	private static final String MASTER_SETTINGS = "haley_master_set";
	private String prefName = "Guest";
	private Context ctx;
	private SharedPreferences masterPref = null;
	private int mode = 0;
	private CharSequence[] userNames;
	private CharSequence[] userRows;
	private ArrayList<String> nameList;
	private ArrayList<String> rowList;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ctx = getActivity();
		if (ctx != null) {
			masterPref = ctx.getSharedPreferences(MASTER_SETTINGS, Context.MODE_PRIVATE);
		}
		Bundle arg = getArguments();
		if (arg != null) {
			prefName = arg.getString("prefUser", prefName);
			mode = arg.getInt("mode", mode);
		}
		PreferenceManager prefMan = getPreferenceManager();
		if (prefMan != null && prefName != null) {
			prefMan.setSharedPreferencesName(prefName);
		}
		switch (mode) {
			default:
			case 0: //This is to show the main settings
				addPreferencesFromResource(R.xml.mainsettings);
				break;
			case 1: //This is to show the test settings
				addPreferencesFromResource(R.xml.testsettings);
				break;
		}
		if (savedInstanceState != null) {
			switch (mode) {
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
		setListeners();
	}

	@Override
	public void onStart() {
		super.onStart();
		switch (mode) {
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
	public void onSaveInstanceState (Bundle savedState) {
		super.onSaveInstanceState(savedState);
		switch (mode) {
			case 0: //for main settings
				savedState.putStringArrayList("listName", nameList);
				savedState.putStringArrayList("listRow", rowList);
				break;
			case 1: //for test settings

				break;
			default:
		}
	}

	private void setListeners() {
		Preference shopButton = findPreference("buy_set");
		if (shopButton != null && masterPref != null) {
			if (masterPref.getBoolean("buy_okay", false)) {
				shopButton.setEnabled(true);
				shopButton.setSummary(R.string.setbuysum);
				shopButton.setOnPreferenceClickListener(this);
			} else {
				shopButton.setEnabled(false);
				shopButton.setSummary(R.string.setbuyoff);
			}
		}
		Preference changeOrie = findPreference("orie_list_set");
		if (changeOrie != null) {
			changeOrie.setOnPreferenceChangeListener(this);
		}
		switch (mode) {
			case 0: //This is to setup the main settings
				Preference resetMarkButton = findPreference("mark_reset_set");
				if (resetMarkButton != null) {
					resetMarkButton.setOnPreferenceClickListener(this);
				}
				Preference resetLevelButton = findPreference("level_reset_set");
				if (resetLevelButton != null) {
					resetLevelButton.setOnPreferenceClickListener(this);
				}
				Preference clearUserScoreButton = findPreference("clear_user_score_set");
				if (clearUserScoreButton != null) {
					clearUserScoreButton.setOnPreferenceClickListener(this);
				}
				Preference clearAllScoreButton = findPreference("clear_all_score_set");
				if (clearAllScoreButton != null) {
					clearAllScoreButton.setOnPreferenceClickListener(this);
				}
				Preference userGone = findPreference("delete_user_set");
				if (userGone != null) {
					userGone.setOnPreferenceChangeListener(this);
				}
				break;
			case 1: //This is to setup the test settings

				break;
			default:
		}
	}

	public boolean onPreferenceClick(Preference pref) {
		boolean usedClick = false;
		String key = pref.getKey();
		String title = null;
		String text = null;
		int id = 0;
		if (key != null) {
			if (key.contentEquals("mark_reset_set")) {
				title = getString(R.string.setresetmarksmain);
				text = getString(R.string.settopresetdialog);
				id = 0;
				usedClick = true;
			} else if (key.contentEquals("level_reset_set")) {
				title = getString(R.string.setresetlevelmain);
				text = getString(R.string.settopresetdialog);
				id = 1;
				usedClick = true;
			} else if (key.contentEquals("clear_user_score_set")) {
				title = getString(R.string.setclearuserscoremain);
				text = getString(R.string.settopresetdialog);
				id = 2;
				usedClick = true;
			} else if (key.contentEquals("clear_all_score_set")) {
				title = getString(R.string.setclearallscoresmain);
				text = getString(R.string.settopresetdialog);
				id = 3;
				usedClick = true;
			} else if (key.contentEquals("howto_set")) {

				usedClick = true;
			} else if (key.contentEquals("about_set")) {

				usedClick = true;
			}
			if (title != null && text != null) {
				showActionDialog(id, prefName, title, text, null);
			}
		}
		return usedClick;
	}

	public boolean onPreferenceChange(Preference pref, Object newValue) {
		String key = pref.getKey();
		if (key != null) {
			if (key.contentEquals("orie_list_set")) {
				((SettingsMenu)ctx).updateOrie(newValue.toString());
				return true;
			} else if (key.contentEquals("analytics_set")) {

				return true;
			} else if (key.contentEquals("delete_user_set")) {
				int arrayRow = rowList.indexOf(newValue.toString());
				String newName = nameList.get(arrayRow);

				String text = getString(R.string.setremovedialogconform);
				showActionDialog(4, prefName, newName, text, newValue.toString());
				return true;
			}
		}
		return false;
	}

	private void recoverUserList() {
		ListPreference deleteUser = (ListPreference) findPreference("delete_user_set");
		if (deleteUser != null) {
			if (nameList.size()>0) {
				deleteUser.setEnabled(true);
				userNames = nameList.toArray(new CharSequence[nameList.size()]);
				userRows = rowList.toArray(new CharSequence[rowList.size()]);
				deleteUser.setEntries(userNames);
				deleteUser.setEntryValues(userRows);
			} else {
				deleteUser.setEnabled(false);
			}
		}
	}

	public void fillUserList() {
		ListPreference deleteUser = (ListPreference) findPreference("delete_user_set");
		Cursor cNames = ((SettingsMenu)ctx).userDB.getUsers();
		rowList = new ArrayList<String>();
		nameList = new ArrayList<String>();
		while (cNames.moveToNext())	{
			Long row = cNames.getLong(cNames.getColumnIndex(UserDBFragment.KEY_ROWA));
			String name = cNames.getString(cNames.getColumnIndex(UserDBFragment.KEY_USER));
			if (name != null) {
				if (!name.matches(prefName)) {
					rowList.add(row.toString());
					nameList.add(name);
				}
			}
		}
		cNames.close();
		((SettingsMenu)ctx).userDB.close();
		if (deleteUser != null) {
			if (nameList.size()>0) {
				deleteUser.setEnabled(true);
				userNames = nameList.toArray(new CharSequence[nameList.size()]);
				userRows = rowList.toArray(new CharSequence[rowList.size()]);
				deleteUser.setEntries(userNames);
				deleteUser.setEntryValues(userRows);
			} else {
				deleteUser.setEnabled(false);
			}
		}
	}

	private void voiceCheck() {
		PackageManager pm = ctx.getPackageManager();
		if (pm != null) {
			List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
			if (activities.size() == 0) {
				//Disabled
				Preference voice = findPreference("speak_set");
				if (voice != null) {
					voice.setEnabled(false);
				}
			}
		}
	}

	private void showActionDialog(int action, String userN, String title, String text, String extra) {
		//0 is for settings 3.0 and up
		DialogFragment newDialog = ActionDialog.newInstance(0, action, userN, title, text, extra);
		newDialog.show(((SettingsMenu)ctx).theManager, "actionDialog");
	}
}
