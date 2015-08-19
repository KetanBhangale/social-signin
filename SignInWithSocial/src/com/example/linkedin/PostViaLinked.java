package com.example.linkedin;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

@SuppressWarnings("deprecation")
public class PostViaLinked extends AsyncTask<String, Void, String> {
	  		ProgressDialog pDialog;
	  		Activity activity;
	  		public SharedPreferences mPrefs;
	  		public static final String MyPREFERENCES = "LinkedIn_Prefs" ;
	  		public PostViaLinked(Activity act){
	  			activity = act;
	  		}

	  		@Override
	  		protected void onPreExecute() {
	  			pDialog = new ProgressDialog(activity);
	  			pDialog.setMessage("Posting...");
	  			pDialog.setCancelable(false);
	  			pDialog.show();
	  			super.onPreExecute();
	  		}

	  		@Override
	  		protected String doInBackground(String... msg) {
	  			try {
	  				
	  				return linkedInPost(msg[0]);

	  			} catch (Exception e) {
	  				if (e.getMessage().toString().contains("duplicate")) {
	  					return "Posting Failed because of Duplicate message...";
	  				}
	  				e.printStackTrace();
	  				return "Posting Failed!!!";
	  			}

	  		}

	  		
	  
	     
	   private String linkedInPost(String msg){
		   mPrefs = activity.getSharedPreferences(MyPREFERENCES,activity.MODE_PRIVATE);
		   
		   String access_token = mPrefs.getString("access_token", null);
		   if(access_token!=null)
		   {
			   String message = messageBuilder(msg);
			   String url = "https://api.linkedin.com/v1/people/~/shares?" +
						"oauth2_access_token="+access_token+"&format=json";
			   
			   HttpClient httpClient = new DefaultHttpClient();
				HttpPost httpget = new HttpPost(url);
				httpget.setHeader("Content-Type", "application/json");
				httpget.setHeader("x-li-format", "json");
				StringEntity entity=null;
				try {
					entity = new StringEntity(message);
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				httpget.setEntity(entity);
				try{
					HttpResponse response = httpClient.execute(httpget);
					if(response!=null){
						//If status is OK 200
						Log.i("response.getStatusLine().getStatusCode()="+response.getStatusLine().getStatusCode(),"786");
						if(response.getStatusLine().getStatusCode()==201){
							//String result = EntityUtils.toString(response.getEntity());
							//Convert the string result to a JSON Object
							return "success";
						}else if(response.getStatusLine().getStatusCode()==400){
							//String result = EntityUtils.toString(response.getEntity());
							//Convert the string result to a JSON Object
							return "duplicate";
						}else{
							return "failed";
						}
					}
				}catch(Exception e){
					Log.e("Authorize","Error Http response "+e.getLocalizedMessage());	
				} 
		   }
			return "failed";
		   
			
	   }
	   
	   @Override
 		protected void onPostExecute(String result) {
 			pDialog.dismiss();

 			if (null != result && result.equals("success")) {
 				showToast("Posted Successfully");
 				//ShareAppActivity.sendDataToAnalytics("linkedin");

 			}else if (null != result && result.equals("duplicate")) {
 				showToast("Posting Failed because of Duplicate message...");
 			}else {
 				showToast("Posting Failed!!!");
 			}

 			super.onPostExecute(result);
 		}
	   
	   private String messageBuilder(String msg){
		   String url ="https://ketub4.wordpress.com/";
		   String temp = "{"+
					  "\"comment\": \""+msg+"\","+
					  "\"content\": {"+
					    "\"submitted-url\": \""+url+"\""+  
					    "},"+
					    "\"visibility\":{" +
					    "    \"code\":\"anyone\"}" +
					"}";
		   return temp.replaceAll("\n", "\\\\n");// for any line breaks in the message
		   
	   }
	   void showToast(final String msg) {
			 activity.runOnUiThread(new Runnable() {

	 			@Override
	 			public void run() {
	 				Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();

	 			}
	 		});

	 	}
	     
}
