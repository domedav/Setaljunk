package com.domedav.setaljunk.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.domedav.setaljunk.R;
import com.domedav.setaljunk.sharedpreferences.AppDataStore;

public class NavigationStepsCounterService extends Service implements SensorEventListener {
	private static final String TAG = "NavigationStepsCounterService";
	private static final int STEP_SERVICE_ID = 101;
	private static final int NOTIFICATION_ID = 1001;
	private static final String CHANNEL_ID = "step_counter_channel";
	
	private SensorManager _sensorManager;
	
	private final IBinder _binder = new NavigationStepsBinder();
	
	public class NavigationStepsBinder extends Binder {
		public NavigationStepsCounterService getService() {
			return NavigationStepsCounterService.this;
		}
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		initializeSensor();
		createNotificationChannel();
		startForegroundService();
	}
	
	private void initializeSensor() {
		_sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		Sensor stepSensor = _sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
		if (stepSensor != null) {
			_sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI);
		}
		Log.i(TAG, "initializeSensor: Initilaized sensor!");
	}
	
	private void createNotificationChannel() {
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
			NotificationChannel channel = new NotificationChannel(
					CHANNEL_ID,
					getString(R.string.step_service_notification_text),
					NotificationManager.IMPORTANCE_LOW
			);
			NotificationManager manager = getSystemService(NotificationManager.class);
			if (manager != null) {
				manager.createNotificationChannel(channel);
			}
		}
		Log.i(TAG, "createNotificationChannel: notification channel created");
	}
	
	private void startForegroundService() {
		Notification notification = buildNotification();
		startForeground(STEP_SERVICE_ID, notification);
		Log.i(TAG, "startForegroundService: foreground service started");
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return _binder;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		if (_sensorManager != null) {
			_sensorManager.unregisterListener(this);
		}
		Log.i(TAG, "onDestroy: step sensor service stopped");
	}
	
	@NonNull
	private Notification buildNotification() {
		return new NotificationCompat.Builder(this, CHANNEL_ID)
				.setContentTitle(getString(R.string.step_service_notification_text))
				.setSmallIcon(R.drawable.bootlogofilled)
				.setPriority(NotificationCompat.PRIORITY_LOW)
				.build();
	}
	
	@Override
	public void onSensorChanged(@NonNull SensorEvent event) {
		if (event.sensor.getType() != Sensor.TYPE_STEP_COUNTER){
			return;
		}
		var currentSteps = event.values[0];
		var lastSteps = AppDataStore.getData(AppDataStore.StepServiceKeys.STOREKEY, AppDataStore.StepServiceKeys.DATAKEY_SENSOR_STEPS, currentSteps);
		
		Log.i(TAG, "onSensorChanged: currentSteps: " + currentSteps);
		
		if(currentSteps < lastSteps){
			lastSteps = 0f;
		}
		AppDataStore.setData(AppDataStore.StepServiceKeys.STOREKEY, AppDataStore.StepServiceKeys.DATAKEY_SENSOR_STEPS, currentSteps);
		
		var isNavigating = AppDataStore.getData(AppDataStore.MainPrefsKeys.STOREKEY, AppDataStore.MainPrefsKeys.DATAKEY_IS_NAVIGATING, false);
		if(!isNavigating){
			return;
		}
		
		var newSteps = currentSteps - lastSteps;
		var oldSteps = AppDataStore.getData(AppDataStore.StatsPrefsKeys.STOREKEY, AppDataStore.StatsPrefsKeys.DATAKEY_TOTAL_STEPS, 0f);
		AppDataStore.setData(AppDataStore.StatsPrefsKeys.STOREKEY, AppDataStore.StatsPrefsKeys.DATAKEY_TOTAL_STEPS, oldSteps + newSteps);
	}
	
	@Override
	public void onAccuracyChanged(Sensor sensor, int i) {
	
	}
}