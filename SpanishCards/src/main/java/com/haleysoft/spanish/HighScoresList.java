package com.haleysoft.spanish;

/**
 * Created by Haleysoftware on 5/23/13.
 */

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TabHost;

import com.google.ads.AdRequest;
import com.google.ads.AdView;
import com.haleysoft.spanish.HighScoresList.TabManager.TabInfo;

import java.util.HashMap;

//This is used for a Tab View that works with Android OS < 3.0
@SuppressLint("NewApi")
@TargetApi(14)
public class HighScoresList extends FragmentActivity
{
	private static final String MASTER_SETTINGS = "haley_master_set";
	private AdView adView;
	private static String userName = "Guest";
	private static TabHost theHost;
	private static String currentStringTab; //high - user - mode
	private static TabInfo currentTab;
	private final FragmentManager theFManager = getSupportFragmentManager();

	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		SharedPreferences masterPref = getSharedPreferences(MASTER_SETTINGS, MODE_PRIVATE);
		Bundle extras = getIntent().getExtras();
		if (extras != null)
		{
			userName = extras.getString("user");
		}
		SharedPreferences preferences = getSharedPreferences(userName, MODE_PRIVATE);
		boolean theme = preferences.getBoolean("theme_set", false);
		if (theme)
		{
			setTheme(R.style.ActivityThemeAlt);
		}
		else
		{
			setTheme(R.style.ActivityTheme);
		}
		String orientationTest = preferences.getString("orie_list_set", "0");
		int orieTest = Integer.parseInt(orientationTest);
		if (Build.VERSION.SDK_INT<Build.VERSION_CODES.GINGERBREAD) //For old OS
		{
			switch (orieTest)
			{
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
		}
		else
		{
			switch (orieTest)
			{
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
		if (savedInstanceState == null)
		{
			addFragments();
		}
		setContentView(R.layout.scoretabs);
		theHost = (TabHost)findViewById(android.R.id.tabhost);
		theHost.setup();

		TabManager theManager = new TabManager (this, theHost, android.R.id.tabcontent);

		Bundle arg = new Bundle();
		arg.putString("user", userName);

		theManager.addTab(theHost.newTabSpec("high").setIndicator(getText(R.string.HighTabText)), HighScoreFragment.class, arg);
		theManager.addTab(theHost.newTabSpec("user").setIndicator(getText(R.string.UserTabText)), HighUserFragment.class, arg);
		theManager.addTab(theHost.newTabSpec("mode").setIndicator(getText(R.string.ModeTabText)), HighModeFragment.class, arg);

		theHost.getTabWidget().getChildAt(0).getLayoutParams().height = 50;
		theHost.getTabWidget().getChildAt(1).getLayoutParams().height = 50;
		theHost.getTabWidget().getChildAt(2).getLayoutParams().height = 50;

		if (savedInstanceState != null)
		{
			theHost.setCurrentTabByTag(savedInstanceState.getString("tab"));
		}
		if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.ICE_CREAM_SANDWICH) //For ICS and up
		{
			ActionBar actionBar = getActionBar();
			actionBar.setHomeButtonEnabled(true);
		}
		if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB) //For ICS and up
		{
			ActionBar actionBar = getActionBar();
			actionBar.setDisplayHomeAsUpEnabled(true);
		}
		addAds(masterPref.getBoolean("remove_ads", false));
	}

	@Override
	public void onDestroy() {
		if (adView != null) {
			adView.removeAllViews();
			adView.destroy();
		}
		super.onDestroy();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		outState.putString("tab", theHost.getCurrentTabTag());
	}

	private void addAds(boolean paid) {
		adView = (AdView) this.findViewById(R.id.adView);
		if (paid) {
			adView.setVisibility(View.INVISIBLE);
			if (adView != null) {
				adView.removeAllViews();
				adView.destroy();
				adView = null;
			}
		}
		else {
			adView.setVisibility(View.VISIBLE);
			AdRequest adRequest = new AdRequest();
			//This code is for testing only
			adRequest.addTestDevice(AdRequest.TEST_EMULATOR);
			adRequest.addTestDevice("2233DFE5B204F720C5A258A482ECAB8E"); //GS2
			adRequest.addTestDevice("79B71208D02B63421ADC58ACF3A19CEE"); //LG G
			//adRequest.addTestDevice("015d0787bd3c0215"); //ASUS Prime
			//End of testing code
			adView.loadAd(adRequest);
			adView.setAdListener(new AdListen());
		}
	}

