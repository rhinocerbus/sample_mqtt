package com.piledrive.sample_mqtt.ui.screens

import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.tooling.preview.Preview
import com.piledrive.lib_compose_components.ui.theme.custom.AppTheme
import com.piledrive.sample_mqtt.model.ConnectionStatus
import com.piledrive.sample_mqtt.mqtt.client.PreviewDummyMqttClient
import com.piledrive.sample_mqtt.ui.coordinators.ServerConnectCoordinator
import com.piledrive.sample_mqtt.ui.coordinators.ServerConnectCoordinatorImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers


@Composable
fun ServerTab(serverCoordinator: ServerConnectCoordinatorImpl) {
	val isEnabled = serverCoordinator.isActiveState.collectAsState().value

	val serverUrl = serverCoordinator.serverUrlState.collectAsState()
	OutlinedTextField(
		value = serverUrl.value,
		label = { Text("Server URL") },
		onValueChange = { serverCoordinator.onServerUrlUpdated(it) },
		enabled = isEnabled
	)

	val serverPort = serverCoordinator.serverPortState.collectAsState()
	OutlinedTextField(
		value = serverPort.value.toString(),
		label = { Text("Server port") },
		onValueChange = { serverCoordinator.onServerPortUpdated(it.toInt()) },
		enabled = isEnabled
	)

	val clientId = serverCoordinator.clientIdState.collectAsState()
	OutlinedTextField(
		value = clientId.value,
		label = { Text("Client id") },
		onValueChange = { serverCoordinator.onClientIdUpdated(it) },
		enabled = isEnabled
	)

	val username = serverCoordinator.usernameState.collectAsState()
	OutlinedTextField(
		value = username.value,
		label = { Text("Username") },
		onValueChange = { serverCoordinator.onUsernameUpdated(it) },
		enabled = isEnabled
	)

	val password = serverCoordinator.passwordState.collectAsState()
	OutlinedTextField(
		value = password.value,
		label = { Text("Password") },
		onValueChange = { serverCoordinator.onPasswordUpdated(it) },
		enabled = isEnabled
	)

	val status = serverCoordinator.connectionState.collectAsState()
	if (status.value == ConnectionStatus.CONNECTED) {
		Button(
			onClick = {
				serverCoordinator.attemptDisconnect()
			},
		) {
			Text("Disconnect")
		}
	} else {
		Button(
			onClick = {
				serverCoordinator.attemptConnect()
			},
			enabled = isEnabled,
		) {
			Text("Connect")
		}
	}

	if (status.value != ConnectionStatus.IDLE) {
		Text("Connection status: ${status.value.name}")
	}
}

@Preview
@Composable
private fun ServerTabPreview() {
	AppTheme {
		val connectionCoordinator = ServerConnectCoordinator(
			coroutineScope = CoroutineScope(Dispatchers.Default),
			mqtt = PreviewDummyMqttClient()
		)
		ServerTab(connectionCoordinator)
	}
}