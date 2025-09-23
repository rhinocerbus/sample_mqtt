package com.piledrive.sample_mqtt.ui.coordinators

import com.piledrive.sample_mqtt.mqtt.model.MqttConnectionStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.math.max


/*
	todo - make generic, move to components lib, make mqtt-specific offshoot to handle connection state toggling messages tab status
 */

enum class TabDestinations { SERVER, MESSAGES }

data class TabDestination(val tab: TabDestinations, var enabled: Boolean)

interface TabsCoordinatorImpl {
	val coroutineScope: CoroutineScope
	val tabsState: StateFlow<List<TabDestination>>
	val activeTabIdxState: StateFlow<Int>
	val activeTabState: StateFlow<TabDestination>
	fun changeTab(tab: TabDestinations)
	fun changeTab(tabIdx: Int)
	fun findTab(tab: TabDestinations): TabDestination?
}

class TabsCoordinator(
	override val coroutineScope: CoroutineScope,
	initTabs: List<TabDestination> = listOf(),
	initActiveTab: Int = 0,
	connectionStatusSourceState: StateFlow<MqttConnectionStatus>,
	val onConnectionStatusChanged: (status: MqttConnectionStatus) -> Unit = {}
) : TabsCoordinatorImpl {
	private val _tabsState: MutableStateFlow<List<TabDestination>> = MutableStateFlow(initTabs)
	override val tabsState: StateFlow<List<TabDestination>> = _tabsState

	private val _activeTabIdxState: MutableStateFlow<Int> = MutableStateFlow(initActiveTab)
	override val activeTabIdxState: StateFlow<Int> = _activeTabIdxState

	private val _activeTabState: MutableStateFlow<TabDestination> = MutableStateFlow(initTabs[initActiveTab])
	override val activeTabState: StateFlow<TabDestination> = _activeTabState

	init {
		coroutineScope.launch(Dispatchers.Default) {
			connectionStatusSourceState.collect { onConnectionStatusChanged(it) }
		}
	}

	override fun changeTab(tab: TabDestinations) {
		val tabIdx = max(tabsState.value.indexOfFirst(predicate = { it.tab.name == tab.name }), 0)
		_activeTabIdxState.value = tabIdx
		_activeTabState.value = tabsState.value[tabIdx]
	}

	override fun changeTab(tabIdx: Int) {
		_activeTabIdxState.value = tabIdx
		_activeTabState.value = tabsState.value[tabIdx]
	}

	override fun findTab(tab: TabDestinations): TabDestination? {
		return tabsState.value.firstOrNull { it.tab.name == tab.name }
	}
}