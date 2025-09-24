package com.piledrive.sample_mqtt.ui.coordinators

import com.piledrive.lib_compose_components.ui.forms.observable.TextFormFieldState
import com.piledrive.lib_compose_components.ui.forms.validators.Validators
import com.piledrive.sample_mqtt.mqtt.client.MqttClientImpl
import com.piledrive.sample_mqtt.mqtt.model.MqttConnectionStatus
import com.piledrive.sample_mqtt.mqtt.model.MqttGenericMessage
import com.piledrive.sample_mqtt.mqtt.model.MqttGenericTopic
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

interface MessagesCoordinatorImpl {
	val coroutineScope: CoroutineScope
	val mqtt: MqttClientImpl
	val topicInputFormState: TextFormFieldState
	val activeTopicsState: StateFlow<List<MqttGenericTopic>>
	val messageInputState: StateFlow<String>
	val recentMessagesState: StateFlow<List<MqttGenericMessage>>
	val onMessageInputUpdated: (String) -> Unit
	fun subscribeTopic()
	fun unsubscribeTopic(topic: String)
	fun sendMessage()
}

class MessagesCoordinator(
	override val coroutineScope: CoroutineScope,
	override val mqtt: MqttClientImpl,
	initTopicInput: String = "",
	initActiveTopics: List<String>? = null,
	initMessageInput: String = "",
	initRecentMessages: List<MqttGenericMessage> = listOf(),
) : MessagesCoordinatorImpl {

	override val topicInputFormState = TextFormFieldState(
		mainValidator = Validators.Required<String>(""),
		externalValidators = listOf(
			Validators.Custom<String>(
				runCheck = { strVal ->
					return@Custom activeTopicsState.value.firstOrNull { it.name == strVal } == null
				},
				errMsg = "Topic already active"
			)
		),
		initialValue = initTopicInput
	)

	override val activeTopicsState: StateFlow<List<MqttGenericTopic>> = initActiveTopics?.let { topics ->
		MutableStateFlow(topics.map { MqttGenericTopic(it, 1) })
	} ?: mqtt.subscribedTopicsStateFlow

	private val _messageInputState: MutableStateFlow<String> = MutableStateFlow(initMessageInput)
	override val messageInputState: StateFlow<String> = _messageInputState

	private val _recentMessagesState: MutableStateFlow<List<MqttGenericMessage>> = MutableStateFlow(initRecentMessages)
	override val recentMessagesState: StateFlow<List<MqttGenericMessage>> = _recentMessagesState

	override val onMessageInputUpdated: (String) -> Unit = { _messageInputState.value = it }


	init {
		coroutineScope.launch {
			mqtt.connectionStateFlow.collect { state ->
				if (state in listOf(
						MqttConnectionStatus.CLIENT_DISCONNECT,
						MqttConnectionStatus.SERVER_DISCONNECT,
						MqttConnectionStatus.INTERRUPTED
					)
				) {
					clear()
				}
			}
		}
		coroutineScope.launch {
			mqtt.latestMessageStateFlow.collect { msg ->
				val message = msg ?: return@collect
				_recentMessagesState.value = _recentMessagesState.value + message
			}
		}
	}

	private fun clear() {
		topicInputFormState.clear()
		_recentMessagesState.value = listOf()
	}

	override fun subscribeTopic() {
		mqtt.subscribe(topicInputFormState.currentValueState.value, 1)
		topicInputFormState.submitFieldChange("")
	}

	override fun unsubscribeTopic(topic: String) {
	}

	override fun sendMessage() {
	}
}