@file:OptIn(ExperimentalMaterial3Api::class)

package com.piledrive.sample_mqtt.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.piledrive.lib_compose_components.ui.theme.custom.AppTheme
import com.piledrive.sample_mqtt.mqtt.client.PreviewDummyMqttClient
import com.piledrive.sample_mqtt.ui.coordinators.MessagesCoordinator
import com.piledrive.sample_mqtt.ui.coordinators.MessagesCoordinatorImpl
import com.piledrive.sample_mqtt.ui.coordinators.ServerConnectCoordinator
import com.piledrive.sample_mqtt.ui.coordinators.ServerConnectCoordinatorImpl
import com.piledrive.sample_mqtt.ui.coordinators.TabDestination
import com.piledrive.sample_mqtt.ui.coordinators.TabDestinations
import com.piledrive.sample_mqtt.ui.coordinators.TabsCoordinator
import com.piledrive.sample_mqtt.ui.coordinators.TabsCoordinatorImpl
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
			viewModel.tabsCoordinator,
			viewModel.serverConnectCoordinator,
			viewModel.messagesCoordinator
		)
	}

	@Composable
	fun drawContent(
		tabsCoordinator: TabsCoordinatorImpl,
		serverCoordinator: ServerConnectCoordinatorImpl,
		messagesCoordinator: MessagesCoordinatorImpl
	) {
		Scaffold(
			topBar = {
				TopAppBar(
					title = { Text("MQTT Sample") },
				)
			},
			bottomBar = {
				TabBar(tabsCoordinator)
			},
			content = { innerPadding ->
				BodyContent(
					modifier = Modifier
						.padding(innerPadding)
						.fillMaxSize(),
					tabsCoordinator,
					serverCoordinator,
					messagesCoordinator
				)
			}
		)
	}

	@Composable
	private fun TabBar(
		tabsCoordinator: TabsCoordinatorImpl
	) {
		val activeTabIdx = tabsCoordinator.activeTabIdxState.collectAsState().value
		val tabs = tabsCoordinator.tabsState.collectAsState().value
		TabRow(
			selectedTabIndex = activeTabIdx
		) {
			tabs.forEachIndexed { i, t ->
				Tab(
					text = { Text(t.tab.name) },
					selected = i == activeTabIdx,
					enabled = t.enabled,
					onClick = { tabsCoordinator.changeTab(i) }
				)
			}
		}
	}

	@Composable
	private fun BodyContent(
		modifier: Modifier,
		tabsCoordinator: TabsCoordinatorImpl,
		serverCoordinator: ServerConnectCoordinatorImpl,
		messagesCoordinator: MessagesCoordinatorImpl
	) {
		val activeTab = tabsCoordinator.activeTabState.collectAsState().value
		when (activeTab.tab) {
			TabDestinations.SERVER -> {
				ServerTab(modifier, serverCoordinator)
			}

			TabDestinations.MESSAGES -> {
				MessagesTab(modifier, messagesCoordinator)
			}
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
		val messagesCoordinator = MessagesCoordinator(
			coroutineScope = CoroutineScope(Dispatchers.Default),
			mqtt = PreviewDummyMqttClient()
		)
		val tabsCoordinator = TabsCoordinator(
			coroutineScope = CoroutineScope(Dispatchers.Default),
			initTabs = listOf(TabDestination(TabDestinations.SERVER, true), TabDestination(TabDestinations.MESSAGES, false)),
			initActiveTab = 0,
			connectionStatusSourceState = connectionCoordinator.connectionState
		)
		MainScreen.drawContent(
			tabsCoordinator = tabsCoordinator,
			serverCoordinator = connectionCoordinator,
			messagesCoordinator = messagesCoordinator
		)
	}
}