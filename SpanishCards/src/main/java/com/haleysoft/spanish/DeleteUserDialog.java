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

public class DeleteUserDialog extends DialogFragment {
	private int preferenceID;
	private String delName;
	private String delRow;

	public static DeleteUserDialog newInstance (int prefId, String uName, String dName, String dRow) {
		DeleteUserDialog dialog = new DeleteUserDialog();
		Bundle args = new Bundle();
		args.putInt("pref", prefId);
		args.putString("name", uName);
		args.putString("delete", dName);
		args.putString("row", dRow);
		dialog.setArguments(args);
		return dialog;
	}

	@Override
	public void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		preferenceID = getArguments().getInt("pref");
		String userName = getArguments().getString("name");
		delName = getArguments().getString("delete");
		delRow = getArguments().getString("row");
		//setRetainInstance(true);
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
	public Dialog onCreateDialog (Bundle savedInstanceState) {
		return new AlertDialog.Builder(getActivity())
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle(R.string.setremovedialogtitle)
				.setMessage(R.string.setremovedialogtext)
				.setPositiveButton(R.string.setremovedialogyes, new DialogInterface.OnClickListener() {
	                   public void onClick(DialogInterface dialog, int whichButton) {
		                   dismiss();
		                   switch (preferenceID) {
			                   case 0: //3.0 up Settings
				                   ((SettingsMenu)getActivity()).removeUser(delName, delRow, true);
				                   break;
			                   case 1: //2.3 down Settings
				                   ((OldSetDelete)getActivity()).removeUser(delName, delRow, true);
				                   break;
			                   case 2: //Test Select Screen

				                   break;
			                   case 3: //Score Screen

				                   break;
			                   default: //nothing
		                   }
	                   }
                   }
				)
				.setNegativeButton(R.string.setremovedialogno, new DialogInterface.OnClickListener() {
	                   public void onClick(DialogInterface dialog, int whichButton) {
		                   dismiss();
		                   switch (preferenceID) {
			                   case 0: //3.0 up Settings
				                   ((SettingsMenu)getActivity()).removeUser(delName, delRow, false);
				                   break;
			                   case 1: //2.3 down Settings
				                   ((OldSetDelete)getActivity()).removeUser(delName, delRow, false);
				                   break;
			                   case 2: //Test Select Screen

				                   break;
			                   case 3: //Score Screen

				                   break;
			                   default: //nothing
		                   }
	                   }
                   }
				).create();
	}
}
