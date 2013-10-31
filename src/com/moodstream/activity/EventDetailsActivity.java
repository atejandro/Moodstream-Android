package com.moodstream.activity;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.json.jackson2.JacksonFactory;
//import com.google.api.client.util.IOUtils;
import com.moodstream.R;
import com.moodstream.activity.CreateEventActivity.ErrorDialogFragment;
import com.moodstream.adapter.PhotoListAdapter;
import com.moodstream.model.checkinendpoint.Checkinendpoint;
import com.moodstream.model.checkinendpoint.model.Checkin;
import com.moodstream.model.eventendpoint.model.Event;
import com.moodstream.model.photoendpoint.Photoendpoint;
import com.moodstream.model.photoendpoint.model.CollectionResponsePhoto;
import com.moodstream.model.photoendpoint.model.Photo;
import com.moodstream.model.userendpoint.model.User;
import com.moodstream.util.AWSUtils;
import com.moodstream.util.DateUtils;
import com.moodstream.util.LocationUtils;

public class EventDetailsActivity extends Activity  implements LocationListener,
GooglePlayServicesClient.ConnectionCallbacks,
GooglePlayServicesClient.OnConnectionFailedListener{
	
	private static final String TAG="EventDetailsActivity";
	
	//AWS S3 initialization
   private AmazonS3Client s3Client = new AmazonS3Client(
					new BasicAWSCredentials(AWSUtils.ACCESS_KEY_ID,
											AWSUtils.SECRET_KEY));
	
	protected static Event selectedEvent;
	protected static User currentUsr;
	
	private static final int ACTION_TAKE_PHOTO=1;//Take a big photo
	private boolean isCheckedIn=false;
	
	private Checkin checkin;
	
	private TextView detail_event_name;
	private TextView detail_event_invitees;
	private TextView detail_event_date;
	private TextView detail_event_address;
	private Button take_photo_btn;
	
	
	//ListView
    private ListView photoList;
    private PhotoListAdapter adapter;
	private List<Photo> photos=null;
	
	//File management
	private File mPhotoFile;
	private String timeStamp;
	
	private LocationClient mLocationClient;
	private Location mCurrentLocation;
	
	@Override
	protected void onStart() {
		super.onStart();
		mLocationClient.connect();
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		mLocationClient.disconnect();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		mLocationClient.disconnect();
	}
	

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_event_details);
		
		//Set views
		detail_event_name=(TextView)findViewById(R.id.detail_event_name);
		detail_event_name.setText(selectedEvent.getEventName());
		
		detail_event_invitees=(TextView)findViewById(R.id.detail_event_invitees);
		if(selectedEvent.getInvitees()==null)
			detail_event_invitees.setText("Invited: 0");
		else
			detail_event_invitees.setText("Invited: "+selectedEvent.getInvitees().size());
		
		LatLng point=new LatLng(selectedEvent.getLocation().getLatitude(), selectedEvent.getLocation().getLongitude());
		
		detail_event_address=(TextView)findViewById(R.id.detail_event_address);
		detail_event_address.setText(LocationUtils.getAddress(this,point));
		
		detail_event_date=(TextView)findViewById(R.id.detail_event_date);
		detail_event_date.setText("Date: "+DateUtils.getDateFormatString(DateUtils.DATE_FORMAT,selectedEvent.getDate()));
		
		take_photo_btn=(Button)findViewById(R.id.take_photo_btn);
		
		mLocationClient=new LocationClient(this, this, this);
		
		new isCheckedinTask().execute();
		
		//setButtonListeners();
		
		photoList=(ListView)findViewById(R.id.photosList);
		
		new ListEventPhotoTask().execute();
		
		mPhotoFile=null;
		CreatePhotoActivity.photoFile=null;	
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		
		if(selectedEvent.getCreator().equals(currentUsr.getNickname()))
		{
		MenuInflater inflater =getMenuInflater();
		inflater.inflate(R.menu.menu_eventdetails, menu);
		}
		return super.onCreateOptionsMenu(menu);
	}
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch(item.getItemId())
		{
			case R.id.action_invite_friend:
				AddFriendActivity.currentUsr=currentUsr;
				AddFriendActivity.event=selectedEvent;
				AddFriendActivity.inviteFriend=true;
				Log.d(TAG,"Calling invite friend intent...");
				startActivity(new Intent(this,AddFriendActivity.class));
				break;
				
			case R.id.action_update_event:
				Log.d(TAG,"Calling update event intent...");
				break;
		}
		return true;
	}
	
	
	@Override
	protected void onResume() {
		super.onResume();
		new ListEventPhotoTask().execute();
	}
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		switch (requestCode) {
		case ACTION_TAKE_PHOTO:
			if(resultCode==RESULT_OK)
			{  
				if(CreatePhotoActivity.mCurrentPhotoPath!=null && CreatePhotoActivity.photoFile!=null)
				 startActivity(new Intent(this,CreatePhotoActivity.class));
			}
			else
				Log.d(TAG,"The path or the File are null");	
			break;
		}
	}
	
	
	
	private void setButtonListeners()
	{
		
		//mLocationClient.connect();
		getCurrentLocation();
		LatLng eventPosition = new LatLng(selectedEvent.getLocation()
				.getLatitude(), selectedEvent.getLocation().getLongitude());
		
		LatLng currentPostition=new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
		
		if(LocationUtils.distanceBetween(currentPostition, eventPosition, LocationUtils.DISTANCE_IN_METERS)<=150)
		{
			if(isCheckedIn)
			{	
				Log.d(TAG,"Setting take photo text");
				take_photo_btn.setText("Take photo");	
				take_photo_btn.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						//Go to a cam activity
						takePictureIntent(ACTION_TAKE_PHOTO);
					}
				});
			}
			else
			{
				Log.d(TAG,"Setting checkin text");
				take_photo_btn.setText("Checkin");	
				
				take_photo_btn.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						
						new CheckinTask().execute();
						
					}
				});
			}
		}
		else
		{
			take_photo_btn.setVisibility(View.GONE);
		}
	}
	
	
	private void takePictureIntent(int actionCode){
		
		//Create the camera picture intent
		Intent takePictureIntent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		
		switch(actionCode)
		{
		case ACTION_TAKE_PHOTO:
			
			
			try {
				Log.d(TAG, "trying to make intent");
				
				//Save variables into CreatePhotoActivity
				mPhotoFile= setUpPhotoFile();
				CreatePhotoActivity.photoFile =mPhotoFile;
				CreatePhotoActivity.mCurrentPhotoPath = CreatePhotoActivity.photoFile.getAbsolutePath();
				CreatePhotoActivity.eventId=selectedEvent.getKey().getId();
				CreatePhotoActivity.usrNickname=currentUsr.getNickname();
				
				//Set extra parameters into the intent
				takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(CreatePhotoActivity.photoFile));
				
			} catch (IOException e) {
				Log.d(TAG, "intent failed");
				e.printStackTrace();
				//Set every value to null to clean buffer
				CreatePhotoActivity.photoFile = null;
				CreatePhotoActivity.mCurrentPhotoPath = null;
				CreatePhotoActivity.eventId=null;
				CreatePhotoActivity.usrNickname=null;
			}
			break;
			
			default:break;
		}		
		startActivityForResult(takePictureIntent, actionCode);
	}
	
	
	
	private File setUpPhotoFile() throws IOException {
		
		File f = createImageFile();
		CreatePhotoActivity.mCurrentPhotoPath = f.getAbsolutePath();
		
		return f;
	}
	
	
	@SuppressLint("SimpleDateFormat")
	private File createImageFile() throws IOException {
		// Create an image file name
		timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		String imageFileName = /*selectedEvent.getKey().toString()+*/"IMG_" + timeStamp + "_";
		
	    File albumF = getAlbumDir();//get album address
	    Log.i(TAG,"Creating temposrary file...");
		File imageF = File.createTempFile(imageFileName, ".jpg", albumF);
		Log.i(TAG,"File: "+imageF.getName());
		return imageF;
	}
	
	
	/**
	 * Gets/Creates the image directory
	 */
	private File getAlbumDir() {
		File storageDir = null;

		//Communicate with the phones external storage state
		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			
			//make directory
			storageDir =new File(
					  Environment.getExternalStoragePublicDirectory(
							    Environment.DIRECTORY_PICTURES
							  ), 
							  "/Moodstream/");

			if (storageDir != null) {
				if (! storageDir.mkdirs()) {
					if (! storageDir.exists()){
						Log.d("Moodstream", "failed to create directory");
						return null;
					}
				}
			}
			
		} else {
			Log.v(getString(R.string.app_name), "External storage is not mounted READ/WRITE.");
		}
		
		return storageDir;
	}

	
	//--------------------------------------------------------------------------------------------//
	//                                      ASYNC TASKS                                           //
	//--------------------------------------------------------------------------------------------//
	
	private class ListEventPhotoTask extends AsyncTask<Void, Void, CollectionResponsePhoto>
	{

		@Override
		protected CollectionResponsePhoto doInBackground(Void... params) {
			
			Photoendpoint.Builder builder=new Photoendpoint.Builder(AndroidHttp.newCompatibleTransport(), new JacksonFactory(), null);
			builder=CloudEndpointUtils.updateBuilder(builder);
			
			
			
			CollectionResponsePhoto result=null;
			Photoendpoint endpoint=builder.build();
			
			
			try {
				result=endpoint.listPhoto().setEventId(selectedEvent.getKey().getId()).execute();
			
				
			} catch (Exception e) {
				e.printStackTrace();
				result =null;
			}
			
			
			return result;
		}
		
		@Override
		protected void onPostExecute(CollectionResponsePhoto result) {
			super.onPostExecute(result);
			Log.d(TAG,"Photos: "+result.toString());
			adapter=new PhotoListAdapter(EventDetailsActivity.this, result.getItems());
			photoList.setAdapter(adapter);
			
		}
	}
	
	
	private class CheckinTask extends AsyncTask<Void, Void, Void>
	{
		ProgressDialog dialog;
		
		@Override
		protected void onPreExecute() {
			dialog=new ProgressDialog(EventDetailsActivity.this);
			dialog.setCancelable(false);
			dialog.show();
		}

		@Override
		protected Void doInBackground(Void... params) {
			
			Checkin checkin=new Checkin();
			checkin.setUserId(currentUsr.getNickname());
			checkin.setEventId(selectedEvent.getKey().getId());
			Calendar cal=Calendar.getInstance();
			checkin.setTimestamp(cal.getTimeInMillis());
			
			
			Checkinendpoint.Builder builder=new Checkinendpoint.Builder(AndroidHttp.newCompatibleTransport(), new JacksonFactory(), null);
			builder=CloudEndpointUtils.updateBuilder(builder);
			Checkinendpoint endpoint=builder.build();
			
			try {
				Log.d(TAG,"Checking in...");
				endpoint.insertCheckin(checkin).execute();
				Log.d(TAG,"Checked in...");
			} catch (Exception e) {
				Log.d(TAG,"Exception occured during insert checkin...");
				e.printStackTrace();
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			isCheckedIn=true;
			setButtonListeners();
			dialog.dismiss();
		}
		
	}
	
	private class isCheckedinTask extends AsyncTask<Void, Void, Void>
	{

		ProgressDialog dialog;
		
		@Override
		protected void onPreExecute() {
			dialog=new ProgressDialog(EventDetailsActivity.this);
			dialog.setCancelable(false);
			dialog.show();
		}
		
		@Override
		protected Void doInBackground(Void... params) {
		
			Checkinendpoint.Builder builder=new Checkinendpoint.Builder(AndroidHttp.newCompatibleTransport(), new JacksonFactory(), null);
			builder=CloudEndpointUtils.updateBuilder(builder);
			Checkinendpoint endpoint=builder.build();
			
			try {
			    checkin=endpoint.isCheckedin(currentUsr.getNickname(), selectedEvent.getKey().getId()).execute();
			    
			    if(checkin.getUserId()!=null)
			    	isCheckedIn=true;
			    else
			    	isCheckedIn=false;
			    
			} catch (Exception e) {
				e.printStackTrace();
			}

			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			setButtonListeners();
			dialog.dismiss();
		}
		
		
	}

	//_______________________________________________________________________________
	//LOCATION_IMPLEMENTATION________________________________________________________
	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		
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
			Log.d(TAG,""+connectionResult.getErrorCode());
		}
		
	}

	@Override
	public void onConnected(Bundle connectionHint) {
		getCurrentLocation();
		//mLocationClient.disconnect();
	}

	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onLocationChanged(Location arg0) {
		// TODO Auto-generated method stub
		
	}
	
	public void getCurrentLocation() {
		// If Google Play Services is available
		if (servicesConnected())
			mCurrentLocation = mLocationClient.getLastLocation();	
	}
	
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

}
