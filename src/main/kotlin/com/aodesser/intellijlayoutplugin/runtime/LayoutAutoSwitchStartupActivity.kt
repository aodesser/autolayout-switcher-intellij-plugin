package com.aodesser.intellijlayoutplugin.runtime

import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity

class LayoutAutoSwitchStartupActivity : StartupActivity.DumbAware {
  private val monitor = LayoutMonitorService()

  override fun runActivity(project: Project) {
    monitor.start()
  }
}
