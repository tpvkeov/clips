package com.example

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.example.data.ReelsFeedSelector
import com.example.model.ClipsLayoutMode
import com.example.model.MainTab
import com.example.model.MediaVisualStyle
import com.example.model.VideoItem
import com.example.model.VideoOrientation
import com.example.update.AppUpdateChecker
import com.example.update.UpdateCheckResult
import com.example.viewmodel.ClipsViewModel
import com.example.ui.theme.AppThemeAccent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

  /** Local fixture only — never seeded into the real app. */
  private fun testVideo(id: String, vertical: Boolean): VideoItem = VideoItem(
    id = id,
    title = "فيديو اختبار",
    description = "",
    creatorName = "مستخدم اختبار",
    creatorHandle = "@test_user",
    creatorAvatarId = "av_creator",
    duration = "00:10",
    viewsCount = 12,
    likesCount = 3,
    sharesCount = 0,
    orientation = if (vertical) VideoOrientation.VERTICAL else VideoOrientation.HORIZONTAL,
    category = "عام",
    timeAgo = "الآن",
    visualStyle = MediaVisualStyle.STUDIO_PHOTO,
    mediaUriString = "https://example.com/$id.mp4"
  )

  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testInitialStateHasNoFakeAccountsOrNumbers() {
    val state = ClipsViewModel().uiState.value

    assertTrue(state.horizontalVideos.isEmpty())
    assertTrue(state.verticalClips.isEmpty())
    assertTrue(state.posts.isEmpty())
    assertTrue(state.stories.isEmpty())
    assertTrue(state.suggestions.isEmpty())
    assertEquals("", state.userProfile.name)
    assertEquals("", state.userProfile.handle)
    assertEquals("", state.userProfile.bio)
    assertEquals("0", state.userProfile.followersCount)
    assertEquals("0", state.userProfile.followingCount)
    assertEquals("0", state.userProfile.totalLikesCount)
  }

  @Test
  fun testLoginAccentSelectionIsExposedInAuthState() {
    val viewModel = ClipsViewModel()
    val argb = Color(0xFF3B82F6).toArgb()

    viewModel.selectAuthAccent(argb)

    assertEquals(argb, viewModel.uiState.value.auth.selectedAccentArgb)
  }

  @Test
  fun testLoginSwatchesMapToFullAppThemes() {
    assertEquals(
      AppThemeAccent.EMERALD_GREEN,
      AppThemeAccent.fromArgb(Color(0xFF10B981).toArgb())
    )
    assertEquals(
      AppThemeAccent.ROYAL_BLUE,
      AppThemeAccent.fromArgb(Color(0xFF3B82F6).toArgb())
    )
    assertEquals(
      AppThemeAccent.CRIMSON,
      AppThemeAccent.fromArgb(Color(0xFFFF3B5C).toArgb())
    )
    assertEquals(
      AppThemeAccent.ORANGE_GREEN,
      AppThemeAccent.fromArgb(Color(0xFFFFB300).toArgb())
    )
  }

  @Test
  fun testSharingDoesNotChangeProfile() {
    val viewModel = ClipsViewModel()
    val before = viewModel.uiState.value.userProfile

    viewModel.shareVideo("missing_video")
    viewModel.sharePost("missing_post")

    assertEquals(before, viewModel.uiState.value.userProfile)
    assertTrue(viewModel.uiState.value.shareFeedbackEvent?.id?.let { it > 0L } == true)
    viewModel.clearShareFeedback()
    assertNull(viewModel.uiState.value.shareFeedbackEvent)
  }

  @Test
  fun testClipsLayoutModeCyclesBetweenTwoModes() {
    val viewModel = ClipsViewModel()
    assertEquals(ClipsLayoutMode.PRIMARY, viewModel.uiState.value.clipsLayoutMode)

    viewModel.toggleClipsLayoutMode()
    assertEquals(ClipsLayoutMode.CINEMA_PLAYER, viewModel.uiState.value.clipsLayoutMode)

    viewModel.toggleClipsLayoutMode()
    assertEquals(ClipsLayoutMode.PRIMARY, viewModel.uiState.value.clipsLayoutMode)

    viewModel.openClipInMode("local_test_clip", ClipsLayoutMode.CINEMA_PLAYER)
    assertEquals(MainTab.CLIPS, viewModel.uiState.value.currentTab)
    assertEquals("local_test_clip", viewModel.uiState.value.selectedClipId)
    assertEquals(ClipsLayoutMode.CINEMA_PLAYER, viewModel.uiState.value.clipsLayoutMode)
  }

  @Test
  fun testReelsFeedIsVerticalOnly() {
    val vertical = listOf(testVideo("v1", true), testVideo("v2", true))
    val horizontal = listOf(testVideo("w1", false))

    val reels = ReelsFeedSelector.build(vertical, horizontal)

    assertEquals(vertical.size, reels.size)
    assertTrue(reels.all { it.orientation == VideoOrientation.VERTICAL })
    assertTrue(reels.none { it.id == "w1" })
  }

  @Test
  fun testReelsSelectorNeverIncludesWideVideos() {
    val wide = testVideo("wide", false)
    val feed = ReelsFeedSelector.build(
      verticalClips = listOf(testVideo("v1", true)),
      horizontalVideos = listOf(wide),
      pinnedVideoId = wide.id
    )

    assertFalse(feed.any { it.id == wide.id })
    assertTrue(feed.all { it.orientation == VideoOrientation.VERTICAL })
  }

  @Test
  fun testUpdateCheckerPicksLatestOnlyWhenItIsNewer() {
    val manifest = """
      {
        "versionCode": 1031,
        "versionName": "1.0.1031",
        "apkUrl": "https://raw.githubusercontent.com/tpvkeov/clips-updates/main/app-debug.apk",
        "notes": "أحدث نسخة"
      }
    """.trimIndent()

    val newer = AppUpdateChecker.parse(manifest, currentVersionCode = 1030)
    assertTrue(newer is UpdateCheckResult.Available)
    assertEquals(1031, (newer as UpdateCheckResult.Available).info.versionCode)
    assertEquals("1.0.1031", newer.info.versionName)

    assertTrue(
      AppUpdateChecker.parse(manifest, currentVersionCode = 1031) is UpdateCheckResult.UpToDate
    )
    assertTrue(
      AppUpdateChecker.parse(manifest, currentVersionCode = 1032) is UpdateCheckResult.UpToDate
    )
    assertTrue(
      AppUpdateChecker.parse("{}", currentVersionCode = 1030) is UpdateCheckResult.Failed
    )
    assertTrue(
      AppUpdateChecker.parse(
        manifest.replace("https://", "http://"),
        currentVersionCode = 1030
      ) is UpdateCheckResult.Failed
    )
  }
}
