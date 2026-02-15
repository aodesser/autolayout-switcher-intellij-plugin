package com.aodesser.intellijlayoutplugin.runtime

import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity

class LayoutAutoSwitchStartupActivity : ProjectActivity {
  override suspend fun execute(project: Project) {
    LayoutMonitorService.getInstance().start()
  }
}
