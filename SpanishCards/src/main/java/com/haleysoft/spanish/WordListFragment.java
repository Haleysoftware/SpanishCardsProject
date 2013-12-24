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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

public class WordListFragment extends ListFragment implements OnItemSelectedListener, LoaderManager.LoaderCallbacks<Cursor> {
	//private static final int maxLevel = 150;
	private String userName = "Guest";
	private static boolean safeLoad = false;
	private static String theOrder = "English Ascending";
	private static String theFilter = "All";
	private static WordDBFragment wordDb;
	private static WordAdapter cAdapter;
	private static long gotID;
	private static LoaderManager loader;
	private static WordListFragment markReset;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
		wordDb = (WordDBFragment) getFragmentManager().findFragmentByTag("wordFragment"); //This is for the mark change
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.wordlisttab, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		userName = getArguments().getString("user");
		spinSetupOne();
		spinSetupTwo();
		cAdapter = new WordAdapter(getActivity(), null);
		setListAdapter(cAdapter);
		getLoaderManager().initLoader(0, null, this);
		loader = getLoaderManager();
		markReset = this;
		safeLoad = true;
		//On Row Clicked
		getListView().setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> list, View view, int row, long id) {
				SharedPreferences preferences = getActivity().getSharedPreferences(userName, 0);
				float ttsRate = Float.valueOf(preferences.getString("tts_rate_set", "1.0"));
				float ttsPitch = Float.valueOf(preferences.getString("tts_pitch_set", "1.0"));
				boolean ttsTest = preferences.getBoolean("tts_set", true);
				if (ttsTest) {
					TTSFragment ttsrun = (TTSFragment) getFragmentManager().findFragmentByTag("ttsFragment");
					if (ttsrun.goodTTS) {
						Object engWord = ((TextView) view.findViewById(R.id.englishBar)).getText();
						Object spnWord = ((TextView) view.findViewById(R.id.spanishBar)).getText();
						if (engWord != null && spnWord != null) {
							String engText = engWord.toString();
							String spnText = spnWord.toString();
							Spinner spinOrder = (Spinner) getActivity().findViewById(R.id.spinnerWordsSort);
							Object spinItem = spinOrder.getSelectedItem();
							if (spinItem != null) {
								if (spinItem.toString().matches(getString(R.string.sortArrayEAsc)) || spinItem.toString().matches(getString(R.string.sortArrayEDes))) {
									ttsrun.sayWord("English", engText, ttsRate, ttsPitch);
									ttsrun.sayWord("Spanish", spnText, ttsRate, ttsPitch);
								} else if (spinItem.toString().matches(getString(R.string.sortArraySAsc)) || spinItem.toString().matches(getString(R.string.sortArraySDes))) {
									ttsrun.sayWord("Spanish", spnText, ttsRate, ttsPitch);
									ttsrun.sayWord("English", engText, ttsRate, ttsPitch);
								}
							}
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
						((WordList) getActivity()).startDialog(4, showHint, markText);
					} else { //For new OS
						gotID = id;
						((WordList) getActivity()).startDialog(4, showHint, markText);
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

	private void spinSetupOne() {
		Spinner spinner = (Spinner) getActivity().findViewById(R.id.spinnerWordsSort);
		ArrayAdapter<CharSequence> adapterOne = ArrayAdapter.createFromResource(getActivity(), R.array.SortArray, android.R.layout.simple_spinner_item);
		adapterOne.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapterOne);
		spinner.setOnItemSelectedListener(this);
	}

	private void spinSetupTwo() {
		Spinner spinner = (Spinner) getActivity().findViewById(R.id.spinnerWordsList);
		ArrayAdapter<CharSequence> adapterTwo = ArrayAdapter.createFromResource(getActivity(), R.array.Levelarray, android.R.layout.simple_spinner_item);
		adapterTwo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapterTwo);
		spinner.setOnItemSelectedListener(this);
	}

	@Override //This is for when the spinner is selected.
	public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
		Spinner spinSelect = (Spinner) getActivity().findViewById(R.id.spinnerWordsList);
		Spinner spinOrder = (Spinner) getActivity().findViewById(R.id.spinnerWordsSort);
		Object selectWord = spinSelect.getSelectedItem();
		Object orderWord = spinOrder.getSelectedItem();
		if (selectWord != null && orderWord != null) {
			theFilter = selectWord.toString();
			theOrder = orderWord.toString();
		}
		if (safeLoad) {
			getLoaderManager().restartLoader(0, null, this);
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		//Do nothing
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
		String[] getColumns = {WordDBCP.KEY_ROWID, WordDBCP.KEY_MARK, WordDBCP.KEY_NOTE, WordDBCP.KEY_CAT, WordDBCP.KEY_ENG, WordDBCP.KEY_SPAN};
		String where;
		String[] key;
		String sort;
		if (theFilter.matches(getString(R.string.arrayAll))) {
			where = null;
			key = null;
		} else if (theFilter.matches(getString(R.string.arrayMarked))) {
			where = WordDBCP.KEY_MARK + " LIKE ?";
			key = new String[]{"1"};
		} else if (theFilter.matches(getString(R.string.arrayLevel))) {
			SharedPreferences preferences = getActivity().getSharedPreferences(userName, Context.MODE_PRIVATE);
			int userLevel = preferences.getInt("user_level", 1);
			where = WordDBCP.KEY_LEVEL + " <= ?";
			key = new String[]{String.valueOf(userLevel)};
		} else {
			String search = WordSwapHelper.cateStringToCode(getActivity(), theFilter);
			where = WordDBCP.KEY_CAT + " LIKE ?";
			key = new String[]{search};
		}
		if (theOrder.matches(getString(R.string.sortArrayEAsc))) {
			sort = WordDBCP.KEY_ENG + " COLLATE NOCASE ASC";
		} else if (theOrder.matches(getString(R.string.sortArrayEDes))) {
			sort = WordDBCP.KEY_ENG + " COLLATE NOCASE DESC";
		} else if (theOrder.matches(getString(R.string.sortArraySAsc))) {
			sort = WordDBCP.KEY_SPAN + " COLLATE NOCASE ASC";
		} else if (theOrder.matches(getString(R.string.sortArraySDes))) {
			sort = WordDBCP.KEY_SPAN + " COLLATE NOCASE DESC";
		} else {
			sort = null;
		}
		return new CursorLoader(getActivity(), wordUri, getColumns, where, key, sort);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor words) {
		cAdapter.swapCursor(words);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> cursor) {
		cAdapter.swapCursor(null);
	}

	public static void markChange(int newMark) {
		wordDb.updateMark(gotID, newMark);
		loader.restartLoader(0, null, markReset);
	}

	class WordAdapter extends CursorAdapter {
		WordAdapter(Context ctx, Cursor theWord) {
			super(ctx, theWord, FLAG_REGISTER_CONTENT_OBSERVER);
		}

		@Override
		public void bindView(View row, Context ctx, Cursor word) {
			changeView();
			RowHolder holder = (RowHolder) row.getTag();
			holder.populateFrom(word, ctx);
			TextView englishText = (TextView) row.findViewById(R.id.englishBar);
			TextView spanishText = (TextView) row.findViewById(R.id.spanishBar);
			TextView spanishTwoText = (TextView) row.findViewById(R.id.spanishTwoBar);
			TextView englishTwoText = (TextView) row.findViewById(R.id.englishTwoBar);
			TextView rightKey = (TextView) getActivity().findViewById(R.id.rightKey);
			TextView leftKey = (TextView) getActivity().findViewById(R.id.leftKey);
			if (theOrder.matches(getString(R.string.sortArrayEAsc)) || theOrder.matches(getString(R.string.sortArrayEDes))) {
				englishText.setVisibility(View.VISIBLE);
				spanishText.setVisibility(View.VISIBLE);
				spanishTwoText.setVisibility(View.GONE);
				englishTwoText.setVisibility(View.GONE);
				leftKey.setText(R.string.tagEnglish);
				rightKey.setText(R.string.tagSpanish);
			} else if (theOrder.matches(getString(R.string.sortArraySAsc)) || theOrder.matches(getString(R.string.sortArraySDes))) {
				englishText.setVisibility(View.GONE);
				spanishText.setVisibility(View.GONE);
				spanishTwoText.setVisibility(View.VISIBLE);
				englishTwoText.setVisibility(View.VISIBLE);
				leftKey.setText(R.string.tagSpanish);
				rightKey.setText(R.string.tagEnglish);
			}
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
				TextView rightKey = (TextView) getActivity().findViewById(R.id.rightKey);
				TextView leftKey = (TextView) getActivity().findViewById(R.id.leftKey);
				if (theOrder.matches(getString(R.string.sortArrayEAsc)) || theOrder.matches(getString(R.string.sortArrayEDes))) {
					englishText.setVisibility(View.VISIBLE);
					spanishText.setVisibility(View.VISIBLE);
					spanishTwoText.setVisibility(View.GONE);
					englishTwoText.setVisibility(View.GONE);
					leftKey.setText(R.string.tagEnglish);
					rightKey.setText(R.string.tagSpanish);
				} else if (theOrder.matches(getString(R.string.sortArraySAsc)) || theOrder.matches(getString(R.string.sortArraySDes))) {
					englishText.setVisibility(View.GONE);
					spanishText.setVisibility(View.GONE);
					spanishTwoText.setVisibility(View.VISIBLE);
					englishTwoText.setVisibility(View.VISIBLE);
					leftKey.setText(R.string.tagSpanish);
					rightKey.setText(R.string.tagEnglish);
				}
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
