package com.piledrive.sample_mqtt.mqtt.client

import com.piledrive.sample_mqtt.model.ClientError
import com.piledrive.sample_mqtt.model.ConnectionStatus
import com.piledrive.sample_mqtt.model.GenericMessage
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.IMqttMessageListener
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence


class PahoMqttClient() : MqttClientImpl {

	val actionListener = object : IMqttActionListener {
		override fun onSuccess(asyncActionToken: IMqttToken?) {
		}

		override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
		}
	}

	val messageListener = IMqttMessageListener { topic, message -> }

	val clientCallback = object : MqttCallback {
		override fun connectionLost(cause: Throwable?) {
		}

		override fun messageArrived(topic: String?, message: MqttMessage?) {
		}

		override fun deliveryComplete(token: IMqttDeliveryToken?) {
		}
	}


	//  region Client instance
	/////////////////////////////////////////////////

	private var client: MqttClient? = null

	private val _clientErrorChannel = Channel<ClientError>()
	override val clientErrorFlow: Flow<ClientError> = _clientErrorChannel.receiveAsFlow()

	/////////////////////////////////////////////////
	//  endregion


	//  region Connection
	/////////////////////////////////////////////////

	private val _connectionStateFlow: MutableStateFlow<ConnectionStatus> = MutableStateFlow(ConnectionStatus.IDLE)
	override val connectionStateFlow: StateFlow<ConnectionStatus> = _connectionStateFlow

	override fun connect(url: String, port: Int, clientId: String, user: String, pw: String) {
		val safeClient = MqttClient("$url:$port", clientId, MemoryPersistence()).apply {
			setCallback(clientCallback)
		}
		// circle back on 3rd MqttClientPersistence param for caching, possible offline support?
		val options = MqttConnectOptions().apply {
			userName = user
			password = pw.toCharArray()
		}


		try {
			_connectionStateFlow.value = ConnectionStatus.CONNECTING

			safeClient.connect(options)
			// connect is blocking, so can infer that connection succeeded if no error thrown
			// todo: look into more async version
			_connectionStateFlow.value = ConnectionStatus.CONNECTED
			client = safeClient
		} catch (e: Exception) {
			// coroutine
			//_clientErrorChannel.send(ClientError("Error during connect()"))
			_connectionStateFlow.value = ConnectionStatus.IDLE
		}
	}

	override fun disconnect() {
		val safeClient = client ?: run {
			return
		}

		try {
			safeClient.disconnect()
			_connectionStateFlow.value = ConnectionStatus.CLIENT_DISCONNECT
		} catch (e: Exception) {
			// error while disconnecting... ok
			// coroutine
			//_clientErrorChannel.send(ClientError("Error during disconnect()"))
			_connectionStateFlow.value = ConnectionStatus.IDLE
		}
	}

	/////////////////////////////////////////////////
	//  endregion


	//  region Topics
	/////////////////////////////////////////////////

	override fun subscribe(topic: String, qos: Int) {
		val safeClient = client ?: run {
			return
		}

		try {
			safeClient.subscribe(topic, qos, messageListener)
		} catch (e: Exception) {
			// coroutine
			//_clientErrorChannel.send(ClientError("Error during subscribe()"))
		}
	}

	override fun unsubscribe(topic: String) {
		val safeClient = client ?: run {
			return
		}

		try {
			safeClient.unsubscribe(topic)
		} catch (e: Exception) {
			// coroutine
			//_clientErrorChannel.send(ClientError("Error during unsubscribe()"))
		}
	}

	/////////////////////////////////////////////////
	//  endregion


	//  region Messages
	/////////////////////////////////////////////////

	private val _latestMessageStateFlow: MutableStateFlow<GenericMessage?> = MutableStateFlow(null)
	override val latestMessageStateFlow: StateFlow<GenericMessage?> = _latestMessageStateFlow

	override fun publish(
		topic: String,
		msg: String,
		qos: Int,
		retained: Boolean,
	) {
		val safeClient = client ?: run {
			return
		}

		try {
			val message = MqttMessage().apply {
				this.payload = msg.toByteArray()
				this.qos = qos
				this.isRetained = retained
			}
			safeClient.publish(topic, message)
		} catch (e: Exception) {
			// coroutine
			//_clientErrorChannel.send(ClientError("Error during publish()"))
		}
	}

	/////////////////////////////////////////////////
	//  endregion
}