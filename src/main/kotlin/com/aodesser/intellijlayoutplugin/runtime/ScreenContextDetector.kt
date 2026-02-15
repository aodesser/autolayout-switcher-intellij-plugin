package com.aodesser.intellijlayoutplugin.runtime

import java.awt.GraphicsDevice
import java.awt.GraphicsEnvironment
import java.awt.KeyboardFocusManager
import java.awt.Rectangle

object ScreenContextDetector {
  fun detect(): ScreenContext {
    val devices = GraphicsEnvironment.getLocalGraphicsEnvironment().screenDevices
    if (devices.isEmpty()) return ScreenContext.LAPTOP_ONLY

    val laptopDevices = devices.filter { isLikelyLaptopDisplay(it) }
    val hasLaptop = laptopDevices.isNotEmpty()
    val externalCount = devices.size - laptopDevices.size
    val activeDevice = activeIdeDevice(devices)

    if (devices.size == 1) {
      return if (hasLaptop) ScreenContext.LAPTOP_ONLY else ScreenContext.SINGLE_EXTERNAL
    }

    // Laptop + external(s): map based on where the IDE window currently is.
    if (hasLaptop && externalCount > 0) {
      if (activeDevice != null && isLikelyLaptopDisplay(activeDevice)) {
        return ScreenContext.LAPTOP_ONLY
      }
      return if (externalCount >= 2) ScreenContext.MULTI_EXTERNAL else ScreenContext.SINGLE_EXTERNAL
    }

    // No laptop display detected: all available displays are external.
    return if (devices.size >= 2) ScreenContext.MULTI_EXTERNAL else ScreenContext.SINGLE_EXTERNAL
  }

  private fun activeIdeDevice(devices: Array<GraphicsDevice>): GraphicsDevice? {
    val window = KeyboardFocusManager.getCurrentKeyboardFocusManager().activeWindow ?: return null
    val centerX = window.x + (window.width / 2)
    val centerY = window.y + (window.height / 2)

    return devices.firstOrNull { device ->
      val bounds: Rectangle = device.defaultConfiguration.bounds
      bounds.contains(centerX, centerY)
    }
  }

  private fun isLikelyLaptopDisplay(device: GraphicsDevice): Boolean {
    val id = device.getIDstring().lowercase()
    if ("built-in" in id || "internal" in id || "retina" in id || "color lcd" in id) return true

    // Only use resolution as a hint for very small screens (typical laptop panels).
    // 2560x1600 is the largest common laptop panel (16" MacBook Pro).
    // Avoiding false positives on QHD (2560x1440) external monitors.
    val mode = device.displayMode
    return mode.width <= 2048 && mode.height <= 1536
  }
}
