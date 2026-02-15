package com.aodesser.intellijlayoutplugin.settings

import com.aodesser.intellijlayoutplugin.runtime.ScreenContext
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for [LayoutAutoSwitchState] and its inner [LayoutAutoSwitchState.State] data class.
 *
 * These tests exercise the pure logic (default values, actionFor mapping, loadState)
 * without requiring the IntelliJ application container.
 */
class LayoutAutoSwitchStateTest {

  // ===================== Default state values =====================

  @Test
  fun `default state has enabled true`() {
    val state = LayoutAutoSwitchState.State()
    assertTrue(state.enabled)
  }

  @Test
  fun `default state has notifications enabled`() {
    val state = LayoutAutoSwitchState.State()
    assertTrue(state.showNotifications)
  }

  @Test
  fun `default polling interval is 4 seconds`() {
    val state = LayoutAutoSwitchState.State()
    assertEquals(4, state.pollingSeconds)
  }

  @Test
  fun `default laptop action is empty`() {
    val state = LayoutAutoSwitchState.State()
    assertEquals("", state.laptopActionId)
  }

  @Test
  fun `default single external action is empty`() {
    val state = LayoutAutoSwitchState.State()
    assertEquals("", state.singleExternalActionId)
  }

  @Test
  fun `default multi external action is RestoreDefaultLayout`() {
    val state = LayoutAutoSwitchState.State()
    assertEquals("RestoreDefaultLayout", state.multiExternalActionId)
  }

  // ===================== actionFor mapping =====================

  @Test
  fun `actionFor LAPTOP_ONLY returns laptopActionId`() {
    val service = LayoutAutoSwitchState()
    service.loadState(LayoutAutoSwitchState.State(laptopActionId = "myLaptopLayout"))
    assertEquals("myLaptopLayout", service.actionFor(ScreenContext.LAPTOP_ONLY))
  }

  @Test
  fun `actionFor SINGLE_EXTERNAL returns singleExternalActionId`() {
    val service = LayoutAutoSwitchState()
    service.loadState(LayoutAutoSwitchState.State(singleExternalActionId = "singleLayout"))
    assertEquals("singleLayout", service.actionFor(ScreenContext.SINGLE_EXTERNAL))
  }

  @Test
  fun `actionFor MULTI_EXTERNAL returns multiExternalActionId`() {
    val service = LayoutAutoSwitchState()
    service.loadState(LayoutAutoSwitchState.State(multiExternalActionId = "multiLayout"))
    assertEquals("multiLayout", service.actionFor(ScreenContext.MULTI_EXTERNAL))
  }

  @Test
  fun `actionFor returns empty string for contexts with no action configured`() {
    val service = LayoutAutoSwitchState()
    service.loadState(LayoutAutoSwitchState.State())
    assertEquals("", service.actionFor(ScreenContext.LAPTOP_ONLY))
    assertEquals("", service.actionFor(ScreenContext.SINGLE_EXTERNAL))
  }

  // ===================== loadState / getState =====================

  @Test
  fun `loadState replaces the current state`() {
    val service = LayoutAutoSwitchState()
    val customState = LayoutAutoSwitchState.State(
      enabled = false,
      showNotifications = false,
      pollingSeconds = 10,
      laptopActionId = "laptop",
      singleExternalActionId = "single",
      multiExternalActionId = "multi"
    )
    service.loadState(customState)
    val retrieved = service.state
    assertFalse(retrieved.enabled)
    assertFalse(retrieved.showNotifications)
    assertEquals(10, retrieved.pollingSeconds)
    assertEquals("laptop", retrieved.laptopActionId)
    assertEquals("single", retrieved.singleExternalActionId)
    assertEquals("multi", retrieved.multiExternalActionId)
  }

  @Test
  fun `getState returns the same instance loaded`() {
    val service = LayoutAutoSwitchState()
    val customState = LayoutAutoSwitchState.State(pollingSeconds = 30)
    service.loadState(customState)
    assertSame(customState, service.state)
  }

  // ===================== State data class copy =====================

  @Test
  fun `State data class copy produces independent object`() {
    val original = LayoutAutoSwitchState.State(pollingSeconds = 5, laptopActionId = "a")
    val copy = original.copy(pollingSeconds = 15)
    assertEquals(5, original.pollingSeconds)
    assertEquals(15, copy.pollingSeconds)
    assertEquals("a", copy.laptopActionId)
  }

  // ===================== All contexts covered =====================

  @Test
  fun `actionFor covers every ScreenContext value`() {
    val service = LayoutAutoSwitchState()
    service.loadState(LayoutAutoSwitchState.State(
      laptopActionId = "L",
      singleExternalActionId = "S",
      multiExternalActionId = "M"
    ))
    // Ensure all enum values are handled without exception
    for (ctx in ScreenContext.entries) {
      val result = service.actionFor(ctx)
      assertNotNull("actionFor($ctx) should not return null", result)
    }
  }
}
