package com.piledrive.sample_mqtt.ui.coordinators

import com.piledrive.lib_compose_components.ui.forms.observable.TextFormFieldState
import com.piledrive.lib_compose_components.ui.forms.validators.Validators
import com.piledrive.lib_compose_components.ui.lists.selectable.SelectableListCoordinatorGenericReactive
import com.piledrive.sample_mqtt.mqtt.client.MqttClientImpl
import com.piledrive.sample_mqtt.mqtt.model.MqttConnectionStatus
import com.piledrive.sample_mqtt.mqtt.model.MqttGenericMessage
import com.piledrive.sample_mqtt.mqtt.model.MqttGenericTopic
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

interface MessagesTabCoordinatorImpl {
	val coroutineScope: CoroutineScope
	val mqtt: MqttClientImpl
	val topicInputFormState: TextFormFieldState
	val subscribedTopicsState: StateFlow<List<MqttGenericTopic>>
	val activeTopicSelectionCoordinator: SelectableListCoordinatorGenericReactive<MqttGenericTopic>
	val messageInputFormState: TextFormFieldState
	val recentMessagesState: StateFlow<List<MqttGenericMessage>>
	fun subscribeTopic()
	fun unsubscribeTopic(topic: String)
	fun sendMessage()
}

class MessagesTabCoordinator(
	override val coroutineScope: CoroutineScope,
	override val mqtt: MqttClientImpl,
	initTopicInput: String = "",
	initActiveTopics: List<String>? = null,
	initMessageInput: String = "",
	initRecentMessages: List<MqttGenericMessage> = listOf(),
) : MessagesTabCoordinatorImpl {

	override val topicInputFormState = TextFormFieldState(
		mainValidator = Validators.Required<String>(""),
		externalValidators = listOf(
			Validators.Custom<String>(
				runCheck = { strVal ->
					return@Custom subscribedTopicsState.value.firstOrNull { it.name == strVal } == null
				},
				errMsg = "Topic already active"
			)
		),
		initialValue = initTopicInput
	)

	override val subscribedTopicsState: StateFlow<List<MqttGenericTopic>> = initActiveTopics?.let { topics ->
		MutableStateFlow(topics.map { MqttGenericTopic(it, 1) })
	} ?: mqtt.subscribedTopicsStateFlow

	override val activeTopicSelectionCoordinator = SelectableListCoordinatorGenericReactive<MqttGenericTopic>(
		coroutineScope = coroutineScope,
		optionsSourceFlow = subscribedTopicsState,
		optionTextMutator = { it.name },
		optionIdForSelectedCheck = { it.name },
	)

	override val messageInputFormState = TextFormFieldState(
		mainValidator = Validators.Required(""),
		externalValidators = listOf(
			Validators.Custom<String>(
				runCheck = { strVal ->
					activeTopicSelectionCoordinator.selectedOptionState.value != null
				},
				errMsg = "Topic selection required"
			)
		),
		initialValue = initMessageInput
	)

	private val _recentMessagesState: MutableStateFlow<List<MqttGenericMessage>> = MutableStateFlow(initRecentMessages)
	override val recentMessagesState: StateFlow<List<MqttGenericMessage>> = _recentMessagesState


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
		val targetTopic = activeTopicSelectionCoordinator.selectedOptionState.value?.name ?: return
		val message = messageInputFormState.currentValueState.value
		mqtt.publish(topic = targetTopic, msg = message)
		messageInputFormState.clear()
	}
}