package com.smartracumn.smartrac.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class SmartracStartReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		// Check user settings. Start only when user set service on in smartrac
		// settings.
		Intent service = new Intent(context, SmartracService.class);
		context.startService(service);
	}
}