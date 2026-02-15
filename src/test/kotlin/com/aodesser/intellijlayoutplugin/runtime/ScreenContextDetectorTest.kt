package com.aodesser.intellijlayoutplugin.runtime

import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.*
import org.junit.Test
import java.awt.DisplayMode
import java.awt.GraphicsConfiguration
import java.awt.GraphicsDevice
import java.awt.Rectangle
import java.lang.reflect.Method

/**
 * Unit tests for [ScreenContextDetector].
 *
 * The `isLikelyLaptopDisplay` method is private, so we use reflection to
 * test the heuristic directly. The `detect()` method depends on
 * GraphicsEnvironment and KeyboardFocusManager statics which are hard to
 * mock in a unit-test context, so we focus on the heuristic logic here.
 */
class ScreenContextDetectorTest {

  // ----- Helpers to invoke private method via reflection -----

  private val isLikelyLaptopDisplay: Method =
    ScreenContextDetector::class.java
      .getDeclaredMethod("isLikelyLaptopDisplay", GraphicsDevice::class.java)
      .also { it.isAccessible = true }

  private fun isLaptop(device: GraphicsDevice): Boolean =
    isLikelyLaptopDisplay.invoke(ScreenContextDetector, device) as Boolean

  private fun mockDevice(
    idString: String = "display0",
    width: Int = 1920,
    height: Int = 1080,
    boundsX: Int = 0,
    boundsY: Int = 0
  ): GraphicsDevice {
    val mode = DisplayMode(width, height, 32, 60)
    val config = mockk<GraphicsConfiguration> {
      every { bounds } returns Rectangle(boundsX, boundsY, width, height)
    }
    return mockk<GraphicsDevice> {
      every { getIDstring() } returns idString
      every { displayMode } returns mode
      every { defaultConfiguration } returns config
    }
  }

  // ===================== isLikelyLaptopDisplay tests =====================

  @Test
  fun `built-in keyword in device ID triggers laptop detection`() {
    val device = mockDevice(idString = "Built-in Retina Display")
    assertTrue("Should detect 'built-in' as laptop display", isLaptop(device))
  }

  @Test
  fun `internal keyword in device ID triggers laptop detection`() {
    val device = mockDevice(idString = "Internal LCD Panel")
    assertTrue("Should detect 'internal' as laptop display", isLaptop(device))
  }

  @Test
  fun `retina keyword in device ID triggers laptop detection`() {
    val device = mockDevice(idString = "Retina")
    assertTrue("Should detect 'retina' as laptop display", isLaptop(device))
  }

  @Test
  fun `color lcd keyword in device ID triggers laptop detection`() {
    val device = mockDevice(idString = "Color LCD")
    assertTrue("Should detect 'color lcd' as laptop display", isLaptop(device))
  }

  @Test
  fun `detection is case-insensitive`() {
    val device = mockDevice(idString = "BUILT-IN RETINA DISPLAY")
    assertTrue("Detection should be case-insensitive", isLaptop(device))
  }

  @Test
  fun `low-resolution display without keywords is treated as laptop`() {
    // 1920x1080 is below the 2560x1800 threshold
    val device = mockDevice(idString = "Generic Monitor", width = 1920, height = 1080)
    assertTrue("Small resolution display should be detected as laptop", isLaptop(device))
  }

  @Test
  fun `2048x1536 display without keywords is treated as laptop (boundary)`() {
    val device = mockDevice(idString = "Unknown Display", width = 2048, height = 1536)
    assertTrue("2048x1536 is exactly at the boundary and should be laptop", isLaptop(device))
  }

  @Test
  fun `2560x1600 display without keywords is NOT laptop`() {
    // MacBook-class resolution but no keyword — external QHD monitors hit this range
    val device = mockDevice(idString = "Unknown Display", width = 2560, height = 1600)
    assertFalse("2560x1600 exceeds the lowered threshold", isLaptop(device))
  }

  @Test
  fun `high-resolution display without keywords is NOT laptop`() {
    val device = mockDevice(idString = "DELL U2723QE", width = 3840, height = 2160)
    assertFalse("4K external monitor should not be detected as laptop", isLaptop(device))
  }

  @Test
  fun `ultrawide display without keywords is NOT laptop`() {
    val device = mockDevice(idString = "LG 34WK95U", width = 5120, height = 2160)
    assertFalse("Ultrawide external should not be detected as laptop", isLaptop(device))
  }

  @Test
  fun `2049 width display without keywords is NOT laptop`() {
    // Just over the 2048 boundary
    val device = mockDevice(idString = "Generic", width = 2049, height = 1440)
    assertFalse("Width 2049 exceeds 2048 threshold", isLaptop(device))
  }

  @Test
  fun `1537 height display without keywords is NOT laptop`() {
    // Width is fine but height exceeds
    val device = mockDevice(idString = "Generic", width = 2048, height = 1537)
    assertFalse("Height 1537 exceeds 1536 threshold", isLaptop(device))
  }

  @Test
  fun `high-res display with built-in keyword IS still laptop`() {
    // Keyword trumps resolution
    val device = mockDevice(idString = "Built-in Retina Display", width = 3456, height = 2234)
    assertTrue("Keyword match should override resolution check", isLaptop(device))
  }

  // ===================== ScreenContext enum tests =====================

  @Test
  fun `ScreenContext enum has exactly three values`() {
    assertEquals(3, ScreenContext.entries.size)
    assertTrue(ScreenContext.entries.contains(ScreenContext.LAPTOP_ONLY))
    assertTrue(ScreenContext.entries.contains(ScreenContext.SINGLE_EXTERNAL))
    assertTrue(ScreenContext.entries.contains(ScreenContext.MULTI_EXTERNAL))
  }
}
