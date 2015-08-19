package com.example.linkedin;

import java.io.IOException;
import java.net.URL;
import java.util.Calendar;

import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.loginwithsocial.GetProfilePicture;
import com.example.loginwithsocial.R;
import com.example.loginwithsocial.TaskCompletionListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

@SuppressWarnings("deprecation")
public class LinkedInLogin extends Activity implements TaskCompletionListener{

/*CONSTANT FOR THE AUTHORIZATION PROCESS*/
	
	/****YOUR LINKEDIN APP INFO HERE*********/
	private static final String API_KEY = "75g4sponvovu25";
	private static final String SECRET_KEY = "CCho5qeVE9JxRxSn";
	//This is any string we want to use. This will be used for avoid CSRF attacks. You can generate one here: http://strongpasswordgenerator.com/
	private static final String STATE = "4cX3k3k77B5i7gO";//E3ZYKC1T6H2yP4z
	private static final String REDIRECT_URI = "https://ketub4.wordpress.com/";
	
	//private static final String SCOPES = "r_fullprofile%20r_emailaddress%20r_network";
	private static final String SCOPES = "r_basicprofile+r_emailaddress+w_share";
	/*********************************************/
	
	//These are constants used for build the urls
	private static final String AUTHORIZATION_URL = "https://www.linkedin.com/uas/oauth2/authorization";
	private static final String ACCESS_TOKEN_URL = "https://www.linkedin.com/uas/oauth2/accessToken";
	private static final String SECRET_KEY_PARAM = "client_secret";
	private static final String RESPONSE_TYPE_PARAM = "response_type";
	private static final String GRANT_TYPE_PARAM = "grant_type";
	private static final String GRANT_TYPE = "authorization_code";
	private static final String RESPONSE_TYPE_VALUE ="code";
	private static final String CLIENT_ID_PARAM = "client_id";
	private static final String SCOPE_PARAM = "scope";
	private static final String STATE_PARAM = "state";
	private static final String REDIRECT_URI_PARAM = "redirect_uri";
	/*---------------------------------------*/
	private static final String QUESTION_MARK = "?";
	private static final String AMPERSAND = "&";
	private static final String EQUALS = "=";
	
	private static final String PROFILE_URL = "https://api.linkedin.com/v1/people/~";
	private static final String OAUTH_ACCESS_TOKEN_PARAM ="oauth2_access_token";
	private static final String FORMAT = "format=json";
	private static final String AMPERCENT = "&";
	private static final String PROFILE_PARAMETERS = "(id,first-name,last-name,maiden-name,email-address,picture-url)";
	private static final String COLON = ":";
	
