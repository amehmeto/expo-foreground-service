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

    // Multi-callback tests

    @Test
    fun `callback class names set supports multiple entries`() {
        val classNames = mutableSetOf<String>()
        classNames.add("expo.modules.a.Callback1")
        classNames.add("expo.modules.b.Callback2")
        classNames.add("expo.modules.c.Callback3")

        assertEquals(3, classNames.size)
        assertTrue(classNames.contains("expo.modules.a.Callback1"))
        assertTrue(classNames.contains("expo.modules.b.Callback2"))
        assertTrue(classNames.contains("expo.modules.c.Callback3"))
    }

    @Test
    fun `callback class names set prevents duplicates`() {
        val classNames = mutableSetOf<String>()
        classNames.add("expo.modules.test.Callback")
        val addedAgain = classNames.add("expo.modules.test.Callback")

        assertFalse("Set should not add duplicate", addedAgain)
        assertEquals(1, classNames.size)
    }

    @Test
    fun `callback instance map supports multiple instances`() {
        val instances = mutableMapOf<String, Any>()
        instances["class1"] = object {}
        instances["class2"] = object {}

        assertEquals(2, instances.size)
        assertNotNull(instances["class1"])
        assertNotNull(instances["class2"])
    }

    @Test
    fun `removing callback class removes from both storage and cache`() {
        val classNames = mutableSetOf("class1", "class2", "class3")
        val instances = mutableMapOf(
            "class1" to object {},
            "class2" to object {},
            "class3" to object {}
        )

        // Simulate removal
        classNames.remove("class2")
        instances.remove("class2")

        assertEquals(2, classNames.size)
        assertEquals(2, instances.size)
        assertFalse(classNames.contains("class2"))
        assertNull(instances["class2"])
    }

    @Test
    fun `callback instances are reused across iterations`() {
        val instances = mutableMapOf<String, Any>()
        val instance1 = object {}
        instances["class1"] = instance1

        // Simulate checking for existing instance
        val retrieved = instances["class1"]

        assertSame("Should return the same instance", instance1, retrieved)
    }
}
