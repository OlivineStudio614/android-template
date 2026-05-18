package com.olivinestudio614.drillnav.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.olivinestudio614.drillnav.ui.theme.AmberAlert
import com.olivinestudio614.drillnav.ui.theme.ArmyGreenDark
import com.olivinestudio614.drillnav.ui.theme.OliveDrab
import com.olivinestudio614.drillnav.ui.theme.OffWhite

@Composable
fun ArrivalScreen(
    distanceRemaining: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(OliveDrab),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "OBJECTIVE\nREACHED",
                style = MaterialTheme.typography.displayLarge.copy(
                    color = AmberAlert,
                    textAlign = TextAlign.Center,
                    lineHeight = 56.sp
                )
            )
            Text(
                text = "MISSION COMPLETE",
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = OffWhite,
                    textAlign = TextAlign.Center
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ArmyGreenDark,
                    contentColor = AmberAlert
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = "AT EASE",
                    style = MaterialTheme.typography.headlineMedium
                )
            }
        }
    }
}
