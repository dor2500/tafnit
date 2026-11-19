package com.tafnit.transit

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.tafnit.transit.service.TafnitWorkScheduler
import dagger.hilt.android.HiltAndroidApp
import org.osmdroid.config.Configuration as OsmConfiguration
import javax.inject.Inject

@HiltAndroidApp
class TafnitApplication : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        OsmConfiguration.getInstance().userAgentValue = packageName
        TafnitWorkScheduler.schedulePeriodicWork(this)
    }
}
