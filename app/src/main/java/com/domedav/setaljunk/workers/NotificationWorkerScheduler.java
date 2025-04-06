package com.domedav.setaljunk.workers;

import android.Manifest;
import android.content.Context;
import android.icu.util.Calendar;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.time.Duration;
import java.util.concurrent.TimeUnit;


public class NotificationWorkerScheduler {
	private static final int REQUEST_CODE = 1002;
	
	public static void scheduleDailyWork(@NonNull Context context){
		var currentTime = Calendar.getInstance();
		
		var targetTime = Calendar.getInstance();
		targetTime.set(Calendar.HOUR_OF_DAY, 15);
		targetTime.set(Calendar.MINUTE, 30);
		targetTime.set(Calendar.SECOND, 0);
		
		if(currentTime.after(targetTime)){
			targetTime.add(Calendar.DATE, 1);
		}
		
		var delayMillis = targetTime.getTimeInMillis() - currentTime.getTimeInMillis();
		
		var request = new PeriodicWorkRequest.Builder(NotifierWorker.class, 1, TimeUnit.DAYS);
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			request.setInitialDelay(Duration.ofMillis(delayMillis));
		}
		
		WorkManager.getInstance(context).cancelAllWork();
		WorkManager.getInstance(context).enqueue(request.build());
	}
}
