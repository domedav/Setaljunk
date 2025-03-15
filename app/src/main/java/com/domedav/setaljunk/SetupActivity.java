package com.domedav.setaljunk;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ViewFlipper;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.domedav.setaljunk.permissions.AppPermissions;
import com.domedav.setaljunk.sharedpreferences.AppDataStore;
import com.google.android.material.button.MaterialButton;
import java.util.Objects;

public class SetupActivity extends AppCompatActivity {
	
	private ViewFlipper _viewFlipper;
	private MaterialButton _nextButton;
	private int _pageCounter;
	
	private LinearLayoutCompat _gpsLayout;
	private MaterialButton _gpsButton;
	private LinearLayoutCompat _cameraLayout;
	private MaterialButton _cameraButton;
	private LinearLayoutCompat _stepsLayout;
	private MaterialButton _stepsButton;
	
	@Override
	public void onBackPressed() {
		if(_pageCounter == 0 || _viewFlipper == null){
			super.onBackPressed();
			return;
		}
		_pageCounter--;
		_viewFlipper.showPrevious();
		if(_pageCounter == 0){
			_nextButton.setVisibility(View.VISIBLE);
			_nextButton.setOnClickListener(l -> {
				_viewFlipper.showNext();
			});
		}
	}
	
	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults, int deviceId) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults, deviceId);
		if(grantResults[0] != PackageManager.PERMISSION_GRANTED){
			return;
		}
		for (var item: permissions) {
			if(Objects.equals(item, AppPermissions.LOCATION)){
				_gpsLayout.setVisibility(View.GONE);
			}
			else if(Objects.equals(item, AppPermissions.CAMERA)){
				_cameraLayout.setVisibility(View.GONE);
			}
			else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
				if(Objects.equals(item, AppPermissions.ACTIVITY_SENSOR)){
					_stepsLayout.setVisibility(View.GONE);
				}
			}
		}
		if(hasAllNeededPerms()){
			_nextButton.setVisibility(View.VISIBLE);
			_nextButton.setOnClickListener(l -> {
				proceedAppIfAllPerms();
			});
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		EdgeToEdge.enable(this);
		setContentView(R.layout.activity_setup);
		ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
			Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
			v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
			return insets;
		});
		
		_viewFlipper = findViewById(R.id.view_flipper);
		_nextButton = findViewById(R.id.next_button);
		
		_gpsLayout = findViewById(R.id.gps_view);
		_stepsLayout = findViewById(R.id.steps_view);
		_cameraLayout = findViewById(R.id.camera_view);
		
		_gpsButton = findViewById(R.id.gps_button);
		_cameraButton = findViewById(R.id.camera_button);
		_stepsButton = findViewById(R.id.steps_button);
		
		_gpsButton.setOnClickListener(l -> AppPermissions.requestPermission(this, AppPermissions.LOCATION));
		_cameraButton.setOnClickListener(l -> AppPermissions.requestPermission(this, AppPermissions.CAMERA));
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
			_stepsButton.setOnClickListener(l -> AppPermissions.requestPermission(this, AppPermissions.ACTIVITY_SENSOR));
		}
		
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
			_stepsLayout.setVisibility(View.GONE); // android 9 or bellow auto grants us this permission
		}
		
		_viewFlipper.setInAnimation(this, android.R.anim.fade_in);
		_viewFlipper.setOutAnimation(this, android.R.anim.fade_out);
		
		_nextButton.setOnClickListener(v -> {
			_viewFlipper.showNext();
			_pageCounter++;
			if(_pageCounter == 1){
				_nextButton.setVisibility(View.INVISIBLE);
				
				if(AppPermissions.hasPermission(this, AppPermissions.LOCATION)){
					_gpsLayout.setVisibility(View.GONE);
				}
				if(AppPermissions.hasPermission(this, AppPermissions.CAMERA)){
					_cameraLayout.setVisibility(View.GONE);
				}
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
					if(AppPermissions.hasPermission(this, AppPermissions.ACTIVITY_SENSOR)){
						_stepsLayout.setVisibility(View.GONE);
					}
				}
			}
		}); // Pressing the next button, should bring the next page
		
		proceedAppIfAllPerms();
	}
	
	private void proceedAppIfAllPerms(){
		if(hasAllNeededPerms()){
			AppDataStore.setData(AppDataStore.SetupPrefsKeys.STOREKEY, AppDataStore.SetupPrefsKeys.DATAKEY_HAS_SETUP, true);
			Intent intent = new Intent(this, MainActivity.class);
			startActivity(intent);
			finish();
		}
	}
	
	private boolean hasAllNeededPerms(){
		return AppPermissions.hasPermission(this, AppPermissions.LOCATION) && (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && AppPermissions.hasPermission(this, AppPermissions.ACTIVITY_SENSOR));
	}
}