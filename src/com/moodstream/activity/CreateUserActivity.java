package com.moodstream.activity;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.moodstream.R;
import com.moodstream.model.userendpoint.Userendpoint;
import com.moodstream.model.userendpoint.model.User;

public class CreateUserActivity extends Activity {

private static final String TAG="CreateUserActivity";
	
	private EditText nickname;
	private EditText name;
	private EditText lastname;
	private EditText email;
	
	private Button save_button;
	
	private OnClickListener save_listener=new OnClickListener() {
		
		@SuppressLint("ShowToast")
		@Override
		public void onClick(View v) {
			//Async Task to Save Data into Datastore
			Log.e(TAG,"entering async task");
			new CreateUserTrask().execute();
			//Redirect to another Activity
			Toast.makeText(getApplicationContext(), "User Created",	3000).show();
			startActivity(new Intent(CreateUserActivity.this,StartActivity.class));
		}
	};
	
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_createuser);
		
		nickname=(EditText) findViewById(R.id.user_nickname);
		name=(EditText)findViewById(R.id.user_name);
		lastname=(EditText) findViewById(R.id.user_lastname);
		email=(EditText) findViewById(R.id.user_email);
		
		save_button=(Button) findViewById(R.id.send);
		save_button.setOnClickListener(save_listener);
		
		//new CreateUserTrask().execute();
		
	}
	
	private class CreateUserTrask extends AsyncTask<Void, Void, Void>{

		@Override
		protected Void doInBackground(Void... params) {
			
			User usr=new User();
			
			
			usr.setNickname("@"+nickname.getText().toString());
			usr.setName(name.getText().toString());
			usr.setLastName(lastname.getText().toString());
			usr.setEmail(email.getText().toString());
			
			Log.d(TAG,"Calling User BackEnd...");
			//Call backend
			Userendpoint.Builder builder=new Userendpoint.Builder(
					AndroidHttp.newCompatibleTransport(),
					new JacksonFactory(), 
					null);
			
			Log.d(TAG,"Building Endpoint...");
			builder=CloudEndpointUtils.updateBuilder(builder);
			Userendpoint endpoint=builder.build();
			Log.d(TAG,"Build Succeed..");
			try {
				Log.d(TAG,"Inserting User...");
				endpoint.insertUser(usr).execute();
				Log.d(TAG,"User "+usr.getNickname()+" inserted");
				/*List<String> friends=new ArrayList<String>();
				
				friends=me.getFriends();
				friends.add("johis");
				me.setFriends(friends);
				Log.i(TAG,"Updating user");
				endpoint.updateUser(me).execute();
				Log.i(TAG,"User updated");
				//endpoint.insertUser(usr).execute();*/
			
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			return null;
		}
		
	}

}
