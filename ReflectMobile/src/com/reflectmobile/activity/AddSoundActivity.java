package com.reflectmobile.activity;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar.LayoutParams;
import android.graphics.Typeface;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.reflectmobile.R;
import com.reflectmobile.utility.NetworkManager;
import com.reflectmobile.utility.NetworkManager.HttpPostSoundTask;
import com.reflectmobile.utility.NetworkManager.HttpPostTask;
import com.reflectmobile.utility.NetworkManager.HttpPutTask;
import com.reflectmobile.utility.NetworkManager.HttpTaskHandler;

import de.neofonie.mobile.app.android.widget.crouton.Crouton;
import de.neofonie.mobile.app.android.widget.crouton.Style;

public class AddSoundActivity extends BaseActivity {

	private String TAG = "AddSoundActivity";

	private String soundName;
	private boolean soundNameSet;

	private static String mFileName = null;
	private Menu menu;
	private LinearLayout mTitle = null;
	private ImageButton mRecordButton = null;
	private TextView mInstruction = null;
	private MediaRecorder mRecorder = null;
	private Chronometer mChronometer = null;
	private boolean isRecording = false;
	private boolean recordingCompleted = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		hasNavigationDrawer = false;
		setContentView(R.layout.add_sound);
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
			setTitle("Edit Sound");
			recordingCompleted = true;
		}

		getActionBar().setDisplayHomeAsUpEnabled(true);
		ImageView view = (ImageView) findViewById(android.R.id.home);
		view.setPadding(10, 0, 0, 0);

		EditText soundNameText = (EditText) findViewById(R.id.sound_name);
		if (getIntent().hasExtra("sound_name")) {
			soundName = getIntent().getStringExtra("sound_name");
			soundNameText.setText(soundName);
			soundNameSet = true;
			findViewById(R.id.recorder).setVisibility(View.GONE);
		}

		soundNameText.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				soundNameSet = s.length() > 0;
				if (soundNameSet) {
					soundName = s.toString();
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

		try {
			createSoundFile();
		} catch (IOException ex) {
			Log.d(TAG, "Can't create sound file");
		}

		mTitle = (LinearLayout) findViewById(R.id.title);
		mChronometer = (Chronometer) findViewById(R.id.chronometer);
		mInstruction = (TextView) findViewById(R.id.instruction);

		mRecordButton = (ImageButton) findViewById(R.id.record);
		mRecordButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (!isRecording) {
					mTitle.setVisibility(View.INVISIBLE);
					mRecordButton.setImageResource(R.drawable.recorder_pause);
					Animation anim = AnimationUtils.loadAnimation(AddSoundActivity.this, R.anim.recording);
					mRecordButton.startAnimation(anim);
					mInstruction.setText("Press to stop");
					MenuItem add_sound = menu.findItem(R.id.action_add_sound);
					add_sound.setVisible(false);
					startRecording();
				} else {
					mTitle.setVisibility(View.VISIBLE);
					mRecordButton.setImageResource(R.drawable.recorder_record);
					mRecordButton.clearAnimation();
					mInstruction.setText("Done!");
					MenuItem add_sound = menu.findItem(R.id.action_add_sound);
					add_sound.setVisible(true);
					stopRecording();
					mRecordButton.setEnabled(false);
				}
				isRecording = !isRecording;
			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.add_sound_menu, menu);
		if (getIntent().hasExtra("memory_id")) {
			MenuItem addSound = menu.findItem(R.id.action_add_sound);
			addSound.setTitle("SAVE");
		}
		this.menu = menu;
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action buttons
		switch (item.getItemId()) {
		case R.id.action_add_sound:
			if (soundNameSet && recordingCompleted) {
				MenuItem add_sound = menu.findItem(R.id.action_add_sound);
				add_sound.setEnabled(false);
				addSound();
			} else if (!soundNameSet) {
				int red = android.R.color.holo_red_light;
				Style CustomAlert = new Style.Builder().setDuration(2000)
						.setHeight(LayoutParams.WRAP_CONTENT).setTextSize(16)
						.setBackgroundColor(red).setPaddingInPixels(26).build();
				Crouton.makeText(this, "Please, add title to the sound",
						CustomAlert).show();
			} else {
				int red = android.R.color.holo_red_light;
				Style CustomAlert = new Style.Builder().setDuration(2000)
						.setHeight(LayoutParams.WRAP_CONTENT).setTextSize(16)
						.setBackgroundColor(red).setPaddingInPixels(26).build();
				Crouton.makeText(this, "Please, record sound first",
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

	@Override
	public void onBackPressed() {
		stopRecording();
		super.onBackPressed();
	}
	
	private File createSoundFile() throws IOException {
		// Create an image file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
				.format(new Date());
		String imageFileName = "GP3_" + timeStamp + ".3gp";
		File storageDir = Environment
				.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
		File image = new File(storageDir, imageFileName);
		mFileName = image.getAbsolutePath();
		Log.d("Sound filename", mFileName);
		return image;
	}

	private void startRecording() {
		mRecorder = new MediaRecorder();
		mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		mRecorder.setOutputFile(mFileName);
		mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

		mChronometer.setBase(SystemClock.elapsedRealtime());
		mChronometer.setText("00:00:00");
		mChronometer
				.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
					@Override
					public void onChronometerTick(Chronometer chronometer) {
						CharSequence text = chronometer.getText();
						if (text.length() == 5) {
							chronometer.setText("00:" + text);
						} else if (text.length() == 7) {
							chronometer.setText("0" + text);
						}
					}
				});
		mChronometer.start();

		try {
			mRecorder.prepare();
		} catch (IOException e) {
			Log.e(TAG, "Player 'prepare' failed");
		}

		mRecorder.start();
	}

	private void stopRecording() {
		if (mRecorder!=null){
			mRecorder.stop();
			mRecorder.release();
			mRecorder = null;
			mChronometer.stop();
		}
		recordingCompleted = true;
	}

	public void addSound() {
		final HttpTaskHandler postSoundHandler = new HttpTaskHandler() {
			@Override
			public void taskSuccessful(String result) {
				finish();
			}

			@Override
			public void taskFailed(String reason) {
				Log.e(TAG, "Error within POST SOUND request: " + reason);
			}
		};

		HttpTaskHandler httpPostTaskHandler = new HttpTaskHandler() {
			@Override
			public void taskSuccessful(String result) {
				try {
					JSONObject soundData = new JSONObject(result);
					int soundId = soundData.getInt("id");
					new HttpPostSoundTask(postSoundHandler,
							NetworkManager.SOUND_HOST_NAME + "/sounds/"
									+ soundId, AddSoundActivity.this)
							.execute(mFileName);

				} catch (JSONException e) {
					Log.e(TAG, "Error parsing JSON");
				}
			}

			@Override
			public void taskFailed(String reason) {
				Log.e("POST", "Error within POST request: " + reason);
			}
		};

		HttpTaskHandler httpPutTaskHandler = new HttpTaskHandler() {
			@Override
			public void taskSuccessful(String result) {
				finish();
			}

			@Override
			public void taskFailed(String reason) {
				Log.e("PUT", "Error within PUT request: " + reason);
			}
		};

		JSONObject storyData = new JSONObject();

		try {
			if (getIntent().hasExtra("tag_id")) {
				int tagId = getIntent().getIntExtra("tag_id", 0);
				storyData.put("tag_id", tagId);
			} else {
				int photoId = getIntent().getIntExtra("photo_id", 0);
				storyData.put("photo_id", photoId);
			}
			storyData.put("memory_type", "sound");
			storyData.put("memory_content", soundName);
		} catch (JSONException e) {
			Log.e(TAG, "Error forming JSON");
		}
		String payload = storyData.toString();

		if (getIntent().hasExtra("memory_id")) {
			int memoryId = getIntent().getIntExtra("memory_id", 0);
			new HttpPutTask(httpPutTaskHandler, payload)
					.execute(NetworkManager.hostName + "/api/memories/"
							+ memoryId);
		} else {
			new HttpPostTask(httpPostTaskHandler, payload)
					.execute(NetworkManager.hostName + "/api/memories");
		}
	}
}
