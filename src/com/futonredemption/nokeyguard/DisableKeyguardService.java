package com.futonredemption.nokeyguard;

import org.beryl.app.ServiceForegrounder;

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
	private ServiceForegrounder _foregrounder = new ServiceForegrounder(this);
	
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

			if (remote_action.equals(RemoteAction_EnableKeyguard)) {
				onEnableKeyguard();
			} else if (remote_action.equals(RemoteAction_DisableKeyguard)) {
				onDisableKeyguard();
			} else if (remote_action.equals(RemoteAction_DisableKeyguardOnCharging)) {
				onDisableKeyguardOnCharging();
			} else if (remote_action.equals(RemoteAction_RefreshWidgets)) {
				onRefreshWidgets();
			}
		}
	}

	private void updateAllWidgets() {
		boolean isLockscreenEnabled = true;

		int lockscreenPreference = getKeyguardEnabledPreference();

		if (lockscreenPreference == Constants.KEYGUARD_Disabled) {
			isLockscreenEnabled = false;
			disableLockscreen();
		} else if (lockscreenPreference == Constants.KEYGUARD_DisableOnCharging) {
			if (isSystemCharging()) {
				isLockscreenEnabled = false;
				disableLockscreen();
			} else {
				enableLockscreen();
			}
		} else if (lockscreenPreference == Constants.KEYGUARD_Enabled) {
			isLockscreenEnabled = true;
			enableLockscreen();
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
				
				_foregrounder.startForeground(Constants.NOTIFICATION_ForegroundService,
						R.drawable.stat_icon,
						R.string.app_name,
						R.string.lockscreen_is_off_tap_to_turn_on,
						R.string.lockscreen_is_off,
						reenableLockScreenIntent);
			}
		}
	}

	private PendingIntent getReenableLockScreenIntent() {
		return PendingIntent.getService(this, 0, Intents.enableKeyguard(this), PendingIntent.FLAG_UPDATE_CURRENT);
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

	private void onRefreshWidgets() {
		updateAllWidgets();
	}

	private void onDisableKeyguard() {
		setKeyguardTogglePreference(Constants.KEYGUARD_Disabled);
		updateAllWidgets();
	}

	private void onEnableKeyguard() {
		setKeyguardTogglePreference(Constants.KEYGUARD_Enabled);
		updateAllWidgets();
		destroyKeyguard();
	}

	private void destroyKeyguard() {
		_wrapper.dispose();
		this.stopSelf();
	}
	
	private void onDisableKeyguardOnCharging() {
		setKeyguardTogglePreference(Constants.KEYGUARD_DisableOnCharging);
		updateAllWidgets();
	}

	private int getKeyguardEnabledPreference() {
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		return prefs.getInt(Constants.Preference_KeyguardToggle, Constants.KEYGUARD_Enabled);
	}

	private void setKeyguardTogglePreference(final int param) {
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		prefs.edit().putInt(Constants.Preference_KeyguardToggle, param).commit();
	}
}
