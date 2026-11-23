package com.example.ui.components

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.example.update.AppUpdateInfo
import kotlinx.coroutines.delay
import java.io.File

private const val APK_FILE_NAME = "clips-update.apk"

/** Shows when a newer public APK manifest is found, then downloads and installs it. */
@Composable
fun UpdateAvailableDialog(
    info: AppUpdateInfo,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val downloadManager = remember {
        context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    }
    var downloadId by remember { mutableStateOf<Long?>(null) }
    var downloadProgress by remember { mutableStateOf(0) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var waitingForPermission by remember { mutableStateOf(false) }

    fun downloadedFile(): File =
        File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), APK_FILE_NAME)

    fun installApk() {
        val file = downloadedFile()
        if (!file.exists()) {
            errorMessage = "ملف التحديث غير موجود، حاول مرة تانية"
            return
        }
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true)
        }
        runCatching { context.startActivity(intent) }
            .onFailure { errorMessage = "تعذر فتح شاشة التثبيت" }
    }

    fun startDownload() {
        errorMessage = null
        waitingForPermission = false
        val request = DownloadManager.Request(Uri.parse(info.apkUrl))
            .setTitle("تحديث تطبيق Clips")
            .setDescription(info.versionName)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalFilesDir(
                context,
                Environment.DIRECTORY_DOWNLOADS,
                APK_FILE_NAME
            )
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)
        runCatching { downloadManager.enqueue(request) }
            .onSuccess { downloadId = it }
            .onFailure { errorMessage = "تعذر بدء التحميل، تأكد من الإنترنت" }
    }

    fun beginUpdate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            !context.packageManager.canRequestPackageInstalls()
        ) {
            waitingForPermission = true
            runCatching {
                context.startActivity(
                    Intent(
                        Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                        Uri.parse("package:${context.packageName}")
                    ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            }
            return
        }
        startDownload()
    }

    LaunchedEffect(downloadId) {
        val id = downloadId ?: return@LaunchedEffect
        while (true) {
            val (status, progress) = runCatching {
                val cursor = downloadManager.query(DownloadManager.Query().setFilterById(id))
                cursor?.use {
                    if (it.moveToFirst()) {
                        val state = it.getInt(
                            it.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS)
                        )
                        val soFar = it.getLong(
                            it.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                        )
                        val total = it.getLong(
                            it.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
                        )
                        val percent = if (total > 0) (soFar * 100 / total).toInt() else 0
                        state to percent.coerceIn(0, 100)
                    } else {
                        -1 to 0
                    }
                } ?: (-1 to 0)
            }.getOrDefault(-1 to 0)

            downloadProgress = progress
            when (status) {
                DownloadManager.STATUS_SUCCESSFUL -> {
                    installApk()
                    break
                }
                DownloadManager.STATUS_FAILED -> {
                    errorMessage = "فشل تحميل التحديث، حاول مرة تانية"
                    break
                }
            }
            delay(600L)
        }
    }

    AlertDialog(
        onDismissRequest = { if (downloadId == null) onDismiss() },
        title = {
            Text(
                text = "يتوفر تحديث جديد ✨",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = "الإصدار: ${info.versionName}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = info.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (waitingForPermission) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "سمح التطبيق بتثبيت APK من مصادر غير متجر، وبعدين اضغط «متابعة».",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                if (downloadId != null) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "جاري التحميل... $downloadProgress%",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { downloadProgress / 100f },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = errorMessage.orEmpty(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            when {
                downloadId != null && errorMessage != null -> {
                    Button(onClick = { startDownload() }) { Text("إعادة المحاولة") }
                }
                downloadId != null -> {
                    TextButton(onClick = {}) { Text("جاري التحميل...") }
                }
                waitingForPermission -> {
                    Button(onClick = { startDownload() }) { Text("متابعة") }
                }
                else -> {
                    Button(onClick = { beginUpdate() }) { Text("تحديث الآن") }
                }
            }
        },
        dismissButton = {
            if (downloadId == null) {
                TextButton(onClick = onDismiss) { Text("لاحقاً") }
            }
        }
    )
}
