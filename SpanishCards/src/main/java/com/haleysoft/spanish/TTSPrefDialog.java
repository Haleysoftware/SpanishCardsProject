package com.haleysoft.spanish;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

/**
 * Created by haleysoftware on 12/23/13.
 */
public class TTSPrefDialog extends DialogsFragment {
	private static String userName;
	private static int prefName;
	private static float maxPref;
	private static float minPref;
	private static float curPref;

	public static TTSPrefDialog newInstance(String userName, int pref, float maxPref, float minPref, float curPref) {
		TTSPrefDialog dialog = new TTSPrefDialog();
		Bundle args = new Bundle();
		args.putString("userName", userName);
		args.putInt("prefName", pref);
		args.putFloat("maxPref", maxPref);
		args.putFloat("minPref", minPref);
		args.putFloat("curPref", curPref);
		dialog.setArguments(args);
		return dialog;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle args = getArguments();
		if (args != null) {
			userName = args.getString("userName");
			prefName = args.getInt("prefName");
			maxPref = args.getFloat("maxPref");
			minPref = args.getFloat("minPref");
			curPref = args.getFloat("curPref");
		}
		SharedPreferences pref = getActivity().getSharedPreferences(userName, Context.MODE_PRIVATE);
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
	public void onPause() {
		super.onPause();
		this.dismiss();
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		String title = "";
		switch (prefName) {
			case 0:

				break;
			case 1:

				break;
		}
		return super.onCreateDialog(savedInstanceState);
	}
}
