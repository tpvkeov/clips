package com.example.ui.screens

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StayCurrentLandscape
import androidx.compose.material.icons.filled.StayCurrentPortrait
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.MediaVisualStyle
import com.example.model.UploadContentType
import com.example.model.UserProfile
import com.example.ui.camera.ProCameraStudioDialog
import com.example.ui.components.ClipsBrandIcon
import com.example.ui.components.MediaSceneArtwork
import com.example.ui.theme.LocalCustomButtonColors

@Composable
fun UploadStudioScreen(
    userProfile: UserProfile,
    onPublish: (
        contentType: UploadContentType,
        title: String,
        description: String,
        category: String,
        visualStyle: MediaVisualStyle,
        mediaUriString: String?,
        avatarOverlayId: String?
    ) -> Unit,
    onScrollDelta: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val customButtonColors = LocalCustomButtonColors.current
    val primaryAccent = customButtonColors.actionButtonsColor ?: MaterialTheme.colorScheme.primary
    val uploadButtonAccent = customButtonColors.uploadButtonColor ?: primaryAccent
    val secondaryAccent = MaterialTheme.colorScheme.secondary

    var selectedType by remember { mutableStateOf(UploadContentType.HORIZONTAL_VIDEO) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var capturedOrPickedUri by remember { mutableStateOf<String?>(null) }
    var showCameraDialog by remember { mutableStateOf(false) }

    val mediaPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            capturedOrPickedUri = uri.toString()
            selectedType = detectMediaContentType(context, uri)
        }
    }

    val scrollState = rememberScrollState()
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                onScrollDelta(-available.y)
                return Offset.Zero
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.statusBars)
            .nestedScroll(nestedScrollConnection)
            .verticalScroll(scrollState)
            .padding(bottom = 112.dp)
    ) {
        // Minimal clean header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ClipsBrandIcon(size = 36.dp)
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "استوديو رفع الفيديوهات",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "اختر فيديو أو صوّر بالكاميرا الاحترافية",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Isolated Points Badge Card on Upload Studio screen
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = primaryAccent.copy(alpha = 0.14f),
            border = BorderStroke(1.dp, primaryAccent.copy(alpha = 0.40f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .testTag("upload_studio_points_badge")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(primaryAccent),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "رصيد نقاط التفاعل",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${userProfile.points} نقطة مساهمة",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = primaryAccent
                        )
                    }
                }
                Surface(
                    shape = RoundedCornerShape(50),
                    color = secondaryAccent.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "+2 لكل مشاركة 🚀",
                        style = MaterialTheme.typography.labelSmall,
                        color = secondaryAccent,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Focused Media Picker + Pro Camera Button Area
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            border = BorderStroke(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    listOf(
                        primaryAccent.copy(alpha = 0.45f),
                        secondaryAccent.copy(alpha = 0.45f)
                    )
                )
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .animateContentSize()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (capturedOrPickedUri != null) {
                    val previewModifier = if (selectedType == UploadContentType.VERTICAL_CLIP) {
                        Modifier
                            .width(176.dp)
                            .aspectRatio(9f / 16f)
                    } else {
                        Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f)
                    }

                    Box(
                        modifier = previewModifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF0E131B))
                    ) {
                        MediaSceneArtwork(
                            visualStyle = MediaVisualStyle.CUSTOM_URI,
                            mediaUriString = capturedOrPickedUri,
                            isPlaying = true,
                            modifier = Modifier.fillMaxSize()
                        )
                        IconButton(
                            onClick = { capturedOrPickedUri = null },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(6.dp)
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.65f))
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "إزالة الملف",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(142.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.48f))
                            .clickable {
                                mediaPicker.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(primaryAccent, secondaryAccent)
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.VideoLibrary,
                                    contentDescription = "اختيار فيديو",
                                    tint = Color.White,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "اضغط لاختيار فيديو من الهاتف",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "يتم التعرف تلقائياً على المقاس (أفقي للرئيسية أو طولي لـ clips)",
                                style = MaterialTheme.typography.labelSmall,
                                color = secondaryAccent
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Pick Media from device + Pro Camera button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            mediaPicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                            )
                        },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("upload_pick_media_button")
                    ) {
                        Icon(Icons.Filled.VideoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (capturedOrPickedUri == null) "اختيار فيديو" else "تغيير الفيديو")
                    }

                    Button(
                        onClick = { showCameraDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = secondaryAccent),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("open_camera_studio_button")
                    ) {
                        Icon(Icons.Filled.Videocam, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("الكاميرا الاحترافية", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Clean Upload Form: Content Type + Title + Description
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "توجيه المحتوى التلقائي",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (capturedOrPickedUri != null) primaryAccent.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                    border = BorderStroke(
                        1.dp,
                        if (capturedOrPickedUri != null) primaryAccent.copy(alpha = 0.45f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.20f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("upload_auto_detected_type_banner")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(
                                    if (capturedOrPickedUri != null) primaryAccent else MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (selectedType == UploadContentType.VERTICAL_CLIP)
                                    Icons.Filled.StayCurrentPortrait
                                else
                                    Icons.Filled.StayCurrentLandscape,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = if (capturedOrPickedUri != null) {
                                        if (selectedType == UploadContentType.VERTICAL_CLIP)
                                            "النوع المحدد تلقائياً: مقطع طولي (clips)"
                                        else
                                            "النوع المحدد تلقائياً: فيديو أفقي عريض"
                                    } else {
                                        "التعرف التلقائي على أبعاد الفيديو"
                                    },
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (capturedOrPickedUri != null) primaryAccent else MaterialTheme.colorScheme.onSurface
                                )
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = secondaryAccent.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "تلقائي ✨",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = secondaryAccent,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(2.dp))

                            Text(
                                text = if (capturedOrPickedUri != null) {
                                    if (selectedType == UploadContentType.VERTICAL_CLIP)
                                        "سيتم النشر مباشرة في تبويب مقاطع clips"
                                    else
                                        "سيتم النشر مباشرة في الصفحة الرئيسية والمنشورات"
                                } else {
                                    "يتم فحص أبعاد الفيديو فور اختياره وتوجيهه للرئيسية أو clips تلقائياً"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("عنوان الفيديو") },
                    placeholder = { Text("اكتب عنواناً جذاباً للفيديو…") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("upload_title_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("وصف الفيديو") },
                    placeholder = { Text("أضف وصفاً يوضح محتوى الفيديو…") },
                    minLines = 3,
                    maxLines = 5,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("upload_description_input")
                )

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        val visualStyle = if (selectedType == UploadContentType.VERTICAL_CLIP) {
                            MediaVisualStyle.CYBER_CITY
                        } else {
                            MediaVisualStyle.STUDIO_PHOTO
                        }
                        onPublish(
                            selectedType,
                            title,
                            description,
                            if (selectedType == UploadContentType.VERTICAL_CLIP) "clips" else "عام",
                            visualStyle,
                            capturedOrPickedUri,
                            null
                        )
                        title = ""
                        description = ""
                        capturedOrPickedUri = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = uploadButtonAccent),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("upload_publish_button")
                ) {
                    Icon(Icons.Filled.CloudUpload, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (selectedType == UploadContentType.VERTICAL_CLIP)
                            "نشر الفيديو في clips"
                        else
                            "نشر في الصفحة الرئيسية",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }

    if (showCameraDialog) {
        ProCameraStudioDialog(
            onMediaCaptured = { uri, _ ->
                capturedOrPickedUri = uri
                if (uri != null) {
                    selectedType = detectMediaContentType(context, Uri.parse(uri))
                }
                showCameraDialog = false
            },
            onDismiss = { showCameraDialog = false }
        )
    }
}

private fun detectMediaContentType(context: Context, uri: Uri): UploadContentType {
    return runCatching {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(context, uri)
        val width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toIntOrNull() ?: 0
        val height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toIntOrNull() ?: 0
        val rotation = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)?.toIntOrNull() ?: 0
        retriever.release()
        val effectiveW = if (rotation == 90 || rotation == 270) height else width
        val effectiveH = if (rotation == 90 || rotation == 270) width else height
        if (effectiveH > effectiveW && effectiveH > 0) {
            UploadContentType.VERTICAL_CLIP
        } else {
            UploadContentType.HORIZONTAL_VIDEO
        }
    }.getOrDefault(UploadContentType.HORIZONTAL_VIDEO)
}
