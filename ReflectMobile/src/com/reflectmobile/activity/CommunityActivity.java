package com.reflectmobile.activity;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;
import android.widget.ImageView.ScaleType;
import android.widget.ListView;
import android.widget.TextView;
import com.reflectmobile.R;
import com.reflectmobile.data.Community;
import com.reflectmobile.data.Moment;
import com.reflectmobile.data.Tag;
import com.reflectmobile.utility.NetworkManager;
import com.reflectmobile.utility.NetworkManager.HttpDeleteTask;
import com.reflectmobile.utility.NetworkManager.HttpGetImageTask;
import com.reflectmobile.utility.NetworkManager.HttpGetTask;
import com.reflectmobile.utility.NetworkManager.HttpImageTaskHandler;
import com.reflectmobile.utility.NetworkManager.HttpTaskHandler;

public class CommunityActivity extends BaseActivity {

	private static String TAG = "CommunityActivity";
	private Community community;
	private static int communityId;
	private CardListViewAdapter cardListViewAdapter;

	// Static identifier for receiving camera apps call back
	private static final int CODE_ADD_MOMENT = 101;
	private static final int CODE_ADD_PHOTO = 102;
	public static final int EMAIL_CODE = 103;
    public static final int CODE_ADD_DONATION = 104;
	public static final int MEDIA_TYPE_IMAGE = 1;

	private String inviteLink = "";
	String photoPath;

	// People name list
	NameGridViewAdapter nameGridViewAdapter;
	ArrayList<String> peopleNameList;
	ArrayList<String> selectedPeopleNameList;
	ArrayList<String> tempSelectedPeopleNameList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		hasNavigationDrawer = false;
		// It is important to set content view before calling super.onCreate
		// because BaseActivity uses references to side menu
		setContentView(R.layout.activity_community);
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

		// Retreive data from the web
		final HttpTaskHandler getCommunityHandler = new HttpTaskHandler() {
			@Override
			public void taskSuccessful(String result) {
				Log.d(TAG, result);
				// Parse JSON to the list of communities
				community = Community.getCommunityInfo(result);
				setTitle(community.getName());
				// set card listview
				ListView cardListView = (ListView) findViewById(R.id.listview_community_card_list);
				cardListViewAdapter = new CardListViewAdapter(
						CommunityActivity.this);
				cardListView.setAdapter(cardListViewAdapter);
			}

			@Override
			public void taskFailed(String reason) {
				Log.e(TAG, "Error within GET request: " + reason);
			}
		};

		new HttpGetTask(getCommunityHandler).execute(NetworkManager.hostName
				+ "/api/communities/" + communityId);
		// Retreive data from the web
		final HttpTaskHandler getInviteHandler = new HttpTaskHandler() {
			@Override
			public void taskSuccessful(String result) {
				Log.d(TAG, result);
				inviteLink = inviteLink + result;

			}

			@Override
			public void taskFailed(String reason) {
				Log.e(TAG, "Error within GET request: " + reason);

			}

		};

		new HttpGetTask(getInviteHandler).execute(NetworkManager.hostName
				+ "/api/invites/link/" + communityId);
		Log.d(TAG, NetworkManager.hostName + "/api/invites/link/" + communityId);

