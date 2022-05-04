package apptentive.com.android.feedback

import apptentive.com.android.feedback.engagement.interactions.Interaction
import apptentive.com.android.feedback.engagement.interactions.InteractionModule
import apptentive.com.android.feedback.engagement.interactions.InteractionType
import apptentive.com.android.util.InternalUseOnly
import apptentive.com.android.util.Log
import apptentive.com.android.util.LogTags.CORE

@InternalUseOnly
class InteractionModuleComponent(
    private val packageName: String,
    private val interactionNames: List<String>,
    private val classPrefix: String = "",
    private val classSuffix: String = ""
) {
    fun getModules(): Map<String, InteractionModule<Interaction>> {
        val classNames = getClassNames(packageName, classPrefix, classSuffix)
        return getModules(classNames)
    }

    private fun getClassNames(
        packageName: String,
        classPrefix: String,
        classSuffix: String
    ): List<String> {
        return interactionNames.map { interactionType ->
            "$packageName.$classPrefix$interactionType$classSuffix"
        }
    }

    private fun getModules(classNames: List<String>): Map<String, InteractionModule<Interaction>> {
        @Suppress("UNCHECKED_CAST")
        return interactionNames.zip(classNames)
            .asSequence()
            .toMap()
            .mapValues { (_, className) -> getModule(className) }
            .mapNotNull { it.value?.let { value -> it.key to value } }
            .toMap() as Map<String, InteractionModule<Interaction>>
    }

    private fun getModule(className: String): InteractionModule<*>? {
        try {
            val moduleClass = Class.forName(className)
            return moduleClass.newInstance() as InteractionModule<*>
        } catch (e: ClassNotFoundException) {
            Log.v(CORE, "Module not found: $className")
        } catch (e: IllegalAccessException) {
            Log.e(CORE, "Module class or its nullary constructor is not accessible: $className", e)
        } catch (e: InstantiationException) {
            Log.e(CORE, "Unable to instantiate module class: $className", e)
        } catch (e: ExceptionInInitializerError) {
            Log.e(CORE, "Exception while initializing module class: $className", e)
        } catch (e: Exception) {
            Log.e(CORE, "Exception while loading module class: $className", e)
        }

        return null
    }

    companion object {
        fun default() = InteractionModuleComponent(
            packageName = "apptentive.com.android.feedback",
            interactionNames = InteractionType.names(),
            classSuffix = "Module"
        )
    }
}
