package com.moodstream.adapter;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.moodstream.R;
import com.moodstream.model.photoendpoint.model.Photo;
import com.moodstream.util.DateUtils;
import com.moodstream.util.PhotoImageLoader;

public class PhotoListAdapter extends BaseAdapter {
	
	
	 	private Activity activity;
	    //private String[] data;
	    private Photo[] photoData;
	    private List<Photo> photos=null;
	    private static LayoutInflater inflater=null;
	    public PhotoImageLoader imageLoader; 
	    
	    public PhotoListAdapter(Activity a, List<Photo> p) {
	        activity = a;
	        
	        if(p==null)
	        	photos=null;
	        else 
	        	photos=p;
	        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	        imageLoader=new PhotoImageLoader(activity.getApplicationContext());
	    }

	    public int getCount() {
	        //return data.length;
	    	if(photos==null)
	    		return 0;
	    	else
	    		return photos.size();
	    }

	    public Object getItem(int position) {
	        return position;
	    }

	    public long getItemId(int position) {
	        return position;
	    }
	    
	    public View getView(int position, View convertView, ViewGroup parent) {
	        View vi=convertView;
	        if(convertView==null)
	            vi = inflater.inflate(R.layout.item_eventphoto, null);

	        TextView photo_taken_by=(TextView)vi.findViewById(R.id.photo_taken_by);
	        photo_taken_by.setText(photos.get(position).getOwnerNickname());
	        
	        TextView photo_description=(TextView)vi.findViewById(R.id.photo_description);
	        photo_description.setText(photos.get(position).getCaption());
	        
	        TextView photo_upload_time=(TextView)vi.findViewById(R.id.photo_upload_time);
	        photo_upload_time.setText(DateUtils.getTimeDifference(photos.get(position).getUploadTime()));
	        
	        ImageView image=(ImageView)vi.findViewById(R.id.event_photo);
	        String path=photos.get(position).getEventId()+"/"+photos.get(position).getBlobPath();
	        imageLoader.DisplayImage(path, image);
	        return vi;
	    }
}
