package com.example.twitter;

import java.net.URL;

import com.example.loginwithsocial.GetProfilePicture;
import com.example.loginwithsocial.R;
import com.example.loginwithsocial.TaskCompletionListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


public class TwitterLogin extends Activity implements TaskCompletionListener{
	TextView title, full_name, email;
	Button share, logout;
	ImageView profile_img;
	URL imageURL;
    static Bitmap bitmap = null;
	private SharedPreferences mPrefs;
	public static final String MyPREFERENCES = "twitter_pref" ;
	SharedPreferences.Editor editor;
	public static TwitterApp mTwitter;
	private static final String consumer_key = "ZC6EuLiVsfMDUAS7M4eCmza8X";
	private static final String secret_key = "Dy8pNCUyy2DKvEeBtBdaA3BOpnmSmACE3W0IJPo3X2ZrhkrqzC";
	public static TwitterLogin twitt_act;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.profile_layout);
		twitt_act = this;
		title = (TextView)findViewById(R.id.title);
		full_name = (TextView)findViewById(R.id.full_name);
		email = (TextView)findViewById(R.id.email);
		share = (Button)findViewById(R.id.share);
		logout = (Button)findViewById(R.id.logout);
		profile_img = (ImageView)findViewById(R.id.profile_img);
		mPrefs = getSharedPreferences(MyPREFERENCES,MODE_PRIVATE);
		hideAllViews();
		mTwitter = new TwitterApp(this, consumer_key, secret_key);
		
		
		share.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				postTwitt();
			}
		});
		
		logout.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				logout();
			}
		});
		
		
	}
	
	private void hideAllViews(){
		title.setVisibility(View.INVISIBLE);
		full_name.setVisibility(View.INVISIBLE);
		email.setVisibility(View.INVISIBLE);
		share.setVisibility(View.INVISIBLE);
		logout.setVisibility(View.INVISIBLE);
		profile_img.setVisibility(View.INVISIBLE);
	}
	private void showAllViews(){
		title.setVisibility(View.VISIBLE);
		full_name.setVisibility(View.VISIBLE);
		email.setVisibility(View.VISIBLE);
		share.setVisibility(View.VISIBLE);
		logout.setVisibility(View.VISIBLE);
		profile_img.setVisibility(View.VISIBLE);
	}
	
	
	private void logout(){
		
		clearSharedPrefernces();
		mTwitter.resetAccessToken();
		if(bitmap !=null)
			bitmap = null;
		new AlertDialog.Builder(this)
	    .setTitle("Logout")
	    .setMessage("You have sucessfully logged out.")
	    .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) { 
	            // continue with delete
	        	TwitterLogin.this.finish();
	        }
	     })
	    .show();
		
	}
	private void clearSharedPrefernces(){
		  editor = mPrefs.edit();
		  editor.putString("first_name","");
		  editor.putString("last_name","");
		  editor.putString("UserId","");
		  editor.putString("imgUrl","");
		  editor.commit();
	}
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
	}
	
	private void setupProfileData(){
		showAllViews();
		title.setText("Welcome to Twitter");
		full_name.setText("Hello,"+mPrefs.getString("first_name", "")+" "+mPrefs.getString("last_name", ""));
		email.setText("("+mPrefs.getString("UserId", "")+")");
		if(bitmap ==null)
			new GetProfilePicture(TwitterLogin.this).execute(mPrefs.getString("imgUrl",""));
		else
			profile_img.setImageBitmap(bitmap);
	}


	@Override
	public void onTwitterLoginComplete() {
		// TODO Auto-generated method stub
		setupProfileData();
	}




	@Override
	public void onTwitterActivityFinish() {
		// TODO Auto-generated method stub
		this.finish();
	}
	
	private void postTwitt(){
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(TwitterLogin.this);
		 alertDialog.setTitle("Your Twitt");
		 final EditText input = new EditText(TwitterLogin.this);
		 LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
		     LinearLayout.LayoutParams.MATCH_PARENT,
		     LinearLayout.LayoutParams.MATCH_PARENT);
		 input.setLines(4);
		 input.setGravity(Gravity.TOP);
		 input.setLayoutParams(lp);
		 alertDialog.setView(input);
		 
	
		 alertDialog.setPositiveButton("YES",
		     new DialogInterface.OnClickListener() {
		         public void onClick(DialogInterface dialog, int which) {
		             String message = input.getEditableText().toString()+"\n created by https://ketub4.wordpress.com/";
		             new PostTwittTask(TwitterLogin.this).execute(message);
		         }
		         
		     });
	
		 alertDialog.setNegativeButton("NO",
		     new DialogInterface.OnClickListener() {
		         public void onClick(DialogInterface dialog, int which) {
		             dialog.cancel();
		         }
		     });
	
		 alertDialog.show();
		 }
	
	@Override
	protected void onDestroy() {
		if(mTwitter !=null)
			mTwitter = null;
		if(TwitterApp.mProgressDlg !=null)
			TwitterApp.mProgressDlg = null;
		if(twitt_act !=null)
			twitt_act = null;
		super.onDestroy();
	}

	@Override
	public void onGetProfilePicTaskComplete(Bitmap bm) {
		// TODO Auto-generated method stub
		if(bm !=null)
		{
			profile_img.setImageBitmap(bm);
			bitmap = bm;
		}
	}

	
}
