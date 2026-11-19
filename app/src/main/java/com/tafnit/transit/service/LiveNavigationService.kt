package com.tafnit.transit.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.tafnit.transit.MainActivity
import com.tafnit.transit.R
import com.tafnit.transit.domain.model.ActiveNavigationState
import com.tafnit.transit.domain.model.NavigationPhase
import com.tafnit.transit.domain.model.TransitMode
import com.tafnit.transit.domain.repository.UserPreferencesRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LiveNavigationService : LifecycleService() {

    @Inject lateinit var navigationManager: LiveNavigationManager
    @Inject lateinit var preferencesRepository: UserPreferencesRepository

    private var lastAlertStage = 0

    override fun onCreate() {
        super.onCreate()
        createChannels()
        lifecycleScope.launch {
            navigationManager.state.collect { state ->
                if (state.isActive) {
                    val notification = buildOngoingNotification(this@LiveNavigationService, state)
                    startForeground(ONGOING_NOTIF_ID, notification)
                    maybeTriggerGetOffAlert(state)
                } else {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                }
            }
        }
        lifecycleScope.launch {
            while (true) {
                delay(5000L)
                val state = navigationManager.state.value
                if (state.isActive && state.isSimulating && state.phase != NavigationPhase.ARRIVED) {
                    navigationManager.advanceSimulationStep()
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        if (intent?.action == ACTION_STOP) {
            navigationManager.stopNavigation()
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
        return START_STICKY
    }

    private suspend fun maybeTriggerGetOffAlert(state: ActiveNavigationState) {
        val prefs = preferencesRepository.preferences.first()
        val stage = state.alertStageTriggered
        if (stage == 0 || stage == lastAlertStage) return
        if (stage == 2 && prefs.getOffAlertStopsBefore < 2) return

        lastAlertStage = stage
        val (title, body) = when (stage) {
            2 -> getString(R.string.alert_two_stops_title) to
                getString(R.string.alert_two_stops_body, state.alightingStopName)
            1 -> getString(R.string.alert_one_stop_title) to
                getString(R.string.alert_one_stop_body, state.alightingStopName)
            else -> getString(R.string.alert_arrived_title) to
                getString(R.string.alert_arrived_body, state.alightingStopName)
        }

        val manager = getSystemService(NotificationManager::class.java)
        val alertNotif = NotificationCompat.Builder(this, CHANNEL_ALERTS_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setSilent(!prefs.alertSound)
            .build()
        manager.notify(ALERT_NOTIF_ID, alertNotif)

        if (prefs.alertVibration) {
            vibrateAlert()
        }
    }

    private fun vibrateAlert() {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            getSystemService(VibratorManager::class.java)?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
        vibrator?.vibrate(VibrationEffect.createOneShot(350L, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    private fun createChannels() {
        val manager = getSystemService(NotificationManager::class.java)
        val navChannel = NotificationChannel(
            CHANNEL_NAV_ID,
            getString(R.string.notif_channel_nav_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = getString(R.string.notif_channel_nav_desc)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }
        val alertsChannel = NotificationChannel(
            CHANNEL_ALERTS_ID,
            getString(R.string.notif_channel_alerts_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = getString(R.string.notif_channel_alerts_desc)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }
        manager.createNotificationChannel(navChannel)
        manager.createNotificationChannel(alertsChannel)
    }

    companion object {
        const val ACTION_START = "com.tafnit.transit.action.START_NAV"
        const val ACTION_STOP = "com.tafnit.transit.action.STOP_NAV"
        const val CHANNEL_NAV_ID = "tafnit_live_navigation"
        const val CHANNEL_ALERTS_ID = "tafnit_get_off_alerts"
        const val ONGOING_NOTIF_ID = 1001
        const val ALERT_NOTIF_ID = 1002

        fun buildOngoingNotification(context: Context, state: ActiveNavigationState): Notification {
            val openIntent = PendingIntent.getActivity(
                context,
                0,
                Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val stopIntent = PendingIntent.getService(
                context,
                1,
                Intent(context, LiveNavigationService::class.java).apply {
                    action = ACTION_STOP
                },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val activeTransitLeg = state.itinerary?.legs?.firstOrNull { it.mode != TransitMode.WALK }
            val lineLabel = activeTransitLeg?.lineNumber ?: "274"
            val title = when (state.phase) {
                NavigationPhase.WALKING_TO_STOP -> context.getString(R.string.leg_walking)
                NavigationPhase.WAITING_AT_STOP -> context.getString(R.string.leg_waiting, lineLabel)
                NavigationPhase.ON_BOARD -> "${context.getString(R.string.leg_onboard, lineLabel)} • ${context.getString(R.string.stops_remaining, state.stopsRemaining)}"
                NavigationPhase.ARRIVED -> context.getString(R.string.leg_arrived)
            }
            val subText = "${context.getString(R.string.next_stop_label, state.nextStopName)} • ${context.getString(R.string.min_away, state.etaMinutes)}"
            val progressMax = 4
            val progressCurrent = when (state.phase) {
                NavigationPhase.WALKING_TO_STOP -> 1
                NavigationPhase.WAITING_AT_STOP -> 2
                NavigationPhase.ON_BOARD -> (4 - state.stopsRemaining).coerceIn(2, 3)
                NavigationPhase.ARRIVED -> 4
            }

            return NotificationCompat.Builder(context, CHANNEL_NAV_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(subText)
                .setSubText(context.getString(R.string.app_name))
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setCategory(NotificationCompat.CATEGORY_NAVIGATION)
                .setProgress(progressMax, progressCurrent, false)
                .setContentIntent(openIntent)
                .addAction(
                    android.R.drawable.ic_menu_close_clear_cancel,
                    context.getString(R.string.stop_navigation),
                    stopIntent
                )
                .build()
        }
    }
}
