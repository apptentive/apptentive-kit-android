package apptentive.com.android.util

import junit.framework.Assert.assertTrue
import org.junit.Test

class ListExtensionsTest {

    @Test
    fun compareTwoIdenticalLists() {
        val list1 = listOf(1, 2, 3, 4)
        val list2 = listOf(1, 2, 3, 4)
        assertTrue(list1.isSame(list2))
    }

    @Test
    fun compareTwoNonIdenticalListsWithSameElements() {
        val list1 = listOf(1, 2, 3, 4)
        val list2 = listOf(4, 3, 2, 1)
        assertTrue(list1.isSame(list2))
    }

    @Test
    fun compareTwoDifferentLists() {
        val list1 = listOf(1, 2, 3, 4)
        val list2 = listOf(4, 3, 1)
        assertTrue(!list1.isSame(list2))
    }
}
