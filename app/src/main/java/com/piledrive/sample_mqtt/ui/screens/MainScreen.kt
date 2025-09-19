@file:OptIn(ExperimentalMaterial3Api::class)

package com.piledrive.sample_mqtt.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.piledrive.lib_compose_components.ui.theme.custom.AppTheme
import com.piledrive.sample_mqtt.model.ConnectionStatus
import com.piledrive.sample_mqtt.mqtt.client.PreviewDummyMqttClient
import com.piledrive.sample_mqtt.ui.coordinators.ServerConnectCoordinator
import com.piledrive.sample_mqtt.ui.coordinators.ServerConnectCoordinatorImpl
import com.piledrive.sample_mqtt.ui.nav.NavRoute
import com.piledrive.sample_mqtt.viewmodel.SampleViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

object MainScreen : NavRoute {
	override val routeValue: String = "home"

	@Composable
	fun draw(
		viewModel: SampleViewModel,
	) {
		drawContent(
			viewModel.serverConnectCoordinator,
		)
	}

	@Composable
	fun drawContent(
		serverCoordinator: ServerConnectCoordinatorImpl,
	) {
		Scaffold(
			topBar = {
				TopAppBar(
					title = { Text("MQTT Sample") },
				)
			},
			content = { innerPadding ->
				BodyContent(
					modifier = Modifier
						.padding(innerPadding)
						.fillMaxSize(), serverCoordinator
				)
			}
		)
	}

	@Composable
	private fun BodyContent(
		modifier: Modifier,
		serverCoordinator: ServerConnectCoordinatorImpl
	) {
		Column(modifier) {
			ServerConnectionContent(serverCoordinator)
		}
	}

	@Composable
	private fun ServerConnectionContent(serverCoordinator: ServerConnectCoordinatorImpl) {
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
}

@Preview
@Composable
fun MainPreview() {
	AppTheme {
		val connectionCoordinator = ServerConnectCoordinator(
			coroutineScope = CoroutineScope(Dispatchers.Default),
			mqtt = PreviewDummyMqttClient()
		)
		MainScreen.drawContent(
			serverCoordinator = connectionCoordinator
		)
	}
}