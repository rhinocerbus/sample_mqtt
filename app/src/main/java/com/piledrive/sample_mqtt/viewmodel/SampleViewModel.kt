package com.piledrive.sample_mqtt.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.piledrive.sample_mqtt.mqtt.model.MqttConnectionStatus
import com.piledrive.sample_mqtt.mqtt.controller.MqttController
import com.piledrive.sample_mqtt.ui.coordinators.MessagesTabCoordinator
import com.piledrive.sample_mqtt.ui.coordinators.ServerTabCoordinator
import com.piledrive.sample_mqtt.ui.coordinators.TabDestination
import com.piledrive.sample_mqtt.ui.coordinators.TabDestinations
import com.piledrive.sample_mqtt.ui.coordinators.TabsCoordinator
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SampleViewModel @Inject constructor(
	private val mqtt: MqttController
) : ViewModel() {

	val serverTabCoordinator = ServerTabCoordinator(
		coroutineScope = viewModelScope,
		mqtt = mqtt
	)

	val messagesTabCoordinator = MessagesTabCoordinator(
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
		connectionStatusSourceState = serverTabCoordinator.connectionState,
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
