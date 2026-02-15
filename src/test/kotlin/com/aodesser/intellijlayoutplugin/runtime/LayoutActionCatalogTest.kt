package com.aodesser.intellijlayoutplugin.runtime

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for the key-parsing and key-building utilities in [LayoutActionCatalog].
 *
 * These methods are pure string logic and do not require the IntelliJ platform,
 * except for `resolveNamedLayoutName` which calls ToolWindowDefaultLayoutManager.
 * We test that one only in integration tests.
 */
class LayoutActionCatalogTest {

  // ===================== namedLayoutKey =====================

  @Test
  fun `namedLayoutKey produces correct prefix`() {
    assertEquals("namedLayout:MyLayout", LayoutActionCatalog.namedLayoutKey("MyLayout"))
  }

  @Test
  fun `namedLayoutKey with empty name produces prefix only`() {
    assertEquals("namedLayout:", LayoutActionCatalog.namedLayoutKey(""))
  }

  @Test
  fun `namedLayoutKey preserves spaces`() {
    assertEquals("namedLayout:My Layout Name", LayoutActionCatalog.namedLayoutKey("My Layout Name"))
  }

  @Test
  fun `namedLayoutKey preserves special characters`() {
    assertEquals("namedLayout:Layout (2)", LayoutActionCatalog.namedLayoutKey("Layout (2)"))
  }

  @Test
  fun `namedLayoutKey round trips with resolveNamedLayoutName prefix stripping`() {
    val name = "DualMonitor"
    val key = LayoutActionCatalog.namedLayoutKey(name)
    // The key starts with namedLayout: and the rest is the name
    assertTrue(key.startsWith("namedLayout:"))
    assertEquals(name, key.removePrefix("namedLayout:"))
  }

  // ===================== findActionByKey (partial - blank key) =====================

  @Test
  fun `findActionByKey returns null for blank key`() {
    // findActionByKey("") should return null without needing ActionManager
    assertNull(LayoutActionCatalog.findActionByKey(""))
  }

  @Test
  fun `findActionByKey returns null for whitespace-only key`() {
    assertNull(LayoutActionCatalog.findActionByKey("   "))
  }

  // ===================== Choice data class =====================

  @Test
  fun `Choice data class holds key and label`() {
    val choice = LayoutActionCatalog.Choice("someKey", "Some Label")
    assertEquals("someKey", choice.key)
    assertEquals("Some Label", choice.label)
  }

  @Test
  fun `Choice equality is structural`() {
    val a = LayoutActionCatalog.Choice("k", "l")
    val b = LayoutActionCatalog.Choice("k", "l")
    assertEquals(a, b)
  }

  @Test
  fun `Choice copy with different key produces different object`() {
    val a = LayoutActionCatalog.Choice("k1", "label")
    val b = a.copy(key = "k2")
    assertNotEquals(a, b)
    assertEquals("k2", b.key)
    assertEquals("label", b.label)
  }
}
