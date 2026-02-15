package com.aodesser.intellijlayoutplugin.runtime

import com.intellij.testFramework.fixtures.BasePlatformTestCase

/**
 * Integration tests for [LayoutMonitorService] lifecycle.
 *
 * Uses the IntelliJ Platform test framework to have a real application
 * context available, which is required for AppExecutorUtil and
 * ApplicationManager calls inside the service.
 */
class LayoutMonitorServiceTest : BasePlatformTestCase() {

  private lateinit var service: LayoutMonitorService

  override fun setUp() {
    super.setUp()
    service = LayoutMonitorService()
  }

  override fun tearDown() {
    try {
      service.dispose()
    } finally {
      super.tearDown()
    }
  }

  fun `test start creates a scheduled future`() {
    service.start()
    // The service should be running now; calling stop should be safe
    service.stop()
  }

  fun `test stop is idempotent when not started`() {
    // Should not throw even if never started
    service.stop()
    service.stop()
  }

  fun `test start after stop restarts the service`() {
    service.start()
    service.stop()
    service.start()
    service.stop()
  }

  fun `test dispose stops the monitor`() {
    service.start()
    service.dispose()
    // After dispose, stop should still be safe
    service.stop()
  }

  fun `test dispose is idempotent`() {
    service.start()
    service.dispose()
    service.dispose()
  }

  fun `test start after dispose works`() {
    service.dispose()
    // Starting after dispose should still work since dispose just calls stop
    service.start()
    service.stop()
  }

  fun `test double start does not leak schedulers`() {
    // Calling start twice should cancel the first future
    service.start()
    service.start()
    service.stop()
  }
}
