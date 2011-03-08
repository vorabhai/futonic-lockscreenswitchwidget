package com.futonredemption.nokeyguard.fragments;

import com.futonredemption.nokeyguard.Intents;
import com.futonredemption.nokeyguard.R;

import android.app.Activity;
import android.os.Bundle;
import android.preference.PreferenceFragment;

public class AppPreferenceFragment extends PreferenceFragment {

	public AppPreferenceFragment() {
		super();
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.pref_main);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		
		final Activity activity = this.getActivity();
		activity.startService(Intents.refreshWidgets(activity));
		
		if(! activity.isChangingConfigurations()) {
			activity.finish();
		}
	}
}
