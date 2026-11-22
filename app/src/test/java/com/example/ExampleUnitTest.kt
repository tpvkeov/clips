package com.example

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import com.example.viewmodel.ClipsViewModel

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
}
