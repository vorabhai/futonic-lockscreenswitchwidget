package com.futonredemption.nokeyguard.activities;

import com.futonredemption.nokeyguard.Intents;
import com.futonredemption.nokeyguard.R;
import com.futonredemption.nokeyguard.StrictModeEnabler;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;

public class NoKeyguardPreferenceActivity extends PreferenceActivity {

	PreferenceScreen Screen;
	CheckBoxPreference VirtualPref_ActivateOnCondition;
	Preference PrefCategory_ActivateOnly;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		StrictModeEnabler.setupStrictMode();
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.pref_main);
		getPreferenceViews();
	}
	
	private void getPreferenceViews() {
		Screen = getPreferenceScreen();
		
		VirtualPref_ActivateOnCondition = (CheckBoxPreference)Screen.findPreference("VirtualPref_ActivateOnCondition");
		PrefCategory_ActivateOnly = Screen.findPreference("PrefCategory_ActivateOnly");
		syncState();
	}

	private void syncState() {
		PrefCategory_ActivateOnly.setEnabled(VirtualPref_ActivateOnCondition.isChecked());
	}
	
	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
		syncState();
		return super.onPreferenceTreeClick(preferenceScreen, preference);
	}

	@Override
	public void onPause() {
		super.onPause();

		// If coming from the first place configuration then return that the preferences were saved.
		Intent originalIntent = getIntent();
		if(originalIntent != null) {
			if(originalIntent.hasExtra(AppWidgetManager.EXTRA_APPWIDGET_ID)) {
				Intent resultValue = new Intent();
				resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, originalIntent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 0));
				setResult(RESULT_OK, resultValue);
			}
		}
		
		this.startService(Intents.refreshWidgets(this));
		finish();
	}
}
