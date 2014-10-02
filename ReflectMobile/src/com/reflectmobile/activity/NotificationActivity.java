package com.reflectmobile.activity;

import java.util.Random;

import com.reflectmobile.R;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
 
public class NotificationActivity extends BaseActivity implements OnClickListener{
 
	private Button b;
	public static int ID = 0;
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	hasNavigationDrawer = false;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        b=(Button)findViewById(R.id.button1);
        b.setOnClickListener(this);
        
    }
 
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_notification, menu);
        return true;
    }
 
	@SuppressWarnings("deprecation")
	@Override
	public void onClick(View v) {
		Intent intent = new Intent();
		PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);
		Random r = new Random();
		String content = "";
		int num = r.nextInt(4);
		switch(num){
		case 0:
			content = "Photo Tagged";
			break;
		case 1:
			content = "Message Received";
			break;
		case 2:
			content = "Invitation Received";
			break;
		case 3:
			content = "Donation Successful";
		    break;
		case 4:
			content = "";
			break;
		}
		Notification noti = new Notification.Builder(this)
		.setTicker("Reflect Mobile")
		.setContentTitle("Reflect Mobile")
		.setContentText(content)
		.setSmallIcon(R.drawable.ic_launcher)
		.setContentIntent(pIntent).getNotification();
		noti.flags=Notification.FLAG_AUTO_CANCEL;
		NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		notificationManager.notify(ID, noti);
		ID++;
	}
}
 