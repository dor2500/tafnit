package com.tafnit.transit.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.tafnit.transit.MainActivity
import com.tafnit.transit.R
import com.tafnit.transit.data.mock.IsraelTransitDataset
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class TafnitNearbyWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TafnitNearbyWidget()
}

class RefreshNearbyWidgetAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        TafnitNearbyWidget().update(context, glanceId)
    }
}

class TafnitNearbyWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val stop = IsraelTransitDataset.stops.first()
        val updatedTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
        val isHebrew = context.resources.configuration.locales[0].language == "iw" ||
            context.resources.configuration.locales[0].language == "he"

        provideContent {
            GlanceTheme {
                NearbyWidgetContent(
                    stopName = stop.localizedName(isHebrew),
                    stopCode = stop.code,
                    arrivals = stop.upcomingArrivals.take(3).map {
                        Triple(
                            it.lineNumber,
                            if (isHebrew) it.headsignHe else it.headsignEn,
                            context.getString(R.string.min_away, it.etaMinutes)
                        )
                    },
                    updatedLabel = context.getString(R.string.widget_updated_at, updatedTime),
                    refreshLabel = context.getString(R.string.widget_refresh)
                )
            }
        }
    }
}

@Composable
fun NearbyWidgetContent(
    stopName: String,
    stopCode: String,
    arrivals: List<Triple<String, String, String>>,
    updatedLabel: String,
    refreshLabel: String
) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.surface)
            .cornerRadius(24.dp)
            .padding(14.dp)
            .clickable(actionStartActivity<MainActivity>())
    ) {
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = GlanceModifier.defaultWeight()) {
                Text(
                    text = stopName,
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurface,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 1
                )
                Text(
                    text = "#$stopCode • $updatedLabel",
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurfaceVariant,
                        fontSize = 11.sp
                    ),
                    maxLines = 1
                )
            }
            Box(
                modifier = GlanceModifier
                    .background(GlanceTheme.colors.secondaryContainer)
                    .cornerRadius(12.dp)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                    .clickable(actionRunCallback<RefreshNearbyWidgetAction>())
            ) {
                Text(
                    text = refreshLabel,
                    style = TextStyle(
                        color = GlanceTheme.colors.onSecondaryContainer,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }

        Spacer(modifier = GlanceModifier.height(10.dp))

        arrivals.forEach { (line, headsign, eta) ->
            Row(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = GlanceModifier
                        .background(GlanceTheme.colors.primaryContainer)
                        .cornerRadius(8.dp)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = line,
                        style = TextStyle(
                            color = GlanceTheme.colors.onPrimaryContainer,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
                Spacer(modifier = GlanceModifier.width(8.dp))
                Text(
                    text = headsign,
                    modifier = GlanceModifier.defaultWeight(),
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurface,
                        fontSize = 13.sp
                    ),
                    maxLines = 1
                )
                Spacer(modifier = GlanceModifier.width(6.dp))
                Box(
                    modifier = GlanceModifier
                        .background(GlanceTheme.colors.tertiaryContainer)
                        .cornerRadius(10.dp)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = eta,
                        style = TextStyle(
                            color = GlanceTheme.colors.onTertiaryContainer,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}
