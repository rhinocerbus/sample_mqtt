package com.piledrive.sample_mqtt.ui.coordinators

import com.piledrive.sample_mqtt.mqtt.client.MqttClientImpl
import com.piledrive.sample_mqtt.mqtt.model.MqttGenericTopic
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

interface MessagesCoordinatorImpl {
	val coroutineScope: CoroutineScope
	val mqtt: MqttClientImpl
	val topicInputState: StateFlow<String>
	val activeTopicsState: StateFlow<List<MqttGenericTopic>>
	val messageInputState: StateFlow<String>
	val recentMessagesState: StateFlow<List<String>>
	val onTopicInputUpdated: (String) -> Unit
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
	initRecentMessages: List<String> = listOf(),
) : MessagesCoordinatorImpl {
	private val _topicInputState: MutableStateFlow<String> = MutableStateFlow(initTopicInput)
	override val topicInputState: StateFlow<String> = _topicInputState

	override val activeTopicsState: StateFlow<List<MqttGenericTopic>> = initActiveTopics?.let { topics ->
		MutableStateFlow(topics.map { MqttGenericTopic(it, 1) })
	} ?: mqtt.subscribedTopicsStateFlow

	private val _messageInputState: MutableStateFlow<String> = MutableStateFlow(initMessageInput)
	override val messageInputState: StateFlow<String> = _messageInputState

	private val _recentMessagesState: MutableStateFlow<List<String>> = MutableStateFlow(initRecentMessages)
	override val recentMessagesState: StateFlow<List<String>> = _recentMessagesState

	override val onTopicInputUpdated: (String) -> Unit = { _topicInputState.value = it }
	override val onMessageInputUpdated: (String) -> Unit = { _messageInputState.value = it }


	init {
		coroutineScope.launch {
		}
	}

	override fun subscribeTopic() {
	}

	override fun unsubscribeTopic(topic: String) {
	}

	override fun sendMessage() {
	}
}