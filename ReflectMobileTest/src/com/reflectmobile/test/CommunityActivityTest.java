package com.reflectmobile.test;

import android.test.ActivityInstrumentationTestCase2;

import com.reflectmobile.activity.CommunityActivity;

public class CommunityActivityTest extends
ActivityInstrumentationTestCase2<CommunityActivity> {
     
	private CommunityActivity communityActivity;
	
	
	public CommunityActivityTest() {
		super(CommunityActivity.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		communityActivity = getActivity();
		
	}
	
	public void testPreconditions() {
		assertNotNull("Community Activity is null", communityActivity);
	}
	
	
}
