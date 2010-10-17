package com.futonredemption.nokeyguard;

import android.app.KeyguardManager;
import android.app.Service;
import android.app.KeyguardManager.KeyguardLock;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.BatteryManager;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.text.Html;
import android.widget.Toast;
public class DisableKeyguardService extends Service
{
	private static final String KeyGuardTag = "DisableKeyguardService";
	private KeyguardManager _guard = null;
	private KeyguardLock _keyguardLock = null;
	private Object _synclock = new Object();
	private boolean _isDisposed = false;
	private boolean _allowingKeyguard = true;

	public static final String RemoteAction_EnableKeyguard = "RemoteAction_EnableKeyguard";
	public static final String RemoteAction_DisableKeyguard = "RemoteAction_DisableKeyguard";
	public static final String RemoteAction_DisableKeyguardOnCharging = "RemoteAction_DisableKeyguardOnCharging";
	public static final String RemoteAction_RefreshWidgets = "RemoteAction_RefreshWidgets";
	public static final String EXTRA_RemoteAction = "EXTRA_RemoteAction";
	public static final String EXTRA_ForceNotify = "EXTRA_ForceNotify";
	
	
	@Override
	public void onCreate()
	{
		super.onCreate();
		
		// Force acquire the keyguard manager.
		getKeyguardManager();
	}
	
	private KeyguardManager getKeyguardManager()
	{
		synchronized(_synclock)
		{
			if(_guard == null && ! _isDisposed)
				_guard = (KeyguardManager)this.getSystemService(Context.KEYGUARD_SERVICE);
		}
		
		return _guard;
	}
	
	private KeyguardLock getKeyguardLock()
	{
		synchronized(_synclock)
		{
			if(_keyguardLock == null)
			{
				final KeyguardManager guard = getKeyguardManager();
				if(guard != null)
					_keyguardLock = guard.newKeyguardLock(KeyGuardTag);
			}
		}
		
		return _keyguardLock;
	}
	
	private void destroyKeyguardLock()
	{
		synchronized(_synclock)
		{
			_isDisposed = true;
			if(_keyguardLock != null)
			{
				_keyguardLock.reenableKeyguard();
				_keyguardLock = null;
			}
			if(_guard != null)
			{
				_guard = null;
			}
		}
	}
	
	private boolean disableKeyguard()
	{
		boolean performedUnlock = false;
		
		synchronized(_synclock)
		{
			final KeyguardLock lock = getKeyguardLock();
			if(lock != null)
			{
				lock.disableKeyguard();
				
				if(_allowingKeyguard)
				{
					_allowingKeyguard = false;
					performedUnlock = true;
				}
			}
		}
		return performedUnlock;
	}
	
	private boolean enableKeyguard()
	{
		boolean performedLock = false;
		synchronized(_synclock)
		{
			if(_keyguardLock != null)
			{
				_keyguardLock.reenableKeyguard();
				
				if(! _allowingKeyguard)
				{
					_allowingKeyguard = true;
					performedLock = true;
				}
			}
		}
		
		return performedLock;
	}


	private void Log(String message)
	{
		android.util.Log.w("KGS", message);
	}
	@Override
	public void onDestroy()
	{
		super.onDestroy();
		Log("!!!OnDestroy");
		destroyKeyguardLock();
	}
	
	@Override
	public void onLowMemory()
	{
		super.onLowMemory();
		Log("!!!On Low Memory");
		// Really, really hope that nothing bad happens.
	}
	
	@Override
	public IBinder onBind(Intent intent)
	{
		return null;
	}
	
