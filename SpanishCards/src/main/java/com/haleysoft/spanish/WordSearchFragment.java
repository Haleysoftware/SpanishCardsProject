package com.haleysoft.spanish;

/**
 * Created by Haleysoftware on 5/23/13.
 * Cleaned by Mike Haley on 9/7/13.
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Locale;

public class WordSearchFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
	//private static final int maxLevel = 150;
	private String userName = "Guest";
	private static WordDBFragment wordDb;
	private static ScoreAdapter cAdapter;
	private static long gotID;
	private static LoaderManager loader;
	private static WordSearchFragment markReset;
	private static String searchWord = null;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
		wordDb = (WordDBFragment) getFragmentManager().findFragmentByTag("wordFragment");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.searchlisttab, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		userName = getArguments().getString("user");
		final EditText search = (EditText) getActivity().findViewById(R.id.searchtext);
		Button finder = (Button) getActivity().findViewById(R.id.searchbutton);
		cAdapter = new ScoreAdapter(getActivity(), null);
		setListAdapter(cAdapter);
		getLoaderManager().initLoader(0, null, this);
		loader = getLoaderManager();
		markReset = this;

		//On Enter press
		search.setOnKeyListener(new OnKeyListener() {
			public boolean onKey(View V, int keyCode, KeyEvent event) {
				if (event.getAction() == KeyEvent.ACTION_DOWN) {
					if (keyCode == KeyEvent.KEYCODE_ENTER) {
						Object searching = search.getText();
						if (searching != null) {
							if (searching.toString().length() != 0) {
								if (searching.toString().matches(" ")) {
									TextView missing = (TextView) getActivity().findViewById(android.R.id.empty);
									missing.setText(R.string.missing_egg);
									searchWord = null;
									getLoaderManager().restartLoader(0, null, markReset);
								} else {
									searchWord = searching.toString().toLowerCase(Locale.US).trim();
									getLoaderManager().restartLoader(0, null, markReset);
								}
							} else {
								TextView missing = (TextView) getActivity().findViewById(android.R.id.empty);
								missing.setText(R.string.NoWords);
								searchWord = null;
								getLoaderManager().restartLoader(0, null, markReset);
							}
						}
						//Hides the soft keyboard
						InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
						imm.hideSoftInputFromWindow(search.getWindowToken(), 0);
						return true;
					}
				}
				return false;
			}
		});

		//On find button press
		finder.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Object searching = search.getText();
				if (searching != null) {
					if (searching.toString().length() != 0) {
						if (searching.toString().matches(" ")) {
							TextView missing = (TextView) getActivity().findViewById(android.R.id.empty);
							missing.setText(R.string.missing_egg);
							searchWord = null;
							getLoaderManager().restartLoader(0, null, markReset);
						} else {
							searchWord = searching.toString().toLowerCase(Locale.US).trim();
							getLoaderManager().restartLoader(0, null, markReset);
						}
					} else {
						TextView missing = (TextView) getActivity().findViewById(android.R.id.empty);
						missing.setText(R.string.NoWords);
						searchWord = null;
						getLoaderManager().restartLoader(0, null, markReset);
					}
				}
				//Hides the soft keyboard
				InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(search.getWindowToken(), 0);
			}
		});

		//On Row Clicked
		getListView().setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> list, View view, int row, long id) {
				SharedPreferences preferences = getActivity().getSharedPreferences(userName, 0);
				boolean ttsTest = preferences.getBoolean("tts_set", true);
				if (ttsTest) {
					TTSFragment ttsrun = (TTSFragment) getFragmentManager().findFragmentByTag("ttsFragment");
					if (ttsrun.goodTTS) {
						Object engWord = ((TextView) view.findViewById(R.id.englishBar)).getText();
						Object spnWord = ((TextView) view.findViewById(R.id.spanishBar)).getText();
						if (engWord != null && spnWord != null) {
							String engText = engWord.toString();
							String spnText = spnWord.toString();
							ttsrun.sayWord("English", engText);
							ttsrun.sayWord("Spanish", spnText);
						}
					}
				}
			}
		});

		//On Row Long Clicked
		getListView().setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> list, View view, int row, long id) {
				Object hintWord = ((TextView) view.findViewById(R.id.hintBar)).getText();
				Object markWord = ((TextView) view.findViewById(R.id.markBar)).getText();
				if (hintWord != null && markWord != null) {
					String hintText = hintWord.toString();
					String showHint = String.format(getString(R.string.hintText), hintText);
					int markText = Integer.parseInt(markWord.toString());
					if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) { //For old OS
						gotID = id;
						((WordList) getActivity()).startDialog(6, showHint, markText);
					} else { //For new OS
						gotID = id;
						((WordList) getActivity()).startDialog(6, showHint, markText);
					}
					return true;
				}
				return false;
			}
		});
	}

	@Override
	public void onResume() {
		super.onResume();
		changeView();
	}

	public void toggleKey() {
		SharedPreferences pref = getActivity().getSharedPreferences(userName, Context.MODE_PRIVATE);
		boolean keyShow = pref.getBoolean("word_key_set", true);
		pref.edit().putBoolean("word_key_set", !keyShow).commit();
		changeView();
	}

	public void changeView() {
		SharedPreferences pref = getActivity().getSharedPreferences(userName, Context.MODE_PRIVATE);
		boolean keyShow = pref.getBoolean("word_key_set", true);
		TextView rightKey = (TextView) getActivity().findViewById(R.id.rightKey);
		TextView leftKey = (TextView) getActivity().findViewById(R.id.leftKey);
		TextView categoryKey = (TextView) getActivity().findViewById(R.id.categoryKey);
		if (keyShow) {
			rightKey.setVisibility(View.VISIBLE);
			leftKey.setVisibility(View.VISIBLE);
			categoryKey.setVisibility(View.VISIBLE);
		} else {
			rightKey.setVisibility(View.GONE);
			leftKey.setVisibility(View.GONE);
			categoryKey.setVisibility(View.GONE);
		}
		rightKey.invalidate();
		leftKey.invalidate();
		categoryKey.invalidate();
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle pack) {
		Uri wordUri = WordDBCP.CONTENT_URI;
		SharedPreferences preferences = getActivity().getSharedPreferences(userName, 0);
		String searchTest = preferences.getString("search_list_set", "1");
		String[] getColumns = {WordDBCP.KEY_ROWID, WordDBCP.KEY_MARK, WordDBCP.KEY_NOTE, WordDBCP.KEY_CAT, WordDBCP.KEY_ENG, WordDBCP.KEY_SPAN};
		String sort = WordDBCP.KEY_ENG + " COLLATE NOCASE ASC";
		String where;
		String[] key = null;
		where = WordDBCP.KEY_ENG + " LIKE ? OR " + WordDBCP.KEY_AENG + " LIKE ? OR " + WordDBCP.KEY_SPAN + " LIKE ? OR " + WordDBCP.KEY_ASPAN + " LIKE ?";
		if (searchTest.contains("0")) { //User wants to search from start
			key = new String[]{searchWord + "%", searchWord + "%", searchWord + "%", searchWord + "%"};
		} else if (searchTest.contains("1")) { //User wants to search anywhere
			key = new String[]{"%" + searchWord + "%", "%" + searchWord + "%", "%" + searchWord + "%", "%" + searchWord + "%"};
		}
		return new CursorLoader(getActivity(), wordUri, getColumns, where, key, sort);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor words) {
		cAdapter.swapCursor(words);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> words) {
		cAdapter.swapCursor(null);
	}

	public static void markChange(int newMark) {
		wordDb.updateMark(gotID, newMark);
		loader.restartLoader(0, null, markReset);
	}

	class ScoreAdapter extends CursorAdapter {
		ScoreAdapter(Context ctx, Cursor theWord) {
			super(ctx, theWord, FLAG_REGISTER_CONTENT_OBSERVER);
		}

		@Override
		public void bindView(View row, Context ctx, Cursor word) {
			changeView();
			RowHolder holder = (RowHolder) row.getTag();
			holder.populateFrom(word, ctx);
		}

		@Override
		public View newView(Context ctx, Cursor word, ViewGroup group) {
			changeView();
			LayoutInflater inflater = getActivity().getLayoutInflater();
			View row = inflater.inflate(R.layout.wordlistbar, group, false);
			if (row != null) {
				TextView englishText = (TextView) row.findViewById(R.id.englishBar);
				TextView spanishText = (TextView) row.findViewById(R.id.spanishBar);
				TextView spanishTwoText = (TextView) row.findViewById(R.id.spanishTwoBar);
				TextView englishTwoText = (TextView) row.findViewById(R.id.englishTwoBar);
				englishText.setVisibility(View.VISIBLE);
				spanishText.setVisibility(View.VISIBLE);
				spanishTwoText.setVisibility(View.GONE);
				englishTwoText.setVisibility(View.GONE);
				RowHolder holder = new RowHolder(row);
				row.setTag(holder);
			}
			return row;
		}
	}

	static class RowHolder {
		private TextView gEnglish = null;
		private TextView gSpanish = null;
		private TextView gSpanishTwo = null;
		private TextView gEnglishTwo = null;
		private TextView gCategory = null;
		private TextView gHint = null;
		private TextView gMark = null;

		RowHolder(View row) {
			gEnglish = (TextView) row.findViewById(R.id.englishBar);
			gSpanish = (TextView) row.findViewById(R.id.spanishBar);
			gSpanishTwo = (TextView) row.findViewById(R.id.spanishTwoBar);
			gEnglishTwo = (TextView) row.findViewById(R.id.englishTwoBar);
			gCategory = (TextView) row.findViewById(R.id.categoryBar);
			gHint = (TextView) row.findViewById(R.id.hintBar);
			gMark = (TextView) row.findViewById(R.id.markBar);
		}

		void populateFrom(Cursor word, Context ctx) {
			gEnglish.setText(word.getString(word.getColumnIndex("English")));
			gSpanish.setText(word.getString(word.getColumnIndex("Spanish")));
			gSpanishTwo.setText(word.getString(word.getColumnIndex("Spanish")));
			gEnglishTwo.setText(word.getString(word.getColumnIndex("English")));
			gCategory.setText(WordSwapHelper.cateCodeToString(ctx, word.getString(word.getColumnIndex("Category"))));
			gHint.setText(WordSwapHelper.noteCodeToString(ctx, word.getString(word.getColumnIndex("Note"))));
			gMark.setText(word.getString(word.getColumnIndex("Marked")));
		}
	}
}
