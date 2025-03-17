package com.domedav.setaljunk;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatSeekBar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

import com.domedav.setaljunk.location.LocationGeneration;
import com.domedav.setaljunk.permissions.AppPermissions;
import com.domedav.setaljunk.sharedpreferences.AppDataStore;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textview.MaterialTextView;

import java.util.Objects;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, SeekBar.OnSeekBarChangeListener {
	
	private static final String TAG = "MainActivity";
	
	private MapView _mapView;
	private GoogleMap _googleMap;
	private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
	
	private static final int MAP_MIN_DISTANCE = 100;
	private static final int MAP_MAX_DISTANCE = 5000;
	private int _currentMeters = MAP_MIN_DISTANCE;
	
	private AppCompatSeekBar _mapSeekbar;
	
	private AppCompatImageButton _statsButton;
	private AppCompatImageButton _qrButton;
	private AppCompatImageButton _walkStartButton;
	
	private CircularProgressIndicator _progressbar;
	private MaterialTextView _helperTextView;
	
	private Circle _drawnCircle;
	private Location _lastLocation;
	private LatLng _lastLatLng;
	
	@Override
	protected void onResume() {
		super.onResume();
		if(_mapView != null)
			_mapView.onResume();
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		if(_mapView != null)
			_mapView.onStart();
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		if(_mapView != null)
			_mapView.onStop();
	}
	
	@Override
	protected void onPause() {
		if(_mapView != null)
			_mapView.onPause();
		super.onPause();
	}
	
	@Override
	protected void onDestroy() {
		if(_mapView != null)
			_mapView.onDestroy();
		super.onDestroy();
	}
	
	@Override
	public void onLowMemory() {
		super.onLowMemory();
		if(_mapView != null)
			_mapView.onLowMemory();
	}
	
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
		
		// Use recently used value
		_currentMeters = AppDataStore.getData(AppDataStore.MainPrefsKeys.STOREKEY, AppDataStore.MainPrefsKeys.DATAKEY_RECENT_MAP_DISTANCE, 500); // default walk distance is 500m
		
		_mapView = new MapView(getApplicationContext()); // need to initialize like this, so app doesnt crash, when no location permission
		_mapView.setFocusable(false);
		((FrameLayout)findViewById(R.id.map_container)).addView(
				_mapView,
				new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
		);
		
		_mapSeekbar = findViewById(R.id.map_zoom_seekbar);
		_mapSeekbar.setMax(MAP_MAX_DISTANCE - MAP_MIN_DISTANCE);
		_mapSeekbar.setProgress(_currentMeters, true);
		
		_mapSeekbar.setOnSeekBarChangeListener(this);
		
		
		// Initialize the MapView
		Bundle mapViewBundle = null;
		if (savedInstanceState != null) {
			mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
		}
		
		_mapView.onCreate(mapViewBundle);
		_mapView.getMapAsync(this);
		
		_walkStartButton = findViewById(R.id.startnew_button);
		_statsButton = findViewById(R.id.chart_button);
		_qrButton = findViewById(R.id.qrcode_button);
		
		_progressbar = findViewById(R.id.startnew_progress);
		_helperTextView = findViewById(R.id.helper_text_display);
		
		_walkStartButton.setOnClickListener(l -> {
			OnWalkButtonPressed();
		});
		
		_statsButton.setOnClickListener(l -> {
			Intent intent = new Intent(this, StatsActivity.class);
			startActivity(intent);
		});
		
		_qrButton.setOnClickListener(l -> {
			Intent intent = new Intent(this, QRActivity.class);
			startActivity(intent);
		});
	}
	
	@RequiresPermission(allOf = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
	@Override
	public void onMapReady(@NonNull GoogleMap googleMap) {
		this._googleMap = googleMap;
		if (!AppPermissions.hasPermission(this, AppPermissions.LOCATION)) { // no location permission, we cant use the maps
			// user shouldnt reach main activity, without necessary permissions
			return;
		}
		LocationManager locationManager = (LocationManager)getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
		if(locationManager == null){
			return;
		}
		var gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		var internet_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
		
		if(!gps_enabled || !internet_enabled){
			Toast.makeText(getApplicationContext(), getString(R.string.main_gps_missing_services), Toast.LENGTH_LONG).show(); // alert user, that no gps or internet is available
			//TODO: make this a dialog
			return;
		}
		MapsInitializer.initialize(this);
		
		googleMap.setMyLocationEnabled(true); // show current location
		googleMap.setOnMyLocationButtonClickListener(null);
		googleMap.setOnMyLocationClickListener(null);
		
		googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
		googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getApplicationContext(), R.raw.map_style));
		
		googleMap.getUiSettings().setTiltGesturesEnabled(true);   // Enable tilting
		googleMap.getUiSettings().setRotateGesturesEnabled(true); // Enable rotation
		
		// Disable everything else
		googleMap.getUiSettings().setScrollGesturesEnabled(false);
		googleMap.getUiSettings().setZoomGesturesEnabled(false);
		googleMap.getUiSettings().setMyLocationButtonEnabled(false);
		googleMap.getUiSettings().setCompassEnabled(false);
		googleMap.getUiSettings().setIndoorLevelPickerEnabled(false);
		googleMap.getUiSettings().setMapToolbarEnabled(false);
		googleMap.getUiSettings().setScrollGesturesEnabledDuringRotateOrZoom(false);
		googleMap.getUiSettings().setZoomControlsEnabled(false);
		
		//locationManager.getCurrentLocation(Objects.requireNonNull(locationManager.getBestProvider(new Criteria(), false)));
		
		refreshZooming(true);
	}
	
	/// Auto aligns the camera and the camera to match the current preferences
	@RequiresPermission(allOf = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
	private void refreshZooming(boolean refreshLocation){
		if(refreshLocation){
			LocationManager locationManager = (LocationManager)getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
			
			_lastLocation = locationManager.getLastKnownLocation(Objects.requireNonNull(locationManager.getBestProvider(new Criteria(), false))); // get last known location
			_lastLatLng = new LatLng(_lastLocation == null ? 0 : _lastLocation.getLatitude(), _lastLocation == null ? 0 : _lastLocation.getLongitude());
			
			if(_drawnCircle != null){
				_drawnCircle.remove();
				_drawnCircle = null;
			}
		}
		var cameraPosition = new CameraPosition.Builder()
				.target(_lastLatLng)
				.zoom(getGoogleMapsZoomLevel(_lastLatLng.latitude)) // Zoom level
				.tilt(0) // No tilt, topdown view
				.build();
		_googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
		
		if(_drawnCircle == null){
			_drawnCircle = _googleMap.addCircle(new CircleOptions()
					.center(_lastLatLng)
					.radius(_currentMeters)
					.strokeColor(getApplicationContext().getColor(R.color.map_border)) // semi-transparent border color
					.strokeWidth(4f)
					.fillColor(getApplicationContext().getColor(R.color.map_background)) // more transparent fill color
			);
		}
		else{
			_drawnCircle.setRadius(_currentMeters);
		}
		
		Log.d(TAG, "refreshZooming: Refreshed zooming, new distance is: " + _currentMeters);
	}
	
	@Override
	protected void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);
		
		if(_mapView != null){
			Bundle mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
			if (mapViewBundle == null) {
				mapViewBundle = new Bundle();
				outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle);
			}
			
			_mapView.onSaveInstanceState(mapViewBundle);
		}
	}
	
	private float getGoogleMapsZoomLevel(double latitude) {
		float screenWidth = getResources().getDisplayMetrics().widthPixels;
		float metersPerPixel = (2 * _currentMeters) / screenWidth;
		
		final int magicNumber = 27000; // no clue why this, the map just works with this number
		
		// This formula calculates the google map zooming, latitude adjusted
		return (float) Math.log(magicNumber * Math.cos(latitude * Math.PI / 180) / metersPerPixel) / (float) Math.log(2);
	}
	
	@RequiresPermission(allOf = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
	@Override
	public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
		_currentMeters = MAP_MIN_DISTANCE + i;
		Log.d(TAG, "onProgressChanged: Progress changed to: " + i);
		refreshZooming(false);
	}
	
	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
	
	}
	
	@RequiresPermission(allOf = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		AppDataStore.setData(AppDataStore.MainPrefsKeys.STOREKEY, AppDataStore.MainPrefsKeys.DATAKEY_RECENT_MAP_DISTANCE, _currentMeters);
		refreshZooming(true);
	}
	
	private void OnWalkButtonPressed(){
		if(_googleMap == null){
			return;
		}
		_walkStartButton.setVisibility(View.INVISIBLE);
		
		_helperTextView.setText(getString(R.string.main_usage_generation_in_progress));
		
		var animTime = 3500;
		_progressbar.setVisibility(View.VISIBLE);
		_progressbar.setProgress(0, false);
		_progressbar.setMax(100);
		ObjectAnimator progressAnimator = ObjectAnimator.ofInt(_progressbar, "progress", 0, 100); // animate progressbar
		progressAnimator.setDuration(animTime);
		progressAnimator.setInterpolator(new FastOutSlowInInterpolator());
		progressAnimator.start();
		
		var latlng = getRandomLocationInRange();
		
		new Handler(Looper.getMainLooper()).postDelayed(() -> {
			OnGeneratedLocation(latlng);
		}, animTime); // call after time passed
	}
	
	private void OnGeneratedLocation(LatLng latlng){
		_progressbar.setVisibility(View.INVISIBLE);
		_progressbar.setProgress(0, false);
		_googleMap.addMarker(new MarkerOptions()
				.position(latlng)
		);
	}
	
	@NonNull
	private LatLng getRandomLocationInRange(){
		return LocationGeneration.generateRandomNearbyLoction(_lastLatLng, _currentMeters, this);
	}
}