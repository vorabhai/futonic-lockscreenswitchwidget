package com.futonredemption.nokeyguard.services;

import org.beryl.app.ServiceForegrounder;

import com.futonredemption.nokeyguard.Constants;
import com.futonredemption.nokeyguard.Intents;
import com.futonredemption.nokeyguard.KeyguardLockWrapper;
import com.futonredemption.nokeyguard.R;
import com.futonredemption.nokeyguard.appwidgets.AppWidgetProvider1x1;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;

public class DisableKeyguardService extends Service {
	private Object _commandLock = new Object();

	private KeyguardLockWrapper _wrapper;
	private ServiceForegrounder _foregrounder = new ServiceForegrounder(this, Constants.NOTIFICATION_ForegroundService);
	
	private static final String KeyGuardTag = "KeyguardLockWrapper";
	
	public static final String RemoteAction_EnableKeyguard = "RemoteAction_EnableKeyguard";
	public static final String RemoteAction_DisableKeyguard = "RemoteAction_DisableKeyguard";
	public static final String RemoteAction_DisableKeyguardOnCharging = "RemoteAction_DisableKeyguardOnCharging";
	public static final String RemoteAction_RefreshWidgets = "RemoteAction_RefreshWidgets";
	public static final String EXTRA_RemoteAction = "EXTRA_RemoteAction";
	public static final String EXTRA_ForceNotify = "EXTRA_ForceNotify";

	@Override
	public void onCreate() {
		super.onCreate();

		_wrapper = new KeyguardLockWrapper(this, KeyGuardTag);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		
		_wrapper.dispose();
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
		// Really, really hope that nothing bad happens.
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onStart(final Intent intent, final int startId) {
		super.onStart(intent, startId);

		handleCommand(intent);
	}

	public int onStartCommand(Intent intent, int flags, int startId) {
		handleCommand(intent);
		return START_REDELIVER_INTENT;
	}

	private void handleCommand(final Intent intent) {
		synchronized (_commandLock) {
			final String remote_action = intent.getStringExtra(EXTRA_RemoteAction);

			// Backwards compatability. If the old "disable on charging" preference is set then put it to enable keyguard.
			if (remote_action.equals(RemoteAction_EnableKeyguard) || remote_action.equals(RemoteAction_DisableKeyguardOnCharging)) {
				onEnableKeyguard();
			} else if (remote_action.equals(RemoteAction_DisableKeyguard)) {
				onDisableKeyguard();
			} else if (remote_action.equals(RemoteAction_RefreshWidgets)) {
				onRefreshWidgets();
			}
		}
	}

	private void updateAllWidgets() {
		boolean isLockscreenEnabled = true;

		int lockscreenPreference = getKeyguardEnabledPreference();

		if (lockscreenPreference == Constants.STATE_Enabled) {
			isLockscreenEnabled = true;
			enableLockscreen();
		} else {
			isLockscreenEnabled = false;
			disableLockscreen();
		}

		AppWidgetProvider1x1.UpdateAllWidgets(this, lockscreenPreference, isLockscreenEnabled);
	}

	private void disableLockscreen() {
		setLockscreenMode(false);
	}

	private void enableLockscreen() {
		setLockscreenMode(true);
	}

	private void setLockscreenMode(boolean enableLockscreen) {

		if (enableLockscreen) {
			_wrapper.enableKeyguard();
		} else {
			_wrapper.disableKeyguard();
		}

		if(enableLockscreen) {
			_foregrounder.stopForeground();
		}
		else {
			
			if(! _foregrounder.isForegrounded()) {
				final PendingIntent reenableLockScreenIntent = getReenableLockScreenIntent();
				
				_foregrounder.startForeground(
						R.drawable.stat_icon,
						R.string.lockscreen_is_off,
						R.string.tap_to_turn_on,
						R.string.lockscreen_is_off,
						reenableLockScreenIntent);
			}
		}
	}

	private PendingIntent getReenableLockScreenIntent() {
		return PendingIntent.getService(this, 0, Intents.enableKeyguard(this), PendingIntent.FLAG_UPDATE_CURRENT);
	}

	private void onRefreshWidgets() {
		updateAllWidgets();
	}

	private void onDisableKeyguard() {
		setKeyguardTogglePreference(Constants.STATE_Disabled);
		updateAllWidgets();
	}

	private void onEnableKeyguard() {
		setKeyguardTogglePreference(Constants.STATE_Enabled);
		updateAllWidgets();
		destroyKeyguard();
	}

	private void destroyKeyguard() {
		_wrapper.dispose();
		this.stopSelf();
	}

	private int getKeyguardEnabledPreference() {
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		return prefs.getInt(Constants.Preference_KeyguardToggle, Constants.STATE_Enabled);
	}

	private void setKeyguardTogglePreference(final int param) {
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		prefs.edit().putInt(Constants.Preference_KeyguardToggle, param).commit();
	}
}
