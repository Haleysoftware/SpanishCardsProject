package com.haleysoft.spanish;

/**
 * Created by Haleysoftware on 5/23/13.
 * Cleaned by Mike Haley on 8/25/13.
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
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.haleysoft.spanish.databases.UserDBCP;

public class HighModeFragment extends ListFragment implements OnItemSelectedListener, LoaderManager.LoaderCallbacks<Cursor> {
    private String userName = "Guest";
    private static boolean safeLoad = false;
    private static String theMode;
    private static ScoreAdapter cAdapter;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        theMode = getString(R.string.ModeSetup);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.modetab, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        userName = getArguments().getString("user");
        spinSetup();
        cAdapter = new ScoreAdapter(getActivity(), null);
        setListAdapter(cAdapter);
        getLoaderManager().initLoader(0, null, this);
        safeLoad = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        changeView();
    }

    private void spinSetup() {
        Spinner spinner = (Spinner) getActivity().findViewById(R.id.spinnermode);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(), R.array.ModeArray, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
    }

    //This is for when the spinner is selected.
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        Spinner spinner = (Spinner) getActivity().findViewById(R.id.spinnermode);
        Object selected = spinner.getSelectedItem();
        if (selected != null) {
            theMode = selected.toString();
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
        boolean keyShow = pref.getBoolean("score_key_set", true);
        pref.edit().putBoolean("score_key_set", !keyShow).commit();
        changeView();
    }

    public void changeView() {
        SharedPreferences pref = getActivity().getSharedPreferences(userName, Context.MODE_PRIVATE);
        boolean keyShow = pref.getBoolean("score_key_set", true);
        TextView scoreKey = (TextView) getActivity().findViewById(R.id.modeScoreTag);
        TextView nameKey = (TextView) getActivity().findViewById(R.id.modeNameTag);
        TextView dateKey = (TextView) getActivity().findViewById(R.id.modeDateTag);
        if (keyShow) {
            scoreKey.setVisibility(View.VISIBLE);
            nameKey.setVisibility(View.VISIBLE);
            dateKey.setVisibility(View.VISIBLE);
        } else {
            scoreKey.setVisibility(View.GONE);
            nameKey.setVisibility(View.GONE);
            dateKey.setVisibility(View.GONE);
        }
        scoreKey.invalidate();
        nameKey.invalidate();
        dateKey.invalidate();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle pack) {
        Uri scoreUri = UserDBCP.CONTENT_URI;
        String[] getColumns = {UserDBCP.KEY_ROWB, UserDBCP.KEY_NAME, UserDBCP.KEY_DATE, UserDBCP.KEY_SCORE};
        String where = UserDBCP.KEY_MODE + " LIKE ?";
        String[] key = {theMode};
        String sort = UserDBCP.KEY_SCORE + " DESC";
        return new CursorLoader(getActivity(), scoreUri, getColumns, where, key, sort);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        cAdapter.swapCursor(data);
    }

    @Override
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
            View row = inflater.inflate(R.layout.modebar, group, false);
            if (row != null) {
                RowHolder holder = new RowHolder(row);
                row.setTag(holder);
            }
            return row;
        }
    }


    static class RowHolder {
        private TextView gName = null;
        private TextView gScore = null;
        private TextView gDate = null;

        RowHolder(View row) {
            gName = (TextView) row.findViewById(R.id.modeNameText);
            gScore = (TextView) row.findViewById(R.id.modeScoreText);
            gDate = (TextView) row.findViewById(R.id.modeDateText);
        }

        void populateFrom(Cursor score) {
            gName.setText(score.getString(score.getColumnIndex("name")));
            gDate.setText(score.getString(score.getColumnIndex("date")));
            gScore.setText(String.valueOf(score.getInt(score.getColumnIndex("score"))));
        }
    }
}
