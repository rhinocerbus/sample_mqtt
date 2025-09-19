package com.piledrive.sample_mqtt.repo

import com.piledrive.sample_mqtt.mqtt.controller.MqttController
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class SampleRepo @Inject constructor(
	private val mqttController: MqttController
) {

}