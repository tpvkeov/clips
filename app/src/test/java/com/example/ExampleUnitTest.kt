package com.example

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Test
import com.example.viewmodel.ClipsViewModel
import com.example.data.DemoDataFactory
import com.example.data.ReelsFeedSelector
import com.example.update.AppUpdateChecker
import com.example.update.UpdateCheckResult
import com.example.model.ClipsLayoutMode
import com.example.model.MainTab
import com.example.model.VideoOrientation

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testShareVideoIncrementsPointsAndSetsEvent() {
    val viewModel = ClipsViewModel()
    val initialPoints = viewModel.uiState.value.userProfile.points
    val clipId = viewModel.uiState.value.verticalClips.first().id

    // First share: points should increment by 2
    viewModel.shareVideo(clipId)
    val stateAfterFirst = viewModel.uiState.value
    assertEquals(initialPoints + 2, stateAfterFirst.userProfile.points)
    assertNotNull(stateAfterFirst.shareFeedbackEvent)
    assertTrue((stateAfterFirst.shareFeedbackEvent?.id ?: 0L) > 0L)
    assertTrue(stateAfterFirst.shareFeedbackEvent?.isNewPointsAwarded == true)

    viewModel.clearShareFeedback()
    assertNull(viewModel.uiState.value.shareFeedbackEvent)

    // Second share of same video: points must NOT increase again (awarded only once)
    viewModel.shareVideo(clipId)
    val stateAfterSecond = viewModel.uiState.value
    assertEquals(initialPoints + 2, stateAfterSecond.userProfile.points)
    assertNotNull(stateAfterSecond.shareFeedbackEvent)
    assertEquals(false, stateAfterSecond.shareFeedbackEvent?.isNewPointsAwarded)
  }

  @Test
  fun testSharePostIncrementsPointsAndSetsEvent() {
    val viewModel = ClipsViewModel()
    val initialPoints = viewModel.uiState.value.userProfile.points
    val postId = viewModel.uiState.value.posts.first().id

    // First share: points should increment by 2
    viewModel.sharePost(postId)
    val stateAfterFirst = viewModel.uiState.value
    assertEquals(initialPoints + 2, stateAfterFirst.userProfile.points)
    assertNotNull(stateAfterFirst.shareFeedbackEvent)
    assertTrue((stateAfterFirst.shareFeedbackEvent?.id ?: 0L) > 0L)
    assertTrue(stateAfterFirst.shareFeedbackEvent?.isNewPointsAwarded == true)

    // Second share of same post: points must NOT increase again
    viewModel.sharePost(postId)
    val stateAfterSecond = viewModel.uiState.value
    assertEquals(initialPoints + 2, stateAfterSecond.userProfile.points)
    assertEquals(false, stateAfterSecond.shareFeedbackEvent?.isNewPointsAwarded)
  }

  @Test
  fun testDemoClipVideoUrlsAreTenAndAssignedToInitialClips() {
    assertEquals(10, DemoDataFactory.demoClipVideoUrls.size)
    assertEquals(10, DemoDataFactory.initialVerticalClips.size)

    val viewModel = ClipsViewModel()
    val clips = viewModel.uiState.value.verticalClips
    assertEquals(10, clips.size)

    for (i in 0 until 10) {
      val url = DemoDataFactory.demoClipVideoUrls[i]
      assertTrue("URL at $i should not be blank", url.isNotBlank())
      assertTrue("URL at $i should be mp4", url.endsWith(".mp4", ignoreCase = true))
      assertEquals("Clip at $i should match demo URL", url, clips[i].mediaUriString)
    }
  }

  @Test
  fun testNoStarInPresetNamesOrClips() {
    for (preset in DemoDataFactory.avatarPresets) {
      assertFalse("Preset name ${preset.nameAr} must not contain 'نجم'", preset.nameAr.contains("نجم"))
      assertFalse("Preset name ${preset.nameAr} must not contain 'ستار'", preset.nameAr.contains("ستار"))
    }
    for (clip in DemoDataFactory.initialVerticalClips) {
      assertFalse("Clip title ${clip.title} must not contain 'نجم'", clip.title.contains("نجم"))
      assertFalse("Clip title ${clip.title} must not contain 'ستار'", clip.title.contains("ستار"))
    }
  }

  @Test
  fun testClipsLayoutModeCyclesBetweenTwoModes() {
    val viewModel = ClipsViewModel()
    assertEquals(ClipsLayoutMode.PRIMARY, viewModel.uiState.value.clipsLayoutMode)

    viewModel.toggleClipsLayoutMode()
    assertEquals(ClipsLayoutMode.CINEMA_PLAYER, viewModel.uiState.value.clipsLayoutMode)

    viewModel.toggleClipsLayoutMode()
    assertEquals(ClipsLayoutMode.PRIMARY, viewModel.uiState.value.clipsLayoutMode)

    val sixthClipId = viewModel.uiState.value.verticalClips[5].id
    viewModel.openClipInMode(sixthClipId, ClipsLayoutMode.CINEMA_PLAYER)
    assertEquals(MainTab.CLIPS, viewModel.uiState.value.currentTab)
    assertEquals(sixthClipId, viewModel.uiState.value.selectedClipId)
    assertEquals(ClipsLayoutMode.CINEMA_PLAYER, viewModel.uiState.value.clipsLayoutMode)
  }

  @Test
  fun testToggleSavePostUpdatesSavedList() {
    val viewModel = ClipsViewModel()
    val postId = viewModel.uiState.value.posts.first().id
    assertTrue(viewModel.uiState.value.savedPosts.isEmpty())

    viewModel.toggleSavePost(postId)
    assertEquals(1, viewModel.uiState.value.savedPosts.size)
    assertEquals(postId, viewModel.uiState.value.savedPosts.first().id)

    viewModel.toggleSavePost(postId)
    assertTrue(viewModel.uiState.value.savedPosts.isEmpty())
  }

  @Test
  fun testResumeOnlyAfterAccidentalExit() {
    val viewModel = ClipsViewModel()
    val clipId = viewModel.uiState.value.verticalClips.first().id
    viewModel.playNewClips()
    viewModel.recordWatchProgress(clipId, 0.4f)
    viewModel.markReelsBackgrounded()

    assertEquals(clipId, viewModel.uiState.value.resumeCandidateId)
    assertEquals(clipId, viewModel.uiState.value.continueWatching?.videoId)

    // Deliberate navigation must clear the resume offer.
    viewModel.markReelsLeftDeliberately()
    assertNull(viewModel.uiState.value.resumeCandidateId)
    assertNull(viewModel.uiState.value.continueWatching)

    // "New videos" starts fresh and resets the pager session.
    val session = viewModel.uiState.value.clipsSessionToken
    viewModel.playNewClips()
    assertNull(viewModel.uiState.value.selectedClipId)
    assertEquals(session + 1, viewModel.uiState.value.clipsSessionToken)
  }

  @Test
  fun testReelsFeedStaysVerticalWithOneRareWideException() {
    val viewModel = ClipsViewModel()
    val reels = viewModel.uiState.value.reelsClips

    assertEquals(10, reels.count { it.orientation == VideoOrientation.VERTICAL })
    assertEquals(1, reels.count { it.orientation == VideoOrientation.HORIZONTAL })
    val exception = reels.first { it.orientation == VideoOrientation.HORIZONTAL }
    assertFalse(
      "Wide reels exception must have a real media URL",
      exception.mediaUriString.isNullOrBlank()
    )
    assertTrue(
      "Wide reels exception must be a playable MP4",
      exception.mediaUriString!!.endsWith(".mp4", ignoreCase = true)
    )
    assertTrue("Wide exception must earn its place", exception.viewsCount >= 100_000)
    assertTrue("Wide exception must be well liked", exception.likesCount >= 5_000)
  }

  @Test
  fun testAllDemoMediaUsesRealPlayableVideos() {
    val horizontalUrls = DemoDataFactory.initialHorizontalVideos.mapNotNull { it.mediaUriString }
    val verticalUrls = DemoDataFactory.initialVerticalClips.mapNotNull { it.mediaUriString }
    val linkedPostUrls = DemoDataFactory.initialPosts
      .filter { !it.linkedVideoId.isNullOrBlank() }
      .mapNotNull { it.mediaUriString }
    val storyUrls = DemoDataFactory.initialStories.flatMap { group ->
      group.slides.mapNotNull { it.mediaUriString }
    }
    val allUrls = DemoDataFactory.demoClipVideoUrls + horizontalUrls + verticalUrls +
      linkedPostUrls + storyUrls

    assertEquals(10, DemoDataFactory.initialHorizontalVideos.size)
    assertTrue(
      "Every horizontal demo video should have a media URL",
      DemoDataFactory.initialHorizontalVideos.all { !it.mediaUriString.isNullOrBlank() }
    )
    assertTrue(
      "Every linked video post should have a playable preview",
      DemoDataFactory.initialPosts
        .filter { !it.linkedVideoId.isNullOrBlank() }
        .all { !it.mediaUriString.isNullOrBlank() }
    )
    assertTrue(
      "Every story slide should have a playable video",
      DemoDataFactory.initialStories.all { group ->
        group.slides.all { !it.mediaUriString.isNullOrBlank() }
      }
    )
    allUrls.forEach { url ->
      assertTrue(
        "Demo media should be an HTTPS MP4: $url",
        url.startsWith("https://", ignoreCase = true) &&
          url.endsWith(".mp4", ignoreCase = true)
      )
    }
  }

  @Test
  fun testReelsSelectorRejectsWeakWideVideos() {
    val weakWide = DemoDataFactory.initialHorizontalVideos[2].copy(
      mediaUriString = "https://example.com/low-quality.mp4",
      viewsCount = 900,
      likesCount = 10
    )
    val feed = ReelsFeedSelector.build(DemoDataFactory.initialVerticalClips, listOf(weakWide))

    assertEquals(DemoDataFactory.initialVerticalClips.size, feed.size)
    assertTrue(feed.all { it.orientation == VideoOrientation.VERTICAL })
  }

  @Test
  fun testReelsSelectorHonoursExplicitlyOpenedWideVideo() {
    val wide = DemoDataFactory.initialHorizontalVideos[3]
    val feed = ReelsFeedSelector.build(
      verticalClips = DemoDataFactory.initialVerticalClips,
      horizontalVideos = DemoDataFactory.initialHorizontalVideos,
      pinnedVideoId = wide.id
    )

    assertTrue(feed.any { it.id == wide.id })
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
