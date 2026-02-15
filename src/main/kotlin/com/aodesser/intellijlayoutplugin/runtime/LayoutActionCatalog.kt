package com.aodesser.intellijlayoutplugin.runtime

import com.intellij.ide.actions.RestoreNamedLayoutActionGroup
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.toolWindow.ToolWindowDefaultLayoutManager

object LayoutActionCatalog {
  data class Choice(
    val key: String,
    val label: String
  )

  fun discoverChoices(): List<Choice> {
    val actionManager = ActionManager.getInstance()
    val choices = linkedMapOf<String, Choice>()
    choices[""] = Choice("", "<Do nothing>")

    // Stable built-in option.
    val restoreDefault = actionManager.getAction("RestoreDefaultLayout")
    if (restoreDefault != null) {
      choices["RestoreDefaultLayout"] = Choice(
        "RestoreDefaultLayout",
        restoreDefault.templateText?.trim().takeUnless { it.isNullOrBlank() } ?: "Restore Default Layout"
      )
    }

    // User-saved named layouts.
    ToolWindowDefaultLayoutManager.getInstance().getLayoutNames()
      .sortedBy { it.lowercase() }
      .forEach { layoutName ->
        choices[namedLayoutKey(layoutName)] = Choice(namedLayoutKey(layoutName), layoutName)
      }

    return choices.values.toList()
  }

  fun findActionByKey(key: String, dataContext: DataContext? = null): Pair<AnAction, String>? {
    if (key.isBlank()) return null

    if (!key.startsWith(NAMED_LAYOUT_PREFIX) && !key.startsWith(LEGACY_NAME_PREFIX)) {
      val asNamed = findRestoreNamedLayoutAction(key, dataContext)
      if (asNamed != null) return asNamed to namedLayoutKey(key)

      val action = ActionManager.getInstance().getAction(key) ?: return null
      return action to key
    }

    val layoutName = key
      .removePrefix(NAMED_LAYOUT_PREFIX)
      .removePrefix(LEGACY_NAME_PREFIX)
      .trim()
    if (layoutName.isBlank()) return null

    val action = findRestoreNamedLayoutAction(layoutName, dataContext) ?: return null
    return action to key
  }

  fun namedLayoutKey(layoutName: String): String = "$NAMED_LAYOUT_PREFIX$layoutName"

  fun resolveNamedLayoutName(key: String): String? {
    val raw = key.trim()
    if (raw.isBlank()) return null

    val requested = when {
      raw.startsWith(NAMED_LAYOUT_PREFIX) -> raw.removePrefix(NAMED_LAYOUT_PREFIX).trim()
      raw.startsWith(LEGACY_NAME_PREFIX) -> raw.removePrefix(LEGACY_NAME_PREFIX).trim()
      else -> raw
    }
    if (requested.isBlank()) return null

    return ToolWindowDefaultLayoutManager.getInstance().getLayoutNames().firstOrNull {
      it.equals(requested, ignoreCase = true)
    }
  }

  private fun findRestoreNamedLayoutAction(layoutName: String, dataContext: DataContext?): AnAction? {
    val group = RestoreNamedLayoutActionGroup()
    val event = dataContext?.let { AnActionEvent.createFromAnAction(group, null, ActionPlaces.UNKNOWN, it) }
    val children = group.getChildren(event)
    return children.firstOrNull { child ->
      child.templateText?.trim().equals(layoutName, ignoreCase = true)
    }
  }

  private const val NAMED_LAYOUT_PREFIX = "namedLayout:"
  private const val LEGACY_NAME_PREFIX = "name:"
}
