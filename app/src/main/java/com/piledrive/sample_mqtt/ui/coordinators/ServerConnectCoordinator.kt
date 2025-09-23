package com.piledrive.sample_mqtt.ui.coordinators

import com.piledrive.sample_mqtt.mqtt.model.MqttConnectionStatus
import com.piledrive.sample_mqtt.mqtt.client.MqttClientImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID

interface ServerConnectCoordinatorImpl {
	val coroutineScope: CoroutineScope
	val mqtt: MqttClientImpl
	val serverUrlState: StateFlow<String>
	val serverPortState: StateFlow<Int>
	val clientIdState: StateFlow<String>
	val usernameState: StateFlow<String>
	val passwordState: StateFlow<String>
	val isActiveState: StateFlow<Boolean>
	val connectionState: StateFlow<MqttConnectionStatus>
	val onServerUrlUpdated: (String) -> Unit
	val onServerPortUpdated: (Int) -> Unit
	val onClientIdUpdated: (String) -> Unit
	val onUsernameUpdated: (String) -> Unit
	val onPasswordUpdated: (String) -> Unit
	fun attemptConnect()
	fun attemptDisconnect()
}

class ServerConnectCoordinator(
	override val coroutineScope: CoroutineScope,
	override val mqtt: MqttClientImpl,
	initServerUrl: String = "broker.hivemq.com",
	initServerPort: Int = 1883,
	initClientId: String = "",
	initUsername: String = "test90734609-72456987-456",
	initPassword: String = "",
	initIsActive: Boolean = false,
) : ServerConnectCoordinatorImpl {
	private val _serverUrlState: MutableStateFlow<String> = MutableStateFlow(initServerUrl)
	override val serverUrlState: StateFlow<String> = _serverUrlState

	private val _serverPortState: MutableStateFlow<Int> = MutableStateFlow(initServerPort)
	override val serverPortState: StateFlow<Int> = _serverPortState

	private val _clientIdState: MutableStateFlow<String> = MutableStateFlow(initClientId)
	override val clientIdState: StateFlow<String> = _clientIdState

	private val _usernameState: MutableStateFlow<String> = MutableStateFlow(initUsername)
	override val usernameState: StateFlow<String> = _usernameState

	private val _passwordState: MutableStateFlow<String> = MutableStateFlow(initPassword)
	override val passwordState: StateFlow<String> = _passwordState

	private val _isActiveState: MutableStateFlow<Boolean> = MutableStateFlow(initIsActive)
	override val isActiveState: StateFlow<Boolean> = _isActiveState

	override val connectionState: StateFlow<MqttConnectionStatus> = mqtt.connectionStateFlow

	override val onServerUrlUpdated: (String) -> Unit = { _serverUrlState.value = it }
	override val onServerPortUpdated: (Int) -> Unit = { _serverPortState.value = it }
	override val onClientIdUpdated: (String) -> Unit = { _clientIdState.value = it }
	override val onUsernameUpdated: (String) -> Unit = { _usernameState.value = it }
	override val onPasswordUpdated: (String) -> Unit = { _passwordState.value = it }

	init {
		coroutineScope.launch {
			mqtt.connectionStateFlow.collect { state ->
				_isActiveState.value = state in listOf(MqttConnectionStatus.IDLE, MqttConnectionStatus.CLIENT_DISCONNECT, MqttConnectionStatus.SERVER_DISCONNECT)
			}
		}
	}

	override fun attemptConnect() {
		val url = serverUrlState.value
		val finalUrl = if(url.startsWith("tcp://")) url else "tcp://$url"
		_serverUrlState.value = finalUrl

		val clientId = clientIdState.value
		val finalId = clientId.ifBlank { UUID.randomUUID().toString() }
		_clientIdState.value = finalId

		val user = usernameState.value
		val finalUser = user.ifBlank { UUID.randomUUID().toString() }
		_usernameState.value = finalUser

		mqtt.connect(
			url = finalUrl,
			port = serverPortState.value,
			clientId = finalId,
			user = finalUser,
			pw = passwordState.value
		)
	}

	override fun attemptDisconnect() {
		mqtt.disconnect()
	}
}