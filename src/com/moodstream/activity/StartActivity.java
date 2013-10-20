package com.moodstream.activity;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.moodstream.R;
import com.moodstream.model.eventendpoint.Eventendpoint;
import com.moodstream.model.eventendpoint.model.CollectionResponseEvent;
import com.moodstream.model.eventendpoint.model.Event;
import com.moodstream.model.userendpoint.model.User;
import com.moodstream.util.TabsAdapter;

public class StartActivity extends /*SherlockFragmentActivity*/ Activity{
	
		private static final String TAG="StartActivity";
	
		//Reference variables
	    protected static User currentUsr;
	    
	    //ListView
	    private ListView eventList;
		private List<Event> events=null;
	    //Tabs variables
		private ViewPager mViewPager; //For hosting fragment contents
		private TabsAdapter mTabsAdapter;//To handle ActionBar an ViewPager events
		
		private TextView event_list_label;
	    
	   

		@Override
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			
			setContentView(R.layout.activity_start);
			
			event_list_label=(TextView) findViewById(R.id.event_list_label);
			
			eventList=(ListView) findViewById(R.id.eventsList);
			Log.d(TAG,"Calling ListMyEventsTask");
			new ListMyEventsTask().execute();
			Log.d(TAG,"End of ListMyEventsTask");
			eventList.setOnItemClickListener(eventsListClickListener);
			
			
			
			/*
			 * CODE FOR ACTIONBAR SHERLOCK NOT WORKING!!!
			 * 

			//The content view will be set as the pager instead of the whole layout
		    mViewPager=new ViewPager(this);
			mViewPager.setId(R.id.pager);
			setContentView(mViewPager);
			
			//Create ActionBar with its Navigation Mode
			final ActionBar actionbar=getSupportActionBar();
			actionbar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
			
			mTabsAdapter = new TabsAdapter(this, mViewPager);
			mTabsAdapter.addTab(actionbar.newTab().setText("My Events"), CurrentFragment.class, null);
			mTabsAdapter.addTab(actionbar.newTab().setText("Other"), CurrentFragment.class, null);
			*/
			
			
		}

		@Override
		protected void onResume() {
		super.onResume();
		new ListMyEventsTask().execute();
		}


		@Override
		public boolean onCreateOptionsMenu(Menu menu) {
			// TODO Auto-generated method stub
			MenuInflater inflater =getMenuInflater();
			inflater.inflate(R.menu.main, menu);
			return super.onCreateOptionsMenu(menu);
		}
		
		
		public boolean onOptionsItemSelected(MenuItem item) {
			
			switch(item.getItemId())
			{
			case R.id.action_create:
				Log.d(TAG,"Starting intent to CreateEventActivity...");
				CreateEventActivity.usr=currentUsr.getNickname();
				startActivity(new Intent(this,CreateEventActivity.class));
				return true;
				
			case R.id.action_add_friend:
				Log.d(TAG,"Starting intent to AddFriendActivity...");
				AddFriendActivity.currentUsr=currentUsr;
				startActivity(new Intent(this,AddFriendActivity.class));
				
			 default:
		            return super.onOptionsItemSelected(item);
			}
			
		}
		
		
		//AsyncTasks
		
		private class ListMyEventsTask extends AsyncTask<Void, Void, CollectionResponseEvent>
		{

			@Override
			protected CollectionResponseEvent doInBackground(Void... params) {
				
				Log.d(TAG,"Calling Backend...");
				Eventendpoint.Builder endpointBuilder=new Eventendpoint.Builder(AndroidHttp.newCompatibleTransport(),
															new JacksonFactory(), null);
				endpointBuilder=CloudEndpointUtils.updateBuilder(endpointBuilder);
				
				CollectionResponseEvent result=null;
				
				Eventendpoint endpoint=endpointBuilder.build();
				Log.d(TAG,"Build succeed...");
				try {
					Log.d(TAG,"Calling listevent");
					result=endpoint.listEvent().setNickname(currentUsr.getNickname()).execute();
				} catch (Exception e) {
					e.printStackTrace();
					result=null;
				}
				
				return result;
			}
			
			@Override
			protected void onPostExecute(CollectionResponseEvent result) {
				
				if(!result.toString().equals("{}"))
				{
				Log.d(TAG,"Resultset is not null: "+result.toString());
				ListAdapter eventsListAdapter=createEventListAdapter(result.getItems());
				eventList.setAdapter(eventsListAdapter);
				events=result.getItems();	
				}
				else
				{
					Log.d(TAG,"Resultset is null");
					event_list_label.setText("You have no events");
				}
					
			}
			
			
			
			/**
			 * Populate a List Adapter with the data
			 * */
			private ListAdapter createEventListAdapter(List<Event> events) {
				
				final double kilometersInAMile = 1.60934;
			    List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
			    
			    for(Event event:events){
			    	Map<String,Object> map=new HashMap<String, Object>();
			    	//TODO: Change the ic_launcher icon
			    	map.put("eventPhoto", R.drawable.ic_launcher);
			    	map.put("eventName", event.getEventName());
			    	map.put("eventDate", event.getDate());
			    	map.put("eventCreator", event.getCreator());
			    	data.add(map);
			    }
			    
			    SimpleAdapter adapter=new SimpleAdapter(StartActivity.this,data,R.layout.item_event,
			    						new String[]{"eventPhoto","eventName","eventDate","eventCreator"},
			    						new int[]{R.id.event_icon,R.id.event_name,R.id.event_date,R.id.event_creator});
			    
				return adapter;
			}

			
			
		}
		
		
		private OnItemClickListener eventsListClickListener = new OnItemClickListener() {
		    @Override
		    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		      Event selectedEvent = events.get((int) arg3);
		     // Toast.makeText(getApplicationContext(), "Event "+selectedEvent.getEventName()+"selected", 3000).show();
		        
		      EventDetailsActivity.selectedEvent = selectedEvent;
		      EventDetailsActivity.usrNickname=currentUsr.getNickname();
		      startActivity(new Intent(StartActivity.this,EventDetailsActivity.class));
		      }
		  };
		

}
