package com.reflectmobile.activity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.annotation.SuppressLint;
import android.app.ActionBar.LayoutParams;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.RectF;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.CursorLoader;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.view.ViewGroup;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import com.reflectmobile.R;
import com.reflectmobile.data.Memory;
import com.reflectmobile.data.Moment;
import com.reflectmobile.data.Photo;
import com.reflectmobile.data.Tag;
import com.reflectmobile.utility.NetworkManager;
import com.reflectmobile.utility.NetworkManager.HttpGetImageTask;
import com.reflectmobile.utility.NetworkManager.HttpGetTask;
import com.reflectmobile.utility.NetworkManager.HttpImageTaskHandler;
import com.reflectmobile.utility.NetworkManager.HttpDeleteTask;
import com.reflectmobile.utility.NetworkManager.HttpPostTask;
import com.reflectmobile.utility.NetworkManager.HttpPutTask;
import com.reflectmobile.utility.NetworkManager.HttpTaskHandler;
import com.reflectmobile.view.CustomScrollView;
import com.reflectmobile.view.CustomViewPager;
import com.reflectmobile.widget.ImageProcessor;
import com.reflectmobile.widget.Segmentation;

import de.neofonie.mobile.app.android.widget.crouton.Crouton;
import de.neofonie.mobile.app.android.widget.crouton.Style;

import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.provider.MediaStore;

public class PhotoActivity extends BaseActivity {

	private String TAG = "PhotoActivity";
	private Moment moment;
	private int memoryCount = 0;
	private int memoryPhotoId;
	private Integer memoryTagId;
	private LayoutInflater mInflater;

	private int communityId;
	private int momentId;

	// Menu items
	private MenuItem add_photo;
	private MenuItem delete_photo;
	private MenuItem edit_tag;
	private MenuItem delete_tag;
	private MenuItem done_add;
	private MenuItem done_edit;

	/* photo gallery variables */
	static final int CODE_ADD_STORY = 101;
	static final int CODE_ADD_DETAIL = 102;
	static final int CODE_ADD_SOUND = 103;
	static final int CODE_SELECT_PICTURE = 104;

	/* photo gallery variables */
	private CustomViewPager viewPager;
	private CustomScrollView scrollView;
	private String selectedImagePath;
	private ImageView img;

	// MediaPlayer for playing sounds
	private MediaPlayer mediaPlayer;
	private boolean isPlaying = false;
	private boolean isPaused = false;
	private int soundPlayingId = -1;
	private ImageView soundIcon;

	// Calculate when the activity start
	private static int photoImageViewHeightDP = 258;
	private static int photoImageViewWidthDP = 360;
	private static int photoImageViewHeightPX = 0;
	private static int photoImageViewWidthPX = 0;

	// Set when the image changes
	private int currentPhotoIndex = 0;
	private int currentPhotoId = 0;
	private Photo currentPhoto;

	private ImageView currentImageView = null;
	private float photoOffsetX = 0;
	private float photoOffsetY = 0;
	private float photoScaleFactor = 1;
	private boolean isExpandHorizontal = false;

	private boolean isSmartMode = false;
	// Set when user want to edit the tag
	// If it is in add tag mode, it should be set the the center of the image
	// If it is in edit tag mode, it should be set to the tag location
	private RectF tagLocation = null;
	private Tag currentTag = null;

	private boolean newTagMode = false;

	// Segmentation related
	private GestureDetector addTagGestureDetector;
	private Segmentation segmentation;
	private int currentThreshold = 10;
	private int maxThreshold = 100;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		hasNavigationDrawer = false;
		setContentView(R.layout.activity_photo);
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

		communityId = getIntent().getIntExtra("community_id", 0);
		momentId = getIntent().getIntExtra("moment_id", 0);
		currentPhotoId = getIntent().getIntExtra("photo_id", 0);

		mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		img = (ImageView) findViewById(R.id.imageView1);
		scrollView = (CustomScrollView) findViewById(R.id.scroll_view);

		// Retreive data from the web
		final HttpTaskHandler getMomentHandler = new HttpTaskHandler() {
			@Override
			public void taskSuccessful(String result) {
				// Parse JSON to the list of communities
				moment = Moment.getMomentInfo(result);
				setTitle(moment.getName());

				int index = 0;
				for (int count = 0; count < moment.getNumOfPhotos(); count++) {
					if (moment.getPhoto(count).getId() == currentPhotoId) {
						index = count;
						break;
					}
				}
				viewPager = (CustomViewPager) findViewById(R.id.view_pager);
				ImagePagerAdapter adapter = new ImagePagerAdapter(
						PhotoActivity.this);
				viewPager.setAdapter(adapter);
				viewPager.setOnPageChangeListener(new OnPageChangeListener() {
					@Override
					public void onPageSelected(final int position) {
						onPhotoSelected(position);
					}

					@Override
					public void onPageScrolled(int arg0, float arg1, int arg2) {
					}

					@Override
					public void onPageScrollStateChanged(int arg0) {
					}
				});
				viewPager.setCurrentItem(index);
				onPhotoSelected(index);

			}

			@Override
			public void taskFailed(String reason) {
				Log.e(TAG, "Error within GET request: " + reason);
			}
		};

		new HttpGetTask(getMomentHandler).execute(NetworkManager.hostName
				+ "/api/moments/" + momentId);

		// Transfer image view size from dp to px
		photoImageViewHeightPX = dpToPx(photoImageViewHeightDP);
		photoImageViewWidthPX = dpToPx(photoImageViewWidthDP);

