package com.moodstream.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.moodstream.R;
import com.moodstream.model.eventendpoint.Eventendpoint;
import com.moodstream.model.eventendpoint.model.Event;
import com.moodstream.model.userendpoint.Userendpoint;
import com.moodstream.model.userendpoint.model.CollectionResponseUser;
import com.moodstream.model.userendpoint.model.User;

public class AddFriendActivity extends Activity {
	
	private static final String TAG="AddFriendActivity";
	
	protected static User currentUsr;
	
	private ListView friendsList;
	private List<String> friends;
	
	private TextView noFriends;
	
	protected static Boolean inviteFriend=false;
	protected static Event event=null;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_addfriend);
		
		noFriends=(TextView)findViewById(R.id.noFriends);
		friendsList=(ListView)findViewById(R.id.friends_listview);
		
		if(inviteFriend)
		{
			friends=currentUsr.getFriends();
			friendsList.setAdapter(createFriendsListAdapter(friends));
		}
		else
			new ListFriendsToAddTask().execute();

			
		friendsList.setOnItemClickListener(friendsListClickListener);
	}
	
	//--------------ASYNC TASK-----------------------------//
	
	private class ListFriendsToAddTask extends AsyncTask<Void, Void, CollectionResponseUser>
	{

		@Override
		protected CollectionResponseUser doInBackground(Void... params) {
			
			Userendpoint.Builder builder=new Userendpoint.Builder(AndroidHttp.newCompatibleTransport(), new JacksonFactory(), null);
			builder=CloudEndpointUtils.updateBuilder(builder);
			
			CollectionResponseUser result;
			
			Userendpoint endpoint=builder.build();
			
			try {
				result=endpoint.listUser().execute();
			} catch (Exception e) {
				e.printStackTrace();
				result=null;
			}
			return result;
		}
		
		@Override
		protected void onPostExecute(CollectionResponseUser result) {
			super.onPostExecute(result);
			//friends=result.getItems();
			ListAdapter userListAdapter=null;
			
			Log.d(TAG,"Result= :"+result.getItems().toString());
			
			userListAdapter=createUserListAdapter(result.getItems());
			
			if(userListAdapter!=null)
			friendsList.setAdapter(userListAdapter);
			else
				noFriends.setVisibility(View.VISIBLE);	
		}

	
	}
	
	
	private class AddFriendTask extends AsyncTask<String, Void, Void>{

		@Override
		protected Void doInBackground(String... params) {
			Log.d(TAG,"Executing InviteFriendTask");
			
			Userendpoint.Builder builder=new Userendpoint.Builder(AndroidHttp.newCompatibleTransport(), new JacksonFactory(), null);
			builder=CloudEndpointUtils.updateBuilder(builder);
					
			Userendpoint endpoint=builder.build();
			
			try {
				endpoint.addFriend(currentUsr.getNickname(), params[0]).execute();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}	
	}
	
	private class InviteFriendTask extends AsyncTask<String, Void, Void>
	{

		@Override
		protected Void doInBackground(String... params) {
			
			Log.d(TAG,"Executing InviteFriendTask");
			
			Eventendpoint.Builder builder=new Eventendpoint.Builder(AndroidHttp.newCompatibleTransport(), new JacksonFactory(), null);
			builder=CloudEndpointUtils.updateBuilder(builder);
			
			Eventendpoint endpoint=builder.build();
			
			try {
				endpoint.inviteFriend(event.getKey().getId(), params[0]).execute();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}
		
	}
	
	
	
    private ListAdapter createUserListAdapter(List<User> friendslist) {
    	
    	friends=new ArrayList<String>();
    	boolean hasNoFriends=(currentUsr.getFriends()==null);
    	
    	if(friendslist!=null)
    	{
	    	for(User u:friendslist)
	    		if(!u.getNickname().equals(currentUsr.getNickname())&&(hasNoFriends||!currentUsr.getFriends().contains(u.getNickname())))
	    		friends.add(u.getNickname());
	    	
	    	ArrayAdapter<String> adapter=new ArrayAdapter<String>(this, R.layout.item_friend, R.id.friendItem, friends);
			return adapter;
	    }
    	else return null;	
	}
    
    
    private ListAdapter createFriendsListAdapter(List<String> friendsL)
    {
    	return new ArrayAdapter<String>(this, R.layout.item_friend, R.id.friendItem, friendsL);
    }
    
    private OnItemClickListener friendsListClickListener=new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			if(inviteFriend)
				new InviteFriendTask().execute(friends.get((int)arg3));
			else
				new AddFriendTask().execute(friends.get((int)arg3));	
			
			finish();
		}
	};

}
