package com.moodstream.activity;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.json.jackson2.JacksonFactory;
//import com.google.api.client.util.IOUtils;
import com.moodstream.R;
import com.moodstream.adapter.PhotoListAdapter;
import com.moodstream.model.eventendpoint.model.Event;
import com.moodstream.model.photoendpoint.Photoendpoint;
import com.moodstream.model.photoendpoint.model.CollectionResponsePhoto;
import com.moodstream.model.photoendpoint.model.Photo;
import com.moodstream.util.Credentials;

public class EventDetailsActivity extends Activity {
	
	private static final String TAG="EventDetailsActivity";
	
	//AWS S3 initialization
   private AmazonS3Client s3Client = new AmazonS3Client(
					new BasicAWSCredentials(Credentials.ACCESS_KEY_ID,
											Credentials.SECRET_KEY));
	
	protected static Event selectedEvent;
	protected static String usrNickname;
	
	private static final int ACTION_TAKE_PHOTO=1;//Take a big photo
	private boolean isCheckedIn=false;
	
	
	private TextView detail_event_name;
	private TextView detail_event_invitees;
	private TextView detail_event_date;
	private TextView detail_event_lat;
	private TextView detail_event_lng;
	private Button take_photo_btn;
	
	//ListView
    private ListView photoList;
    private PhotoListAdapter adapter;
	private List<Photo> photos=null;
	
	//File management
	private File mPhotoFile;
	private String timeStamp;
	
	
	
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
		
		detail_event_lat=(TextView)findViewById(R.id.detail_event_lat);
		detail_event_lat.setText("Lat: "+selectedEvent.getLocation().getLatitude());
		
		detail_event_lng=(TextView)findViewById(R.id.detail_event_lng);
		detail_event_lng.setText("Lng: "+selectedEvent.getLocation().getLongitude());
		
		detail_event_date=(TextView)findViewById(R.id.detail_event_date);
		detail_event_date.setText("Date: "+selectedEvent.getDate());
		
		take_photo_btn=(Button)findViewById(R.id.take_photo_btn);
		setButtonListeners();
		
		photoList=(ListView)findViewById(R.id.photosList);
		
		//TODO Call ListEventPhotosTask
		new ListEventPhotoTask().execute();
		
		//TODO setOnItemClickListener
		
		mPhotoFile=null;
		CreatePhotoActivity.photoFile=null;	
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
			{
				Log.d(TAG,"The path or the File are null");
			}
				
			break;

		}
	}
	
	
	
	private void setButtonListeners()
	{
		take_photo_btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//Go to a cam activity
				takePictureIntent(ACTION_TAKE_PHOTO);
			}
		});
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
				CreatePhotoActivity.usrNickname=usrNickname;
				
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
		
			CollectionResponsePhoto result;
			Photoendpoint endpoint=builder.build();
			
			try {
				result=endpoint.listPhoto().setEventId(selectedEvent.getKey().getId()).execute();
				
				/*
				S3ObjectInputStream content = s3Client.getObject(Credentials.getPictureBucket(), <the key of the targeted file>).getObjectContent();
				byte[] bytes = IOUtils.toByteArray(content);
				Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
				*/
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
			
			/*ListAdapter photosListAdapter=createPhotoListAdapter(result.getItems());
			photoList.setAdapter(photosListAdapter);
			Log.d(TAG,"Photo list adapter settled");
			photos=result.getItems();*/
			
		}
		
		/**
		 * Populate a List Adapter with the data
		 * */
		private ListAdapter createPhotoListAdapter(List<Photo> photos) {
			
		    List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
		    
		    for(Photo photo:photos){
		    	Map<String,Object> map=new HashMap<String, Object>();
		    	//TODO: Change the ic_launcher icon
		    	map.put("takenBy", photo.getOwnerNickname());
		    	map.put("description",photo.getCaption() );
		    	map.put("uploadTime",photo.getUploadTime() );
		    	map.put("blob", new FetchBlobTask().execute(selectedEvent.getKey().getId()+"/"+photo.getBlobPath()));
		    	data.add(map);
		    }
		    
		    SimpleAdapter adapter=new SimpleAdapter(EventDetailsActivity.this,data,R.layout.item_eventphoto,
		    						new String[]{"takenBy","description","uploadTime","blob"},
		    						new int[]{R.id.photo_taken_by,R.id.photo_description,R.id.photo_upload_time,R.id.event_photo});
		    
			return adapter;
		}
		
		
		
		private class FetchBlobTask extends AsyncTask<String, Void, Bitmap>
		{

			@Override
			protected Bitmap doInBackground(String... path) {
				
				Bitmap bitmap=null;
				
				try {
					S3ObjectInputStream content = s3Client.getObject(Credentials.getPictureBucket(),path[0]).getObjectContent();
					byte[] bytes = IOUtils.toByteArray(content);
					bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
					
				} catch (Exception e) {
					e.printStackTrace();
				}		
				return bitmap;
			}
			
		}
		
	}
}
