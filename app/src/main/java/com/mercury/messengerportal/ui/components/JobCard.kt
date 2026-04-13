package com.mercury.messengerportal.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.GetApp
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mercury.messengerportal.domain.model.Job
import com.mercury.messengerportal.domain.model.JobType
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.background
import kotlin.math.absoluteValue

val LocationColors = listOf(
    Color(0xFFE57373), Color(0xFFF06292), Color(0xFFBA68C8), Color(0xFF9575CD),
    Color(0xFF7986CB), Color(0xFF64B5F6), Color(0xFF4FC3F7), Color(0xFF4DD0E1),
    Color(0xFF4DB6AC), Color(0xFF81C784), Color(0xFFAED581), Color(0xFFFF8A65)
)

fun getLocationColor(locationName: String): Color {
    val index = locationName.hashCode().absoluteValue % LocationColors.size
    return LocationColors[index]
}

@Composable
fun getJobTypeIcon(jobType: JobType): ImageVector {
    return when (jobType) {
        JobType.DOCUMENT_DELIVERY -> Icons.Default.Description
        JobType.CHEQUE_CLEARING -> Icons.Default.AttachMoney
        JobType.PARCEL_PICKUP -> Icons.Default.GetApp
        JobType.PARCEL_DELIVERY -> Icons.Default.LocalShipping
        JobType.BANK_DEPOSIT -> Icons.Default.AccountBalance
        JobType.GOVERNMENT_DOCUMENT -> Icons.Default.AccountBalance
        JobType.OTHER -> Icons.Default.MoreHoriz
    }
}

@Composable
fun getJobTypeColor(jobType: JobType): Color {
    return when (jobType) {
        JobType.DOCUMENT_DELIVERY -> Color(0xFF1E88E5)
        JobType.CHEQUE_CLEARING -> Color(0xFF43A047)
        JobType.PARCEL_PICKUP -> Color(0xFFFB8C00)
        JobType.PARCEL_DELIVERY -> Color(0xFFF4511E)
        JobType.BANK_DEPOSIT -> Color(0xFF8E24AA)
        JobType.GOVERNMENT_DOCUMENT -> Color(0xFF3949AB)
        JobType.OTHER -> Color(0xFF757575)
    }
}

@Composable
fun JobCard(
    job: Job,
    modifier: Modifier = Modifier,
    isSharedLocation: Boolean = false,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.pointerInput(Unit) {
            detectTapGestures(onTap = { onClick() })
        },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = getJobTypeIcon(job.type),
                        contentDescription = job.type.displayName(),
                        modifier = Modifier.size(36.dp),
                        tint = getJobTypeColor(job.type)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                "#${job.sequenceOrder}",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                job.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val locColor = if (isSharedLocation) getLocationColor(job.locationName) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            val textColor = if (isSharedLocation) locColor.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = locColor
                            )
                            Spacer(Modifier.width(4.dp))
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (isSharedLocation) locColor.copy(alpha = 0.15f) else Color.Transparent, 
                                        RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = if (isSharedLocation) 4.dp else 0.dp, vertical = if (isSharedLocation) 1.dp else 0.dp)
                            ) {
                                Text(
                                    job.locationName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = textColor,
                                    fontWeight = if (isSharedLocation) FontWeight.SemiBold else FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                job.receiverName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                Spacer(Modifier.width(8.dp))
                StatusChip(status = job.status)
            }
        }
    }
}
