package com.example.loginwithsocial;

import java.net.URL;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

public class GetProfilePicture extends AsyncTask<String, Void, Bitmap> {
	Activity activity;
	URL imageURL;
    Bitmap bitmap = null;
    TaskCompletionListener _mCallback;
    
    public GetProfilePicture(Activity act){
		activity =  act;
		_mCallback = (TaskCompletionListener)act;
	}
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected Bitmap doInBackground(String... msg) {
				try {
				imageURL = new URL(msg[0]);
				bitmap = BitmapFactory.decodeStream(imageURL.openConnection().getInputStream());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return bitmap;
		}

	@Override
	protected void onPostExecute(Bitmap result) {
		super.onPostExecute(result);
		_mCallback.onGetProfilePicTaskComplete(result);
	}
}