package apptentive.com.android.feedback

import apptentive.com.android.feedback.engagement.interactions.Interaction
import apptentive.com.android.feedback.engagement.interactions.InteractionTypeConverter
import apptentive.com.android.feedback.engagement.interactions.InteractionLauncher
import apptentive.com.android.feedback.engagement.interactions.InteractionModule
import apptentive.com.android.feedback.test.TestCase
import com.google.common.truth.Truth.assertThat
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
    fun testAbstractModule() {
        val component = InteractionModuleComponent(
            interactionNames = listOf("AbstractInteraction", "TestInteraction"),
            packageName = javaClass.getPackage()!!.name,
            classPrefix = "My",
            classSuffix = "Module"
        )
        val modules = component.getModules()
        assertThat(modules.size).isEqualTo(1)
        assertThat(modules["TestInteraction"]).isInstanceOf(MyTestInteractionModule::class.java)
    }

    @Test
    fun testExceptionInModuleInitializer() {
        // 1. create a new interaction module which would throw an exception upon initialization
        // 2. get all the modules
        // 3. observe nothing had crashed and you have successfully loaded everything else
        TODO("Implement me")
    }
}

private class TestInteraction(id: String) : Interaction(id)

private class MyTestInteractionModule : InteractionModule<TestInteraction> {
    override val interactionClass = TestInteraction::class.java

    override fun provideInteractionTypeConverter(): InteractionTypeConverter<TestInteraction> {
        throw AssertionError("Should not get there")
    }

    override fun provideInteractionLauncher(): InteractionLauncher<TestInteraction> {
        throw AssertionError("Should not get there")
    }
}

private abstract class MyAbstractInteractionModule : InteractionModule<TestInteraction>