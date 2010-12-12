package org.beryl.app;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;

/**
 * Sets an Android Service into foreground state with a notification.
 * Since 2.0, Android requires that all foregrounded service have a pending notification shown.
 * This class provides backwards compatibility to older Android handsets that do not have this API.
 * 
 * @author jeremyje
 *
 */
public class ServiceForegrounder {
	
	private final Service _service;
	private NotificationManager _notificationManager = null;
	private int _notificationId = -1;
	
	public ServiceForegrounder(final Service service) {
		_service = service;
	}

	private NotificationManager getNotificationManager() {
		if(_notificationManager == null) {
			_notificationManager = (NotificationManager) _service.getSystemService(Service.NOTIFICATION_SERVICE);
		}
		return _notificationManager;
	}
	public void startForeground(final int notificationId, final int resIconId, final int title, final int description, final int tickerText, final PendingIntent onClickIntent) {
		final CharSequence titleString = _service.getText(title);
		final CharSequence descriptionString = _service.getText(description);
		final CharSequence tickerTextString = _service.getText(tickerText);
		
		startForeground(notificationId, resIconId, titleString, descriptionString, tickerTextString, onClickIntent);
	}

	public void startForeground(final int notificationId, final int resIconId, final CharSequence title, final CharSequence description, final CharSequence tickerText, final PendingIntent onClickIntent) {
		
		final Notification notifier = new Notification();
		notifier.flags = Notification.FLAG_ONGOING_EVENT | Notification.FLAG_FOREGROUND_SERVICE;
		notifier.icon = resIconId;

		notifier.tickerText = tickerText;
		notifier.setLatestEventInfo(_service, title, description, onClickIntent);
		
		startForeground(notificationId, notifier);
	}
	
	public void startForeground(final int notificationId, final Notification notification) {
		
		// If currently foregrounded and the new id is different, cancel the old id.
		if(isForegrounded()) {
			if (_notificationId != notificationId) {
				stopForeground();
			}
		}
		
		if(AndroidVersion.isEclairOrHigher()) {
			_service.startForeground(notificationId, notification);
		}
		else {
			_service.setForeground(true);
			final NotificationManager nm = getNotificationManager();
			nm.notify(notificationId, notification);
		}
	}
	
	public boolean isForegrounded() {
		return _notificationId != -1;
	}
	
	public void stopForeground() {
		
		if(AndroidVersion.isEclairOrHigher()) {
			_service.stopForeground(true);
		}
		else {
			final NotificationManager nm = getNotificationManager();
			nm.cancel(_notificationId);
			_service.setForeground(false);
		}
	}
}