		// Generate name list
		GridView nameGridView = (GridView) findViewById(R.id.gridview_community_people_name_list);
		// Dummy name in the community
		peopleNameList = new ArrayList<String>();
		peopleNameList.add("John");
		peopleNameList.add("David");
		peopleNameList.add("Josh");
		selectedPeopleNameList = new ArrayList<String>();
		tempSelectedPeopleNameList = new ArrayList<String>();
		// Make adapter
		nameGridViewAdapter = new NameGridViewAdapter(CommunityActivity.this);
		nameGridView.setAdapter(nameGridViewAdapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.community_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action buttons
		switch (item.getItemId()) {
		case android.R.id.home:
			onBackPressed();
			return true;
			// If the filter item is selected
		case R.id.action_add_photo:
			addPhoto();
			return true;
		case R.id.action_filter_moments:
			filterView();
			return true;
		case R.id.action_add_moment:
			createMoment();
			return true;
		case R.id.action_invite:
			inviteToCommunity();
			return true;
		case R.id.action_campaign:
            startCampaign();
            return false;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onBackPressed() {
		Intent intent = new Intent(CommunityActivity.this,
				CommunitiesActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_CLEAR_TASK);
		startActivity(intent);
		super.onBackPressed();
	}

	private void addPhoto() {
		Intent intent = new Intent(CommunityActivity.this,
				GalleryActivity.class);
		intent.putExtra("community_id", communityId);
		startActivityForResult(intent, CODE_ADD_PHOTO);
	}

	private void addPhotoForMoment(int momentId) {
		Intent intent = new Intent(CommunityActivity.this,
				GalleryActivity.class);
		intent.putExtra("community_id", communityId);
		intent.putExtra("moment_id", momentId);
		startActivityForResult(intent, CODE_ADD_PHOTO);
	}

	public void createMoment() {
		Intent intent = new Intent(CommunityActivity.this,
				AddMomentActivity.class);
		intent.putExtra("community_id", communityId);
		startActivityForResult(intent, CODE_ADD_MOMENT);
	}

	@SuppressLint("InflateParams")
	private void filterView() {
		// Clear on temp selected Name list
		tempSelectedPeopleNameList.clear();
		// Initialze dialog window and set content
		View dialogView = getLayoutInflater().inflate(
				R.layout.dialog_community_filter, null);
		ListView filterListView = (ListView) dialogView
				.findViewById(R.id.listView_community_dialog_filter);

		// Generate and bind adapter
		FilterListViewAdapter adapter = new FilterListViewAdapter(
				CommunityActivity.this, peopleNameList);

		filterListView.setAdapter(adapter);

		// Generate the custom center title view
		TextView title = new TextView(this);
		title.setText(R.string.title_dialog_community_filter);
		title.setPadding(20, 20, 20, 20);
		title.setGravity(Gravity.CENTER);
		title.setTextSize(25);

		// Generate the dialog
		new AlertDialog.Builder(CommunityActivity.this)
				.setView(dialogView)
				.setPositiveButton("Apply",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								selectedPeopleNameList.clear();
								selectedPeopleNameList
										.addAll(tempSelectedPeopleNameList);
								nameGridViewAdapter.notifyDataSetChanged();
							}
						})
				.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
							}
						}).setCustomTitle(title).setCancelable(false).show();
	}

	 public void startCampaign() {
	        Intent intent = new Intent(CommunityActivity.this,
	                StartCampaignActivity.class);
	        intent.putExtra("community_id", communityId);
	        startActivityForResult(intent, CODE_ADD_DONATION);
	    }
	 
	    public void onDonate(View button) {
	        Intent intent = new Intent(CommunityActivity.this,
	                DonationActivity.class);
	        intent.putExtra("community_id", communityId);
	        startActivityForResult(intent, CODE_ADD_DONATION);
	    }
	    
	public void inviteToCommunity() {
		Intent i = new Intent(Intent.ACTION_SEND);
		i.setType("message/rfc822");
		i.putExtra(Intent.EXTRA_EMAIL, new String[] { "" });
		i.putExtra(Intent.EXTRA_SUBJECT, "Rewyndr Community Invite Link");
		Log.d(TAG, "sending the generated invite link " + inviteLink);
		getIntent().addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

		i.putExtra(Intent.EXTRA_TEXT, inviteLink);

		try {
			startActivity(Intent.createChooser(i, "Send mail..."));
		} catch (android.content.ActivityNotFoundException ex) {
			Toast.makeText(CommunityActivity.this,
					"There are no email clients installed.", Toast.LENGTH_SHORT)
					.show();
		}
		Toast.makeText(CommunityActivity.this, "Just one more step.... ",
				Toast.LENGTH_SHORT).show();
		// Log.d(sendInvite.class.getSimpleName(), "Sending Invitation.... ");
		// inviteLink = "";

	}

	// Specific adapter for Community Activity
	private class CardListViewAdapter extends BaseAdapter {
		private LayoutInflater mInflater;
		private Context mContext;
		private Drawable[] mDrawables;

		public CardListViewAdapter(Context context) {
			mDrawables = new Drawable[3 * community.getNumOfMoments()];
			mContext = context;
			mInflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			for (int count = 0; count < community.getNumOfMoments(); count++) {
				final int index = count;

				Moment moment = community.getMoment(count);

				for (int photoCount = 0; photoCount < Math.min(
						moment.getNumOfPhotos(), 3); photoCount++) {
					final int photoIndex = photoCount;
					// Load images asynchronously and notify about their loading
					new HttpGetImageTask(new HttpImageTaskHandler() {
						private int drawableIndex = 3 * index + photoIndex;

						@Override
						public void taskSuccessful(Drawable drawable) {
							mDrawables[drawableIndex] = drawable;
							notifyDataSetChanged();
						}

						@Override
						public void taskFailed(String reason) {
							Log.e(TAG, "Error downloading the image");
						}
					}).execute(moment.getPhoto(photoCount)
							.getImageMediumThumbURL());
				}
			}
		}

		@Override
		public int getCount() {
			return community.getNumOfMoments();
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
			public TextView name;
			public TextView date;
			public Button totalPhoto;
			public ImageView[] photos = new ImageView[3];
			public TextView people;
			public ImageButton menu;
		}

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parentView) {
			// If there is no view to recycle - create a new one
			final Moment moment = community.getMoment(position);

			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.card_moment,
						parentView, false);
				final CardViewHolder holder = new CardViewHolder();
				holder.name = (TextView) convertView
						.findViewById(R.id.text_community_card_community_name);
				holder.date = (TextView) convertView
						.findViewById(R.id.text_community_card_date);
				holder.totalPhoto = (Button) convertView
						.findViewById(R.id.button_community_card_total_photo);
				holder.people = (TextView) convertView
						.findViewById(R.id.text_community_card_people_name);
				holder.photos[0] = (ImageView) convertView
						.findViewById(R.id.card_photo_1);
				holder.photos[1] = (ImageView) convertView
						.findViewById(R.id.card_photo_2);
				holder.photos[2] = (ImageView) convertView
						.findViewById(R.id.card_photo_3);

				holder.totalPhoto.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						int position = (Integer) v.getTag();
						Intent intent = new Intent(mContext,
								MomentActivity.class);
						intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
								| Intent.FLAG_ACTIVITY_CLEAR_TASK);
						intent.putExtra("community_id", communityId);
						intent.putExtra("moment_id",
								community.getMoment(position).getId());
						mContext.startActivity(intent);
					}
				});
				holder.menu = (ImageButton) convertView
						.findViewById(R.id.card_menu);
				holder.menu.setOnClickListener(onCardMenuClicked);
				holder.menu.setTag(position);

				convertView.setTag(holder);
			}

			final CardViewHolder holder = (CardViewHolder) convertView.getTag();

			if (moment.getPeopleList() == null) {
				moment.setPeopleList(new ArrayList<String>());
				for (int count = 0; count < moment.getNumOfPhotos(); count++) {
					final HttpTaskHandler getTagsHandler = new HttpTaskHandler() {
						@Override
						public void taskSuccessful(String result) {
							Log.d(TAG, result);
							JSONArray tagJSONArray;
							try {
								tagJSONArray = new JSONArray(result);
								for (int j = 0; j <= tagJSONArray.length() - 1; j++) {
									Tag tag = Tag.getTagInfo(tagJSONArray
											.getString(j));
									moment.addPerson(tag.getName());
								}
							} catch (JSONException e) {
								Log.e(TAG, "Error parse the tag json");
							}
							notifyDataSetChanged();
						}

						@Override
						public void taskFailed(String reason) {
							Log.e(TAG, "Error downloading the tag");
						}
					};

					new HttpGetTask(getTagsHandler)
							.execute(NetworkManager.hostName + "/api/photos/"
									+ moment.getPhoto(count).getId() + "/tags");
				}
			}

			holder.totalPhoto.setTag(position);
			setPeopleNames(position, holder.people);
			holder.name.setText(moment.getName());

			// set moment date
			holder.date.setText(moment.getDate());
			// set moment photo total
			int numOfPhotos = moment.getNumOfPhotos();
			if (numOfPhotos <= 1) {
				holder.totalPhoto.setText(moment.getNumOfPhotos() + " photo");
			} else {
				holder.totalPhoto.setText(moment.getNumOfPhotos() + " photos");
			}

			// set moment photos
			for (int count = 0; count < 3; count++) {
				if (count < numOfPhotos) {
					holder.photos[count].setImageDrawable(mDrawables[3
							* position + count]);
					holder.photos[count].setScaleType(ScaleType.CENTER_CROP);
					View parent = (View) holder.photos[count].getParent();
					parent.setTag(position);
					holder.photos[count].setTag(count);

					if (numOfPhotos > 3) {
						holder.photos[count]
								.setOnClickListener(new OnClickListener() {
									public void onClick(View v) {
										int position = (Integer) ((View) v
												.getParent()).getTag();
										Intent intent = new Intent(mContext,
												MomentActivity.class);
										intent.putExtra("community_id",
												communityId);
										intent.putExtra("moment_id", community
												.getMoment(position).getId());
										mContext.startActivity(intent);
									}
								});
					} else {
						holder.photos[count]
								.setOnClickListener(new OnClickListener() {
									public void onClick(View v) {
										int position = (Integer) ((View) v
												.getParent()).getTag();
										int count = (Integer) v.getTag();
										Intent intent = new Intent(mContext,
												PhotoActivity.class);
										intent.putExtra("community_id",
												communityId);
										Moment selectedMoment = community
												.getMoment(position);
										intent.putExtra("moment_id",
												selectedMoment.getId());
										intent.putExtra("photo_id",
												selectedMoment.getPhoto(count)
														.getId());

										mContext.startActivity(intent);
									}
								});
					}
				} else if (count == numOfPhotos) {
					holder.photos[count].setImageDrawable(getResources()
							.getDrawable(R.drawable.add_photo_community));
					holder.photos[count].setScaleType(ScaleType.CENTER);
					holder.photos[count]
							.setOnClickListener(new OnClickListener() {
								public void onClick(View v) {
									addPhotoForMoment(community.getMoment(
											position).getId());
								}
							});
				} else {
					holder.photos[count].setImageDrawable(null);
					holder.photos[count].setOnClickListener(null);
				}
			}

			// set moment photo list
			return convertView;
		}

		private void setPeopleNames(int momentId, TextView people) {
			ArrayList<String> tagList = community.getMoment(momentId)
					.getPeopleList();
			if (tagList.size() > 0) {
				if (tagList.size() > 2) {
					String caption = tagList.get(0) + ", " + tagList.get(1)
							+ " and " + (tagList.size() - 2) + " more";
					people.setText(caption);
				} else if (tagList.size() == 2) {
					String caption = tagList.get(0) + " and " + tagList.get(1);
					people.setText(caption);
				} else {
					people.setText(tagList.get(0));
				}
			} else {
				people.setText("No people tagged");
			}
		}
	}

	private class FilterListViewAdapter extends BaseAdapter {
		private ArrayList<String> nameList;
		private LayoutInflater mInflater;
		private Context mContext;

		public FilterListViewAdapter(Context context, ArrayList<String> nameList) {
			this.nameList = nameList;
			this.mContext = context;
			this.mInflater = (LayoutInflater) this.mContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public int getCount() {
			return nameList.size();
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
		private class FilterItemHolder {
			public TextView name;
			public CheckBox checkBox;
		}

		@Override
		public View getView(int position, View view, ViewGroup parentView) {
			if (view == null) {
				view = mInflater.inflate(
						R.layout.item_community_filter_dialog_listview,
						parentView, false);
				final FilterItemHolder holder = new FilterItemHolder();
				holder.name = (TextView) view
						.findViewById(R.id.textView_community_filter_listview_item_name);
				holder.checkBox = (CheckBox) view
						.findViewById(R.id.checkbox_community_filter_listview_item);
				holder.checkBox.setChecked(false);
				view.setTag(holder);
			}

			final FilterItemHolder holder = (FilterItemHolder) view.getTag();
			holder.name.setText(nameList.get(position));
			holder.checkBox
					.setOnCheckedChangeListener(new OnCheckedChangeListener() {

						@Override
						public void onCheckedChanged(CompoundButton buttonView,
								boolean isChecked) {
							if (isChecked) {
								tempSelectedPeopleNameList.add(holder.name
										.getText().toString());
							} else {
								tempSelectedPeopleNameList.remove(holder.name
										.getText().toString());
							}
						}
					});
			return view;
		}
	}

	// Photo app callback function (define how to handle the photo)
	// Currently only add the photo to the gallery
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == CODE_ADD_MOMENT && resultCode == RESULT_OK) {
			Intent intent = getIntent();
			finish();
			startActivity(intent);
		}
	}

	private OnClickListener onCardMenuClicked = new OnClickListener() {

		@Override
		public void onClick(View v) {
			final int position = (Integer) v.getTag();

			// Creating the instance of PopupMenu
			PopupMenu popup = new PopupMenu(CommunityActivity.this, v);
			// Inflating the Popup using xml file
			popup.getMenuInflater().inflate(R.menu.popup_moment,
					popup.getMenu());
			// registering popup with OnMenuItemClickListener
			popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
				public boolean onMenuItemClick(MenuItem item) {
					switch (item.getItemId()) {
					case R.id.action_edit_moment:
						editMoment(position);
						return true;
					case R.id.action_delete_moment:
						deleteMoment(position);
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

	private void editMoment(int position) {
		Intent intent = new Intent(CommunityActivity.this,
				AddMomentActivity.class);
		Moment moment = community.getMoment(position);
		intent.putExtra("name", moment.getName());
		intent.putExtra("community_id", community.getId());
		intent.putExtra("moment_id", moment.getId());

		startActivityForResult(intent, CODE_ADD_MOMENT);
	}

	private void deleteMoment(int position) {
		Moment moment = community.getMoment(position);
		HttpTaskHandler httpDeleteTaskHandler = new HttpTaskHandler() {

			@Override
			public void taskSuccessful(String result) {
				Intent intent = getIntent();
				finish();
				startActivity(intent);
			}

			@Override
			public void taskFailed(String reason) {
				Log.e(TAG, "Error deleting memory");
			}
		};
		new HttpDeleteTask(httpDeleteTaskHandler)
				.execute(NetworkManager.hostName + "/api/moments/"
						+ moment.getId());
	}

	private class NameGridViewAdapter extends BaseAdapter {
		private LayoutInflater mInflater;
		private Context mContext;

		public NameGridViewAdapter(Context context) {
			this.mContext = context;
			this.mInflater = (LayoutInflater) this.mContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public int getCount() {
			return selectedPeopleNameList.size();
		}

		@Override
		public Object getItem(int arg0) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View view, ViewGroup parentView) {
			if (view == null) {
				view = mInflater.inflate(R.layout.item_community_people_name,
						parentView, false);
			}
			final TextView textView = (TextView) view
					.findViewById(R.id.button_community_people_name);
			textView.setText(selectedPeopleNameList.get(position));
			view.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					selectedPeopleNameList
							.remove(textView.getText().toString());
					notifyDataSetChanged();
				}
			});
			return view;
		}

	}
}