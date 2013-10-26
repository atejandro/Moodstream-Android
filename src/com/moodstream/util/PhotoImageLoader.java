package com.moodstream.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.widget.ImageView;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.S3Object;
import com.moodstream.R;


/**
 * Class that handles the image loading from AWS S3 in case its needed.
 * */
public class PhotoImageLoader {
	
		//Memory Cache Variables
	 	MemoryCache memoryCache=new MemoryCache();
	    PhotoFileCache fileCache;
	    
	    //Collection of ImageViews
	    private Map<ImageView, String> imageViews=Collections.synchronizedMap(new WeakHashMap<ImageView, String>());
	    
	    //Concurrent thread hadlers
	    ExecutorService executorService;
	    Handler handler=new Handler();//handler to display images in UI thread
	    
	    //Photo in case the image doesn't load as required
	    final int stub_id=R.drawable.ic_launcher;
	    
	    
	    //Constructor
	    public PhotoImageLoader(Context context){
	        fileCache=new PhotoFileCache(context);
	        executorService=Executors.newFixedThreadPool(5);
	    }
	    
	    
	    /**
	     * Display an image
	     * */
	    public void DisplayImage(String url, ImageView imageView)
	    {
	    	//Assign a new image view
	        imageViews.put(imageView, url);
	        
	        //Verify if the photo is already in cache
	        Bitmap bitmap=memoryCache.get(url);
	        if(bitmap!=null)
	            imageView.setImageBitmap(bitmap);
	        else
	        {
	        	
	            queuePhoto(url, imageView);
	            imageView.setImageResource(stub_id);
	        }
	    }
	    
	    
	    /**
	     * Loads a photo with the specific url
	     * 
	     * TODO Chage the executorservice to AsyncTask
	     * */
	    private void queuePhoto(String url, ImageView imageView)
	    {
	        PhotoToLoad p=new PhotoToLoad(url, imageView);
	        //executorService.submit(new PhotosLoader(p));
	        new PhotosLoaderTask().execute(p);
	    }
	    
	    
	    
	    /**
	     * 
	     * @param url
	     * @return
	     */
	    private Bitmap getBitmap(String url) 
	    {
	        File f=fileCache.getFile(url);
	        
	        //from SD cache
	        Bitmap b = decodeFile(f);
	        if(b!=null)
	            return b;
	        
	        //from web
	        try {
	           Bitmap bitmap=null;
	            
	           AmazonS3 s3Client = new AmazonS3Client(
						new BasicAWSCredentials(AWSUtils.ACCESS_KEY_ID,
												AWSUtils.SECRET_KEY));
	           
	            S3Object object =s3Client.getObject(AWSUtils.getPictureBucket(),url);
	            
	            InputStream is=object.getObjectContent(); 
	            OutputStream os = new FileOutputStream(f);
	            FileUtils.CopyStream(is, os);
	            os.close();
	            bitmap = decodeFile(f);
	            return bitmap;
	        } catch (Throwable ex){
	           ex.printStackTrace();
	           if(ex instanceof OutOfMemoryError)
	               memoryCache.clear();
	           return null;
	        }
	    }

