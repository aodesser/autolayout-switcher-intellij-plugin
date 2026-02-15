package com.aodesser.intellijlayoutplugin.settings

import com.aodesser.intellijlayoutplugin.runtime.ScreenContext
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

@State(name = "LayoutAutoSwitchState", storages = [Storage("layout-auto-switcher.xml")])
@Service(Service.Level.APP)
class LayoutAutoSwitchState : PersistentStateComponent<LayoutAutoSwitchState.State> {
  data class State(
    var enabled: Boolean = true,
    var showNotifications: Boolean = true,
    var pollingSeconds: Int = 4,
    var laptopActionId: String = "",
    var singleExternalActionId: String = "",
    var multiExternalActionId: String = "RestoreDefaultLayout"
  )

  private var state = State()

  override fun getState(): State = state

  override fun loadState(state: State) {
    this.state = state
  }

  fun actionFor(context: ScreenContext): String = when (context) {
    ScreenContext.LAPTOP_ONLY -> state.laptopActionId
    ScreenContext.SINGLE_EXTERNAL -> state.singleExternalActionId
    ScreenContext.MULTI_EXTERNAL -> state.multiExternalActionId
  }

  companion object {
    fun getInstance(): LayoutAutoSwitchState = ApplicationManager.getApplication().getService(LayoutAutoSwitchState::class.java)
  }
}
