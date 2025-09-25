package com.piledrive.sample_mqtt.mqtt

import com.piledrive.sample_mqtt.mqtt.client.MqttClientImpl
import com.piledrive.sample_mqtt.mqtt.model.MqttClientError
import com.piledrive.sample_mqtt.mqtt.model.MqttConnectionStatus
import com.piledrive.sample_mqtt.mqtt.model.MqttGenericMessage
import com.piledrive.sample_mqtt.mqtt.model.MqttGenericTopic
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MqttController @Inject constructor(
	val clientImpl: MqttClientImpl
) : MqttClientImpl {

	override suspend fun connect(
		url: String,
		port: Int,
		clientId: String,
		user: String,
		pw: String
	) {
		Timber.d("> Connecting to | url: $url | port: $port | clientId: $clientId | user: $user | pw | $pw")
		clientImpl.connect(url, port, clientId, user, pw)
	}

	override suspend fun disconnect() {
		Timber.d("> Disconnecting from server")
		clientImpl.disconnect()
	}

	override suspend fun subscribe(topic: String, qos: Int) {
		Timber.d("> Starting subscribe attempt")
		clientImpl.subscribe(topic, qos)
	}


	override suspend fun unsubscribe(topic: String) = clientImpl.unsubscribe(topic)

	override suspend fun publish(topic: String, msg: String, qos: Int, retained: Boolean) =
		clientImpl.publish(topic, msg, qos, retained)

	override fun clearState() = clientImpl.clearState()

	override val connectionStateFlow: StateFlow<MqttConnectionStatus> = clientImpl.connectionStateFlow
	override val subscribedTopicsStateFlow: StateFlow<List<MqttGenericTopic>> = clientImpl.subscribedTopicsStateFlow
	override val latestMessageStateFlow: StateFlow<MqttGenericMessage?> = clientImpl.latestMessageStateFlow
	override val clientErrorFlow: Flow<MqttClientError> = clientImpl.clientErrorFlow
}