package com.reflectmobile.activity;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar.LayoutParams;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.Spinner;
import android.widget.TextView;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.reflectmobile.R;
import com.reflectmobile.data.Community;
import com.reflectmobile.utility.NetworkManager;
import com.reflectmobile.utility.NetworkManager.HttpGetTask;
import com.reflectmobile.utility.NetworkManager.HttpTaskHandler;
import com.reflectmobile.utility.NetworkManager.HttpPostImageTask;

import de.neofonie.mobile.app.android.widget.crouton.Crouton;
import de.neofonie.mobile.app.android.widget.crouton.Style;

public class AddPhotosActivity extends BaseActivity {

	private static final String TAG = "AddPhotosActivity";

	private final static int CODE_ADD_COMMUNITY = 101;
	private static final int CODE_ADD_MOMENT = 102;
	private LayoutInflater inflater;

	private ArrayList<String> imageUrls;
	private ImageAdapter imageAdapter;
	private DisplayImageOptions options;
	private Community[] mCommunities;
	private Community mCommunity;
	private boolean communityChosen = false;
	private boolean momentChosen = false;
	private int communityId;
	private int momentId;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		hasNavigationDrawer = false;
		setContentView(R.layout.add_photos);
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

		// set configuration for the image loader instance
		// we can have default configuration but this config will invoke faster
		// loading of the images
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
				getApplicationContext())
				.threadPoolSize(3)
				.threadPriority(Thread.NORM_PRIORITY - 2)
				.memoryCacheSize(1500000)
				// 1.5 Mb
				.denyCacheImageMultipleSizesInMemory()
				.discCacheFileNameGenerator(new Md5FileNameGenerator())
				.enableLogging().build();

		ImageLoader.getInstance().init(config);

		this.imageUrls = getIntent().getStringArrayListExtra("images");

		imageAdapter = new ImageAdapter(this, imageUrls);

		GridView gridView = (GridView) findViewById(R.id.selected_images);
		gridView.setAdapter(imageAdapter);

		inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		final Spinner spinnerCommunity = (Spinner) findViewById(R.id.community);
		String[] initialChoicesCommunity = { "Choose a Community" };

		final SpinnerAdapter spinnerCommunityInitialAdapter = new SpinnerAdapter(
				this, R.layout.spinner, initialChoicesCommunity);
		spinnerCommunity.setAdapter(spinnerCommunityInitialAdapter);

		final Spinner spinnerMoment = (Spinner) findViewById(R.id.moment);
		String[] initialChoicesMoment = { "Choose a Moment" };
		final SpinnerAdapter spinnerMomentInitialAdapter = new SpinnerAdapter(
				this, R.layout.spinner, initialChoicesMoment);
		spinnerMoment.setAdapter(spinnerMomentInitialAdapter);

		final HttpTaskHandler getMomentsHandler = new HttpTaskHandler() {

			@Override
			public void taskSuccessful(String result) {
				// Parse JSON to the list of networks
				Log.d(TAG, result);
				mCommunity = Community.getCommunityInfo(result);
				String[] choices = new String[mCommunity.getNumOfMoments() + 2];
				choices[0] = "Choose a Moment";
				
				boolean moment_defined = getIntent().hasExtra("moment_id");
				int moment_id = getIntent().getIntExtra("moment_id", 0);
				int selection = 0;
				
				for (int count = 0; count < mCommunity.getNumOfMoments(); count++) {
					choices[count + 1] = mCommunity.getMoment(count).getName();
					if (moment_defined && mCommunity.getMoment(count).getId() == moment_id){
						selection = count + 1;
					}
				}
				choices[mCommunity.getNumOfMoments() + 1] = "New Moment";

				final SpinnerAdapter spinnerMomentInitialAdapter = new SpinnerAdapter(
						AddPhotosActivity.this, R.layout.spinner, choices);
				spinnerMoment.setAdapter(spinnerMomentInitialAdapter);
				spinnerMoment.setSelection(selection);
				
				spinnerMoment
						.setOnItemSelectedListener(new OnItemSelectedListener() {

							public void onItemSelected(AdapterView<?> parent,
									View view, int pos, long id) {
								if (pos == mCommunity.getNumOfMoments() + 1) {
									addMoment();
								} else {
									momentChosen = pos > 0;
									if (momentChosen) {
										momentId = mCommunity.getMoment(pos - 1)
												.getId();
									}
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

		final HttpTaskHandler getCommunitiesHandler = new HttpTaskHandler() {

			@Override
			public void taskSuccessful(String result) {
				// Parse JSON to the list of networks
				mCommunities = Community.getCommunitiesInfo(result);
				String[] choices = new String[mCommunities.length + 2];
				choices[0] = "Choose a Community";
				
				boolean community_defined = getIntent().hasExtra("community_id");
				int community_id = getIntent().getIntExtra("community_id", 0);
				int selection = 0;
				
				for (int count = 0; count < mCommunities.length; count++) {
					choices[count + 1] = mCommunities[count].getName();
					if (community_defined && mCommunities[count].getId() == community_id){
						selection = count + 1;
					}
				}
				choices[mCommunities.length + 1] = "New Community";

				final SpinnerAdapter spinnerCommunityInitialAdapter = new SpinnerAdapter(
						AddPhotosActivity.this, R.layout.spinner, choices);
				spinnerCommunity.setAdapter(spinnerCommunityInitialAdapter);
				spinnerCommunity.setSelection(selection);
				
				spinnerCommunity
						.setOnItemSelectedListener(new OnItemSelectedListener() {

							public void onItemSelected(AdapterView<?> parent,
									View view, int pos, long id) {
								if (pos == mCommunities.length + 1) {
									addCommunity();
								} else {
									communityChosen = pos > 0;
									if (communityChosen) {
										if (communityId != mCommunities[pos - 1]
												.getId()) {
											communityId = mCommunities[pos - 1]
													.getId();
											momentChosen = false;
											spinnerMoment.setSelection(0);
											new HttpGetTask(getMomentsHandler)
													.execute(NetworkManager.hostName
															+ "/api/communities/"
															+ communityId);
										}
									}
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
		new HttpGetTask(getCommunitiesHandler).execute(NetworkManager.hostName
				+ "/api/communities");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.add_photos_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action buttons
		switch (item.getItemId()) {
		case R.id.action_add_photos:
			uploadPhotos();
			return true;
		case android.R.id.home:
			onBackPressed();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void uploadPhotos() {
		if (momentChosen && communityChosen) {
			ArrayList<String> imageUrlsList = imageAdapter.getImageFilenames();
			String[] imageUrls = imageUrlsList.toArray(new String[imageUrlsList
					.size()]);
			final HttpTaskHandler getMomentsHandler = new HttpTaskHandler() {
				@Override
				public void taskSuccessful(String result) {
					try {
						JSONObject photoData = new JSONObject(result);
						int photoId = photoData.getInt("id");
						Intent intent = new Intent(AddPhotosActivity.this,
								PhotoActivity.class);
						intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
								| Intent.FLAG_ACTIVITY_CLEAR_TASK);
						intent.putExtra("moment_id", momentId);
						intent.putExtra("community_id", communityId);
						intent.putExtra("photo_id", photoId);
						startActivity(intent);
					} catch (JSONException e) {
						Log.e(TAG, "Error parsing JSON");
					}
				}

				@Override
				public void taskFailed(String reason) {
					Log.e(TAG, "Error within POST IMAGE request: " + reason);
				}
			};

			new HttpPostImageTask(getMomentsHandler, NetworkManager.hostName
					+ "/api/moments/" + momentId + "/photos/",
					AddPhotosActivity.this).execute(imageUrls);
		} else {
			int red = android.R.color.holo_red_light;
			Style CustomAlert = new Style.Builder().setDuration(2000)
					.setHeight(LayoutParams.WRAP_CONTENT).setTextSize(16)
					.setBackgroundColor(red).setPaddingInPixels(26).build();
			Crouton.makeText(this, "Please, choose community and moment",
					CustomAlert).show();
		}
	}

	private void addCommunity() {
		Intent intent = new Intent(AddPhotosActivity.this,
				AddCommunityActivity.class);
		startActivityForResult(intent, CODE_ADD_COMMUNITY);
	}

	private void addMoment() {
		Intent intent = new Intent(AddPhotosActivity.this,
				AddMomentActivity.class);
		intent.putExtra("community_id", communityId);
		startActivityForResult(intent, CODE_ADD_MOMENT);
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Intent intent = getIntent();
		if (data.hasExtra("moment_id")){
			intent.putExtra("moment_id", data.getIntExtra("moment_id", 0));
		}
		finish();
		startActivity(intent);
	}

	// This class defines the view for the photo gallery and populates the data
	// structure for holding the
	// selected images
	public class ImageAdapter extends BaseAdapter {

		ArrayList<String> mList;
		LayoutInflater mInflater;
		Context mContext;

		public ImageAdapter(Context context, ArrayList<String> imageList) {
			mContext = context;
			mInflater = LayoutInflater.from(mContext);
			mList = imageList;
		}

		public ArrayList<String> getImageFilenames() {
			return this.mList;
		}

		@Override
		public int getCount() {
			return mList.size();
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.item_add_photos,
						parent, false);
			}

			final ImageView imageView = (ImageView) convertView
					.findViewById(R.id.imageView1);
			final ImageView checkbox = (ImageView) convertView
					.findViewById(R.id.checkBox);

			imageView.setScaleType(ScaleType.CENTER_CROP);
			imageLoader.displayImage("file://" + mList.get(position),
					imageView, options, new SimpleImageLoadingListener() {
						@Override
						public void onLoadingComplete(Bitmap loadedImage) {
							Animation anim = AnimationUtils.loadAnimation(
									AddPhotosActivity.this, R.anim.fade_in);
							imageView.setAnimation(anim);
							anim.start();
						}
					});

			options = new DisplayImageOptions.Builder().cacheInMemory()
					.cacheOnDisc().build();

			checkbox.setTag(position);
			checkbox.setOnClickListener(mOnClickListener);

			return convertView;
		}

		OnClickListener mOnClickListener = new OnClickListener() {

			@Override
			public void onClick(View v) {
				int position = (Integer) v.getTag();
				mList.remove(position);
				notifyDataSetChanged();
				Intent intent = new Intent();
				intent.putExtra("selected_photos", mList);
				setResult(RESULT_CANCELED, intent);
				if (mList.size() == 0) {
					onBackPressed();
				}
			}
		};
	}

	private class SpinnerAdapter extends ArrayAdapter<String> {

		public SpinnerAdapter(Context context, int textViewResourceId,
				String[] objects) {
			super(context, textViewResourceId, objects);
		}

		@Override
		public View getDropDownView(int position, View convertView,
				ViewGroup parent) {
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.spinner_dropdown,
						parent, false);
			}
			((TextView) convertView).setText(getItem(position));

			// Add plus to last item
			if (position > 0 && position == getCount() - 1) {
				((TextView) convertView)
						.setCompoundDrawablesWithIntrinsicBounds(
								R.drawable.plus_green, 0, 0, 0);
				((TextView) convertView).setCompoundDrawablePadding(10);

			} else {
				((TextView) convertView)
						.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
			}
			return convertView;
		}

	}

}