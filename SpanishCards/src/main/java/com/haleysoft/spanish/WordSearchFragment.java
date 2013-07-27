package com.haleysoft.spanish;

/**
 * Created by Haleysoftware on 5/23/13.
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
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

public class WordSearchFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor>
{
	private static final int demoLevel = 30;
	//private static final int maxLevel = 150;
	private String userName = "Guest";
	private static WordDBFragment wordDb;
	private static ScoreAdapter cAdapter;
	private static Cursor words;
	private static long gotID;
	private static LoaderManager loader;
	private static WordSearchFragment markReset;
	private static String searchWord = null;
	private static final int VISIBLE = 0;
	private static final int GONE = 8;

	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
		wordDb = (WordDBFragment) getFragmentManager().findFragmentByTag("wordFragment");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.searchlisttab, container, false);
	}

	@Override
	public void onActivityCreated (Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		userName = getArguments().getString("user");
		final EditText search = (EditText) getActivity().findViewById(R.id.searchtext);
		Button finder = (Button) getActivity().findViewById(R.id.searchbutton);
		cAdapter = new ScoreAdapter(getActivity(), words);
		setListAdapter(cAdapter);
		getLoaderManager().initLoader(0, null, this);
		loader = getLoaderManager();
		markReset = this;

		//On Enter press
		search.setOnKeyListener(new OnKeyListener()
		{
			public boolean onKey(View V, int keyCode, KeyEvent event)
			{
				if (event.getAction() == KeyEvent.ACTION_DOWN)
				{
					if (keyCode == KeyEvent.KEYCODE_ENTER)
					{
						if (search.getText().length() != 0)
						{
							if (search.getText().toString().matches(" "))
							{
								TextView missing = (TextView) getActivity().findViewById(android.R.id.empty);
								missing.setText(R.string.missing_egg);
								searchWord = null;
								getLoaderManager().restartLoader(0, null, markReset);
							}
							else
							{
								searchWord = search.getText().toString().toLowerCase(Locale.US).trim();
								getLoaderManager().restartLoader(0, null, markReset);
							}
						}
						else
						{
							TextView missing = (TextView) getActivity().findViewById(android.R.id.empty);
							missing.setText(R.string.NoWords);
							searchWord = null;
							getLoaderManager().restartLoader(0, null, markReset);
						}
						//Hides the soft keyboard
						InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
						imm.hideSoftInputFromWindow(search.getWindowToken(), 0);
						return true;
					}
				}
				return false;
			}
		});

		//On find button press
		finder.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if (search.getText().length() != 0)
				{
					if (search.getText().toString().matches(" "))
					{
						TextView missing = (TextView) getActivity().findViewById(android.R.id.empty);
						missing.setText(R.string.missing_egg);
						searchWord = null;
						getLoaderManager().restartLoader(0, null, markReset);
					}
					else
					{
						searchWord = search.getText().toString().toLowerCase(Locale.US).trim();
						getLoaderManager().restartLoader(0, null, markReset);
					}
				}
				else
				{
					TextView missing = (TextView) getActivity().findViewById(android.R.id.empty);
					missing.setText(R.string.NoWords);
					searchWord = null;
					getLoaderManager().restartLoader(0, null, markReset);
				}
				//Hides the soft keyboard
				InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(search.getWindowToken(), 0);
			}
		});

		//On Row Clicked
		getListView().setOnItemClickListener( new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> list, View view, int row, long id)
			{
				SharedPreferences preferences = getActivity().getSharedPreferences(userName, 0);
				boolean ttsTest = preferences.getBoolean("tts_set", true);
				if (ttsTest)
				{
					TTSFragment ttsrun = (TTSFragment) getFragmentManager().findFragmentByTag("ttsFragment");
					if (ttsrun.goodTTS)
					{
						String engText = ((TextView)view.findViewById(R.id.englishBar)).getText().toString();
						String spnText = ((TextView)view.findViewById(R.id.spanishBar)).getText().toString();
						ttsrun.sayWord("English", engText);
						ttsrun.sayWord("Spanish", spnText);
					}
				}
			}
		});

		//On Row Long Clicked
		getListView().setOnItemLongClickListener( new OnItemLongClickListener()
		{
			@Override
			public boolean onItemLongClick(AdapterView<?> list, View view, int row, long id)
			{
				String hintText = ((TextView)view.findViewById(R.id.hintBar)).getText().toString();
				String showHint = String.format(getString(R.string.hintText), hintText);
				int markText = Integer.parseInt(((TextView)view.findViewById(R.id.markBar)).getText().toString());
				if (Build.VERSION.SDK_INT<Build.VERSION_CODES.HONEYCOMB) //For old OS
				{
					gotID = id;
					((WordList)getActivity()).startDialog(6, showHint, markText);
				}
				else //For new OS
				{
					gotID = id;
					((WordList)getActivity()).startDialog(6, showHint, markText);
				}
				return true;
			}
		});
	}

	@Override
	public void onPause ()
	{
		super.onPause();
	}

	@Override
	public void onResume()
	{
		super.onResume();
		changeView();
	}

	@Override
	public void onDestroyView()
	{
		super.onDestroyView();
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
	}

	private boolean checkPaid()
	{
		String mainAppPkg = "com.haleysoft.spanish";
		String keyPkg = "com.haleysoft.spanish.key";
		String cardKeyPkg = "com.haleysoft.card.key";
		String haleyKeyPkg = "com.haleysoft.master.key";
		PackageManager manager = getActivity().getPackageManager();
		int sigMatch = manager.checkSignatures(mainAppPkg, keyPkg);
		int sigCardMatch = manager.checkSignatures(mainAppPkg, cardKeyPkg);
		int sigHaleyMatch = manager.checkSignatures(mainAppPkg, haleyKeyPkg);
		return sigMatch == PackageManager.SIGNATURE_MATCH
				|| sigCardMatch == PackageManager.SIGNATURE_MATCH
				|| sigHaleyMatch == PackageManager.SIGNATURE_MATCH;
	}

	public void toggleKey()
	{
		SharedPreferences pref = getActivity().getSharedPreferences(userName, Context.MODE_PRIVATE);
		boolean keyShow = pref.getBoolean("word_key_set", true);
		boolean newKey;
		if (keyShow)
		{
			newKey = false;
		}
		else
		{
			newKey = true;
		}
		pref.edit().putBoolean("word_key_set", newKey).commit();
		changeView();
	}

	public void changeView()
	{
		SharedPreferences pref = getActivity().getSharedPreferences(userName, Context.MODE_PRIVATE);
		boolean keyShow = pref.getBoolean("word_key_set", true);
		TextView rightKey = (TextView) getActivity().findViewById(R.id.rightKey);
		TextView leftKey = (TextView) getActivity().findViewById(R.id.leftKey);
		TextView categoryKey = (TextView) getActivity().findViewById(R.id.categoryKey);
		if (keyShow)
		{
			rightKey.setVisibility(View.VISIBLE);
			leftKey.setVisibility(View.VISIBLE);
			categoryKey.setVisibility(View.VISIBLE);
		}
		else
		{
			rightKey.setVisibility(View.GONE);
			leftKey.setVisibility(View.GONE);
			categoryKey.setVisibility(View.GONE);
		}
		rightKey.invalidate();
		leftKey.invalidate();
		categoryKey.invalidate();
	}

	@Override //LoadManager
	public Loader<Cursor> onCreateLoader(int id, Bundle pack)
	{
		Uri wordUri = WordDBCP.CONTENT_URI;
		SharedPreferences preferences = getActivity().getSharedPreferences(userName, 0);
		String searchTest = preferences.getString("search_list_set", "1");
		String[] getColumns = {WordDBCP.KEY_ROWID, WordDBCP.KEY_MARK, WordDBCP.KEY_NOTE, WordDBCP.KEY_CAT, WordDBCP.KEY_ENG, WordDBCP.KEY_SPAN};
		String sort = WordDBCP.KEY_ENG + " COLLATE NOCASE ASC";
		String where = null;
		String[] key = null;
		if (checkPaid())
		{
			where = WordDBCP.KEY_ENG + " LIKE ? OR " + WordDBCP.KEY_AENG + " LIKE ? OR " + WordDBCP.KEY_SPAN + " LIKE ? OR " + WordDBCP.KEY_ASPAN + " LIKE ?";
			if (searchTest.contains("0")) //User wants to search from start
			{
				key = new String[] {searchWord + "%", searchWord + "%", searchWord + "%", searchWord + "%"};
			}
			else if (searchTest.contains("1")) //User wants to search anywhere
			{
				key = new String[] {"%" + searchWord + "%", "%" + searchWord + "%", "%" + searchWord + "%", "%" + searchWord + "%"};
			}
		}
		else
		{
			where = WordDBCP.KEY_ENG + " LIKE ? AND " + WordDBCP.KEY_LEVEL + " <= ? OR " + WordDBCP.KEY_AENG + " LIKE ? AND " + WordDBCP.KEY_LEVEL + " <= ? OR " + WordDBCP.KEY_SPAN + " LIKE ? AND " + WordDBCP.KEY_LEVEL + " <= ? OR " + WordDBCP.KEY_ASPAN + " LIKE ? AND " + WordDBCP.KEY_LEVEL + " <= ?";
			if (searchTest.contains("0")) //User wants to search from start
			{
				key = new String[] {searchWord + "%", String.valueOf(demoLevel), searchWord + "%", String.valueOf(demoLevel), searchWord + "%", String.valueOf(demoLevel), searchWord + "%", String.valueOf(demoLevel)};
			}
			else if (searchTest.contains("1")) //User wants to search anywhere
			{
				key = new String[] {"%" + searchWord + "%", String.valueOf(demoLevel), "%" + searchWord + "%", String.valueOf(demoLevel), "%" + searchWord + "%", String.valueOf(demoLevel), "%" + searchWord + "%", String.valueOf(demoLevel)};
			}
		}
		CursorLoader wordCursor = new CursorLoader(getActivity(), wordUri, getColumns, where, key, sort);
		return wordCursor;
	}

	@Override //LoadManager
	public void onLoadFinished(Loader<Cursor> loader, Cursor words)
	{
		cAdapter.swapCursor(words);
	}

	@Override //LoadManager
	public void onLoaderReset(Loader<Cursor> words)
	{
		cAdapter.swapCursor(null);
	}

	public static void markChange (int newMark)
	{
		wordDb.updateMark(gotID, newMark);
		loader.restartLoader(0, null, markReset);
	}

	class ScoreAdapter extends CursorAdapter
	{
		ScoreAdapter (Context ctx, Cursor theWord)
		{
			super(getActivity(), theWord,  FLAG_REGISTER_CONTENT_OBSERVER);
		}

		@Override
		public void bindView(View row, Context ctx, Cursor word)
		{
			changeView();
			RowHolder holder = (RowHolder) row.getTag();
			holder.populateFrom(word, ctx);
		}

		@Override
		public View newView(Context ctx, Cursor word, ViewGroup group)
		{
			changeView();
			LayoutInflater inflater = getActivity().getLayoutInflater();
			View row = inflater.inflate(R.layout.wordlistbar, group, false);
			TextView englishText = (TextView) row.findViewById(R.id.englishBar);
			TextView spanishText = (TextView) row.findViewById(R.id.spanishBar);
			TextView spanishTwoText = (TextView) row.findViewById(R.id.spanishTwoBar);
			TextView englishTwoText = (TextView) row.findViewById(R.id.englishTwoBar);
			englishText.setVisibility(VISIBLE);
			spanishText.setVisibility(VISIBLE);
			spanishTwoText.setVisibility(GONE);
			englishTwoText.setVisibility(GONE);
			RowHolder holder = new RowHolder(row);
			row.setTag(holder);
			return row;
		}
	}

	static class RowHolder
	{
		private TextView gEnglish = null;
		private TextView gSpanish = null;
		private TextView gSpanishTwo = null;
		private TextView gEnglishTwo = null;
		private TextView gCategory = null;
		private TextView gHint = null;
		private TextView gMark = null;

		RowHolder(View row)
		{
			gEnglish = (TextView) row.findViewById(R.id.englishBar);
			gSpanish = (TextView) row.findViewById(R.id.spanishBar);
			gSpanishTwo = (TextView) row.findViewById(R.id.spanishTwoBar);
			gEnglishTwo = (TextView) row.findViewById(R.id.englishTwoBar);
			gCategory = (TextView) row.findViewById(R.id.categoryBar);
			gHint = (TextView) row.findViewById(R.id.hintBar);
			gMark = (TextView) row.findViewById(R.id.markBar);
		}

		void populateFrom(Cursor word, Context ctx)
		{
			//String sCategory;
			//String sHint;
			gEnglish.setText(word.getString(word.getColumnIndex("English")));
			gSpanish.setText(word.getString(word.getColumnIndex("Spanish")));
			gSpanishTwo.setText(word.getString(word.getColumnIndex("Spanish")));
			gEnglishTwo.setText(word.getString(word.getColumnIndex("English")));
			gCategory.setText(WordSwapHelper.cateCodeToString
					(ctx, word.getString(word.getColumnIndex("Category"))));
			/*
			sCategory = word.getString(word.getColumnIndex("Category"));
			if (sCategory.matches("Action"))
			{
				gCategory.setText(ctx.getString(R.string.arrayAction));
			}
			else if (sCategory.matches("Animal"))
			{
				gCategory.setText(ctx.getString(R.string.arrayAnimal));
			}
			else if (sCategory.matches("Define"))
			{
				gCategory.setText(ctx.getString(R.string.arrayDefine));
			}
			else if (sCategory.matches("Expression"))
			{
				gCategory.setText(ctx.getString(R.string.arrayExpress));
			}
			else if (sCategory.matches("Food"))
			{
				gCategory.setText(ctx.getString(R.string.arrayFood));
			}
			else if (sCategory.matches("Language"))
			{
				gCategory.setText(ctx.getString(R.string.arrayTalk));
			}
			else if (sCategory.matches("Numeral"))
			{
				gCategory.setText(ctx.getString(R.string.arrayNum));
			}
			else if (sCategory.matches("Object"))
			{
				gCategory.setText(ctx.getString(R.string.arrayObject));
			}
			else if (sCategory.matches("Person"))
			{
				gCategory.setText(ctx.getString(R.string.arrayPeople));
			}
			else if (sCategory.matches("Place"))
			{
				gCategory.setText(ctx.getString(R.string.arrayPlace));
			}
			else //Time
			{
				gCategory.setText(ctx.getString(R.string.arrayTime));
			}
			*/
			gHint.setText(WordSwapHelper.noteCodeToString(ctx, word.getString(word.getColumnIndex("Note"))));
			/*
			sHint = word.getString(word.getColumnIndex("Note"));
			if (sHint.matches("Masculine"))
			{
				gHint.setText(ctx.getString(R.string.hintM));
			}
			else if (sHint.matches("Feminine"))
			{
				gHint.setText(ctx.getString(R.string.hintF));
			}
			else //no hint = 0
			{
				gHint.setText(sHint);
			}
			*/
			gMark.setText(word.getString(word.getColumnIndex("Marked")));
		}
	}
}
