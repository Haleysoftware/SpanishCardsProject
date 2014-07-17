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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.haleysoft.spanish.databases.UserDBCP;
import com.haleysoft.spanish.databases.UserDBFragment;

public class HighUserFragment extends ListFragment implements OnItemSelectedListener, LoaderManager.LoaderCallbacks<Cursor> {
    private String userName = "Guest";
    private static final String MASTER_SETTINGS = "haley_master_set";
    private static boolean safeLoad = false;
    private static String theUser;
    private static ScoreAdapter cAdapter;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        theUser = getString(R.string.gName);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.nametab, container, false);
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
        int last;
        UserDBFragment names = (UserDBFragment) getFragmentManager().findFragmentByTag("userFragment");
        //Sets the data and function for the Spinner.
        Spinner spinner = (Spinner) getActivity().findViewById(R.id.spinnername);
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(getActivity(), android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapter.add(getString(R.string.gName));
        Cursor spinNames = names.getUsers();
        while (spinNames.moveToNext()) {
            String user = spinNames.getString(spinNames.getColumnIndex("user"));
            adapter.add(user);
        }
        spinNames.close();
        names.close();
        if (adapter.getCount() > 1) {
            //getActivity();
            SharedPreferences masterPref = getActivity().getSharedPreferences(MASTER_SETTINGS, 0);
            String userName = masterPref.getString("last_user_set", "Guest");
            if (userName.matches("Guest")) {
                last = 0;
            } else {
                last = adapter.getPosition(userName);
            }
        } else {
            last = 0;
        }
        spinner.setAdapter(adapter);
        spinner.setSelection(last);
        spinner.setOnItemSelectedListener(this);
    }

    //This is for when the spinner is selected.
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        Spinner spinner = (Spinner) getActivity().findViewById(R.id.spinnername);
        Object spinWord = spinner.getSelectedItem();
        if (spinWord != null) {
            theUser = spinWord.toString();
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
        TextView nameKey = (TextView) getActivity().findViewById(R.id.nameScoreTag);
        TextView modeKey = (TextView) getActivity().findViewById(R.id.nameModeTag);
        TextView dateKey = (TextView) getActivity().findViewById(R.id.nameDateTag);
        nameKey.setVisibility(keyShow ? View.VISIBLE : View.GONE);
        modeKey.setVisibility(keyShow ? View.VISIBLE : View.GONE);
        dateKey.setVisibility(keyShow ? View.VISIBLE : View.GONE);
        nameKey.invalidate();
        modeKey.invalidate();
        dateKey.invalidate();
    }

    @Override //LoadManager
    public Loader<Cursor> onCreateLoader(int id, Bundle pack) {
        Uri scoreUri = UserDBCP.CONTENT_URI;
        String[] getColumns = {UserDBCP.KEY_ROWB, UserDBCP.KEY_DATE, UserDBCP.KEY_SCORE, UserDBCP.KEY_MODE};
        String where = UserDBCP.KEY_NAME + " LIKE ?";
        String[] key = {theUser};
        String sort = UserDBCP.KEY_SCORE + " DESC";
        return new CursorLoader(getActivity(), scoreUri, getColumns, where, key, sort);
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
            View row = inflater.inflate(R.layout.namebar, group, false);
            if (row != null) {
                RowHolder holder = new RowHolder(row);
                row.setTag(holder);
            }
            return row;
        }
    }

    static class RowHolder {
        private TextView gMode = null;
        private TextView gScore = null;
        private TextView gDate = null;

        RowHolder(View row) {
            gMode = (TextView) row.findViewById(R.id.nameModeText);
            gScore = (TextView) row.findViewById(R.id.nameScoreText);
            gDate = (TextView) row.findViewById(R.id.nameDateText);
        }

        void populateFrom(Cursor score) {
            gDate.setText(score.getString(score.getColumnIndex("date")));
            gScore.setText(String.valueOf(score.getInt(score.getColumnIndex("score"))));
            gMode.setText(score.getString(score.getColumnIndex("mode")));
        }
    }
}
