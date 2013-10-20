package com.moodstream.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.moodstream.R;
import com.moodstream.model.userendpoint.Userendpoint;
import com.moodstream.model.userendpoint.model.CollectionResponseUser;
import com.moodstream.model.userendpoint.model.User;

public class AddFriendActivity extends Activity {
	
	private static final String TAG="AddFriendActivity";
	
	protected static User currentUsr;
	
	private ListView friendsList;
	private List<String> friends;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_addfriend);
		friendsList=(ListView)findViewById(R.id.friends_listview);
		new ListFriendsTask().execute();
		friendsList.setOnItemClickListener(friendsListClickListener);
	}
	
	//--------------ASYNC TASK-----------------------------//
	
	private class ListFriendsTask extends AsyncTask<Void, Void, CollectionResponseUser>
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
			ListAdapter userListAdapter=createUserListAdapter(result.getItems());
			friendsList.setAdapter(userListAdapter);
			
		}

	
	}
	
	
	private class AddFriendTask extends AsyncTask<String, Void, Void>{

		@Override
		protected Void doInBackground(String... params) {
			
			List<String> newFriends=new ArrayList<String>();
			
			if(currentUsr.getFriends()!=null)
			newFriends=currentUsr.getFriends();
			
			newFriends.add(params[0]);
			
			currentUsr.setFriends(newFriends);
			
			Userendpoint.Builder builder=new Userendpoint.Builder(AndroidHttp.newCompatibleTransport(), new JacksonFactory(), null);
			builder=CloudEndpointUtils.updateBuilder(builder);
					
			Userendpoint endpoint=builder.build();
			
			try {
				endpoint.updateUser(currentUsr).execute();
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			return null;
		}	
	}
	
    private ListAdapter createUserListAdapter(List<User> friendslist) {
    	friends=new ArrayList<String>();
    	
    	for(User u:friendslist)
    		if(!u.getNickname().equals(currentUsr.getNickname())&&!currentUsr.getFriends().contains(u.getNickname()))
    		friends.add(u.getNickname());
    	
		ArrayAdapter<String> adapter=new ArrayAdapter<String>(this, R.layout.item_friend, R.id.friendItem, friends);
		return adapter;
	}
    
    private OnItemClickListener friendsListClickListener=new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {		
			new AddFriendTask().execute(friends.get((int)arg3));	
			finish();
		}
	};

}
