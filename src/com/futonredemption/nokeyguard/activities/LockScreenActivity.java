package com.futonredemption.nokeyguard.activities;

import com.futonredemption.nokeyguard.Constants;
import com.futonredemption.nokeyguard.Intents;
import com.futonredemption.nokeyguard.R;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class LockScreenActivity extends Activity {

	public ImageView BackgroundGlowImageView;
	public ImageButton ToggleLockButton;
	public TextView StatusTextView;
	public Button PreferencesButton;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		setWindowMode();
		getViews();
	}

	private void setWindowMode() {
		Window window = getWindow();
		window.setFormat(PixelFormat.RGBA_8888);
	}

	private void getViews() {
		BackgroundGlowImageView = (ImageView)findViewById(R.id.BackgroundGlowImageView);
		ToggleLockButton = (ImageButton)findViewById(R.id.ToggleLockButton);
		StatusTextView = (TextView)findViewById(R.id.StatusTextView);
		ToggleLockButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if(LockState.Mode == Constants.MODE_Enabled) {
					startService(Intents.disableKeyguard(LockScreenActivity.this));
				} else {
					startService(Intents.enableKeyguard(LockScreenActivity.this));
				}
			}
		}
		);
		
		PreferencesButton = (Button)findViewById(R.id.PreferencesButton);
		PreferencesButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				startActivity(Intents.showPreferencesActivity(LockScreenActivity.this));
			}
		}
		);
	}

	public final LockStatusReceiver LockState = new LockStatusReceiver();
	
	public class LockStatusReceiver extends BroadcastReceiver {

		public int Mode = Constants.MODE_Enabled;
		
		@Override
		public void onReceive(Context context, Intent intent) {
			Mode = intent.getIntExtra("mode", Constants.MODE_Enabled);
			final boolean isLockActive = intent.getBooleanExtra("isActive", true);
			
			StringBuilder sb = new StringBuilder();
			
			if(isLockActive) {
				BackgroundGlowImageView.setImageResource(R.drawable.bg_glow_white);
				ToggleLockButton.setImageResource(R.drawable.active_lock);
				sb.append("Lock Screen is Active");
			} else {
				BackgroundGlowImageView.setImageResource(R.drawable.bg_glow_blue);
				ToggleLockButton.setImageResource(R.drawable.inactive_lock);
				sb.append("Lock Screen is Not Active");
			}
			if(Mode == Constants.MODE_ConditionalToggle) {
				sb.append(", conditionally toggled.");
			}
			
			StatusTextView.setText(sb.toString());
		}
		
	}

	@Override
	public void onResume() {
		super.onResume();
		registerReceiver(LockState, Intents.broadcastLockStateIntentFilter());
		startService(Intents.refreshWidgets(this));
	}
	
	@Override
	public void onPause() {
		super.onPause();
		unregisterReceiver(LockState);
	}
}
