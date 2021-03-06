package com.haleysoft.spanish;

/**
 * Created by Haleysoftware on 5/23/13.
 * Cleaned by Mike Haley on 9/7/13.
 */

import android.app.ActionBar;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TabHost;

import com.haleysoft.spanish.WordList.TabManager.TabInfo;
import com.haleysoft.spanish.databases.WordDBFragment;
import com.haleysoft.spanish.dialogs.DialogsFragment;

import java.util.HashMap;

public class WordList extends FragmentActivity {
    //private static final String MASTER_SETTINGS = "haley_master_set";
    public static Boolean ttsErrorDialog = false;
    private static final String MODES = "Word List";
    private static String userName;
    private static TabHost theHost;
    private static String currentStringTab; //listall or listsearch
    private static TabInfo currentTab;
    private final FragmentManager theFManager = getSupportFragmentManager();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //SharedPreferences masterPref = getSharedPreferences(MASTER_SETTINGS, MODE_PRIVATE);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            userName = extras.getString("user");
        }
        SharedPreferences preferences = getSharedPreferences(userName, MODE_PRIVATE);
        boolean theme = preferences.getBoolean("theme_set", false);
        setTheme(theme ? R.style.ActivityThemeAlt : R.style.ActivityTheme);
        String orientationTest = preferences.getString("orie_list_set", "0");
        int orieTest = Integer.parseInt(orientationTest);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) { //For old OS
            switch (orieTest) {
                case 0:
                    this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                    break;
                case 1:
                    this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    break;
                case 2:
                    this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    break;
                default:
            }
        } else {
            switch (orieTest) {
                case 0:
                    this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                    break;
                case 1:
                    this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
                    break;
                case 2:
                    this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
                    break;
                default:
            }
        }
        if (savedInstanceState == null) {
            addFragments();
            addTTS();
        }

        setContentView(R.layout.wordlisthost);
        theHost = (TabHost) findViewById(android.R.id.tabhost);
        theHost.setup();

        TabManager theManager = new TabManager(this, theHost, android.R.id.tabcontent);

        Bundle arg = new Bundle();
        arg.putString("user", userName);

        theManager.addTab(theHost.newTabSpec("listall").setIndicator(getText(R.string.AllWordTab)), WordListFragment.class, arg);
        theManager.addTab(theHost.newTabSpec("listsearch").setIndicator(getText(R.string.SearchWordTab)), WordSearchFragment.class, arg);

        //Change the height of the tabs
        /*
		theHost.getTabWidget().getChildAt(0).getLayoutParams().height = 50;
		theHost.getTabWidget().getChildAt(1).getLayoutParams().height = 50;
		*/

        if (savedInstanceState != null) {
            theHost.setCurrentTabByTag(savedInstanceState.getString("tab"));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            ActionBar actionBar = getActionBar();
            if (actionBar != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) { //For ICS and up
                    actionBar.setHomeButtonEnabled(true);
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) { //For ICS and up
                    actionBar.setDisplayHomeAsUpEnabled(true);
                }
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("tab", theHost.getCurrentTabTag());
    }

    @Override
    public void onPostResume() {
        super.onPostResume();
        if (ttsErrorDialog) {
            startDialog(5, null, 2); //The 2 in place 4 is null
            ttsErrorDialog = false;
        }
    }

    private void addFragments() {
        FragmentTransaction theTransaction = theFManager.beginTransaction();

        WordDBFragment worddb = new WordDBFragment();
        theTransaction.add(worddb, "wordFragment");

        theTransaction.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.studymenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: //This is the up button
                finish();
                return true;
            case R.id.menukey:
                if (currentStringTab.contains("listall")) {
                    ((WordListFragment) currentTab.fragment).toggleKey();
                } else if (currentStringTab.contains("listsearch")) {
                    ((WordSearchFragment) currentTab.fragment).toggleKey();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void addTTS() {
        SharedPreferences preferences = getSharedPreferences(userName, 0);
        boolean ttsTest = preferences.getBoolean("tts_set", true);
        if (ttsTest) {
            FragmentTransaction theTransaction = theFManager.beginTransaction();

            TTSFragment ttsset = new TTSFragment();
            Bundle arg = new Bundle();
            arg.putString("mode", MODES);
            ttsset.setArguments(arg);
            theTransaction.add(ttsset, "ttsFragment");

            theTransaction.commit();
        }
    }

    public void startDialog(int mode, String hint, int mark) {
        FragmentTransaction theTransaction = theFManager.beginTransaction();
        Fragment dialogset = theFManager.findFragmentByTag("dialogFragment");
        if (dialogset != null) {
            theTransaction.remove(dialogset);
        }
        DialogFragment newDialog = DialogsFragment.newInstance(hint, mode, null, mark, userName);
        newDialog.show(theTransaction, "dialogFragment");
    }

    //Tab helper class
    public static class TabManager implements TabHost.OnTabChangeListener {
        private final FragmentActivity mActivity;
        private final TabHost mTabHost;
        private final int mContainerId;
        private final HashMap<String, TabInfo> mTabs = new HashMap<String, TabInfo>();
        TabInfo mLastTab;

        static final class TabInfo {
            private final String tag;
            private final Class<?> clss;
            private final Bundle args;
            private Fragment fragment;

            TabInfo(String _tag, Class<?> _class, Bundle _args) {
                tag = _tag;
                clss = _class;
                args = _args;
            }
        }

        static class DummyTabFactory implements TabHost.TabContentFactory {
            private final Context mContext;

            public DummyTabFactory(Context context) {
                mContext = context;
            }

            @Override
            public View createTabContent(String tag) {
                View v = new View(mContext);
                v.setMinimumWidth(0);
                v.setMinimumHeight(0);
                return v;
            }
        }

        public TabManager(FragmentActivity activity, TabHost tabHost, int containerId) {
            mActivity = activity;
            mTabHost = tabHost;
            mContainerId = containerId;
            mTabHost.setOnTabChangedListener(this);
        }

        public void addTab(TabHost.TabSpec tabSpec, Class<?> clss, Bundle args) {
            tabSpec.setContent(new DummyTabFactory(mActivity));
            String tag = tabSpec.getTag();

            TabInfo info = new TabInfo(tag, clss, args);

            // Check to see if we already have a fragment for this tab, probably
            // from a previously saved state.  If so, deactivate it, because our
            // initial state is that a tab isn't shown.
            info.fragment = mActivity.getSupportFragmentManager().findFragmentByTag(tag);
            if (info.fragment != null && !info.fragment.isDetached()) {
                FragmentTransaction ft = mActivity.getSupportFragmentManager().beginTransaction();
                ft.detach(info.fragment);
                ft.commit();
            }

            mTabs.put(tag, info);
            mTabHost.addTab(tabSpec);
        }

        @Override
        public void onTabChanged(String tabId) {
            WordList.currentStringTab = tabId;
            WordList.currentTab = mTabs.get(tabId);
            //This hides the soft keyboard on tab change
            InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(WordList.theHost.getApplicationWindowToken(), 0);
            TabInfo newTab = mTabs.get(tabId);
            if (mLastTab != newTab) {
                FragmentTransaction ft = mActivity.getSupportFragmentManager().beginTransaction();
                if (mLastTab != null) {
                    if (mLastTab.fragment != null) {
                        ft.detach(mLastTab.fragment);
                    }
                }
                if (newTab != null) {
                    if (newTab.fragment == null) {
                        newTab.fragment = Fragment.instantiate(mActivity, newTab.clss.getName(), newTab.args);
                        ft.add(mContainerId, newTab.fragment, newTab.tag);
                    } else {
                        ft.attach(newTab.fragment);
                    }
                }
                mLastTab = newTab;
                ft.commit();
                mActivity.getSupportFragmentManager().executePendingTransactions();
            }
        }
    }
}
