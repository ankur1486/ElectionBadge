package com.abkibarmodisarkar.photobadge;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;

public class SplashActivity extends FragmentActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// remove title
//		requestWindowFeature(Window.FEATURE_NO_TITLE);
//		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_splash);

		moveToNextScreen();
	}

	public void moveToNextScreen() {
		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				Intent intent = new Intent(SplashActivity.this,
						ImageCropActivity.class);
				startActivity(intent);
				 overridePendingTransition( R.anim.slide_in_up, R.anim.slide_out_up );
				finish();
			}
		}, 1000);
	}

}
