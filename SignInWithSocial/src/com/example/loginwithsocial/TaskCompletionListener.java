package com.example.loginwithsocial;

import android.graphics.Bitmap;

public interface TaskCompletionListener {
	public void onTwitterLoginComplete();
	public void onTwitterActivityFinish();
	public void onGetProfilePicTaskComplete(Bitmap bm);
}
