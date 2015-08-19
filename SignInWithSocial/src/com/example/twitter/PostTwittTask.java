package com.example.twitter;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.widget.Toast;

public class PostTwittTask extends AsyncTask<String, Void, String> {
	ProgressDialog pDialog;
	Activity activity;
	
	public PostTwittTask(Activity act){
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
	protected String doInBackground(String... twitt) {
		try {
			if(TwitterLogin.mTwitter !=null)
				TwitterLogin.mTwitter.updateStatus(twitt[0]);
			return "success";

		} catch (Exception e) {
			if (e.getMessage().toString().contains("duplicate")) {
				return "Posting Failed because of Duplicate message...";
			}
			e.printStackTrace();
			return "Posting Failed!!!";
		}

	}

	@Override
	protected void onPostExecute(String result) {
		pDialog.dismiss();
		if (null != result && result.equals("success")) {
			showToast("Posted Successfully");

		} else {
			showToast(result);
		}

		super.onPostExecute(result);
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