package com.reflectmobile.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;

import com.reflectmobile.R;
import com.reflectmobile.data.Community;
import com.reflectmobile.utility.NetworkManager;
import com.reflectmobile.utility.NetworkManager.HttpGetImageTask;
import com.reflectmobile.utility.NetworkManager.HttpGetTask;
import com.reflectmobile.utility.NetworkManager.HttpImageTaskHandler;
import com.reflectmobile.utility.NetworkManager.HttpTaskHandler;

public class CommunitiesActivity extends BaseActivity {

	private static String TAG = "CommunitiesActivity";

	private Community[] communities;

	private final static int CODE_ADD_COMMUNITY = 101;
	private static final int CODE_ADD_PHOTO = 102;
	private static final int CODE_ADD_DONATION = 103;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// It is important to set content view before calling super.onCreate
		// because BaseActivity uses references to side menu
		setContentView(R.layout.activity_communities);
		super.onCreate(savedInstanceState);

		// Modify action bar title
		int titleId = getResources().getIdentifier("action_bar_title", "id",
				"android");
		TextView title = (TextView) findViewById(titleId);
		title.setTypeface(Typeface.createFromAsset(getAssets(),
				"fonts/RobotoCondensed-Regular.ttf"));

		setTitle("My Communities");

		// Set margin before title
		ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) title
				.getLayoutParams();
		mlp.setMargins(5, 0, 0, 0);

		// Retreive data from the web
		final HttpTaskHandler getCommunitiesHandler = new HttpTaskHandler() {
			@Override
			public void taskSuccessful(String result) {
				// Parse JSON to the list of communities
				communities = Community.getCommunitiesInfo(result);
				GridView parentView = (GridView) findViewById(R.id.parentView);
				parentView
						.setAdapter(new CardAdapter(CommunitiesActivity.this));
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
	public void onBackPressed() {
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.communities_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action buttons
		switch (item.getItemId()) {
		case R.id.action_add_photo:
			addPhoto();
			return true;
		case R.id.action_add_community:
			createCommunity();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void addPhoto() {
		Intent intent = new Intent(CommunitiesActivity.this,
				GalleryActivity.class);
		startActivityForResult(intent, CODE_ADD_PHOTO);
	}

	private void addPhotoToCommunity(int communityId) {
		Intent intent = new Intent(CommunitiesActivity.this,
				GalleryActivity.class);
		intent.putExtra("community_id", communityId);
		startActivityForResult(intent, CODE_ADD_PHOTO);
	}

	public void createCommunity() {
		Intent intent = new Intent(CommunitiesActivity.this,
				AddCommunityActivity.class);
		startActivityForResult(intent, CODE_ADD_COMMUNITY);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == CODE_ADD_COMMUNITY && resultCode == RESULT_OK) {
			Intent intent = getIntent();
			finish();
			startActivity(intent);
		}
	};

	// Specific adapter for Communities Activity
	private class CardAdapter extends BaseAdapter {

		private LayoutInflater mInflater;
		private Context mContext;
		private Drawable[] mDrawables;

		public CardAdapter(Context context) {
			mDrawables = new Drawable[communities.length];
			mContext = context;
			mInflater = (LayoutInflater) mContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			for (int count = 0; count < communities.length; count++) {
				final int index = count;

				// Load images asynchronously and notify about their loading
				new HttpGetImageTask(new HttpImageTaskHandler() {
					private int drawableIndex = index;
					
					@Override
					public void taskSuccessful(Drawable drawable) {
						mDrawables[drawableIndex] = drawable;
						notifyDataSetChanged();
					}

					@Override
					public void taskFailed(String reason) {
						Log.e(TAG, "Error downloading the image");
					}
				}).execute(communities[count].getFirstPhoto());
			}
		}

		@Override
		public int getCount() {
			return communities.length;
		}

		@Override
		public Object getItem(int item) {
			return item;
		}

		@Override
		public long getItemId(int id) {
			return id;
		}

		// Uses common Android ViewHolder pattern
		private class CardViewHolder {
			public ImageView image;
			public TextView text;
			public int position;
			public ImageButton menu;
			public TextView hiddenText;
            public Button donate;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parentView) {
			// If there is no view to recycle - create a new one
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.card_community,
						parentView, false);
				final CardViewHolder holder = new CardViewHolder();
				holder.text = (TextView) convertView
						.findViewById(R.id.card_text);
				holder.hiddenText = (TextView) convertView
						.findViewById(R.id.add_photos);
				holder.image = (ImageView) convertView
						.findViewById(R.id.card_image);
				holder.donate = (Button) convertView.findViewById(R.id.donate_money);
				holder.image.setScaleType(ScaleType.CENTER_CROP);
              
				holder.image.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						int position = ((CardViewHolder) v.getTag()).position;
						if (mDrawables[position] != null) {
							Intent intent = new Intent(mContext,
									CommunityActivity.class);
							intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
									| Intent.FLAG_ACTIVITY_CLEAR_TASK);
							intent.putExtra("community_id",
									communities[position].getId());
							mContext.startActivity(intent);
						} else {
							addPhotoToCommunity(communities[position].getId());
						}
					}
					
					
				});
				holder.menu = (ImageButton) convertView
						.findViewById(R.id.card_menu);
				holder.menu.setOnClickListener(onCardMenuClicked);
				holder.menu.setTag(position);
				holder.image.setTag(holder);
				convertView.setTag(holder);
				
				holder.donate.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						
					        Intent intent = new Intent(CommunitiesActivity.this,
					                DonationActivity.class);
					        startActivityForResult(intent, CODE_ADD_DONATION);
					    
	
					}
					
				});
			}

			final CardViewHolder holder = (CardViewHolder) convertView.getTag();
			holder.position = position;
          
            
			holder.text.setText(communities[position].getName());
			holder.image.setImageDrawable(mDrawables[position]);
			if (mDrawables[position] == null) {
				holder.hiddenText.setTypeface(Typeface.createFromAsset(
						getAssets(), "fonts/RobotoCondensed-Regular.ttf"));
				holder.hiddenText.setVisibility(View.VISIBLE);
			} else {
				holder.hiddenText.setVisibility(View.GONE);
			}

			return convertView;
		}
	}

	private OnClickListener onCardMenuClicked = new OnClickListener() {

		@Override
		public void onClick(View v) {
			final int position = (Integer) v.getTag();

			// Creating the instance of PopupMenu
			PopupMenu popup = new PopupMenu(CommunitiesActivity.this, v);
			// Inflating the Popup using xml file
			popup.getMenuInflater().inflate(R.menu.popup_community,
					popup.getMenu());
			// registering popup with OnMenuItemClickListener
			popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
				public boolean onMenuItemClick(MenuItem item) {
					switch (item.getItemId()) {
					case R.id.action_edit_community:
						editCommunity(position);
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

	private void editCommunity(int position) {
		Intent intent = new Intent(CommunitiesActivity.this,
				AddCommunityActivity.class);
		Community community = communities[position];
		intent.putExtra("network_id", community.getNetworkId());
		intent.putExtra("name", community.getName());
		intent.putExtra("description", community.getDescription());
		intent.putExtra("community_id", community.getId());

		startActivityForResult(intent, CODE_ADD_COMMUNITY);
	}
	
	
}
