package com.moodstream.activity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.ResponseHeaderOverrides;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.moodstream.R;
import com.moodstream.model.photoendpoint.Photoendpoint;
import com.moodstream.model.photoendpoint.model.Photo;
import com.moodstream.util.AWSUtils;

public class CreatePhotoActivity extends Activity {
	
	   private static final String TAG="CreatePhotoActivity";
	
	   //AWS S3 initialization
	   private AmazonS3Client s3Client = new AmazonS3Client(
						new BasicAWSCredentials(AWSUtils.ACCESS_KEY_ID,
												AWSUtils.SECRET_KEY));
	
	   protected static String mCurrentPhotoPath;
	   
	   protected static File photoFile;
	   protected static Long eventId;
	   protected static String usrNickname;
	   
	   private static final String BITMAP_STORAGE_KEY = "viewbitmap";//For saving the bitmap
	   private static final String IMAGEVIEW_VISIBILITY_STORAGE_KEY = "imageviewvisibility";//For saving the ImageView state
	   
	   /*FOR PHOTO CONVERSION TODO*/
	   private static final int CONVERT_2_THUMBNAIL=0;
	   private static final int CONVERT_2_NORMAL_SIZE=1;
	   private static final int THUMBNAIL_QUALITY=30;
	   private static final int NORMAL_SIZE_QUALITY=30;

	   private ImageView photo_preview;
	   private Bitmap mImageBitmap;
	   private Button upload_photo_btn;
	   private EditText photo_description;
	   
	   
	   
