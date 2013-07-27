package com.haleysoft.spanish;

/**
 * Created by Haleysoftware on 5/23/13.
 */

import java.util.ArrayList;

import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

public class OldSetDelete extends FragmentActivity implements OnItemSelectedListener
{
	//This is a helper for the old settings

	FragmentManager theManager = getSupportFragmentManager();
	private UserDBFragment userdb;
	private String prefName = "Guest";
	private ArrayAdapter<CharSequence> nameAdapter = null;
	//private CharSequence[] userNames;
	//private CharSequence[] userRows;
	private ArrayList<String> nameList;
	private ArrayList<String> rowList;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_old_set_delete);
		Bundle extras = getIntent().getExtras();
		if (extras != null)
		{
			prefName = extras.getString("uName");
		}
		setupUserList();
		if (savedInstanceState != null)
		{
			prefName = savedInstanceState.getString("uName");
			rowList = new ArrayList<String>();
			nameList = new ArrayList<String>();
			rowList = savedInstanceState.getStringArrayList("listRow");
			nameList = savedInstanceState.getStringArrayList("listName");
			recoverUserList();
		}
		else
		{
			addFragments();
		}

		SharedPreferences preferences = getSharedPreferences(SettingsMenuOld.prefName, MODE_PRIVATE);
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
	}

	@Override
	public void onStart()
	{
		super.onStart();
		userdb = (UserDBFragment) theManager.findFragmentByTag("userFragment");
		//fillUserList();
	}

	@Override
	public void onResume()
	{
		super.onResume();
		fillUserList();
	}

	@Override
	public void onSaveInstanceState (Bundle savedState)
	{
		super.onSaveInstanceState(savedState);
		savedState.putString("uName", SettingsMenuOld.prefName);
		savedState.putStringArrayList("listName", nameList);
		savedState.putStringArrayList("listRow", rowList);
	}

	private void addFragments()
	{

		FragmentTransaction theTransaction = theManager.beginTransaction();

		WordDBFragment worddb = new WordDBFragment();
		theTransaction.add(worddb, "wordFragment");

		UserDBFragment userdb = new UserDBFragment();
		theTransaction.add(userdb, "userFragment");

		theTransaction.commit();
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

	public void markReset(View v)
	{
		String title = getString(R.string.setresetmarksmain);
		String text = getString(R.string.settopresetdialog);
		int id = 0;
		showActionDialog(id, prefName, title, text, null);
	}

	public void levelReset(View v)
	{
		String title = getString(R.string.setresetlevelmain);
		String text = getString(R.string.settopresetdialog);
		int id = 1;
		showActionDialog(id, prefName, title, text, null);
	}

	public void userDelete(String name)
	{
		//int arrayRow = rowList.indexOf(newValue.toString());
		//String newName = nameList.get(arrayRow);

		int arrayName = nameList.indexOf(name);
		String newRow = rowList.get(arrayName);

		String text = getString(R.string.setremovedialogconform);
		int id = 4;
		showActionDialog(id, prefName, name, text, newRow);
	}

	public void userScores(View v)
	{
		String title = getString(R.string.setclearuserscoremain);
		String text = getString(R.string.settopresetdialog);
		int id = 2;
		showActionDialog(id, prefName, title, text, null);
	}

	public void allScores(View v)
	{
		String title = getString(R.string.setclearallscoresmain);
		String text = getString(R.string.settopresetdialog);
		int id = 3;
		showActionDialog(id, prefName, title, text, null);
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
				SharedPreferences pref = getSharedPreferences(SettingsMenuOld.prefName, MODE_PRIVATE);
				pref.edit().putInt("user_level", 1).commit();
				pref.edit().putInt("level_points", 0).commit();
				break;
			case 2: //Clear user scores
				userdb.open();
				userdb.removeScore(SettingsMenuOld.prefName);
				userdb.close();
				break;
			case 3: //Clear all scores
				userdb.emptyScore();
				break;
			case 4: //Delete user verify
				DialogFragment newDialog = DeleteUserDialog.newInstance(1, SettingsMenuOld.prefName, dTitle, extra);
				newDialog.show(theManager, "deleteDialog");
				break;
			default:
				//nothing
		}
	}

	private void setupUserList()
	{
		Spinner removeName = (Spinner) findViewById(R.id.deleteSpinner);
		nameAdapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item);
		nameAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		nameAdapter.add(getString(R.string.setremovedialogtitle));
		removeName.setAdapter(nameAdapter);
		removeName.setOnItemSelectedListener(this);
	}

	private void fillUserList()
	{
		//ListPreference deleteUser = (ListPreference) findPreference("delete_user_set");
		Spinner removeName = (Spinner) findViewById(R.id.deleteSpinner);
		if (nameAdapter != null)
		{
			nameAdapter.clear();
		}
		nameAdapter.add(getString(R.string.setremovedialogtitle));
		Cursor cNames = userdb.getUsers();
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
		userdb.close();
		if (nameList.size()>0)
		{
			removeName.setEnabled(true);
			//deleteUser.setEnabled(true);
			//userNames = nameList.toArray(new CharSequence[nameList.size()]);
			//userRows = rowList.toArray(new CharSequence[rowList.size()]);
			int list = 0;
			while (list<nameList.size())
			{
				nameAdapter.add(nameList.get(list));
				list++;
			}
			//deleteUser.setEntries(userNames);
			//deleteUser.setEntryValues(userRows);
		}
		else
		{
			removeName.setEnabled(false);
			//deleteUser.setEnabled(false);
		}
		nameAdapter.notifyDataSetChanged();
		removeName.setAdapter(nameAdapter);
	}

	private void recoverUserList()
	{
		Spinner removeName = (Spinner) findViewById(R.id.deleteSpinner);
		//ListPreference deleteUser = (ListPreference) findPreference("delete_user_set");
		if (nameAdapter != null)
		{
			nameAdapter.clear();
		}
		nameAdapter.add(getString(R.string.setremovedialogtitle));
		if (nameList.size()>0)
		{
			removeName.setEnabled(true);
			//deleteUser.setEnabled(true);
			//userNames = nameList.toArray(new CharSequence[nameList.size()]);
			//userRows = rowList.toArray(new CharSequence[rowList.size()]);
			int list = 0;
			while (list<nameList.size())
			{
				nameAdapter.add(nameList.get(list));
				list++;
			}
			//deleteUser.setEntries(userNames);
			//deleteUser.setEntryValues(userRows);
		}
		else
		{
			removeName.setEnabled(false);
			//deleteUser.setEnabled(false);
		}
		nameAdapter.notifyDataSetChanged();
		removeName.setAdapter(nameAdapter);
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
		fillUserList();
	}

	public void showInfoDialog(String uName, String dTitle, String dText)
	{
		DialogFragment newDialog = InfoDialog.newInstance(uName, dTitle, dText);
		newDialog.show(theManager, "infoDialog");
	}

	public void showActionDialog(int action, String userN, String title, String text, String extra)
	{
		int pref = 1; //settings for below 3.0
		DialogFragment newDialog = ActionDialog.newInstance(pref, action, userN, title, text, extra);
		newDialog.show(theManager, "actionDialog");
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
	{
		// TODO Auto-generated method stub
		Spinner removeName = (Spinner) findViewById(R.id.deleteSpinner);
		if (pos == 0)
		{
			//do nothing
		}
		else
		{
			String delName = removeName.getSelectedItem().toString();
			userDelete(delName);
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent)
	{
		// TODO Auto-generated method stub
		Spinner removeName = (Spinner) findViewById(R.id.deleteSpinner);
		removeName.setSelection(0);
	}
}
