package com.moodstream.activity;



import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.moodstream.R;

/**
 * The Main Activity.
 * 
 * This activity starts up the RegisterActivity immediately, which communicates
 * with your App Engine backend using Cloud Endpoints. It also receives push
 * notifications from backend via Google Cloud Messaging (GCM).
 * 
 * Check out RegisterActivity.java for more details.
 */
public class MainActivity extends Activity {
	
	
	private Button login_btn;
	private Button register_btn;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		login_btn=(Button) findViewById(R.id.login_btn);
		register_btn=(Button) findViewById(R.id.register_btn);
		
		setButtonListeners();
	}
	
	
	private void setButtonListeners()
	{
		login_btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startActivity(new Intent(MainActivity.this,LoginActivity.class));	
			}
		});
		
		register_btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startActivity(new Intent(MainActivity.this,CreateUserActivity.class));	
			}
		});
	}
}
