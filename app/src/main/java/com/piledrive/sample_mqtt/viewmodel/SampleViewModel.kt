package com.piledrive.sample_mqtt.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.piledrive.sample_mqtt.mqtt.controller.MqttController
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

	val tabsCoordinator = TabsCoordinator(
		listOf(
			TabDestination(TabDestinations.SERVER, enabled = true),
			TabDestination(TabDestinations.MESSAGES, enabled = true)
		),
		TabDestinations.SERVER
	)

	val serverConnectCoordinator = ServerConnectCoordinator(
		coroutineScope = viewModelScope,
		mqtt = mqtt
	)
}
