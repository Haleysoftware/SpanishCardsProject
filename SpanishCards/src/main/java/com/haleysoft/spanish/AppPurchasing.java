package com.haleysoft.spanish;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;

import com.android.vending.billing.util.IabHelper;
import com.android.vending.billing.util.IabResult;
import com.android.vending.billing.util.Inventory;
import com.android.vending.billing.util.Purchase;

/**
 * Created by mhaley on 8/6/13.
 */
public class AppPurchasing extends Activity {
	private static final String MASTER_SETTINGS = "haley_master_set";
	private SharedPreferences masterPref;
    public static final String LICENSE_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAhf7caMdtA37LLLSVLOrH4DbBU/8VpODxmetrLvVbimpZ7pzadIw/8UcZW9LFSihOcHQkel5Y3qu9QAFH/t87fwAYRbKeRY9nyZ8pNS4eqB+JpKKbI+jUm2aKZ4DYkTG8E8NP4w7FtFioH7+QTGBbLlZh0xz8mHNmHRPm50TIauyDq8x5ULh+me7XbJyfis2m3rzWAQIe9d2U51yMw51DEYN0+yccMKHTrhiA/72veinMfd6WBs3dGNT2jaVzUZ74Sr0iSPNSbuNftQBTwqI7ICFrDXsy5eUz6OsMfiQXKq+9HtgQAAlUuZZnVvGZUCCVY129Tlw6nNt0sODHOfOjmwIDAQAB";
	private IabHelper billHelper;
	public static final String SKU_ADS = "removeads";
	private static final int REQUEST_CODE = 7589;

	@Override
	public void onCreate (Bundle savedState) {
		super.onCreate(savedState);
		this.setContentView(R.layout.shoplayout);
		masterPref = getSharedPreferences(MASTER_SETTINGS, MODE_PRIVATE);

		inAppCheck();
		checkBuys();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (billHelper != null) billHelper.dispose();
		billHelper = null;
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

	private void checkBuys () {
		Button adButton = (Button) this.findViewById(R.id.shop_adButton);
		if (masterPref.getBoolean("remove_ads", false)) {
			adButton.setClickable(false);
			adButton.setText(R.string.shopadbuttonyes);
		} else {
			adButton.setClickable(true);
			adButton.setText(R.string.shopadbuttonno);
		}

		if (!masterPref.getBoolean("buy_okay", false)) {
			adButton.setClickable(false);
		}
	}

	//Verifies if purchase is real
	public static boolean verifyDeveloperPayload(Purchase purchase) {
		boolean isReal = true;
		String payload = purchase.getDeveloperPayload();

		return isReal;
	}
}