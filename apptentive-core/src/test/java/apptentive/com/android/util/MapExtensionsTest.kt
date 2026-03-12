package apptentive.com.android.util

import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import org.junit.Test

class MapExtensionsTest {

    private val testMap = mapOf(
        "string" to "value",
        "int" to 42,
        "double" to 42.9,
        "boolean" to true,
        "map" to mapOf("nested" to "yes"),
        "list" to listOf(1, 2, 3)
    )

    @Test
    fun getString_returnsStringValue() {
        assertEquals("value", testMap.getString("string"))
    }

    @Test(expected = MissingKeyException::class)
    fun getString_missingKey_throwsException() {
        testMap.getString("missing")
    }

    @Test
    fun optString_returnsStringOrDefault() {
        assertEquals("value", testMap.optString("string"))
        assertEquals("default", testMap.optString("missing", "default"))
        assertNull(testMap.optString("missing"))
    }

    @Test
    fun getInt_returnsIntOrCastsDouble() {
        assertEquals(42, testMap.getInt("int"))
        assertEquals(42, testMap.getInt("double"))
    }

    @Test(expected = MissingKeyException::class)
    fun getInt_missingKey_throwsException() {
        testMap.getInt("missing")
    }

    @Test
    fun optInt_returnsIntOrDefault() {
        assertEquals(42, testMap.optInt("int"))
        assertEquals(42, testMap.optInt("double"))
        assertEquals(99, testMap.optInt("missing", 99))
    }

    @Test
    fun optNullableInt_returnsIntOrNull() {
        assertEquals(42, testMap.optNullableInt("int"))
        assertEquals(42, testMap.optNullableInt("double"))
        assertNull(testMap.optNullableInt("missing"))
    }

    @Test
    fun getBoolean_returnsBoolean() {
        assertTrue(testMap.getBoolean("boolean"))
    }

    @Test(expected = MissingKeyException::class)
    fun getBoolean_missingKey_throwsException() {
        testMap.getBoolean("missing")
    }

    @Test
    fun optBoolean_returnsBooleanOrDefault() {
        assertTrue(testMap.optBoolean("boolean"))
        assertFalse(testMap.optBoolean("missing"))
        assertTrue(testMap.optBoolean("missing", true))
    }

    @Test
    fun getMap_returnsMap() {
        assertEquals(mapOf("nested" to "yes"), testMap.getMap("map"))
    }

    @Test(expected = MissingKeyException::class)
    fun getMap_missingKey_throwsException() {
        testMap.getMap("missing")
    }

    @Test
    fun optMap_returnsMapOrDefault() {
        assertEquals(mapOf("nested" to "yes"), testMap.optMap("map"))
        val defaultMap = mapOf("default" to 1)
        assertEquals(defaultMap, testMap.optMap("missing", defaultMap))
        assertNull(testMap.optMap("missing"))
    }

    @Test
    fun getList_returnsList() {
        assertEquals(listOf(1, 2, 3), testMap.getList("list"))
    }

    @Test(expected = MissingKeyException::class)
    fun getList_missingKey_throwsException() {
        testMap.getList("missing")
    }

    @Test
    fun optList_returnsListOrDefault() {
        assertEquals(listOf(1, 2, 3), testMap.optList("list"))
        val defaultList = listOf("a", "b")
        assertEquals(defaultList, testMap.optList("missing", defaultList))
        assertNull(testMap.optList("missing"))
    }
}
