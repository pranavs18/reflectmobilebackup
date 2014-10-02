package com.reflectmobile.activity;

import org.json.JSONException;
import org.json.JSONObject;

import com.reflectmobile.R;
import com.reflectmobile.utility.NetworkManager;
import com.reflectmobile.utility.NetworkManager.HttpPostTask;
import com.reflectmobile.utility.NetworkManager.HttpPutTask;
import com.reflectmobile.utility.NetworkManager.HttpTaskHandler;

import de.neofonie.mobile.app.android.widget.crouton.Crouton;
import de.neofonie.mobile.app.android.widget.crouton.Style;

import android.app.ActionBar.LayoutParams;
import android.content.Context;
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
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

public class AddDetailActivity extends BaseActivity {

	private String TAG = "AddDetailActivity";

	private boolean nameSet = false;
	private boolean detailSet = false;

	private String name;
	private String detail;
	
	private boolean canCreate = false;
	private Menu menu;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		hasNavigationDrawer = false;
		setContentView(R.layout.add_detail);
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
		
		if (getIntent().hasExtra("memory_id")) {
			setTitle("Edit Detail");
		}
		
		getActionBar().setDisplayHomeAsUpEnabled(true);
		ImageView view = (ImageView) findViewById(android.R.id.home);
		view.setPadding(10, 0, 0, 0);

		Spinner spinner = (Spinner) findViewById(R.id.spinner_emotions);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				this, R.array.detail_items,
				R.layout.spinner);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		
		if (getIntent().hasExtra("spinner_value")){
			String spinnerValue = getIntent().getStringExtra("spinner_value");
			for (int count=0; count < spinner.getCount(); count++){
				if (spinner.getItemAtPosition(count).equals(spinnerValue)){
					spinner.setSelection(count);
				}
			}
		}
		
		EditText nameText = (EditText) findViewById(R.id.tagged);
		if (getIntent().hasExtra("name")){
			name = getIntent().getStringExtra("name");
			nameText.setText(name);
			nameSet = true;
		}
		
		nameText.postDelayed(new Runnable() {
			@Override
			public void run() {
				InputMethodManager inputMethodManager=(InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
			    inputMethodManager.toggleSoftInput(0, InputMethodManager.SHOW_IMPLICIT);
			}
		}, 200);
		
		EditText detailText = (EditText) findViewById(R.id.detail_text);
		if (getIntent().hasExtra("detail")){
			detail = getIntent().getStringExtra("detail");
			detailText.setText(detail);
			detailSet = true;
		}
		modifySaveButton();
		
		nameText.addTextChangedListener(new TextWatcher() {
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

		detailText.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				detailSet = s.length() > 0;
				modifySaveButton();
				if (detailSet) {
					detail = s.toString();
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

		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.add_detail_menu, menu);
		if (getIntent().hasExtra("memory_id")) {
			MenuItem addDetail = menu.findItem(R.id.action_add_detail);
			addDetail.setTitle("SAVE");
		}
		this.menu = menu;
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action buttons
		switch (item.getItemId()) {
		case R.id.action_add_detail:
			if (canCreate){
				MenuItem add_detail = menu.findItem(R.id.action_add_detail);
				add_detail.setEnabled(false);
				addDetail();
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

	public void addDetail() {
		HttpTaskHandler httpPostTaskHandler = new HttpTaskHandler() {
			@Override
			public void taskSuccessful(String result) {
				Log.d("POST", result);
				finish();
			}

			@Override
			public void taskFailed(String reason) {
				Log.e("POST", "Error within POST request: " + reason);
			}
		};
		JSONObject storyData = new JSONObject();

		Spinner spinner = (Spinner) findViewById(R.id.spinner_emotions);
		String spinnerText = spinner.getSelectedItem().toString();


		String memoryText = name + " " + spinnerText + " " + detail;
		try {
			if (getIntent().hasExtra("tag_id")){
				int tagId = getIntent().getIntExtra("tag_id", 0); 
				storyData.put("tag_id", tagId);
			}
			else {
				int photoId = getIntent().getIntExtra("photo_id", 0);
				storyData.put("photo_id", photoId);
			}
			storyData.put("memory_type", "detail");
			storyData.put("memory_content", memoryText);
		} catch (JSONException e) {
			Log.e(TAG, "Error forming JSON");
		}
		String payload = storyData.toString();
		
		if (getIntent().hasExtra("memory_id")){
			int memoryId = getIntent().getIntExtra("memory_id", 0);
			new HttpPutTask(httpPostTaskHandler, payload)
				.execute(NetworkManager.hostName+"/api/memories/" + memoryId);
		}
		else {
			new HttpPostTask(httpPostTaskHandler, payload)
				.execute(NetworkManager.hostName+"/api/memories");
		}
	}
	
	private void modifySaveButton() {
		if (detailSet && nameSet) {
			canCreate = true;
		} else {
			canCreate = false;
		}
	}


}
