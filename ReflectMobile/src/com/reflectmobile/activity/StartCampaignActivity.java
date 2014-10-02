package com.reflectmobile.activity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import com.reflectmobile.R;

import com.reflectmobile.view.ColorPickerDialog;
import android.app.DatePickerDialog;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

public class StartCampaignActivity extends BaseActivity implements ColorPickerDialog.OnColorChangedListener  {
	
	@SuppressWarnings("unused")
	private Menu menu;
	private Paint mPaint;
    boolean start = false;
    boolean end = false;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		hasNavigationDrawer = false;
		setContentView(R.layout.activity_startcampaign);
		super.onCreate(savedInstanceState);

		// Modify action bar title
		int titleId = getResources().getIdentifier("action_bar_title", "id",
				"android");
		TextView title = (TextView) findViewById(titleId);
		title.setTextColor(getResources().getColor(R.color.yellow));
		title.setTypeface(Typeface.createFromAsset(getAssets(),
				"fonts/RobotoCondensed-Regular.ttf"));
		
		final EditText campaignStart = (EditText) findViewById(R.id.startID);
		
		final EditText campaignEnd = (EditText) findViewById(R.id.endID);
		final Calendar myCalendar = Calendar.getInstance();
		
		final Spinner spinner = (Spinner) findViewById(R.id.campaign);
		String [] choices = new String[5];
		for (int count = 0; count < 5; count++) {
			choices[count] = "Sports Event " + (count+1);
		}

		final ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(
				StartCampaignActivity.this, R.layout.spinner, choices);
		spinnerArrayAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(spinnerArrayAdapter);

		final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

		    @Override
		    public void onDateSet(DatePicker view, int year, int monthOfYear,
		            int dayOfMonth) {
		        myCalendar.set(Calendar.YEAR, year);
		        myCalendar.set(Calendar.MONTH, monthOfYear);
		        myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
		        if(start == true) {
		        	
		        	updateLabel(campaignStart,myCalendar);
		        }
		        if(end == true) {
		        	
		        	updateLabel(campaignEnd,myCalendar);
		        }
		    }
		};

		   campaignStart.setOnClickListener(new OnClickListener() {

		        @Override
		        public void onClick(View v) {
		     
		            new DatePickerDialog(StartCampaignActivity.this, date, myCalendar
		                    .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
		                    myCalendar.get(Calendar.DAY_OF_MONTH)).show();
		           start = true;
		           end = false;
		        }
		        
		    });
            
		   campaignEnd.setOnClickListener(new OnClickListener() {

		        @Override
		        public void onClick(View v) {
		           
		            new DatePickerDialog(StartCampaignActivity.this, date, myCalendar
		                    .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
		                    myCalendar.get(Calendar.DAY_OF_MONTH)).show();
		          end = true;
		          start = false;
		        }
		    });
		 
		   ImageButton b= (ImageButton) findViewById(R.id.add_color);
		   b.setOnClickListener(new OnClickListener()
		   {
		            public void onClick(View v)
		             {
		               mPaint = new Paint();
		               new ColorPickerDialog(StartCampaignActivity.this, StartCampaignActivity.this, mPaint.getColor()).show();
		             }

				
		   }); 
		   
	}
	
	protected void updateLabel(EditText campaignName, Calendar myCalendar) {
	
		    String myFormat = "dd MMM, yyyy"; 
		    SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

		    campaignName.setText(sdf.format(myCalendar.getTime()));
		    
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		this.menu = menu;
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.add_campaign_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public void colorChanged(int color) {
		
	}
	
}

