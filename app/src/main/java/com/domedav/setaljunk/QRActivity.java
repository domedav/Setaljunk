package com.domedav.setaljunk;

import android.Manifest;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.ImageView;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.domedav.setaljunk.permissions.AppPermissions;
import com.domedav.setaljunk.qrcode.QrCodeGenerator;
import com.domedav.setaljunk.sharedpreferences.AppDataStore;
import com.domedav.setaljunk.views.SquareSurfaceView;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import org.jetbrains.annotations.Contract;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class QRActivity extends AppCompatActivity {
	private static final String TAG = "QRActivity";
	
	private AppCompatImageButton _navigateBack;
	private ImageView _qrLayout;
	private SquareSurfaceView _surfaceView;
	
	private LinearLayoutCompat _myQrLayout;
	private LinearLayoutCompat _otherQrLayout;
	private LinearLayoutCompat _rootLayout;
	
	private CameraDevice _cameraDevice;
	private CaptureRequest.Builder _captureRequestBuilder;
	private CaptureRequest.Builder _captureRequestBuilderDisplayer;
	private ImageReader _cameraImageReader;
	
	private HandlerThread _cameraCaptureThread;
	private Handler _cameraCaptureHandler;
	private Runnable _cameraCaptureRunnable;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		EdgeToEdge.enable(this);
		setContentView(R.layout.activity_qractivity);
		ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
			Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
			v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
			return insets;
		});
		
		_rootLayout = findViewById(R.id.main);
		
		_navigateBack = findViewById(R.id.navigate_back_button);
		_qrLayout = findViewById(R.id.qrcode_holder);
		_myQrLayout = findViewById(R.id.layout_display_qr);
		_otherQrLayout = findViewById(R.id.layout_scan_qr);
		
		_myQrLayout.setVisibility(View.GONE);
		_otherQrLayout.setVisibility(View.GONE);
		
		_surfaceView = findViewById(R.id.live_camera_view);
		
		_navigateBack.setOnClickListener(l -> finish()); // if back is pressed, close this activity
		
		var isNavigating = AppDataStore.getData(AppDataStore.MainPrefsKeys.STOREKEY, AppDataStore.MainPrefsKeys.DATAKEY_IS_NAVIGATING, false);
		if(isNavigating){
			// display a qr code that users can scan
			_qrLayout.setImageBitmap(QrCodeGenerator.generateQRCode(this,
				AppDataStore.getData(AppDataStore.MainPrefsKeys.STOREKEY, AppDataStore.MainPrefsKeys.DATAKEY_NAVIGATION_DESTINATION_LATLNG, "0.0;0.0")
			));
			_myQrLayout.setVisibility(View.VISIBLE);
			_otherQrLayout.setVisibility(View.GONE);
		}
		else{
			// let users scan qr code
			if(!AppPermissions.hasPermission(this, AppPermissions.CAMERA)){
				showPopupMenuYesNo(getString(R.string.qr_popup_no_camera_header), getString(R.string.qr_popup_no_camera_description), getString(R.string.qr_popup_no_camera_no), getString(R.string.qr_popup_no_camera_yes));
				return;
			}
			addQRScannerView();
		}
	}
	
	private void addQRScannerView(){
		_myQrLayout.setVisibility(View.GONE);
		_otherQrLayout.setVisibility(View.VISIBLE);
		
		SurfaceHolder surfaceHolder = _surfaceView.getHolder();
		var activity = this;
		surfaceHolder.addCallback(new SurfaceHolder.Callback() {
			@RequiresPermission(Manifest.permission.CAMERA)
			@Override
			public void surfaceCreated(@NonNull SurfaceHolder holder) {
				if(AppPermissions.hasPermission(activity, AppPermissions.CAMERA)){
					openCamera(holder);
				}
			}
			
			@Override
			public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
			}
			
			@Override
			public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
				closeCamera();
			}
		});
	}
	
	@RequiresPermission(Manifest.permission.CAMERA)
	private void openCamera(SurfaceHolder holder) {
		CameraManager cameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);
		try {
			String cameraId = cameraManager.getCameraIdList()[0]; // Use the first available camera (usually rear-facing)
			cameraManager.openCamera(cameraId, new CameraDevice.StateCallback() {
				@Override
				public void onOpened(@NonNull CameraDevice camera) {
					_cameraDevice = camera;
					startCameraPreview(holder);
				}
				
				@Override
				public void onDisconnected(@NonNull CameraDevice camera) {
					closeCamera();
				}
				
				@Override
				public void onError(@NonNull CameraDevice camera, int error) {
					closeCamera();
					Log.e(TAG, "onError: camera closed with error: " + error);
				}
			}, null);
		} catch (CameraAccessException e) {
			// if some error occures, make user add camera perms
			showPopupMenuYesNo(getString(R.string.qr_popup_no_camera_header), getString(R.string.qr_popup_no_camera_description), getString(R.string.qr_popup_no_camera_no), getString(R.string.qr_popup_no_camera_yes));
			Log.e(TAG, "openCamera: Failed to open camera", e);
		}
	}
	
	private void startCameraPreview(@NonNull SurfaceHolder holder) {
		try {
			Surface surface = holder.getSurface();
			_captureRequestBuilder = _cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
			
			_captureRequestBuilderDisplayer = _cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
			_captureRequestBuilderDisplayer.addTarget(surface);
			
			LinearLayoutCompat.LayoutParams params = getSurfaceLayoutImgCorrectedParams();
			params.gravity = android.view.Gravity.CENTER;
			_surfaceView.setLayoutParams(params);
			_surfaceView.setCameraAspectRatio(getCameraAspectRatio());
			
			_cameraImageReader = ImageReader.newInstance(params.width, params.height, ImageFormat.JPEG, 2);
			_cameraImageReader.setOnImageAvailableListener(imageReader -> {
				var image = imageReader.acquireLatestImage();
				if(image != null){
					var bitmap = convertImageToBitmap(image);
					scanQRCode(bitmap);
					image.close();
				}
			}, _cameraCaptureHandler);
			
			_surfaceView.post(() -> {
				// Create a CameraCaptureSession for preview
				try {
					_cameraDevice.createCaptureSession(Arrays.asList(_cameraImageReader.getSurface(), surface), new CameraCaptureSession.StateCallback() {
						@Override
						public void onConfigured(@NonNull CameraCaptureSession session) {
							try {
								CameraManager manager = (CameraManager) getSystemService(CAMERA_SERVICE);
								String cameraId = manager.getCameraIdList()[0];
								CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
								Rect sensorSize = characteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
								
								assert sensorSize != null;
								Rect cropRegion = getCenterCropRegion(sensorSize, getCameraAspectRatio());
								
								_captureRequestBuilder.set(CaptureRequest.SCALER_CROP_REGION, cropRegion);
								session.setRepeatingRequest(_captureRequestBuilderDisplayer.build(), null, null); // this is needed for smooth preview
								
								if(_cameraCaptureThread == null){
									_cameraCaptureThread = new HandlerThread("CameraQRThread");
									_cameraCaptureThread.start();
								}
								if(_cameraCaptureHandler == null){
									_cameraCaptureHandler = new Handler(_cameraCaptureThread.getLooper());
								}
								// allow autofocus
								_captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
								_captureRequestBuilder.addTarget(_cameraImageReader.getSurface());
								
								var captureRequest = _captureRequestBuilder.build();
								
								_cameraCaptureRunnable = () -> {
									try {
										if(_cameraDevice == null || _cameraCaptureHandler == null){
											return;
										}
										session.capture(captureRequest, null, _cameraCaptureHandler);
										_cameraCaptureHandler.postDelayed(_cameraCaptureRunnable, 1000);
									} catch (Exception e) {
										Log.e(TAG, "_cameraCaptureRunnable: camera access error", e);
									}
								};
								_cameraCaptureHandler.post(_cameraCaptureRunnable);
								
							} catch (Exception e) {
								Log.e(TAG, "onConfigured: cant access camera", e);
							}
						}
						
						@Override
						public void onConfigureFailed(@NonNull CameraCaptureSession session) {
							// if error, make user add camera perms, just in case
							showPopupMenuYesNo(getString(R.string.qr_popup_no_camera_header), getString(R.string.qr_popup_no_camera_description), getString(R.string.qr_popup_no_camera_no), getString(R.string.qr_popup_no_camera_yes));
						}
					}, _cameraCaptureHandler);
				} catch (CameraAccessException e) {
					Log.e(TAG, "createCaptureSession: camera error occured", e);
				}
			});
		} catch (CameraAccessException e) {
			Log.e(TAG, "startCameraPreview: camera error occured", e);
		}
	}
	
	@NonNull
	private LinearLayoutCompat.LayoutParams getSurfaceLayoutImgCorrectedParams() {
		int maxSizeDp = 320;
		float density = getResources().getDisplayMetrics().density;
		int maxSizePx = (int) (maxSizeDp * density);
		
		float cameraAspectRatio = getCameraAspectRatio();
		
		int width, height;
		if (cameraAspectRatio > 1) {
			width = maxSizePx;
			height = (int) (width / cameraAspectRatio);
		} else {
			height = maxSizePx;
			width = (int) (height * cameraAspectRatio);
		}
		
		return new LinearLayoutCompat.LayoutParams(width, height);
	}
	
	@NonNull
	@Contract("_, _ -> new")
	private Rect getCenterCropRegion(@NonNull Rect sensorSize, float targetAspectRatio) {
		int sensorWidth = sensorSize.width();
		int sensorHeight = sensorSize.height();
		
		int cropWidth, cropHeight;
		if ((float) sensorWidth / sensorHeight > targetAspectRatio) {
			cropHeight = sensorHeight;
			cropWidth = (int) (sensorHeight * targetAspectRatio);
		} else {
			cropWidth = sensorWidth;
			cropHeight = (int) (sensorWidth / targetAspectRatio);
		}
		
		int x = (sensorWidth - cropWidth) / 2;
		int y = (sensorHeight - cropHeight) / 2;
		return new Rect(x, y, x + cropWidth, y + cropHeight);
	}
	
	/// Get camera's native aspect ratio
	private float getCameraAspectRatio() {
		try {
			CameraManager manager = (CameraManager) getSystemService(CAMERA_SERVICE);
			String cameraId = manager.getCameraIdList()[0];
			CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
			Rect sensorSize = characteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
			assert sensorSize != null;
			return (float) ((float) sensorSize.width() / ((sensorSize.height() * 1.3333))); // 1.3333 corrects the stretching issue somehow
		} catch (CameraAccessException e) {
			return 4f / 3f;
		}
	}
	
	@NonNull
	private Bitmap convertImageToBitmap(@NonNull Image image) {
		ByteBuffer buffer = image.getPlanes()[0].getBuffer();
		
		byte[] bytes = new byte[buffer.remaining()];
		buffer.get(bytes);
		
		return cropImageToAspectRatio(BitmapFactory.decodeByteArray(bytes, 0, bytes.length), 1, 1);
	}
	
	@NonNull
	private Bitmap cropImageToAspectRatio(@NonNull Bitmap bitmap, int targetWidth, int targetHeight) {
		int originalWidth = bitmap.getWidth();
		int originalHeight = bitmap.getHeight();
		
		float targetAspectRatio = (float) targetWidth / targetHeight;
		
		int cropWidth = originalWidth;
		int cropHeight = (int) (originalWidth / targetAspectRatio);
		
		if (cropHeight > originalHeight) {
			cropHeight = originalHeight;
			cropWidth = (int) (originalHeight * targetAspectRatio);
		}
		
		int startX = (originalWidth - cropWidth) / 2;
		int startY = (originalHeight - cropHeight) / 2;
		
		// Create cropped bitmap
		return Bitmap.createBitmap(bitmap, startX, startY, cropWidth, cropHeight);
	}
	
	
	private void closeCamera() {
		if (_cameraDevice != null) {
			_cameraDevice.close();
			_cameraDevice = null;
		}
		if (_cameraCaptureHandler != null && _cameraCaptureRunnable != null) {
			_cameraCaptureHandler.removeCallbacks(_cameraCaptureRunnable);
		}
		if(_cameraCaptureThread != null){
			_cameraCaptureThread.quit();
			try {
				_cameraCaptureThread.join();
				_cameraCaptureThread = null;
				_cameraCaptureHandler.getLooper().quit();
				_cameraCaptureHandler = null;
				_cameraCaptureRunnable = null;
			} catch (InterruptedException e) {
				Log.e(TAG, "closeCamera: failed to terminate CameraThread", e);
			}
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		closeCamera();
	}
	
	
	private void showPopupMenuYesNo(String header, String description, String actionNo, String actionYes){
		var activity = this;
		_navigateBack.post(()->{ // post dialog on the UI thread, ensuring the UI in initialized, and the user can see it
			AppPopupMenu.showPopupMenuYesNo(this, header, description, actionNo, actionYes, new AppPopupMenu.Callback() {
				@Override
				public void onActionNo() {
					AppPopupMenu.dismissPopupMenu();
					finish();
				}
				
				@Override
				public void onActionYes() {
					AppPermissions.requestPermission(activity, AppPermissions.CAMERA);
					if(AppPermissions.hasPermission(activity, AppPermissions.CAMERA)){
						AppPopupMenu.dismissPopupMenu();
						addQRScannerView();
						return;
					}
					// user didnt give permission, so ask again
					showPopupMenuYesNo(header, description, actionNo, actionYes);
				}
				
				@Override
				public void onActionOk() {
				
				}
			});
		});
	}
	
	private void scanQRCode(Bitmap bitmap) {
		InputImage image = InputImage.fromBitmap(bitmap, 0);
		BarcodeScanner scanner = BarcodeScanning.getClient();
		
		scanner.process(image)
			.addOnSuccessListener(barcodes -> {
				Log.i(TAG, "scanQRCode: checking image for QR code, len: " + barcodes.toArray().length);
				for (Barcode barcode : barcodes) {
					String rawValue = barcode.getRawValue();
					Log.i(TAG, "scanQRCode: scanned: " + rawValue);
					if(rawValue == null || !rawValue.contains(QrCodeGenerator.APPVALIDATION_QR)){
						continue;
					}
					String actualValue = rawValue.replace(QrCodeGenerator.APPVALIDATION_QR, "");
					Log.i(TAG, "scanQRCode: found valid: " + actualValue);
					onQRScanned(actualValue);
					return;
				}
			})
			.addOnFailureListener(e -> Log.e(TAG, "scanQRCode: failed scanning QR code", e));
	}
	
	private void onQRScanned(String qrValue){
	finish();
	}
}