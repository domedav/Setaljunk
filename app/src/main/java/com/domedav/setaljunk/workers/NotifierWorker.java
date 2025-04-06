package com.domedav.setaljunk.workers;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.domedav.setaljunk.R;

public class NotifierWorker extends Worker {
	private static final String CHANNEL_ID = "daily_notification_channel";
	private static final String TAG = "NavigationStepsCounterService";
	private static final int NOTIFICATION_ID = 1001;
	
	public NotifierWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
		super(context, workerParams);
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
	
	public void showNotification(@NonNull Context context) {
		Log.i(TAG, "showNotification: showing notification");
		NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
		
		NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
				.setContentTitle(context.getString(R.string.daily_notification_text))
				.setContentText(context.getString(R.string.daily_notification_textsub))
				.setSmallIcon(R.drawable.bootlogofilled)
				.setPriority(NotificationCompat.PRIORITY_HIGH)
				.setAutoCancel(true);
		
		notificationManager.notify(NOTIFICATION_ID, builder.build());
	}
	
	@NonNull
	@Override
	public Result doWork() {
		createNotificationChannel(getApplicationContext());
		showNotification(getApplicationContext());
		return Result.success();
	}
}
