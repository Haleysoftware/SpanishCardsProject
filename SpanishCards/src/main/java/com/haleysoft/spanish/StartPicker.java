package com.haleysoft.spanish;

/**
 * Created by Haleysoftware on 5/23/13.
 */

import android.app.Activity;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.content.Intent;
import android.content.SharedPreferences;

import com.android.vending.billing.util.IabHelper;
import com.android.vending.billing.util.IabResult;
import com.android.vending.billing.util.Inventory;
import com.android.vending.billing.util.Purchase;

public class StartPicker extends Activity
{
	private static final String MASTER_SETTINGS = "haley_master_set";
	private SharedPreferences masterPref;
	private IabHelper billHelper;
	private String userName = "Guest";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setupPref();
		//Check to go right to test mode
		masterPref = getSharedPreferences(MASTER_SETTINGS, MODE_PRIVATE);
		userName = masterPref.getString("last_user_set", "Guest");
		inAppCheck();
		SharedPreferences preferences = getSharedPreferences(userName, MODE_PRIVATE);
		boolean testing = preferences.getBoolean("return_test_set", false);
		if (testing)
		{
			goTest();
		}
		else
		{
			goSelect();
		}

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (billHelper != null) billHelper.dispose();
		billHelper = null;
	}

	private void setupPref()
	{
		PreferenceManager.setDefaultValues(this, MASTER_SETTINGS, MODE_PRIVATE, R.xml.programsettings, false);
		PreferenceManager.setDefaultValues(this, userName, MODE_PRIVATE, R.xml.mastersettings, false);
	}

	private void inAppCheck() {
		billHelper = new IabHelper(this, AppPurchasing.LICENSE_KEY);
		billHelper.enableDebugLogging(true);
		billHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
			public void onIabSetupFinished(IabResult result) {
				if (!result.isSuccess()) {
					//Problem with billing
					masterPref.edit().putBoolean("buy_okay", false).commit();
					return;
				}
				//Billing is setup
				masterPref.edit().putBoolean("buy_okay", true).commit();
				billHelper.queryInventoryAsync(billGotInventoryListener);
			}
		});
	}

	//Listener for items that the user owns
	private IabHelper.QueryInventoryFinishedListener billGotInventoryListener = new
			IabHelper.QueryInventoryFinishedListener() {
		public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
			if (result.isFailure()) {
				//Failed to get item list
				return;
			}
			//This is where we check for items.
			Purchase removeAdsPurchase = inventory.getPurchase(AppPurchasing.SKU_ADS);
			boolean adsRemoved = (removeAdsPurchase != null &&
					AppPurchasing.verifyDeveloperPayload(removeAdsPurchase));
			masterPref.edit().putBoolean("remove_ads", adsRemoved).commit();

			/*
			//for Items that are consumed in replacement of setting the value
			billHelper.consumeAsync(inventory.getPurchase(AppPurchasing.SKU_ADS), billConsumeFinishedListener);
			*/
		}
	};

	/*
	//Listener for when item consumption is finished
	IabHelper.OnConsumeFinishedListener billConsumeFinishedListener = new IabHelper.OnConsumeFinishedListener() {
				public void onConsumeFinished(Purchase purchase, IabResult result) {
					if (result.isSuccess()) {

					} else {
						//Item was not consumed
					}
				}
			};
	*/

	public void goTest()
	{
		Intent test = new Intent(this, TestMain.class);
		test.putExtra("user", userName);
		startActivity(test);
		finish();
	}

	public void goSelect()
	{
		Intent select = new Intent(this, TestSelect.class);
		startActivity(select);
		finish();
	}
}