	   @Override
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.activity_createphoto);
			
			photo_preview=(ImageView)findViewById(R.id.photo_preview);
			mImageBitmap=null;
			photo_description=(EditText)findViewById(R.id.photo_description);
			upload_photo_btn=(Button) findViewById(R.id.upload_photo_btn);
			setButtonListeners();
			handleBigCameraPhoto();
			
		}
	   
	   
	   
	   
	   
	   private void setButtonListeners()
	   {
		   upload_photo_btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {		
				//TODO photo compression
				
				//Log.d(TAG, "Compressing photo files...");
				//photoFile_thumb=compressPhotoFile(CONVERT_2_THUMBNAIL);
				//photoFile_normal=compressPhotoFile(CONVERT_2_NORMAL_SIZE);
				
				Log.d(TAG,"Uploading photo meta-data to GAE...");
				//Upload photo meta-data to datastore
				uploadPhotoGAE();
				
				
				Log.d(TAG,"Uploading photo AWS S3...");
			   //TODO upload photo(s) to S3
			   //uploadPhotoS3(thumbnailFile);
			   //uploadPhotoS3(mediumSizeFile);	
				uploadPhotoS3();
				
				Log.d(TAG,"Saving photo in local memory...");
			   //Save photo into internal storage
				galleryAddPic();
			}
		});
	   }
	   
	   
	   private File compressPhotoFile(int action)
	   {
		   switch(action)
		   {
		   case CONVERT_2_THUMBNAIL:
			   return compressFromBitmap(eventId+"/thumb/"+photoFile.getName(), THUMBNAIL_QUALITY);
			    
		   case CONVERT_2_NORMAL_SIZE:
			   return compressFromBitmap(eventId+"/", NORMAL_SIZE_QUALITY);  
		   }   
		   return null;
	   }
	   
	   private File compressFromBitmap(String path,int quality)
	   {
		   File file = new File(path);
		   FileOutputStream fOut;
		   
			try {
	
				   fOut = new FileOutputStream(file);
				   mImageBitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
				   fOut.flush();
				   fOut.close(); 
				   
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
	
		   return file;
	   }
	   
	   
	   
        //Saves the image in the gallery
 		private void galleryAddPic() {
 			    Intent mediaScanIntent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
 				File f = new File(mCurrentPhotoPath);
 			    Uri contentUri = Uri.fromFile(f);
 			    mediaScanIntent.setData(contentUri);
 			    this.sendBroadcast(mediaScanIntent);
 		}
	   
	   
	   
	   /**
	    * Upload photo to AWS S3
	    * @param file : File to upload
	    * */
	   private void uploadPhotoS3()
	   {
			//Send the photo to AWS S3 via AsyncTask
			new UploadPhotoS3Task().execute();		
	   }
	   
	  
	   /**
	    * Send photo meta-data to AppEngine via Endpoints
	    * */
	   private void uploadPhotoGAE()
	   {  
		   
			//Get presigned url from AWS S3
			//new GeneratePresignedUrlS3Task().execute();
			//Persist information in the datastore
			new UploadPhotoGAETask().execute();
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
			photo_preview.setImageBitmap(mImageBitmap);
			photo_preview.setVisibility(
					savedInstanceState.getBoolean(IMAGEVIEW_VISIBILITY_STORAGE_KEY) ? 
							ImageView.VISIBLE : ImageView.INVISIBLE
			);
			
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

			 // There isn't enough memory to open up more than a couple camera photos
			// So pre-scale the target bitmap into which the file is decoded 

			// Get the size of the ImageView 
			int targetW = photo_preview.getWidth();
			int targetH = photo_preview.getHeight();

			//Get the size of the image 
			BitmapFactory.Options bmOptions = new BitmapFactory.Options();
			bmOptions.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
			//Parse the options into width and height
			int photoW = bmOptions.outWidth;
			int photoH = bmOptions.outHeight;
			
			// Figure out which way needs to be reduced less 
			int scaleFactor = 1;
			if ((targetW > 0) || (targetH > 0)) {
				scaleFactor = Math.min(photoW/targetW, photoH/targetH);	
			}

			//Set bitmap options to scale the image decode target
			bmOptions.inJustDecodeBounds = false;
			bmOptions.inSampleSize = scaleFactor;
			bmOptions.inPurgeable = true;

			// Decode the JPEG file into a Bitmap 
			Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
			mImageBitmap=bitmap;
			
			// Associate the Bitmap to the ImageView
			photo_preview.setImageBitmap(bitmap);
			photo_preview.setVisibility(View.VISIBLE);
		}
		
		
		private void putFileToS3(File file)
		{
			PutObjectRequest por = new PutObjectRequest(
					AWSUtils.getPictureBucket(), 
				            photoFile.getName(),
					photoFile)
			.withCannedAcl(CannedAccessControlList.PublicRead);
			s3Client.putObject(por);
		}
		
		/**
		 * Name: displayErrorAlert
		 * Displays an error alert to the user
		 * @param title: the title of the dialog
		 * @param message: the message to display
		 * 
		 */
		protected void displayErrorAlert(String title, String message) {

			AlertDialog.Builder confirm = new AlertDialog.Builder(this);
			confirm.setTitle(title);
			confirm.setMessage(message);

			confirm.setNegativeButton(
					CreatePhotoActivity.this.getString(R.string.ok),
					new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {

							CreatePhotoActivity.this.finish();
						}
					});

			confirm.show().show();
		}
		
		
		//================================================================================//
		//                               ASYNCTASKS                                       //
		//================================================================================//
		
		private class UploadPhotoS3Task extends AsyncTask<Void, Void, S3TaskResult> {

			ProgressDialog dialog;

			protected void onPreExecute() {
				
				dialog = new ProgressDialog(CreatePhotoActivity.this);
				dialog.setMessage(CreatePhotoActivity.this
						.getString(R.string.uploading));
				dialog.setCancelable(false);
				dialog.show();
			}
			

			@Override
			protected S3TaskResult doInBackground(Void... params)
		    {

					S3TaskResult result = new S3TaskResult();
					// Put the image data into S3.
					try {
						//Upload thumb to S3
						//Log.d(TAG,"Uploading thumbnail to S3...");
						PutObjectRequest por = new PutObjectRequest(AWSUtils.getPictureBucket(), eventId+"/"+photoFile.getName(),photoFile)
						.withCannedAcl(CannedAccessControlList.PublicRead);
						
						s3Client.putObject(por);
						//Log.d(TAG,"Thumbnail uploaded...");
						
						//Upload photo to S3
						/*Log.d(TAG,"Uploading normal photo to S3...");
						PutObjectRequest por = new PutObjectRequest(Credentials.getPictureBucket(), photoFile_normal.getName(),photoFile_normal)
						.withCannedAcl(CannedAccessControlList.PublicRead);
						
						s3Client.putObject(por);
						Log.d(TAG,"Normal photo uploaded...");*/
						
						
					} catch (Exception exception) {
						result.setErrorMessage(exception.getMessage());
					}

					return result;
			}

			
			protected void onPostExecute(S3TaskResult result) {

				dialog.dismiss();

				if (result.getErrorMessage() != null) {

					displayErrorAlert(
							CreatePhotoActivity.this
									.getString(R.string.upload_failure_title),
							result.getErrorMessage());
				}
				else
				{
					//Close activity
					finish();
				}
			}	
		}
		
		
		private class GeneratePresignedUrlS3Task extends AsyncTask<Void, Void, S3TaskResult> {
	
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
							"moodstream2-bucket", photoFile.getName());
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
				
				if (result.getErrorMessage() != null) {
		
					displayErrorAlert(
							CreatePhotoActivity.this
									.getString(R.string.browser_failure_title),
							result.getErrorMessage());
				} else if (result.getUri() != null) {
		
					// Display in Browser.
					startActivity(new Intent(Intent.ACTION_VIEW, result.getUri()));
				}
			}
		}
		
		
		private class UploadPhotoGAETask extends AsyncTask<Void, Void, Void>
		{
			ProgressDialog dialog;

			protected void onPreExecute() {
				/*dialog = new ProgressDialog(CreatePhotoActivity.this);
				dialog.setMessage(CreatePhotoActivity.this
						.getString(R.string.uploading));
				dialog.setCancelable(false);
				dialog.show();*/
			}

			@Override
			protected Void doInBackground(Void... params) {
		
				Photo photo=new Photo();
				
				photo.setBlobPath(photoFile.getName());
				photo.setEventId(eventId);
				photo.setCaption(photo_description.getText().toString());
				photo.setOwnerNickname(usrNickname);
				photo.setUploadTime(System.currentTimeMillis());
				
				
				Photoendpoint.Builder builder=new Photoendpoint.Builder(AndroidHttp.newCompatibleTransport(),
															new JacksonFactory() ,null);
				builder=CloudEndpointUtils.updateBuilder(builder);
				Photoendpoint endpoint=builder.build();
				
				try {
					endpoint.insertPhoto(photo).execute();
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				
				return null;
			}
			
		}
		
		
		
		//================================================================================//
	    //                             INNER CLASSES                                      //
	    //================================================================================//
		
		
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
}

