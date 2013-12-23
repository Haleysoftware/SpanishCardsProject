package com.haleysoft.spanish;

/**
 * Created by Haleysoftware on 5/23/13.
 * Cleaned by Mike Haley on 9/6/13.
 */

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.Fragment;
import android.widget.Toast;

import java.util.Locale;

public class TTSFragment extends Fragment implements TextToSpeech.OnInitListener {
	private static String mode;
	//static final int DIALOG_WRONG_ID = 0;
	//static final int DIALOG_TYPE_ID = 1;
	public TextToSpeech TTS;
	public boolean goodTTS = false;
	public int TTS_DATA_CHECK = 0;
	public boolean oneTTS = true;

	public TTSFragment() {

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
		mode = getArguments().getString("mode");
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (TTS == null) {
			if (oneTTS) {
				checkTTS();
				oneTTS = false;
			}
		}
	}

	@Override
	public void onPause() {
		quietTTS();
		super.onPause();
	}

	@Override
	public void onDestroy() {
		quietTTS();
		if (TTS != null) {
			TTS.shutdown();
		}
		super.onDestroy();
	}

	private void checkTTS() {
		Intent intent = new Intent(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
		startActivityForResult(intent, TTS_DATA_CHECK);
	}

	public void quietTTS() {
		if (TTS != null) {
			TTS.stop();
		}
	}

	private void checkSpeak() {
		int usCheck = TTS.isLanguageAvailable(Locale.US);
		int esCheck = TTS.isLanguageAvailable(new Locale("spa", "ES"));
		switch (usCheck) {
			case TextToSpeech.LANG_AVAILABLE:
			case TextToSpeech.LANG_COUNTRY_AVAILABLE:
			case TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE:
				//Should be good.
				break;
			case TextToSpeech.LANG_MISSING_DATA:
			case TextToSpeech.LANG_NOT_SUPPORTED:
				//No language on Phone.
				if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) { //For old OS
					if (mode.matches("Word List")) {
						((WordList) getActivity()).startDialog(5, null, 2); //2 = null
					} else if (mode.matches("Test")) {
						((TestMain) getActivity()).startDialog(5, null);
					}
				} else { //For new OS
					if (mode.matches("Word List")) {
						WordList.ttsErrorDialog = true;
					} else if (mode.matches("Test")) {
						TestMain.ttsErrorDialog = true;
					}
				}
				break;
			default:
		}
		switch (esCheck) {
			case TextToSpeech.LANG_AVAILABLE:
			case TextToSpeech.LANG_COUNTRY_AVAILABLE:
			case TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE:
				//Should be good.
				break;
			case TextToSpeech.LANG_MISSING_DATA:
			case TextToSpeech.LANG_NOT_SUPPORTED:
				//No language on Phone.
				if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) { //For old OS
					if (mode.matches("Word List")) {
						((WordList) getActivity()).startDialog(5, null, 2); //2 = null
					} else if (mode.matches("Test")) {
						((TestMain) getActivity()).startDialog(5, null);
					}
				} else { //For new OS
					if (mode.matches("Word List")) {
						WordList.ttsErrorDialog = true;
					} else if (mode.matches("Test")) {
						TestMain.ttsErrorDialog = true;
					}
				}
				break;
			default:
		}

	}

	//This runs after the voice recognition activity is finished and handles the result.
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == TTS_DATA_CHECK) {
			switch (resultCode) {
				case TextToSpeech.Engine.CHECK_VOICE_DATA_MISSING_DATA: //This is a bug in JB
					Toast.makeText(getActivity(), "Missing Data in speech.", Toast.LENGTH_SHORT).show();
				case TextToSpeech.Engine.CHECK_VOICE_DATA_PASS:
					TTS = new TextToSpeech(this.getActivity().getApplicationContext(), this);
					break;
				case TextToSpeech.Engine.CHECK_VOICE_DATA_BAD_DATA:
					//case TextToSpeech.Engine.CHECK_VOICE_DATA_MISSING_DATA:
				case TextToSpeech.Engine.CHECK_VOICE_DATA_MISSING_VOLUME:
					//No TTS on Phone.
					if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) { //For old OS
						if (mode.matches("Word List")) {
							((WordList) getActivity()).startDialog(5, null, 2); //2 = null
						} else if (mode.matches("Test")) {
							((TestMain) getActivity()).startDialog(5, null);
						}
					} else { //For new OS
						if (mode.matches("Word List")) {
							WordList.ttsErrorDialog = true;
						} else if (mode.matches("Test")) {
							TestMain.ttsErrorDialog = true;
						}
					}
					break;
				case TextToSpeech.Engine.CHECK_VOICE_DATA_FAIL:
				default:
					Toast.makeText(getActivity(), getActivity().getString(R.string.badTTS), Toast.LENGTH_SHORT).show();
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onInit(int status) {
		switch (status) {
			case TextToSpeech.SUCCESS:
				goodTTS = true;
				checkSpeak();
				break;
			case TextToSpeech.ERROR:
				Toast.makeText(getActivity(), getActivity().getString(R.string.badTTS), Toast.LENGTH_LONG).show();
				break;
		}
	}

	public void sayWord(String type, String word) {
		TTS.setSpeechRate(0.5f);
		TTS.setPitch(1.0f);
		if (type.matches("English")) {
			TTS.setLanguage(Locale.US);
			TTS.speak(word, TextToSpeech.QUEUE_ADD, null);
		} else if (type.matches("Spanish")) {
			TTS.setLanguage(new Locale("spa", "ESP"));
			TTS.speak(word, TextToSpeech.QUEUE_ADD, null);
		}
	}
}
