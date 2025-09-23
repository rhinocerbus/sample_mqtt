package com.piledrive.sample_mqtt

import android.app.Application
import android.content.pm.ApplicationInfo
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class AppClass : Application() {
	override fun onCreate() {
		super.onCreate()

		if (isDebug()) {
			Timber.plant(Timber.DebugTree())
		}
	}
}

fun Application.isDebug(): Boolean {
	return applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
}