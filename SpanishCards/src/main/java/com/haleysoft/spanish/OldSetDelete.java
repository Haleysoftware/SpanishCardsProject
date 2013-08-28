package com.haleysoft.spanish;

/**
 * Created by Haleysoftware on 5/23/13.
 * Cleaned by Mike Haley on 8/27/13.
 */

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
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;

public class OldSetDelete extends FragmentActivity implements OnItemSelectedListener, View.OnClickListener {
	//This is a helper for the old settings

	FragmentManager theManager = getSupportFragmentManager();
	private UserDBFragment userDB;
	private String prefName = "Guest";
	private ArrayAdapter<CharSequence> nameAdapter = null;
	private ArrayList<String> nameList;
	private ArrayList<String> rowList;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_old_set_delete);
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			prefName = extras.getString("uName");
		}
		setupUserList();
		if (savedInstanceState != null) {
			prefName = savedInstanceState.getString("uName");
			rowList = new ArrayList<String>();
			nameList = new ArrayList<String>();
			rowList = savedInstanceState.getStringArrayList("listRow");
			nameList = savedInstanceState.getStringArrayList("listName");
			recoverUserList();
		} else {
			addFragments();
		}

		SharedPreferences preferences = getSharedPreferences(SettingsMenuOld.prefName, MODE_PRIVATE);
		boolean theme = preferences.getBoolean("theme_set", false);
		setTheme(theme? R.style.ActivityThemeAlt : R.style.ActivityTheme);
		String orientationTest = preferences.getString("orie_list_set", "0");
		orientation(orientationTest);
		addButtons();
	}

	@Override
	public void onStart() {
		super.onStart();
		userDB = (UserDBFragment) theManager.findFragmentByTag("userFragment");
	}

	@Override
	public void onResume() {
		super.onResume();
		fillUserList();
	}

	@Override
	public void onSaveInstanceState (Bundle savedState) {
		super.onSaveInstanceState(savedState);
		savedState.putString("uName", SettingsMenuOld.prefName);
		savedState.putStringArrayList("listName", nameList);
		savedState.putStringArrayList("listRow", rowList);
	}

	@Override
	public void onClick(View v) {
		String title = null;
		String text = null;
		int id = 0;
		switch (v.getId()) {
			case R.id.setmarkbutton:
				title = getString(R.string.setresetmarksmain);
				text = getString(R.string.settopresetdialog);
				id = 0;
				break;
			case R.id.setlevelbutton:
				title = getString(R.string.setresetlevelmain);
				text = getString(R.string.settopresetdialog);
				id = 1;
				break;
			case R.id.setscorebutton:
				title = getString(R.string.setclearuserscoremain);
				text = getString(R.string.settopresetdialog);
				id = 2;
				break;
			case R.id.setscoresbutton:
				title = getString(R.string.setclearallscoresmain);
				text = getString(R.string.settopresetdialog);
				id = 3;
				break;
			default:
		}
		if (title != null && text != null) {
			showActionDialog(id, prefName, title, text, null);
		}
	}

	private void addButtons() {
		Button markReset = (Button) this.findViewById(R.id.setmarkbutton);
		Button levelReset = (Button) this.findViewById(R.id.setlevelbutton);
		Button userScores = (Button) this.findViewById(R.id.setscorebutton);
		Button allScores = (Button) this.findViewById(R.id.setscoresbutton);
		markReset.setOnClickListener(this);
		levelReset.setOnClickListener(this);
		userScores.setOnClickListener(this);
		allScores.setOnClickListener(this);
	}

	private void addFragments() {
		WordDBFragment wordDB = new WordDBFragment();
		UserDBFragment userDB = new UserDBFragment();
		FragmentTransaction theTransaction = theManager.beginTransaction();
		theTransaction.add(wordDB, "wordFragment");
		theTransaction.add(userDB, "userFragment");
		theTransaction.commit();
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

	public void userDelete(String name) {
		int arrayName = nameList.indexOf(name);
		String newRow = rowList.get(arrayName);
		String text = getString(R.string.setremovedialogconform);
		showActionDialog(4, prefName, name, text, newRow);
	}

	public void onDialogOkay(int actionID, String dTitle, String extra) {
		switch (actionID) {
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
				userDB.open();
				userDB.removeScore(SettingsMenuOld.prefName);
				userDB.close();
				break;
			case 3: //Clear all scores
				userDB.emptyScore();
				break;
			case 4: //Delete user verify
				DialogFragment newDialog = DeleteUserDialog.newInstance(1, SettingsMenuOld.prefName, dTitle, extra);
				newDialog.show(theManager, "deleteDialog");
				break;
			default:
				//nothing
		}
	}

	private void setupUserList() {
		Spinner removeName = (Spinner) findViewById(R.id.deleteSpinner);
		nameAdapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item);
		nameAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		nameAdapter.add(getString(R.string.setremovedialogtitle));
		removeName.setAdapter(nameAdapter);
		removeName.setOnItemSelectedListener(this);
	}

	private void fillUserList() {
		Spinner removeName = (Spinner) findViewById(R.id.deleteSpinner);
		if (nameAdapter != null) {
			nameAdapter.clear();
		}
		nameAdapter.add(getString(R.string.setremovedialogtitle));
		Cursor cNames = userDB.getUsers();
		rowList = new ArrayList<String>();
		nameList = new ArrayList<String>();
		while (cNames.moveToNext()) {
			Long row = cNames.getLong(cNames.getColumnIndex(UserDBFragment.KEY_ROWA));
			String name = cNames.getString(cNames.getColumnIndex(UserDBFragment.KEY_USER));
			if (name != null && !name.matches(prefName)) {
				rowList.add(row.toString());
				nameList.add(name);
			}
		}
		cNames.close();
		userDB.close();
		if (nameList.size()>0) {
			removeName.setEnabled(true);
			int list = 0;
			while (list<nameList.size()) {
				nameAdapter.add(nameList.get(list));
				list++;
			}
		} else {
			removeName.setEnabled(false);
		}
		nameAdapter.notifyDataSetChanged();
		removeName.setAdapter(nameAdapter);
	}

	private void recoverUserList() {
		Spinner removeName = (Spinner) findViewById(R.id.deleteSpinner);
		if (nameAdapter != null) {
			nameAdapter.clear();
		}
		nameAdapter.add(getString(R.string.setremovedialogtitle));
		if (nameList.size()>0) {
			removeName.setEnabled(true);
			int list = 0;
			while (list<nameList.size()) {
				nameAdapter.add(nameList.get(list));
				list++;
			}
		} else {
			removeName.setEnabled(false);
		}
		nameAdapter.notifyDataSetChanged();
		removeName.setAdapter(nameAdapter);
	}

	public void removeUser(String noName, String noRow, boolean clearScores) {
		userDB.open();
		if (clearScores) {
			userDB.removeScore(noName);
		}
		if (userDB.deleteUser(noRow)) {
			Toast.makeText(this, noName  + " was removed!", Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(this, noName + " at " + noRow + " was not removed!", Toast.LENGTH_SHORT).show();
		}
		userDB.close();
		fillUserList();
	}

	public void showActionDialog(int action, String userN, String title, String text, String extra) {
		//1 is for settings below 3.0
		DialogFragment newDialog = ActionDialog.newInstance(1, action, userN, title, text, extra);
		newDialog.show(theManager, "actionDialog");
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
		Spinner removeName = (Spinner) findViewById(R.id.deleteSpinner);
		if (pos != 0) {
			Object selected = removeName.getSelectedItem();
			if (selected != null) {
				String delName = selected.toString();
				userDelete(delName);
			}
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		Spinner removeName = (Spinner) findViewById(R.id.deleteSpinner);
		removeName.setSelection(0);
	}
}
