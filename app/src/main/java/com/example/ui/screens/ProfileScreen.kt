package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.model.AvatarPreset
import com.example.model.PostItem
import com.example.model.UserProfile
import com.example.model.VideoItem
import com.example.ui.components.ClipsBrandIcon
import com.example.ui.components.HighQualityAvatarView
import com.example.ui.components.MediaSceneArtwork
import com.example.ui.components.formatCompactCount
import com.example.ui.theme.AppThemeAccent
import com.example.ui.theme.ButtonColorTarget
import com.example.ui.theme.ButtonSwatchPalette
import com.example.ui.theme.CustomButtonColors
import com.example.viewmodel.ProfileUpdate
import java.io.File
import java.io.IOException

@Composable
fun ProfileScreen(
    userProfile: UserProfile,
    currentAccent: AppThemeAccent,
    customButtonColors: CustomButtonColors,
    isDarkMode: Boolean,
    myHorizontalVideos: List<VideoItem>,
    myVerticalClips: List<VideoItem>,
    myPosts: List<PostItem>,
    savedVideos: List<VideoItem>,
    getAvatarPreset: (String) -> AvatarPreset,
    onSelectAccent: (AppThemeAccent) -> Unit,
    onSetButtonColor: (ButtonColorTarget, Color?) -> Unit,
    onResetButtonColors: () -> Unit,
    onToggleDarkMode: () -> Unit,
    currentVersionName: String,
    isCheckingUpdate: Boolean,
    onCheckForUpdates: () -> Unit,
    onOpenVideo: (String) -> Unit,
    onJumpToClipsTab: () -> Unit,
    onOpenSuggestions: () -> Unit,
    isGuest: Boolean = false,
    userEmail: String? = null,
    onOpenAuth: () -> Unit = {},
    onSignOut: () -> Unit = {},
    onUpdateProfile: (ProfileUpdate) -> Unit,
    onScrollDelta: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedSectionTab by remember { mutableIntStateOf(0) }
    var showEditSheet by remember { mutableStateOf(false) }
    var showPerButtonCustomizer by remember { mutableStateOf(false) }
    var selectedColorTarget by remember { mutableStateOf(ButtonColorTarget.UPLOAD_BUTTON) }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                onScrollDelta(-available.y)
                return Offset.Zero
            }
        }
    }

    val sectionTitles = listOf("الفيديوهات", "clips", "المنشورات", "المحفوظات")

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .nestedScroll(nestedScrollConnection),
        contentPadding = PaddingValues(bottom = 112.dp)
    ) {
        // 1. Top Wide Banner + Avatar below/overlapping + Large Name on its left + Bio underneath
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_profile_banner),
                        contentDescription = "بانر الحساب",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color.Black.copy(alpha = 0.35f),
                                        Color.Transparent,
                                        MaterialTheme.colorScheme.background.copy(alpha = 0.92f)
                                    )
                                )
                            )
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .windowInsetsPadding(WindowInsets.statusBars)
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = Color.Black.copy(alpha = 0.5f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ClipsBrandIcon(size = 24.dp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "clips",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(50),
                            color = Color.Black.copy(alpha = 0.5f),
                            modifier = Modifier
                                .testTag("edit_profile_button")
                                .clickable { showEditSheet = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Edit,
                                    contentDescription = "تعديل الحساب",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "تعديل الحساب",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                    }
                }

                // In RTL: Avatar on Start (Right), and to its left ("على شمالها") Large Name + Bio underneath!
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .offset(y = (-24).dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.background)
                            .padding(4.dp)
                    ) {
                        HighQualityAvatarView(
                            preset = getAvatarPreset(userProfile.avatarPresetId),
                            size = 94.dp,
                            borderColor = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(top = 26.dp)
                    ) {
                        Text(
                            text = userProfile.name,
                            fontSize = 26.sp,
                            lineHeight = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = userProfile.handle,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = userProfile.bio,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            border = BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .clickable {
                                    if (isGuest) onOpenAuth() else onSignOut()
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.AccountCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = if (isGuest) {
                                        "وضع الضيف — اضغط للتسجيل"
                                    } else {
                                        "حساب: ${userEmail.orEmpty()} • اضغط لتسجيل الخروج"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. Clean Stats Strip
        item {
            HorizontalDivider(
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.22f),
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ProfileStatBlock(value = userProfile.followersCount, label = "متابع")
                ProfileStatBlock(value = userProfile.followingCount, label = "يتابع")
                ProfileStatBlock(value = userProfile.totalLikesCount, label = "إعجاب")
                ProfileStatBlock(
                    value = "${myHorizontalVideos.size + myVerticalClips.size}",
                    label = "فيديو"
                )
            }
            HorizontalDivider(
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.22f),
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(14.dp))
        }

        // 3. Minimal 5-Color Theme Bar + Dark/Light Mode Toggle
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.24f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Palette,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "لون التطبيق",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                                .testTag("profile_dark_mode_switch")
                                .clickable { onToggleDarkMode() }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Icon(
                                imageVector = if (isDarkMode) Icons.Filled.DarkMode else Icons.Filled.LightMode,
                                contentDescription = "تبديل المظهر",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = if (isDarkMode) "داكن" else "فاتح",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        AppThemeAccent.entries.forEach { accent ->
                            val selected = accent == currentAccent
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .testTag("theme_color_${accent.id}")
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { onSelectAccent(accent) }
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.linearGradient(
                                                colors = listOf(accent.swatchColor, accent.secondarySwatch)
                                            )
                                        )
                                        .then(
                                            if (selected) {
                                                Modifier.border(
                                                    width = 2.5.dp,
                                                    color = MaterialTheme.colorScheme.onBackground,
                                                    shape = CircleShape
                                                )
                                            } else Modifier
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (selected) {
                                        Icon(
                                            imageVector = Icons.Filled.Check,
                                            contentDescription = "اللون المختار",
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = accent.labelAr,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Toggle row for customizing each button's individual color
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .testTag("toggle_per_button_colors")
                            .clickable { showPerButtonCustomizer = !showPerButtonCustomizer }
                            .padding(vertical = 6.dp, horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "تخصيص لون كل زر",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "حدّد لوناً مستقلاً للشريط وعلامة الرفع والأزرار",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (showPerButtonCustomizer || customButtonColors.hasAnyCustomColor()) {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            }
                        ) {
                            Text(
                                text = if (showPerButtonCustomizer) "إخفاء" else "تخصيص",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (showPerButtonCustomizer || customButtonColors.hasAnyCustomColor()) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }

                    if (showPerButtonCustomizer) {
                        Spacer(modifier = Modifier.height(10.dp))
                        // 1. Target button selector chips
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(ButtonColorTarget.entries) { target ->
                                val isTargetSelected = target == selectedColorTarget
                                val resolvedDotColor = customButtonColors.colorFor(target)
                                    ?: if (target == ButtonColorTarget.CLIPS_TAB || target == ButtonColorTarget.PROFILE_TAB) {
                                        MaterialTheme.colorScheme.secondary
                                    } else {
                                        MaterialTheme.colorScheme.primary
                                    }
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = if (isTargetSelected) {
                                        resolvedDotColor.copy(alpha = 0.18f)
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
                                    },
                                    border = BorderStroke(
                                        width = if (isTargetSelected) 1.25.dp else 0.5.dp,
                                        color = if (isTargetSelected) resolvedDotColor
                                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.28f)
                                    ),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .testTag("button_target_${target.id}")
                                        .clickable { selectedColorTarget = target }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(resolvedDotColor)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = target.labelAr,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = if (isTargetSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isTargetSelected) MaterialTheme.colorScheme.onSurface
                                            else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // 2. Color swatches for selected button target + Auto + Reset all
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            item {
                                val isAuto = customButtonColors.colorFor(selectedColorTarget) == null
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = if (isAuto) {
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant
                                    },
                                    border = if (isAuto) {
                                        BorderStroke(1.25.dp, MaterialTheme.colorScheme.primary)
                                    } else null,
                                    modifier = Modifier
                                        .height(36.dp)
                                        .clip(RoundedCornerShape(50))
                                        .clickable { onSetButtonColor(selectedColorTarget, null) }
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.padding(horizontal = 12.dp)
                                    ) {
                                        Text(
                                            text = "افتراضي",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isAuto) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                            items(ButtonSwatchPalette) { (colorLabel, swatchColor) ->
                                val isPicked = customButtonColors.colorFor(selectedColorTarget) == swatchColor
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(swatchColor)
                                        .then(
                                            if (isPicked) {
                                                Modifier.border(
                                                    width = 2.5.dp,
                                                    color = MaterialTheme.colorScheme.onBackground,
                                                    shape = CircleShape
                                                )
                                            } else Modifier
                                        )
                                        .testTag("button_swatch_${colorLabel}")
                                        .clickable { onSetButtonColor(selectedColorTarget, swatchColor) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isPicked) {
                                        Icon(
                                            imageVector = Icons.Filled.Check,
                                            contentDescription = colorLabel,
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }

                        if (customButtonColors.hasAnyCustomColor()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Text(
                                    text = "إعادة تعيين كل الأزرار",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { onResetButtonColors() }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // 3b. One-tap check for the newest published version
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.24f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_check_updates_button")
                        .clickable(enabled = !isCheckingUpdate) { onCheckForUpdates() }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.SystemUpdate,
                        contentDescription = "التحقق من التحديثات",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isCheckingUpdate) "جاري التحقق من أحدث نسخة..." else "التحقق من التحديثات",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "الإصدار الحالي $currentVersionName",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (isCheckingUpdate) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // 3c. Feature suggestions board: add ideas, like and comment publicly
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.24f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_suggestions_button")
                        .clickable { onOpenSuggestions() }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.Lightbulb,
                                contentDescription = "اقتراحات التطوير",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "اقتراحات التطوير",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "شاركنا فكرتك، وصوّت على أفكار الآخرين",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // 4. Professional Uploaded Videos & Content Tabs
        item {
            TabRow(
                selectedTabIndex = selectedSectionTab,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.primary,
                indicator = { tabPositions ->
                    if (selectedSectionTab < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedSectionTab]),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            ) {
                sectionTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedSectionTab == index,
                        onClick = { selectedSectionTab = index },
                        text = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = if (selectedSectionTab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        modifier = Modifier.testTag("profile_tab_$index")
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        when (selectedSectionTab) {
            0 -> {
                if (myHorizontalVideos.isEmpty()) {
                    item { EmptyProfileSection("لم ترفع فيديوهات أفقية بعد") }
                } else {
                    items(myHorizontalVideos, key = { "mh_${it.id}" }) { video ->
                        ProfileVideoRowCard(
                            video = video,
                            onClick = { onOpenVideo(video.id) }
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }
            1 -> {
                if (myVerticalClips.isEmpty()) {
                    item { EmptyProfileSection("لم ترفع مقاطع clips بعد") }
                } else {
                    val rows = myVerticalClips.chunked(3)
                    items(rows) { rowClips ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowClips.forEach { clip ->
                                ProfileVerticalClipTile(
                                    clip = clip,
                                    onClick = onJumpToClipsTab,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            repeat(3 - rowClips.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
            2 -> {
                if (myPosts.isEmpty()) {
                    item { EmptyProfileSection("لا توجد منشورات بعد") }
                } else {
                    items(myPosts, key = { "mp_${it.id}" }) { post ->
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 5.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = post.timeAgo,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = post.content,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
            3 -> {
                if (savedVideos.isEmpty()) {
                    item { EmptyProfileSection("قائمة المحفوظات فارغة حالياً") }
                } else {
                    items(savedVideos, key = { "sv_${it.id}" }) { video ->
                        ProfileVideoRowCard(
                            video = video,
                            onClick = { onOpenVideo(video.id) }
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }
        }
    }

    if (showEditSheet) {
        EditProfileSheet(
            profile = userProfile,
            onDismiss = { showEditSheet = false },
            onSave = { update ->
                onUpdateProfile(update)
                showEditSheet = false
            }
        )
    }
}

@Composable
private fun ProfileStatBlock(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ProfileVideoRowCard(
    video: VideoItem,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(10.dp))
            ) {
                MediaSceneArtwork(
                    visualStyle = video.visualStyle,
                    mediaUriString = video.mediaUriString,
                    mediaInfo = video.mediaInfo,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.56f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.Black.copy(alpha = 0.78f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = video.duration,
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                val isDark = MaterialTheme.colorScheme.background.red < 0.25f
                Text(
                    text = video.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Visibility,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${formatCompactCount(video.viewsCount)} • ${video.timeAgo}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (video.isSaved) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Bookmark,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "محفوظ",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileVerticalClipTile(
    clip: VideoItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(9f / 15f)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
    ) {
        MediaSceneArtwork(
            visualStyle = clip.visualStyle,
            mediaUriString = clip.mediaUriString,
            mediaInfo = clip.mediaInfo,
            fillScreen = true,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.72f))
                    )
                )
        )
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = formatCompactCount(clip.viewsCount),
                color = Color.White,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun EmptyProfileSection(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** Copy the picked photo into app storage so it survives restarts and URI permission loss. */
private fun savePickedAvatar(context: android.content.Context, source: Uri): String? = runCatching {
    context.contentResolver.openInputStream(source)?.use { input ->
        val original = BitmapFactory.decodeStream(input) ?: return@use null
        val maxSide = 1080
        val scale = minOf(1f, maxSide.toFloat() / maxOf(original.width, original.height))
        val bitmap = if (scale < 1f) {
            Bitmap.createScaledBitmap(
                original,
                maxOf(1, (original.width * scale).toInt()),
                maxOf(1, (original.height * scale).toInt()),
                true
            )
        } else original
        val file = File(context.filesDir, "profile_avatar.jpg")
        java.io.FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 88, out)
        }
        Uri.fromFile(file).toString()
    }
}.getOrNull()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditProfileSheet(
    profile: UserProfile,
    onDismiss: () -> Unit,
    onSave: (ProfileUpdate) -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var name by remember { mutableStateOf(profile.name) }
    var bio by remember { mutableStateOf(profile.bio) }
    var photoUri by remember { mutableStateOf(profile.avatarImageUri) }
    var zoom by remember { mutableStateOf(profile.avatarImageZoom.coerceIn(1f, 2.5f)) }
    var offsetX by remember { mutableStateOf(profile.avatarImageOffsetX.coerceIn(-0.3f, 0.3f)) }
    var offsetY by remember { mutableStateOf(profile.avatarImageOffsetY.coerceIn(-0.3f, 0.3f)) }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { picked ->
        if (picked != null) {
            savePickedAvatar(context, picked)?.let { localUri ->
                photoUri = localUri
                zoom = 1f
                offsetX = 0f
                offsetY = 0f
            }
        }
    }

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
                Text(
                    text = "تعديل الملف الشخصي",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, contentDescription = "إغلاق")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "صورة الحساب",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "ارفع صورة حقيقية من جهازك واختر القص المناسب لها — بدون أفاتار جاهز.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .size(116.dp)
                    .align(Alignment.CenterHorizontally)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(2.5.dp, MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (photoUri != null) {
                    AsyncImage(
                        model = photoUri,
                        contentDescription = "صورة حسابك",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                scaleX = zoom
                                scaleY = zoom
                                translationX = size.width * offsetX
                                translationY = size.height * offsetY
                            }
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(46.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            OutlinedButton(
                onClick = {
                    photoPicker.launch(
                        PickVisualMediaRequest(
                            ActivityResultContracts.PickVisualMedia.ImageOnly
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Filled.AddAPhoto, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (photoUri == null) "رفع صورة من الجهاز" else "تغيير الصورة")
            }
            if (photoUri != null) {
                Text(
                    text = "إزالة الصورة والعودة للشكل الافتراضي",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            photoUri = null
                            zoom = 1f
                            offsetX = 0f
                            offsetY = 0f
                        }
                        .padding(vertical = 8.dp, horizontal = 4.dp)
                )

                Spacer(modifier = Modifier.height(4.dp))
                Text("تكبير الصورة", style = MaterialTheme.typography.labelSmall)
                Slider(value = zoom, onValueChange = { zoom = it }, valueRange = 1f..2.5f)
                Text("تحريك أفقي", style = MaterialTheme.typography.labelSmall)
                Slider(value = offsetX, onValueChange = { offsetX = it }, valueRange = -0.3f..0.3f)
                Text("تحريك عمودي", style = MaterialTheme.typography.labelSmall)
                Slider(value = offsetY, onValueChange = { offsetY = it }, valueRange = -0.3f..0.3f)
            }

            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("الاسم") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = bio,
                onValueChange = { bio = it },
                label = { Text("الوصف الشخصي") },
                minLines = 2,
                maxLines = 4,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("إلغاء")
                }
                Button(
                    onClick = {
                        onSave(
                            ProfileUpdate(
                                name = name,
                                bio = bio,
                                avatarImageUri = photoUri,
                                avatarImageZoom = zoom,
                                avatarImageOffsetX = offsetX,
                                avatarImageOffsetY = offsetY
                            )
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("حفظ التعديلات")
                }
            }
        }
    }
}
