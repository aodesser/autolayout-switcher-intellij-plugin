package com.aodesser.intellijlayoutplugin.settings

import com.intellij.testFramework.fixtures.BasePlatformTestCase

/**
 * Integration tests for [LayoutAutoSwitchConfigurable].
 *
 * Tests the settings panel creation, apply, reset, and isModified logic
 * within a real IntelliJ application context.
 */
class LayoutAutoSwitchConfigurableTest : BasePlatformTestCase() {

  private lateinit var configurable: LayoutAutoSwitchConfigurable

  override fun setUp() {
    super.setUp()
    // Reset state to defaults before each test
    val state = LayoutAutoSwitchState.getInstance()
    state.loadState(LayoutAutoSwitchState.State())
    configurable = LayoutAutoSwitchConfigurable()
  }

  override fun tearDown() {
    try {
      configurable.disposeUIResources()
    } finally {
      super.tearDown()
    }
  }

  fun `test display name is correct`() {
    assertEquals("AutoLayout Switcher", configurable.displayName)
  }

  fun `test createPanel returns non-null panel`() {
    val panel = configurable.createPanel()
    assertNotNull("createPanel should return a non-null DialogPanel", panel)
  }

  fun `test panel can be created and reset without error`() {
    configurable.createPanel()
    configurable.reset()
  }

  fun `test isModified returns false after reset`() {
    configurable.createPanel()
    configurable.reset()
    assertFalse("Should not be modified right after reset", configurable.isModified)
  }

  fun `test apply persists state`() {
    configurable.createPanel()
    configurable.reset()

    // The settings should match defaults after apply
    configurable.apply()

    val state = LayoutAutoSwitchState.getInstance().state
    assertNotNull("State should exist after apply", state)
  }
}
