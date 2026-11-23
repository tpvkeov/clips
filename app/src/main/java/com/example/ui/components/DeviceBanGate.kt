package com.example.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.model.AuthUiState
import com.example.ui.theme.LocalCustomButtonColors

/** Blocks the whole app when the current device fingerprint has been banned. */
@Composable
fun DeviceBanGate(
    auth: AuthUiState,
    onRecheck: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(enabled = true) { /* banned devices cannot navigate away */ }

    val accent = LocalCustomButtonColors.current.actionButtonsColor
        ?: MaterialTheme.colorScheme.primary
    val pulse = rememberInfiniteTransition(label = "ban_pulse")
    val scale by pulse.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1_400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ban_icon_scale"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.errorContainer,
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .statusBarsPadding()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .scale(scale)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onError,
                        modifier = Modifier.size(46.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "تم حظر هذا الجهاز",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "تحذير أمني: تم ربط الحساب المحظور بمعرّف الجهاز وعنوان الشبكة وإصدار النواة والنطاق الأساسي. هذا الجهاز محظور نهائيًا؛ أي محاولة لإنشاء حساب جديد عليه ستبقى محظورة ما دام الحظر مسجّلًا على الجهاز أو الخادم. استخدم جهازًا آخر أو تواصل مع الإدارة.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(20.dp))
            Surface(
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = auth.device?.displaySummaryAr ?: "معرّف الجهاز غير متاح",
                        style = MaterialTheme.typography.labelSmall,
                        color = accent,
                        textAlign = TextAlign.Center
                    )
                    auth.device?.let { device ->
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = device.detailedSummaryAr,
                            style = MaterialTheme.typography.labelSmall,
                            color = accent,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "البصمة: ${device.fingerprint}\nبصمة النظام: ${device.extendedFingerprint}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onRecheck, shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp)) {
                Text("إعادة التحقق من حالة الجهاز")
            }
        }
    }
}
