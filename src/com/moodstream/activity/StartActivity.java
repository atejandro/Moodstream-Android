package com.moodstream.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.moodstream.R;
import com.moodstream.activity.CreateEventActivity.ErrorDialogFragment;
import com.moodstream.model.eventendpoint.Eventendpoint;
import com.moodstream.model.eventendpoint.model.CollectionResponseEvent;
import com.moodstream.model.eventendpoint.model.Event;
import com.moodstream.model.userendpoint.model.User;
import com.moodstream.util.DateUtils;
import com.moodstream.util.LocationUtils;

public class StartActivity extends
/* SherlockFragmentActivity */SherlockActivity implements LocationListener,
		GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener {

	// _____________________________________________________________________________
	// ATTRIBUTES___________________________________________________________________
	// **DEBUG**//
	private static final String TAG = "StartActivity";

	// **FROM OTHER ACTIVITIES**//
	protected static User currentUsr;// From LoginActivity

	// **GUI**//
	private MapView mMapview;
	private TextView event_list_label;
	private ListView eventList;

	// **OBJECTS**//
	private List<Event> events = null;

	public GoogleMap mMap; // Map Object
	private Location mCurrentLocation;// stores the current location
	private List<Marker> eventMarkers = null;
	private LocationClient mLocationClient;

	// Tabs variables
	// private ViewPager mViewPager; //For hosting fragment contents
	// private TabsAdapter mTabsAdapter;//To handle ActionBar an ViewPager
	// events

	// __________________________________________________________________________
	// OVERRIDES_________________________________________________________________
	// **ACTIVITY**//
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_start);

		setMap(savedInstanceState);

		event_list_label = (TextView) findViewById(R.id.event_list_label);

		eventList = (ListView) findViewById(R.id.eventsList);
		new ListMyEventsTask().execute();
		eventList.setOnItemClickListener(eventsListClickListener);

		mLocationClient = new LocationClient(this, this, this);

		/*
		 * CODE FOR ACTIONBAR SHERLOCK NOT WORKING!!!
		 * 
		 * 
		 * //The content view will be set as the pager instead of the whole
		 * layout mViewPager=new ViewPager(this); mViewPager.setId(R.id.pager);
		 * setContentView(mViewPager);
		 * 
		 * //Create ActionBar with its Navigation Mode final ActionBar
		 * actionbar=getSupportActionBar();
		 * actionbar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		 * 
		 * mTabsAdapter = new TabsAdapter(this, mViewPager);
		 * mTabsAdapter.addTab(actionbar.newTab().setText("My Events"),
		 * CurrentFragment.class, null);
		 * mTabsAdapter.addTab(actionbar.newTab().setText("Other"),
		 * CurrentFragment.class, null);
		 */
	}

	@Override
	protected void onResume() {
		super.onResume();
		mMapview.onResume();
		new ListMyEventsTask().execute();
	}

	@Override
	protected void onStop() {
		super.onStop();
		mLocationClient.disconnect();
	}

	@Override
	protected void onStart() {
		super.onStart();
		mLocationClient.connect();
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

	// **ON_CONNECTION_FAILED_LISTENER**//
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
			// showErrorDialog(connectionResult.getErrorCode());
		}

	}

	// **CONNECTION_CALLBACKS**//
	@Override
	public void onConnected(Bundle connectionHint) {
		getCurrentLocation();
		animateMapTo(new LatLng(mCurrentLocation.getLatitude(),
				mCurrentLocation.getLongitude()));

	}

	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub

	}

	// **CONNECTION_LISTENER**//
	@Override
	public void onLocationChanged(Location arg0) {
		// TODO Auto-generated method stub

	}

	// ______________________________________________________________________
	// MENU__________________________________________________________________

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}

	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.action_create:
			Log.d(TAG, "Starting intent to CreateEventActivity...");
			CreateEventActivity.usr = currentUsr.getNickname();
			startActivity(new Intent(this, CreateEventActivity.class));
			return true;

		case R.id.action_add_friend:
			Log.d(TAG, "Starting intent to AddFriendActivity...");
			AddFriendActivity.currentUsr = currentUsr;
			startActivity(new Intent(this, AddFriendActivity.class));

		default:
			return super.onOptionsItemSelected(item);
		}

	}

	// ___________________________________________________________________________
	// METHODS____________________________________________________________________
	private void setMap(Bundle savedInstanceState) {
		mMapview = (MapView) findViewById(R.id.start_mapView);
		mMapview.onCreate(savedInstanceState);
		mMap = mMapview.getMap();
		mMap.getUiSettings().setMyLocationButtonEnabled(true);
		mMap.getUiSettings().setZoomControlsEnabled(true);
		mMap.getUiSettings().setCompassEnabled(false);
		mMap.setMyLocationEnabled(true);
		mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
			
			@Override
			public void onInfoWindowClick(Marker marker) {
				
				int pos=0;
				
				Log.d(TAG,"Eventmarkers size()="+eventMarkers.size());
				
				for (int i = 0; i < eventMarkers.size(); i++) {
					if(eventMarkers.get(i).getSnippet().equals(marker.getSnippet()))
					{
						Log.d(TAG,"FOUND IT!! "+i);
						pos=i;
					}
				}
				
				Log.d(TAG,"POS: "+pos);
				
				EventDetailsActivity.selectedEvent = events.get(pos);
				EventDetailsActivity.currentUsr = currentUsr;
				startActivity(new Intent(StartActivity.this,
						EventDetailsActivity.class));
				
			}
		});

		try {
			MapsInitializer.initialize(this);
		} catch (Exception e) {
			Log.d(TAG, e.getMessage());
		}
	}

	public void getCurrentLocation() {
		// If Google Play Services is available
		if (servicesConnected())
			mCurrentLocation = mLocationClient.getLastLocation();
	}
	
	private LatLng getCurrentLatLng() {
		getCurrentLocation();
		return new LatLng(mCurrentLocation.getLatitude(),mCurrentLocation.getLongitude());
	}

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

	/**
	 * Handles map camera animation from last location to new location
	 */
	private void animateMapTo(LatLng newLocation) {

		CameraPosition camera = new CameraPosition.Builder()
				.target(newLocation).zoom((15.5F)).bearing(0).tilt(70F).build();

		mMap.animateCamera(CameraUpdateFactory.newCameraPosition(camera));
	}

	// __________________________________________________________________________
	// ASYNC_TASKS_______________________________________________________________

	private class ListMyEventsTask extends
			AsyncTask<Void, Void, CollectionResponseEvent> {

		@Override
		protected CollectionResponseEvent doInBackground(Void... params) {

			Log.d(TAG, "Calling Backend...");
			Eventendpoint.Builder endpointBuilder = new Eventendpoint.Builder(
					AndroidHttp.newCompatibleTransport(), new JacksonFactory(),
					null);
			endpointBuilder = CloudEndpointUtils.updateBuilder(endpointBuilder);

			CollectionResponseEvent result = null;

			Eventendpoint endpoint = endpointBuilder.build();
			Log.d(TAG, "Build succeed...");
			try {
				Log.d(TAG, "Calling listevent");
				result = endpoint.listEvent()
						.setNickname(currentUsr.getNickname()).execute();
			} catch (Exception e) {
				e.printStackTrace();
				result = null;
			}

			return result;
		}

		@Override
		protected void onPostExecute(CollectionResponseEvent result) {

			if (!result.toString().equals("{}")) {
				Log.d(TAG, "Resultset is not null: " + result.toString());
				ListAdapter eventsListAdapter = createEventListAdapter(result
						.getItems());
				eventList.setAdapter(eventsListAdapter);
				events = result.getItems();
			} else {
				Log.d(TAG, "Resultset is null");
				event_list_label.setText("You have no events");
			}

		}

		// ______________________________________________________________________________
		// LIST__________________________________________________________________________
		/**
		 * Populate a List Adapter with the data
		 * */
		private ListAdapter createEventListAdapter(List<Event> events) {

			List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
			eventMarkers = new ArrayList<Marker>();
			LatLng currentPosition= getCurrentLatLng();

			if (events != null)
				for (Event event : events) {
					Map<String, Object> map = new HashMap<String, Object>();
					
					LatLng eventPosition = new LatLng(event.getLocation()
							.getLatitude(), event.getLocation().getLongitude());
					
										
					if(LocationUtils.distanceBetween(currentPosition, eventPosition, LocationUtils.DISTANCE_IN_METERS)<=150)
					{
						mMap.addCircle(new CircleOptions().center(eventPosition).radius(150)
								.fillColor(0x4000ff00).strokeColor(Color.TRANSPARENT)
								.strokeWidth(2));
						
						eventMarkers.add(mMap.addMarker(new MarkerOptions()
						.position(eventPosition).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)).title(event.getEventName())
						.snippet(event.getDescription())));
					}
					else
					{	
						mMap.addCircle(new CircleOptions().center(eventPosition).radius(150)
								.fillColor(0x40ff0000).strokeColor(Color.TRANSPARENT)
								.strokeWidth(2));
						
						eventMarkers.add(mMap.addMarker(new MarkerOptions()
						.position(eventPosition).title(event.getEventName())
						.snippet(event.getDescription())));
					}
						

					// TODO: Change the ic_launcher icon
					map.put("eventPhoto", R.drawable.ic_launcher);
					map.put("eventName", event.getEventName());
					map.put("eventDate", DateUtils.getDateFormatString(
							DateUtils.TODAY_HOUR_FORMAT, event.getDate()));
					map.put("eventCreator", event.getCreator());
					data.add(map);
				}

			SimpleAdapter adapter = new SimpleAdapter(StartActivity.this, data,
					R.layout.item_event, new String[] { "eventPhoto",
							"eventName", "eventDate", "eventCreator" },
					new int[] { R.id.event_icon, R.id.event_name,
							R.id.event_date, R.id.event_creator });

			return adapter;
		}

	

	}

	private OnItemClickListener eventsListClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			Event selectedEvent = events.get((int) arg3);

			eventMarkers.get((int)arg3).showInfoWindow();
			LatLng position=new LatLng(selectedEvent.getLocation().getLatitude(), selectedEvent.getLocation().getLongitude());
			animateMapTo(position);
		}
	};

}
