package com.piledrive.sample_mqtt.hilt

import com.piledrive.sample_mqtt.mqtt.client.MqttClientImpl
import com.piledrive.sample_mqtt.mqtt.client.PahoAsyncMqttClient
import com.piledrive.sample_mqtt.mqtt.client.PahoMqttClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object MqttClientHiltModule {
	@Provides
	fun provideTypedMqttClient(): MqttClientImpl {
		return PahoAsyncMqttClient()
	}
}