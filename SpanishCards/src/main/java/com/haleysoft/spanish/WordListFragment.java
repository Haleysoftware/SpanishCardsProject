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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;

public class WordListFragment extends ListFragment implements OnItemSelectedListener, LoaderManager.LoaderCallbacks<Cursor>
{
	private static final int demoLevel = 30;
	//private static final int maxLevel = 150;
	private String userName = "Guest";
	private static boolean safeLoad = false;
	private ArrayAdapter<CharSequence> adapterOne = null;
	private static String theOrder = "English Ascending";
	private ArrayAdapter<CharSequence> adapterTwo = null;
	private static String theFilter = "All";
	private static WordDBFragment wordDb;
	private static WordAdapter cAdapter;
	private static Cursor words;
	private static long gotID;
	private static LoaderManager loader;
	private static WordListFragment markReset;

	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
		wordDb = (WordDBFragment) getFragmentManager().findFragmentByTag("wordFragment"); //This is for the mark change
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.wordlisttab, container, false);
	}

	@Override
	public void onActivityCreated (Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		userName = getArguments().getString("user");
		spinSetupOne();
		spinSetupTwo();
		cAdapter = new WordAdapter(getActivity(), words);
		setListAdapter(cAdapter);
		getLoaderManager().initLoader(0, null, this);
		loader = getLoaderManager();
		markReset = this;
		safeLoad = true;
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
						Spinner spinOrder = (Spinner) getActivity().findViewById(R.id.spinnerWordsSort);
						if (spinOrder.getSelectedItem().toString().matches(getString(R.string.sortArrayEAsc)) || spinOrder.getSelectedItem().toString().matches(getString(R.string.sortArrayEDes)))
						{
							ttsrun.sayWord("English", engText);
							ttsrun.sayWord("Spanish", spnText);
						}
						else if (spinOrder.getSelectedItem().toString().matches(getString(R.string.sortArraySAsc)) || spinOrder.getSelectedItem().toString().matches(getString(R.string.sortArraySDes)))
						{
							ttsrun.sayWord("Spanish", spnText);
							ttsrun.sayWord("English", engText);
						}
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
					((WordList)getActivity()).startDialog(4, showHint, markText);
				}
				else //For new OS
				{
					gotID = id;
					((WordList)getActivity()).startDialog(4, showHint, markText);
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

	private void spinSetupOne()
	{
		Spinner spinner = (Spinner) getActivity().findViewById(R.id.spinnerWordsSort);
		adapterOne = ArrayAdapter.createFromResource(getActivity(), R.array.SortArray, android.R.layout.simple_spinner_item);
		this.adapterOne.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapterOne);
		spinner.setOnItemSelectedListener(this);
	}

	private void spinSetupTwo()
	{
		Spinner spinner = (Spinner) getActivity().findViewById(R.id.spinnerWordsList);
		adapterTwo = ArrayAdapter.createFromResource(getActivity(), R.array.Levelarray, android.R.layout.simple_spinner_item);
		this.adapterTwo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapterTwo);
		spinner.setOnItemSelectedListener(this);
	}

	@Override //This is for when the spinner is selected.
	public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
	{
		Spinner spinSelect = (Spinner) getActivity().findViewById(R.id.spinnerWordsList);
		Spinner spinOrder = (Spinner) getActivity().findViewById(R.id.spinnerWordsSort);
		theOrder = spinOrder.getSelectedItem().toString();
		theFilter = spinSelect.getSelectedItem().toString();
		if (safeLoad)
		{
			getLoaderManager().restartLoader(0, null, this);
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent)
	{
		//Do nothing
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
		String[] getColumns = {WordDBCP.KEY_ROWID, WordDBCP.KEY_MARK, WordDBCP.KEY_NOTE, WordDBCP.KEY_CAT, WordDBCP.KEY_ENG, WordDBCP.KEY_SPAN};
		String where = null;
		String[] key = null;
		String sort = null;
		if (theFilter.matches(getString(R.string.arrayAll)))
		{
			if (checkPaid())
			{
				where = null;
				key = null;
			}
			else
			{
				where = WordDBCP.KEY_LEVEL + " <= ?";
				key = new String[] {String.valueOf(demoLevel)};
			}
		}
		else if (theFilter.matches(getString(R.string.arrayMarked)))
		{
			if (checkPaid())
			{
				where = WordDBCP.KEY_MARK + " LIKE ?";
				key = new String[] {"1"};
			}
			else
			{
				where = WordDBCP.KEY_MARK + " LIKE ? AND " + WordDBCP.KEY_LEVEL + " <= ?";
				key = new String[] {"1", String.valueOf(demoLevel)};
			}

		}
		else if (theFilter.matches(getString(R.string.arrayLevel)))
		{
			SharedPreferences preferences = getActivity().getSharedPreferences(userName, Context.MODE_PRIVATE);
			int userLevel = preferences.getInt("user_level", 1);
			where = WordDBCP.KEY_LEVEL + " <= ?";
			key = new String[] {String.valueOf(userLevel)};
		}
		else
		{
			String search = WordSwapHelper.cateStringToCode(getActivity(), theFilter);
			/*
			if (theFilter.matches(getString(R.string.arrayAction)))
			{
				search = "Action";
			}
			else if (theFilter.matches(getString(R.string.arrayAnimal)))
			{
				search = "Animal";
			}
			else if (theFilter.matches(getString(R.string.arrayDefine)))
			{
				search = "Define";
			}
			else if (theFilter.matches(getString(R.string.arrayExpress)))
			{
				search = "Expression";
			}
			else if (theFilter.matches(getString(R.string.arrayFood)))
			{
				search = "Food";
			}
			else if (theFilter.matches(getString(R.string.arrayTalk)))
			{
				search = "Language";
			}
			else if (theFilter.matches(getString(R.string.arrayNum)))
			{
				search = "Numeral";
			}
			else if (theFilter.matches(getString(R.string.arrayObject)))
			{
				search = "Object";
			}
			else if (theFilter.matches(getString(R.string.arrayPeople)))
			{
				search = "Person";
			}
			else if (theFilter.matches(getString(R.string.arrayPlace)))
			{
				search = "Place";
			}
			else //Time
			{
				search = "Time";
			}
			*/
			if (checkPaid())
			{
				where = WordDBCP.KEY_CAT + " LIKE ?";
				key = new String[] {search};
			}
			else
			{
				where = WordDBCP.KEY_CAT + " LIKE ? AND " + WordDBCP.KEY_LEVEL + " <= ?";
				key = new String[] {search, String.valueOf(demoLevel)};
			}
		}
		if (theOrder.matches(getString(R.string.sortArrayEAsc)))
		{
			sort = WordDBCP.KEY_ENG + " COLLATE NOCASE ASC";
		}
		else if (theOrder.matches(getString(R.string.sortArrayEDes)))
		{
			sort = WordDBCP.KEY_ENG + " COLLATE NOCASE DESC";
		}
		else if (theOrder.matches(getString(R.string.sortArraySAsc)))
		{
			sort = WordDBCP.KEY_SPAN + " COLLATE NOCASE ASC";
		}
		else if (theOrder.matches(getString(R.string.sortArraySDes)))
		{
			sort = WordDBCP.KEY_SPAN + " COLLATE NOCASE DESC";
		}
		else
		{
			sort = null;
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
	public void onLoaderReset(Loader<Cursor> cursor)
	{
		cAdapter.swapCursor(null);

	}

	public static void markChange (int newMark)
	{
		wordDb.updateMark(gotID, newMark);
		loader.restartLoader(0, null, markReset);
	}

	class WordAdapter extends CursorAdapter
	{
		WordAdapter (Context ctx, Cursor theWord)
		{
			super(getActivity(), theWord,  FLAG_REGISTER_CONTENT_OBSERVER);
		}

		@Override
		public void bindView(View row, Context ctx, Cursor word)
		{
			changeView();
			RowHolder holder = (RowHolder) row.getTag();
			holder.populateFrom(word, ctx);
			TextView englishText = (TextView) row.findViewById(R.id.englishBar);
			TextView spanishText = (TextView) row.findViewById(R.id.spanishBar);
			TextView spanishTwoText = (TextView) row.findViewById(R.id.spanishTwoBar);
			TextView englishTwoText = (TextView) row.findViewById(R.id.englishTwoBar);
			TextView rightKey = (TextView) getActivity().findViewById(R.id.rightKey);
			TextView leftKey = (TextView) getActivity().findViewById(R.id.leftKey);
			if (theOrder.matches(getString(R.string.sortArrayEAsc)) || theOrder.matches(getString(R.string.sortArrayEDes)))
			{
				englishText.setVisibility(View.VISIBLE);
				spanishText.setVisibility(View.VISIBLE);
				spanishTwoText.setVisibility(View.GONE);
				englishTwoText.setVisibility(View.GONE);
				leftKey.setText(R.string.tagEnglish);
				rightKey.setText(R.string.tagSpanish);
			}
			else if (theOrder.matches(getString(R.string.sortArraySAsc)) || theOrder.matches(getString(R.string.sortArraySDes)))
			{
				englishText.setVisibility(View.GONE);
				spanishText.setVisibility(View.GONE);
				spanishTwoText.setVisibility(View.VISIBLE);
				englishTwoText.setVisibility(View.VISIBLE);
				leftKey.setText(R.string.tagSpanish);
				rightKey.setText(R.string.tagEnglish);
			}

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
			TextView rightKey = (TextView) getActivity().findViewById(R.id.rightKey);
			TextView leftKey = (TextView) getActivity().findViewById(R.id.leftKey);
			if (theOrder.matches(getString(R.string.sortArrayEAsc)) || theOrder.matches(getString(R.string.sortArrayEDes)))
			{
				englishText.setVisibility(View.VISIBLE);
				spanishText.setVisibility(View.VISIBLE);
				spanishTwoText.setVisibility(View.GONE);
				englishTwoText.setVisibility(View.GONE);
				leftKey.setText(R.string.tagEnglish);
				rightKey.setText(R.string.tagSpanish);
			}
			else if (theOrder.matches(getString(R.string.sortArraySAsc)) || theOrder.matches(getString(R.string.sortArraySDes)))
			{
				englishText.setVisibility(View.GONE);
				spanishText.setVisibility(View.GONE);
				spanishTwoText.setVisibility(View.VISIBLE);
				englishTwoText.setVisibility(View.VISIBLE);
				leftKey.setText(R.string.tagSpanish);
				rightKey.setText(R.string.tagEnglish);
			}
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
			gCategory.setText(WordSwapHelper.cateCodeToString(ctx, word.getString(word.getColumnIndex("Category"))));
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
			else //no hint = 0 or something else
			{
				gHint.setText(sHint);
			}
			*/
			gMark.setText(word.getString(word.getColumnIndex("Marked")));
		}
	}
}
