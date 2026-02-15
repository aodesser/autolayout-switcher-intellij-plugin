package com.aodesser.intellijlayoutplugin.runtime

import com.aodesser.intellijlayoutplugin.settings.LayoutAutoSwitchState
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.util.concurrency.AppExecutorUtil
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

@Service(Service.Level.APP)
class LayoutMonitorService : Disposable {
  @Volatile
  private var lastContext: ScreenContext? = null

  private val monitorRef = AtomicReference<ScheduledFuture<*>>(null)

  fun start() {
    val app = ApplicationManager.getApplication()
    if (app.isDisposed) return

    stop()
    val interval = LayoutAutoSwitchState.getInstance().state.pollingSeconds.coerceIn(2, 60).toLong()

    val future = AppExecutorUtil.getAppScheduledExecutorService().scheduleWithFixedDelay(
      {
        val settings = LayoutAutoSwitchState.getInstance().state
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
    monitorRef.set(future)
  }

  fun stop() {
    monitorRef.getAndSet(null)?.cancel(false)
  }

  override fun dispose() {
    stop()
  }

  companion object {
    fun getInstance(): LayoutMonitorService =
      ApplicationManager.getApplication().getService(LayoutMonitorService::class.java)
  }
}
