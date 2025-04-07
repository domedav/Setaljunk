package com.domedav.setaljunk.location;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.PolyUtil;

import org.jetbrains.annotations.Unmodifiable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class LocationRequest {
	private static final String TAG = "LocationRequest";
	
	@NonNull
	public static @Unmodifiable CompletableFuture<String> fetchRouteDataAsync(@NonNull Context context, @NonNull LatLng origin, @NonNull LatLng dest){
		return CompletableFuture.supplyAsync(()->{
			ApplicationInfo appInfo;
			try {
				appInfo = context.getPackageManager().getApplicationInfo("com.domedav.setaljunk", PackageManager.GET_META_DATA);
			} catch (PackageManager.NameNotFoundException e) {
				throw new RuntimeException(e);
			}
			String apiKey = appInfo.metaData.getString("com.google.android.geo.API_KEY");
			String url = "https://maps.googleapis.com/maps/api/directions/json?"
					+ "origin=" + origin.latitude + "," + origin.longitude
					+ "&destination=" + dest.latitude + "," + dest.longitude
					+ "&mode=walking"
					+ "&key=" + apiKey;
			
			Log.d(TAG, "fetchRouteData: ");
			
			OkHttpClient eagerClient = new OkHttpClient()
					.newBuilder()
					.readTimeout(10000, TimeUnit.MILLISECONDS).build();
			Request request = new Request.Builder().url(url).build();
			Response response;
			try {
				response = eagerClient.newCall(request).execute();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			try {
				assert response.body() != null;
				return response.body().string();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});
	}
	
	@NonNull
	public static LatLng[] parseRoute(String json) throws JSONException {
		JSONObject jsonResponse = new JSONObject(json);
		JSONArray routes = jsonResponse.getJSONArray("routes");
		if(routes.length() == 0){
			Log.e(TAG, "parseRoute: no valid json data");
			return null;
		}
		JSONObject route = routes.getJSONObject(0);
		JSONObject polyline = route.getJSONObject("overview_polyline");
		String encodedPath = polyline.getString("points");
		return PolyUtil.decode(encodedPath).toArray(new LatLng[0]);
	}
}
