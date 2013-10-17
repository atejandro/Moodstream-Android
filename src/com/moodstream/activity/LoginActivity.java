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

public class LoginActivity extends Activity {
	
	private static final String TAG="LoginActivity";
	
	private EditText usr_login_field;
	private EditText usr_password_field;
	private Button login_request_btn;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		
		usr_login_field=(EditText) findViewById(R.id.usr_login_field);
		usr_password_field=(EditText) findViewById(R.id.usr_password_field);
		login_request_btn=(Button) findViewById(R.id.login_request_btn);
		
		setButtonListeners();
		
	}
	
	private void setButtonListeners()
	{
		login_request_btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				new LoginUserTask().execute();
				startActivity(new Intent(LoginActivity.this,StartActivity.class));
			}
		});
	}
	
	private class LoginUserTask extends AsyncTask<Void, Void, User>{

		@Override
		protected User doInBackground(Void... params) {
			Userendpoint.Builder builder=new Userendpoint.Builder(AndroidHttp.newCompatibleTransport(), 
																	new JacksonFactory(),
																	null);
			builder=CloudEndpointUtils.updateBuilder(builder);
			Userendpoint endpoint=builder.build();
			
			try {
				User result=endpoint.getUser("@"+usr_login_field.getText().toString()).execute();
				Log.d(TAG,"User "+result.getNickname()+" found");
				
				return result;
			} catch (Exception e) {
				// TODO: handle exception
			}
			
			return null;
		}
		
		@SuppressLint("ShowToast")
		@Override
		protected void onPostExecute(User result) {
			if(result!=null)
			{
				Log.d(TAG,"onPostExecute... passing user to StartActivity");
				StartActivity.currentUsr=result;
			}
			else
			{
				Toast.makeText(getApplicationContext(), "Invalid username", 3000).show();
			}
		}
	}

}
