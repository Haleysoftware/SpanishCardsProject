package com.haleysoft.spanish;

/**
 * Created by Haleysoftware on 5/23/13.
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
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
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;

public class HighUserFragment extends ListFragment implements OnItemSelectedListener, LoaderManager.LoaderCallbacks<Cursor>
{
	private String userName = "Guest";
	private static final String MASTER_SETTINGS = "haley_master_set";
	private static boolean safeLoad = false;
	private static String theUser;
	private static ArrayAdapter<CharSequence> adapter = null;
	private static ScoreAdapter cAdapter;
	private static Cursor scores;

	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
		theUser = getString(R.string.gName);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.nametab, container, false);
	}

	@Override
	public void onActivityCreated (Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		userName = getArguments().getString("user");
		spinsetup();
		cAdapter = new ScoreAdapter(getActivity(), scores);
		setListAdapter(cAdapter);
		getLoaderManager().initLoader(0, null, this);
		safeLoad = true;
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

	private void spinsetup()
	{
		int last;
		UserDBFragment names = (UserDBFragment) getFragmentManager().findFragmentByTag("userFragment");
		//Sets the data and function for the Spinner.
		Spinner spinner = (Spinner) getActivity().findViewById(R.id.spinnername);
		adapter = new ArrayAdapter<CharSequence>(getActivity(), android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		adapter.add(getString(R.string.gName));
		Cursor spinNames = names.getUsers();
		while (spinNames.moveToNext())
		{
			String user = spinNames.getString(spinNames.getColumnIndex("user"));
			adapter.add(user);
		}
		spinNames.close();
		names.close();
		if (adapter.getCount() > 1)
		{
			//getActivity();
			SharedPreferences masterPref = getActivity().getSharedPreferences(MASTER_SETTINGS, 0);
			String userName = masterPref.getString("last_user_set", "Guest");
			if (userName.matches("Guest"))
			{
				last = 0;
			}
			else
			{
				last = adapter.getPosition(userName);
			}
		}
		else
		{
			last = 0;
		}
		spinner.setAdapter(adapter);
		spinner.setSelection(last);
		spinner.setOnItemSelectedListener(this);
	}

	//This is for when the spinner is selected.
	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
	{
		Spinner spinner = (Spinner) getActivity().findViewById(R.id.spinnername);
		theUser = spinner.getSelectedItem().toString();
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
		boolean keyShow = pref.getBoolean("score_key_set", true);
		boolean newKey;
		if (keyShow)
		{
			newKey = false;
		}
		else
		{
			newKey = true;
		}
		pref.edit().putBoolean("score_key_set", newKey).commit();
		changeView();
	}

	public void changeView()
	{
		SharedPreferences pref = getActivity().getSharedPreferences(userName, Context.MODE_PRIVATE);
		boolean keyShow = pref.getBoolean("score_key_set", true);
		TextView nameKey = (TextView) getActivity().findViewById(R.id.nameScoreTag);
		TextView modeKey = (TextView) getActivity().findViewById(R.id.nameModeTag);
		TextView dateKey = (TextView) getActivity().findViewById(R.id.nameDateTag);
		if (keyShow)
		{
			nameKey.setVisibility(View.VISIBLE);
			modeKey.setVisibility(View.VISIBLE);
			dateKey.setVisibility(View.VISIBLE);
		}
		else
		{
			nameKey.setVisibility(View.GONE);
			modeKey.setVisibility(View.GONE);
			dateKey.setVisibility(View.GONE);
		}
		nameKey.invalidate();
		modeKey.invalidate();
		dateKey.invalidate();
	}

	@Override //LoadManager
	public Loader<Cursor> onCreateLoader(int id, Bundle pack)
	{
		Uri scoreUri = UserDBCP.CONTENT_URI;
		String[] getColumns = {UserDBCP.KEY_ROWB, UserDBCP.KEY_DATE, UserDBCP.KEY_SCORE, UserDBCP.KEY_MODE};
		String where = UserDBCP.KEY_NAME + " LIKE ?";
		String[] key = {theUser};
		String sort = UserDBCP.KEY_SCORE + " DESC";
		CursorLoader wordCursor = new CursorLoader(getActivity(), scoreUri, getColumns, where, key, sort);
		return wordCursor;
	}

	@Override //LoadManager
	public void onLoadFinished(Loader<Cursor> loader, Cursor data)
	{
		cAdapter.swapCursor(data);
	}

	@Override //LoadManager
	public void onLoaderReset(Loader<Cursor> cursor)
	{
		cAdapter.swapCursor(null);
	}

	class ScoreAdapter extends CursorAdapter
	{
		ScoreAdapter (Context ctx, Cursor theScore)
		{
			super(getActivity(), theScore,  FLAG_REGISTER_CONTENT_OBSERVER);
		}

		@Override
		public void bindView(View row, Context ctx, Cursor score)
		{
			changeView();
			RowHolder holder = (RowHolder) row.getTag();
			holder.populateFrom(score);
		}

		@Override
		public View newView(Context ctx, Cursor score, ViewGroup group)
		{
			changeView();
			LayoutInflater inflater = getActivity().getLayoutInflater();
			View row = inflater.inflate(R.layout.namebar, group, false);
			RowHolder holder = new RowHolder(row);
			row.setTag(holder);
			return row;
		}
	}

	static class RowHolder
	{
		private TextView gMode = null;
		private TextView gScore = null;
		private TextView gDate = null;

		RowHolder(View row)
		{
			gMode = (TextView) row.findViewById(R.id.nameModeText);
			gScore = (TextView) row.findViewById(R.id.nameScoreText);
			gDate = (TextView) row.findViewById(R.id.nameDateText);
		}

		void populateFrom(Cursor score)
		{
			gDate.setText(score.getString(score.getColumnIndex("date")));
			gScore.setText(String.valueOf(score.getInt(score.getColumnIndex("score"))));
			gMode.setText(score.getString(score.getColumnIndex("mode")));
		}
	}
}
