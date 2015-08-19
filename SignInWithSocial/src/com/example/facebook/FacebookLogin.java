package com.example.facebook;

import java.net.URL;
import java.util.Arrays;

import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.loginwithsocial.GetProfilePicture;
import com.example.loginwithsocial.R;
import com.example.loginwithsocial.TaskCompletionListener;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.share.ShareApi;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;


public class FacebookLogin extends Activity implements TaskCompletionListener{
	private CallbackManager callbackManager;
	ProgressDialog Dialog;
	TextView title, full_name, email;
	ImageView profile_img;
	Button share, logout;
	private SharedPreferences mPrefs;
	public static final String MyPREFERENCES = "facebook_pref";
	SharedPreferences.Editor editor;
	
	//for sharing
	private boolean canPresentShareDialog;
    private ShareDialog shareDialog;
    
    URL imageURL;
    static Bitmap bitmap = null;
	
    private FacebookCallback<Sharer.Result> shareCallback = new FacebookCallback<Sharer.Result>() {
        @Override
        public void onCancel() {
            Log.d("HelloFacebook", "Canceled");
        }

        @Override
        public void onError(FacebookException error) {
            Log.d("HelloFacebook", String.format("Error: %s", error.toString()));
            String title = "Error";
            String alertMessage = error.getMessage();
            showResult(title, alertMessage);
        }

        @Override
        public void onSuccess(Sharer.Result result) {
            Log.d("HelloFacebook", "Success!");
            if (result.getPostId() != null) {
                String title = "Success";
                result.getPostId();
                String alertMessage = "Posted Sucessfully";
                showResult(title, alertMessage);
            }
        }

        private void showResult(String title, String alertMessage) {
            new AlertDialog.Builder(FacebookLogin.this)
                    .setTitle(title)
                    .setMessage(alertMessage)
                    .setPositiveButton("OK", null)
                    .show();
        }
    };
    
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.profile_layout);
		title = (TextView)findViewById(R.id.title);
		full_name = (TextView)findViewById(R.id.full_name);
		email = (TextView)findViewById(R.id.email);
		share = (Button)findViewById(R.id.share);
		logout = (Button)findViewById(R.id.logout);
		profile_img = (ImageView)findViewById(R.id.profile_img);
		mPrefs = getSharedPreferences(MyPREFERENCES,MODE_PRIVATE);
		hideAllViews();
		initFB();
		performPublish();
		
		
		share.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				postStatusUpdate();
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
	private void initFB(){
		FacebookSdk.sdkInitialize(getApplicationContext());
		//calculateHashKey("com.qiosk.news");
		callbackManager = CallbackManager.Factory.create();
		
        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        //handlePendingAction();
                        Log.i("onSuccess","onSuccess");
                        //updateUI();
                        GraphRequest request = GraphRequest.newMeRequest(
                                loginResult.getAccessToken(),
                                new GraphRequest.GraphJSONObjectCallback() {
                                   @Override
									public void onCompleted(JSONObject object,
											GraphResponse response) {
										// TODO Auto-generated method stub
                                	   try{
										  //Log.i("LoginActivity", response.toString());
										  //Log.i("LoginActivity", object.toString());
										  JSONObject res = new JSONObject(object.toString());
										  Log.i("res="+res,"786="+res);
										  /*add profile details to shared preferences. Use can use sqlite also*/
										  editor = mPrefs.edit();
										  editor.putString("first_name",res.getString("first_name"));
										  editor.putString("last_name",res.getString("last_name"));
										  editor.putString("email",res.getString("email"));
										  editor.putString("id",res.getString("id"));
										  editor.commit();
										  setupProfileData();
									   }catch(Exception e){
                                		   e.printStackTrace();
                                		   
                                	   }
									}
                                });
                        Bundle parameters = new Bundle();
                        parameters.putString("fields", "first_name,last_name,email");
                        request.setParameters(parameters);
                        request.executeAsync();
                     }

                    @Override
                    public void onCancel() {
                    	Log.i("onCancel", "onCancel");
                    	if(Dialog != null && Dialog.isShowing()){
                    		Dialog.dismiss();
                    	}
                    	FacebookLogin.this.finish();
                    		
                    }

                    @Override
                    public void onError(FacebookException exception) {
                    	FacebookLogin.this.finish();
                    }
                   
                });
        
        //for sharing
        shareDialog = new ShareDialog(this);
        shareDialog.registerCallback(
                callbackManager,
                shareCallback);
        canPresentShareDialog = ShareDialog.canShow(
                ShareLinkContent.class);
        
	}
	
	public void performPublish() {
		Log.i("performPublish()","performPublish()");
		Dialog = new ProgressDialog(this);
		Dialog.setMessage("Loading...");
		Dialog.show();
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        Log.i("accessToken="+accessToken,"786");
        if (accessToken == null) {
//            pendingAction = action;
           LoginManager.getInstance().logInWithReadPermissions(
        		   this,
                    Arrays.asList("public_profile", "email"));
            
        }else{
        	setupProfileData();
             
        }
       
    }
	
	private void setupProfileData(){
		if(Dialog.isShowing())
    		 Dialog.dismiss();
		showAllViews();
		title.setText("Welcome to Facebook");
		full_name.setText("Hello, "+mPrefs.getString("first_name", "")+" "+mPrefs.getString("last_name", ""));
		email.setText("("+mPrefs.getString("email", "")+")");
		String img_url = "https://graph.facebook.com/" + mPrefs.getString("id", "") + "/picture?type=large";
		if(bitmap ==null)
			new GetProfilePicture(FacebookLogin.this).execute(img_url);
		else
			profile_img.setImageBitmap(bitmap);
		 
	}
	
	
	private void postStatusUpdate() {
        Profile profile = Profile.getCurrentProfile();
        String title = "Shared Using Facebook";
        String details= "shared using latest facebook sdk";
        String url = "https://ketub4.wordpress.com/";
        ShareLinkContent linkContent = new ShareLinkContent.Builder()
                .setContentTitle(title)
                .setContentDescription(details)
                .setContentUrl(Uri.parse(url))
                .build();
        
        if (canPresentShareDialog) {
            shareDialog.show(linkContent);
            
            //ShareApi.share(linkContent, shareCallback);
        } else if (profile != null && hasPublishPermission()) {//for custom share popup dialog
            ShareApi.share(linkContent, shareCallback);
        } else {
//            pendingAction = PendingAction.POST_STATUS_UPDATE;
        }
        
        
    }
	
	private void logout(){
		LoginManager.getInstance().logOut();
		clearSharedPrefernces();
		if(bitmap !=null)
			bitmap = null;
		new AlertDialog.Builder(this)
	    .setTitle("Logout")
	    .setMessage("You have sucessfully logged out.")
	    .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) { 
	            // continue with delete
	        	FacebookLogin.this.finish();
	        }
	     })
	    .show();
		
	}
	private void clearSharedPrefernces(){
		  editor = mPrefs.edit();
		  editor.putString("first_name","");
		  editor.putString("last_name","");
		  editor.putString("email","");
		  editor.putString("id","");
		  editor.commit();
	}
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		callbackManager.onActivityResult(requestCode, resultCode, data);
		
	}
	 private boolean hasPublishPermission() {
	        AccessToken accessToken = AccessToken.getCurrentAccessToken();
	        return accessToken != null && accessToken.getPermissions().contains("publish_actions");
	   
	    }

	@Override
	public void onTwitterLoginComplete() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTwitterActivityFinish() {
		// TODO Auto-generated method stub
		
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
