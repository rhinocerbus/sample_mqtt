package com.piledrive.sample_mqtt.ui.coordinators

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.max


enum class TabDestinations { SERVER, MESSAGES }

data class TabDestination(val tab: TabDestinations, var enabled: Boolean)

interface TabsCoordinatorImpl {
	val tabsState: StateFlow<List<TabDestination>>
	val activeTabIdxState: StateFlow<Int>
	val activeTabState: StateFlow<TabDestination>
	fun changeTab(tab: TabDestinations)
	fun changeTab(tabIdx: Int)
}

class TabsCoordinator(
	initTabs: List<TabDestination> = listOf(),
	initActiveTab: Int = 0
) : TabsCoordinatorImpl {
	private val _tabsState: MutableStateFlow<List<TabDestination>> = MutableStateFlow(initTabs)
	override val tabsState: StateFlow<List<TabDestination>> = _tabsState

	private val _activeTabIdxState: MutableStateFlow<Int> = MutableStateFlow(initActiveTab)
	override val activeTabIdxState: StateFlow<Int> = _activeTabIdxState

	private val _activeTabState: MutableStateFlow<TabDestination> = MutableStateFlow(initTabs[initActiveTab])
	override val activeTabState: StateFlow<TabDestination> = _activeTabState


	override fun changeTab(tab: TabDestinations) {
		val tabIdx = max(tabsState.value.indexOfFirst(predicate = {it.tab.name == tab.name}), 0)
		_activeTabIdxState.value = tabIdx
	}
	override fun changeTab(tabIdx: Int) {
		_activeTabIdxState.value = tabIdx
	}
}