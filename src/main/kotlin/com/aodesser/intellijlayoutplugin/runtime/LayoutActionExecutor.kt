package com.aodesser.intellijlayoutplugin.runtime

import com.intellij.ide.DataManager
import com.intellij.notification.Notification
import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.actionSystem.ex.ActionUtil
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.wm.ex.ToolWindowManagerEx
import com.intellij.toolWindow.ToolWindowDefaultLayoutManager
import com.aodesser.intellijlayoutplugin.settings.LayoutAutoSwitchState

object LayoutActionExecutor {
  fun execute(actionKey: String, context: ScreenContext) {
    if (actionKey.isBlank()) return

    DataManager.getInstance().dataContextFromFocusAsync.onSuccess { dataContext ->
      val actionManager = ActionManager.getInstance()
      val actionFromId = actionManager.getAction(actionKey)
      val namedLayout = LayoutActionCatalog.resolveNamedLayoutName(actionKey)
      val isExplicitNamed = actionKey.startsWith("namedLayout:") || actionKey.startsWith("name:")

      if (namedLayout != null && (isExplicitNamed || actionFromId == null)) {
        applyNamedLayout(namedLayout, dataContext, context)
        return@onSuccess
      }

      val resolved = LayoutActionCatalog.findActionByKey(actionKey, dataContext)
      if (resolved == null) {
        notify("No matching layout/action found for ${context.displayName()}.", NotificationType.WARNING)
        return@onSuccess
      }
      val (action, resolvedKey) = resolved

      val event = AnActionEvent.createFromAnAction(action, null, ActionPlaces.UNKNOWN, dataContext)
      ActionUtil.performActionDumbAwareWithCallbacks(action, event)
      val actionLabel = action.templateText?.trim().takeUnless { it.isNullOrBlank() } ?: resolvedKey
      notify("Switched ${context.displayName()} -> $actionLabel", NotificationType.INFORMATION)
    }.onError {
      notify("Couldn't determine IDE context to apply layout.", NotificationType.WARNING)
    }
  }

  private fun applyNamedLayout(layoutName: String, dataContext: com.intellij.openapi.actionSystem.DataContext, context: ScreenContext) {
    val project = CommonDataKeys.PROJECT.getData(dataContext)
      ?: ProjectManager.getInstance().openProjects.firstOrNull { !it.isDisposed }

    if (project == null) {
      notify("Can't apply '$layoutName': no open project.", NotificationType.WARNING)
      return
    }

    ApplicationManager.getApplication().invokeLater {
      val layoutManager = ToolWindowDefaultLayoutManager.getInstance()
      layoutManager.activeLayoutName = layoutName
      ToolWindowManagerEx.getInstanceEx(project).setLayout(layoutManager.getLayoutCopy())
      notify("Switched ${context.displayName()} -> $layoutName", NotificationType.INFORMATION)
    }
  }

  private fun notify(message: String, type: NotificationType) {
    val state = LayoutAutoSwitchState.getInstance().state
    if (!state.showNotifications) return

    val notification = Notification(NOTIFICATION_GROUP_ID, PLUGIN_TITLE, message, type)
    notification.addAction(
      NotificationAction.createSimpleExpiring("Don't show again") {
        LayoutAutoSwitchState.getInstance().state.showNotifications = false
      }
    )
    Notifications.Bus.notify(notification)
  }

  private fun ScreenContext.displayName(): String = when (this) {
    ScreenContext.LAPTOP_ONLY -> "Laptop only"
    ScreenContext.SINGLE_EXTERNAL -> "Single external display"
    ScreenContext.MULTI_EXTERNAL -> "Multiple external displays"
  }

  private const val NOTIFICATION_GROUP_ID = "AutoLayout Switcher"
  private const val PLUGIN_TITLE = "AutoLayout Switcher"
}
