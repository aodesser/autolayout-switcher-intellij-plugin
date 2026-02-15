package com.aodesser.intellijlayoutplugin.settings

import com.aodesser.intellijlayoutplugin.runtime.LayoutActionCatalog
import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.dsl.builder.panel
import javax.swing.DefaultComboBoxModel
import javax.swing.JComboBox
import javax.swing.JSpinner
import javax.swing.SpinnerNumberModel

class LayoutAutoSwitchConfigurable : BoundConfigurable("AutoLayout Switcher") {
  private val service = LayoutAutoSwitchState.getInstance()

  private lateinit var enabledCheckBox: JBCheckBox
  private lateinit var notificationsCheckBox: JBCheckBox
  private lateinit var pollingSpinner: JSpinner
  private lateinit var laptopCombo: JComboBox<LayoutOption>
  private lateinit var singleExternalCombo: JComboBox<LayoutOption>
  private lateinit var multiExternalCombo: JComboBox<LayoutOption>

  private data class LayoutOption(val actionId: String, val label: String) {
    override fun toString(): String = label
  }

  override fun createPanel(): DialogPanel {
    val options = discoverLayoutOptions()
    return panel {
      group("Behavior") {
        row {
          enabledCheckBox = checkBox("Automatically switch layout").component
        }
        row {
          notificationsCheckBox = checkBox("Show notifications").component
        }
        row("Polling interval (seconds)") {
          pollingSpinner = spinner(2..60, 1).component
        }
      }

      group("Window Layout Mapping") {
        row("IDE on laptop display") {
          laptopCombo = comboBox(options.toList()).component
        }
        row("IDE on external (single)") {
          singleExternalCombo = comboBox(options.toList()).component
        }
        row("IDE on external (multi)") {
          multiExternalCombo = comboBox(options.toList()).component
        }
        row {
          label(
            "When laptop + external are connected, moving the IDE window between screens switches context."
          )
        }
      }
    }
  }

  override fun reset() {
    val state = service.state
    val options = discoverLayoutOptions(
      state.laptopActionId,
      state.singleExternalActionId,
      state.multiExternalActionId
    )
    laptopCombo.model = DefaultComboBoxModel(options.toTypedArray())
    singleExternalCombo.model = DefaultComboBoxModel(options.toTypedArray())
    multiExternalCombo.model = DefaultComboBoxModel(options.toTypedArray())

    enabledCheckBox.isSelected = state.enabled
    notificationsCheckBox.isSelected = state.showNotifications
    pollingSpinner.model = SpinnerNumberModel(state.pollingSeconds.coerceIn(2, 60), 2, 60, 1)
    selectByActionId(laptopCombo, state.laptopActionId)
    selectByActionId(singleExternalCombo, state.singleExternalActionId)
    selectByActionId(multiExternalCombo, state.multiExternalActionId)
  }

  override fun isModified(): Boolean {
    val state = service.state
    return enabledCheckBox.isSelected != state.enabled ||
      notificationsCheckBox.isSelected != state.showNotifications ||
      (pollingSpinner.value as Int) != state.pollingSeconds ||
      selectedActionId(laptopCombo) != state.laptopActionId ||
      selectedActionId(singleExternalCombo) != state.singleExternalActionId ||
      selectedActionId(multiExternalCombo) != state.multiExternalActionId
  }

  override fun apply() {
    val state = service.state
    val oldPolling = state.pollingSeconds
    state.enabled = enabledCheckBox.isSelected
    state.showNotifications = notificationsCheckBox.isSelected
    state.pollingSeconds = (pollingSpinner.value as Int).coerceIn(2, 60)
    state.laptopActionId = selectedActionId(laptopCombo)
    state.singleExternalActionId = selectedActionId(singleExternalCombo)
    state.multiExternalActionId = selectedActionId(multiExternalCombo)

    // Restart the monitor so new polling interval takes effect immediately.
    if (oldPolling != state.pollingSeconds) {
      com.aodesser.intellijlayoutplugin.runtime.LayoutMonitorService.getInstance().start()
    }
  }

  private fun selectedActionId(comboBox: JComboBox<LayoutOption>): String {
    return (comboBox.selectedItem as? LayoutOption)?.actionId.orEmpty()
  }

  private fun selectByActionId(comboBox: JComboBox<LayoutOption>, actionId: String) {
    val preferredKeys = preferredKeys(actionId)
    if (preferredKeys.isEmpty()) {
      comboBox.selectedIndex = 0
      return
    }

    val model = comboBox.model
    for (key in preferredKeys) {
      for (i in 0 until model.size) {
        val option = model.getElementAt(i)
        if (option.actionId == key) {
          comboBox.selectedIndex = i
          return
        }
      }
    }
    comboBox.selectedIndex = 0
  }

  private fun preferredKeys(raw: String): List<String> {
    val value = raw.trim()
    if (value.isBlank()) return emptyList()

    val keys = linkedSetOf<String>()
    keys.add(value)

    if (value.startsWith("name:")) {
      val legacyName = value.removePrefix("name:").trim()
      if (legacyName.isNotBlank()) {
        keys.add(LayoutActionCatalog.namedLayoutKey(legacyName))
        keys.add(legacyName)
      }
    } else if (!value.startsWith("namedLayout:")) {
      keys.add(LayoutActionCatalog.namedLayoutKey(value))
    }
    return keys.toList()
  }

  private fun discoverLayoutOptions(vararg forceIncludeIds: String): List<LayoutOption> {
    val options = linkedMapOf<String, LayoutOption>()
    LayoutActionCatalog.discoverChoices().forEach { choice ->
      options[choice.key] = LayoutOption(choice.key, choice.label)
    }

    forceIncludeIds.filter { it.isNotBlank() }.forEach { actionId ->
      if (!options.containsKey(actionId)) {
        options[actionId] = LayoutOption(actionId, "$actionId (unavailable)")
      }
    }

    return options.values.toList()
  }
}
