package com.piledrive.sample_mqtt.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.piledrive.sample_mqtt.mqtt.controller.MqttController
import com.piledrive.sample_mqtt.repo.SampleRepo
import com.piledrive.sample_mqtt.ui.coordinators.ServerConnectCoordinator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class SampleViewModel @Inject constructor(
	private val mqtt: MqttController
) : ViewModel() {

	val serverConnectCoordinator = ServerConnectCoordinator(
		coroutineScope = viewModelScope,
		mqtt = mqtt
	)


}