	    /**
	     * decodes image and scales it to reduce memory consumption
	     * @param f
	     * @return
	     */
	    private Bitmap decodeFile(File f){
	        try {
	               //decode image size
	                BitmapFactory.Options o = new BitmapFactory.Options();
		            o.inJustDecodeBounds = true;
		            FileInputStream stream1=new FileInputStream(f);
		            BitmapFactory.decodeStream(stream1,null,o);
		            stream1.close();
		            
		            //Find the correct scale value. It should be the power of 2.
		            final int REQUIRED_SIZE=70;
		            int width_tmp=o.outWidth, height_tmp=o.outHeight;
		            int scale=1;
		            while(true)
		            {
		                if(width_tmp/2<REQUIRED_SIZE || height_tmp/2<REQUIRED_SIZE)
		                    break;
		                width_tmp/=2;
		                height_tmp/=2;
		                scale*=2;
		            }
		            
		            //decode with inSampleSize
		            BitmapFactory.Options o2 = new BitmapFactory.Options();
		            o2.inSampleSize=scale;
		            FileInputStream stream2=new FileInputStream(f);
		            Bitmap bitmap=BitmapFactory.decodeStream(stream2, null, o2);
		            stream2.close();
		            return bitmap;
	        } catch (FileNotFoundException e) {
	        } 
	        catch (IOException e) {
	            e.printStackTrace();
	        }
	        return null;
	    }
	    
	    
	    /**
	     * Internal class that contains the an ImageView and its assigned url.
	     * */
	    private class PhotoToLoad
	    {
	        public String url;
	        public ImageView imageView;
	        public PhotoToLoad(String u, ImageView i){
	            url=u; 
	            imageView=i;
	        }
	    }
	    
	    
	    
	    private class PhotosLoaderTask extends AsyncTask<PhotoToLoad, Void, Void>
	    {

			@Override
			protected Void doInBackground(PhotoToLoad... photos) {
				try{
	            	//verify if the image is already loaded
	                if(imageViewReused(photos[0]))
	                    cancel(true);//end the task
	                
	                //get the bitmap from AWS S3
	                Bitmap bmp=getBitmap(photos[0].url);
	                
	                //Put the bitmap into memory cache
	                memoryCache.put(photos[0].url, bmp);
	                
	                if(imageViewReused(photos[0]))
	                	 cancel(true);//end the task
	                
	                BitmapDisplayer bd=new BitmapDisplayer(bmp, photos[0]);
	                handler.post(bd);
	                
	            }catch(Throwable th){
	                th.printStackTrace();
	            }
				return null;
			}
	    	
	    }
	    
	    
	    /**
	     * TODO Replace this for the Async Task
	     * */
	    /*class PhotosLoader implements Runnable {
	        PhotoToLoad photoToLoad;
	        PhotosLoader(PhotoToLoad photoToLoad){
	            this.photoToLoad=photoToLoad;
	        }
	        
	        @Override
	        public void run() {
	            try{
	            	//verify if the image is already loaded
	                if(imageViewReused(photoToLoad))
	                    return;
	                
	                //get the bitmap from AWS S3
	                Bitmap bmp=getBitmap(photoToLoad.url);
	                //Put the bitmap into memory cache
	                memoryCache.put(photoToLoad.url, bmp);
	                if(imageViewReused(photoToLoad))
	                    return;
	                BitmapDisplayer bd=new BitmapDisplayer(bmp, photoToLoad);
	                handler.post(bd);
	            }catch(Throwable th){
	                th.printStackTrace();
	            }
	        }
	    }*/
	    
	    
	    /**
	     * Verify if the photo is already in memory
	     * @param photoToLoad
	     * @return
	     */
	    boolean imageViewReused(PhotoToLoad photoToLoad){
	        String tag=imageViews.get(photoToLoad.imageView);
	        if(tag==null || !tag.equals(photoToLoad.url))
	            return true;
	        return false;
	    }
	    
	    /**
	     * Used to display bitmap in the UI thread
	     *
	     */
	    class BitmapDisplayer implements Runnable
	    {
	        Bitmap bitmap;
	        PhotoToLoad photoToLoad;
	        public BitmapDisplayer(Bitmap b, PhotoToLoad p){bitmap=b;photoToLoad=p;}
	        public void run()
	        {
	            if(imageViewReused(photoToLoad))
	                return;
	            if(bitmap!=null)
	                photoToLoad.imageView.setImageBitmap(bitmap);
	            else
	                photoToLoad.imageView.setImageResource(stub_id);
	        }
	    }

	    public void clearCache() {
	        memoryCache.clear();
	        fileCache.clear();
	    }

}
