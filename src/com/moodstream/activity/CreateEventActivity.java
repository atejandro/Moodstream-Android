package com.moodstream.activity;

import java.util.ArrayList;
import java.util.Calendar;

import android.app.Activity;
import android.app.Dialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.moodstream.R;
import com.moodstream.model.eventendpoint.Eventendpoint;
import com.moodstream.model.eventendpoint.model.Event;
import com.moodstream.model.eventendpoint.model.GeoPt;

public class CreateEventActivity extends Activity {

	protected static String usr;
	
	private static final String TAG="CreateEventActivity";
	
	private EditText eventName;
	private EditText eventDescription;
	private Button inviteFriendsBtn;
	private Button createEventBtn;
	
	private OnClickListener createEventListener=new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			
			//Execute Async Task to Create event
			new CreateEventTask().execute();
			finish();
			
		}
	};
	
	private OnClickListener inviteFriendsListener=new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			popupFriendsDialog(v);
			
		}


	};
	
	@SuppressWarnings("null")
	private void popupFriendsDialog(View v) {
		
		ArrayList<String> friends = null;
		
		friends.add("@laura");
		friends.add("@johis");
		friends.add("@pedro");
		friends.add("@vlad");
		
		
		final Dialog dialog =new Dialog(CreateEventActivity.this);
        dialog.setContentView(R.layout.dialog_listfriends);
        dialog.setTitle("Select Friends");
        ListView listView = (ListView) dialog.findViewById(R.id.dialog_listview);

        ArrayAdapter<String> ad =new ArrayAdapter<String>(getApplicationContext(), R.layout.item_friend, R.id.friendItem, friends);
        listView.setAdapter(ad);
		
        listView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
        
        dialog.show();
	}
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_createevent);
	
		eventName=(EditText) findViewById(R.id.eventName);
		eventDescription=(EditText) findViewById(R.id.eventDescription);
		inviteFriendsBtn=(Button)findViewById(R.id.inviteFriendsBtn);
		inviteFriendsBtn.setOnClickListener(inviteFriendsListener);
		createEventBtn=(Button) findViewById(R.id.createEventBtn);
		createEventBtn.setOnClickListener(createEventListener);
		
	}
	
	
	private class CreateEventTask extends AsyncTask<Void, Void, Void>{

		@Override
		protected Void doInBackground(Void... params) {
			Log.d(TAG,"Starting to create event");
			//Calendar to create Date format
			Calendar calendar=Calendar.getInstance();
			DateTime date=new DateTime(calendar.getTime());
			
			//TODO: Get current location. Getting constant location meanwhile.	
			GeoPt gpt=new GeoPt();
			gpt.setLatitude((float) 4.685639);
			gpt.setLongitude( (float) -74.054111);
			
	
			
			//Create Event
			Event e=new Event();
			e.setEventName(eventName.getText().toString());
			e.setDescription(eventDescription.getText().toString());
			Log.d(TAG,"Setting even owner "+usr+" ...");
			e.setCreator(usr);
			e.setDate(date);
			e.setLocation(gpt);
			
			
			Log.d(TAG,"Calling Backend");
			//Call backend
			//1. Create Builder
			Eventendpoint.Builder builder=new Eventendpoint.Builder(AndroidHttp.newCompatibleTransport(), 
							new JacksonFactory(), null);
			builder=CloudEndpointUtils.updateBuilder(builder);
			//2. Assign builder to Endpoint
			Eventendpoint endpoint=builder.build();
			Log.d(TAG,"Build Succeed...");
			try {
				Log.d(TAG,"Inserting event...");
				endpoint.insertEvent(e).execute();
				Log.d(TAG,"Event inserted");
			} catch (Exception e2) {
				e2.printStackTrace();
			}
			
			return null;
		}
		
	}

	
}