	@Override
	public void onStart(final Intent intent, final int startId)
	{
		super.onStart(intent, startId);

		handleCommand(intent);
	}
	
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		handleCommand(intent);
		return START_REDELIVER_INTENT;
	}
	
	private void handleCommand(final Intent intent)
	{
		synchronized(_synclock)
		{
			final String remote_action = intent.getStringExtra(EXTRA_RemoteAction);
			
			if(remote_action.equals(RemoteAction_EnableKeyguard))
			{
				onEnableKeyguard();
			}
			else if(remote_action.equals(RemoteAction_DisableKeyguard))
			{
				onDisableKeyguard();
			}
			else if(remote_action.equals(RemoteAction_DisableKeyguardOnCharging))
			{
				onDisableKeyguardOnCharging();
			}
			else if(remote_action.equals(RemoteAction_RefreshWidgets))
			{
				onRefreshWidgets();
			}
		}
	}
	
	private void updateAllWidgets(boolean notifyUser)
	{
		boolean isLockscreenEnabled = true;
		
		int lockscreenPreference = getKeyguardEnabledPreference();
		
		if(lockscreenPreference == Constants.KEYGUARD_Disabled)
		{
			isLockscreenEnabled = false;
			disableLockscreen(notifyUser);
		}
		else if(lockscreenPreference == Constants.KEYGUARD_DisableOnCharging)
		{
			if(isSystemCharging())
			{
				isLockscreenEnabled = false;
				disableLockscreen(notifyUser);
			}
			else
			{
				enableLockscreen(notifyUser);
			}
		}
		else if(lockscreenPreference == Constants.KEYGUARD_Enabled)
		{
			isLockscreenEnabled = true;
			enableLockscreen(notifyUser);
		}
		
		AppWidgetProvider1x1.UpdateAllWidgets(this, lockscreenPreference, isLockscreenEnabled);
	}
	
	private void disableLockscreen(boolean notifyUser)
	{
		setLockscreenMode(false, notifyUser);
	}
	
	private void enableLockscreen(boolean notifyUser)
	{
		setLockscreenMode(true, notifyUser);
	}
	
	private void setLockscreenMode(boolean enableLockscreen, boolean forceNotifyUser)
	{
		boolean actionPerformed;
		
		if(enableLockscreen)
		{
			actionPerformed = enableKeyguard();
		}
		else
		{
			actionPerformed = disableKeyguard();
		}
		
		final int mode = getKeyguardEnabledPreference();
		if(actionPerformed)
		{
			forceNotifyUser = true;
		}
		
		if(forceNotifyUser)
		{
			showLockscreenStateMessage(enableLockscreen, mode);
		}
	}
	
	
	private boolean isSystemCharging()
	{
		boolean isCharging = false;
		final Intent powerstate = Intents.getBatteryState(this); 
		if(powerstate != null)
		{
			final int battstate = powerstate.getIntExtra("status", BatteryManager.BATTERY_STATUS_FULL);
			if(battstate == BatteryManager.BATTERY_STATUS_CHARGING || battstate == BatteryManager.BATTERY_STATUS_FULL)
			{
				isCharging = true;
			}
		}
		
		return isCharging;
	}
	
	
	private void onRefreshWidgets()
	{
		updateAllWidgets(false);
	}

	private void onDisableKeyguard()
	{
		setKeyguardTogglePreference(Constants.KEYGUARD_Disabled);
		updateAllWidgets(true);
	}
	
	private void onEnableKeyguard()
	{
		setKeyguardTogglePreference(Constants.KEYGUARD_Enabled);
		updateAllWidgets(true);
		this.stopSelf();
	}
	
	private void onDisableKeyguardOnCharging()
	{
		setKeyguardTogglePreference(Constants.KEYGUARD_DisableOnCharging);
		updateAllWidgets(true);
	}
	
	private void showLockscreenStateMessage(boolean isLockscreenEnabled, int mode)
	{
		final Resources res = this.getResources();
		int highlightColor;
		String colorHexCode;
		int toggleResId;
		
		if(isLockscreenEnabled == true)
		{
			toggleResId = R.string.on;
			highlightColor = res.getColor(R.color.green);
		}
		else
		{
			toggleResId = R.string.off;
			highlightColor = res.getColor(R.color.red);
		}
		
		colorHexCode = Integer.toHexString(highlightColor);
		
		String baseMessage = this.getString(R.string.lockscreen_is);
		String toggleDescription = this.getString(toggleResId);
		StringBuilder sb = new StringBuilder();
		sb.append(baseMessage);
		
		sb.append(" <b><font color=\"#");
		sb.append(colorHexCode);
		sb.append("\">");
		sb.append(toggleDescription.toUpperCase());
		sb.append("</font></b>");
		sb.append(".");
		
		if(mode == Constants.KEYGUARD_DisableOnCharging)
		{
			sb.append("<br /><i>");
			sb.append(getString(R.string.lock_screen_is_disabled_while_charging));
			sb.append("</i>");
		}
		
		Toast.makeText(this, Html.fromHtml(sb.toString()), Toast.LENGTH_LONG).show();
	}

	private int getKeyguardEnabledPreference()
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		return prefs.getInt(Constants.Preference_KeyguardToggle, Constants.KEYGUARD_Enabled);
	}
	
	private void setKeyguardTogglePreference(final int param)
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		prefs.edit().putInt(Constants.Preference_KeyguardToggle, param).commit();
	}
}
