package com.piledrive.sample_mqtt.mqtt.client

import com.piledrive.sample_mqtt.mqtt.model.MqttClientError
import com.piledrive.sample_mqtt.mqtt.model.MqttConnectionStatus
import com.piledrive.sample_mqtt.mqtt.model.MqttGenericMessage
import com.piledrive.sample_mqtt.mqtt.model.MqttGenericTopic
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
	fun clearState()

	val connectionStateFlow: StateFlow<MqttConnectionStatus>
	val subscribedTopicsStateFlow: StateFlow<List<MqttGenericTopic>>
	val latestMessageStateFlow: StateFlow<MqttGenericMessage?>
	val clientErrorFlow: Flow<MqttClientError>
}

