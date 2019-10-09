package apptentive.com.android.feedback

import apptentive.com.android.feedback.engagement.interactions.Interaction
import apptentive.com.android.feedback.engagement.interactions.InteractionModule
import apptentive.com.android.util.Log
import apptentive.com.android.util.LogTags.core

// TODO: should we load interaction modules lazily?
class InteractionModuleComponent(
    private val interactionNames: List<String>,
    private val packageName: String,
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
            Log.d(core, "Module not found: $className")
        } catch (e: IllegalAccessException) {
            Log.e(core, "Module class or its nullary constructor is not accessible: $className", e)
        } catch (e: InstantiationException) {
            Log.e(core, "Unable to instantiate module class: $className", e)
        } catch (e: ExceptionInInitializerError) {
            Log.e(core, "Exception while initializing module class: $className", e)
        } catch (e: Exception) {
            Log.e(core, "Exception while loading module class: $className", e)
        }

        return null
    }
}
