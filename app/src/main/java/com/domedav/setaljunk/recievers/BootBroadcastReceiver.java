package com.domedav.setaljunk.recievers;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import com.domedav.setaljunk.workers.NotificationWorkerScheduler;

import java.util.Objects;

public class BootBroadcastReceiver extends BroadcastReceiver {
	private static final String TAG = "BootBroadcastReceiver";
	
	@SuppressLint("ScheduleExactAlarm")
	@Override
	public void onReceive(Context context, @NonNull Intent intent) {
		if (Objects.equals(intent.getAction(), Intent.ACTION_BOOT_COMPLETED) || Objects.equals(intent.getAction(), Intent.ACTION_MY_PACKAGE_REPLACED)) {
			Log.d(TAG, "onReceive: Action Boot Completed!\nSetting up workers!");
			NotificationWorkerScheduler.scheduleDailyWork(context);
		}
	}
}
