package com.moodstream.util;

import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.text.Html;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

public class LocationUtils {
	// Debugging tag for the application
	public static final String TAG = "LocationUtils";

	// Name of shared preferences repository that stores persistent state
	public static final String SHARED_PREFERENCES = "com.example.android.location.SHARED_PREFERENCES";

	// Key for storing the "updates requested" flag in shared preferences
	public static final String KEY_UPDATES_REQUESTED = "com.example.android.location.KEY_UPDATES_REQUESTED";

	/*
	 * Define a request code to send to Google Play services This code is
	 * returned in Activity.onActivityResult
	 */
	public final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

	/*
	 * Constants for location update parameters
	 */
	// Milliseconds per second
	public static final int MILLISECONDS_PER_SECOND = 1000;

	// The update interval
	public static final int UPDATE_INTERVAL_IN_SECONDS = 5;

	// A fast interval ceiling
	public static final int FAST_CEILING_IN_SECONDS = 1;

	// Update interval in milliseconds
	public static final long UPDATE_INTERVAL_IN_MILLISECONDS = MILLISECONDS_PER_SECOND
			* UPDATE_INTERVAL_IN_SECONDS;

	// A fast ceiling of update intervals, used when the app is visible
	public static final long FAST_INTERVAL_CEILING_IN_MILLISECONDS = MILLISECONDS_PER_SECOND
			* FAST_CEILING_IN_SECONDS;

	
	public static final String DISTANCE_IN_MILES="DISTANCE_IN_MILES";
	
	public static final String DISTANCE_IN_METERS="DISTANCE_IN_METERS";
	
	public static final String DISTANCE_IN_KILOMETERS="DISTANCE_IN_KILOMETERS";
	
	public static final int EARTH_RADIUS=6371;
	
	public static final double KILOMETERS_IN_MILE=1.609344;
	
	public static final double METERS_IN_MILE=KILOMETERS_IN_MILE*1000;
	
	
	// Create an empty string for initializing strings
	public static final String EMPTY_STRING = new String();

	/**
	 * Get the latitude and longitude from the Location object returned by
	 * Location Services.
	 * 
	 * @param currentLocation
	 *            A Location object containing the current location
	 * @return The latitude and longitude of the current location, or null if no
	 *         location is available.
	 */
	/*
	 * public static String getLatLng(Context context, Location currentLocation)
	 * { // If the location is valid if (currentLocation != null) {
	 * 
	 * // Return the latitude and longitude as strings return context.getString(
	 * R.string.latitude_longitude, currentLocation.getLatitude(),
	 * currentLocation.getLongitude()); } else {
	 * 
	 * // Otherwise, return the empty string return EMPTY_STRING; } }
	 */

	public static String getAddress(Context context,LatLng point ) {
		String address = "";
		Geocoder geocoder;
		List<Address> addresses = null;

		geocoder = new Geocoder(context, Locale.getDefault());

		try {
			addresses = geocoder.getFromLocation(point.latitude,
					point.longitude, 1);
			address = addresses.get(0).getAddressLine(0);
			address = Html.fromHtml(address).toString();

			Log.d(TAG, address);
			return address;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			Log.d(TAG, "Address couldnt be found");
			return EMPTY_STRING;
		}
	}
	
	  public static double distanceBetween(LatLng point1, LatLng point2, String unit) {
		  double lat1=point1.latitude, lon1=point1.longitude;
		  double lat2=point2.latitude, lon2=point2.longitude;
		  
	      double theta = lon1 - lon2;
	      double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
	      dist = Math.acos(dist);
	      dist = rad2deg(dist);
	      dist = dist * 60 * 1.1515;
	      if (unit == DISTANCE_IN_KILOMETERS) {
	        dist = dist * KILOMETERS_IN_MILE;
	      } else if (unit == DISTANCE_IN_METERS) {
	        dist = dist * METERS_IN_MILE;
	        }
	      return (dist);
	    }

	    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
	    /*::  This function converts decimal degrees to radians             :*/
	    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
	    private static double deg2rad(double deg) {
	      return (deg * Math.PI / 180.0);
	    }

	    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
	    /*::  This function converts radians to decimal degrees             :*/
	    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
	    private static double rad2deg(double rad) {
	      return (rad * 180.0 / Math.PI);
	    }
	
	

}
