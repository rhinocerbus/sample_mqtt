package com.piledrive.sample_mqtt.mqtt.client

import com.piledrive.sample_mqtt.model.ClientError
import com.piledrive.sample_mqtt.model.ConnectionStatus
import com.piledrive.sample_mqtt.model.GenericMessage
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow

class PreviewDummyMqttClient(
	previewStatus: ConnectionStatus = ConnectionStatus.IDLE
): MqttClientImpl {
	override fun connect(
		url: String,
		port: Int,
		clientId: String,
		user: String,
		pw: String
	) {}

	override fun disconnect() {}

	override fun subscribe(topic: String, qos: Int) {}

	override fun unsubscribe(topic: String) {}

	override fun publish(topic: String, msg: String, qos: Int, retained: Boolean) {}

	override val connectionStateFlow: StateFlow<ConnectionStatus> = MutableStateFlow(previewStatus)
	override val latestMessageStateFlow: StateFlow<GenericMessage?> = MutableStateFlow(null)
	override val clientErrorFlow: Flow<ClientError> = Channel<ClientError>().receiveAsFlow()
}