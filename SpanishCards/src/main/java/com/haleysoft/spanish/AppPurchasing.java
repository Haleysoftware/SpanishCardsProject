package com.haleysoft.spanish;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.vending.billing.util.IabHelper;
import com.android.vending.billing.util.IabResult;
import com.android.vending.billing.util.Inventory;
import com.android.vending.billing.util.Purchase;

/**
 * Created by mhaley on 8/6/13.
 */
public class AppPurchasing extends Activity implements View.OnClickListener {
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
		setWaitScreen(true);
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
		billHelper.enableDebugLogging(false); //Need to change to false before release
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

					checkBuys();
					setWaitScreen(false);
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

	//Listener for when a purchase is finished
	IabHelper.OnIabPurchaseFinishedListener billPurchaseFinishListener =
			new IabHelper.OnIabPurchaseFinishedListener() {
				public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
					if (result.isFailure()) {
						//complain("Error purchasing: " + result);
						setWaitScreen(false);
						return;
					}
					if (!verifyDeveloperPayload(purchase)) {
						//complain("Error purchasing. Authenticity verification failed.");
						setWaitScreen(false);
						return;
					}
					//Purchase was successful!
					if (purchase.getSku().equals(SKU_ADS)) {
						//alert("Thank you for your the purchase!");
						masterPref.edit().putBoolean("remove_ads", true).commit();
						checkBuys();
						setWaitScreen(false);
					}
				}
			};

	private void checkBuys () {
		Button adButton = (Button) this.findViewById(R.id.shop_adButton);
		if (masterPref.getBoolean("remove_ads", false)) {
			adButton.setClickable(false);
			adButton.setText(R.string.shopadbuttonyes);
		} else {
			adButton.setClickable(true);
			adButton.setText(R.string.shopadbuttonno);
			adButton.setOnClickListener(this);
		}

		if (!masterPref.getBoolean("buy_okay", false)) {
			adButton.setClickable(false);
		}
	}

	private void setWaitScreen(boolean state) {
		TextView wait = (TextView) this.findViewById(R.id.shop_Wait);
		if (wait != null) {
			wait.setVisibility(state ? View.VISIBLE : View.GONE);
		}
		LinearLayout adLayout = (LinearLayout) this.findViewById(R.id.shop_AdLayout);
		if (adLayout != null) {
			adLayout.setVisibility(state ? View.INVISIBLE : View.VISIBLE);
		}
	}

	//Verifies if purchase is real
	public static boolean verifyDeveloperPayload(Purchase purchase) {
		boolean isReal;
		String payload = purchase.getDeveloperPayload();
		isReal = payload.contentEquals("s6f54safa5f4as65f46s54fas65f4ff4a6sf7s1fs35f4a6");
		return isReal;
	}

	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.shop_adButton:
				setWaitScreen(true);
				String payload = "s6f54safa5f4as65f46s54fas65f4ff4a6sf7s1fs35f4a6";
				billHelper.launchPurchaseFlow(this, SKU_ADS, REQUEST_CODE, billPurchaseFinishListener, payload);
				break;
		}
	}

	/*
	void complain(String message) {
		alert("Error: " + message);
	}

	void alert(String message) {
		AlertDialog.Builder bld = new AlertDialog.Builder(this);
		bld.setMessage(message);
		bld.setNeutralButton("OK", null);
		bld.create().show();
	}
	*/
}
