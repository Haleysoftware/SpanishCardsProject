package com.haleysoft.spanish;

/**
 * Created by Haleysoftware on 5/23/13.
 */

import java.util.ArrayList;
import java.util.Locale;

import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.TextView;
import android.widget.ToggleButton;

public class TestSelect extends FragmentActivity implements OnItemSelectedListener
{
	private static final String MASTER_SETTINGS = "haley_master_set";
	FragmentManager theManager = getSupportFragmentManager();
	private static final int SETTING_REQUEST_CODE = 2010;
	public String userName = "Guest";
	private ArrayAdapter<CharSequence> hideAdapter = null;
	private ArrayAdapter<CharSequence> showAdapter = null;
	private ArrayAdapter<CharSequence> nameAdapter = null;
	private ArrayList<CharSequence> littleNames;
	private boolean newUser = false;
	private int spinman = 0;
	private int lastSpin = 0;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		SharedPreferences masterPref = getSharedPreferences(MASTER_SETTINGS, MODE_PRIVATE);
		userName = masterPref.getString("last_user_set", "Guest");
		SharedPreferences pref = getSharedPreferences(userName, MODE_PRIVATE);
		boolean theme = pref.getBoolean("theme_set", false);
		if (theme)
		{
			setTheme(R.style.ActivityThemeAlt);
		}
		else
		{
			setTheme(R.style.ActivityTheme);
		}
		setContentView(R.layout.selectlayout);
		nameSpinSetup();
		if (savedInstanceState == null)
		{
			addFragments();
		}
		else
		{
			lastSpin = savedInstanceState.getInt("last");
		}
	}

	@Override
	public void onStart()
	{
		super.onStart();
		updateLevel();
		uiUpdate();
		hideSpinSetup();
		showSpinSetup();
		wordSpinUpdate();
		nameSpinUpdate();

	}

	@Override
	public void onResume()
	{
		super.onResume();
	}

	@Override
	public void onPostResume()
	{
		super.onPostResume();
	}

	@Override
	public void onSaveInstanceState (Bundle savedState)
	{
		savedState.putInt("last", lastSpin);
		super.onSaveInstanceState(savedState);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.selectmenu, menu);
		if (Build.VERSION.SDK_INT<Build.VERSION_CODES.HONEYCOMB) //Below 3.0
		{
			menu.removeItem(R.id.menu_theme); //Need to work on this
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected (MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.menu_settings:
				spinman = 1;
				Intent set;
				if (Build.VERSION.SDK_INT<Build.VERSION_CODES.HONEYCOMB) //For old OS
				{
					set = new Intent(this, SettingsMenuOld.class);
				}
				else //For new OS
				{
					set = new Intent(this, SettingsMenu.class);
				}
				set.putExtra("prefUser", userName);
				set.putExtra("mode", 0); //0 = Main Settings
				this.startActivityForResult(set, SETTING_REQUEST_CODE);
				return true;
			case R.id.menu_scores:
				Intent scores;
				if (Build.VERSION.SDK_INT<Build.VERSION_CODES.HONEYCOMB) //For old OS
				{
					scores = new Intent(this, HighScoresList.class);
				}
				else //For new OS
				{
					scores = new Intent(this, HighScoresList.class);
					//scores = new Intent(this, HighScoresBar.class);
				}
				scores.putExtra("user", userName);
				this.startActivity(scores);
				return true;
			case R.id.menu_list:
				Intent list;
				if (Build.VERSION.SDK_INT<Build.VERSION_CODES.HONEYCOMB) //For old OS
				{
					list = new Intent(this, WordList.class);
				}
				else //For new OS
				{
					list = new Intent(this, WordList.class);
				}
				list.putExtra("user", userName);
				this.startActivity(list);
				return true;
			case R.id.menu_theme:
				toggleTheme();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	private void setupPref()
	{
		PreferenceManager.setDefaultValues(this, userName, MODE_PRIVATE, R.xml.mastersettings, false);
	}

	private void addFragments()
	{
		FragmentTransaction theTransaction = theManager.beginTransaction();

		UserDBFragment userdb = new UserDBFragment();
		theTransaction.add(userdb, "userFragment");

		theTransaction.commit();
	}

	//This is for when the spinner is selected.
	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
	{
		if (spinman >= 3)
		{
			switch (parent.getId())
			{
				case R.id.spinnerName:
					if (pos != lastSpin)
					{
						Spinner spinner = (Spinner) findViewById(R.id.spinnerName);
						if (pos == 0)
						{
							lastSpin = pos;
							userName = "Guest";
							setupPref();
							updateOrie();
							wordSpinUpdate();
							updateLevel();
							uiUpdate();
							getSharedPreferences(MASTER_SETTINGS, MODE_PRIVATE).edit().putString("last_user_set", userName).commit();
							finish();
							Intent select = new Intent(this, TestSelect.class);
							startActivity(select);
						}
						else if (pos == 1)
						{
							spinman = 2;
							int currentUser;
							if (userName.matches("Guest"))
							{
								currentUser = 0;
							}
							else
							{
								currentUser = nameAdapter.getPosition(userName);
							}
							FragmentTransaction theTransaction = theManager.beginTransaction();
							Fragment dialogset = theManager.findFragmentByTag("dialogFragment");
							if (dialogset != null)
							{
								theTransaction.remove(dialogset);
							}
							DialogFragment newDialog = DialogsFragment.newInstance(null, 3, null, currentUser, userName);
							newDialog.show(theTransaction, "dialogFragment");
						}
						else
						{
							lastSpin = pos;
							userName = spinner.getSelectedItem().toString();
							setupPref();
							updateOrie();
							wordSpinUpdate();
							updateLevel();
							uiUpdate();
							getSharedPreferences(MASTER_SETTINGS, MODE_PRIVATE).edit().putString("last_user_set", userName).commit();
							finish();
							Intent select = new Intent(this, TestSelect.class);
							startActivity(select);
						}
					}
					break;
				case R.id.spinnerHide:
					Spinner hideSpinner = (Spinner) findViewById(R.id.spinnerHide);
					String pickHide = hideSpinner.getSelectedItem().toString();
					String langHide;
					if (pickHide.matches(getString(R.string.langSpanish)))
					{
						langHide = "Spanish";
					}
					else //English
					{
						langHide = "English";
					}
					getSharedPreferences(userName, MODE_PRIVATE).edit().putString("hide_word_set", langHide).commit();
					break;
				case R.id.spinnerShow:
					Spinner showSpinner = (Spinner) findViewById(R.id.spinnerShow);
					String pickShow = showSpinner.getSelectedItem().toString();
					String langShow;
					if (pickShow.matches(getString(R.string.langSpanish)))
					{
						langShow = "Spanish";
					}
					else //English
					{
						langShow = "English";
					}
					getSharedPreferences(userName, MODE_PRIVATE).edit().putString("show_word_set", langShow).commit();
					break;
				default:
			}
		}
		else
		{
			spinman++;
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent)
	{
		//Do nothing
	}

	private void toggleTheme()
	{
		SharedPreferences preference = getSharedPreferences(userName, MODE_PRIVATE);
		boolean theme = preference.getBoolean("theme_set", false);
		if (theme)
		{
			preference.edit().putBoolean("theme_set", false).commit();
		}
		else
		{
			preference.edit().putBoolean("theme_set", true).commit();
		}
		finish();
		Intent select = new Intent(this, TestSelect.class);
		startActivity(select);
	}

	private void uiUpdate()
	{
		SharedPreferences preferences = getSharedPreferences(userName, MODE_PRIVATE);
		ToggleButton pointButton = (ToggleButton) findViewById(R.id.togglePoints);
		boolean pointTest = preferences.getBoolean("point_set", false);
		pointButton.setChecked(pointTest);
	}

	private void updateLevel()
	{
		SharedPreferences preferences = getSharedPreferences(userName, MODE_PRIVATE);
		TextView lPoint = (TextView) findViewById(R.id.pointDisplay);
		TextView lUser = (TextView) findViewById(R.id.levelDisplay);
		int userLevel = preferences.getInt("user_level", 1);
		int maxPoints = userLevel * 10;
		int userPoints = preferences.getInt("level_points", 0);
		String userText = String.format(getString(R.string.showLevel), userLevel);
		String pointText = String.format(getString(R.string.showPoints), userPoints, maxPoints);
		lUser.setText(userText);
		lPoint.setText(pointText);
	}

	private void wordSpinUpdate()
	{
		Spinner hideSpinner = (Spinner) findViewById(R.id.spinnerHide);
		Spinner showSpinner = (Spinner) findViewById(R.id.spinnerShow);
		SharedPreferences preferences = getSharedPreferences(userName, MODE_PRIVATE);
		String hWord = preferences.getString("hide_word_set", "English");
		String sWord = preferences.getString("show_word_set", "Spanish");
		String hText;
		String sText;
		if (hWord.matches("Spanish"))
		{
			hText = getString(R.string.langSpanish);
		}
		else //English
		{
			hText = getString(R.string.langEnglish);
		}
		if (sWord.matches("Spanish"))
		{
			sText = getString(R.string.langSpanish);
		}
		else //English
		{
			sText = getString(R.string.langEnglish);
		}
		hideSpinner.setSelection(hideAdapter.getPosition(hText));
		showSpinner.setSelection(showAdapter.getPosition(sText));
	}

	private void hideSpinSetup()
	{
		Spinner hideSpinner = (Spinner) findViewById(R.id.spinnerHide);
		hideAdapter = ArrayAdapter.createFromResource(this, R.array.startwordlist, android.R.layout.simple_spinner_item);
		this.hideAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		hideSpinner.setAdapter(hideAdapter);
		hideSpinner.setOnItemSelectedListener(this);
	}

	private void showSpinSetup()
	{
		Spinner showSpinner = (Spinner) findViewById(R.id.spinnerShow);
		showAdapter = ArrayAdapter.createFromResource(this, R.array.startwordlist, android.R.layout.simple_spinner_item);
		this.showAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		showSpinner.setAdapter(showAdapter);
		showSpinner.setOnItemSelectedListener(this);
	}

	private void nameSpinSetup()
	{
		//Sets the data and function for the Spinner.
		Spinner nameSpinner = (Spinner) findViewById(R.id.spinnerName);
		nameAdapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item);
		nameAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		nameAdapter.add(getString(R.string.gName));
		nameAdapter.add(getString(R.string.nUName));
		nameSpinner.setAdapter(nameAdapter);
		nameSpinner.setOnItemSelectedListener(this);
		littleNames = new ArrayList<CharSequence>();
		littleNames.add(getString(R.string.gName).toLowerCase(Locale.US)); //Little list update
		littleNames.add(getString(R.string.nUName).toLowerCase(Locale.US)); //Little list update
	}

	private void nameSpinUpdate()
	{
		UserDBFragment userdb = (UserDBFragment) theManager.findFragmentByTag("userFragment");
		Spinner nameSpinner = (Spinner) findViewById(R.id.spinnerName);
		if (nameAdapter != null)
		{
			nameAdapter.clear();
		}
		nameAdapter.add(getString(R.string.gName));
		nameAdapter.add(getString(R.string.nUName));
		if (littleNames != null) //Little list update
		{
			littleNames.clear();
		}
		littleNames.add(getString(R.string.gName).toLowerCase(Locale.US)); //Little list update
		littleNames.add(getString(R.string.nUName).toLowerCase(Locale.US)); //Little list update
		int last;
		Cursor spinNames = userdb.getUsers();
		while (spinNames.moveToNext())
		{
			String user = spinNames.getString(spinNames.getColumnIndex(UserDBFragment.KEY_USER));
			nameAdapter.add(user);
			littleNames.add(user.toLowerCase(Locale.US)); //Little list update
		}
		spinNames.close();
		userdb.close();
		if (userName.matches("Guest"))
		{
			last = 0;
		}
		else
		{
			last = nameAdapter.getPosition(userName);
		}
		nameAdapter.notifyDataSetChanged();
		nameSpinner.setAdapter(nameAdapter);
		nameSpinner.setSelection(last);
		lastSpin = last;
		if (newUser)
		{
			newUser = false;
			finish();
			Intent select = new Intent(this, TestSelect.class);
			startActivity(select);
		}
	}

	public void addUser(String name)
	{
		UserDBFragment userdb = (UserDBFragment) theManager.findFragmentByTag("userFragment");
		Spinner spinner = (Spinner) findViewById(R.id.spinnerName);
		if (userdb.addUser(name)) //New name was added
		{
			newUser = true;
			userName = name;
			setupPref();
			getSharedPreferences(MASTER_SETTINGS, MODE_PRIVATE).edit().putString("last_user_set", userName).commit();
			nameSpinUpdate();
		}
		else //name already on list
		{
			spinner.setSelection(littleNames.indexOf(name.toLowerCase(Locale.US)));
		}
	}

	public void goTest(View v)
	{
		getSharedPreferences(userName, MODE_PRIVATE).edit().putBoolean("return_test_set", true).commit();
		Intent test = new Intent(this, TestMain.class);
		test.putExtra("user", userName);
		startActivity(test);
		finish();
	}

	public void points(View v)
	{
		SharedPreferences pref = getSharedPreferences(userName, MODE_PRIVATE);
		boolean point = pref.getBoolean("point_set", false);
		if (!point)
		{
			point = true;
		}
		else if (point)
		{
			point = false;
		}
		pref.edit().putBoolean("point_set", point).commit();
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, requestCode, data);
		if (requestCode == SETTING_REQUEST_CODE)
		{
			updateOrie();
		}
	}

	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	private void updateOrie()
	{
		SharedPreferences preferences = getSharedPreferences(userName, MODE_PRIVATE);
		String orientationTest = preferences.getString("orie_list_set", "0");
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
