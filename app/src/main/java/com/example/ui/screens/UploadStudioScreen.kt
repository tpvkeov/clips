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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudUpload
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
import com.example.data.DemoDataFactory
import com.example.model.AvatarPreset
import com.example.model.MediaVisualStyle
import com.example.model.UploadContentType
import com.example.ui.camera.CameraAvatarVideoStudioDialog
import com.example.ui.components.ClipsBrandIcon
import com.example.ui.components.MediaSceneArtwork

@Composable
fun UploadStudioScreen(
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

    var selectedType by remember { mutableStateOf(UploadContentType.HORIZONTAL_VIDEO) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var capturedOrPickedUri by remember { mutableStateOf<String?>(null) }
    var selectedAvatarOverlay by remember { mutableStateOf<AvatarPreset?>(null) }
    var showCameraDialog by remember { mutableStateOf(false) }

    val videoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            capturedOrPickedUri = uri.toString()
            selectedType = detectVideoContentType(context, uri)
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
                    text = "رفع فيديو",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "اختر فيديو أو صوّر فيديو بالكاميرا مع الأفاتار",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Focused Video Picker + Camera Button Area
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            border = BorderStroke(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    listOf(
                        Color(0xFFFF7A1A).copy(alpha = 0.45f),
                        Color(0xFF10B981).copy(alpha = 0.45f)
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
                if (capturedOrPickedUri != null || selectedAvatarOverlay != null) {
                    val previewModifier = if (selectedType == UploadContentType.VERTICAL_CLIP) {
                        Modifier
                            .width(176.dp)
                            .height(260.dp)
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
                            visualStyle = if (capturedOrPickedUri != null) {
                                MediaVisualStyle.CUSTOM_URI
                            } else if (selectedType == UploadContentType.VERTICAL_CLIP) {
                                MediaVisualStyle.CYBER_CITY
                            } else {
                                MediaVisualStyle.STUDIO_PHOTO
                            },
                            mediaUriString = capturedOrPickedUri,
                            avatarOverlay = selectedAvatarOverlay,
                            isPlaying = capturedOrPickedUri != null,
                            modifier = Modifier.fillMaxSize()
                        )
                        IconButton(
                            onClick = {
                                capturedOrPickedUri = null
                                selectedAvatarOverlay = null
                            },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(6.dp)
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.65f))
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "إزالة الفيديو",
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
                                videoPicker.launch(
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
                                            listOf(Color(0xFFFF7A1A), Color(0xFF10B981))
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
                                text = "الفيديو العمودي ينشر تلقائياً في clips فقط",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF10B981)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Two clean actions: Pick Video from device + Dedicated Video Camera with Avatars button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            videoPicker.launch(
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
                        Text(if (capturedOrPickedUri == null) "اختيار فيديو" else "تغيير")
                    }

                    Button(
                        onClick = { showCameraDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("open_camera_studio_button")
                    ) {
                        Icon(Icons.Filled.Videocam, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("تصوير فيديو", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Clean Upload Form: Video Orientation (Horizontal -> Home, Vertical -> clips only) + Title + Description
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
                    text = "اتجاه الفيديو ووجهة النشر",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Only Horizontal (Home) and Vertical (clips only) — Posts are exclusively in Posts tab!
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val videoTypes = listOf(
                        UploadContentType.HORIZONTAL_VIDEO to Icons.Filled.StayCurrentLandscape,
                        UploadContentType.VERTICAL_CLIP to Icons.Filled.StayCurrentPortrait
                    )
                    videoTypes.forEach { (type, icon) ->
                        val active = selectedType == type
                        val label = if (type == UploadContentType.HORIZONTAL_VIDEO) "فيديو أفقي" else "فيديو عمودي (clips)"
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (active) Color(0xFFFF7A1A) else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .testTag("upload_type_${type.name}")
                                .clickable { selectedType = type }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = if (active) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (active) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (selectedType == UploadContentType.VERTICAL_CLIP) {
                        "سيتم نشر الفيديو العمودي في قسم clips فقط"
                    } else {
                        "سيتم نشر الفيديو الأفقي في الصفحة الرئيسية"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF10B981),
                    fontWeight = FontWeight.SemiBold
                )

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
                            selectedAvatarOverlay?.id
                        )
                        title = ""
                        description = ""
                        capturedOrPickedUri = null
                        selectedAvatarOverlay = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF7A1A)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("upload_publish_button")
                ) {
                    Icon(Icons.Filled.CloudUpload, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (selectedType == UploadContentType.VERTICAL_CLIP) {
                            "نشر الفيديو في clips"
                        } else {
                            "نشر في الصفحة الرئيسية"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }

    // Dedicated Full-Screen Video Recording Camera + Open-Source High-Quality Avatars Dialog
    if (showCameraDialog) {
        CameraAvatarVideoStudioDialog(
            initialAvatar = selectedAvatarOverlay ?: DemoDataFactory.avatarPresets.first(),
            onVideoRecorded = { uri, avatar ->
                capturedOrPickedUri = uri
                selectedAvatarOverlay = avatar
                selectedType = if (uri != null) {
                    detectVideoContentType(context, Uri.parse(uri))
                } else {
                    UploadContentType.VERTICAL_CLIP
                }
                showCameraDialog = false
            },
            onDismiss = { showCameraDialog = false }
        )
    }
}

private fun detectVideoContentType(context: Context, uri: Uri): UploadContentType {
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
