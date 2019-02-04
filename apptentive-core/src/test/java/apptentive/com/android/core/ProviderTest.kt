package apptentive.com.android.core

import org.junit.Test

import org.junit.Assert.*
import org.junit.Before

class ProviderTest {

    @Before
    fun setUp() {
        Provider.clear()
    }

    @Test
    fun testRegisterProvidables() {
        val providable1 = MockProvidable1()
        val providable2 = MockProvidable2()

        Provider.register(providable1)
        Provider.register(providable2)

        assertSame(providable1, Provider.of<MockProvidable1>())
        assertSame(providable2, Provider.of<MockProvidable2>())
        assertNull(Provider.of<MockProvidable3>())
    }

    private class MockProvidable1 : Providable
    private class MockProvidable2 : Providable
    private class MockProvidable3 : Providable
}