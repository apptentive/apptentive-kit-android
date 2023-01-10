package apptentive.com.android.feedback.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.test.core.app.ApplicationProvider
import apptentive.com.android.TestCase
import apptentive.com.android.core.DependencyProvider
import apptentive.com.android.feedback.engagement.interactions.Interaction
import apptentive.com.android.feedback.engagement.interactions.InteractionType
import apptentive.com.android.feedback.engagement.util.MockAndroidSharedPrefDataStore
import apptentive.com.android.platform.AndroidSharedPrefDataStore
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit

class ThrottleUtilsTest : TestCase() {
    private val APPTENTIVE_TEST_SHARED_PREF = "APPTENTIVE TEST SHARED PREF"
    val context: Context = ApplicationProvider.getApplicationContext()

    private var sharedPreferences: SharedPreferences? = null
    private var throttleUtils = ThrottleUtils

    private val inAppReviewInteraction =
        object : Interaction("inAppReviewID_1", InteractionType.GoogleInAppReview) {}
    private val ratingDialogInteraction =
        object : Interaction("ratingDialogID_1", InteractionType.RatingDialog) {}
    private val noteInteractionOne =
        object : Interaction("noteID_1", InteractionType.TextModal) {}
    private val noteInteractionTwo =
        object : Interaction("noteID_2", InteractionType.TextModal) {}
    private val surveyInteraction =
        object : Interaction("surveyID_1", InteractionType.Survey) {}

    @Before
    override fun setUp() {
        sharedPreferences =
            context.getSharedPreferences(APPTENTIVE_TEST_SHARED_PREF, Context.MODE_PRIVATE)
        throttleUtils.ratingThrottleLength = 100L
        throttleUtils.throttleSharedPrefs = sharedPreferences
        super.setUp()
    }

    @After
    fun tearDown() {
        sharedPreferences = null
    }

    @Test
    fun shouldThrottleInAppReviewInteractionTest() {
        try {
            // First call
            assertFalse(throttleUtils.shouldThrottleInteraction(inAppReviewInteraction))

            // Call right after
            assertTrue(throttleUtils.shouldThrottleInteraction(inAppReviewInteraction))
            TimeUnit.MILLISECONDS.sleep(10L)

            // 10ms since first call
            assertTrue(throttleUtils.shouldThrottleInteraction(inAppReviewInteraction))
            TimeUnit.MILLISECONDS.sleep(50L)

            // 60ms since first call
            assertTrue(throttleUtils.shouldThrottleInteraction(inAppReviewInteraction))
            TimeUnit.MILLISECONDS.sleep(60L)

            // 120ms since first call (should be able to call again)
            assertFalse(throttleUtils.shouldThrottleInteraction(inAppReviewInteraction))
            TimeUnit.MILLISECONDS.sleep(50L)

            // 50ms since second call
            assertTrue(throttleUtils.shouldThrottleInteraction(inAppReviewInteraction))
        } catch (e: Exception) {
        }
    }

    @Test
    fun shouldThrottleRatingDialogInteractionTest() {
        try {
            // First call
            assertFalse(throttleUtils.shouldThrottleInteraction(ratingDialogInteraction))

            // Call right after
            assertTrue(throttleUtils.shouldThrottleInteraction(ratingDialogInteraction))
            TimeUnit.MILLISECONDS.sleep(10L)

            // 10ms since first call
            assertTrue(throttleUtils.shouldThrottleInteraction(ratingDialogInteraction))
            TimeUnit.MILLISECONDS.sleep(50L)

            // 60ms since first call
            assertTrue(throttleUtils.shouldThrottleInteraction(ratingDialogInteraction))
            TimeUnit.MILLISECONDS.sleep(60L)

            // 120ms since first call (should be able to call again)
            assertFalse(throttleUtils.shouldThrottleInteraction(ratingDialogInteraction))
            TimeUnit.MILLISECONDS.sleep(50L)

            // 50ms since second call
            assertTrue(throttleUtils.shouldThrottleInteraction(ratingDialogInteraction))
        } catch (e: Exception) {
        }
    }

    @Test
    fun shouldThrottleInteractionWithOtherInteractionsTest() {
        try {
            // Default throttle length is 1 second aka 1000 ms

            // First call interactionTwo
            assertFalse(throttleUtils.shouldThrottleInteraction(noteInteractionOne))
            TimeUnit.MILLISECONDS.sleep(300L)

            // 300ms since first call interactionTwo
            assertTrue(throttleUtils.shouldThrottleInteraction(noteInteractionOne))

            // Same Type as interactionTwo (so 300ms since last called this type)
            assertTrue(throttleUtils.shouldThrottleInteraction(noteInteractionTwo))

            // first call interactionFour
            assertFalse(throttleUtils.shouldThrottleInteraction(surveyInteraction))
            TimeUnit.MILLISECONDS.sleep(500L)

            // 800ms since second call interactionTwo
            assertTrue(throttleUtils.shouldThrottleInteraction(noteInteractionOne))
            assertTrue(throttleUtils.shouldThrottleInteraction(noteInteractionTwo))
            TimeUnit.MILLISECONDS.sleep(300L)

            // 1100ms since first call of interactionTwo (should be able to call)
            assertFalse(throttleUtils.shouldThrottleInteraction(noteInteractionTwo))

            // Same type as interactionThree that was just called (shouldn't be able to call)
            assertTrue(throttleUtils.shouldThrottleInteraction(noteInteractionOne))

            // 800ms since first call of interactionFour
            assertTrue(throttleUtils.shouldThrottleInteraction(surveyInteraction))
            TimeUnit.MILLISECONDS.sleep(300L)

            // 1100ms since first call of interactionFour
            assertFalse(throttleUtils.shouldThrottleInteraction(surveyInteraction))

            // Call right after same interaction should not call
            assertTrue(throttleUtils.shouldThrottleInteraction(surveyInteraction))
        } catch (e: Exception) {
        }
    }

    @Test
    fun shouldThrottleResetConversationTest() {
        DependencyProvider.register<AndroidSharedPrefDataStore>(MockAndroidSharedPrefDataStore())
        val result = ThrottleUtils.shouldThrottleResetConversation()
        assertFalse(result)
        assertTrue(ThrottleUtils.shouldThrottleResetConversation())
    }
}
