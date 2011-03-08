package com.futonredemption.nokeyguard.fragments;

import com.futonredemption.nokeyguard.Constants;
import com.futonredemption.nokeyguard.Intents;
import com.futonredemption.nokeyguard.R;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

public class LockStatusFragment extends Fragment {

	public final LockStatusReceiver LockState = new LockStatusReceiver();
	
	public class LockStatusReceiver extends BroadcastReceiver {

		public int Mode = Constants.MODE_Enabled;
		
		@Override
		public void onReceive(Context context, Intent intent) {
			Mode = intent.getIntExtra("mode", Constants.MODE_Enabled);
			final boolean isLockActive = intent.getBooleanExtra("isActive", true);
			
			StringBuilder sb = new StringBuilder();
			
			if(isLockActive) {
				sb.append("Lock Screen is Active");
			} else {
				sb.append("Lock Screen is Not Active");
			}
			if(Mode == Constants.MODE_ConditionalToggle) {
				sb.append(", conditionally toggled.");
			}
			
			StatusTextView.setText(sb.toString());
		}
		
	}
	public LockStatusFragment() {
		super();
	}
	
	public ImageButton ToggleLockButton;
	public TextView StatusTextView;
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View result = inflater.inflate(R.layout.fragment_lockstatus, null);
		
		ToggleLockButton = (ImageButton)result.findViewById(R.id.ToggleLockButton);
		StatusTextView = (TextView)result.findViewById(R.id.StatusTextView);
		ToggleLockButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				final Activity activity = getActivity();
				
				if(LockState.Mode == Constants.MODE_Enabled) {
					activity.startService(Intents.disableKeyguard(activity));
				} else {
					activity.startService(Intents.enableKeyguard(activity));
				}
			}
		}
		);
		return result;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		final Activity activity = getActivity();
		activity.registerReceiver(LockState, Intents.broadcastLockStateIntentFilter());
		activity.startService(Intents.refreshWidgets(activity));
	}
	
	@Override
	public void onPause() {
		super.onPause();
		final Activity activity = getActivity();
		activity.unregisterReceiver(LockState);
	}
	
	
}
