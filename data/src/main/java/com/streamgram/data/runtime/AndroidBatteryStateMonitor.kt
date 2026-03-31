package com.streamgram.data.runtime

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.PowerManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

data class DevicePowerState(
    val batteryPercent: Int,
    val isCharging: Boolean,
    val systemPowerSaveEnabled: Boolean,
) {
    val isBatteryLow: Boolean = batteryPercent in 1..20
}

@Singleton
class AndroidBatteryStateMonitor @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun observeState(): Flow<DevicePowerState> = callbackFlow {
        val appContext = context.applicationContext
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                trySend(appContext.toPowerState(intent))
            }
        }

        trySend(appContext.toPowerState(appContext.registerReceiver(null, filter)))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            appContext.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("DEPRECATION")
            appContext.registerReceiver(receiver, filter)
        }

        awaitClose {
            runCatching { appContext.unregisterReceiver(receiver) }
        }
    }.distinctUntilChanged()
}

private fun Context.toPowerState(intent: Intent?): DevicePowerState {
    val batteryManager = getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
    val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
    val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
    val percentFromIntent = if (level >= 0 && scale > 0) (level * 100) / scale else -1
    val batteryPercent = when {
        percentFromIntent in 0..100 -> percentFromIntent
        else -> batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)?.coerceIn(0, 100) ?: 100
    }
    val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
    val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
    val powerManager = getSystemService(Context.POWER_SERVICE) as? PowerManager
    return DevicePowerState(
        batteryPercent = batteryPercent,
        isCharging = isCharging,
        systemPowerSaveEnabled = powerManager?.isPowerSaveMode == true,
    )
}
