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
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.IMqttMessageListener
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttAsyncClient
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.MqttPingSender
import org.eclipse.paho.client.mqttv3.internal.ClientComms
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import timber.log.Timber


class PahoAsyncMqttClient() : MqttClientImpl {

	val messageListener = IMqttMessageListener { topic, message ->
		Timber.d("messageListener.messageArrived | topic: $topic | msg: $message")
		_latestMessageStateFlow.value = MqttGenericMessage(topic ?: "NO_TOPIC", message?.toString() ?: "NO_MSG")
	}

	val clientCallback = object : MqttCallback {
		override fun connectionLost(cause: Throwable?) {
			Timber.w("clientCallback.connectionLost | $cause")
			_connectionStateFlow.value = MqttConnectionStatus.INTERRUPTED
		}

		override fun messageArrived(topic: String?, message: MqttMessage?) {
			Timber.d("clientCallback.messageArrived | topic: $topic | msg: $message")
			_latestMessageStateFlow.value = MqttGenericMessage(topic ?: "NO_TOPIC", message?.payload?.toString() ?: "NO_MSG")
		}

		override fun deliveryComplete(token: IMqttDeliveryToken?) {
			Timber.d("clientCallback.deliveryComplete | token: $token")
		}
	}


	//  region Client instance
	/////////////////////////////////////////////////

	private var client: MqttAsyncClient? = null

	val pingSender = object : MqttPingSender {
		override fun init(comms: ClientComms?) {
			TODO("Not yet implemented")
		}

		override fun start() {
			TODO("Not yet implemented")
		}

		override fun stop() {
			TODO("Not yet implemented")
		}

		override fun schedule(delayInMilliseconds: Long) {
			TODO("Not yet implemented")
		}
	}

	private val _clientErrorChannel = Channel<MqttClientError>()
	override val clientErrorFlow: Flow<MqttClientError> = _clientErrorChannel.receiveAsFlow()

	/////////////////////////////////////////////////
	//  endregion


	//  region Connection
	/////////////////////////////////////////////////

	private val _connectionStateFlow: MutableStateFlow<MqttConnectionStatus> = MutableStateFlow(MqttConnectionStatus.IDLE)
	override val connectionStateFlow: StateFlow<MqttConnectionStatus> = _connectionStateFlow

	override suspend fun connect(url: String, port: Int, clientId: String, user: String, pw: String) {
		val safeClient = MqttAsyncClient("$url:$port", clientId, MemoryPersistence()).apply {
			setCallback(clientCallback)
		}
		// circle back on 3rd MqttClientPersistence param for caching, possible offline support?
		val options = MqttConnectOptions().apply {
			userName = user
			password = pw.toCharArray()
		}

		runCatching {
			Timber.d(">> Starting connection attempt")
			_connectionStateFlow.value = MqttConnectionStatus.CONNECTING

			Timber.d(">> Connecting...")
			safeClient.connect(options, "", object : IMqttActionListener {
				override fun onSuccess(asyncActionToken: IMqttToken?) {
					Timber.d("connectListener.onSuccess | token: $asyncActionToken")
					Timber.d(">> ...Connected")
					_connectionStateFlow.value = MqttConnectionStatus.CONNECTED
					client = safeClient
				}

				override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
					Timber.d("connectListener.onFailure | token: $asyncActionToken | exc: $exception")
					Timber.d(">> ...Failed")
					_connectionStateFlow.value = MqttConnectionStatus.IDLE
				}
			}
			)
		}.onFailure {
			Timber.w("<< Problem during connection: $it")

			// coroutine
			//_clientErrorChannel.send(ClientError("Error during connect()"))
			_connectionStateFlow.value = MqttConnectionStatus.IDLE
		}
	}

	override suspend fun disconnect() {
		val safeClient = client ?: run {
			return
		}

		runCatching {
			Timber.d(">> Starting disconnection attempt")
			safeClient.disconnect("", object : IMqttActionListener {
				override fun onSuccess(asyncActionToken: IMqttToken?) {
					Timber.d("disconnectListener.onSuccess | token: $asyncActionToken")
					Timber.d(">> ...Disconnected")
					_connectionStateFlow.value = MqttConnectionStatus.CLIENT_DISCONNECT
				}

				override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
					Timber.d("disconnectListener.onFailure | token: $asyncActionToken | exc: $exception")
					Timber.d(">> ...Failed")
					_connectionStateFlow.value = MqttConnectionStatus.IDLE
				}
			})
		}.onFailure {
			Timber.w("<< Problem during disconnection: $it")

			// error while disconnecting... ok
			// coroutine
			//_clientErrorChannel.send(ClientError("Error during disconnect()"))
			_connectionStateFlow.value = MqttConnectionStatus.IDLE
		}
	}

	override fun clearState() {
		_subscribedTopicsStateFlow.value = listOf()
		_latestMessageStateFlow.value = null
	}

	/////////////////////////////////////////////////
	//  endregion


	//  region Topics
	/////////////////////////////////////////////////

	private val _subscribedTopicsStateFlow: MutableStateFlow<List<MqttGenericTopic>> = MutableStateFlow(listOf())
	override val subscribedTopicsStateFlow: StateFlow<List<MqttGenericTopic>> = _subscribedTopicsStateFlow

	override suspend fun subscribe(topic: String, qos: Int) {
		val safeClient = client ?: run {
			return
		}

		runCatching {
			safeClient.subscribe(topic, qos, messageListener)
			val updatedTopics = _subscribedTopicsStateFlow.value + MqttGenericTopic(topic, qos)
			_subscribedTopicsStateFlow.value = updatedTopics
		}.onFailure {
			// coroutine
			//_clientErrorChannel.send(ClientError("Error during subscribe()"))
		}
	}

	override suspend fun unsubscribe(topic: String) {
		val safeClient = client ?: run {
			return
		}

		runCatching {
			safeClient.unsubscribe(topic)
			val updatedSet = _subscribedTopicsStateFlow.value.toMutableList()
			if (updatedSet.removeIf { it.name == topic }) {
				_subscribedTopicsStateFlow.value = updatedSet
			}
		}.onFailure {
			// coroutine
			//_clientErrorChannel.send(ClientError("Error during unsubscribe()"))
		}
	}

	/////////////////////////////////////////////////
	//  endregion


	//  region Messages
	/////////////////////////////////////////////////

	private val _latestMessageStateFlow: MutableStateFlow<MqttGenericMessage?> = MutableStateFlow(null)
	override val latestMessageStateFlow: StateFlow<MqttGenericMessage?> = _latestMessageStateFlow

	override suspend fun publish(
		topic: String,
		msg: String,
		qos: Int,
		retained: Boolean,
	) {
		val safeClient = client ?: run {
			return
		}

		runCatching {
			val message = MqttMessage().apply {
				this.payload = msg.toByteArray()
				this.qos = qos
				this.isRetained = retained
			}
			safeClient.publish(topic, message)
		}.onFailure {
			// coroutine
			//_clientErrorChannel.send(ClientError("Error during publish()"))
		}
	}

	/////////////////////////////////////////////////
	//  endregion
}