package com.domedav.setaljunk.workers;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.icu.util.Calendar;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;

import com.domedav.setaljunk.recievers.DailyAlarmReceiver;

public class NotificationWorkerScheduler {
	private static final int REQUEST_CODE = 1002;
	
	@RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
	public static void scheduleDailyWork(@NonNull Context context){
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		
		Intent intent = new Intent(context, DailyAlarmReceiver.class);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(
				context,
				REQUEST_CODE,
				intent,
				PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
		);
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, 15);
		calendar.set(Calendar.MINUTE, 30);
		calendar.set(Calendar.SECOND, 0);
		
		if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
			calendar.add(Calendar.DAY_OF_YEAR, 1);
		}
		
		if (alarmManager != null) {
			alarmManager.setExactAndAllowWhileIdle(
					AlarmManager.RTC_WAKEUP,
					calendar.getTimeInMillis(),
					pendingIntent
			);
		}
	}
}
