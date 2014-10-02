package com.reflectmobile.activity;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar.LayoutParams;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.reflectmobile.R;
import com.reflectmobile.utility.NetworkManager;
import com.reflectmobile.utility.NetworkManager.HttpPostTask;
import com.reflectmobile.utility.NetworkManager.HttpPutTask;
import com.reflectmobile.utility.NetworkManager.HttpTaskHandler;

import de.neofonie.mobile.app.android.widget.crouton.Crouton;
import de.neofonie.mobile.app.android.widget.crouton.Style;

public class AddMomentActivity extends BaseActivity {

	private String TAG = "AddMomentActivity";

	private boolean nameSet = false;

	private int communityId;
	private String name;
	
	private Menu menu;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		hasNavigationDrawer = false;
		setContentView(R.layout.add_moment);
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
		
		if (getIntent().hasExtra("moment_id")) {
			setTitle("Edit Moment");
		}

		communityId = getIntent().getIntExtra("community_id", 0);

		EditText momentName = (EditText) findViewById(R.id.moment_name);
		if (getIntent().hasExtra("name")) {
			name = getIntent().getStringExtra("name");
			momentName.setText(name);
			nameSet = true;
		}
		
		momentName.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				nameSet = s.length() > 0;
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
		
		momentName.requestFocus();
		
		momentName.postDelayed(new Runnable() {
			
			@Override
			public void run() {
				InputMethodManager inputMethodManager=(InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
			    inputMethodManager.toggleSoftInput(0, InputMethodManager.SHOW_IMPLICIT);
			}
		}, 200);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.add_moment_menu, menu);
		if (getIntent().hasExtra("moment_id")) {
			MenuItem addMoment = menu.findItem(R.id.action_add_moment);
			addMoment.setTitle("SAVE");
		}
		this.menu = menu;
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action buttons
		switch (item.getItemId()) {
		case R.id.action_add_moment:
			if (nameSet) {
				MenuItem addMoment = menu.findItem(R.id.action_add_moment);
				addMoment.setEnabled(false);
				addMoment();
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

	public void addMoment() {
		HttpTaskHandler httpPostTaskHandler = new HttpTaskHandler() {
			@Override
			public void taskSuccessful(String result) {
				Log.d("POST", result);
				setResult(RESULT_OK);
				try {
					JSONObject momentData = new JSONObject(result);
					int momentId = momentData.getInt("id");
					Intent intent = new Intent();
					intent.putExtra("moment_id", momentId);
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
		JSONObject momentData = new JSONObject();
		try {
			momentData.put("community_id", communityId);
			momentData.put("name", name);
		} catch (JSONException e) {
			Log.e(TAG, "Error forming JSON");
		}
		String payload = momentData.toString();
		
		if (getIntent().hasExtra("moment_id")) {
			int momentId = getIntent().getIntExtra("moment_id", 0);
			new HttpPutTask(httpPostTaskHandler, payload)
					.execute(NetworkManager.hostName + "/api/moments/"
							+ momentId);
		} else {
			new HttpPostTask(httpPostTaskHandler, payload)
					.execute(NetworkManager.hostName + "/api/communities/"
							+ communityId + "/moments");
		}
	}
}
