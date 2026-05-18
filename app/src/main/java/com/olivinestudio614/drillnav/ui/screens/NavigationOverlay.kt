package com.olivinestudio614.drillnav.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.olivinestudio614.drillnav.ui.theme.AmberAlert
import com.olivinestudio614.drillnav.ui.theme.ArmyGreenDark
import com.olivinestudio614.drillnav.ui.theme.DangerRed
import com.olivinestudio614.drillnav.ui.theme.MapOverlayBg
import com.olivinestudio614.drillnav.ui.theme.OffWhite

@Composable
fun TurnInstructionCard(
    instruction: String,
    distanceRemaining: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MapOverlayBg)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = instruction.ifBlank { "AWAITING ORDERS" },
            style = MaterialTheme.typography.bodyLarge.copy(color = OffWhite),
            modifier = Modifier.weight(1f)
        )
        Text(
            text = distanceRemaining,
            style = MaterialTheme.typography.titleLarge.copy(color = AmberAlert)
        )
    }
}

@Composable
fun SpeedStrip(
    currentSpeedMph: Float,
    speedLimitMph: Float,
    modifier: Modifier = Modifier
) {
    val isOverLimit = speedLimitMph > 0f && currentSpeedMph > speedLimitMph + 2f
    val bgColor = if (isOverLimit) DangerRed else ArmyGreenDark

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(bgColor)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "SPEED: ${currentSpeedMph.toInt()} MPH",
            style = MaterialTheme.typography.labelMedium.copy(color = OffWhite)
        )
        if (speedLimitMph > 0f) {
            Text(
                text = "LIMIT: ${speedLimitMph.toInt()} MPH",
                style = MaterialTheme.typography.labelMedium.copy(
                    color = if (isOverLimit) OffWhite else AmberAlert
                )
            )
        }
    }
}
