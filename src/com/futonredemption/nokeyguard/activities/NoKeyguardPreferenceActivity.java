package com.futonredemption.nokeyguard.activities;

import com.futonredemption.nokeyguard.Intents;
import com.futonredemption.nokeyguard.R;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class NoKeyguardPreferenceActivity extends PreferenceActivity {

	private boolean isChangingConfiguration = false;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.pref_main);
	}
	
	@Override
	public Object onRetainNonConfigurationInstance() {
		isChangingConfiguration = true;
		return super.onRetainNonConfigurationInstance();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		
		if(! isChangingConfiguration) {
			this.startService(Intents.refreshWidgets(this));
			finish();
		}
	}
}