	private WebView webView;
	Activity activity;
	public SharedPreferences mPrefs;
	SharedPreferences.Editor editor;
	public static final String MyPREFERENCES = "LinkedIn_Prefs" ;
	String accessToken;
	String firstName,lastName,UserEmail;
	ProgressDialog progressDialog;
	
	
	TextView title, full_name, email;
	Button share, logout;
	ImageView profile_img;
	URL imageURL;
    static Bitmap bitmap = null;
    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.profile_layout);
		activity = this;
		title = (TextView)findViewById(R.id.title);
		full_name = (TextView)findViewById(R.id.full_name);
		email = (TextView)findViewById(R.id.email);
		share = (Button)findViewById(R.id.share);
		logout = (Button)findViewById(R.id.logout);
		profile_img = (ImageView)findViewById(R.id.profile_img);
		
		progressDialog = new ProgressDialog(activity);
		progressDialog.setMessage("Loading...");
		progressDialog.setCancelable(true);
		
		mPrefs = getSharedPreferences(MyPREFERENCES,activity.MODE_PRIVATE);
		  
		String access_token = mPrefs.getString("access_token", null);
		
		webView = (WebView) findViewById(R.id.webView);
		webView.requestFocus(View.FOCUS_DOWN);
		
		hideAllViews();
		
		if (access_token != null) {
			//this.finish();
			hideWebView();
			setupProfileData();
			
		}else
		{
			linkedInLogin();
		}
		
		
		share.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				sharePost();
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
	
    private void hideWebView(){
    	if(webView !=null)
    		webView.setVisibility(View.GONE);
    }
	private void linkedInLogin(){
		webView.setVisibility(View.VISIBLE);
		progressDialog.show();
		
		//Set a custom web view client
		webView.setWebViewClient(new WebViewClient(){
			  @Override
	          public void onPageFinished(WebView view, String url) {
					//This method will be executed each time a page finished loading.
					//The only we do is dismiss the progressDialog, in case we are showing any.
	              if(progressDialog!=null && progressDialog.isShowing()){
	            	  progressDialog.dismiss();
	              }
	          }
			@SuppressLint("NewApi")
			@Override
	        public boolean shouldOverrideUrlLoading(WebView view, String authorizationUrl) {
				//This method will be called when the Auth proccess redirect to our RedirectUri.
				//We will check the url looking for our RedirectUri.
				
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		        imm.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
		        
				if(authorizationUrl.startsWith(REDIRECT_URI)){
					Log.i("Authorize", "");
					Uri uri = Uri.parse(authorizationUrl);
					//We take from the url the authorizationToken and the state token. We have to check that the state token returned by the Service is the same we sent.
					//If not, that means the request may be a result of CSRF and must be rejected.
					String stateToken = uri.getQueryParameter(STATE_PARAM);
					if(stateToken==null || !stateToken.equals(STATE)){
						Log.e("Authorize", "State token doesn't match");
						return true;
					}
					
					//If the user doesn't allow authorization to our application, the authorizationToken Will be null.
					String authorizationToken = uri.getQueryParameter(RESPONSE_TYPE_VALUE);
        			if(authorizationToken==null){
        				Log.i("Authorize", "The user doesn't allow authorization.");
        				LinkedInLogin.this.finish();
        				return true;
        			}
        			Log.i("Authorize", "Auth token received: "+authorizationToken);
        			
        			//Generate URL for requesting Access Token
        			String accessTokenUrl = getAccessTokenUrl(authorizationToken);
        			//We make the request in a AsyncTask
        			//new PostRequestAsyncTask().execute(accessTokenUrl);
        			new PostRequestAsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,accessTokenUrl);
					
				}else{
					//Default behaviour
                	Log.i("Authorize","Redirecting to: "+authorizationUrl);
                    webView.loadUrl(authorizationUrl);
                }
				return true;
			}
		});
		
		//Get the authorization Url
		String authUrl = getAuthorizationUrl();
        Log.i("Authorize","Loading Auth Url: "+authUrl);
        //Load the authorization URL into the webView
        webView.loadUrl(authUrl);
	}
	/**
	 * Method that generates the url for get the access token from the Service
	 * @return Url
	 */
	private static String getAccessTokenUrl(String authorizationToken){
		String URL = ACCESS_TOKEN_URL
				+QUESTION_MARK
				+GRANT_TYPE_PARAM+EQUALS+GRANT_TYPE
				+AMPERSAND
				+RESPONSE_TYPE_VALUE+EQUALS+authorizationToken
				+AMPERSAND
				+CLIENT_ID_PARAM+EQUALS+API_KEY
				+AMPERSAND
				+REDIRECT_URI_PARAM+EQUALS+REDIRECT_URI
				+AMPERSAND
				+SECRET_KEY_PARAM+EQUALS+SECRET_KEY;
		Log.i("accessToken URL",""+URL);
		return URL;
	}
	/**
	 * Method that generates the url for get the authorization token from the Service
	 * @return Url
	 */
	private static String getAuthorizationUrl(){
		String URL = AUTHORIZATION_URL
				+QUESTION_MARK+RESPONSE_TYPE_PARAM+EQUALS+RESPONSE_TYPE_VALUE
				+AMPERSAND  +CLIENT_ID_PARAM    +EQUALS +API_KEY
				+AMPERSAND  +SCOPE_PARAM        +EQUALS +SCOPES
				+AMPERSAND  +STATE_PARAM        +EQUALS +STATE
				+AMPERSAND  +REDIRECT_URI_PARAM +EQUALS +REDIRECT_URI;
		Log.i("authorization URL",""+URL);
		
		return URL;
	}
	

	private class PostRequestAsyncTask extends AsyncTask<String, Void, Boolean>{

		@Override
		protected void onPreExecute(){
			  progressDialog.show();
            
		}
		
		@Override
		protected Boolean doInBackground(String... urls) {
			if(urls.length>0){
				String url = urls[0];
				HttpClient httpClient = new DefaultHttpClient();
				HttpPost httpost = new HttpPost(url);
				try{
					HttpResponse response = httpClient.execute(httpost);
					if(response!=null){
						//If status is OK 200
						if(response.getStatusLine().getStatusCode()==200){
							String result = EntityUtils.toString(response.getEntity());
							Log.i("result="+result.toString(),"786="+result.toString());
							//Convert the string result to a JSON Object
							JSONObject resultJson = new JSONObject(result);
							//Extract data from JSON Response
							int expiresIn = resultJson.has("expires_in") ? resultJson.getInt("expires_in") : 0;
							
							accessToken = resultJson.has("access_token") ? resultJson.getString("access_token") : null;
							Log.e("Tokenm", ""+accessToken);
							if(expiresIn>0 && accessToken!=null){
								Log.i("Authorize", "This is the access Token: "+accessToken+". It will expires in "+expiresIn+" secs");
								
								//Calculate date of expiration
								Calendar calendar = Calendar.getInstance();
								calendar.add(Calendar.SECOND, expiresIn);
								long expireDate = calendar.getTimeInMillis();
								
								////Store both expires in and access token in shared preferences
								editor = mPrefs.edit();
								editor.putString("access_token",accessToken);
								editor.commit();
								return true;
							}
						}
					}
				}catch(IOException e){
					Log.e("Authorize","Error Http response "+e.getLocalizedMessage());	
				}
				catch (ParseException e) {
					Log.e("Authorize","Error Parsing Http response "+e.getLocalizedMessage());
				} catch (JSONException e) {
					Log.e("Authorize","Error Parsing Http response "+e.getLocalizedMessage());
				}
			}
			return false;
		}
		
		@SuppressLint("NewApi")
		@Override
		protected void onPostExecute(Boolean status){
			if(progressDialog!=null && progressDialog.isShowing()){
				progressDialog.dismiss();
            }
			if(status){
				//If everything went Ok, change to another activity.
					if(accessToken!=null){
						String profileUrl = getProfileUrl(accessToken);
						//new GetProfileRequestAsyncTask().execute(profileUrl);
						new GetProfileRequestAsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,profileUrl);
					}
				
			}
		}
		
		
		
	};
	
	private class GetProfileRequestAsyncTask extends AsyncTask<String, Void, JSONObject>{

		@Override
		protected void onPreExecute(){
			  progressDialog.show();
            
		}
		
		@Override
		protected JSONObject doInBackground(String... urls) {
			if(urls.length>0){
				String url = urls[0];
				HttpClient httpClient = new DefaultHttpClient();
				HttpGet httpget = new HttpGet(url);
				httpget.setHeader("x-li-format", "json");
				try{
					HttpResponse response = httpClient.execute(httpget);
					if(response!=null){
						//If status is OK 200
						if(response.getStatusLine().getStatusCode()==200){
							String result = EntityUtils.toString(response.getEntity());
							//Convert the string result to a JSON Object
							return new JSONObject(result);
						}
					}
				}catch(IOException e){
					Log.e("Authorize","Error Http response "+e.getLocalizedMessage());	
				} catch (JSONException e) {
					Log.e("Authorize","Error Http response "+e.getLocalizedMessage());	
				}
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(JSONObject data){
			Log.i("data=","data="+data.toString());
			if(progressDialog!=null && progressDialog.isShowing()){
				progressDialog.dismiss();
            }
			if(data!=null){
				
				try {
					editor = mPrefs.edit();
					editor.putString("first_name",data.getString("firstName"));
					editor.putString("last_name",data.getString("lastName"));
					editor.putString("email",data.getString("emailAddress"));
					editor.putString("imgUrl",data.getString("pictureUrl"));
					editor.commit();
					hideWebView();
					setupProfileData();
					
				} catch (JSONException e) {
					Log.e("Authorize","Error Parsing json "+e.getLocalizedMessage());	
				}
			}
			
			
		}
		
		
	};

	private static final String getProfileUrl(String accessToken){
//		return PROFILE_URL
//				+QUESTION_MARK
//				+OAUTH_ACCESS_TOKEN_PARAM+EQUALS+accessToken;
		//return "https://api.linkedin.com/v1/people/~:(id,first-name,last-name,headline,email-address)?format=json";
//		
//		return "https://api.linkedin.com/v1/people/~:" +
//				"(id,first-name,last-name,maiden-name,email-address)?oauth2_access_token="+accessToken+"&format=json";

		return PROFILE_URL+COLON+PROFILE_PARAMETERS+QUESTION_MARK+OAUTH_ACCESS_TOKEN_PARAM+EQUALS+accessToken+AMPERCENT+FORMAT;
				
	}
	
	public void resetAccessToken() {
		editor = mPrefs.edit();
		editor.putString("access_token", null);
		//editor.putString("access_token_secret", null);
		editor.commit();
	}
	
	private void clearSharedPrefernces(){
		  editor = mPrefs.edit();
		  editor.putString("first_name","");
		  editor.putString("last_name","");
		  editor.putString("email","");
		  editor.putString("imgUrl","");
		  editor.commit();
	}
	private void setupProfileData(){
		showAllViews();
		title.setText("Welcome to Linkedin");
		full_name.setText("Hello,"+mPrefs.getString("first_name", "")+" "+mPrefs.getString("last_name", ""));
		email.setText("("+mPrefs.getString("email", "")+")");
		if(bitmap ==null)
			new GetProfilePicture(activity).execute(mPrefs.getString("imgUrl",""));
		else
			profile_img.setImageBitmap(bitmap);
		
	}
	
	
	private void logout(){
		
		clearSharedPrefernces();
		resetAccessToken();
		if(bitmap !=null)
			bitmap = null;
		new AlertDialog.Builder(this)
	    .setTitle("Logout")
	    .setMessage("You have sucessfully logged out.")
	    .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) { 
	            // continue with delete
	        	LinkedInLogin.this.finish();
	        }
	     })
	    .show();
		
	}
	
	private void sharePost(){
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(LinkedInLogin.this);
		 alertDialog.setTitle("Your message");
		 final EditText input = new EditText(LinkedInLogin.this);
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
		             String message = input.getEditableText().toString();
		             new PostViaLinked(LinkedInLogin.this).execute(message);
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
	      protected void onResume() 
	      { 
	         super.onResume( ) ;
	          
	       }
	      @Override
	      protected void onPause() 
	      { 
	          super.onPause( );    
	       
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
