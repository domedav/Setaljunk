package com.domedav.setaljunk.workers;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.domedav.setaljunk.R;

public class NotificationHelper {
	private static final String CHANNEL_ID = "daily_notification_channel";
	private static final String TAG = "NavigationStepsCounterService";
	private static final int NOTIFICATION_ID = 1002;
	
	public NotificationHelper(Context context) {
		createNotificationChannel(context);
	}
	
	private void createNotificationChannel(Context context) {
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
			NotificationChannel channel = new NotificationChannel(
					CHANNEL_ID,
					context.getString(R.string.daily_notification_text),
					NotificationManager.IMPORTANCE_HIGH
			);
			NotificationManager manager = context.getSystemService(NotificationManager.class);
			if (manager != null) {
				manager.createNotificationChannel(channel);
			}
		}
		Log.i(TAG, "createNotificationChannel: notification channel created");
	}
	
	public void showNotification(Context context) {
		NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
				.setContentTitle(context.getString(R.string.daily_notification_text))
				.setContentText(context.getString(R.string.daily_notification_textsub))
				.setSmallIcon(R.drawable.bootlogofilled)
				.setPriority(NotificationCompat.PRIORITY_HIGH)
				.setAutoCancel(true);
		
		NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, builder.build());
	}
}
