package com.piledrive.sample_mqtt.mqtt.client

import com.piledrive.sample_mqtt.mqtt.MqttControllerCallbacks
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
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import timber.log.Timber


class PahoMqttClient(private val controllerCallbacks: MqttControllerCallbacks) : MqttClientImpl {

	val actionListener = object : IMqttActionListener {
		override fun onSuccess(asyncActionToken: IMqttToken?) {
			Timber.d("actionListener.onSuccess | token: $asyncActionToken")
		}

		override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
			Timber.d("actionListener.onFailure | token: $asyncActionToken | exc: $exception")
		}
	}

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

	private var client: MqttClient? = null

	private val _clientErrorChannel = Channel<MqttClientError>()
	override val clientErrorFlow: Flow<MqttClientError> = _clientErrorChannel.receiveAsFlow()

	/////////////////////////////////////////////////
	//  endregion


	//  region Connection
	/////////////////////////////////////////////////

	private val _connectionStateFlow: MutableStateFlow<MqttConnectionStatus> = MutableStateFlow(MqttConnectionStatus.IDLE)
	override val connectionStateFlow: StateFlow<MqttConnectionStatus> = _connectionStateFlow

	override suspend fun connect(url: String, port: Int, clientId: String, user: String, pw: String) {
		val safeClient = MqttClient("$url:$port", clientId, MemoryPersistence()).apply {
			setCallback(clientCallback)
		}
		// circle back on 3rd MqttClientPersistence param for caching, possible offline support?
		val options = MqttConnectOptions().apply {
			userName = user
			password = pw.toCharArray()
		}

		_connectionStateFlow.value = MqttConnectionStatus.CONNECTING

		safeClient.runCatching {
			Timber.d(">> Connecting...")
			safeClient.connect(options)
			0
		}.onSuccess {
			Timber.d("<< ...Connected")
			_connectionStateFlow.value = MqttConnectionStatus.CONNECTED
			client = safeClient
		}.onFailure {
			Timber.w("<< Problem during connection: $it")
			_clientErrorChannel.send(MqttClientError("Error during connect()"))
			_connectionStateFlow.value = MqttConnectionStatus.IDLE
		}
	}

	override suspend fun disconnect() {
		val safeClient = client ?: run {
			return
		}

		runCatching {
			Timber.d("> Starting disconnection attempt")
			safeClient.disconnect()
		}.onSuccess {
			Timber.d("<< ...Disconnected")
			_connectionStateFlow.value = MqttConnectionStatus.CLIENT_DISCONNECT
		}.onFailure {
			// error while disconnecting... ok
			Timber.w("<< Problem during disconnection: $it")
			_clientErrorChannel.send(MqttClientError("Error during disconnect()"))
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
		}.onSuccess {
			Timber.w(">> Subscribe success")
			val updatedTopics = _subscribedTopicsStateFlow.value + MqttGenericTopic(topic, qos)
			_subscribedTopicsStateFlow.value = updatedTopics
		}.onFailure {
			Timber.w(">> Problem during subscribe: $it")
			_clientErrorChannel.send(MqttClientError("Error during subscribe()"))
		}
		Timber.d("<< done")
	}

	override suspend fun unsubscribe(topic: String) {
		val safeClient = client ?: run {
			return
		}

		Timber.d("> Starting unsubscribe attempt")
		runCatching {
			safeClient.unsubscribe(topic)
		}.onSuccess {
			Timber.w(">> Unsubscribe success")
			val updatedSet = _subscribedTopicsStateFlow.value.toMutableList()
			if (updatedSet.removeIf { it.name == topic }) {
				_subscribedTopicsStateFlow.value = updatedSet
			}
		}.onFailure {
			Timber.w(">> Problem during unsubscribe: $it")
			_clientErrorChannel.send(MqttClientError("Error during unsubscribe()"))
		}
		Timber.d("<< done")
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

		Timber.d("> Starting publish attempt")
		val message = MqttMessage().apply {
			this.payload = msg.toByteArray()
			this.qos = qos
			this.isRetained = retained
		}

		runCatching {
			safeClient.publish(topic, message)
		}.onSuccess {
			Timber.w(">> publish success")
		}.onFailure {
			Timber.w(">> Problem during publish: $it")
			_clientErrorChannel.send(MqttClientError("Error during publish()"))
		}
		Timber.d("<< done")
	}

	/////////////////////////////////////////////////
	//  endregion
}