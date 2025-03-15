package com.domedav.setaljunk;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.domedav.setaljunk.permissions.AppPermissions;
import com.domedav.setaljunk.sharedpreferences.AppDataStore;

public class MainActivity extends AppCompatActivity {
	
	private static final String TAG = "MainActivity";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		EdgeToEdge.enable(this);
		setContentView(R.layout.activity_main);
		ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
			Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
			v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
			return insets;
		});
		
		// Initialize app data store
		AppDataStore.initialize(getApplicationContext());
		
		var hasSetup = AppDataStore.getData(AppDataStore.SetupPrefsKeys.STOREKEY, AppDataStore.SetupPrefsKeys.DATAKEY_HAS_SETUP, false);
		Log.i(TAG, "onCreate: hasSetup: " + hasSetup);
		
		if(!hasSetup || (!AppPermissions.hasPermission(this, AppPermissions.LOCATION) || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !AppPermissions.hasPermission(this, AppPermissions.ACTIVITY_SENSOR)))){ // User needs to grant us the permissions to be able to use the app
			AppDataStore.setData(AppDataStore.SetupPrefsKeys.STOREKEY, AppDataStore.SetupPrefsKeys.DATAKEY_HAS_SETUP, false);
			Intent intent = new Intent(this, SetupActivity.class);
			startActivity(intent); // launch the setup activity, and close this activity
			finish();
			return;
		}
	}
}