package com.example.twitter;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;

import com.example.loginwithsocial.TaskCompletionListener;


import oauth.signpost.OAuthProvider;
import oauth.signpost.basic.DefaultOAuthProvider;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
//import android.util.Log;
import android.view.Window;
import android.widget.Toast;

public class TwitterApp {
	private Twitter mTwitter;
	private TwitterSession mSession;
	private AccessToken mAccessToken;
	private CommonsHttpOAuthConsumer mHttpOauthConsumer;
	private OAuthProvider mHttpOauthprovider;
	private String mConsumerKey;
	private String mSecretKey;
	public static ProgressDialog mProgressDlg;
	private TwDialogListener mListener;
	private Activity context;

	public static final String CALLBACK_URL = "twitterapp://connect";
	private static final String TWITTER_ACCESS_TOKEN_URL = "https://api.twitter.com/oauth/access_token";
	private static final String TWITTER_AUTHORZE_URL = "https://api.twitter.com/oauth/authorize?force_login=true";
	//private static final String TWITTER_AUTHORZE_URL = "https://api.twitter.com/oauth/authenticate?force_login=true";
	private static final String TWITTER_REQUEST_URL = "https://api.twitter.com/oauth/request_token";

	public static String  firstName="",lastName="";
	public static long UserId;
	
	private SharedPreferences mPrefs;
	public static final String MyPREFERENCES = "twitter_pref" ;
	SharedPreferences.Editor editor;
	
