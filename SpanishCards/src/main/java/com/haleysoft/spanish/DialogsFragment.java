package com.haleysoft.spanish;

/**
 * Created by Haleysoftware on 5/23/13.
 * Cleaned by Mike Haley on 8/25/13.
 */

import java.util.Locale;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

public class DialogsFragment extends DialogFragment {
	private static String userName;
	private String hintText; //Used to pass the hint word from the word list and search
	private String userSaid;
	private String userTyped;
	private int extraData;
	private int ID;

	static DialogsFragment newInstance(String hint, int id, String userSaid2, int extra, String userName) {
		DialogsFragment dialog = new DialogsFragment();
		Bundle args = new Bundle();
		args.putString("name", userName);
		args.putString("hint", hint);
		args.putString("said", userSaid2);
		args.putInt("id", id);
		args.putInt("extra", extra);
		dialog.setArguments(args);
		return dialog;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		userName = getArguments().getString("name");
		hintText = getArguments().getString("hint");
		userSaid = getArguments().getString("said");
		ID = getArguments().getInt("id");
		extraData = getArguments().getInt("extra");
		setRetainInstance(true);
		SharedPreferences pref = getActivity().getSharedPreferences(userName, Context.MODE_PRIVATE);
		boolean userTheme = pref.getBoolean("theme_set", false);
		int theme;
		if (userTheme) {
			theme = (R.style.DialogThemeAlt);
		} else {
			theme = (R.style.DialogTheme);
		}
		setStyle(DialogFragment.STYLE_NORMAL, theme);
	}

