package com.piledrive.sample_mqtt.mqtt.controller

import com.piledrive.sample_mqtt.model.ClientError
import com.piledrive.sample_mqtt.model.GenericMessage
import com.piledrive.sample_mqtt.mqtt.client.ConnectionStatus
import com.piledrive.sample_mqtt.mqtt.client.MqttClientImpl
import com.piledrive.sample_mqtt.mqtt.client.PahoMqttClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Singleton

@Singleton
class MqttController() : MqttClientImpl {

	//private val clientImpl: MqttClientImpl
	private val clientImpl = PahoMqttClient()

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

	override val connectionStateFlow: StateFlow<ConnectionStatus> = clientImpl.connectionStateFlow
	override val latestMessageStateFlow: StateFlow<GenericMessage?> = clientImpl.latestMessageStateFlow
	override val clientErrorFlow: Flow<ClientError> = clientImpl.clientErrorFlow
}