package com.moodstream.activity;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
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
import android.widget.ImageView;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.ResponseHeaderOverrides;
import com.moodstream.R;
import com.moodstream.model.eventendpoint.model.Event;
import com.moodstream.model.photoendpoint.model.Photo;
import com.moodstream.model.userendpoint.model.User;
import com.moodstream.util.AWSUtils;

public class TakePhotoActivity extends Activity {
	//REFERENCES TO PREVIOUS ACTIVITY
		protected static User currentUser;
		protected static Event currentEvent;
		
		
		//AWS S3 initialization
		private AmazonS3Client s3Client = new AmazonS3Client(
				new BasicAWSCredentials(AWSUtils.ACCESS_KEY_ID,
						AWSUtils.SECRET_KEY));
		

		//ATTRIBUTES
		private static final String TAG="TakePhotoActivity";
		private static final int ACTION_TAKE_PHOTO=1;//Take a big photo
		private static final String BITMAP_STORAGE_KEY = "viewbitmap";//For saving the bitmap
		private static final String IMAGEVIEW_VISIBILITY_STORAGE_KEY = "imageviewvisibility";//For saving the ImageView state
		
		private static final int THUMBNAIL_SIZE=30;
		private static final int UPLOAD_PHOTO_SIZE=60; 
		
		private ImageView mImageView;
		private Bitmap mImageBitmap;
		private File photoFile;
		
		private Button camera_button;
		private Button send_button;
		
