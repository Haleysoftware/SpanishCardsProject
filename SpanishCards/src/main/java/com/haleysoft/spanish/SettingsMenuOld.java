package com.haleysoft.spanish;

/**
 * Created by Haleysoftware on 5/23/13.
 * Cleaned by Mike Haley 8/29/13.
 */

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

public class SettingsMenuOld extends PreferenceActivity {
	private static final String MASTER_SETTINGS = "haley_master_set";
	public static String prefName = "Guest";
	private int mode = 0;

	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SharedPreferences masterPref = getSharedPreferences(MASTER_SETTINGS, MODE_PRIVATE);
		Bundle extras = this.getIntent().getExtras();
		if (extras != null) {
			prefName = extras.getString("prefUser");
			mode = extras.getInt("mode");
		}
		SharedPreferences preferences = this.getSharedPreferences(prefName, MODE_PRIVATE);
		boolean theme = preferences.getBoolean("theme_set", false);
		if (theme) {
			setTheme(R.style.ActivityThemeAlt);
		} else {
			setTheme(R.style.ActivityTheme);
		}
		String orientationTest = preferences.getString("orie_list_set", "0");
		orientation(orientationTest);
		this.getPreferenceManager().setSharedPreferencesName(prefName);
		switch (mode) {
			default:
			case 0:
				this.addPreferencesFromResource(R.xml.oldmainsettings);
				break;
			case 1:
				this.addPreferencesFromResource(R.xml.testsettings);
				break;
		}
		if (savedInstanceState != null) {
			switch (mode) {
				case 0: //This is for the main settings

					break;
				case 1: //This is for the test settings

					break;
				default:
			}
		}
		Preference shopButton = this.findPreference("buy_set");
		if (shopButton != null) {
			if (masterPref.getBoolean("buy_okay", false)) {
				shopButton.setEnabled(true);
				shopButton.setSummary(R.string.setbuysum);
				shopButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference arg0) {
						Intent goShop = new Intent(SettingsMenuOld.this, AppPurchasing.class);
						startActivity(goShop);
						return true;
					}
				});
			} else {
				shopButton.setEnabled(false);
				shopButton.setSummary(R.string.setbuyoff);
			}
		}
		switch (mode) {
			case 0: //This is to setup the main settings

				Preference resetButton = this.findPreference("mark_reset_set");
				if (resetButton != null) {
					resetButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
						@Override
						public boolean onPreferenceClick(Preference arg0) {
							//Code goes here
							Intent reset = new Intent(SettingsMenuOld.this, OldSetDelete.class);
							reset.putExtra("uName", prefName);
							startActivity(reset);
							return true;
						}
					});
				}
				break;
			case 1: //This is to setup the test settings

				break;
			default:
		}
		//This is where to setup that is common to all settings
		Preference changeOrie = findPreference("orie_list_set");
		if (changeOrie != null) {
			changeOrie.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					orientation(newValue.toString());
					return true;
				}
			});
		}

	}

	@Override
	public void onStart() {
		super.onStart();
		switch (mode) {
			case 0: //This is for the main settings

				break;
			case 1: //This is for the test settings

				break;
			default:
		}
		voiceCheck();
	}

	@SuppressWarnings("deprecation")
	private void voiceCheck() {
		PackageManager pm = getPackageManager();
		if (pm != null) {
			List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
			if (activities.size() == 0) {
				//Disabled
				Preference voice = findPreference("speak_set");
				if (voice != null) {
					voice.setEnabled(false);
					voice.setSummary(R.string.noVoice);
				}
			}
		}
	}

	private void orientation(String orientationTest) {
		int orieTest = Integer.parseInt(orientationTest);
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) { //For old OS
			switch (orieTest) {
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
		} else {
			switch (orieTest) {
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
