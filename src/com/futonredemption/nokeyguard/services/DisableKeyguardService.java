package com.futonredemption.nokeyguard.services;

import org.beryl.app.ServiceForegrounder;

import com.futonredemption.nokeyguard.Constants;
import com.futonredemption.nokeyguard.HeadsetStateGetter;
import com.futonredemption.nokeyguard.Intents;
import com.futonredemption.nokeyguard.KeyguardLockWrapper;
import com.futonredemption.nokeyguard.LockScreenState;
import com.futonredemption.nokeyguard.R;
import com.futonredemption.nokeyguard.StrictModeEnabler;
import com.futonredemption.nokeyguard.appwidgets.AppWidgetProvider1x1;
import com.futonredemption.nokeyguard.receivers.RelayRefreshWidgetReceiver;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.BatteryManager;
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

	final RelayRefreshWidgetReceiver receiver = new RelayRefreshWidgetReceiver();

	@Override
	public void onCreate() {
		StrictModeEnabler.setupStrictMode();
		super.onCreate();
		
		RelayRefreshWidgetReceiver.startReceiver(this, receiver);
		_wrapper = new KeyguardLockWrapper(this, KeyGuardTag);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		RelayRefreshWidgetReceiver.stopReceiver(this, receiver);
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
		final LockScreenState state = new LockScreenState();
		
		state.Mode = getKeyguardEnabledPreference();

		if (state.Mode == Constants.MODE_Enabled) {
			state.IsLockActive = true;
		} else {
			determineIfLockShouldBeDeactivated(state);
		}
		
		if(state.IsLockActive) {
			enableLockscreen();
		}
		else {
			disableLockscreen();
		}

		AppWidgetProvider1x1.UpdateAllWidgets(this, state);
	}

	private void determineIfLockShouldBeDeactivated(final LockScreenState state) {
		final SharedPreferences prefs = getPreferences();
		
		boolean prefActivateOnlyWhenCharging = prefs.getBoolean("PrefActivateOnlyWhenCharging", false);
		boolean prefActivateOnlyWhenHeadphonesPluggedIn = prefs.getBoolean("PrefActivateOnlyWhenHeadphonesPluggedIn", false);
		boolean prefActivateOnlyWhenDocked = prefs.getBoolean("PrefActivateOnlyWhenDocked", false);
		
		// Assume that the lock will be disabled.
		state.Mode = Constants.MODE_Disabled;
		state.IsLockActive = false;
		
		if(prefActivateOnlyWhenCharging || prefActivateOnlyWhenHeadphonesPluggedIn || prefActivateOnlyWhenDocked) {
			
			// Since we have a restriction we need to build an or case against it to see if the lock really should be disabled.
			state.Mode = Constants.MODE_ConditionalToggle;
			state.IsLockActive = true;

			if(prefActivateOnlyWhenCharging) {
				if(isSystemCharging()) {
					state.IsLockActive = false;
				}
			}
			
			if(prefActivateOnlyWhenHeadphonesPluggedIn) {
				if(isHeadsetPluggedIn()) {
					state.IsLockActive = false;
				}
			}
		}
	}
	
	private boolean isSystemCharging() {
		boolean isCharging = false;
		final Intent powerstate = Intents.getBatteryState(this);
		if (powerstate != null) {
			final int battstate = powerstate.getIntExtra("status", BatteryManager.BATTERY_STATUS_FULL);
			if (battstate == BatteryManager.BATTERY_STATUS_CHARGING || battstate == BatteryManager.BATTERY_STATUS_FULL) {
				isCharging = true;
			}
		}

		return isCharging;
	}
	
	private boolean isHeadsetPluggedIn() {
		final HeadsetStateGetter getter = new HeadsetStateGetter(this);
		return getter.isHeadsetPluggedIn();
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
				final PendingIntent showPreferencesIntent = getShowPreferencesActivity();
				
				_foregrounder.startForeground(
						R.drawable.stat_icon,
						R.string.lockscreen_is_off,
						R.string.select_to_configure,
						R.string.lockscreen_is_off,
						showPreferencesIntent);
			}
		}
	}

	private PendingIntent getShowPreferencesActivity() {
		return PendingIntent.getActivity(this, 0, Intents.showPreferencesActivity(this), PendingIntent.FLAG_UPDATE_CURRENT);
	}

	private void onRefreshWidgets() {
		updateAllWidgets();
	}

	private void onDisableKeyguard() {
		setKeyguardTogglePreference(Constants.MODE_Disabled);
		updateAllWidgets();
	}

	private void onEnableKeyguard() {
		setKeyguardTogglePreference(Constants.MODE_Enabled);
		updateAllWidgets();
		destroyKeyguard();
	}

	private void destroyKeyguard() {
		_wrapper.dispose();
		this.stopSelf();
	}

	private int getKeyguardEnabledPreference() {
		final SharedPreferences prefs = getPreferences();
		return prefs.getInt(Constants.Preference_KeyguardToggle, Constants.MODE_Enabled);
	}

	private void setKeyguardTogglePreference(final int param) {
		final SharedPreferences prefs = getPreferences();
		prefs.edit().putInt(Constants.Preference_KeyguardToggle, param).commit();
	}
	
	private SharedPreferences getPreferences() {
		return PreferenceManager.getDefaultSharedPreferences(this);
	}
}
