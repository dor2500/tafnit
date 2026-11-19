package com.tafnit.transit.service

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.tafnit.transit.domain.repository.TransitRepository
import com.tafnit.transit.widget.TafnitCommuteWidget
import com.tafnit.transit.widget.TafnitNearbyWidget
import androidx.glance.appwidget.updateAll
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

@HiltWorker
class WidgetRefreshWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        runCatching {
            TafnitNearbyWidget().updateAll(context)
            TafnitCommuteWidget().updateAll(context)
        }
        return Result.success()
    }
}

@HiltWorker
class ServiceAlertsWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val transitRepository: TransitRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        runCatching {
            transitRepository.observeServiceAlerts().first()
        }
        return Result.success()
    }
}

object TafnitWorkScheduler {
    fun schedulePeriodicWork(context: Context) {
        val wm = WorkManager.getInstance(context)
        val widgetReq = PeriodicWorkRequestBuilder<WidgetRefreshWorker>(15, TimeUnit.MINUTES).build()
        val alertsReq = PeriodicWorkRequestBuilder<ServiceAlertsWorker>(30, TimeUnit.MINUTES).build()
        wm.enqueueUniquePeriodicWork("tafnit_widget_refresh", ExistingPeriodicWorkPolicy.KEEP, widgetReq)
        wm.enqueueUniquePeriodicWork("tafnit_alerts_sync", ExistingPeriodicWorkPolicy.KEEP, alertsReq)
    }
}
