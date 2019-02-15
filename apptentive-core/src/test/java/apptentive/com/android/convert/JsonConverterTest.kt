package apptentive.com.android.convert

import org.junit.Assert.assertEquals
import org.junit.Test

class JsonConverterTest {
    @Test
    fun fromJson() {
        val json = """{
            "doubleField": 1.0,
            "floatField": 2.0,
            "longField": 3,
            "intField": 4,
            "shortField": 5,
            "byteField": 6,
            "stringField": "value",
            child: {
                "doubleField": 7.0,
                "floatField": 8.0,
                "longField": 9,
                "intField": 10,
                "shortField": 11,
                "byteField": 12,
                "stringField": "child value"
            }
        }
        """
        val actual = createMyClass(
            doubleField = 1.0,
            floatField = 2.0f,
            longField = 3,
            intField = 4,
            shortField = 5,
            byteField = 6,
            stringField = "value",
            child = createMyClass(
                doubleField = 7.0,
                floatField = 8.0f,
                longField = 9,
                intField = 10,
                shortField = 11,
                byteField = 12,
                stringField = "child value"
            )
        )
        val expected = JsonConverter.fromJson<MyClass>(json)
        assertEquals(expected, actual)
    }
}

private class MyClass {
    var doubleField: Double = 0.0
    var floatField: Float = 0.0f
    var longField: Long = 0L
    var intField: Int = 0
    var shortField: Short = 0
    var byteField: Byte = 0
    var stringField: String? = null
    var child: MyClass? = null
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MyClass

        if (doubleField != other.doubleField) return false
        if (floatField != other.floatField) return false
        if (longField != other.longField) return false
        if (intField != other.intField) return false
        if (shortField != other.shortField) return false
        if (byteField != other.byteField) return false
        if (stringField != other.stringField) return false
        if (child != other.child) return false

        return true
    }

    override fun hashCode(): Int {
        var result = doubleField.hashCode()
        result = 31 * result + floatField.hashCode()
        result = 31 * result + longField.hashCode()
        result = 31 * result + intField
        result = 31 * result + shortField
        result = 31 * result + byteField
        result = 31 * result + (stringField?.hashCode() ?: 0)
        result = 31 * result + (child?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "MyClass(doubleField=$doubleField, floatField=$floatField, longField=$longField, intField=$intField, shortField=$shortField, byteField=$byteField, stringField=$stringField, child=$child)"
    }
}

private fun createMyClass(
    doubleField: Double = 0.0,
    floatField: Float = 0.0f,
    longField: Long = 0L,
    intField: Int = 0,
    shortField: Short = 0,
    byteField: Byte = 0,
    stringField: String? = null,
    child: MyClass? = null
): MyClass {
    val myClass = MyClass()
    myClass.doubleField = doubleField
    myClass.floatField = floatField
    myClass.longField = longField
    myClass.intField = intField
    myClass.shortField = shortField
    myClass.byteField = byteField
    myClass.stringField = stringField
    myClass.child = child
    return myClass
}