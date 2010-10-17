package com.futonredemption.nokeyguard;

import android.app.KeyguardManager;
import android.app.Service;
import android.app.KeyguardManager.KeyguardLock;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
public class DisableKeyguardService extends Service
{
	private static final String KeyGuardTag = "DisableKeyguardService";
	private KeyguardManager _guard = null;
	private KeyguardLock _lock = null;
	private Object _synclock = new Object();
	//private boolean _isKeyguardEnabled = true;
	
	public static final String EXTRA_RemoteAction = "EXTRA_RemoteAction";
	public static final String RemoteAction_EnableKeyguard = "RemoteAction_EnableKeyguard";
	public static final String RemoteAction_DisableKeyguard = "RemoteAction_DisableKeyguard";
	public static final String RemoteAction_RefreshWidgets = "RemoteAction_RefreshWidgets";
	
	public static final String Preference_KeyguardToggle = "Preference_KeyguardToggle";
	
	//private static final String TAG = "DKS";
	
	@Override
	public void onCreate()
	{
		super.onCreate();
		
		synchronized(_synclock)
		{
			maybeCreateKeyguardLock();
		}
	}
	
	private void maybeCreateKeyguardLock()
	{
		if(_lock == null)
		{
			_guard = (KeyguardManager)this.getSystemService(Context.KEYGUARD_SERVICE);
	        _lock = _guard.newKeyguardLock(KeyGuardTag);
		}
	}

	public void onDestroy()
	{
		super.onDestroy();
		synchronized(_synclock)
		{
			reenableKeyguard();
		}
	}
	
	@Override
	public void onLowMemory()
	{
		super.onLowMemory();
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
			else if(remote_action.equals(RemoteAction_RefreshWidgets))
			{
				onRefreshWidgets();
			}
		}
	}
	
	private void updateAllWidgets()
	{
		boolean keyguard_enabled = getKeyguardEnabledPreference();
		
		if(keyguard_enabled == false)
		{
			disableKeyguard();
		}
		
		AppWidgetProvider1x1.UpdateAllWidgets(this, keyguard_enabled);
	}
	
	private void onRefreshWidgets()
	{
		updateAllWidgets();
	}

	private void onDisableKeyguard()
	{
		setKeyguardTogglePreference(false);
		disableKeyguard();
		updateAllWidgets();
	}
	
	private void onEnableKeyguard()
	{
		setKeyguardTogglePreference(true);
		synchronized(_synclock)
		{
			reenableKeyguard();
		}
		
		updateAllWidgets();
		this.stopSelf();
	}

	private void disableKeyguard()
	{
		synchronized(_synclock)
		{
			maybeCreateKeyguardLock();
			_lock.disableKeyguard();
		}
	}
	
	private void reenableKeyguard()
	{
		if(_lock != null)
		{
	        _lock.reenableKeyguard();
	        _lock = null;
	        _guard = null;
		}
	}

	private boolean getKeyguardEnabledPreference()
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		return prefs.getBoolean(Preference_KeyguardToggle, true);
	}
	
	private void setKeyguardTogglePreference(final boolean param)
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		prefs.edit().putBoolean(Preference_KeyguardToggle, param).commit();
	}
}
