package apptentive.com.android.feedback.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.test.core.app.ApplicationProvider
import apptentive.com.android.TestCase
import apptentive.com.android.core.DependencyProvider
import apptentive.com.android.feedback.engagement.interactions.Interaction
import apptentive.com.android.feedback.engagement.interactions.InteractionType
import apptentive.com.android.feedback.engagement.util.MockAndroidSharedPrefDataStore
import apptentive.com.android.feedback.utils.ThrottleUtils.CONVERSATION_TYPE
import apptentive.com.android.feedback.utils.ThrottleUtils.ROSTER_TYPE
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
            assertFalse(throttleUtils.shouldThrottleInteraction("app_review_event", inAppReviewInteraction))

            // Call right after
            assertTrue(throttleUtils.shouldThrottleInteraction("app_review_event", inAppReviewInteraction))
            TimeUnit.MILLISECONDS.sleep(10L)

            // 10ms since first call
            assertTrue(throttleUtils.shouldThrottleInteraction("app_review_event", inAppReviewInteraction))
            TimeUnit.MILLISECONDS.sleep(50L)

            // 60ms since first call
            assertTrue(throttleUtils.shouldThrottleInteraction("app_review_event", inAppReviewInteraction))
            TimeUnit.MILLISECONDS.sleep(60L)

            // 120ms since first call (should be able to call again)
            assertFalse(throttleUtils.shouldThrottleInteraction("app_review_event", inAppReviewInteraction))
            TimeUnit.MILLISECONDS.sleep(50L)

            // 50ms since second call
            assertTrue(throttleUtils.shouldThrottleInteraction("app_review_event", inAppReviewInteraction))
        } catch (e: Exception) {
        }
    }

    @Test
    fun shouldThrottleRatingDialogInteractionTest() {
        try {
            // First call
            assertFalse(throttleUtils.shouldThrottleInteraction("app_review_event", ratingDialogInteraction))

            // Call right after
            assertTrue(throttleUtils.shouldThrottleInteraction("app_review_event", ratingDialogInteraction))
            TimeUnit.MILLISECONDS.sleep(10L)

            // 10ms since first call
            assertTrue(throttleUtils.shouldThrottleInteraction("app_review_event", ratingDialogInteraction))
            TimeUnit.MILLISECONDS.sleep(50L)

            // 60ms since first call
            assertTrue(throttleUtils.shouldThrottleInteraction("app_review_event", ratingDialogInteraction))
            TimeUnit.MILLISECONDS.sleep(60L)

            // 120ms since first call (should be able to call again)
            assertFalse(throttleUtils.shouldThrottleInteraction("app_review_event", ratingDialogInteraction))
            TimeUnit.MILLISECONDS.sleep(50L)

            // 50ms since second call
            assertTrue(throttleUtils.shouldThrottleInteraction("app_review_event", ratingDialogInteraction))
        } catch (e: Exception) {
        }
    }

    @Test
    fun shouldThrottleInteractionWithOtherInteractionsTest() {
        try {
            // Throttle after 1 count per session

            ThrottleUtils.interactionCountLimit = 1

            // First call interactionOne
            assertFalse(throttleUtils.shouldThrottleInteraction("note_1", noteInteractionOne))
            // 2nd call
            assertTrue(throttleUtils.shouldThrottleInteraction("note_1", noteInteractionOne))

            // First call to InteractionTwo
            assertFalse(throttleUtils.shouldThrottleInteraction("note_2", noteInteractionTwo))
            // 2nd call
            assertTrue(throttleUtils.shouldThrottleInteraction("note_2", noteInteractionTwo))

            // first call interactionThree
            assertFalse(throttleUtils.shouldThrottleInteraction("survey_1", surveyInteraction))
            // 2nd call
            assertTrue(throttleUtils.shouldThrottleInteraction("survey_1", surveyInteraction))

            // Throttle count increased to 2 per session
            ThrottleUtils.interactionCountLimit = 2

            // 3rd call but technically we have executed only once
            assertFalse(throttleUtils.shouldThrottleInteraction("note_1", noteInteractionOne))
            assertFalse(throttleUtils.shouldThrottleInteraction("note_2", noteInteractionTwo))
            assertFalse(throttleUtils.shouldThrottleInteraction("survey_1", surveyInteraction))

            // 4th call and already executed twice
            assertTrue(throttleUtils.shouldThrottleInteraction("note_1", noteInteractionOne))
            assertTrue(throttleUtils.shouldThrottleInteraction("note_2", noteInteractionTwo))
            assertTrue(throttleUtils.shouldThrottleInteraction("survey_1", surveyInteraction))

            ThrottleUtils.interactionCountLimit = 0
            assertTrue(throttleUtils.shouldThrottleInteraction("any_event", noteInteractionOne))
            assertTrue(throttleUtils.shouldThrottleInteraction("any_event", noteInteractionOne))
            assertTrue(throttleUtils.shouldThrottleInteraction("any_event", noteInteractionOne))

            ThrottleUtils.interactionCountLimit = -2
            assertTrue(throttleUtils.shouldThrottleInteraction("any_event", noteInteractionOne))

            val initiatorInteraction = object : Interaction("initiatorID", InteractionType.Initiator) {}
            ThrottleUtils.interactionCountLimit = 1
            assertFalse(throttleUtils.shouldThrottleInteraction("initiator_event", initiatorInteraction))

            ThrottleUtils.interactionCountLimit = 1
            val exemptedEvent = ThrottleUtils.exemptedEvents.first()
            assertFalse(throttleUtils.shouldThrottleInteraction(exemptedEvent, noteInteractionOne))
            assertFalse(throttleUtils.shouldThrottleInteraction(exemptedEvent, noteInteractionOne))
            assertFalse(throttleUtils.shouldThrottleInteraction(exemptedEvent, noteInteractionOne))
        } catch (e: Exception) {
        }
    }

    @Test
    fun resetEngagedEventsTest() {
        ThrottleUtils.engagedInteractions["test_id"] = 5
        throttleUtils.resetEngagedEvents()
        assertTrue(ThrottleUtils.engagedInteractions.isEmpty())
    }

    @Test
    fun shouldThrottleResetConversationTest() {
        DependencyProvider.register<AndroidSharedPrefDataStore>(MockAndroidSharedPrefDataStore())
        val result = ThrottleUtils.shouldThrottleReset(CONVERSATION_TYPE)
        assertFalse(result)
        assertTrue(ThrottleUtils.shouldThrottleReset(CONVERSATION_TYPE))
    }

    @Test
    fun shouldThrottleResetRosterTest() {
        DependencyProvider.register<AndroidSharedPrefDataStore>(MockAndroidSharedPrefDataStore())
        assertFalse(ThrottleUtils.shouldThrottleReset(ROSTER_TYPE))
        assertTrue(ThrottleUtils.shouldThrottleReset(ROSTER_TYPE))
    }

    @Test
    fun shouldThrottleResetConversationTest() {
        DependencyProvider.register<AndroidSharedPrefDataStore>(MockAndroidSharedPrefDataStore())
        val result = ThrottleUtils.shouldThrottleReset(CONVERSATION_TYPE)
        assertFalse(result)
        assertTrue(ThrottleUtils.shouldThrottleReset(CONVERSATION_TYPE))
    }

    @Test
    fun shouldThrottleResetRosterTest() {
        DependencyProvider.register<AndroidSharedPrefDataStore>(MockAndroidSharedPrefDataStore())
        assertFalse(ThrottleUtils.shouldThrottleReset(ROSTER_TYPE))
        assertTrue(ThrottleUtils.shouldThrottleReset(ROSTER_TYPE))
    }
}
