package com.futonredemption.nokeyguard.activities;

import com.futonredemption.nokeyguard.Intents;
import com.futonredemption.nokeyguard.R;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class NoKeyguardPreferenceActivity extends PreferenceActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.pref_main);
	}

	@Override
	public void onPause() {
		super.onPause();
		if(! isChangingConfigurations()) {
			this.startService(Intents.refreshWidgets(this));
			finish();
		}
	}
}
