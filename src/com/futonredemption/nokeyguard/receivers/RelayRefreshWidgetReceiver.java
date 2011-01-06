package com.futonredemption.nokeyguard.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

public class RelayRefreshWidgetReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.w("RelayRefreshWidgetReceiver", intent.getAction());
	}
	

	public static void startReceiver(final Context context, final RelayRefreshWidgetReceiver receiver) {
		final IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_SCREEN_OFF);
		filter.addAction(Intent.ACTION_SCREEN_ON);
		filter.addAction(Intent.ACTION_HEADSET_PLUG);
		filter.addAction(Intent.ACTION_CAMERA_BUTTON);
		filter.addAction(Intent.ACTION_CALL_BUTTON);
		
		//filter.addAction(Intent.ACTION_DOCK_EVENT);
		
		context.registerReceiver(receiver, filter);
	}
	
	public static void stopReceiver(final Context context, final RelayRefreshWidgetReceiver receiver) {
		context.unregisterReceiver(receiver);
	}
}
