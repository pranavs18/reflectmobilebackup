package com.reflectmobile.activity;

import java.util.ArrayList;
import java.util.Locale;

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
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.speech.RecognizerIntent;
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

public class AddStoryActivity extends BaseActivity {

	private String TAG = "AddStoryActivity";
	public static int CODE_SPEECH_RECOGNITION = 101;
	//private int photoId;

	private Menu menu;
	private String story;
	boolean storySet = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		hasNavigationDrawer = false;
		setContentView(R.layout.add_story);
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
			setTitle("Edit Story");
		}

		getActionBar().setDisplayHomeAsUpEnabled(true);
		ImageView view = (ImageView) findViewById(android.R.id.home);
		view.setPadding(10, 0, 0, 0);

		EditText storyText = (EditText) findViewById(R.id.story_text);
		if (getIntent().hasExtra("story")) {
			story = getIntent().getStringExtra("story");
			storyText.setText(story);
			storySet = true;
		}

		storyText.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				storySet = s.length() > 0;
				if (storySet) {
					story = s.toString();
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
		inflater.inflate(R.menu.add_story_menu, menu);
		if (getIntent().hasExtra("memory_id")) {
			MenuItem addStory = menu.findItem(R.id.action_add_story);
			addStory.setTitle("SAVE");
		}
		this.menu = menu;
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action buttons
		switch (item.getItemId()) {
		case R.id.action_add_story:
			if (storySet) {
				MenuItem add_story = menu.findItem(R.id.action_add_story);
				add_story.setEnabled(false);
				addStory();
			} else {
				int red = android.R.color.holo_red_light;
				Style CustomAlert = new Style.Builder().setDuration(2000)
						.setHeight(LayoutParams.WRAP_CONTENT).setTextSize(16)
						.setBackgroundColor(red).setPaddingInPixels(26).build();
				Crouton.makeText(this, "Please, write your story first",
						CustomAlert).show();

			}
			return true;
		case R.id.action_speech_to_text:
			startVoiceRecognitionActivity();
			return true;
		case android.R.id.home:
			onBackPressed();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void startVoiceRecognitionActivity() {
		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
				RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
				"Speak your story...");
		startActivityForResult(intent, CODE_SPEECH_RECOGNITION);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == CODE_SPEECH_RECOGNITION && resultCode == RESULT_OK) {
			// Populate the wordsList with the String values the recognition
			// engine thought it heard
			ArrayList<String> matches = data
					.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
			String recognized = matches.get(0);
			if (recognized.length() > 0) {
				String firstLetter = recognized.substring(0, 1);
				String capitalized = firstLetter.toUpperCase(Locale.US);
				recognized = recognized.replaceFirst(firstLetter, capitalized);
			}
			
			EditText storyText = (EditText) findViewById(R.id.story_text);
			
			if (storyText.length() > 0){
				storyText.getText().append(" ");
			}
			storyText.getText().append(recognized);
			storyText.getText().append(".");
			
			storyText.requestFocus();
			
			InputMethodManager inputMethodManager=(InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		    inputMethodManager.showSoftInput(storyText, 0);
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	public void addStory() {
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

		try {
			if (getIntent().hasExtra("tag_id")){
				int tagId = getIntent().getIntExtra("tag_id", 0); 
				storyData.put("tag_id", tagId);
			}
			else {
				int photoId = getIntent().getIntExtra("photo_id", 0);
				storyData.put("photo_id", photoId);
			}
			storyData.put("memory_type", "story");
			storyData.put("memory_content", story);
		} catch (JSONException e) {
			Log.e(TAG, "Error forming JSON");
		}
		String payload = storyData.toString();

		if (getIntent().hasExtra("memory_id")) {
			int memoryId = getIntent().getIntExtra("memory_id", 0);
			new HttpPutTask(httpPostTaskHandler, payload)
					.execute(NetworkManager.hostName + "/api/memories/"
							+ memoryId);
		} else {
			new HttpPostTask(httpPostTaskHandler, payload)
					.execute(NetworkManager.hostName + "/api/memories");
		}
	}
}