		private String mCurrentPhotoPath;
		private String timeStamp;
		
		
		/*LISTENERS*/
		private OnClickListener cambtn_listener=new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					//Go to a cam activity
					takePictureIntent(ACTION_TAKE_PHOTO);
				}
			};
			
		private OnClickListener sendbtn_listener=new OnClickListener() {
			
			@Override
			public void onClick(View v) {	
				uploadPhoto(photoFile);
				galleryAddPic();//Gallery
				mCurrentPhotoPath = null;
				
			}
		};
		
		
		/*OVERRIDES*/
		@Override
		protected void onCreate(Bundle savedInstanceState) {
			// TODO Auto-generated method stub
			super.onCreate(savedInstanceState);
			setContentView(R.layout.activity_takephoto);
			
			photoFile=null;
			
			camera_button=(Button) findViewById(R.id.take_photo);
			camera_button.setOnClickListener(cambtn_listener);
			
			send_button=(Button)findViewById(R.id.upload_image);
			send_button.setOnClickListener(sendbtn_listener);
			
			mImageView=(ImageView) findViewById(R.id.image_preview);
			mImageBitmap=null;
		}
		

		
		@Override
		protected void onActivityResult(int requestCode, int resultCode, Intent data) {
			
			switch (requestCode) {
			case ACTION_TAKE_PHOTO:
				if(resultCode==RESULT_OK)
					 handleBigCameraPhoto();
				break;

			}
			
		}
		
		/**
		 * Handles memory when the screen flips orientation
		 * */
		@Override
		protected void onSaveInstanceState(Bundle outState) {
			outState.putParcelable(BITMAP_STORAGE_KEY, mImageBitmap);
			outState.putBoolean(IMAGEVIEW_VISIBILITY_STORAGE_KEY, (mImageBitmap != null) );
			super.onSaveInstanceState(outState);
		}
		
		/**
		 * Restores memory due to screen orientation
		 * */
		@Override
		protected void onRestoreInstanceState(Bundle savedInstanceState) {
			super.onRestoreInstanceState(savedInstanceState);
			mImageBitmap = savedInstanceState.getParcelable(BITMAP_STORAGE_KEY);
			mImageView.setImageBitmap(mImageBitmap);
			mImageView.setVisibility(
					savedInstanceState.getBoolean(IMAGEVIEW_VISIBILITY_STORAGE_KEY) ? 
							ImageView.VISIBLE : ImageView.INVISIBLE
			);
			
		}
		
		
		//**********************************************************************************//
		//********************************** METHODS ***************************************//
		//**********************************************************************************//
		
		
		/**
		 * Invokes an event to capture a photo
		 */
		private void takePictureIntent(int actionCode){
			
			//Create the camera picture intent
			Intent takePictureIntent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			
			switch(actionCode)
			{
			case ACTION_TAKE_PHOTO:
				
				
				try {
					Log.d(TAG, "trying to make intent");
					photoFile = setUpPhotoFile();
					mCurrentPhotoPath = photoFile.getAbsolutePath();
					takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
				} catch (IOException e) {
					Log.d(TAG, "intent failed");
					e.printStackTrace();
					photoFile = null;
					mCurrentPhotoPath = null;
				}
				break;
				
				default:break;
			}
			
			startActivityForResult(takePictureIntent, actionCode);
		}
		
		
		/**
		 * Gets the photo, pre-scales it and adds it to the phones gallery
		 */
		private void handleBigCameraPhoto() {

			if (mCurrentPhotoPath != null) {
				setPic();
			}

		}
		
		
		/**
		 * It pre-scales the picture taken depending on the user's screen
		 * Sets the mImageView with the processed bitmap*/
		private void setPic() {

			/* There isn't enough memory to open up more than a couple camera photos */
			/* So pre-scale the target bitmap into which the file is decoded */

			/* Get the size of the ImageView */
			int targetW = mImageView.getWidth();
			int targetH = mImageView.getHeight();

			/* Get the size of the image */
			BitmapFactory.Options bmOptions = new BitmapFactory.Options();
			bmOptions.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
			int photoW = bmOptions.outWidth;
			int photoH = bmOptions.outHeight;
			
			/* Figure out which way needs to be reduced less */
			int scaleFactor = 1;
			if ((targetW > 0) || (targetH > 0)) {
				scaleFactor = Math.min(photoW/targetW, photoH/targetH);	
			}

			/* Set bitmap options to scale the image decode target */
			bmOptions.inJustDecodeBounds = false;
			bmOptions.inSampleSize = scaleFactor;
			bmOptions.inPurgeable = true;

			/* Decode the JPEG file into a Bitmap */
			Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
			mImageBitmap=bitmap;
			
			/* Associate the Bitmap to the ImageView */
			mImageView.setImageBitmap(bitmap);
			mImageView.setVisibility(View.VISIBLE);
		}
		
		

		//Saves the image in the gallery
		private void galleryAddPic() {
			    Intent mediaScanIntent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
				File f = new File(mCurrentPhotoPath);
			    Uri contentUri = Uri.fromFile(f);
			    mediaScanIntent.setData(contentUri);
			    this.sendBroadcast(mediaScanIntent);
		}
		
		

		private void uploadPhoto(File file)
		{
			//TODO
			
			//1.Compress the photo to a small size
			//Thumbnail
			//File thumbnail=decodeFile(file,THUMBNAIL_SIZE);
			
			//Normal Size
			//File photo=decodeFile(file,UPLOAD_PHOTO_SIZE);
			
			//2.Send the photo to AWS S3
			new S3UploadPhotoTask().execute();
				
			//3.Send photo meta-data to AppEngine via Endpoints
			//3.1. Get presigned url from AWS S3
			new S3GeneratePresignedUrlTask().execute();
			//3.2. Persist information in the datastore
		}
		
		
		//**********************************************************************************//
		//***************************** FILE MANAGEMENT ************************************//
		//**********************************************************************************//
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
								  "/Moodstream,/");

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
		
		private File setUpPhotoFile() throws IOException {
			
			File f = createImageFile();
			mCurrentPhotoPath = f.getAbsolutePath();
			
			return f;
		}
		
		/**
		 * Creates the image file
		 * */
		@SuppressLint("SimpleDateFormat")
		private File createImageFile() throws IOException {
			// Create an image file name
			timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
			String imageFileName = "IMG_" + timeStamp + "_";
			
		    File albumF = getAlbumDir();//get album address
			File imageF = File.createTempFile(imageFileName, ".jpg", albumF);
			return imageF;
		}
		
		
		
		//**********************************************************************************//
		//***************************** INTERNAL CLASSES ************************************//
		//**********************************************************************************//
		
		/**
		 * S3TaskResult
		 * */
		private class S3TaskResult {
			String errorMessage = null;
			Uri uri = null;

			public String getErrorMessage() {
				return errorMessage;
			}

			public void setErrorMessage(String errorMessage) {
				this.errorMessage = errorMessage;
			}

			public Uri getUri() {
				return uri;
			}

			public void setUri(Uri uri) {
				this.uri = uri;
			}
		}
		
		
		/**
		 * Async Tasks
		 * */
		
		private class S3UploadPhotoTask extends AsyncTask<Void, Void, S3TaskResult> {

			ProgressDialog dialog;

			protected void onPreExecute() {
				/*dialog = new ProgressDialog(S3UploaderActivity.this);
				dialog.setMessage(S3UploaderActivity.this
						.getString(R.string.uploading));
				dialog.setCancelable(false);
				dialog.show();*/
			}

			protected S3TaskResult doInBackground(Void... params) {

				S3TaskResult result = new S3TaskResult();
				// Put the image data into S3.
				try {
					
					// Content type is determined by file extension.
					PutObjectRequest por = new PutObjectRequest(
							AWSUtils.getPictureBucket(), 
							"events/"+photoFile.getName(),
							photoFile)
					.withCannedAcl(CannedAccessControlList.PublicRead);
					s3Client.putObject(por);
					
				} catch (Exception exception) {
					result.setErrorMessage(exception.getMessage());
				}

				return result;
			}

			protected void onPostExecute(S3TaskResult result) {

				/*dialog.dismiss();

				if (result.getErrorMessage() != null) {

					displayErrorAlert(
							S3UploaderActivity.this
									.getString(R.string.upload_failure_title),
							result.getErrorMessage());
				}*/
			}
		}
		
		
		
		
		
		private class S3GeneratePresignedUrlTask extends AsyncTask<Void, Void, S3TaskResult> {

			protected S3TaskResult doInBackground(Void... voids) {

				S3TaskResult result = new S3TaskResult();
			
					try {
						// Ensure that the image will be treated as such.
						ResponseHeaderOverrides override = new ResponseHeaderOverrides();
						override.setContentType("image/jpeg");
				
						// Generate the presigned URL.
				
						// Added an hour's worth of milliseconds to the current time.
						Date expirationDate = new Date(
								System.currentTimeMillis() + 3600000);
						GeneratePresignedUrlRequest urlRequest = new GeneratePresignedUrlRequest(
								AWSUtils.getPictureBucket(), photoFile.getName());
						urlRequest.setExpiration(expirationDate);
						urlRequest.setResponseHeaders(override);
				
						URL url = s3Client.generatePresignedUrl(urlRequest);
				
						result.setUri(Uri.parse(url.toURI().toString()));
				
						} catch (Exception exception) {
					
							result.setErrorMessage(exception.getMessage());
						}
				
					return result;
				}

			protected void onPostExecute(S3TaskResult result) {
				
				Photo photo=new Photo();
				
				photo.setOwnerNickname(currentUser.getNickname());
				photo.setUploadTime(System.currentTimeMillis());
				
				
				
				
				/*if (result.getErrorMessage() != null) {
			
					displayErrorAlert(
							S3UploaderActivity.this
									.getString(R.string.browser_failure_title),
							result.getErrorMessage());
				} else if (result.getUri() != null) {
			
					// Display in Browser.
					startActivity(new Intent(Intent.ACTION_VIEW, result.getUri()));
				}*/
			}
	}

}
