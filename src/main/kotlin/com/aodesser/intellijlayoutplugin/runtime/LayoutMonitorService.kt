package com.aodesser.intellijlayoutplugin.runtime

import com.aodesser.intellijlayoutplugin.settings.LayoutAutoSwitchState
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.util.concurrency.AppExecutorUtil
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

class LayoutMonitorService : Disposable {
  private var lastContext: ScreenContext? = null
  private var monitorFuture: ScheduledFuture<*>? = null

  fun start() {
    val app = ApplicationManager.getApplication()
    if (app.isDisposed) return

    stop()
    val settings = LayoutAutoSwitchState.getInstance().state
    val interval = settings.pollingSeconds.coerceIn(2, 60).toLong()

    monitorFuture = AppExecutorUtil.getAppScheduledExecutorService().scheduleWithFixedDelay(
      {
        if (!settings.enabled) return@scheduleWithFixedDelay
        val current = ScreenContextDetector.detect()
        if (current != lastContext) {
          lastContext = current
          LayoutActionExecutor.execute(LayoutAutoSwitchState.getInstance().actionFor(current), current)
        }
      },
      2,
      interval,
      TimeUnit.SECONDS
    )
  }

  fun stop() {
    monitorFuture?.cancel(false)
    monitorFuture = null
  }

  override fun dispose() {
    stop()
  }
}
