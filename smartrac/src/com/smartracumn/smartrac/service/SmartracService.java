package com.smartracumn.smartrac.service;

import java.text.ParseException;
import java.util.Calendar;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.smartracumn.smartrac.MainActivity;
import com.smartracumn.smartrac.R;
import com.smartracumn.smartrac.data.SmartracData;
import com.smartracumn.smartrac.service.CalendarItemUtility.CalendarItemService;
import com.smartracumn.smartrac.service.SmartracDataService.State;
import com.smartracumn.smartrac.util.ActivityDetector;
import com.smartracumn.smartrac.util.SmartracDataFormat;

public class SmartracService extends Service {
	private final String TAG = getClass().getName();

	private final IBinder smartracBinder = new SmartracBinder();

	private SmartracData smartracData;

	private SmartracDataService smartracDataService;

	private CalendarItemUtility calendarItemUtil;

	private ActivityDetector activityDetector;

	private int smallIconId = R.drawable.smartrac_data_service_off_icon;

	private static boolean serviceStarted = false;

	private BroadcastReceiver dataServiceOnOffBroadcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			Log.i(TAG, "data service switch received.");

			Bundle extra = intent.getExtras();
			boolean dataServiceOn = extra.getBoolean(context.getResources()
					.getString(R.string.data_service_state));

