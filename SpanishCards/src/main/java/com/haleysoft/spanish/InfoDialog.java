package com.haleysoft.spanish;

/**
 * Created by Haleysoftware on 5/23/13.
 */

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class InfoDialog extends DialogFragment
{
	private String userName;
	private String dialogTitle;
	private String dialogText;

	static InfoDialog newInstance(String uName, String dTitle, String dText)
	{
		InfoDialog dialog = new InfoDialog();
		Bundle args = new Bundle();
		args.putString("name", uName);
		args.putString("title", dTitle);
		args.putString("text", dText);
		dialog.setArguments(args);
		return dialog;
	}

	@Override
	public void onCreate (Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		userName = getArguments().getString("name");
		dialogTitle = getArguments().getString("title");
		dialogText = getArguments().getString("text");
		//setRetainInstance(true);
		SharedPreferences pref = getActivity().getSharedPreferences(userName, Context.MODE_PRIVATE);
		boolean userTheme = pref.getBoolean("theme_set", false);
		int theme;
		if (userTheme)
		{
			theme = (R.style.DialogThemeAlt);
		}
		else
		{
			theme = (R.style.DialogTheme);
		}
		int style = DialogFragment.STYLE_NORMAL;
		setStyle(style, theme);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		//userName = getArguments().getString("name");
		//dialogTitle = getArguments().getString("title");
		//dialogText = getArguments().getString("text");

		return new AlertDialog.Builder(getActivity())
				.setIcon(android.R.drawable.ic_dialog_info)
				.setTitle(dialogTitle)
				.setMessage(dialogText)
				.setPositiveButton(R.string.InfoOkButton,
				                   new DialogInterface.OnClickListener() {
					                   public void onClick(DialogInterface dialog, int whichButton) {
						                   dismiss();
					                   }
				                   }
				)
				.create();
	}
}
