package com.reflectmobile.test;

import android.test.ActivityInstrumentationTestCase2;

import com.reflectmobile.activity.MomentActivity;

public class MomentActivityTest extends
ActivityInstrumentationTestCase2<MomentActivity> {
     
	private MomentActivity momentActivity;
	
	
	public MomentActivityTest() {
		super(MomentActivity.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		momentActivity = getActivity();
		
	}
	
	public void testPreconditions() {
		assertNotNull("Moment Activity is null", momentActivity);
	}
	
	
}
