package com.domedav.setaljunk.permissions;
import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

public class AppPermissions {
	public static String LOCATION = Manifest.permission.ACCESS_FINE_LOCATION; // we need to know exactly where the user is
	public static String CAMERA = Manifest.permission.CAMERA;
	
	@RequiresApi(api = Build.VERSION_CODES.Q)
	public static String ACTIVITY_SENSOR = Manifest.permission.ACTIVITY_RECOGNITION;
	
	public static boolean hasPermission(@NonNull Activity activity, String permission){
		return ContextCompat.checkSelfPermission(activity.getApplicationContext(), permission) == PackageManager.PERMISSION_GRANTED;
	}
	
	public static void requestPermission(@NonNull Activity activity, String permission){
		activity.requestPermissions(new String[]{permission}, 0);
	}
}
