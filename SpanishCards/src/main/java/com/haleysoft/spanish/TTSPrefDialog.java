package com.haleysoft.spanish;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

/**
 * Created by haleysoftware on 12/23/13.
 */
public class TTSPrefDialog extends DialogsFragment implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {
	private static String userName;
	private static int prefName;
	private static int fromSet;
	private static float curRate;
	private static float curPitch;
	private static String newValue = "1.0";
	private static SeekBar setBar;
	private static TextView curText;

	public static TTSPrefDialog newInstance(String userName, int set, int pref) {
		TTSPrefDialog dialog = new TTSPrefDialog();
		Bundle args = new Bundle();
		args.putString("userName", userName);
		args.putInt("fromSet", set);
		args.putInt("prefName", pref);
		dialog.setArguments(args);
		return dialog;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle args = getArguments();
		if (args != null) {
			userName = args.getString("userName");
			fromSet = args.getInt("fromSet");
			prefName = args.getInt("prefName");
		}
		SharedPreferences pref = getActivity().getSharedPreferences(userName, Context.MODE_PRIVATE);
		curRate = Float.valueOf(pref.getString("tts_rate_set", "1.0"));
		curPitch = Float.valueOf(pref.getString("tts_pitch_set", "1.0"));
		boolean userTheme = pref.getBoolean("theme_set", false);
		int theme;
		if (userTheme) {
			theme = (R.style.DialogThemeAlt);
		} else {
			theme = (R.style.DialogTheme);
		}
		this.setStyle(DialogFragment.STYLE_NORMAL, theme);
	}

	@Override
	public void onDestroyView() {
		Dialog dialog = getDialog();
		if (dialog != null && getRetainInstance()) {
			dialog.setOnDismissListener(null);
		}
		super.onDestroyView();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View dialog = inflater.inflate(R.layout.ttsprefdialoglayout, container, false);
		if (dialog != null) {
			Button setButton = (Button) dialog.findViewById(R.id.setbutton);
			Button resetButton = (Button) dialog.findViewById(R.id.resetbutton);
			Button cancleButton = (Button) dialog.findViewById(R.id.cancelbutton);
			setBar = (SeekBar) dialog.findViewById(R.id.seekBar);
			curText = (TextView) dialog.findViewById(R.id.curText);
			setButton.setOnClickListener(this);
			resetButton.setOnClickListener(this);
			cancleButton.setOnClickListener(this);
			setBar.setOnSeekBarChangeListener(this);
			String title = "";
			switch (prefName) {
				case 0: //This is for the rate
					title = getString(R.string.ttsrateset);
					setBar.setMax(20);
					setBar.setProgress(Math.round(curRate * 10));
					break;
				case 1: //This is for the pitch
					title = getString(R.string.ttspichset);
					setBar.setMax(20);
					setBar.setProgress(Math.round(curPitch * 10));
					break;
			}
			curText.setText(String.valueOf(setBar.getProgress() * 10) + "%");
			getDialog().setTitle(title);
		}
		return dialog;
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.setbutton:
				SharedPreferences pref = getActivity().getSharedPreferences(userName, Context.MODE_PRIVATE);
				SharedPreferences.Editor editor = pref.edit();
				switch (prefName) {
					case 0: //This is for the rate
						editor.putString("tts_rate_set", newValue);
						break;
					case 1: //This is for the pitch
						editor.putString("tts_pitch_set", newValue);
						break;
				}
				editor.commit();
				switch (fromSet) {
					case 0: //3.0 up Settings
						((SettingsMenu) getActivity()).updateScreen();
						break;
					case 1: //2.3 down Settings
						//((OldSetDelete)getActivity()).onDialogOkay(actionID, dialogTitle, dataExtra);
						break;
					case 2: //Test Select Screen

						break;
					case 3: //Score Screen

						break;
					default: //nothing
				}
				dismiss();
				break;
			case R.id.resetbutton:
				setBar.setProgress(10);
				break;
			case R.id.cancelbutton:
				this.dismiss();
				break;
		}
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int pos, boolean userChange) {
		int check;
		if (pos == 0) {
			check = 1;
			seekBar.setProgress(1);
		} else {
			check = pos;
		}
		float change = (float) check / 10;
		newValue = String.valueOf(change);
		curText.setText(String.valueOf(check * 10) + "%");
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {

	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {

	}
}
