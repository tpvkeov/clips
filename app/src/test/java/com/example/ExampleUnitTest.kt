package com.example

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Test
import com.example.viewmodel.ClipsViewModel
import com.example.data.DemoDataFactory
import com.example.model.ClipsLayoutMode
import com.example.model.MainTab

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
}
