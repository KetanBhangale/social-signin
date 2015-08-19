package com.example.loginwithsocial;

import com.example.facebook.FacebookLogin;
import com.example.linkedin.LinkedInLogin;
import com.example.twitter.TwitterLogin;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {

	Button facebook, twitter, linkedin;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		facebook = (Button) findViewById(R.id.fb);
		facebook.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent i = new Intent(MainActivity.this, FacebookLogin.class);
				MainActivity.this.startActivity(i);
			}
		});
		
		twitter = (Button) findViewById(R.id.tw);
		twitter.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent i = new Intent(MainActivity.this, TwitterLogin.class);
				MainActivity.this.startActivity(i);
			}
		});
		linkedin = (Button) findViewById(R.id.ln);
		linkedin.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent i = new Intent(MainActivity.this, LinkedInLogin.class);
				MainActivity.this.startActivity(i);
			}
		});
	}

	
}
