package com.lateensoft.pathfinder.toolkit;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ExpandableListView.OnGroupCollapseListener;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.lateensoft.pathfinder.toolkit.datahelpers.PTDatabaseManager;
import com.lateensoft.pathfinder.toolkit.datahelpers.PTUserPrefsManager;

public class PTMainActivity extends SherlockFragmentActivity implements
		OnClickListener, OnChildClickListener, OnGroupClickListener,
		OnGroupExpandListener, OnGroupCollapseListener {
	private final String TAG = PTMainActivity.class.getSimpleName();

	private DrawerLayout mDrawerLayout;
	private ActionBarDrawerToggle mDrawerToggle;
	private ExpandableListView mDrawerList;

	String mListLabels[];
	private PTUserPrefsManager mUserPrefsManager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mUserPrefsManager = new PTUserPrefsManager(this);
		PTDatabaseManager SQLManager = new PTDatabaseManager(
				this.getApplicationContext());

		// Needs to update the database after upgrading
		if (mUserPrefsManager.checkLastUsedVersion(this)) {
			SQLManager.performUpdates(this);
			mUserPrefsManager.setLastUsedVersion(this);
		}

		mListLabels = getResources().getStringArray(R.array.main_menu_array);

		setContentView(R.layout.activity_drawer_main);

		showRateDialogIfRequired();

		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
				GravityCompat.START);
		mDrawerToggle = new ActionBarDrawerToggle(this, /* host Activity */
		mDrawerLayout, /* DrawerLayout object */
		R.drawable.ic_drawer, /* nav drawer icon to replace 'Up' caret */
		R.string.app_name, /* "open drawer" description */
		R.string.app_name /* "close drawer" description */
		) {

			/** Called when a drawer has settled in a completely closed state. */
			public void onDrawerClosed(View view) {
				supportInvalidateOptionsMenu();
			}

			/** Called when a drawer has settled in a completely open state. */
			public void onDrawerOpened(View drawerView) {
				supportInvalidateOptionsMenu();
			}
		};
		mDrawerToggle.syncState();

		// Set the drawer toggle as the DrawerListener
		mDrawerLayout.setDrawerListener(mDrawerToggle);

		mDrawerList = (ExpandableListView) findViewById(R.id.left_drawer);

		// Set the adapter for the list view
		mDrawerList.setAdapter(new PTNavDrawerAdapter(this));
		mDrawerList.setGroupIndicator(getResources().getDrawable(R.drawable.nav_item_expand_icon));
		// Set the list's click listener
		// mDrawerList.setOnItemClickListener(this);
		mDrawerList.setOnChildClickListener(this);
		mDrawerList.setOnGroupClickListener(this);
		mDrawerList.setOnGroupExpandListener(this);
		mDrawerList.setOnGroupCollapseListener(this);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);
	}

	private void showRateDialogIfRequired() {
		if (mUserPrefsManager.checkLastRateTime()
				&& mUserPrefsManager.checkLastRatedVersion(this)) {
			showRateAppPromptDialog();
		}
	}

	public void showRateAppPromptDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		// Set up dialog layout

		ImageView dialogMemeImage = new ImageView(this);
		dialogMemeImage.setImageResource(R.drawable.rate_meme);
		dialogMemeImage.setScaleType(ScaleType.FIT_CENTER);

		builder.setTitle(getString(R.string.rate_dialog_header));

		builder.setView(dialogMemeImage)
				.setPositiveButton(
						getString(R.string.rate_dialog_positive_button), this)
				.setNegativeButton(
						getString(R.string.rate_dialog_negative_button), this);

		AlertDialog alert = builder.create();
		alert.show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Initialize the global menu items
		PTSharedMenu.onCreateOptionsMenu(menu, getApplicationContext());

		// Add aditional menu items
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
		for (int i = 0; i < menu.size(); i++) {
			menu.getItem(i).setVisible(!drawerOpen);

		}
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			if (mDrawerLayout.isDrawerOpen(mDrawerList)) {
				mDrawerLayout.closeDrawer(mDrawerList);
			} else {
				mDrawerLayout.openDrawer(mDrawerList);
			}
		}

		if (PTSharedMenu.onOptionsItemSelected(item, this) == false) {
			// handle local menu items here or leave blank
		}

		return super.onOptionsItemSelected(item);
	}

	// For dialog
	public void onClick(DialogInterface dialogInterface, int selection) {
		switch (selection) {
		case DialogInterface.BUTTON_POSITIVE:
			mUserPrefsManager.setLastRatedVersion(this);
			mUserPrefsManager.setRatePromptTime();
			Log.v(TAG, "Attempting to open market page");
			String appPackageName = this.getPackageName();
			try {
				startActivity(new Intent(Intent.ACTION_VIEW,
						Uri.parse("market://details?id=" + appPackageName)));
			} catch (ActivityNotFoundException e) {
				Log.v(TAG, "Failed to open market. Attempting in browser.");
				startActivity(new Intent(
						Intent.ACTION_VIEW,
						Uri.parse("http://play.google.com/store/apps/details?id="
								+ appPackageName)));
			}
			break;
		default:
			Log.v(TAG, "Delaying rate dialog.");
			mUserPrefsManager.setRatePromptTime();
		}

	}

	@Override
	public void onGroupCollapse(int arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onGroupExpand(int arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onGroupClick(ExpandableListView arg0, View arg1, int arg2,
			long arg3) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onChildClick(ExpandableListView arg0, View arg1, int arg2,
			int arg3, long arg4) {
		// TODO Auto-generated method stub
		return false;
	}
}
