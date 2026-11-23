package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.ReportReason
import com.example.model.ReportTarget
import com.example.ui.theme.LocalCustomButtonColors

/**
 * One report sheet for every kind of content in clips: account, video, post,
 * story and comment. Keeping a single sheet makes reporting predictable.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportBottomSheet(
    target: ReportTarget,
    alreadyReported: Boolean,
    onSwitchToAccount: ((String) -> Unit)? = null,
    onSubmit: (ReportReason, String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedReason by remember(target.targetKey) { mutableStateOf<ReportReason?>(null) }
    var details by remember(target.targetKey) { mutableStateOf("") }
    val accent = LocalCustomButtonColors.current.actionButtonsColor
        ?: MaterialTheme.colorScheme.primary

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Flag,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = "إبلاغ",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, contentDescription = "إغلاق")
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                border = BorderStroke(0.6.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = when (target) {
                            is ReportTarget.Account -> "إبلاغ عن حساب"
                            is ReportTarget.Video -> "إبلاغ عن فيديو"
                            is ReportTarget.Post -> "إبلاغ عن منشور"
                            is ReportTarget.Story -> "إبلاغ عن قصة"
                            is ReportTarget.Comment -> "إبلاغ عن تعليق"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = accent,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = target.titleAr.take(160),
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2
                    )
                }
            }

            if (alreadyReported) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "لقد أرسلت بلاغاً عن هذا المحتوى سابقاً — يمكنك إرسال بلاغ محدّث إذا لزم الأمر.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "ما سبب البلاغ؟",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))

            ReportReason.entries.forEach { reason ->
                val selected = selectedReason == reason
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (selected) accent.copy(alpha = 0.12f)
                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    border = if (selected) BorderStroke(1.dp, accent.copy(alpha = 0.6f)) else null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                        .clickable { selectedReason = reason }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selected,
                            onClick = { selectedReason = reason }
                        )
                        Text(
                            text = reason.labelAr,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (selected) accent else Color.Unspecified
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = details,
                onValueChange = { details = it },
                label = { Text("تفاصيل إضافية (اختياري)") },
                placeholder = { Text("اشرح لنا ما حدث…") },
                minLines = 2,
                maxLines = 4,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )

            val ownerHandle = target.ownerHandle
            if (!ownerHandle.isNullOrBlank() && target !is ReportTarget.Account && onSwitchToAccount != null) {
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = { onSwitchToAccount(ownerHandle) },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("إبلاغ عن الحساب بدلاً من المحتوى")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = {
                    selectedReason?.let { onSubmit(it, details) }
                },
                enabled = selectedReason != null,
                colors = ButtonDefaults.buttonColors(containerColor = accent),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Filled.Flag,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text("إرسال البلاغ")
            }
        }
    }
}
