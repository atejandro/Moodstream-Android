package com.moodstream.activity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;

import com.actionbarsherlock.app.SherlockActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.moodstream.R;
import com.moodstream.model.eventendpoint.Eventendpoint;
import com.moodstream.model.eventendpoint.model.Event;
import com.moodstream.model.eventendpoint.model.GeoPt;
import com.moodstream.util.DateTimePickerDialog;
import com.moodstream.util.LocationUtils;

public class CreateEventActivity extends SherlockActivity implements
		OnMapLongClickListener, LocationListener,
		GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener {

	// _____________________________________________________________________________
	// ATTRIBUTES___________________________________________________________________
	// **DEBUG**//
	private static final String TAG = "CreateEventActivity";

	// **FROM OTHER ACTIVITIES**//
	protected static String usr;// From StartActivity

	// **CONSTANTS**//
	CameraPosition INIT;

	// **GUI**//
	private EditText eventName;
	private EditText eventDescription;
	private Button inviteFriendsBtn;
	private Button createEventBtn;
	private MapView mMapview;

	// **OBJECTS**//
	// Location Services
	public GoogleMap mMap; // Map Object
	private LocationRequest mLocationRequest;// A request to connect to Location
												// Services
	private LocationClient mLocationClient;// Stores the current instantiation
											// of the location client in this
											// object
	private Location mCurrentLocation;// stores the current location
	private LatLng eventLocation;
	private Marker marker = null;
	private CircleOptions circleOption;
	private Circle circle;
	//Dates
	private DateTime start;
	private Date end;

	// _____________________________________________________________________________
	// LISTENERS____________________________________________________________________
	private OnClickListener createEventListener = new OnClickListener() {

		@Override
		public void onClick(View v) {

			// Execute Async Task to Create event
			new CreateEventTask().execute();
			finish();

		}
	};

	private OnClickListener inviteFriendsListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			Log.d(TAG,"Opening dialog...");
			showDateTimeDialog();
			Log.d(TAG,"Dialog opened...");
			
		}

	};

	// _____________________________________________________________________________
	// OVERRIDES____________________________________________________________________

	// **Activity**//
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Attach layout to activity
		setContentView(R.layout.activity_createevent);

		// Set UI elements
		eventName = (EditText) findViewById(R.id.eventName);
		eventDescription = (EditText) findViewById(R.id.eventDescription);
		inviteFriendsBtn = (Button) findViewById(R.id.inviteFriendsBtn);
		inviteFriendsBtn.setOnClickListener(inviteFriendsListener);
		createEventBtn = (Button) findViewById(R.id.createEventBtn);
		createEventBtn.setOnClickListener(createEventListener);
		mMapview = (MapView) findViewById(R.id.mapView);
		mMapview.onCreate(savedInstanceState);

		// Initialize object
		// 1.Map
		mMap = mMapview.getMap();
		mMap.getUiSettings().setMyLocationButtonEnabled(true);
		mMap.getUiSettings().setZoomControlsEnabled(true);
		mMap.getUiSettings().setCompassEnabled(false);
		mMap.setMyLocationEnabled(true);
		mMap.setOnMapLongClickListener(this);

		try {
			MapsInitializer.initialize(this);
		} catch (Exception e) {
			Log.d(TAG, e.getMessage());
		}

		// 2.Location client (for current location)
		mLocationClient = new LocationClient(this, this, this);

	}

	@Override
	protected void onStop() {
		// After disconnect() is called, the client is considered "dead".
		mLocationClient.disconnect();
		super.onStop();

	}

	@Override
	public void onStart() {

		super.onStart();
		/*
		 * Connect the client. Don't re-start any requests here; instead, wait
		 * for onResume()
		 */
		mLocationClient.connect();
		// mMap.setMyLocationEnabled(true);
	}

	/*
	 * Called when the system detects that this Activity is now visible.
	 */
	@Override
	protected void onResume() {
		super.onResume();
		mMapview.onResume();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mMapview.onDestroy();
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
		mMapview.onLowMemory();
	}

	/*
	 * Handle results returned to this Activity by other Activities started with
	 * startActivityForResult(). In particular, the method onConnectionFailed()
	 * in LocationUpdateRemover and LocationUpdateRequester may call
	 * startResolutionForResult() to start an Activity that handles Google Play
	 * services problems. The result of this call returns here, to
	 * onActivityResult.
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {

		// Choose what to do based on the request code
		switch (requestCode) {

		// If the request code matches the code sent in onConnectionFailed
		case LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST:

			switch (resultCode) {
			// If Google Play services resolved the problem
			case Activity.RESULT_OK:

				// Log the result
				Log.d(LocationUtils.APPTAG, "RESOLVED");

				break;

			// If any other result was returned by Google Play services
			default:
				// Log the result
				Log.d(LocationUtils.APPTAG, "NO RESOLUTION");

				break;
			}

			// If any other request code was received
		default:
			// Report that this Activity received an unknown requestCode
			Log.d(LocationUtils.APPTAG, "UNKNOWN ACTIVITY REQUEST CODE");

			break;
		}
	}

	// **OnMapLongClickListener**//
	@Override
	public void onMapLongClick(LatLng point) {
		// TODO Auto-generated method stub

		if (marker == null) {
			marker = mMap.addMarker(new MarkerOptions().position(point));
			marker.showInfoWindow();
		} else {
			circle.remove();
			marker.setPosition(point);
		}

		circleOption = new CircleOptions().center(point).radius(150)
				.fillColor(0x40ff0000).strokeColor(Color.TRANSPARENT)
				.strokeWidth(2);
		circle = mMap.addCircle(circleOption);
		eventLocation = point;
		// marker.setSnippet(getAddress(point));
	}

	private String getAddress(LatLng point) {
		String address = "";
		Geocoder geocoder;
		List<Address> addresses = null;

		geocoder = new Geocoder(this, Locale.getDefault());

		try {
			addresses = geocoder.getFromLocation(point.latitude,
					point.longitude, 1);
			address = addresses.get(0).getAddressLine(0);
			String city = addresses.get(0).getAddressLine(1);
			String country = addresses.get(0).getCountryName();
			address = Html.fromHtml(address + "<br>" + city + "<br>" + country)
					.toString();

			Log.d("CLAAAAAS", address);
			return address;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			Log.d("CLAAAAAS", "Address couldnt be found");
			return "";
		}
	}

	// **LocationListener**//
	@Override
	public void onLocationChanged(Location location) {

		// In the UI, set the latitude and longitude to the value received
		// mLatLng.setText(LocationUtils.getLatLng(this, location));

	}

	// **GooglePlayServicesClient.ConnectionCallbacks**//

	/*
	 * Called by Location Services when the request to connect the client
	 * finishes successfully. At this point, you can request the current
	 * location or start periodic updates
	 */
	@Override
	public void onConnected(Bundle arg0) {
		getCurrentLocation();
		animateMapTo(new LatLng(mCurrentLocation.getLatitude(),
				mCurrentLocation.getLongitude()));
	}

	/*
	 * Called by Location Services if the connection to the location client
	 * drops because of an error.
	 */
	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub

	}

	// **GooglePlayServicesClient.OnConnectionFailedListener**//

	/*
	 * Called by Location Services if the attempt to Location Services fails.
	 */
	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		/*
		 * Google Play services can resolve some errors it detects. If the error
		 * has a resolution, try sending an Intent to start a Google Play
		 * services activity that can resolve error.
		 */
		if (connectionResult.hasResolution()) {
			try {

				// Start an Activity that tries to resolve the error
				connectionResult.startResolutionForResult(this,
						LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST);

				/*
				 * Thrown if Google Play services canceled the original
				 * PendingIntent
				 */

			} catch (IntentSender.SendIntentException e) {

				// Log the error
				e.printStackTrace();
			}
		} else {

			// If no resolution is available, display a dialog to the user with
			// the error.
			showErrorDialog(connectionResult.getErrorCode());
		}
	}

	// _____________________________________________________________________________
	// METHODS______________________________________________________________________

	/**
	 * Calls getLastLocation() to get the current location
	 * 
	 */
	public void getCurrentLocation() {
		// If Google Play Services is available
		if (servicesConnected()) 
			mCurrentLocation = mLocationClient.getLastLocation();	
	}

	/**
	 * In response to a request to start updates, send a request to Location
	 * Services
	 */
	private void startPeriodicUpdates() {

		mLocationClient.requestLocationUpdates(mLocationRequest, this);
		// mConnectionState.setText(R.string.location_requested);
	}

	/**
	 * In response to a request to stop updates, send a request to Location
	 * Services
	 */
	private void stopPeriodicUpdates() {
		mLocationClient.removeLocationUpdates(this);
		// mConnectionState.setText(R.string.location_updates_stopped);
	}

	/**
	 * Pops up the dialog containing the datetime picker
	 */
	private void showDateTimeDialog() {

		// Inflate a view to assign to the AlertDialog
		View view = LayoutInflater.from(this).inflate(
				R.layout.dialog_datetimepicker, null);

		// Assign control variables to the view inflated
		final TimePicker tp = (TimePicker) view.findViewById(R.id.timepicker);
		final DatePicker dp = (DatePicker) view.findViewById(R.id.datepicker);

		// Create dialog builder
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		dialog.setTitle("Choose a date");
		dialog.setCancelable(true);
		dialog.setView(view);
		dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				Log.d(TAG, "Date saved");
				
				Calendar cal=Calendar.getInstance();
				cal.set(dp.getYear(), dp.getMonth(), dp.getDayOfMonth(), tp.getCurrentHour(), tp.getCurrentMinute());
				start=new DateTime(cal.getTime());
				Log.d(TAG,getDateFormatString("dd/MM/yyyy - h:mm a"));
				dialog.dismiss();
			}
		});
		
		dialog.create().show();
	}
	
	
	@SuppressLint("SimpleDateFormat")
	private String getDateFormatString(String format)
	{
		Date date=new Date(start.getValue());
		SimpleDateFormat formatter = new SimpleDateFormat(format);
	    String dateString = formatter.format(date);
	    return dateString;
	}

	

	/**
	 * Handles map camera animation from last location to new location
	 */
	private void animateMapTo(LatLng newLocation) {
		INIT = new CameraPosition.Builder().target(newLocation).zoom((15.5F))
				.bearing(0).tilt(70F).build();
		mMap.animateCamera(CameraUpdateFactory.newCameraPosition(INIT));
	}

	// _____________________________________________________________________________
	// LOCATION_SERVICES____________________________________________________________
	/**
	 * Verify that Google Play services is available before making a request.
	 * 
	 * @return true if Google Play services is available, otherwise false
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@SuppressLint("NewApi")
	private boolean servicesConnected() {

		// Check that Google Play services is available
		int resultCode = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(this);

		// If Google Play services is available
		if (ConnectionResult.SUCCESS == resultCode) {

			// In debug mode, log the status
			Log.d(TAG, "Play services available");

			// Continue
			return true;

			// Google Play services was not available for some reason
		} else {

			// Display an error dialog
			Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode,
					this, 0);
			if (dialog != null) {
				dialog.show();
				ErrorDialogFragment errorFragment = new ErrorDialogFragment();
				errorFragment.setDialog(dialog);
				errorFragment.show(getFragmentManager(), TAG);
			}
			return false;
		}
	}

	// _____________________________________________________________________________
	// ASYNC TASKS___________________________________________________________
	private class CreateEventTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			Log.d(TAG, "Starting to create event");
			// Calendar to create Date format
			Calendar calendar = Calendar.getInstance();
			//DateTime date = new DateTime(calendar.getTime());

			// TODO: Get current location. Getting constant location meanwhile.
			GeoPt gpt = new GeoPt();
			gpt.setLatitude((float) eventLocation.latitude);
			gpt.setLongitude((float) eventLocation.longitude);

			// Create Event
			Event e = new Event();
			e.setEventName(eventName.getText().toString());
			e.setDescription(eventDescription.getText().toString());
			Log.d(TAG, "Setting even owner " + usr + " ...");
			e.setCreator(usr);
			e.setDate(start);
			e.setLocation(gpt);

			Log.d(TAG, "Calling Backend");
			// Call backend
			// 1. Create Builder
			Eventendpoint.Builder builder = new Eventendpoint.Builder(
					AndroidHttp.newCompatibleTransport(), new JacksonFactory(),
					null);
			builder = CloudEndpointUtils.updateBuilder(builder);
			// 2. Assign builder to Endpoint
			Eventendpoint endpoint = builder.build();
			Log.d(TAG, "Build Succeed...");
			try {
				Log.d(TAG, "Inserting event...");
				endpoint.insertEvent(e).execute();
				Log.d(TAG, "Event inserted");
			} catch (Exception e2) {
				e2.printStackTrace();
			}

			return null;
		}

	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@SuppressLint("NewApi")
	private void showErrorDialog(int errorCode) {

		// Get the error dialog from Google Play services
		Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(errorCode,
				this, LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST);

		// If Google Play services can provide an error dialog
		if (errorDialog != null) {

			// Create a new DialogFragment in which to show the error dialog
			ErrorDialogFragment errorFragment = new ErrorDialogFragment();

			// Set the dialog in the DialogFragment
			errorFragment.setDialog(errorDialog);

			// Show the error dialog in the DialogFragment
			errorFragment.show(getFragmentManager(), LocationUtils.APPTAG);
		}
	}

	// _____________________________________________________________________________
	// ERROR DIALOG
	// CLASS___________________________________________________________
	/**
	 * Define a DialogFragment to display the error dialog generated in
	 * showErrorDialog.
	 */
	@SuppressLint("NewApi")
	public static class ErrorDialogFragment extends DialogFragment {

		// Global field to contain the error dialog
		private Dialog mDialog;

		/**
		 * Default constructor. Sets the dialog field to null
		 */
		public ErrorDialogFragment() {

			mDialog = null;
		}

		/**
		 * Set the dialog to display
		 * 
		 * @param dialog
		 *            An error dialog
		 */
		public void setDialog(Dialog dialog) {
			mDialog = dialog;
		}

		/*
		 * This method must return a Dialog to the DialogFragment.
		 */
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			return mDialog;
		}
	}

}
