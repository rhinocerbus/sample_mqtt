package com.piledrive.sample_mqtt.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.piledrive.lib_compose_components.ui.theme.custom.AppTheme
import com.piledrive.sample_mqtt.mqtt.client.PreviewDummyMqttClient
import com.piledrive.sample_mqtt.ui.coordinators.MessagesCoordinator
import com.piledrive.sample_mqtt.ui.coordinators.MessagesCoordinatorImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers


@Composable
fun MessagesTab(modifier: Modifier, messagesCoordinator: MessagesCoordinatorImpl) {
	Column(modifier) {
		val topicInput = messagesCoordinator.topicInputState.collectAsState()
		OutlinedTextField(
			value = topicInput.value,
			label = { Text("Topic") },
			onValueChange = { messagesCoordinator.onTopicInputUpdated(it) },
		)
		Button(onClick = { messagesCoordinator.subscribeTopic() }) {
			Text("Subscribe")
		}

		TopicsList(messagesCoordinator)
	}
}

@Composable
private fun TopicsList(messagesCoordinator: MessagesCoordinatorImpl) {
	val topics = messagesCoordinator.activeTopicsState.collectAsState()

	Text("Active topics:")

	LazyColumn() {
		itemsIndexed(items = topics.value, key = { _, topic -> topic.name }) {_, topic ->
			Row() {
				Text(topic.name)
			}
		}
	}
}

@Preview
@Composable
private fun MessagesTabPreview() {
	AppTheme {
		val coordinator = MessagesCoordinator(
			coroutineScope = CoroutineScope(Dispatchers.Default),
			mqtt = PreviewDummyMqttClient(),
			initActiveTopics = listOf("topic/a", "topic/b")
		)
		MessagesTab(Modifier, coordinator)
	}
}