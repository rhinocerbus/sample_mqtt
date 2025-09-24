package com.piledrive.sample_mqtt.mqtt.client

import com.hivemq.client.mqtt.MqttClient
import com.piledrive.sample_mqtt.mqtt.model.MqttClientError
import com.piledrive.sample_mqtt.mqtt.model.MqttConnectionStatus
import com.piledrive.sample_mqtt.mqtt.model.MqttGenericMessage
import com.piledrive.sample_mqtt.mqtt.model.MqttGenericTopic
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

	override fun clearState() {
		TODO("Not yet implemented")
	}

	override val connectionStateFlow: StateFlow<MqttConnectionStatus>
	override val subscribedTopicsStateFlow: StateFlow<List<MqttGenericTopic>>
	override val latestMessageStateFlow: StateFlow<MqttGenericMessage?>
	override val clientErrorFlow: Flow<MqttClientError>
}
