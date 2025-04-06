package com.domedav.setaljunk.location;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.Locale;

public class LocationGeneration {
	
	private static final String TAG = "LocationGeneration";
	
	/// Generates a random location, by the given parameters
	@NonNull
	public static LatLng generateRandomNearbyLoction(@NonNull LatLng currentLocation, double range, Context context){
		Geocoder geocoder = new Geocoder(context, Locale.getDefault());
		
		double randomAngle = Math.random() * 2 * Math.PI; // pick a random direction
		var randomRadius = Math.sqrt(Math.random()) * range; // in a random radius within the range
		
		randomRadius = lerp(randomRadius, range, .65); // add heavy bias towards the end of the radius circle
		
		// Convert meters to latitude degrees (approximately)
		double latOffset = randomRadius * Math.sin(randomAngle) / 111111.0;
		
		double lngOffset = randomRadius * Math.cos(randomAngle) / (111111.0 * Math.cos(Math.toRadians(currentLocation.latitude)));
		
		// add the result to our current location
		double newLat = currentLocation.latitude + latOffset;
		double newLng = currentLocation.longitude + lngOffset;
		
		Address address = new Address(Locale.getDefault());
		address.setLatitude(newLat);
		address.setLongitude(newLng); // if the address is somehow incorrect, even with geocoder, just use the likely not navigatable generated location
		try{
			var addresses = geocoder.getFromLocation(newLat, newLng, 1);
			if(addresses != null && !addresses.isEmpty()){
				Log.i(TAG, "generateRandomNearbyLoction: Random Location Name: " + addresses.get(0).toString());
				address = addresses.get(0);
			}
		}
		catch (IOException iex){
			Log.e(TAG, "generateRandomNearbyLoction: ", iex);
		}
		
		return new LatLng(address.getLatitude(), address.getLongitude());
	}
	
	private static double lerp(double a, double b, double t){
		return a + t * (b - a);
	}
}
