package com.domedav.setaljunk.sharedpreferences;

import android.content.Context;
import android.util.Log;

/// Simplifies data management
public class AppDataStore {
	private static final String TAG = "AppDataStore";
	
	public static final class SetupPrefsKeys{
		public static final String STOREKEY = "SetupPrefsKeys_SetupPrefs";
		public static final String DATAKEY_HAS_SETUP = "SetupPrefsKeys_hasSetup";
	}
	
	public static final class MainPrefsKeys{
		public static final String STOREKEY = "MainPrefsKeys_MainPrefs";
		public static final String DATAKEY_RECENT_MAP_DISTANCE = "MainPrefsKeys_recentMapDistance";
		public static final String DATAKEY_IS_NAVIGATING = "MainPrefsKeys_isNavigating";
		public static final String DATAKEY_NAVIGATION_DESTINATION_LATLNG = "MainPrefsKeys_navigationDestinationLatLng";
	}
	
	public static final class StatsPrefsKeys{
		public static final String STOREKEY = "StatsPrefsKeys_StatsPrefs";
		
		public static final String DATAKEY_TOTAL_STEPS = "SetupPrefsKeys_totalSteps";
	}
	
	public static final class QrPrefsKeys{
		public static final String STOREKEY = "QrPrefsKeys_QrPrefs";
		
		public static final String DATAKEY_NAVIGATE_LOCATION = "QrPrefsKeys_navigateLocation";
		public static final String DATAKEY_SUCCESS_SCAN = "QrPrefsKeys_scanSuccess";
	}
	
	public static final class StepServiceKeys{
		public static final String STOREKEY = "StepServiceKeys_StepService";
		
		public static final String DATAKEY_SENSOR_STEPS = "StepServiceKeys_sensorSteps";
	}
	
	static Context _AppContext;
	
	/// Must be called, before use
	public static void initialize(Context appContext){
		_AppContext = appContext;
	}
	
	/// Saves data to the given prefs file, with the given key, saving the given data
	public static <T> void setData(String storeKey, String dataKey, T data){
		if(_AppContext == null){
			Log.e(TAG, "saveData: ", new Exception("AppContext is null"));
			return;
		}
		// Open shared prefs
		var prefs = _AppContext.getSharedPreferences(storeKey, Context.MODE_PRIVATE);
		
		var editor = prefs.edit();
		
		var tType = data.getClass().getName(); // no switch-case, as comparing types are not constant
		if(tType.equals(Integer.class.getName()) || tType.equals(Long.class.getName())){
			editor.putInt(dataKey, (int)data);
		}
		else if(tType.equals(Double.class.getName()) || tType.equals(Float.class.getName())){
			editor.putFloat(dataKey, (float)data);
		}
		else if(tType.equals(Boolean.class.getName())){
			editor.putBoolean(dataKey, (boolean)data);
		}
		else{
			editor.putString(dataKey, data.toString());
		}
		
		editor.apply();
	}
	
	/// Get the data from the preferences
	@SuppressWarnings("unchecked")
	public static <T> T getData(String storeKey, String dataKey, T defaultValue){
		if(_AppContext == null){
			Log.e(TAG, "getData: ", new Exception("AppContext is null"));
			return defaultValue;
		}
		
		// Open shared prefs
		var prefs = _AppContext.getSharedPreferences(storeKey, Context.MODE_PRIVATE);
		
		if(!hasData(storeKey, dataKey)){
			Log.e(TAG, "getData: ", new Exception("No data found with the given key: " + dataKey));
			return defaultValue;
		}
		
		var tType = defaultValue.getClass().getName(); // no switch-case, as comparing types are not constant
		if(tType.equals(Integer.class.getName()) || tType.equals(Long.class.getName())){
			return (T)defaultValue.getClass().cast(prefs.getInt(dataKey, (int)defaultValue));
		}
		else if(tType.equals(Double.class.getName()) || tType.equals(Float.class.getName())){
			return (T)defaultValue.getClass().cast(prefs.getFloat(dataKey, (float)defaultValue));
		}
		else if(tType.equals(Boolean.class.getName())){
			return (T)defaultValue.getClass().cast(prefs.getBoolean(dataKey, (boolean)defaultValue));
		}
		else{
			return (T)defaultValue.getClass().cast(prefs.getString(dataKey, defaultValue.toString()));
		}
	}
	
	/// Check if data exists with the given key
	public static boolean hasData(String storeKey, String dataKey){
		if(_AppContext == null){
			Log.e(TAG, "hasData: ", new Exception("AppContext is null"));
			return false;
		}
		
		// Open shared prefs
		var prefs = _AppContext.getSharedPreferences(storeKey, Context.MODE_PRIVATE);
		return prefs.contains(dataKey);
	}
	
	/// Delete data from the prefs
	public static void removeData(String storeKey, String dataKey){
		if(_AppContext == null){
			Log.e(TAG, "removeData: ", new Exception("AppContext is null"));
			return;
		}
		
		// Open shared prefs
		var prefs = _AppContext.getSharedPreferences(storeKey, Context.MODE_PRIVATE);
		
		var editor = prefs.edit();
		
		editor.remove(dataKey);
		
		editor.apply();
	}
}
