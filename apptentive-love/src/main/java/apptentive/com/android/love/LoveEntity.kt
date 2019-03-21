package apptentive.com.android.love

abstract class LoveEntity(val identifier: String) {
    override fun toString(): String {
        return "${javaClass.simpleName} '$identifier'"
    }
}