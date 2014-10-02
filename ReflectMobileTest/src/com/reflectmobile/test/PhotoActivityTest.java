package com.reflectmobile.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Intent;
import android.os.Environment;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;

import com.reflectmobile.activity.PhotoActivity;
import com.robotium.solo.Solo;

public class PhotoActivityTest extends
ActivityInstrumentationTestCase2<PhotoActivity> {
     
	private PhotoActivity photoActivity;
	private Solo solo;
	
	public PhotoActivityTest() {
		super(PhotoActivity.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	    setSolo(new Solo(getInstrumentation(),getActivity()));
		photoActivity = getActivity();
		
	}
	
	public void testPreconditions() {
		assertNotNull("Photo Activity is null", photoActivity);
	}

	public Solo getSolo() {
		return solo;
	}

	public void setSolo(Solo solo) {
		this.solo = solo;
	}
	
	public void testPhoto(){
		 solo.assertMemoryNotLow();
		/*assertNotNull("Intent should have triggered after button press",
		        triggeredIntent);
		    String data = triggeredIntent.getExtras().getString("result");
		    assertEquals("Incorrect result data passed via the intent",
		        "Testing Text", data);*/
	}
	
	public void testExternalSDCard(){
        try {
            File sdCard = Environment.getExternalStorageDirectory();
            assertNotNull("SD Card mounted", sdCard);
            Log.d("can write", String.valueOf(sdCard.canWrite()));
            Log.d("ExternalStorageState", Environment.getExternalStorageState());
            File file = new File(sdCard, "VisitedScreen.temp");
            //file.createNewFile();
            FileOutputStream f = new FileOutputStream(file);
             byte[] buf = "Hello".getBytes();
             f.write(buf);
             f.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
	
	@Override
	protected void tearDown() throws Exception{
	solo.finishOpenedActivities();
	}

	
	
}
