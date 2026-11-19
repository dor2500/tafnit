package com.tafnit.transit.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
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

class TafnitCommuteWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TafnitCommuteWidget()
}

class TafnitCommuteWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val homeLabel = context.getString(R.string.chip_home)
        val workLabel = context.getString(R.string.chip_work)
        val titleLabel = context.getString(R.string.app_name)
        val min3 = context.getString(R.string.min_away, 3)
        val min6 = context.getString(R.string.min_away, 6)

        provideContent {
            GlanceTheme {
                CommuteWidgetContent(
                    appTitle = titleLabel,
                    homeLabel = homeLabel,
                    workLabel = workLabel,
                    line1 = "274",
                    line1Eta = min3,
                    line2 = "R1",
                    line2Eta = min6
                )
            }
        }
    }
}

@Composable
fun CommuteWidgetContent(
    appTitle: String,
    homeLabel: String,
    workLabel: String,
    line1: String,
    line1Eta: String,
    line2: String,
    line2Eta: String
) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.surface)
            .cornerRadius(24.dp)
            .padding(14.dp)
            .clickable(actionStartActivity<MainActivity>())
    ) {
        Text(
            text = appTitle,
            style = TextStyle(
                color = GlanceTheme.colors.onSurface,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        )
        Spacer(modifier = GlanceModifier.height(10.dp))
        Row(modifier = GlanceModifier.fillMaxWidth()) {
            Box(
                modifier = GlanceModifier
                    .defaultWeight()
                    .background(GlanceTheme.colors.primaryContainer)
                    .cornerRadius(16.dp)
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = homeLabel,
                    style = TextStyle(
                        color = GlanceTheme.colors.onPrimaryContainer,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
            Spacer(modifier = GlanceModifier.width(8.dp))
            Box(
                modifier = GlanceModifier
                    .defaultWeight()
                    .background(GlanceTheme.colors.secondaryContainer)
                    .cornerRadius(16.dp)
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = workLabel,
                    style = TextStyle(
                        color = GlanceTheme.colors.onSecondaryContainer,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }
        Spacer(modifier = GlanceModifier.height(10.dp))
        Row(modifier = GlanceModifier.fillMaxWidth()) {
            Box(
                modifier = GlanceModifier
                    .defaultWeight()
                    .background(GlanceTheme.colors.surfaceVariant)
                    .cornerRadius(14.dp)
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "$line1 • $line1Eta",
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurfaceVariant,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
            Spacer(modifier = GlanceModifier.width(8.dp))
            Box(
                modifier = GlanceModifier
                    .defaultWeight()
                    .background(GlanceTheme.colors.surfaceVariant)
                    .cornerRadius(14.dp)
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "$line2 • $line2Eta",
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurfaceVariant,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}