	//Dirty workaround for fixing on rotate
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
		View dialog = null;
		if (ID == 0) { //Dialog for the user to enter text
			dialog = inflater.inflate(R.layout.enterdialoglayout, container, false);
            if (dialog != null) {
			    getDialog().setTitle(R.string.ETitle);
                Button submit = (Button) dialog.findViewById(R.id.submitbutton);
                Button cancel = (Button) dialog.findViewById(R.id.cancelbutton);
                final EditText text = (EditText) dialog.findViewById(R.id.enteredText);

                //On Enter press
                text.setOnKeyListener(new OnKeyListener() {
                    public boolean onKey(View V, int keyCode, KeyEvent event) {
                        if (event.getAction() == KeyEvent.ACTION_DOWN) {
                            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                                Editable words = text.getText();
                                if (words != null) {
                                    if (words.length() != 0) {
                                        userTyped = words.toString().trim().toLowerCase(Locale.US);
                                        ((TestMain)getActivity()).UserText(userTyped);
                                        dismiss();
                                    }
                                    return true;
                                }
                            }
                        }
                        return false;
                    }
                });

                //On button press
                submit.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Editable words = text.getText();
                        if (words !=null) {
                            if (words.length() != 0) {
                                userTyped = words.toString().trim().toLowerCase(Locale.US);
                                ((TestMain)getActivity()).UserText(userTyped);
                                dismiss();
                            }
                        }
                    }
                });
                cancel.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dismiss();
                    }
                });
            }
		}
		else if (ID == 1) { //Dialog for when the wrong word was said
			dialog = inflater.inflate(R.layout.wrongdialoglayout, container, false);
            if (dialog != null) {
                getDialog().setTitle(R.string.WTitleSaid);
                TextView error = (TextView) dialog.findViewById(R.id.errortext);
                TextView said = (TextView) dialog.findViewById(R.id.saidthis);
                error.setVisibility(View.VISIBLE);
                said.setText(userSaid);

                //On button press
                Button ok = (Button) dialog.findViewById(R.id.okbutton);
                Button retry = (Button) dialog.findViewById(R.id.retrybutton);
                ok.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dismiss();
                    }
                });
                retry.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ((TestMain)getActivity()).startVoice();
                        dismiss();
                    }
                });
            }
		}
		else if (ID == 2) { //Dialog for when the wrong text was entered
			dialog = inflater.inflate(R.layout.wrongdialoglayout, container, false);
            if (dialog != null) {
                getDialog().setTitle(R.string.WTitleEnter);
                TextView error = (TextView) dialog.findViewById(R.id.errortext);
                TextView said = (TextView) dialog.findViewById(R.id.saidthis);
                error.setVisibility(View.INVISIBLE);
                said.setText(R.string.WText);

                //On button press
                Button ok = (Button) dialog.findViewById(R.id.okbutton);
                Button retry = (Button) dialog.findViewById(R.id.retrybutton);
                ok.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dismiss();
                    }
                });
                retry.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ((TestMain)getActivity()).startText();
                        dismiss();
                    }
                });
            }
		}
		else if (ID == 3) { //Dialog for the user to enter their name
			dialog = inflater.inflate(R.layout.namedialoglayout, container, false);
            if (dialog != null) {
                getDialog().setTitle(R.string.NTitle);
                final EditText text = (EditText) dialog.findViewById(R.id.editName);
                Button enter = (Button) dialog.findViewById(R.id.enterButton);
                Button mind = (Button) dialog.findViewById(R.id.nMindButton);
                Spinner spinner = (Spinner) getActivity().findViewById(R.id.spinnerName);
                spinner.setSelection(extraData);

                //On Enter press
                text.setOnKeyListener(new OnKeyListener() {
                    public boolean onKey(View V, int keyCode, KeyEvent event) {
                        if (event.getAction() == KeyEvent.ACTION_DOWN) {
                            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                                Editable words = text.getText();
                                if (words != null) {
                                    if (words.length() != 0) {
                                        userTyped = words.toString().trim();
                                        ((TestSelect)getActivity()).addUser(userTyped);
                                        dismiss();
                                    }
                                    return true;
                                }
                            }
                        }
                        return false;
                    }
                });

                //On button press
                enter.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Editable words = text.getText();
                        if (words != null) {
                            if (words.length() != 0) {
                                userTyped = words.toString().trim();
                                ((TestSelect)getActivity()).addUser(userTyped);
                                dismiss();
                            }
                        }
                    }
                });
                mind.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dismiss();
                    }
                });
            }
		}
		else if (ID == 4) { //Dialog for when the user wants to see the hint in the word list
			dialog = inflater.inflate(R.layout.hintdialoglayout, container, false);
            if (dialog != null) {
                Button done = (Button) dialog.findViewById(R.id.hintOkButton);
                ToggleButton mark = (ToggleButton) dialog.findViewById(R.id.hintMarkButton);

                if (hintText.contains("0")) { //No hint for word
                    getDialog().setTitle(R.string.HintTitleText);
                } else {
                    getDialog().setTitle(hintText); //Word has a hint
                }

                if (extraData == 1) {
                    mark.setChecked(true);
                } else if (extraData == 0) {
                    mark.setChecked(false);
                }

                //On button press
                mark.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (extraData == 1) {
                            extraData = 0;
                        } else if (extraData == 0) {
                            extraData = 1;
                        }
                        WordListFragment.markChange(extraData);
                    }
                });

                done.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dismiss();
                    }
                });
            }
		}
		else if (ID == 5) { //Dialog to ask user if they want to install a TTS
			dialog = inflater.inflate(R.layout.nottsdialoglayout, container, false);
            if (dialog != null) {
                getDialog().setTitle(R.string.NoTtsTitle);

                //On button press
                Button install = (Button) dialog.findViewById(R.id.ttsDialogInstall);
                Button off = (Button) dialog.findViewById(R.id.ttsDialogOff);
                install.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Maybe move this to a new method in the TTS Fragment and have it called here?
                        //Need to find out if ACTION_TTS_DATA_INSTALLED is called after install is done.
                        Intent installIntent = new Intent();
                        installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                        startActivity(installIntent);
                        dismiss();
                    }
                });
                off.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getActivity().getSharedPreferences(userName, 0).edit().putBoolean("tts_set", false).commit();

                        dismiss();
                    }
                });
            }
		}
		else if (ID == 6) { //Dialog for when the user wants to see the hint in the word search
			dialog = inflater.inflate(R.layout.hintdialoglayout, container, false);
            if (dialog != null) {
                Button done = (Button) dialog.findViewById(R.id.hintOkButton);
                final ToggleButton mark = (ToggleButton) dialog.findViewById(R.id.hintMarkButton);

                if (hintText.contains("0")) { //No hint for word
                    getDialog().setTitle(R.string.HintTitleText);
                } else {
                    getDialog().setTitle(hintText); //Word has a hint
                }

                if (extraData == 1) {
                    mark.setChecked(true);
                } else if (extraData == 0) {
                    mark.setChecked(false);
                }


                //On button press
                mark.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (extraData == 1) {
                            extraData = 0;
                        } else if (extraData == 0) {
                            extraData = 1;
                        }
                        WordSearchFragment.markChange(extraData);
                    }
                });

                done.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dismiss();
                    }
                });
            }
		}
		return dialog;
	}
}