	private void addFragments()
	{
		FragmentTransaction theTransaction = theFManager.beginTransaction();

		UserDBFragment userdb = new UserDBFragment();
		theTransaction.add(userdb, "userFragment");

		theTransaction.commit();
	}

	@Override
	public boolean onCreateOptionsMenu (Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.scoremenu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected (MenuItem item)
	{
		switch (item.getItemId())
		{
			case android.R.id.home: //This is the up button
			/*
			Intent intent = new Intent(this, TestSelect.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			*/
				finish();
				return true;
			case R.id.menukey:
				if (currentStringTab.contains("mode"))
				{
					((HighModeFragment) currentTab.fragment).toggleKey();
				}
				else if (currentStringTab.contains("high"))
				{
					((HighScoreFragment) currentTab.fragment).toggleKey();
				}
				else if (currentStringTab.contains("user"))
				{
					((HighUserFragment) currentTab.fragment).toggleKey();
				}
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	//Tab helper class
	public static class TabManager implements TabHost.OnTabChangeListener
	{
		private final FragmentActivity mActivity;
		private final TabHost mTabHost;
		private final int mContainerId;
		private final HashMap<String, TabInfo> mTabs = new HashMap<String, TabInfo>();
		TabInfo mLastTab;

		static final class TabInfo
		{
			private final String tag;
			private final Class<?> clss;
			private final Bundle args;
			private Fragment fragment;

			TabInfo(String _tag, Class<?> _class, Bundle _args)
			{
				tag = _tag;
				clss = _class;
				args = _args;
			}
		}

		static class DummyTabFactory implements TabHost.TabContentFactory
		{
			private final Context mContext;

			public DummyTabFactory(Context context)
			{
				mContext = context;
			}

			@Override
			public View createTabContent(String tag)
			{
				View v = new View(mContext);
				v.setMinimumWidth(0);
				v.setMinimumHeight(0);
				return v;
			}
		}

		public TabManager(FragmentActivity activity, TabHost tabHost, int containerId)
		{
			mActivity = activity;
			mTabHost = tabHost;
			mContainerId = containerId;
			mTabHost.setOnTabChangedListener(this);
		}

		public void addTab(TabHost.TabSpec tabSpec, Class<?> clss, Bundle args)
		{
			tabSpec.setContent(new DummyTabFactory(mActivity));
			String tag = tabSpec.getTag();

			TabInfo info = new TabInfo(tag, clss, args);

			// Check to see if we already have a fragment for this tab, probably
			// from a previously saved state.  If so, deactivate it, because our
			// initial state is that a tab isn't shown.
			info.fragment = mActivity.getSupportFragmentManager().findFragmentByTag(tag);
			if (info.fragment != null && !info.fragment.isDetached())
			{
				FragmentTransaction ft = mActivity.getSupportFragmentManager().beginTransaction();
				ft.detach(info.fragment);
				ft.commit();
			}

			mTabs.put(tag, info);
			mTabHost.addTab(tabSpec);
		}

		@Override
		public void onTabChanged(String tabId)
		{
			HighScoresList.currentStringTab = tabId;
			HighScoresList.currentTab = mTabs.get(tabId);
			TabInfo newTab = mTabs.get(tabId);
			if (mLastTab != newTab)
			{
				FragmentTransaction ft = mActivity.getSupportFragmentManager().beginTransaction();
				if (mLastTab != null)
				{
					if (mLastTab.fragment != null)
					{
						ft.detach(mLastTab.fragment);
					}
				}
				if (newTab != null)
				{
					if (newTab.fragment == null)
					{
						newTab.fragment = Fragment.instantiate(mActivity, newTab.clss.getName(), newTab.args);
						ft.add(mContainerId, newTab.fragment, newTab.tag);
					}
					else
					{
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
