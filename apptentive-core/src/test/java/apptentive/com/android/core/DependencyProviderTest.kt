package apptentive.com.android.core

import apptentive.com.android.TestCase
import org.junit.Assert.assertSame
import org.junit.Test

class DependencyProviderTest : TestCase() {

    @Test
    fun testRegisterProvidables() {
        val providable1 = MockProvidable1Impl()
        val providable2 = MockProvidable2Impl()

        DependencyProvider.register<MockProvidable1>(providable1)
        DependencyProvider.register<MockProvidable2>(providable2)

        assertSame(providable1, DependencyProvider.of<MockProvidable1>())
        assertSame(providable2, DependencyProvider.of<MockProvidable2>())
    }

    private interface MockProvidable1 : Providable
    private interface MockProvidable2 : Providable

    private class MockProvidable1Impl : MockProvidable1
    private class MockProvidable2Impl : MockProvidable2
}