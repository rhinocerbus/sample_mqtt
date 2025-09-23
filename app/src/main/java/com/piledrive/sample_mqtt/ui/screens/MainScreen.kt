@file:OptIn(ExperimentalMaterial3Api::class)

package com.piledrive.sample_mqtt.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
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
import com.piledrive.sample_mqtt.model.ConnectionStatus
import com.piledrive.sample_mqtt.mqtt.client.PreviewDummyMqttClient
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
		)
	}

	@Composable
	fun drawContent(
		tabsCoordinator: TabsCoordinatorImpl,
		serverCoordinator: ServerConnectCoordinatorImpl,
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
					serverCoordinator
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
					onClick = { tabsCoordinator.changeTab(i) }
				)
			}
		}
	}

	@Composable
	private fun BodyContent(
		modifier: Modifier,
		tabsCoordinator: TabsCoordinatorImpl,
		serverCoordinator: ServerConnectCoordinatorImpl
	) {
		val activeTab = tabsCoordinator.activeTabState.collectAsState().value
		Column(modifier) {
			when (activeTab.tab) {
				TabDestinations.SERVER -> {
					ServerTab(serverCoordinator)
				}

				TabDestinations.MESSAGES -> {
					MessagesTab(serverCoordinator)
				}
			}
		}
	}

}

@Preview
@Composable
fun MainPreview() {
	AppTheme {
		val tabsCoordinator = TabsCoordinator(
			initTabs = listOf(TabDestination(TabDestinations.SERVER, true), TabDestination(TabDestinations.MESSAGES, false)),
			initActiveTab = 1
		)
		val connectionCoordinator = ServerConnectCoordinator(
			coroutineScope = CoroutineScope(Dispatchers.Default),
			mqtt = PreviewDummyMqttClient()
		)
		MainScreen.drawContent(
			tabsCoordinator = tabsCoordinator,
			serverCoordinator = connectionCoordinator
		)
	}
}