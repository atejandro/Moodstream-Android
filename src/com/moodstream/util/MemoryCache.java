package com.moodstream.util;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import android.graphics.Bitmap;
import android.util.Log;

/**
 * Class for handling the memory cache of scrolling photo bitmaps
 * */
public class MemoryCache {
	
	private static final String TAG = "MemoryCache";
	
    private Map<String, Bitmap> cache=Collections.synchronizedMap(
            new LinkedHashMap<String, Bitmap>(10,1.5f,true));//Last argument true for LRU ordering
    private long size=0;//current allocated size
    private long limit=1000000;//max memory in bytes

    
    //Constructor
    public MemoryCache(){
        //use 25% of available heap size
        setLimit(Runtime.getRuntime().maxMemory()/4);
    }
    
    
    /**
     * Set new memory limit
     * */
    public void setLimit(long new_limit){
        limit=new_limit;
        Log.i(TAG, "MemoryCache will use up to "+limit/1024./1024.+"MB");
    }

    
    /**
     * Get a bitmap from Memory Cache
     * */
    public Bitmap get(String id){
        try{
        	//Verify if that kwy exists in the map
            if(!cache.containsKey(id))
                return null;
            //NullPointerException sometimes happen here http://code.google.com/p/osmdroid/issues/detail?id=78 
            return cache.get(id);
        }catch(NullPointerException ex){
            ex.printStackTrace();
            return null;
        }
    }

    
    /**
     * Put a bitmap to Memory Cache
     * */
    public void put(String id, Bitmap bitmap){
        try{
            if(cache.containsKey(id))
            {
            	//Decrease the memory size
                size-=getSizeInBytes(cache.get(id));
            }
            //Put new bitmap into Cache
            cache.put(id, bitmap);
            //Increment the memory
            size+=getSizeInBytes(bitmap);
            checkSize();
        }catch(Throwable th){
            th.printStackTrace();
        }
    }
    
    
    /**
     * Checks the cache size of the cache memory. In case the memory exceeds its limit it cleans the least 
     * recently accessed item.
     * */
    private void checkSize() {
        Log.i(TAG, "cache size="+size+" length="+cache.size());
        if(size>limit){
            Iterator<Entry<String, Bitmap>> iter=cache.entrySet().iterator();//least recently accessed item will be the first one iterated  
            while(iter.hasNext()){
                Entry<String, Bitmap> entry=iter.next();
                size-=getSizeInBytes(entry.getValue());
                iter.remove();
                if(size<=limit)
                    break;
            }
            Log.i(TAG, "Clean cache. New size "+cache.size());
        }
    }

    
    /**
     * Clears all the cache memory
     * */
    public void clear() {
        try{
            //NullPointerException sometimes happen here http://code.google.com/p/osmdroid/issues/detail?id=78 
            cache.clear();
            size=0;
        }catch(NullPointerException ex){
            ex.printStackTrace();
        }
    }

    
    /**
     * Get the size of the bitmap in bytes.
     * */
    long getSizeInBytes(Bitmap bitmap) {
        if(bitmap==null)
            return 0;
        return bitmap.getRowBytes() * bitmap.getHeight();
    }

}
