package com.haleysoft.spanish;

/**
 * Created by Haleysoftware on 5/23/13.
 */

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.ads.AdRequest;
import com.google.ads.AdView;

public class TestMain extends FragmentActivity implements OnItemSelectedListener
{
	private static final String MASTER_SETTINGS = "haley_master_set";
	private AdView adView;
	private static final int demoLevel = 30;
	private static final int maxLevel = 150;
	FragmentManager theManager = getSupportFragmentManager();
	private boolean startNull = false;
	private String hideWord;
	private String showWord;
	private String MODE = "Test";
	private static final int VOICE_REQUEST_CODE = 2040;
	private static final int SETTING_REQUEST_CODE = 1010;
	public static String userSaid = "Help me, I'm trapped in the phone";
	private String userName;
	private WordDBFragment dBHelp;
	private Long scoreId;
	public int score = 0;
	private int spinman = 0;
	private boolean spinset = true;
	private String selected;
	private boolean shown = false;
	private boolean ttsChange = false;
	private boolean topTypeNotify = false;
	private boolean topMostNotify = false;
	public boolean wrongDialog = false;
	public static boolean ttsErrorDialog = false;
	private ArrayAdapter<CharSequence> adapter;
	//Holds the cursor from Word Database
	private Long wordID;
	private String category = "";
	private String type = "";
	private int wordLevel = 0;
	private int theMark = 0;
	private int gotPoint = 0;
	private String note = "0";
	private String english = "";
	private String altEnglish = "";
	private String spanish = "";
	private String altSpanish = "";


	//Called when the activity is first created.
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
		SharedPreferences masterPref = getSharedPreferences(MASTER_SETTINGS, MODE_PRIVATE);
		Bundle extras = this.getIntent().getExtras();
		if (extras != null)
		{
			userName = extras.getString("user");
		}
		SharedPreferences preferences = this.getSharedPreferences(userName, MODE_PRIVATE);
		boolean theme = preferences.getBoolean("theme_set", false);
		if (theme)
		{
			this.setTheme(R.style.ActivityThemeAlt);
		}
		else
		{
			this.setTheme(R.style.ActivityTheme);
		}
		updateOrie();
		hideWord = preferences.getString("hide_word_set", "English");
		showWord = preferences.getString("show_word_set", "Spanish");
		if (savedInstanceState != null)
		{
			wordID = savedInstanceState.getLong("id");
			category = savedInstanceState.getString("cate");
			type = savedInstanceState.getString("type");
			wordLevel = savedInstanceState.getInt("wLevel");
			theMark = savedInstanceState.getInt("mark");
			gotPoint = savedInstanceState.getInt("points");
			note = savedInstanceState.getString("notes");
			english = savedInstanceState.getString("eng");
			altEnglish = savedInstanceState.getString("aEng");
			spanish = savedInstanceState.getString("span");
			altSpanish = savedInstanceState.getString("aSpan");

			userSaid = savedInstanceState.getString("said");
			scoreId = savedInstanceState.getLong("sID");
			spinset = savedInstanceState.getBoolean("spin");
			score = savedInstanceState.getInt("score");
			shown = savedInstanceState.getBoolean("show");
			topTypeNotify = savedInstanceState.getBoolean("topType");
			topMostNotify = savedInstanceState.getBoolean("topMost");
		}
		spinman = 0;
		this.setupspin();
		if (savedInstanceState == null)
		{
			startNull = true;
			this.addFragments();
			this.addTTS();
		}

