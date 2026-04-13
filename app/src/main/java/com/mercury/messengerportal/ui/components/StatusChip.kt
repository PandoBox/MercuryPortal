package com.mercury.messengerportal.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mercury.messengerportal.domain.model.JobStatus
import com.mercury.messengerportal.ui.theme.*

@Composable
fun StatusChip(
    status: JobStatus,
    modifier: Modifier = Modifier,
    textStyle: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.labelMedium
) {
    val (background, text) = when (status) {
        JobStatus.ASSIGNED -> StatusAssigned.copy(alpha = 0.15f) to StatusAssigned
        JobStatus.DEPARTED -> StatusDeparted.copy(alpha = 0.15f) to StatusDeparted
        JobStatus.ARRIVED -> StatusArrived.copy(alpha = 0.15f) to StatusArrived
        JobStatus.COMPLETED -> StatusCompleted.copy(alpha = 0.15f) to StatusCompleted
        JobStatus.DELAYED -> StatusReassign.copy(alpha = 0.15f) to StatusReassign
    }
    Text(
        text = status.displayName(),
        style = textStyle,
        fontWeight = FontWeight.SemiBold,
        color = text,
        modifier = modifier
            .background(background, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    )
}
