package apptentive.com.android.serialization.json

import org.junit.Assert.assertEquals
import org.junit.Test

class JsonConverterTest {
    @Test
    fun fromJson() {
        val json =
            """{"doubleField":1.0,"floatField":2.0,"longField":3,"intField":4,"shortField":5,"byteField":6,"stringField":"value","child":{"doubleField":7.0,"floatField":8.0,"longField":9,"intField":10,"shortField":11,"byteField":12,"stringField":"child value"}}"""
        val actual = MyClass(
            doubleField = 1.0,
            floatField = 2.0f,
            longField = 3,
            intField = 4,
            shortField = 5,
            byteField = 6,
            stringField = "value",
            child = MyClass(
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

        val converted = JsonConverter.toJson(actual)
        assertEquals(json, converted)
    }
}

private data class MyClass(
    val doubleField: Double = 0.0,
    val floatField: Float = 0.0f,
    val longField: Long = 0L,
    val intField: Int = 0,
    val shortField: Short = 0,
    val byteField: Byte = 0,
    val stringField: String? = null,
    val child: MyClass? = null
)