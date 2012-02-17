package com.futonredemption.nokeyguard.activities;

import com.futonredemption.nokeyguard.R;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class HiddenActivity extends Activity {

	public static void launch(final Context context) {
		final Intent intent = new Intent(context, HiddenActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
		context.startActivity(intent);
	}
	
	public class ScreenOnReceiver extends BroadcastReceiver {
		
		public void register() {
			IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
			HiddenActivity.this.registerReceiver(this, filter);
		}
		
		public void unregister() {
			HiddenActivity.this.unregisterReceiver(this);
		}
		
		@Override
		public void onReceive(Context context, Intent intent) {
			finish();
		}
	}
	
	private final ScreenOnReceiver screenOnListener = new ScreenOnReceiver();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_hidden);
		
		final View view = findViewById(R.id.HiddenImageView);
		view.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				finish();
				return true;
			}
		});
		
		screenOnListener.register();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		screenOnListener.unregister();
	}
}