	TaskCompletionListener _mCompleteListener;
	
	 
	public TwitterApp(Activity context, String consumerKey, String secretKey) {
		this.context = context;
		mTwitter = new TwitterFactory().getInstance();
		mSession = new TwitterSession(context);
		mProgressDlg = new ProgressDialog(context);
		mProgressDlg.requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		mConsumerKey = consumerKey;
		mSecretKey = secretKey;
		
		setListener(mListener);
		mPrefs = this.context.getSharedPreferences(MyPREFERENCES,this.context.MODE_PRIVATE);
		
		_mCompleteListener =  (TaskCompletionListener)context;
				
		mHttpOauthConsumer = new CommonsHttpOAuthConsumer(mConsumerKey,
				mSecretKey);

		String request_url = TWITTER_REQUEST_URL;
		String access_token_url = TWITTER_ACCESS_TOKEN_URL;
		String authorize_url = TWITTER_AUTHORZE_URL;

		mHttpOauthprovider = new DefaultOAuthProvider(request_url,
				access_token_url, authorize_url);
		mAccessToken = mSession.getAccessToken();
		Log.i("mAccessToken="+mAccessToken, "786");
		
		configureToken();
		if(mAccessToken !=null)
			_mCompleteListener.onTwitterLoginComplete();
		
		mListener = new TwDialogListener() {

			public void onError(String value) {
				showToast("Login Failed");
				if(mProgressDlg!=null && mProgressDlg.isShowing())
					mProgressDlg.dismiss();
				resetAccessToken();
				_mCompleteListener.onTwitterActivityFinish();
				
			}

			public void onComplete(String value) {
				//showTwittDialog();
				if(mProgressDlg!=null && mProgressDlg.isShowing())
					mProgressDlg.dismiss();
				_mCompleteListener.onTwitterLoginComplete();
			}
		};

	        
	}
	void showToast(final String msg) {
		this.context.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();

			}
		});

	}
	public void setListener(TwDialogListener listener) {
		mListener = listener;
	}

	private void configureToken() {
		if (mAccessToken != null) {
			mTwitter.setOAuthConsumer(mConsumerKey, mSecretKey);
			mTwitter.setOAuthAccessToken(mAccessToken);
		}else{
			authorize();//show login dialog
		}
	}

	public boolean hasAccessToken() {
		return (mAccessToken == null) ? false : true;
	}

	public void resetAccessToken() {
		if (mAccessToken != null) {
			mSession.resetAccessToken();

			mAccessToken = null;
		}
	}

	public String getUsername() {
		return mSession.getUsername();
	}

	public void updateStatus(String status) throws Exception {
		try {
			mTwitter.updateStatus(status);
		} catch (TwitterException e) {
			throw e;
		}
	}

	public void authorize() {
		mProgressDlg.setMessage("Loading ...");
		mProgressDlg.show();

		new Thread() {
			@Override
			public void run() {
				String authUrl = "";
				int what = 1;

				try {
					authUrl = mHttpOauthprovider.retrieveRequestToken(
							mHttpOauthConsumer, CALLBACK_URL);
					what = 0;
				} catch (Exception e) {
					e.printStackTrace();
				}
				mHandler.sendMessage(mHandler
						.obtainMessage(what, 1, 0, authUrl));
			}
		}.start();
	}

	public void processToken(String callbackUrl) {
		mProgressDlg.setMessage("Finalizing ...");
		mProgressDlg.show();
		final String verifier = getVerifier(callbackUrl);

		new Thread() {
			@Override
			public void run() {
				int what = 1;

				try {
					mHttpOauthprovider.retrieveAccessToken(mHttpOauthConsumer,
							verifier);

					mAccessToken = new AccessToken(
							mHttpOauthConsumer.getToken(),
							mHttpOauthConsumer.getTokenSecret());

					configureToken();

					User user = mTwitter.verifyCredentials();
					
					Log.i("user.getName()="+user.getBiggerProfileImageURL(), "786");
					try{
						firstName=user.getName().split(" ")[0];
						lastName=user.getName().split(" ")[1];
					}catch(Exception e){
						e.printStackTrace();
						firstName = user.getName();
						lastName = "";
					}
					Log.i("firstName="+firstName+" lastName="+lastName, "786");
					UserId=user.getId();
					
					/*add profile details to shared preferences. Use can use sqlite also*/
					editor = mPrefs.edit();
					editor.putString("first_name",firstName);
					editor.putString("last_name",lastName);
					editor.putString("UserId",""+UserId);
					editor.putString("imgUrl",""+user.getBiggerProfileImageURL());
					
					editor.commit();
					mSession.storeAccessToken(mAccessToken, user.getName());
					Log.i("sucessful login twitter","786");
					what = 0;
				} catch (Exception e) {
					e.printStackTrace();
				}

				mHandler.sendMessage(mHandler.obtainMessage(what, 2, 0));
			}
		}.start();
	}
	
	   
	private String getVerifier(String callbackUrl) {
		String verifier = "";

		try {
			callbackUrl = callbackUrl.replace("twitterapp", "http");

			URL url = new URL(callbackUrl);
			String query = url.getQuery();

			String array[] = query.split("&");

			for (String parameter : array) {
				String v[] = parameter.split("=");

				if (URLDecoder.decode(v[0]).equals(
						oauth.signpost.OAuth.OAUTH_VERIFIER)) {
					verifier = URLDecoder.decode(v[1]);
					break;
				}
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

		return verifier;
	}

	private void showLoginDialog(String url) {
		final TwDialogListener listener = new TwDialogListener() {

			public void onComplete(String value) {

				processToken(value);

			}

			public void onError(String value) {
				mListener.onError("Failed opening authorization page");
			}
		};

		new TwitterDialog(context, url, listener).show();
	}

	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			Log.i("msg="+msg.what, "222");
			Log.i("msg="+msg.arg1, "222");
			if (msg.what == 1) {
				if (msg.arg1 == 1)
					mListener.onError("Error getting request token");
				else
					mListener.onError("Error getting access token");
			} else {
				if (msg.arg1 == 1){
					showLoginDialog((String) msg.obj);
		}
				else
					{
						mListener.onComplete("");
						//
					}
			}
		}
	};
	
	
	public interface TwDialogListener {
		public void onComplete(String value);

		public void onError(String value);
	}
}