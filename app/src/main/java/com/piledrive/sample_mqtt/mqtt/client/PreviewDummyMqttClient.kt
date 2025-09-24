package com.piledrive.sample_mqtt.mqtt.client

import com.piledrive.sample_mqtt.mqtt.model.MqttClientError
import com.piledrive.sample_mqtt.mqtt.model.MqttConnectionStatus
import com.piledrive.sample_mqtt.mqtt.model.MqttGenericMessage
import com.piledrive.sample_mqtt.mqtt.model.MqttGenericTopic
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow

class PreviewDummyMqttClient(
	previewStatus: MqttConnectionStatus = MqttConnectionStatus.IDLE
) : MqttClientImpl {
	override fun connect(
		url: String,
		port: Int,
		clientId: String,
		user: String,
		pw: String
	) {
	}

	override fun disconnect() {}

	override fun subscribe(topic: String, qos: Int) {}

	override fun unsubscribe(topic: String) {}

	override fun publish(topic: String, msg: String, qos: Int, retained: Boolean) {}

	override fun clearState() {}

	override val connectionStateFlow: StateFlow<MqttConnectionStatus> = MutableStateFlow(previewStatus)
	override val subscribedTopicsStateFlow: StateFlow<List<MqttGenericTopic>> = MutableStateFlow(listOf())
	override val latestMessageStateFlow: StateFlow<MqttGenericMessage?> = MutableStateFlow(null)
	override val clientErrorFlow: Flow<MqttClientError> = Channel<MqttClientError>().receiveAsFlow()
}