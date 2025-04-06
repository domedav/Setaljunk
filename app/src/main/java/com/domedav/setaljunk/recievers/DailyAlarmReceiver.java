package com.domedav.setaljunk.recievers;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.domedav.setaljunk.workers.NotificationHelper;
import com.domedav.setaljunk.workers.NotificationWorkerScheduler;

public class DailyAlarmReceiver extends BroadcastReceiver {
	private static final String TAG = "DailyAlarmReceiver";
	
	@SuppressLint("ScheduleExactAlarm")
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(TAG, "onReceive: notifying user, and rescheduling");
		NotificationHelper notificationHelper = new NotificationHelper(context);
		notificationHelper.showNotification(context);
		
		NotificationWorkerScheduler.scheduleDailyWork(context);
	}
}