			if (dataServiceOn) {
				smartracDataService.startDataService();
			} else {
				smartracDataService.stopDataService();
			}
		}
	};

	private BroadcastReceiver dataServiceStateChangeBroadcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			Log.i(TAG, "data service state change received.");

			Bundle extra = intent.getExtras();
			String state = extra.getString(context.getResources().getString(
					R.string.data_service_state));

			if (!state.equals(State.Off.name())
					&& smallIconId == R.drawable.smartrac_data_service_off_icon) {
				smallIconId = R.drawable.smartrac;
			} else if (state.equals(State.Off.name())
					&& smallIconId != R.drawable.smartrac_data_service_off_icon) {
				smallIconId = R.drawable.smartrac_data_service_off_icon;
			}

			NotificationManager mNotificationManager = (NotificationManager) getSystemService(SmartracService.NOTIFICATION_SERVICE);
			mNotificationManager.notify(1,
					generateNotification("Data Service: " + state));
		}
	};

	private BroadcastReceiver dataServiceScheduleReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			scheduleDataServiceAlarm();
		}
	};

	public static class DataServiceScheduledStartBroadcastReceiver extends
			BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			Log.i("AlarmReceiver", "start received");

			context.getSharedPreferences(
					context.getResources().getString(R.string.app_domain),
					Context.MODE_PRIVATE)
					.edit()
					.putBoolean(
							context.getResources().getString(
									R.string.smartrac_service_switch), true)
					.commit();

			Intent it = new Intent(context.getResources().getString(
					R.string.data_service_on_off_broadcast));
			it.putExtra(
					context.getResources().getString(
							R.string.data_service_state), true);

			context.sendBroadcast(it);
		}
	}

	public static class DataServiceScheduledStopBroadcastReceiver extends
			BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			Log.i("AlarmReceiver", "stop received");

			context.getSharedPreferences(
					context.getResources().getString(R.string.app_domain),
					Context.MODE_PRIVATE)
					.edit()
					.putBoolean(
							context.getResources().getString(
									R.string.smartrac_service_switch), false)
					.commit();

			Intent it = new Intent(context.getResources().getString(
					R.string.data_service_on_off_broadcast));
			it.putExtra(
					context.getResources().getString(
							R.string.data_service_state), false);

			context.sendBroadcast(it);
		}

	}

	private void scheduleDataServiceAlarm() {
		boolean scheduleEnabled = getSharedPreferences(
				getResources().getString(R.string.app_domain), MODE_PRIVATE)
				.getBoolean(
						getResources().getString(
								R.string.smartrac_service_schedule_enabled),
						false);

		if (scheduleEnabled) {
			AlarmManager alarmMgr = (AlarmManager) this
					.getSystemService(Context.ALARM_SERVICE);
			Calendar calendar = Calendar.getInstance();
			Calendar time = Calendar.getInstance();

			String scheduledStart = getSharedPreferences(
					getResources().getString(R.string.app_domain), MODE_PRIVATE)
					.getString(
							getResources()
									.getString(
											R.string.smartrac_service_start_time_of_day),
							"00:00 AM");

			try {
				time.setTime(SmartracDataFormat.getTimeFormat().parse(
						scheduledStart));
			} catch (ParseException e) {
				e.printStackTrace();
			}

			calendar.setTimeInMillis(System.currentTimeMillis());
			calendar.set(Calendar.HOUR_OF_DAY, time.get(Calendar.HOUR_OF_DAY));
			calendar.set(Calendar.MINUTE, time.get(Calendar.MINUTE));
			calendar.set(Calendar.SECOND, time.get(Calendar.SECOND));
			calendar.set(Calendar.MILLISECOND, time.get(Calendar.MILLISECOND));

			if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
				calendar.add(Calendar.MILLISECOND,
						(int) AlarmManager.INTERVAL_DAY);
			}

			Intent startIntent = new Intent(this,
					DataServiceScheduledStartBroadcastReceiver.class);
			PendingIntent alarmStartIntent = PendingIntent.getBroadcast(this,
					0, startIntent, 0);

			alarmMgr.cancel(alarmStartIntent);
			alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP,
					calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY,
					alarmStartIntent);

			String scheduledStop = getSharedPreferences(
					getResources().getString(R.string.app_domain), MODE_PRIVATE)
					.getString(
							getResources().getString(
									R.string.smartrac_service_stop_time_of_day),
							"00:00 AM");

			try {
				time.setTime(SmartracDataFormat.getTimeFormat().parse(
						scheduledStop));
			} catch (ParseException e) {
				e.printStackTrace();
			}

			calendar.setTimeInMillis(System.currentTimeMillis());
			calendar.set(Calendar.HOUR_OF_DAY, time.get(Calendar.HOUR_OF_DAY));
			calendar.set(Calendar.MINUTE, time.get(Calendar.MINUTE));
			calendar.set(Calendar.SECOND, time.get(Calendar.SECOND));
			calendar.set(Calendar.MILLISECOND, time.get(Calendar.MILLISECOND));

			if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
				calendar.set(Calendar.MILLISECOND,
						(int) AlarmManager.INTERVAL_DAY);
			}

			Intent stopIntent = new Intent(this,
					DataServiceScheduledStopBroadcastReceiver.class);
			PendingIntent alarmStopIntent = PendingIntent.getBroadcast(this, 0,
					stopIntent, 0);

			alarmMgr.cancel(alarmStopIntent);
			alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP,
					calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY,
					alarmStopIntent);
		}
	}

	/**
	 * On create, instantiate dwelling/non-dwelling separator and mode detector.
	 * Bring service foreground so that it keeps sampling data until user stops
	 * service.
	 */
	@Override
	public void onCreate() {
		Log.i(TAG, getClass().getSimpleName() + ":entered onCreate()");
		super.onCreate();

		smartracData = new SmartracData(this);
		smartracDataService = new SmartracDataService(this, 30 * 1000,
				smartracData);

		activityDetector = new ActivityDetector(this, new Handler());
		calendarItemUtil = new CalendarItemUtility(this, smartracData,
				activityDetector);

		IntentFilter summaryUpdatedFilter = new IntentFilter(getResources()
				.getString(R.string.dwelling_summary_updated_broadcast));

		IntentFilter locationNotAvailableFilter = new IntentFilter(
				getResources().getString(
						R.string.location_not_available_broadcast));

		IntentFilter modeChangeFilter = new IntentFilter(getResources()
				.getString(R.string.mode_changed_broadcast));

		IntentFilter dwellingChangeFilter = new IntentFilter(getResources()
				.getString(R.string.dwelling_changed_broadcast));

		IntentFilter serviceStartedFilter = new IntentFilter(getResources()
				.getString(R.string.service_started_broadcast));

		IntentFilter dataServiceStateFilter = new IntentFilter(getResources()
				.getString(R.string.data_service_state_change_broadcast));

		IntentFilter dataServiceOnOffFilter = new IntentFilter(getResources()
				.getString(R.string.data_service_on_off_broadcast));

		IntentFilter dataServiceRescheduleFilter = new IntentFilter(
				getResources().getString(
						R.string.data_service_reschedule_broadcast));

		registerReceiver(dataServiceScheduleReceiver,
				dataServiceRescheduleFilter);

		registerReceiver(activityDetector.getDwellingLocationManager()
				.getReceiver(), summaryUpdatedFilter);

		registerReceiver(calendarItemUtil.getInaccurateGPSReceiver(),
				locationNotAvailableFilter);

		registerReceiver(calendarItemUtil.getActivityChangeReceiver(),
				dwellingChangeFilter);

		registerReceiver(calendarItemUtil.getModeChangeReceiver(),
				modeChangeFilter);

		registerReceiver(calendarItemUtil.getDataServiceStartReceiver(),
				serviceStartedFilter);

		registerReceiver(dataServiceStateChangeBroadcastReceiver,
				dataServiceStateFilter);

		registerReceiver(dataServiceOnOffBroadcastReceiver,
				dataServiceOnOffFilter);

		Notification startNotification = generateNotification("Data Service: OFF");
		startForeground(1, startNotification);
	}

	public static Bitmap drawableToBitmap(Drawable drawable) {
		if (drawable instanceof BitmapDrawable) {
			return ((BitmapDrawable) drawable).getBitmap();
		}

		int width = drawable.getIntrinsicWidth();
		width = width > 0 ? width : 1;
		int height = drawable.getIntrinsicHeight();
		height = height > 0 ? height : 1;

		Bitmap bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
		drawable.draw(canvas);

		return bitmap;
	}

	private Notification generateNotification(String contentText) {
		Drawable icon = this.getResources().getDrawable(R.drawable.action_map);
		Bitmap bitmapIcon = drawableToBitmap(icon);

		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				this).setSmallIcon(smallIconId).setLargeIcon(bitmapIcon)
				.setContentTitle(getText(R.string.smartrac_service))
				.setContentText(contentText);

		Intent resultIntent = new Intent(this, MainActivity.class);

		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

		stackBuilder.addParentStack(MainActivity.class);

		stackBuilder.addNextIntent(resultIntent);
		PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,
				PendingIntent.FLAG_UPDATE_CURRENT);
		mBuilder.setContentIntent(resultPendingIntent);

		return mBuilder.build();
	}

	@Override
	public void onDestroy() {
		Log.i(TAG, getClass().getSimpleName() + ":entered onDestroy()");

		super.onDestroy();
	}

	/**
	 * On start command start listening to location and motion changes.
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i(TAG, getClass().getSimpleName() + ":entered onStartCommand()");

		if (serviceStarted) {
			return startId;
		}

		boolean dataServiceOn = getSharedPreferences(
				this.getResources().getString(R.string.app_domain),
				Context.MODE_PRIVATE).getBoolean(
				getResources().getString(R.string.smartrac_service_switch),
				true);

		if (dataServiceOn) {
			smartracDataService.startDataService();
		}

		scheduleDataServiceAlarm();

		serviceStarted = true;

		return startId;
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return smartracBinder;
	}

	public class SmartracBinder extends Binder {
		public CalendarItemService getService() {
			return calendarItemUtil.getService();
		}
	}
}
