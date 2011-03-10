package com.futonredemption.nokeyguard;

import com.futonredemption.nokeyguard.activities.NoKeyguardPreferenceActivity;
import com.futonredemption.nokeyguard.services.DisableKeyguardService;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class Intents {
	public static final Intent disableKeyguard(final Context context) {
		final Intent result = new Intent(context, DisableKeyguardService.class);
		result.putExtra(DisableKeyguardService.EXTRA_RemoteAction, DisableKeyguardService.RemoteAction_DisableKeyguard);
		result.putExtra(DisableKeyguardService.EXTRA_ForceNotify, true);

		return result;
	}

	public static final Intent enableKeyguard(final Context context) {
		final Intent result = new Intent(context, DisableKeyguardService.class);
		result.putExtra(DisableKeyguardService.EXTRA_RemoteAction, DisableKeyguardService.RemoteAction_EnableKeyguard);
		result.putExtra(DisableKeyguardService.EXTRA_ForceNotify, true);
		return result;
	}

	public static final Intent userInvokedRefreshWidgets(final Context context) {
		final Intent result = new Intent(context, DisableKeyguardService.class);
		result.putExtra(DisableKeyguardService.EXTRA_RemoteAction, DisableKeyguardService.RemoteAction_RefreshWidgets);
		result.putExtra(DisableKeyguardService.EXTRA_ForceNotify, false);
		return result;
	}
	
	public static final Intent refreshWidgets(final Context context) {
		final Intent result = new Intent(context, DisableKeyguardService.class);
		result.putExtra(DisableKeyguardService.EXTRA_RemoteAction, DisableKeyguardService.RemoteAction_RefreshWidgets);
		result.putExtra(DisableKeyguardService.EXTRA_ForceNotify, false);
		
		return result;
	}
	
	public static final Intent showPreferencesActivity(final Context context) {
		final Intent result = new Intent(context, NoKeyguardPreferenceActivity.class);
		return result;
	}
	
	public static final Intent getBatteryState(final Context context) {
		return context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
	}
	

	public static PendingIntent pendingShowPreferencesActivity(final Context context) {
		return PendingIntent.getActivity(context, 0, Intents.showPreferencesActivity(context), PendingIntent.FLAG_UPDATE_CURRENT);
	}

	public static PendingIntent pendingEnableKeyguard(Context context) {
		return PendingIntent.getService(context, 0, Intents.enableKeyguard(context), PendingIntent.FLAG_UPDATE_CURRENT);
	}

	public static PendingIntent pendingDisableKeyguard(Context context) {
		return PendingIntent.getService(context, 0, Intents.disableKeyguard(context), PendingIntent.FLAG_UPDATE_CURRENT);
	}
	
	public static final String ACTION_LOCKSTATE = "com.futonredemption.nokeyguard.lockstate";
	
	public static IntentFilter broadcastLockStateIntentFilter() {
		return new IntentFilter(ACTION_LOCKSTATE);
	}
	public static Intent broadcastLockState(final LockScreenState state) {
		final Intent intent = new Intent(ACTION_LOCKSTATE);
		intent.putExtra("isActive", state.IsLockActive);
		intent.putExtra("mode", state.Mode);
		return intent;
	}

}
