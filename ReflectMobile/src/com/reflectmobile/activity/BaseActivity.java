package com.reflectmobile.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.reflectmobile.R;

import com.reflectmobile.utility.NetworkManager;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;


public abstract class BaseActivity extends Activity {

	private static String TAG = "BaseActivity";

	public static final int SIGNED_OUT = 1001;
	public static final int SIGNED_IN_GOOGLE = 1002;
	public static final int SIGNED_IN_FACEBOOK = 1003;
	public static final int SIGNED_IN_REFLECT = 1004;

	private static int signInStatus;

	// Client used to interact with Google APIs.
	protected static GoogleApiClient mGoogleApiClient;

	protected boolean hasNavigationDrawer = true;

	private ActionBarDrawerToggle drawerToggle;
	
	protected static ImageLoader imageLoader = ImageLoader.getInstance();
  

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ImageLoaderConfiguration.createDefault(this);
		if (hasNavigationDrawer) {
			ListView sideMenuView = (ListView) findViewById(R.id.left_drawer);
			String[] menuItemTitles = getResources().getStringArray(
					R.array.side_menu_titles);

			// Get drawable resources from string-array
			TypedArray imgs = getResources().obtainTypedArray(
					R.array.side_menu_drawables);
			final int drawables[] = new int[imgs.length()];
			for (int i = 0; i < imgs.length(); i++) {
				drawables[i] = imgs.getResourceId(i, -1);
			}
			imgs.recycle();

			// Set the adapter for the list view
			sideMenuView.setAdapter(new ArrayAdapter<String>(this,
					R.layout.side_menu_item, menuItemTitles) {
				@Override
				public View getView(int position, View convertView,
						ViewGroup parent) {
					// Setting the image drawable for the view
					View v = super.getView(position, convertView, parent);
					((TextView) v).setCompoundDrawablesWithIntrinsicBounds(
							drawables[position], 0, 0, 0);
					return v;
				}
			});
			// Set the list's click listener
			sideMenuView
					.setOnItemClickListener(new SideMenuItemClickListener());

			DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
			drawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
					R.drawable.ic_navigation_drawer, R.string.error_title,
					R.string.error_title);

			// Set the drawer toggle as the DrawerListener
			drawerLayout.setDrawerListener(drawerToggle);

			getActionBar().setDisplayHomeAsUpEnabled(true);
			getActionBar().setHomeButtonEnabled(true);
		}
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		if (hasNavigationDrawer){
			// Sync the toggle state after onRestoreInstanceState has occurred.
			drawerToggle.syncState();
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if (hasNavigationDrawer){
			drawerToggle.onConfigurationChanged(newConfig);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (hasNavigationDrawer){
			// The action bar home/up action should open or close the drawer.
			// ActionBarDrawerToggle will take care of this.
			if (drawerToggle.onOptionsItemSelected(item)) {
				return true;
			}
		}
		return super.onOptionsItemSelected(item);
	}

	private class SideMenuItemClickListener implements
			ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			switch (position) {
			case 0:
				break;
			case 1:
				signOut();
				break;
			default:
				break;
			}
		}
	}

	private static void googleSignOut() {
		if (mGoogleApiClient.isConnected()) {
			Log.d(TAG, "Clear default account");
			Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
			mGoogleApiClient.disconnect();
		}
	}

	protected static void setSignInStatus(int newStatus) {
		signInStatus = newStatus;
	}

	protected void signOut() {
		Log.d(TAG, "Signin status " + signInStatus);
		switch (signInStatus) {
		case SIGNED_IN_GOOGLE:
			googleSignOut();
			break;
		default:
			break;
		}
		signInStatus = SIGNED_OUT;
		NetworkManager.clearCookies();
		Intent intent = new Intent(this, LoginActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}
}
