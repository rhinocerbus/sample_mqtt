package com.piledrive.sample_mqtt.mqtt.client

import com.hivemq.client.mqtt.MqttClient
import com.piledrive.sample_mqtt.model.ClientError
import com.piledrive.sample_mqtt.model.ConnectionStatus
import com.piledrive.sample_mqtt.model.GenericMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow


class HiveMqttClient() : MqttClientImpl {

	private var client: MqttClient? = null

	override fun connect(url: String, port: Int, password: String, user: String, pw: String) {
		val safeClient = MqttClient.builder().identifier("").serverHost("").useMqttVersion3().build()

		client = safeClient
	}

	override fun disconnect() {
		TODO("Not yet implemented")
	}

	override fun subscribe(topic: String, qos: Int) {
		TODO("Not yet implemented")
	}

	override fun unsubscribe(topic: String) {
		TODO("Not yet implemented")
	}

	override fun publish(topic: String, msg: String, qos: Int, retained: Boolean) {
		TODO("Not yet implemented")
	}

	override val connectionStateFlow: StateFlow<ConnectionStatus>
		get() = TODO("Not yet implemented")
	override val latestMessageStateFlow: StateFlow<GenericMessage?>
		get() = TODO("Not yet implemented")
	override val clientErrorFlow: Flow<ClientError>
		get() = TODO("Not yet implemented")
}
