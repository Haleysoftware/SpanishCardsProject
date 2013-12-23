package com.haleysoft.spanish;

/**
 * Created by Haleysoftware on 5/23/13.
 * Cleaned by Mike Haley on 8/26/13.
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
import android.widget.TextView;

public class HighScoreFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
	private String userName = "Guest";
	private static ScoreAdapter cAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.hightab, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		userName = getArguments().getString("user");
		cAdapter = new ScoreAdapter(getActivity(), null);
		setListAdapter(cAdapter);
		getLoaderManager().initLoader(0, null, this);
	}

	@Override
	public void onResume() {
		super.onResume();
		changeView();
	}

	public void toggleKey() {
		SharedPreferences pref = getActivity().getSharedPreferences(userName, Context.MODE_PRIVATE);
		boolean keyShow = pref.getBoolean("score_key_set", true);
		pref.edit().putBoolean("score_key_set", !keyShow).commit();
		changeView();
	}

	public void changeView() {
		SharedPreferences pref = getActivity().getSharedPreferences(userName, Context.MODE_PRIVATE);
		boolean keyShow = pref.getBoolean("score_key_set", true);
		TextView scoreKey = (TextView) getActivity().findViewById(R.id.highScoreTag);
		TextView userKey = (TextView) getActivity().findViewById(R.id.highUserTag);
		TextView modeKey = (TextView) getActivity().findViewById(R.id.highModeTag);
		TextView dateKey = (TextView) getActivity().findViewById(R.id.highDateTag);
		if (keyShow) {
			scoreKey.setVisibility(View.VISIBLE);
			userKey.setVisibility(View.VISIBLE);
			modeKey.setVisibility(View.VISIBLE);
			dateKey.setVisibility(View.VISIBLE);
		} else {
			scoreKey.setVisibility(View.GONE);
			userKey.setVisibility(View.GONE);
			modeKey.setVisibility(View.GONE);
			dateKey.setVisibility(View.GONE);
		}
		scoreKey.invalidate();
		userKey.invalidate();
		modeKey.invalidate();
		dateKey.invalidate();
	}

	@Override //LoadManager
	public Loader<Cursor> onCreateLoader(int id, Bundle pack) {
		Uri scoreUri = UserDBCP.CONTENT_URI;
		String[] getColumns = {UserDBCP.KEY_ROWB, UserDBCP.KEY_NAME, UserDBCP.KEY_DATE, UserDBCP.KEY_SCORE, UserDBCP.KEY_MODE};
		//String where = null;
		//String[] key = null;
		String sort = UserDBCP.KEY_SCORE + " DESC";
		return new CursorLoader(getActivity(), scoreUri, getColumns, null, null, sort);
	}

	@Override //LoadManager
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		cAdapter.swapCursor(data);
	}

	@Override //LoadManager
	public void onLoaderReset(Loader<Cursor> cursor) {
		cAdapter.swapCursor(null);
	}

	class ScoreAdapter extends CursorAdapter {
		ScoreAdapter(Context ctx, Cursor theScore) {
			super(ctx, theScore, FLAG_REGISTER_CONTENT_OBSERVER);
		}

		@Override
		public void bindView(View row, Context ctx, Cursor score) {
			changeView();
			RowHolder holder = (RowHolder) row.getTag();
			holder.populateFrom(score);
		}

		@Override
		public View newView(Context ctx, Cursor score, ViewGroup group) {
			changeView();
			LayoutInflater inflater = getActivity().getLayoutInflater();
			View row = inflater.inflate(R.layout.highbar, group, false);
			if (row != null) {
				RowHolder holder = new RowHolder(row);
				row.setTag(holder);
			}
			return row;
		}
	}

	static class RowHolder {
		private TextView gName = null;
		private TextView gMode = null;
		private TextView gScore = null;
		private TextView gDate = null;

		RowHolder(View row) {
			gName = (TextView) row.findViewById(R.id.highUserText);
			gMode = (TextView) row.findViewById(R.id.highModeText);
			gScore = (TextView) row.findViewById(R.id.highScoreText);
			gDate = (TextView) row.findViewById(R.id.highDateText);
		}

		void populateFrom(Cursor score) {
			gName.setText(score.getString(score.getColumnIndex("name")));
			gDate.setText(score.getString(score.getColumnIndex("date")));
			gScore.setText(String.valueOf(score.getInt(score.getColumnIndex("score"))));
			gMode.setText(score.getString(score.getColumnIndex("mode")));
		}
	}
}
