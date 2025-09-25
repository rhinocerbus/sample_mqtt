package com.piledrive.sample_mqtt.mqtt

import com.piledrive.sample_mqtt.mqtt.client.MqttClientImpl
import com.piledrive.sample_mqtt.mqtt.model.MqttClientError
import com.piledrive.sample_mqtt.mqtt.model.MqttConnectionStatus
import com.piledrive.sample_mqtt.mqtt.model.MqttGenericMessage
import com.piledrive.sample_mqtt.mqtt.model.MqttGenericTopic
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MqttController @Inject constructor(val clientImpl: MqttClientImpl) : MqttClientImpl {

	override fun connect(
		url: String,
		port: Int,
		clientId: String,
		user: String,
		pw: String
	) = clientImpl.connect(url, port, clientId, user, pw)

	override fun disconnect() = clientImpl.disconnect()

	override fun subscribe(topic: String, qos: Int) = clientImpl.subscribe(topic, qos)

	override fun unsubscribe(topic: String) = clientImpl.unsubscribe(topic)

	override fun publish(topic: String, msg: String, qos: Int, retained: Boolean) =
		clientImpl.publish(topic, msg, qos, retained)

	override fun clearState() = clientImpl.clearState()

	override val connectionStateFlow: StateFlow<MqttConnectionStatus> = clientImpl.connectionStateFlow
	override val subscribedTopicsStateFlow: StateFlow<List<MqttGenericTopic>> = clientImpl.subscribedTopicsStateFlow
	override val latestMessageStateFlow: StateFlow<MqttGenericMessage?> = clientImpl.latestMessageStateFlow
	override val clientErrorFlow: Flow<MqttClientError> = clientImpl.clientErrorFlow
}