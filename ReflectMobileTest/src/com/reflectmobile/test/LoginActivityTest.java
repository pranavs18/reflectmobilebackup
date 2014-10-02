package com.reflectmobile.test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.reflectmobile.activity.LoginActivity;
import com.reflectmobile.utility.NetworkManager.HttpPostTask;
import com.reflectmobile.utility.NetworkManager.HttpGetTask;
import com.reflectmobile.utility.NetworkManager.HttpTaskHandler;

import com.reflectmobile.R;

import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;

import android.widget.Button;

public class LoginActivityTest extends
		ActivityInstrumentationTestCase2<LoginActivity> {

	private LoginActivity loginActivity;
	private Button button;

	public LoginActivityTest() {
		super(LoginActivity.class);
	}

	/*@Override
	protected void setUp() throws Exception {
		super.setUp();
		loginActivity = getActivity();
		button = (Button) loginActivity
				.findViewById(R.id.button_log_in_facebook);
	}*/

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		loginActivity = getActivity();
		button = (Button) loginActivity
				.findViewById(R.id.button_log_in_google);
	}
	public void testPreconditions() {
		assertNotNull("Login Activity is null", loginActivity);
		assertNotNull("Button is null", button);
	}

	final CountDownLatch signalPost = new CountDownLatch(1);
	final CountDownLatch signalGet = new CountDownLatch(1);
	
	public void testRequests() throws Throwable {
		runTestOnUiThread(new Runnable() { 
            @Override 
            public void run() { 
        		HttpTaskHandler httpPostTaskHandler = new HttpTaskHandler() {

        			@Override
        			public void taskSuccessful(String result) {
        				Log.d("POST", result);
        				signalPost.countDown();
        			}

        			@Override
        			public void taskFailed(String reason) {
        				Log.e("POST", "Error within POST request: " + reason);
        				signalPost.countDown();
        			}
        		};

        		String payload = "{\"user_data\":{\"uid\":\"101913420909954842982\",\"token\":\"ya29.KgC0ZSfXun6IRyMAAABB6UwLVnz9IgYqbJzlxzfwC0Dt7dgN6YZsscNtcGjEyQjIY3y8DLUsvNmWURtu7Cw\",\"expires_in\":3600,\"first_name\":\"Pranav\",\"last_name\":\"Saxena\",\"email\":\"psbits@gmail.com\",\"provider\":\"google\"},\"_utf8\":\"\u2603\"}";

        		new HttpPostTask(httpPostTaskHandler, payload)
        				.execute("http://rewyndr.truefitdemo.com/api/authentication/login");            } 
        });
		
		signalPost.await(30, TimeUnit.SECONDS);

		runTestOnUiThread(new Runnable() { 
            @Override 
            public void run() { 
        		HttpTaskHandler httpGetTaskHandler = new HttpTaskHandler() {

        			@Override
        			public void taskSuccessful(String result) {
        				Log.d("GET", result);
        				signalGet.countDown();
        			}

        			@Override
        			public void taskFailed(String reason) {
        				Log.e("GET", "Error within GET request: " + reason);
        				signalGet.countDown();
        			}
        		};

        		new HttpGetTask(httpGetTaskHandler)
        				.execute("http://rewyndr.truefitdemo.com/api/communities");
            }
        });

		signalGet.await(30, TimeUnit.SECONDS);
	}
}