		// Gesture detector
		this.addTagGestureDetector = new GestureDetector(PhotoActivity.this,
				new AddTagGestureListener());
	}

	@Override
	public void onBackPressed() {
		if (newTagMode) {
			newTagMode = false;
			showTags();
		} else {
			Intent intent = new Intent(PhotoActivity.this, MomentActivity.class);
			intent.putExtra("moment_id", momentId);
			intent.putExtra("community_id", communityId);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
					| Intent.FLAG_ACTIVITY_CLEAR_TASK);
			startActivity(intent);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.photo_menu, menu);
		add_photo = menu.findItem(R.id.action_add_photo);
		delete_photo = menu.findItem(R.id.action_delete_photo);
		edit_tag = menu.findItem(R.id.action_edit_tag);
		delete_tag = menu.findItem(R.id.action_delete_tag);
		done_add = menu.findItem(R.id.action_done_add);
		done_edit = menu.findItem(R.id.action_done_edit);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	protected void onStop() {
		stopPlaying();
		super.onStop();
	};

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action buttons
		EditText tagName = (EditText) findViewById(R.id.tag_name);

		switch (item.getItemId()) {
		case R.id.action_add_photo:
			Intent intent = new Intent(PhotoActivity.this,
					GalleryActivity.class);
			intent.putExtra("community_id", communityId);
			intent.putExtra("moment_id", momentId);
			startActivity(intent);
			return true;
		case R.id.action_done_add:
			if (isSmartMode) {
				// TODO: get rectangle
				if (segmentation != null) {
					tagLocation = segmentation.getSquareLocation();
				}
			}
			if (tagLocation != null && tagName.getText().length() > 0) {
				persistTag(tagName.getText().toString(), false);
				tagLocation = null;
			} else {
				int red = android.R.color.holo_red_light;
				Style CustomAlert = new Style.Builder().setDuration(2000)
						.setHeight(LayoutParams.WRAP_CONTENT).setTextSize(16)
						.setBackgroundColor(red).setPaddingInPixels(26).build();
				Crouton.makeText(this,
						"Please, select tag and specify its name", CustomAlert)
						.show();
			}
			return true;
		case R.id.action_edit_tag:
			editTag();
			return true;
		case R.id.action_done_edit:
			if (tagLocation != null && tagName.getText().length() > 0) {
				persistTag(tagName.getText().toString(), true);
				tagLocation = null;
			} else {
				int red = android.R.color.holo_red_light;
				Style CustomAlert = new Style.Builder().setDuration(2000)
						.setHeight(LayoutParams.WRAP_CONTENT).setTextSize(16)
						.setBackgroundColor(red).setPaddingInPixels(26).build();
				Crouton.makeText(this,
						"Please, select tag and specify its name", CustomAlert)
						.show();
			}
			return true;
		case R.id.action_delete_tag:
			deleteTag();
			return true;
		case R.id.action_delete_photo:
			deletePhoto(currentPhotoId);
			return true;
		case android.R.id.home:
			onBackPressed();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			if (requestCode == CODE_SELECT_PICTURE) {
				Uri selectedImageUri = data.getData();
				selectedImagePath = getPath(selectedImageUri);
				System.out.println("Image Path : " + selectedImagePath);
				img.setImageURI(selectedImageUri);
			}
		}
		reloadMemories();
	}

	public String getPath(Uri uri) {
		String[] projection = { MediaStore.Images.Media.DATA };
		CursorLoader cursor = new CursorLoader(getApplication(), uri,
				projection, null, null, null);
		int column_index = ((Cursor) cursor)
				.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		((Cursor) cursor).moveToFirst();
		return ((Cursor) cursor).getString(column_index);
	}

	public void onPhotoSelected(final int position) {
		currentPhotoIndex = position;
		currentPhoto = moment.getPhoto(position);
		currentPhotoId = currentPhoto.getId();

		TextView date = (TextView) findViewById(R.id.date);
		date.setText(currentPhoto.getDate());

		ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
		currentImageView = (ImageView) viewPager
				.findViewWithTag(currentPhotoIndex);

		ToggleButton tagButton = (ToggleButton) findViewById(R.id.button_photo_tag);
		tagButton.setActivated(false);
		tagButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (v.isActivated()) {
					v.setActivated(false);
					showPhoto();
				} else {
					v.setActivated(true);
					showTags();
				}
			}
		});

		ImageButton addSoundButton = (ImageButton) findViewById(R.id.add_sound);
		addSoundButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(PhotoActivity.this,
						AddSoundActivity.class);
				intent.putExtra("photo_id", currentPhotoId);
				if (currentTag != null) {
					intent.putExtra("tag_id", currentTag.getId());
				}
				startActivityForResult(intent, CODE_ADD_SOUND);
			}
		});

		ImageButton addStoryButton = (ImageButton) findViewById(R.id.add_story);
		addStoryButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(PhotoActivity.this,
						AddStoryActivity.class);
				intent.putExtra("photo_id", currentPhotoId);
				if (currentTag != null) {
					intent.putExtra("tag_id", currentTag.getId());
				}
				startActivityForResult(intent, CODE_ADD_STORY);
			}
		});

		ImageButton addDetailButton = (ImageButton) findViewById(R.id.add_detail);
		addDetailButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(PhotoActivity.this,
						AddDetailActivity.class);
				intent.putExtra("photo_id", currentPhotoId);
				if (currentTag != null) {
					intent.putExtra("tag_id", currentTag.getId());
					intent.putExtra("name", currentTag.getName());
				}
				startActivityForResult(intent, CODE_ADD_DETAIL);
			}
		});

		loadTags();
	}

	private void loadTags() {
		// Load list of tags
		final HttpTaskHandler getTagsHandler = new HttpTaskHandler() {

			@Override
			public void taskSuccessful(String result) {
				Log.d(TAG, result);
				JSONArray tagJSONArray;
				try {
					currentPhoto.setTagList(new ArrayList<Tag>());
					tagJSONArray = new JSONArray(result);
					for (int j = 0; j <= tagJSONArray.length() - 1; j++) {
						Tag tag = Tag.getTagInfo(tagJSONArray.getString(j));
						currentPhoto.addTag(tag);
					}
					
					// Revised 8/1
					// Get tags point list from custom server if possible
					new HttpGetTask(new HttpTaskHandler() {
						
						@Override
						public void taskSuccessful(String result) {
							currentPhoto.addAllTagBoundary(result);
							loadMemories(currentPhotoId);
						}
						
						@Override
						public void taskFailed(String reason) {
							Log.e(TAG, "Error downloading the tag point list");
						}
					}).execute(NetworkManager.SOUND_HOST_NAME+"/tags/"+currentPhoto.getId());
				} catch (JSONException e) {
					Log.e(TAG, "Error parse the tag json");
				}
				setPeopleNames();
			}

			@Override
			public void taskFailed(String reason) {
				Log.e(TAG, "Error downloading the tag");
			}
		};

		new HttpGetTask(getTagsHandler).execute(NetworkManager.hostName
				+ "/api/photos/" + currentPhotoId + "/tags");
	}

	private void setPeopleNames() {
		TextView people = (TextView) findViewById(R.id.people);
		ArrayList<Tag> tagList = currentPhoto.getTagList();
		if (tagList.size() > 0) {
			if (tagList.size() > 2) {
				String caption = tagList.get(0).getName() + ", "
						+ tagList.get(1).getName() + " and "
						+ (tagList.size() - 2) + " more";
				people.setText(caption);
			} else if (tagList.size() == 2) {
				String caption = tagList.get(0).getName() + " and "
						+ tagList.get(1).getName();
				people.setText(caption);
			} else {
				people.setText(tagList.get(0).getName());
			}
		} else {
			people.setText("No tags");
		}
	}

	private void reloadMemories() {
		loadMemories(memoryPhotoId, memoryTagId, true);
	}

	private void loadMemories(int photoId) {
		loadMemories(photoId, null, false);
	}

	private void loadMemories(int photoId, Integer tagId) {
		loadMemories(photoId, tagId, false);
	}

	private void loadMemories(final int photoId, final Integer tagId,
			boolean reload) {
		// If we already loaded the data - do not load it again
		if (!reload && tagId == memoryTagId && photoId == memoryPhotoId) {
			return;
		}
		memoryPhotoId = photoId;
		memoryTagId = tagId;

		final ViewGroup memoryContainer = (ViewGroup) findViewById(R.id.memories_container);
		memoryContainer.removeAllViews();
		final TextView memoryCaption = (TextView) findViewById(R.id.memories_caption);
		memoryCount = 0;
		memoryCaption.setText("0 MEMORIES");

		final HttpTaskHandler getMemoriesHandler = new HttpTaskHandler() {
			final private int loadPhotoId = photoId;
			final private Integer loadTagId = tagId;

			@Override
			public void taskSuccessful(String result) {
				if (loadPhotoId == memoryPhotoId && loadTagId == memoryTagId) {
					// Parse JSON to the list of memories
					Memory[] mMemories = Memory.getMemoriesInfo(result);
					memoryCount += mMemories.length;

					if (memoryCount == 1) {
						memoryCaption.setText(memoryCount + " MEMORY");
					} else {
						memoryCaption.setText(memoryCount + " MEMORIES");
					}

					for (int count = 0; count < mMemories.length; count++) {
						View card = mInflater.inflate(R.layout.card_memory,
								memoryContainer, false);
						ImageView memoryIcon = (ImageView) card
								.findViewById(R.id.memory_icon);
						TextView memoryText = (TextView) card
								.findViewById(R.id.memory_text);
						TextView memoryInfo = (TextView) card
								.findViewById(R.id.memory_info);
						final ImageButton dotMenu = (ImageButton) card
								.findViewById(R.id.memory_card_dot);
						final Memory memory = mMemories[count];

						if (memory.getType().equals("sound")) {
							card.setTag(memory);
							card.setOnClickListener(onSoundPlayClicked);
						}

						memoryIcon.setImageResource(memory.getResourceId());
						memoryText.setText(memory.getContent());
						memoryInfo.setText(memory.getInfo());
						dotMenu.setTag(memory);
						dotMenu.setOnClickListener(onDotMenuClicked);

						memoryContainer.addView(card);
					}
				}
			}

			@Override
			public void taskFailed(String reason) {
				Log.e(TAG, "Error within GET request: " + reason);
			}
		};

		if (tagId == null) {
			new HttpGetTask(getMemoriesHandler).execute(NetworkManager.hostName
					+ "/api/memories?photo_id=" + photoId);
			for (Tag tag : currentPhoto.getTagList()) {
				new HttpGetTask(getMemoriesHandler)
						.execute(NetworkManager.hostName
								+ "/api/memories?tag_id=" + tag.getId());
			}
		} else {
			new HttpGetTask(getMemoriesHandler).execute(NetworkManager.hostName
					+ "/api/memories?tag_id=" + tagId);
		}

	}

	private OnClickListener onDotMenuClicked = new OnClickListener() {

		@Override
		public void onClick(View v) {
			final Memory memory = (Memory) v.getTag();

			// Creating the instance of PopupMenu
			PopupMenu popup = new PopupMenu(PhotoActivity.this, v);
			// Inflating the Popup using xml file
			popup.getMenuInflater().inflate(R.menu.popup_memory,
					popup.getMenu());
			// registering popup with OnMenuItemClickListener
			popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
				public boolean onMenuItemClick(MenuItem item) {
					switch (item.getItemId()) {
					case R.id.action_edit_memory:
						editMemory(memory, currentPhotoId);
						return true;
					case R.id.action_delete_memory:
						deleteMemory(memory.getId());
						return true;
					default:
						return true;
					}
				}
			});

			// showing popup menu
			popup.show();
		}
	};

	private void editMemory(Memory memory, int photoId) {
		String type = memory.getType();
		if (type.equals("detail")) {
			Intent intent = new Intent(PhotoActivity.this,
					AddDetailActivity.class);
			intent.putExtra("memory_id", memory.getId());
			intent.putExtra("photo_id", photoId);
			if (currentTag != null) {
				intent.putExtra("tag_id", currentTag.getId());
			}
			Pattern pattern = Pattern.compile("(.*) (WAS [A-Z]*) (.*)");
			Matcher matcher = pattern.matcher(memory.getContent());
			if (matcher.find()) {
				intent.putExtra("name", matcher.group(1));
				intent.putExtra("spinner_value", matcher.group(2));
				intent.putExtra("detail", matcher.group(3));
			}
			startActivityForResult(intent, CODE_ADD_DETAIL);
		} else if (type.equals("story")) {
			Intent intent = new Intent(PhotoActivity.this,
					AddStoryActivity.class);
			intent.putExtra("memory_id", memory.getId());
			intent.putExtra("story", memory.getContent());
			intent.putExtra("photo_id", photoId);
			if (currentTag != null) {
				intent.putExtra("tag_id", currentTag.getId());
			}
			startActivityForResult(intent, CODE_ADD_STORY);
		} else if (type.equals("sound")) {
			Intent intent = new Intent(PhotoActivity.this,
					AddSoundActivity.class);
			intent.putExtra("memory_id", memory.getId());
			intent.putExtra("sound_name", memory.getContent());
			intent.putExtra("photo_id", photoId);
			if (currentTag != null) {
				intent.putExtra("tag_id", currentTag.getId());
			}
			startActivityForResult(intent, CODE_ADD_SOUND);
		}
	}

	private void deleteMemory(int id) {
		HttpTaskHandler httpDeleteTaskHandler = new HttpTaskHandler() {

			@Override
			public void taskSuccessful(String result) {
				reloadMemories();
			}

			@Override
			public void taskFailed(String reason) {
				Log.e(TAG, "Error deleting memory");
			}
		};
		new HttpDeleteTask(httpDeleteTaskHandler)
				.execute(NetworkManager.hostName + "/api/memories/" + id);
	}

	private void deletePhoto(int id) {
		HttpTaskHandler httpDeleteTaskHandler = new HttpTaskHandler() {

			@Override
			public void taskSuccessful(String result) {
				onBackPressed();
			}

			@Override
			public void taskFailed(String reason) {
				Log.e(TAG, "Error deleting memory");
			}
		};
		new HttpDeleteTask(httpDeleteTaskHandler)
				.execute(NetworkManager.hostName + "/api/photos/" + id);
	}

	private void showMenuShowTags() {
		add_photo.setVisible(false);
		delete_photo.setVisible(false);
		edit_tag.setVisible(false);
		delete_tag.setVisible(false);
		done_add.setVisible(false);
		done_edit.setVisible(false);
	}

	private void showMenuTagSelected() {
		add_photo.setVisible(false);
		delete_photo.setVisible(false);
		edit_tag.setVisible(true);
		delete_tag.setVisible(true);
		done_add.setVisible(false);
		done_edit.setVisible(false);
	}

	private void showMenuEditTag() {
		add_photo.setVisible(false);
		delete_photo.setVisible(false);
		edit_tag.setVisible(false);
		delete_tag.setVisible(false);
		done_add.setVisible(false);
		done_edit.setVisible(true);
	}

	private void showMenuAddTag() {
		add_photo.setVisible(false);
		delete_photo.setVisible(false);
		edit_tag.setVisible(false);
		delete_tag.setVisible(false);
		done_add.setVisible(true);
		done_edit.setVisible(false);
	}

	private void showMenuPhoto() {
		add_photo.setVisible(true);
		delete_photo.setVisible(true);
		edit_tag.setVisible(false);
		delete_tag.setVisible(false);
		done_add.setVisible(false);
		done_edit.setVisible(false);
	}

	private void showTags() {
		// Show corresponding menu
		showMenuShowTags();

		// Disable paging
		viewPager.setPagingEnabled(false);

		// Enable scrolling
		scrollView.setScrollingEnabled(true);

		// Hide keyboard
		InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		inputMethodManager.hideSoftInputFromWindow(findViewById(R.id.tag_name)
				.getWindowToken(), 0);

		// Show tabs
		findViewById(R.id.tab_view).setVisibility(View.VISIBLE);

		// Hide EditText
		findViewById(R.id.tag_name_container).setVisibility(View.GONE);

		// Hide instructions
		findViewById(R.id.instructions).setVisibility(View.GONE);

		// Show memories
		findViewById(R.id.memories_caption).setVisibility(View.VISIBLE);
		findViewById(R.id.memories_container).setVisibility(View.VISIBLE);
		
		// Show add tag button
		findViewById(R.id.photo_description).setVisibility(View.GONE);
		Button add_tag = (Button) findViewById(R.id.add_tag);
		add_tag.setVisibility(View.VISIBLE);
		add_tag.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				addTag();
			}
		});

		// Load list of tags
		final HttpTaskHandler getTagsHandler = new HttpTaskHandler() {

			@Override
			public void taskSuccessful(String result) {
				Log.d(TAG, result);
				JSONArray tagJSONArray;
				try {
					currentPhoto.setTagList(new ArrayList<Tag>());
					tagJSONArray = new JSONArray(result);
					for (int j = 0; j <= tagJSONArray.length() - 1; j++) {
						Tag tag = Tag.getTagInfo(tagJSONArray.getString(j));
						currentPhoto.addTag(tag);
					}
					
					// Get tags point list from custom server if possible
					new HttpGetTask(new HttpTaskHandler() {
						
						@Override
						public void taskSuccessful(String result) {
							currentPhoto.addAllTagBoundary(result);
							
							// Revised 8/1
							currentPhoto.refreshTags();

							setPeopleNames();

							// Set the tagged bitmap
							currentImageView.setImageBitmap(currentPhoto
									.getTaggedLargeBitmap());
							// Calculate the offset
							setPhotoOffset();
							// Set on touch listener for the photo
							currentImageView.setOnTouchListener(onPhotoTouchListener);
						}
						
						@Override
						public void taskFailed(String reason) {
							Log.e(TAG, "Error downloading the tag point list");
						}
					}).execute(NetworkManager.SOUND_HOST_NAME+"/tags/"+currentPhoto.getId());
				} catch (JSONException e) {
					Log.e(TAG, "Error parse the tag json");
				}
			}

			@Override
			public void taskFailed(String reason) {
				Log.e(TAG, "Error downloading the tag");
			}
		};

		// Load large image
		HttpImageTaskHandler httpImageTaskHandler = new HttpImageTaskHandler() {
			@Override
			public void taskSuccessful(Drawable drawable) {
				currentPhoto.setLargeBitmap(((BitmapDrawable) drawable)
						.getBitmap());
				new HttpGetTask(getTagsHandler).execute(NetworkManager.hostName
						+ "/api/photos/" + currentPhotoId + "/tags");
			}

			@Override
			public void taskFailed(String reason) {
				Log.e(TAG, "Error downloading the tags");
			}
		};

		Bitmap largeBitmap = currentPhoto.getLargeBitmap();
		if (largeBitmap == null) {
			new HttpGetImageTask(httpImageTaskHandler).execute(currentPhoto
					.getImageLargeURL());
		} else {
			new HttpGetTask(getTagsHandler).execute(NetworkManager.hostName
					+ "/api/photos/" + currentPhotoId + "/tags");
		}
	}

	private void onTagSelected() {
		// Set title to tag name
		setTitle(currentTag.getName());

		// Show corresponding menu
		showMenuTagSelected();

		// Load Memories related to tag
		loadMemories(currentPhotoId, currentTag.getId());
	}

	private void onTagUnselected() {
		// Set title to normal
		setTitle(moment.getName());

		// Show corresponding menu
		showMenuShowTags();

		// Load Memories related to tag
		loadMemories(currentPhotoId);
	}

	private void addTag() {
		newTagMode = true;

		// Show corresponding menu
		showMenuAddTag();

		// Disable scrolling
		scrollView.setScrollingEnabled(false);

		// Hide tabs
		findViewById(R.id.tab_view).setVisibility(View.GONE);

		// Hide add tag button
		findViewById(R.id.add_tag).setVisibility(View.GONE);

		// Show EditText instead
		findViewById(R.id.tag_name_container).setVisibility(View.VISIBLE);
		((EditText) findViewById(R.id.tag_name)).setText("");

		// Hide memories
		findViewById(R.id.memories_caption).setVisibility(View.GONE);
		findViewById(R.id.memories_container).setVisibility(View.GONE);
		
		// Show instructions
		TextView instructions = (TextView) findViewById(R.id.instructions);
		instructions.setText("Tap photo to add tag. \n Long tap to select an object");
		instructions.setVisibility(View.VISIBLE);

		// Indicate that there is no tag
		tagLocation = null;

		currentImageView.setImageBitmap(currentPhoto.getLargeBitmap());
		currentImageView.setOnTouchListener(null);
		currentImageView.setOnTouchListener(onAddTagTouchListener);
	}

	private void editTag() {
		// Show corresponding menu
		showMenuEditTag();

		// Disable scrolling
		scrollView.setScrollingEnabled(false);

		// Hide tabs
		findViewById(R.id.tab_view).setVisibility(View.GONE);

		// Hide add tag button
		findViewById(R.id.add_tag).setVisibility(View.GONE);

		// Show EditText instead
		findViewById(R.id.tag_name_container).setVisibility(View.VISIBLE);
		((EditText) findViewById(R.id.tag_name)).setText(currentTag.getName());

		// Indicate that there is no tag
		tagLocation = new RectF(currentTag.getUpLeftX(),
				currentTag.getUpLeftY(), currentTag.getUpLeftX()
						+ currentTag.getBoxWidth(), currentTag.getUpLeftY()
						+ currentTag.getBoxLength());

		currentImageView.setOnTouchListener(onEditTagTouchListener);
	}

	private void showPhoto() {
		currentImageView.setImageDrawable(currentPhoto.getMediumDrawable());
		currentImageView.setOnTouchListener(null);

		showMenuPhoto();
		viewPager.setPagingEnabled(true);

		findViewById(R.id.photo_description).setVisibility(View.VISIBLE);
		findViewById(R.id.add_tag).setVisibility(View.GONE);
	}

	private void persistTag(String tagName, boolean edit) {
		final boolean sendToCustomBackend = isSmartMode;

		final HttpTaskHandler postTagHandler = new HttpTaskHandler() {
			@Override
			public void taskSuccessful(String result) {
				showTags();
			}

			@Override
			public void taskFailed(String reason) {
				Log.e(TAG, "Error within POST SOUND request: " + reason);
			}
		};

		HttpTaskHandler httpPostTaskHandler = new HttpTaskHandler() {
			@Override
			public void taskSuccessful(String result) {
				if (sendToCustomBackend) {
					JSONObject tagData;
					try {
						tagData = new JSONObject(result);
						int tagId = tagData.getInt("id");
						JSONObject json = new JSONObject();

						JSONArray boundary = new JSONArray();

						// TODO: change coordinates
						if (segmentation != null) {
							for (Point point : segmentation
									.getConvevHullPointList()) {
								JSONObject jsonPoint = new JSONObject();
								jsonPoint.put("x", point.x);
								jsonPoint.put("y", point.y);
								boundary.put(jsonPoint);
							}
							json.put("boundary", boundary);
							json.put("tag_id", tagId);
						}

						new HttpPostTask(postTagHandler, json.toString())
								.execute(NetworkManager.SOUND_HOST_NAME
										+ "/tags/" + tagId + "/photo/"
										+ currentPhotoId);
					} catch (JSONException e) {
						Log.e(TAG, "Error parsing JSON");
					}

				} else {
					showTags();
				}
			}

			@Override
			public void taskFailed(String reason) {
				Log.e("POST", "Error within POST request: " + reason);
			}
		};

		JSONObject tagData = new JSONObject();
		tagLocation = getValidTagLocation(tagLocation);
		try {
			tagData.put("x_coordinate", (int) tagLocation.left + "px");
			tagData.put("y_coordinate", (int) tagLocation.top + "px");
			tagData.put("box_width",
					(int) (tagLocation.right - tagLocation.left) + "px");
			tagData.put("box_length",
					(int) (tagLocation.bottom - tagLocation.top) + "px");
			tagData.put("tag_type", "object");
			tagData.put("object_name", tagName);
			tagData.put("photo_id", currentPhotoId);

		} catch (JSONException e) {
			Log.e(TAG, "Error forming JSON");
		}
		String payload = tagData.toString();

		if (edit) {
			new HttpPutTask(httpPostTaskHandler, payload)
					.execute(NetworkManager.hostName + "/api/tags/"
							+ currentTag.getId());
		} else {
			new HttpPostTask(httpPostTaskHandler, payload)
					.execute(NetworkManager.hostName + "/api/photos/"
							+ currentPhotoId + "/tags");
		}
	}

	private void deleteTag() {
		HttpTaskHandler httpDeleteTaskHandler = new HttpTaskHandler() {

			@Override
			public void taskSuccessful(String result) {
				showTags();
			}

			@Override
			public void taskFailed(String reason) {
				Log.e(TAG, "Error deleting memory");
			}
		};
		new HttpDeleteTask(httpDeleteTaskHandler)
				.execute(NetworkManager.hostName + "/api/tags/"
						+ currentTag.getId());
	}

	private void stopPlaying() {
		isPlaying = false;
		isPaused = false;
		soundPlayingId = -1;
		if (soundIcon != null) {
			soundIcon.setImageResource(R.drawable.sound_small);
		}
		if (mediaPlayer != null) {
			mediaPlayer.release();
		}
		mediaPlayer = null;
	}

	private OnClickListener onSoundPlayClicked = new OnClickListener() {

		@Override
		public void onClick(View v) {
			Memory memory = (Memory) v.getTag();
			if (memory.getId() != soundPlayingId) {
				stopPlaying();
				soundPlayingId = memory.getId();
				soundIcon = (ImageView) v.findViewById(R.id.memory_icon);
			}

			if (!isPlaying && !isPaused) {
				isPlaying = true;
				isPaused = false;
				soundIcon.setImageResource(R.drawable.pause);
				int sound_id = memory.getId();
				String url = NetworkManager.SOUND_HOST_NAME + "/sounds/"
						+ sound_id;
				mediaPlayer = new MediaPlayer();
				mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
				try {
					mediaPlayer.setDataSource(url);
				} catch (IllegalArgumentException e) {
					Log.d(TAG, "Illegal Argument Exception");
				} catch (SecurityException e) {
					Log.d(TAG, "Security Exception");
				} catch (IllegalStateException e) {
					Log.d(TAG, "Illegal State Exception");
				} catch (IOException e) {
					Log.d(TAG, "IOException");
				}
				mediaPlayer.setOnPreparedListener(new OnPreparedListener() {
					@Override
					public void onPrepared(MediaPlayer player) {
						player.start();
					}
				});
				mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
					@Override
					public void onCompletion(MediaPlayer arg0) {
						stopPlaying();
					}
				});

				mediaPlayer.prepareAsync();
			} else if (isPlaying && !isPaused) {
				isPaused = true;
				isPlaying = false;
				soundIcon.setImageResource(R.drawable.sound_small);
				mediaPlayer.pause();
			} else if (!isPlaying && isPaused) {
				soundIcon.setImageResource(R.drawable.pause);
				isPaused = false;
				isPlaying = true;
				mediaPlayer.start();
			}
		}
	};

	@SuppressLint("ClickableViewAccessibility")
	private OnTouchListener onPhotoTouchListener = new OnTouchListener() {
		public boolean onTouch(View v, MotionEvent event) {
			// Transfer the coordinate from the image view to the
			// photo bitmap
			// Location on the enlarged photo
			float bitmapX = event.getX() + photoOffsetX;
			float bitmapY = event.getY() + photoOffsetY;
			// Location to original photo
			bitmapX = bitmapX / photoScaleFactor;
			bitmapY = bitmapY / photoScaleFactor;
			Log.d(TAG, "Bitmap" + bitmapX + " " + bitmapY);
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				// Generate the highlighted tag bitmap
				Bitmap newBitmap = ImageProcessor
						.generateHighlightedTaggedBitmap(
								currentPhoto.getLargeBitmap(),
								currentPhoto.getTaggedLargeBitmap(),
								currentPhoto.getDarkenTaggedLargeBitmap(),
								currentPhoto.getTagList(), bitmapX, bitmapY);
				// Set to the current image view
				currentImageView.setImageBitmap(newBitmap);

				currentTag = ImageProcessor.getSelectedTag(
						currentPhoto.getTagList(), bitmapX, bitmapY);
				if (currentTag != null) {
					onTagSelected();
				} else {
					onTagUnselected();
				}
				break;
			default:
				break;
			}
			return true;
		}
	};

	private class AddTagGestureListener extends SimpleOnGestureListener {

		@Override
		public boolean onDown(MotionEvent e) {
			return true;
		}

		@Override
		public void onLongPress(MotionEvent event) {
			isSmartMode = true;
			TextView instructions = (TextView) findViewById(R.id.instructions);
			instructions.setText("Tap tap to select more.");
			
			Toast.makeText(PhotoActivity.this, "Smart Mode", Toast.LENGTH_LONG)
					.show();
			// Transfer the coordinate from the image view to the
			// photo bitmap
			// Location on the enlarged photo
			float bitmapX = event.getX() + photoOffsetX;
			float bitmapY = event.getY() + photoOffsetY;
			// Location to original photo
			bitmapX = bitmapX / photoScaleFactor;
			bitmapY = bitmapY / photoScaleFactor;
			// Segmentation
			segmentation = new Segmentation(currentPhoto.getLargeBitmap());
			segmentation.setTouchLocation((int) bitmapX, (int) bitmapY);
			segmentation.segmentation(currentThreshold);
			currentImageView.setImageBitmap(segmentation.getResultBitmap());

			// Set following on touch listener
			currentImageView.setOnTouchListener(null);
			currentImageView.setOnTouchListener(onSegmentationTouchListener);
		}

		@Override
		public boolean onSingleTapUp(MotionEvent event) {
			isSmartMode = false;
			Toast.makeText(PhotoActivity.this, "Normal Mode", Toast.LENGTH_LONG)
					.show();
			// Transfer the coordinate from the image view to the
			// photo bitmap
			// Location on the enlarged photo
			float bitmapX = event.getX() + photoOffsetX;
			float bitmapY = event.getY() + photoOffsetY;

			float left = Math.max(10, bitmapX - 100);
			float top = Math.max(10, bitmapY - 100);
			float right = left + 200;
			float bottom = top + 200;

			tagLocation = new RectF(left, top, right, bottom);

			// Draw initial tag square
			Bitmap initialBitmap = ImageProcessor.drawEditSquare(
					currentPhoto.getLargeBitmap(),
					currentPhoto.getDarkenLargeBitmap(), tagLocation, true);
			currentImageView.setImageBitmap(initialBitmap);

			// Set following on touch listener
			currentImageView.setOnTouchListener(null);
			currentImageView.setOnTouchListener(onEditTagTouchListener);
			return true;
		}

	}

	@SuppressLint("ClickableViewAccessibility")
	private OnTouchListener onAddTagTouchListener = new OnTouchListener() {

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			PhotoActivity.this.addTagGestureDetector.onTouchEvent(event);
			return true;
		}
	};

	@SuppressLint("ClickableViewAccessibility")
	private OnTouchListener onEditTagTouchListener = new OnTouchListener() {
		float prevBitmapX = 0;
		float prevBitmapY = 0;
		int movingNode = 8;

		public boolean onTouch(View v, MotionEvent event) {
			// Transfer the coordinate from the image view to the
			// photo bitmap
			// Location on the enlarged photo
			float bitmapX = event.getX() + photoOffsetX;
			float bitmapY = event.getY() + photoOffsetY;
			// Location to original photo
			bitmapX = bitmapX / photoScaleFactor;
			bitmapY = bitmapY / photoScaleFactor;

			switch (event.getActionMasked()) {
			case MotionEvent.ACTION_MOVE:
				// Only single touch trigger this event
				Log.d(TAG, "MovingNode:" + movingNode);
				// During move, keep updating the edit square
				// Change tag location based on touch location
				changeTagLocation(prevBitmapX, prevBitmapY, bitmapX, bitmapY,
						movingNode);
				// Draw new edit tag square
				Bitmap newBitmap = ImageProcessor.drawEditSquare(
						currentPhoto.getLargeBitmap(),
						currentPhoto.getDarkenLargeBitmap(),
						getValidTagLocation(tagLocation), true);
				currentImageView.setImageBitmap(newBitmap);
				// Save touch location
				prevBitmapX = bitmapX;
				prevBitmapY = bitmapY;
				break;
			case MotionEvent.ACTION_DOWN:
				movingNode = determineMovingMode(bitmapX, bitmapY);
				break;
			case MotionEvent.ACTION_UP:
				// Clear last touch location
				prevBitmapX = -1;
				prevBitmapY = -1;
				tagLocation = getValidTagLocation(tagLocation);
				break;
			default:
				break;
			}
			return true;
		}
	};

	@SuppressLint("ClickableViewAccessibility")
	private OnTouchListener onSegmentationTouchListener = new OnTouchListener() {
		@Override
		public boolean onTouch(View view, MotionEvent event) {
			switch (event.getActionMasked()) {
			case MotionEvent.ACTION_DOWN:
				// Add threshold
				currentThreshold = (currentThreshold + 10) % maxThreshold;
				// Segmentation
				segmentation.segmentation(currentThreshold);
				currentImageView.setImageBitmap(segmentation.getResultBitmap());
			default:
				break;
			}
			return true;
		}
	};

	public class ImagePagerAdapter extends PagerAdapter {

		public ImagePagerAdapter(Context context) {

		}

		@Override
		public int getCount() {
			return moment.getNumOfPhotos();
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view.equals(object);
		}

		@Override
		public Object instantiateItem(final ViewGroup container,
				final int position) {
			Log.d(TAG, position + "");
			ImageView imageView = new ImageView(PhotoActivity.this);
			if (position == currentPhotoIndex) {
				currentImageView = imageView;
			}
			imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
			imageView.setTag(position);
			((ViewPager) container).addView(imageView);

			HttpImageTaskHandler httpImageTaskHandler = new HttpImageTaskHandler() {
				private int drawableIndex = position;

				@Override
				public void taskSuccessful(Drawable drawable) {
					moment.getPhoto(drawableIndex).setMediumDrawable(drawable);
					ImageView imageView = (ImageView) ((ViewPager) container)
							.findViewWithTag(drawableIndex);
					if (imageView != null) {
						imageView.setImageDrawable(drawable);
					}
				}

				@Override
				public void taskFailed(String reason) {
					Log.e(TAG, "Error downloading the tags");
				}
			};

			Drawable mediumDrawable = moment.getPhoto(position)
					.getMediumDrawable();
			if (mediumDrawable == null) {
				new HttpGetImageTask(httpImageTaskHandler).execute(moment
						.getPhoto(position).getImageMediumURL());
			} else {
				imageView.setImageDrawable(mediumDrawable);
			}
			return imageView;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			((ViewPager) container).removeView((ImageView) object);
		}
	}

	// Change dp to px
	private int dpToPx(int dp) {
		DisplayMetrics displayMetrics = PhotoActivity.this.getResources()
				.getDisplayMetrics();
		int px = Math.round(dp * displayMetrics.density);
		return px;
	}

	// This function calculates the offset of the photo in the image view
	// This should be called when the current image view change
	// Currently hardcode to tagbutton onclick listener
	private void setPhotoOffset() {
		isExpandHorizontal = ((float) currentPhoto.getLargeBitmap().getHeight() / currentPhoto
				.getLargeBitmap().getWidth()) > ((float) photoImageViewHeightPX / photoImageViewWidthPX);
		// If the photo in image view is expanded horizontally
		if (isExpandHorizontal) {
			photoScaleFactor = (float) photoImageViewHeightPX
					/ currentPhoto.getLargeBitmap().getHeight();
			float offsetX = ((photoImageViewWidthPX - photoScaleFactor
					* currentPhoto.getLargeBitmap().getWidth()) / 2);
			photoOffsetX = -offsetX;
			photoOffsetY = 0;
		} else {
			photoScaleFactor = (float) photoImageViewWidthPX
					/ currentPhoto.getLargeBitmap().getWidth();
			float offsetY = ((photoImageViewHeightPX - photoScaleFactor
					* currentPhoto.getLargeBitmap().getHeight()) / 2);
			photoOffsetX = 0;
			photoOffsetY = -offsetY;
		}
	}

	// This function test whether user wants to move the tag or resize the tag
	// based
	// on touch location
	// All location is location on the bitmap, not on the enlarged bitmap
	private int determineMovingMode(float curX, float curY) {
		// Currently decide the radius is 30, maybe changed
		int squareRadius = 80;
		// Calculate whether the touch location is in the edit square region
		float left = tagLocation.left;
		float right = tagLocation.right;
		float top = tagLocation.top;
		float bottom = tagLocation.bottom;
		float[] editSquareXs = { left, (left + right) / 2, right, left, right,
				left, (left + right) / 2, right };
		float[] editSquareYs = { top, top, top, (top + bottom) / 2,
				(top + bottom) / 2, bottom, bottom, bottom };

		for (int i = 0; i <= 7; i++) {
			boolean inSquare = Math.abs(curX - editSquareXs[i]) <= squareRadius
					&& Math.abs(curY - editSquareYs[i]) <= squareRadius;
			if (inSquare) {
				return i;
			}
		}
		// Not in any edit square region, then default is 8, move the tag
		return 8;
	}

	// This function is used to change tag location based on the user
	// touch location
	// All location is location on the bitmap, not on the enlarged bitmap
	private void changeTagLocation(float prevX, float prevY, float curX,
			float curY, int movingMode) {
		// Fist touch situation, when prevX and prevY are less than 0
		if (prevX < 0 && prevY < 0) {
			return;
		}

		float left = tagLocation.left;
		float right = tagLocation.right;
		float top = tagLocation.top;
		float bottom = tagLocation.bottom;
		float offsetX = curX - prevX;
		float offsetY = curY - prevY;

		// Change the tag location
		if (movingMode == 0) {
			// Change leftTop
			tagLocation.set(curX, curY, right, bottom);
		} else if (movingMode == 1) {
			// Change middleTop
			tagLocation.set(left, curY, right, bottom);
		} else if (movingMode == 2) {
			// Change rightTop
			tagLocation.set(left, curY, curX, bottom);
		} else if (movingMode == 3) {
			// Change leftMiddle
			tagLocation.set(curX, top, right, bottom);
		} else if (movingMode == 4) {
			// Change rightMiddle
			tagLocation.set(left, top, curX, bottom);
		} else if (movingMode == 5) {
			// Change leftBottom
			tagLocation.set(curX, top, right, curY);
		} else if (movingMode == 6) {
			// Change middleBottom
			tagLocation.set(left, top, right, curY);
		} else if (movingMode == 7) {
			// Change bottomRight
			tagLocation.set(left, top, curX, curY);
		} else {
			// Check whether the new location is in the boundary
			boolean isInBoundary = left + offsetX >= ImageProcessor.IMAGE_BORDER_WIDTH
					&& top + offsetY >= ImageProcessor.IMAGE_BORDER_WIDTH
					&& right + offsetX <= currentPhoto.getLargeBitmap()
							.getWidth() - ImageProcessor.IMAGE_BORDER_WIDTH
					&& bottom + offsetY <= currentPhoto.getLargeBitmap()
							.getHeight() - ImageProcessor.IMAGE_BORDER_WIDTH;
			// Move the tag
			if (isInBoundary) {
				tagLocation.set(left + offsetX, top + offsetY, right + offsetX,
						bottom + offsetY);
			}
		}
	}

	private RectF getValidTagLocation(RectF tagLocation) {
		float left = tagLocation.left;
		float right = tagLocation.right;
		float top = tagLocation.top;
		float bottom = tagLocation.bottom;
		float temp = 0;
		if (left > right) {
			temp = left;
			left = right;
			right = temp;
		}
		if (top > bottom) {
			temp = top;
			top = bottom;
			bottom = temp;
		}
		return new RectF(left, top, right, bottom);
	}
}
