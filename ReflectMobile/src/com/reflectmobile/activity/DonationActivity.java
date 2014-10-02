package com.reflectmobile.activity;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Shader.TileMode;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.reflectmobile.R;


public class DonationActivity extends BaseActivity {
	

	//@SuppressWarnings("deprecation")

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		hasNavigationDrawer = false;
		setContentView(R.layout.activity_donation);
		super.onCreate(savedInstanceState);

		// Modify action bar title
		int titleId = getResources().getIdentifier("action_bar_title", "id",
				"android");
		TextView title = (TextView) findViewById(titleId);
		title.setTextColor(getResources().getColor(R.color.yellow));
		title.setTypeface(Typeface.createFromAsset(getAssets(),
				"fonts/RobotoCondensed-Regular.ttf"));
		
		ImageView mImageView = (ImageView) findViewById(R.id.donationView);
		Bitmap bitmap= ((BitmapDrawable)mImageView.getDrawable()).getBitmap();
		
	    
		Bitmap bitmap1 = bitmap.copy(Bitmap.Config.ARGB_8888, true);
	    for(int x = bitmap1.getWidth() -1; x>=0;x--)
	        for(int y= bitmap1.getHeight()-1;y>= 0.75 * bitmap1.getHeight();y--)
	                bitmap1.setPixel(x, y, Color.DKGRAY);
	    
		Bitmap circleBitmap2 = Bitmap.createBitmap(bitmap1.getWidth(), bitmap1.getHeight(), Bitmap.Config.ARGB_8888);
		BitmapShader shader = new BitmapShader (bitmap1,  TileMode.CLAMP, TileMode.CLAMP);
			Paint paint = new Paint();
			paint.setShader(shader);
			
		Canvas c = new Canvas(circleBitmap2);
		c.drawCircle(circleBitmap2.getWidth()/2, circleBitmap2.getHeight()/2, circleBitmap2.getWidth()/2, paint);
	    mImageView.setImageBitmap(circleBitmap2);
	     
	  
		
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.donation_menu, menu);
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
		
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onBackPressed() {
		Intent intent = new Intent(DonationActivity.this,
				CommunitiesActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_CLEAR_TASK);
		startActivity(intent);
		super.onBackPressed();
	}
	
	
}