		boolean voiceTest = preferences.getBoolean("speak_set", true);
		if (voiceTest)
		{
			this.voiceCheck();
		}
		if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.ICE_CREAM_SANDWICH) //For ICS and up
		{
			ActionBar actionBar = this.getActionBar();
			actionBar.setHomeButtonEnabled(true);
		}
		if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB) //For HC and up
		{
			ActionBar actionBar = this.getActionBar();
			actionBar.setDisplayHomeAsUpEnabled(true);
		}
		addAds(masterPref.getBoolean("remove_ads", false));
	}

	/*
	@Override
	public void onPostCreate(Bundle savedInstanceState)
	{
		super.onPostCreate(savedInstanceState);
		dBHelp = (WordDBFragment) theManager.findFragmentByTag("wordFragment");

		if (savedInstanceState == null)
		{
			SharedPreferences preferences = getSharedPreferences(userName, MODE_PRIVATE);
			UserDBFragment userdb = (UserDBFragment) theManager.findFragmentByTag("userFragment");
			boolean scoreCheck = preferences.getBoolean("score_saved", false);
			if (scoreCheck)
			{
				scoreId = preferences.getLong("score_id", 1);
				score = preferences.getInt("last_score", 0);
			}
			else
			{
				userdb.cleanScore();
				dBHelp.resetPoint();
			}
			nextWord();
		}
	}
	*/

	@Override
	public void onStart()
	{
		super.onStart();
		dBHelp = (WordDBFragment) theManager.findFragmentByTag("wordFragment");
		if (startNull)
		{
			SharedPreferences preferences = getSharedPreferences(userName, MODE_PRIVATE);
			UserDBFragment userdb = (UserDBFragment) theManager.findFragmentByTag("userFragment");
			boolean scoreCheck = preferences.getBoolean("score_saved", false);
			if (scoreCheck)
			{
				scoreId = preferences.getLong("score_id", 1);
				score = preferences.getInt("last_score", 0);
			}
			else
			{
				userdb.cleanScore();
				dBHelp.resetPoint();
			}
			this.nextWord();
		}
	}

	@Override
	public void onPause()
	{
		super.onPause();
	}

	@Override
	public void onResume()
	{
		super.onResume();
		this.getWord();
		if (shown)
		{
			this.showWord();
		}
		else
		{
			Button nextWord = (Button) this.findViewById(R.id.newButton);
			nextWord.setText(R.string.SkipWord);
		}
		showScore();
	}

	@Override
	public void onPostResume()
	{
		super.onPostResume();

		if (ttsChange) //This is for when TTS is changed on the settings page.
		{
			SharedPreferences preferences = getSharedPreferences(userName, MODE_PRIVATE);
			boolean ttsTest = preferences.getBoolean("tts_set", true);
			if (ttsTest) //TTS is on
			{
				addTTS();
			}
			else //TTS is off
			{
				removeTTS();
			}
			ttsChange = false;
		}

		//This is for Android HC and up
		if (ttsErrorDialog)
		{
			startDialog(5, null, 2); //2 = null
			ttsErrorDialog = false;
		}
		if (wrongDialog)
		{
			startDialog(1, userSaid, 2); //2 = null
			wrongDialog = false;
		}
	}

	@Override
	public void onDestroy() {
		if (adView != null) {
			adView.removeAllViews();
			adView.destroy();
		}
		super.onDestroy();
	}

	@Override
	public void onSaveInstanceState (Bundle savedState)
	{
		super.onSaveInstanceState(savedState);
		if (wordID != null)
		{
			savedState.putLong("id", wordID);
		}
		savedState.putString("cate", category);
		savedState.putString("type", type);
		savedState.putInt("wLevel", wordLevel);
		savedState.putInt("mark", theMark);
		savedState.putInt("points", gotPoint);
		savedState.putString("notes", note);
		savedState.putString("eng", english);
		savedState.putString("aEng", altEnglish);
		savedState.putString("span", spanish);
		savedState.putString("aSpan", altSpanish);

		savedState.putString("said", userSaid);
		if (scoreId != null)
		{
			savedState.putLong("sID", scoreId);
		}
		savedState.putBoolean("spin", spinset);
		savedState.putInt("score", score);
		savedState.putBoolean("show", shown);
		savedState.putBoolean("topType", topTypeNotify);
		savedState.putBoolean("topMost", topMostNotify);
	}

	private void addAds(boolean paid) {
		adView = (AdView) this.findViewById(R.id.adView);
		if (paid) {
			adView.setVisibility(View.INVISIBLE);
			if (adView != null) {
				adView.removeAllViews();
				adView.destroy();
				adView = null;
			}
		}
		else {
			adView.setVisibility(View.VISIBLE);
			AdRequest adRequest = new AdRequest();
			//This code is for testing only
			adRequest.addTestDevice(AdRequest.TEST_EMULATOR);
			adRequest.addTestDevice("2233DFE5B204F720C5A258A482ECAB8E"); //GS2
			adRequest.addTestDevice("79B71208D02B63421ADC58ACF3A19CEE"); //LG G
			//adRequest.addTestDevice("015d0787bd3c0215"); //ASUS Prime
			//End of testing code
			adView.loadAd(adRequest);
			adView.setAdListener(new AdListen());
		}
	}

	private boolean checkPaid()
	{
		String mainAppPkg = "com.haleysoft.spanish";
		String keyPkg = "com.haleysoft.spanish.key";
		String cardKeyPkg = "com.haleysoft.card.key";
		String haleyKeyPkg = "com.haleysoft.master.key";
		PackageManager manager = getPackageManager();
		int sigMatch = manager.checkSignatures(mainAppPkg, keyPkg);
		int sigCardMatch = manager.checkSignatures(mainAppPkg, cardKeyPkg);
		int sigHaleyMatch = manager.checkSignatures(mainAppPkg, haleyKeyPkg);
		return sigMatch == PackageManager.SIGNATURE_MATCH
				|| sigCardMatch == PackageManager.SIGNATURE_MATCH
				|| sigHaleyMatch == PackageManager.SIGNATURE_MATCH;
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
		boolean flipTest = preferences.getBoolean("hand_set", false);
		if (flipTest)
		{
			setContentView(R.layout.testlayoutflip);
		}
		else
		{
			setContentView(R.layout.testlayout);
		}
	}

	private void addFragments()
	{
		Spinner spinner = (Spinner) findViewById(R.id.select);
		selected = spinner.getSelectedItem().toString();

		FragmentTransaction theTransaction = theManager.beginTransaction();

		WordDBFragment worddb = new WordDBFragment();
		theTransaction.add(worddb, "wordFragment");

		UserDBFragment userdb = new UserDBFragment();
		theTransaction.add(userdb, "userFragment");

		theTransaction.commit();
	}

	private void addTTS()
	{
		SharedPreferences preferences = getSharedPreferences(userName, 0);
		boolean ttsTest = preferences.getBoolean("tts_set", true);
		if (ttsTest)
		{
			TTSFragment ttscheck = (TTSFragment) theManager.findFragmentByTag("ttsFragment");
			if (ttscheck == null)
			{
				FragmentTransaction theTransaction = theManager.beginTransaction();

				TTSFragment ttsset = new TTSFragment();
				Bundle arg = new Bundle();
				arg.putString("mode", MODE);
				arg.putString("hide", showWord);
				arg.putString("show", hideWord);
				ttsset.setArguments(arg);
				theTransaction.add(ttsset, "ttsFragment");

				theTransaction.commit();
			}
		}
	}

	private void removeTTS()
	{
		TTSFragment ttsset = (TTSFragment) theManager.findFragmentByTag("ttsFragment");
		if (ttsset != null)
		{
			FragmentTransaction theTransaction = theManager.beginTransaction();
			theTransaction.remove(ttsset);

			theTransaction.commit();
		}
	}

	private void setupspin()
	{
		SharedPreferences preferences = getSharedPreferences(userName, 0);
		boolean pointTest = preferences.getBoolean("point_set", false);
		boolean freePlay = preferences.getBoolean("game_set", false);
		String spinRemember = preferences.getString("remember_spin", "All");
		Spinner spinner = (Spinner) findViewById(R.id.select);
		if (pointTest) //User wants to get points once a word
		{
			if (freePlay)
			{
				adapter = ArrayAdapter.createFromResource(this, R.array.PointLevelarray, android.R.layout.simple_spinner_item);
			}
			else
			{
				adapter = ArrayAdapter.createFromResource(this, R.array.Pointarray, android.R.layout.simple_spinner_item);
			}
		}
		else //User wants to gets points for every word
		{
			if (freePlay)
			{
				adapter = ArrayAdapter.createFromResource(this, R.array.Levelarray, android.R.layout.simple_spinner_item);
			}
			else
			{
				adapter = ArrayAdapter.createFromResource(this, R.array.Selectarray, android.R.layout.simple_spinner_item);
			}
		}
		this.adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		spinner.setOnItemSelectedListener(this);
		if (spinRemember.matches("Points Only") && !pointTest || spinRemember.matches("Level") && !freePlay)
		{
			spinRemember = "All";
			preferences.edit().putString("remember_spin", "All").commit();
		}

		spinner.setSelection(adapter.getPosition(WordSwapHelper.cateCodeToString(this, spinRemember)));
	}

	//This is for when the spinner is selected.
	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
	{
		Spinner spinner = (Spinner) findViewById(R.id.select);
		if (spinman == 0)
		{
			if (pos == 0)
			{
				//Do nothing and spinner is ready for use
				spinman = 2;
				selected = spinner.getSelectedItem().toString();
				getSharedPreferences(userName, MODE_PRIVATE).edit().putString("remember_spin", WordSwapHelper.cateStringToCode(this, selected)).commit();
			}
			else
			{
				//Do nothing and spinner is not yet ready
				if (spinset)
				{
					spinman = 2; //spinner on first boot and ready
					spinset = false;
				}
				else
				{
					spinman = 1; //phone rotated or returned from settings, not ready yet
				}

			}
		}
		else if (spinman == 1)
		{
			//Do nothing and spinner is ready for use
			spinman = 2;
			selected = spinner.getSelectedItem().toString();
			getSharedPreferences(userName, MODE_PRIVATE).edit().putString("remember_spin", WordSwapHelper.cateStringToCode(this, selected)).commit();
		}
		else if (spinman == 2)
		{
			//Do nothing and spinner is ready for use
			selected = spinner.getSelectedItem().toString();
			getSharedPreferences(userName, MODE_PRIVATE).edit().putString("remember_spin", WordSwapHelper.cateStringToCode(this, selected)).commit();
			nextWord();
			getWord();
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent)
	{
		//Do Nothing
	}

	private boolean networkCheck()
	{
		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		return activeNetworkInfo != null;
	}

	@Override
	public boolean onCreateOptionsMenu (Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.testmenu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected (MenuItem item)
	{
		SharedPreferences preferences = getSharedPreferences(userName, 0);
		switch (item.getItemId())
		{
			case android.R.id.home: //This is the up button
			case R.id.menugohome:
				SharedPreferences pref = getSharedPreferences(userName, MODE_PRIVATE);
				pref.edit().putBoolean("score_saved", false).commit();
				pref.edit().putBoolean("return_test_set", false).commit();
				Intent intent = new Intent(this, TestSelect.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				finish();
				return true;
			case R.id.menunote:
				boolean noteTest = preferences.getBoolean("note_set", true);
				boolean newNote;
				if (noteTest)
				{
					newNote = false;
				}
				else
				{
					newNote = true;
				}
				getSharedPreferences(userName, MODE_PRIVATE).edit().putBoolean("note_set", newNote).commit();
				changeNote(newNote);
				return true;
			case R.id.menucata:
				boolean cataTest = preferences.getBoolean("cata_set", true);
				boolean newCata;
				if (cataTest)
				{
					newCata = false;
				}
				else
				{
					newCata = true;
				}
				getSharedPreferences(userName, MODE_PRIVATE).edit().putBoolean("cata_set", newCata).commit();
				changeCata(newCata);
				return true;
			case R.id.menuset:
				spinset = true;
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
				set.putExtra("mode", 1);
				this.startActivityForResult(set, SETTING_REQUEST_CODE);
				return true;
			case R.id.menuscores:
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
			case R.id.menulist:
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
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	private void levelPointUp()
	{
		Resources res = getResources();
		SharedPreferences preferences = getSharedPreferences(userName, MODE_PRIVATE);
		boolean userMaxed = preferences.getBoolean("level_max_set", false);
		boolean freePlay = preferences.getBoolean("game_set", false);
		if (!freePlay && !hideWord.matches(showWord))
		{
			int userLevel = preferences.getInt("user_level", 1);
			int maxPoints = userLevel * 10;
			int userPoints = preferences.getInt("level_points", 0);
			if (checkPaid() && userLevel < maxLevel || !checkPaid() && userLevel < demoLevel)
			{
				userPoints = userPoints + wordLevel;
				preferences.edit().putBoolean("level_max_set", false).commit();
			}
			else if (!userMaxed) //user is at max level
			{
				preferences.edit().putBoolean("level_max_set", true).commit();
				String maxText = res.getString(R.string.levelMaxed);
				Toast.makeText(this, maxText, Toast.LENGTH_SHORT).show();
			}
			else
			{
				//Do nothing, user already knows.
			}
			if (userPoints >= maxPoints) //Level Up
			{

				userPoints = userPoints - maxPoints;
				userLevel++;
				String levelText = String.format(res.getString(R.string.levelUp), userLevel);
				Toast.makeText(this, levelText, Toast.LENGTH_SHORT).show();
				preferences.edit().putInt("user_level", userLevel).commit();
			}
			preferences.edit().putInt("level_points", userPoints).commit();
			updateLevel();
		}
	}

	private void updateLevel()
	{
		Resources res = getResources();
		SharedPreferences preferences = getSharedPreferences(userName, MODE_PRIVATE);
		TextView lPoint = (TextView) findViewById(R.id.pointDisplay);
		TextView lUser = (TextView) findViewById(R.id.levelDisplay);
		int userLevel = preferences.getInt("user_level", 1);
		int maxPoints = userLevel * 10;
		int userPoints = preferences.getInt("level_points", 0);
		String userText = String.format(res.getString(R.string.testLevel), userLevel);
		String pointText = String.format(res.getString(R.string.testPoints), userPoints, maxPoints);
		lUser.setText(userText);
		lPoint.setText(pointText);
	}

	private void scoreUp()
	{
		UserDBFragment userdb = (UserDBFragment) theManager.findFragmentByTag("userFragment");
		SharedPreferences preferences = getSharedPreferences(userName, MODE_PRIVATE);

		score++;

		if (score > 0) //If the score is 0 then nothing to check or save
		{
			if (!topMostNotify)
			{
				Cursor topMost = userdb.topScore(null, null, userName);
				if (topMost.moveToFirst())
				{
					int tScore = topMost.getInt(topMost.getColumnIndex("score"));
					if (score > tScore)
					{
						String tName = topMost.getString(topMost.getColumnIndex("name"));
						//String tMode = topMost.getString(topMost.getColumnIndex("mode"));
						CharSequence tText;
						if (tName.matches("Guest")) //Last highest score was held by a guest
						{
							tText = getString(R.string.notifyMost);
						}
						else if (tName.matches(userName)) //Last highest score was held by the same user
						{
							tText = getString(R.string.notifyMost);
						}
						else //Last highest score was held by a different user (tName)
						{
							tText = getString(R.string.notifyMost);
						}
						Toast.makeText(this, tText, Toast.LENGTH_LONG).show();
						topMostNotify = true;
					}
				}
				userdb.close();
				topMost.close();
			}
			if (!topTypeNotify)
			{
				Cursor topScore = userdb.topScore(showWord, hideWord, userName);
				if (topScore.moveToFirst())
				{
					int hScore = topScore.getInt(topScore.getColumnIndex("score"));
					if (score > hScore)
					{
						String hName = topScore.getString(topScore.getColumnIndex("name"));
						//String hMode = topScore.getString(topScore.getColumnIndex("mode"));
						CharSequence hText;
						if (hName.matches("Guest")) //Last high score in this mode was held by a guest
						{
							hText = getString(R.string.notifyHigh);
						}
						else if (hName.matches(userName)) //Last high score in this mode was held by the same user
						{
							hText = getString(R.string.notifyHigh);
						}
						else //Last high score in this mode was held by a different user (hName)
						{
							hText = getString(R.string.notifyHigh);
						}
						Toast.makeText(this, hText, Toast.LENGTH_LONG).show();
						topTypeNotify = true;
					}
				}
				userdb.close();
				topScore.close();
			}

			if (scoreId == null) //Inserts the current test score
			{
				scoreId = userdb.addScore(userName, score, showWord, hideWord);
				boolean keepPoint = preferences.getBoolean("keep_score", false);
				if (keepPoint)
				{
					preferences.edit().putLong("score_id", scoreId).commit();
					preferences.edit().putInt("last_score", score).commit();
					preferences.edit().putBoolean("score_saved", true).commit();
				}
			}
			else //Updates the current test score
			{
				userdb.updateScore(scoreId, score);
				preferences.edit().putInt("last_score", score).commit();
			}
		}
		showScore();
		boolean nextTest = preferences.getBoolean("auto_set", false);
		if (nextTest)
		{
			nextWord();
			getWord();
		}
		else
		{
			showWord();
		}
	}

	private void showScore()
	{
		Resources res = getResources();
		TextView viewScore = (TextView) findViewById(R.id.correct);
		String scoreText = String.format(res.getString(R.string.testScore), score);
		viewScore.setText(scoreText);
	}

	public void words(View v)
	{
		nextWord();
		getWord();
	}

	//This pulls a random word from the database and adjust the view.
	private void nextWord ()
	{
		Button nextWord = (Button) findViewById(R.id.newButton);
		nextWord.setText(R.string.SkipWord);
		//Sets up the views to be used.
		Button speakB = (Button) findViewById(R.id.speakButton);
		Button showB = (Button) findViewById(R.id.showButton);
		SharedPreferences preferences = getSharedPreferences(userName, MODE_PRIVATE);
		int userLevel = preferences.getInt("user_level", 1);
		boolean freePlay = preferences.getBoolean("game_set", false);
		//Enables the speak and show buttons.
		speakB.setEnabled(true);
		showB.setEnabled(true);
		shown = false;
		//Asks the Words Database Adapter for a random row.
		try
		{
			Cursor wordCursor = dBHelp.getRandomWord(selected, userName, freePlay, userLevel);
			if (wordCursor.moveToFirst())
			{
				wordID = wordCursor.getLong(wordCursor.getColumnIndex(WordDBFragment.KEY_ROWID));

				String tempCate = wordCursor.getString(wordCursor.getColumnIndex(WordDBFragment.KEY_CAT));
				category = WordSwapHelper.cateCodeToString(this, tempCate);
				String tempType = wordCursor.getString(wordCursor.getColumnIndex(WordDBFragment.KEY_TYPE));
				type = tempType;     //Need to work on using this later.

				wordLevel = wordCursor.getInt(wordCursor.getColumnIndex(WordDBFragment.KEY_LEVEL));
				theMark = wordCursor.getInt(wordCursor.getColumnIndex(WordDBFragment.KEY_MARK));
				gotPoint = wordCursor.getInt(wordCursor.getColumnIndex(WordDBFragment.KEY_POINT));
				note = WordSwapHelper.noteCodeToString(this, wordCursor.getString(wordCursor.getColumnIndex(WordDBFragment.KEY_NOTE)));
				/*
				String tempNote = wordCursor.getString(wordCursor.getColumnIndex(WordDBFragment.KEY_NOTE));
				if (tempNote.matches("Masculine"))
				{
					note = getString(R.string.hintM);
				}
				else if (tempNote.matches("Feminine"))
				{
					note = getString(R.string.hintF);
				}
				else //no hint = 0
				{
					note = "0";
				}
				*/
				english = wordCursor.getString(wordCursor.getColumnIndex(WordDBFragment.KEY_ENG)).toLowerCase(Locale.US);
				altEnglish = wordCursor.getString(wordCursor.getColumnIndex(WordDBFragment.KEY_AENG)).toLowerCase(Locale.US);
				spanish = wordCursor.getString(wordCursor.getColumnIndex(WordDBFragment.KEY_SPAN)).toLowerCase(Locale.US);
				altSpanish = wordCursor.getString(wordCursor.getColumnIndex(WordDBFragment.KEY_ASPAN)).toLowerCase(Locale.US);
			}
			else
			{
				String notFound = String.format(getString(R.string.noWordsFound), selected);
				Toast.makeText(this, notFound, Toast.LENGTH_LONG).show();
			}
			wordCursor.close();
			dBHelp.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private void getWord ()
	{
		SharedPreferences preferences = getSharedPreferences(userName, 0);
		boolean cataTest = preferences.getBoolean("cata_set", true);
		boolean noteTest = preferences.getBoolean("note_set", true);
		TextView textHide = (TextView) findViewById(R.id.hideText);
		TextView textShow = (TextView) findViewById(R.id.showText);
		TextView noteText = (TextView) findViewById(R.id.noteText);
		TextView catText = (TextView) findViewById(R.id.catText);
		ToggleButton markB = (ToggleButton) findViewById(R.id.markButton);
		//Sets the returned database text to the text views.
		if (hideWord.contains("English"))
		{
			textHide.setText(english);
		}
		else if (hideWord.contains("Spanish"))
		{
			textHide.setText(spanish);
		}
		if (showWord.contains("English"))
		{
			textShow.setText(english);
		}
		else if (showWord.contains("Spanish"))
		{
			textShow.setText(spanish);
		}
		String showHint = String.format(getString(R.string.hintText), note);
		noteText.setText(showHint);
		catText.setText(category);
		//Sets the unknown text to invisible.
		textHide.setVisibility(View.INVISIBLE);
		changeCata(cataTest);
		changeNote(noteTest);
		//Toggles the mark button depending if the word is marked in the database.
		if (theMark == 1)
		{
			markB.setChecked(true);
		}
		else if (theMark == 0)
		{
			markB.setChecked(false);
		}
		updateLevel();
	}

	private void changeNote(boolean current)
	{
		//Hides or shows the hints depending on the user's settings.
		TextView noteText = (TextView) findViewById(R.id.noteText);
		if (current)
		{
			//If the note word is 0 then it will not be shown.
			if (note.contains("0"))
			{
				noteText.setVisibility(View.INVISIBLE);
			}
			else
			{
				noteText.setVisibility(View.VISIBLE);
			}
		}
		else
		{
			noteText.setVisibility(View.INVISIBLE);
		}
	}

	private void changeCata(boolean current)
	{
		//Hides or shows the category depending on the user's settings.
		TextView catCurrent = (TextView) findViewById(R.id.currentCata);
		TextView catText = (TextView) findViewById(R.id.catText);
		if (current)
		{
			catText.setVisibility(View.VISIBLE);
			catCurrent.setVisibility(View.VISIBLE);
		}
		else
		{
			catText.setVisibility(View.INVISIBLE);
			catCurrent.setVisibility(View.INVISIBLE);
		}
	}

	public void voice(View v)
	{
		SharedPreferences preferences = getSharedPreferences(userName, 0);
		boolean voiceTest = preferences.getBoolean("speak_set", true);
		if (networkCheck()) //Network check pass, ok to use voice
		{
			if (voiceTest) //Voice is true. Using the voice
			{
				startVoice();
			}
			else if (!voiceTest) //Voice is False. Using the text
			{
				startText();
			}
		}
		else //Network check failed, should use text
		{
			startText();
		}
	}

	private void voiceCheck()
	{
		PackageManager pm = getPackageManager();
		List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
		if (activities.size() == 0)
		{
			//Disabled
			getSharedPreferences(userName, 0).edit().putBoolean("speak_set", false).commit();
			Toast.makeText(this, getString(R.string.noVoice), Toast.LENGTH_LONG).show();
		}
	}

	//This starts the voice recognition activity with an intent.
	public void startVoice ()
	{
		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		if (hideWord.contains("English"))
		{
			intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
			intent.putExtra(RecognizerIntent.EXTRA_PROMPT, R.string.englishSay);
		}
		else if (hideWord.contains("Spanish"))
		{
			intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es");
			intent.putExtra(RecognizerIntent.EXTRA_PROMPT, R.string.spanishSay);
		}
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		startActivityForResult(intent, VOICE_REQUEST_CODE);
	}

	public void startText ()
	{
		startDialog(0, null, 2); //2 = null
	}

	//This runs after the voice recognition activity is finished and handles the result.
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		//This is for settings
		if (requestCode == SETTING_REQUEST_CODE)
		{
			updateOrie();
			spinman = 0;
			setupspin();
			ttsChange = true;
			SharedPreferences preferences = getSharedPreferences(userName, 0);
			boolean voiceTest = preferences.getBoolean("speak_set", true);
			if (voiceTest)
			{
				voiceCheck();
			}
		}
		//This section is for the Voice Recognition
		if (requestCode == VOICE_REQUEST_CODE && resultCode == RESULT_OK )
		{
			String testText = null;
			String altTestText = null;
			if (hideWord.contains("English"))
			{
				testText = english;
				altTestText = altEnglish;
			}
			else if (hideWord.contains("Spanish"))
			{
				testText = spanish;
				altTestText = altSpanish;
			}
			//Pulls the results of what the recognition engine thought it heard into an array list
			ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
			//Moves the array to my variable to work with.
			userSaid = matches.toString().toLowerCase(Locale.US);
			//Checks if the user said the correct word.
			if (userSaid.contains(testText) || userSaid.contains(altTestText)) //The user was correct
			{
				pointCheck();
			}
			else //The activity did not hear right or the wrong word was said
			{
				if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) //For old OS
				{
					startDialog(1, userSaid, 2); //2 = null
				}
				else //For new OS
				{
					wrongDialog = true;
				}
			}
		}
		else
		{
			//Got Something else????
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	public void UserText(String userTyped)
	{
		String testText = null;
		String altTestText = null;
		if (hideWord.matches("English"))
		{
			testText = english;
			altTestText = altEnglish;
		}
		else if (hideWord.matches("Spanish"))
		{
			testText = spanish;
			altTestText = altSpanish;
		}
		//Checks if the user said the correct word.
		if (userTyped.toLowerCase(Locale.US).contains(testText)
				|| userTyped.toLowerCase(Locale.US).contains(altTestText)) //The user was correct
		{
			pointCheck();
		}
		else //The user did not type in the correct word
		{
			userSaid = null;
			startDialog(2, userSaid, 2); //2 = null
		}
	}

	private void pointCheck()
	{
		SharedPreferences preferences = getSharedPreferences(userName, MODE_PRIVATE);
		boolean pointTest = preferences.getBoolean("point_set", false);
		CharSequence toastText = null;
		if (!pointTest) //Should be false. User wants to get a point for every word
		{
			scoreUp();
			levelPointUp();
			toastText = getString(R.string.wordRight);
		}
		else if (pointTest) //Should be true. User wants to get a point once a word
		{
			if (gotPoint == 0) //Should be set to 0. User has not received a point yet for the word
			{

				gotPoint = 1;
				WordDBFragment word = (WordDBFragment) theManager.findFragmentByTag("wordFragment");
				word.updatePoint(wordID);
				scoreUp();
				levelPointUp();
				toastText = getString(R.string.wordPoint);
			}
			else if (gotPoint == 1) //Should be set to 1. User has already received a point for the word
			{
				toastText = getString(R.string.wordNoPoint);
			}
		}
		Toast.makeText(this, toastText, Toast.LENGTH_SHORT).show();
	}

	public void show(View v)
	{
		showWord();
	}

	/*
	*Shows the Hidden word and disables the speak and show buttons.
	*This runs when the show button is pressed or when the user gets the word correct.
	*/
	private void showWord ()
	{
		Button nextWord = (Button) findViewById(R.id.newButton);
		TextView wordHide = (TextView) findViewById(R.id.hideText);
		Button speakB = (Button) findViewById(R.id.speakButton);
		Button showB = (Button) findViewById(R.id.showButton);
		nextWord.setText(R.string.NextWord);
		wordHide.setVisibility(View.VISIBLE);
		speakB.setEnabled(false);
		showB.setEnabled(false);
		shown = true;
	}

	public void wordSpeak(View v) //New
	{
		SharedPreferences preferences = getSharedPreferences(userName, 0);
		boolean ttsTest = preferences.getBoolean("tts_set", true);
		if (ttsTest)
		{
			TTSFragment ttsrun = (TTSFragment) theManager.findFragmentByTag("ttsFragment");
			if (ttsrun.goodTTS)
			{
				switch (v.getId())
				{
					case R.id.hideText:
						if (hideWord.matches("English"))
						{
							ttsrun.sayWord("English", english);
						}
						else if (hideWord.matches("Spanish"))
						{
							ttsrun.sayWord("Spanish", spanish);
						}
						break;
					case R.id.showText:
						if (showWord.matches("English"))
						{
							ttsrun.sayWord("English", english);
						}
						else if (showWord.matches("Spanish"))
						{
							ttsrun.sayWord("Spanish", spanish);
						}
						break;
					default:
				}
			}
		}
	}

	public void marks(View v)
	{
		//If the word is not marked right now, this will run.
		if (theMark == 0) //Should be 0.
		{
			theMark = 1;
		}
		//If the word is marked right now, this will run.
		else if (theMark == 1) //Should be 1.
		{
			theMark = 0;
		}
		//Updates the database.
		dBHelp.updateMark(wordID, theMark);
	}

	public void startDialog(int id, String userSaid2, int extra)
	{
		FragmentTransaction theTransaction = theManager.beginTransaction();
		Fragment dialogset = theManager.findFragmentByTag("dialogFragment");
		if (dialogset != null)
		{
			theTransaction.remove(dialogset);
		}
		DialogFragment newDialog = DialogsFragment.newInstance(null, id, userSaid2, 2, userName); //2 = null
		newDialog.show(theTransaction, "dialogFragment");
	}
}
