package com.futonredemption.nokeyguard;

import com.futonredemption.nokeyguard.activities.NoKeyguardPreferenceActivity;
import com.futonredemption.nokeyguard.services.DisableKeyguardService;

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
	
	public static final boolean isUserInvoked(final Intent intent) {
		return intent.getBooleanExtra(DisableKeyguardService.EXTRA_UserInvoked, false);
	}
	
	public static final void attachUserInvoked(final Intent intent) {
		intent.putExtra(DisableKeyguardService.EXTRA_UserInvoked, true);
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
}
