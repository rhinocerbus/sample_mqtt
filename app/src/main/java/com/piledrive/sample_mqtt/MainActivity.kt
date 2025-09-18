package com.piledrive.sample_mqtt

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import com.piledrive.lib_compose_components.ui.theme.custom.AppTheme
import com.piledrive.lib_compose_components.ui.util.updateStatusBarColorCompose
import com.piledrive.sample_mqtt.ui.nav.RootNavHost
import com.piledrive.sample_mqtt.ui.theme.SampleComposeTheme
import com.piledrive.sample_mqtt.viewmodel.SampleViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

	private val viewModel: SampleViewModel by viewModels()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		enableEdgeToEdge()
		setContent {
			AppTheme {
				updateStatusBarColorCompose(MaterialTheme.colorScheme.background)
				RootNavHost()
			}
		}
	}
}