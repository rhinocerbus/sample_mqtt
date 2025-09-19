package com.piledrive.sample_mqtt.mqtt.client

import com.piledrive.sample_mqtt.model.ClientError
import com.piledrive.sample_mqtt.model.GenericMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface MqttClientImpl {
	fun connect(url: String, port: Int, clientId: String, user: String = "", pw: String = "")
	fun disconnect()
	fun subscribe(topic: String, qos: Int = 1)
	fun unsubscribe(topic: String)
	fun publish(
		topic: String,
		msg: String,
		qos: Int = 1,
		retained: Boolean = false,
	)

	val connectionStateFlow: StateFlow<ConnectionStatus>
	val latestMessageStateFlow: StateFlow<GenericMessage?>
	val clientErrorFlow: Flow<ClientError>
}

