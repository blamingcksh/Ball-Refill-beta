package com.neuro.minimalistlauncher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neuro.minimalistlauncher.data.model.AppItem
import com.neuro.minimalistlauncher.ui.theme.CharcoalElevated
import com.neuro.minimalistlauncher.ui.theme.PrimaryOffWhite
import com.neuro.minimalistlauncher.ui.theme.SecondaryMutedAsh
import com.neuro.minimalistlauncher.ui.theme.SubtleBorder
import com.neuro.minimalistlauncher.ui.util.HapticsHelper
import com.neuro.minimalistlauncher.ui.util.IntentHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppOptionsDialog(
    app: AppItem,
    onDismiss: () -> Unit,
    onTogglePin: (AppItem) -> Unit,
    onToggleFriction: (AppItem) -> Unit
) {
    val context = LocalContext.current

    BasicAlertDialog(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(CharcoalElevated)
                .padding(vertical = 16.dp, horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = app.label,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = PrimaryOffWhite,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            HorizontalDivider(color = SubtleBorder, thickness = 1.dp)

            // Pin / Unpin option
            DialogOptionRow(
                title = if (app.isPinned) "Unpin from Essentials" else "Pin to Essentials",
                subtitle = if (app.isPinned) "Remove from top section" else "Place in top essential apps",
                onClick = {
                    HapticsHelper.performLightTick(context)
                    onTogglePin(app)
                    onDismiss()
                }
            )

            // Friction tag option
            DialogOptionRow(
                title = if (app.isFrictionTagged) "Remove Intentional Friction" else "Enable Intentional Friction",
                subtitle = if (app.isFrictionTagged) "Open instantly without 5s pause" else "Add 5-second breath pause before opening",
                onClick = {
                    HapticsHelper.performLightTick(context)
                    onToggleFriction(app)
                    onDismiss()
                }
            )

            // System App Info
            DialogOptionRow(
                title = "Application Settings",
                subtitle = "Manage permissions, storage, or notifications",
                onClick = {
                    HapticsHelper.performLightTick(context)
                    IntentHelper.openAppSettings(context, app.packageName)
                    onDismiss()
                }
            )

            HorizontalDivider(color = SubtleBorder, thickness = 1.dp)

            // Cancel
            DialogOptionRow(
                title = "Cancel",
                subtitle = null,
                isSecondary = true,
                onClick = {
                    HapticsHelper.performLightTick(context)
                    onDismiss()
                }
            )
        }
    }
}

@Composable
private fun DialogOptionRow(
    title: String,
    subtitle: String?,
    isSecondary: Boolean = false,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 10.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = title,
            fontSize = 15.sp,
            fontWeight = FontWeight.Normal,
            color = if (isSecondary) SecondaryMutedAsh else PrimaryOffWhite
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                color = SecondaryMutedAsh,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}
