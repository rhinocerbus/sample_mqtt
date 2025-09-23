package com.piledrive.sample_mqtt.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.piledrive.sample_mqtt.mqtt.model.MqttConnectionStatus
import com.piledrive.sample_mqtt.mqtt.controller.MqttController
import com.piledrive.sample_mqtt.ui.coordinators.MessagesCoordinator
import com.piledrive.sample_mqtt.ui.coordinators.ServerConnectCoordinator
import com.piledrive.sample_mqtt.ui.coordinators.TabDestination
import com.piledrive.sample_mqtt.ui.coordinators.TabDestinations
import com.piledrive.sample_mqtt.ui.coordinators.TabsCoordinator
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SampleViewModel @Inject constructor(
	private val mqtt: MqttController
) : ViewModel() {

	val serverConnectCoordinator = ServerConnectCoordinator(
		coroutineScope = viewModelScope,
		mqtt = mqtt
	)

	val messagesCoordinator = MessagesCoordinator(
		coroutineScope = viewModelScope,
		mqtt = mqtt
	)

	val tabsCoordinator = TabsCoordinator(
		viewModelScope,
		listOf(
			TabDestination(TabDestinations.SERVER, enabled = true),
			TabDestination(TabDestinations.MESSAGES, enabled = true)
		),
		0,
		connectionStatusSourceState = serverConnectCoordinator.connectionState,
		onConnectionStatusChanged = { status -> updateMessagesTag(status) }
	)

	private fun updateMessagesTag(status: MqttConnectionStatus) {
		tabsCoordinator.findTab(TabDestinations.MESSAGES)?.also {
			it.enabled = status == MqttConnectionStatus.CONNECTED
			if(it.enabled) {
				tabsCoordinator.changeTab(it.tab)
			}
		}
	}
}
