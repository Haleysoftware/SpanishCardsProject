package com.haleysoft.spanish.dialogs;

/**
 * Created by Haleysoftware on 5/23/13.
 * Cleaned by Mike Haley on 8/25/13.
 */

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.haleysoft.spanish.OldSetDelete;
import com.haleysoft.spanish.R;
import com.haleysoft.spanish.SettingsMenu;

public class ActionDialog extends DialogFragment {
    private int preferenceID;
    private int actionID;
    private String userName;
    private String dialogTitle;
    private String dialogText;
    private String dataExtra;

    public static ActionDialog newInstance(int prefId, int actionId, String uName, String dTitle, String dText, String extra) {
        ActionDialog dialog = new ActionDialog();
        Bundle args = new Bundle();
        args.putInt("pref", prefId);
        args.putInt("action", actionId);
        args.putString("name", uName);
        args.putString("title", dTitle);
        args.putString("text", dText);
        args.putString("bouns", extra);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferenceID = getArguments().getInt("pref");
        actionID = getArguments().getInt("action");
        userName = getArguments().getString("name");
        dialogTitle = getArguments().getString("title");
        dialogText = getArguments().getString("text");
        dataExtra = getArguments().getString("bouns");
        SharedPreferences pref = getActivity().getSharedPreferences(userName, Context.MODE_PRIVATE);
        boolean userTheme = pref.getBoolean("theme_set", false);
        int theme;
        if (userTheme) {
            theme = (R.style.DialogThemeAlt);
        } else {
            theme = (R.style.DialogTheme);
        }
        setStyle(DialogsFragment.STYLE_NORMAL, theme);
    }

    @Override
    public void onPause() {
        super.onPause();
        dismiss();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(dialogTitle)
                .setMessage(dialogText)
                .setPositiveButton(R.string.WarningOkButtion, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                switch (preferenceID) {
                                    case 0: //3.0 up Settings
                                        ((SettingsMenu) getActivity()).onDialogOkay(actionID, dialogTitle, dataExtra);
                                        dismiss();
                                        break;
                                    case 1: //2.3 down Settings
                                        ((OldSetDelete) getActivity()).onDialogOkay(actionID, dialogTitle, dataExtra);
                                        dismiss();
                                        break;
                                    case 2: //Test Select Screen

                                        dismiss();
                                        break;
                                    case 3: //Score Screen

                                        dismiss();
                                        break;
                                    default: //nothing
                                        dismiss();
                                }
                            }
                        }
                )
                .setNegativeButton(R.string.WarningNoButtion, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dismiss();
                            }
                        }
                ).create();
    }
}
