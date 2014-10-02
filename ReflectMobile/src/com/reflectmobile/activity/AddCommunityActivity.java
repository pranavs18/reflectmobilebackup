package com.reflectmobile.activity;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar.LayoutParams;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.reflectmobile.R;
import com.reflectmobile.data.Network;
import com.reflectmobile.utility.NetworkManager;
import com.reflectmobile.utility.NetworkManager.HttpGetTask;
import com.reflectmobile.utility.NetworkManager.HttpPostTask;
import com.reflectmobile.utility.NetworkManager.HttpPutTask;
import com.reflectmobile.utility.NetworkManager.HttpTaskHandler;

import de.neofonie.mobile.app.android.widget.crouton.Crouton;
import de.neofonie.mobile.app.android.widget.crouton.Style;

public class AddCommunityActivity extends BaseActivity {

	private String TAG = "AddCommunityActivity";
	private boolean networkChosen = false;
	private boolean nameSet = false;
	private boolean descriptionSet = false;

	private boolean canCreate = false;

	private int networkId;
	private String name;
	private String description;

	private Network[] mNetworks;
	
	private Menu menu;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		hasNavigationDrawer = false;
		setContentView(R.layout.add_community);
		super.onCreate(savedInstanceState);

		// Modify action bar title
		int titleId = getResources().getIdentifier("action_bar_title", "id",
				"android");
		TextView title = (TextView) findViewById(titleId);
		title.setTypeface(Typeface.createFromAsset(getAssets(),
				"fonts/RobotoCondensed-Regular.ttf"));

		// Set margin before title
		ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) title
				.getLayoutParams();
		mlp.setMargins(5, 0, 0, 0);

		getActionBar().setDisplayHomeAsUpEnabled(true);
		ImageView view = (ImageView) findViewById(android.R.id.home);
		view.setPadding(10, 0, 0, 0);

		if (getIntent().hasExtra("community_id")) {
			setTitle("Edit Community");
		}

		EditText communityName = (EditText) findViewById(R.id.community_name);
		if (getIntent().hasExtra("name")) {
			name = getIntent().getStringExtra("name");
			communityName.setText(name);
			nameSet = true;
		}

		EditText communityDesc = (EditText) findViewById(R.id.community_description);
		if (getIntent().hasExtra("description")) {
			description = getIntent().getStringExtra("description");
			communityDesc.setText(description);
			descriptionSet = true;
		}

		communityName.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				nameSet = s.length() > 0;
				modifySaveButton();
				if (nameSet) {
					name = s.toString();
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});

		communityDesc.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				descriptionSet = s.length() > 0;
				modifySaveButton();
				if (descriptionSet) {
					description = s.toString();
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});

		final Spinner spinner = (Spinner) findViewById(R.id.network);

		String[] initialChoices = { "Choose a Network" };
		final ArrayAdapter<String> spinnerInitialAdapter = new ArrayAdapter<String>(
				this, R.layout.spinner, initialChoices);
		spinnerInitialAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(spinnerInitialAdapter);

		final HttpTaskHandler getNetworksHandler = new HttpTaskHandler() {
			@Override
			public void taskSuccessful(String result) {
				// Parse JSON to the list of networks
				mNetworks = Network.getNetworksInfo(result);
				String[] choices = new String[mNetworks.length + 1];
				choices[0] = "Choose a Network";
				for (int count = 0; count < mNetworks.length; count++) {
					choices[count + 1] = mNetworks[count].getName();
				}

				final ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(
						AddCommunityActivity.this, R.layout.spinner, choices);
				spinnerArrayAdapter
						.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				spinner.setAdapter(spinnerArrayAdapter);

				if (getIntent().hasExtra("network_id")) {
					int networkId = getIntent().getIntExtra("network_id", 0);
					for (int count = 0; count < mNetworks.length; count++) {
						if (mNetworks[count].getId() == networkId) {
							spinner.setSelection(count + 1);
						}
					}
				}
				spinner.setOnItemSelectedListener(new OnItemSelectedListener() {

					public void onItemSelected(AdapterView<?> parent,
							View view, int pos, long id) {
						networkChosen = pos > 0;
						modifySaveButton();
						if (networkChosen) {
							networkId = mNetworks[pos - 1].getId();
						}
					}

					public void onNothingSelected(AdapterView<?> parent) {

					}
				});

			}

			@Override
			public void taskFailed(String reason) {
				Log.e(TAG, "Error within GET request: " + reason);
			}
		};
		new HttpGetTask(getNetworksHandler).execute(NetworkManager.hostName
				+ "/api/networks");

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.add_community_menu, menu);
		if (getIntent().hasExtra("community_id")) {
			MenuItem addCommunity = menu.findItem(R.id.action_add_community);
			addCommunity.setTitle("SAVE");
		}
		this.menu = menu;
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action buttons
		switch (item.getItemId()) {
		case R.id.action_add_community:
			if (canCreate) {
				MenuItem addMoment = menu.findItem(R.id.action_add_community);
				addMoment.setEnabled(false);
				addCommunity();
			} else {
				int red = android.R.color.holo_red_light;
				Style CustomAlert = new Style.Builder().setDuration(2000)
						.setHeight(LayoutParams.WRAP_CONTENT).setTextSize(16)
						.setBackgroundColor(red).setPaddingInPixels(26).build();
				Crouton.makeText(this, "Please, specify all fields",
						CustomAlert).show();
			}
			return true;
		case android.R.id.home:
			onBackPressed();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void addCommunity() {
		HttpTaskHandler httpPostTaskHandler = new HttpTaskHandler() {
			@Override
			public void taskSuccessful(String result) {
				Log.d("POST", result);
				setResult(RESULT_OK);
				try {
					JSONObject communityData = new JSONObject(result);
					int communityId = communityData.getInt("id");
					Intent intent = new Intent();
					intent.putExtra("community_id", communityId);
					setResult(RESULT_OK, intent);
				} catch (JSONException e) {
					Log.e("POST", "Error parsing JSON");
				}
				finish();
			}

			@Override
			public void taskFailed(String reason) {
				Log.e("POST", "Error within POST request: " + reason);
			}
		};
		JSONObject communityData = new JSONObject();
		try {
			communityData.put("network_id", networkId);
			communityData.put("name", name);
			communityData.put("description", description);
		} catch (JSONException e) {
			Log.e(TAG, "Error forming JSON");
		}
		String payload = communityData.toString();

		if (getIntent().hasExtra("community_id")) {
			int communityId = getIntent().getIntExtra("community_id", 0);
			new HttpPutTask(httpPostTaskHandler, payload)
					.execute(NetworkManager.hostName + "/api/communities/"
							+ communityId);
		} else {
			new HttpPostTask(httpPostTaskHandler, payload)
					.execute(NetworkManager.hostName
							+ "/api/communities?network_id=" + networkId);
		}

	}

	private void modifySaveButton() {
		if (descriptionSet && nameSet && networkChosen) {
			canCreate = true;
		} else {
			canCreate = false;
		}
	}

}
