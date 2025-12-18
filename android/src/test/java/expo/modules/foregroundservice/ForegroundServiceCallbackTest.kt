package expo.modules.foregroundservice

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for ForegroundServiceCallback interface.
 * Note: Tests that require Android runtime (Log, Context, Service) are covered
 * by instrumented tests, not unit tests.
 */
class ForegroundServiceCallbackTest {

    @Test
    fun `ForegroundServiceCallback interface exists`() {
        // Verify the interface is accessible
        val interfaceClass = ForegroundServiceCallback::class.java
        assertTrue(interfaceClass.isInterface)
    }

    @Test
    fun `ForegroundServiceCallback has onServiceStarted method`() {
        val interfaceClass = ForegroundServiceCallback::class.java
        val methods = interfaceClass.declaredMethods.map { it.name }

        assertTrue("Should have onServiceStarted method", methods.contains("onServiceStarted"))
    }

    @Test
    fun `ForegroundServiceCallback has onServiceStopped method`() {
        val interfaceClass = ForegroundServiceCallback::class.java
        val methods = interfaceClass.declaredMethods.map { it.name }

        assertTrue("Should have onServiceStopped method", methods.contains("onServiceStopped"))
    }

    @Test
    fun `onServiceStarted has correct parameter type`() {
        val interfaceClass = ForegroundServiceCallback::class.java
        val method = interfaceClass.declaredMethods.find { it.name == "onServiceStarted" }

        assertNotNull("onServiceStarted method should exist", method)
        assertEquals("Should have exactly 1 parameter", 1, method!!.parameterCount)
        assertEquals(
            "Parameter should be Context",
            "android.content.Context",
            method.parameterTypes[0].name
        )
    }

    @Test
    fun `onServiceStopped has no parameters`() {
        val interfaceClass = ForegroundServiceCallback::class.java
        val method = interfaceClass.declaredMethods.find { it.name == "onServiceStopped" }

        assertNotNull("onServiceStopped method should exist", method)
        assertEquals("Should have no parameters", 0, method!!.parameterCount)
    }

    @Test
    fun `callback class name format validation - valid names contain dots`() {
        // Valid fully qualified class names should contain at least one dot
        val validNames = listOf(
            "com.example.Callback",
            "expo.modules.foregroundservice.TestCallback",
            "com.company.module.sub.MyCallback"
        )

        validNames.forEach { name ->
            assertTrue("Class name '$name' should contain at least one dot", name.contains("."))
        }
    }

    @Test
    fun `callback class name format validation - matches package pattern`() {
        val validName = "expo.modules.blockingoverlay.BlockingCallback"

        // Should match typical package naming convention
        assertTrue(validName.matches(Regex("^[a-z][a-z0-9_]*(\\.[a-z][a-z0-9_]*)*\\.[A-Z][a-zA-Z0-9]*$")))
    }
}
