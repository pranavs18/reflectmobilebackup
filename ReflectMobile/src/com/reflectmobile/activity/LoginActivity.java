package com.reflectmobile.activity;

import java.io.File;
import java.io.IOException;

import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.net.http.HttpResponseCache;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.reflectmobile.R;
import com.reflectmobile.utility.NetworkManager;
import com.reflectmobile.utility.NetworkManager.HttpTaskHandler;

public class LoginActivity extends BaseActivity implements ConnectionCallbacks,
		OnConnectionFailedListener {
	// private static final String URL_PREFIX_FRIENDS =
	// "https://graph.facebook.com/me/friends?access_token=";
	private static final String TAG = "LoginActivity";

	private boolean signInClicked;

	// A flag indicating that a PendingIntent is in progress and prevents us
	// from starting further intents.
	private boolean mGoogleIntentInProgress;

	// Store the connection result from onConnectionFailed callbacks so that we
	// can resolve them
	private ConnectionResult mGoogleConnectionResult;

	// Request code used to invoke sign in user interactions.
	private static final int RC_SIGN_IN = 1001;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		try {
			File httpCacheDir = new File(getCacheDir(), "http");
			long httpCacheSize = 10 * 1024 * 1024; // 10 MiB
			HttpResponseCache.install(httpCacheDir, httpCacheSize);
		} catch (IOException e) {
			Log.e(TAG, "HTTP response cache installation failed:" + e);
		}

		hasNavigationDrawer = false;
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		// Initialize Google API client
		mGoogleApiClient = new GoogleApiClient.Builder(this)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this)
				.addApi(Plus.API, new Plus.PlusOptions.Builder().build())
				.addScope(Plus.SCOPE_PLUS_LOGIN).build();
	}

	@Override
	protected void onStart() {
		super.onStart();
		// Connect to Google API
		mGoogleApiClient.connect();
	}

	public void onClickLogInGoogle(View button) {
		if (!signInClicked && !mGoogleApiClient.isConnecting()) {
			signInClicked = true;
			googleSignIn();
		}
	}

	public void onClickLogInReflect(View button) {
		if (!signInClicked) {
			signInClicked = true;
			setSignInStatus(SIGNED_IN_REFLECT);
			String userId = "109014750652754814692";
			NetworkManager.setUsedId(userId);

			final HttpTaskHandler loginReflectWebHandler = new HttpTaskHandler() {
				@Override
				public void taskSuccessful(String result) {
					Log.d("POST", result);
					Intent intent = new Intent(LoginActivity.this,
							CommunitiesActivity.class);
					startActivity(intent);
				}

				@Override
				public void taskFailed(String reason) {
					Log.e("POST", "Error within POST request: " + reason);
				}
			};
			NetworkManager.loginViaReflect(loginReflectWebHandler);
		}
	}

	// Method for signing in with google in both first attempt and retry
	private void googleSignIn() {
		if (mGoogleConnectionResult != null
				&& mGoogleConnectionResult.hasResolution()) {
			try {
				mGoogleIntentInProgress = true;
				mGoogleConnectionResult.startResolutionForResult(this,
						RC_SIGN_IN);
			} catch (SendIntentException e) {
				// The intent was canceled before it was sent. Return to the
				// default state and attempt to connect to get an updated
				// ConnectionResult.
				mGoogleIntentInProgress = false;
				mGoogleApiClient.connect();
			}
		}
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		// Allow user to retry
		signInClicked = false;

		if (!result.hasResolution()) {
			GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(), this,
					0).show();
			return;
		}

		if (!mGoogleIntentInProgress) {
			// Store the ConnectionResult for later usage
			mGoogleConnectionResult = result;
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == RC_SIGN_IN) {
			if (resultCode != RESULT_OK) {
				signInClicked = false;
			}

			mGoogleIntentInProgress = false;

			if (!mGoogleApiClient.isConnecting()
					&& resultCode != RESULT_CANCELED) {
				mGoogleApiClient.connect();
			}
		}
	}

	@Override
	public void onConnected(Bundle connectionHint) {
		// We've resolved any connection errors. mGoogleApiClient can be used to
		// access Google APIs on behalf of the user.
		signInClicked = false;
		setSignInStatus(SIGNED_IN_GOOGLE);
		Toast.makeText(this, "User is connected!", Toast.LENGTH_LONG).show();

		String userId = null;
		try {
			if (Plus.PeopleApi.getCurrentPerson(mGoogleApiClient) != null) {
				Person currentPerson = Plus.PeopleApi
						.getCurrentPerson(mGoogleApiClient);
				userId = currentPerson.getId();
				NetworkManager.setUsedId(userId);
			} else {
				Toast.makeText(getApplicationContext(),
						"Person information is null", Toast.LENGTH_LONG).show();
			}
		} catch (Exception e) {
			Log.e(TAG, "Error getting Google profile information");
		}

		final HttpTaskHandler loginReflectWebHandler = new HttpTaskHandler() {
			@Override
			public void taskSuccessful(String result) {
				Log.d("POST", result);
				Intent intent = new Intent(LoginActivity.this,
						CommunitiesActivity.class);
				startActivity(intent);
			}

			@Override
			public void taskFailed(String reason) {
				Log.e("POST", "Error within POST request: " + reason);
			}
		};
		NetworkManager.loginViaReflect(loginReflectWebHandler);
	}

	@Override
	public void onConnectionSuspended(int cause) {
		mGoogleApiClient.connect();
	}

}
