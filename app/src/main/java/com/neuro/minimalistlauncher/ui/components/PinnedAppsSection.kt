package com.neuro.minimalistlauncher.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neuro.minimalistlauncher.data.model.AppItem
import com.neuro.minimalistlauncher.ui.theme.PrimaryOffWhite
import com.neuro.minimalistlauncher.ui.theme.SecondaryMutedAsh
import com.neuro.minimalistlauncher.ui.util.HapticsHelper

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PinnedAppsSection(
    pinnedApps: List<AppItem>,
    onAppClick: (AppItem) -> Unit,
    onAppLongClick: (AppItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "ESSENTIALS",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.2.sp,
            color = SecondaryMutedAsh,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        if (pinnedApps.isEmpty()) {
            Text(
                text = "Long press any app in drawer to pin here (up to 6)",
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                color = SecondaryMutedAsh,
                modifier = Modifier.padding(vertical = 12.dp)
            )
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                pinnedApps.take(6).forEach { app ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .combinedClickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = {
                                    HapticsHelper.performLightTick(context)
                                    onAppClick(app)
                                },
                                onLongClick = {
                                    HapticsHelper.performLightTick(context)
                                    onAppLongClick(app)
                                }
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = app.label,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Normal,
                            color = PrimaryOffWhite
                        )
                        if (app.isFrictionTagged) {
                            Text(
                                text = " · paused",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Normal,
                                color = SecondaryMutedAsh,
                                modifier = Modifier.padding(start = 6.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
