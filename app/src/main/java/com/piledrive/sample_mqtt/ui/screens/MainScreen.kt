@file:OptIn(ExperimentalMaterial3Api::class)

package com.piledrive.sample_mqtt.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.piledrive.sample_mqtt.ui.nav.NavRoute
import com.piledrive.sample_mqtt.ui.theme.SampleComposeTheme
import com.piledrive.sample_mqtt.ui.util.previewMainContentFlow
import com.piledrive.sample_mqtt.viewmodel.SampleViewModel
import kotlinx.coroutines.flow.StateFlow

object MainScreen : NavRoute {
	override val routeValue: String = "home"

	@Composable
	fun draw(
		viewModel: SampleViewModel,
	) {
		drawContent(
			viewModel.contentState,
		)
	}

	@Composable
	fun drawContent(
		contentState: StateFlow<Int>,
	) {
		val homeState = contentState.collectAsState().value
		Scaffold(
			topBar = {
				TopAppBar(
					title = { Text("MQTT Sample")},
				)
			},
			content = { innerPadding ->
				Box(modifier = Modifier.padding(innerPadding))
			}
		)
	}
}

@Preview
@Composable
fun MainPreview() {
	SampleComposeTheme {
		val contentState = previewMainContentFlow()
		MainScreen.drawContent(
			contentState
		)
	}
}