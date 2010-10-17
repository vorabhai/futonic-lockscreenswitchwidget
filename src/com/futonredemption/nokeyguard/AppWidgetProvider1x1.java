package com.futonredemption.nokeyguard;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class AppWidgetProvider1x1 extends AppWidgetProvider
{
	@Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
	{
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		context.startService(Intents.refreshWidgets(context));
	}
	
	@Override
	public void onDisabled(Context context)
	{
		context.startService(Intents.enableKeyguard(context));
	}
	
	public static void UpdateAllWidgets(final Context context, final boolean is_keyguardactive)
	{
		int i, len;
		final AppWidgetManager widget_manager = AppWidgetManager.getInstance(context);
		final int [] ids1x1 = widget_manager.getAppWidgetIds(new ComponentName(context, AppWidgetProvider1x1.class));
		
		len = ids1x1.length;
		for(i = 0; i < len; i++)
		{
			AppWidgetProvider1x1.UpdateWidget(context, widget_manager, ids1x1[i], is_keyguardactive);
		}
	}

	public static void UpdateWidget(Context context, AppWidgetManager widgetManager, int widget_id, boolean is_keyguardactive)
	{
		RemoteViews views = null;
		
		views = new RemoteViews(context.getPackageName(), R.layout.appwidget_1x1);

		Intent intent = null;
		int iconId = 0;
		int indicatorId = 0;
		
		if(is_keyguardactive)
		{
			intent = Intents.disableKeyguard(context);
			indicatorId = R.drawable.appwidget_settings_ind_on_single;
			iconId = R.drawable.ic_appwidget_screenlock_on;
		}
		else
		{
			intent = Intents.enableKeyguard(context);
			indicatorId = R.drawable.appwidget_settings_ind_off_single;
			iconId = R.drawable.ic_appwidget_screenlock_off;
		}
		
		final PendingIntent pIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		views.setOnClickPendingIntent(R.id.btnKeyguard, pIntent);
		views.setImageViewResource(R.id.imgKeyguard, iconId);
		views.setImageViewResource(R.id.indKeyguard, indicatorId);
		widgetManager.updateAppWidget(widget_id, views);
	}
}
