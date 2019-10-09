package apptentive.com.android.feedback

import apptentive.com.android.feedback.engagement.interactions.Interaction
import apptentive.com.android.feedback.engagement.interactions.InteractionConverter
import apptentive.com.android.feedback.engagement.interactions.InteractionLauncher
import apptentive.com.android.feedback.engagement.interactions.InteractionModule
import apptentive.com.android.feedback.test.TestCase
import com.google.common.truth.Truth.assertThat
import org.junit.Ignore
import org.junit.Test

class InteractionModuleComponentTest : TestCase() {
    @Test
    fun testGetModules() {
        val component = InteractionModuleComponent(
            interactionNames = listOf("TestInteraction", "MissingInteraction"),
            packageName = javaClass.getPackage()!!.name,
            classPrefix = "My",
            classSuffix = "Module"
        )
        val modules = component.getModules()
        assertThat(modules["TestInteraction"]).isInstanceOf(MyTestInteractionModule::class.java)
    }

    @Test
    @Ignore
    fun testAbstractModule() {
        TODO("Should skip module and log error message")
    }

    @Test
    @Ignore
    fun testExceptionInModuleInitializer() {
        TODO("Should skip module and log error message")
    }
}

private class TestInteraction(id: String) : Interaction(id)

private class MyTestInteractionModule : InteractionModule<TestInteraction> {
    override val interactionClass = TestInteraction::class.java

    override fun provideInteractionConverter(): InteractionConverter<TestInteraction> {
        throw AssertionError("Should not get there")
    }

    override fun provideInteractionLauncher(): InteractionLauncher<TestInteraction> {
        throw AssertionError("Should not get there")
    }
}