package com.tfm_central.monitor.config;

import org.eclipse.paho.client.mqttv3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tfm_central.monitor.model.MonitoringData;
import com.tfm_central.monitor.service.MonitoringDataService;

import java.text.Normalizer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Date;

import io.swagger.v3.oas.annotations.parameters.RequestBody;

@Configuration
public class MqttConfig {

    @Autowired
    MonitoringDataService monitoringDataService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // 📌 MQTT Broker details
    private static final String BROKER_URI = "tcp://rabbitmq:1883";
    private static final String CLIENT_ID = "SpringBootClient";
    private static final String MQTT_USERNAME = "admin";
    private static final String MQTT_PASSWORD = "admin";

    // 📌 MQTT Topics
    private static final String DISCOVERY_TOPIC = "sistema/nuevos_dispositivos";
    private static final String MONITORING_TOPIC = "sistema/monitores/+";
    private static final String COMMANDS_TOPIC = "sistema/comandos/+";
    private static final String CONFIG_TOPIC = "sistema/configuracion/+";
    private static final String BROADCAST_TOPIC = "sistema/broadcast";
    private static final String RESPONSES_TOPIC = "sistema/respuestas/+";

    @Bean
    public MqttClient mqttClient() throws MqttException {
        MqttClient client = new MqttClient(BROKER_URI, CLIENT_ID);
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(false); // Keep subscriptions between reconnects
        options.setUserName(MQTT_USERNAME);
        options.setPassword(MQTT_PASSWORD.toCharArray());

        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                System.err.println("🔴 MQTT connection lost: " + cause.getMessage());
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) {
                String payload = new String(message.getPayload());
                System.out.println("📥 Received MQTT message on topic: " + topic);
                System.out.println("📌 Payload: " + payload);

                // Handle different topics
                if (topic.equals(DISCOVERY_TOPIC)) {
                    handleDiscovery(payload);
                } else if (topic.startsWith("sistema/monitores/")) {
                    try {
                        handleMonitoring(payload);
                    } catch (JsonMappingException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (JsonProcessingException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                } else if (topic.startsWith("sistema/respuestas/")) {
                    handleResponse(payload);
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                System.out.println("✅ Message delivered successfully");
            }
        });

        client.connect(options);

        // 📡 Subscribe to necessary topics
        client.subscribe(DISCOVERY_TOPIC);
        client.subscribe(MONITORING_TOPIC);
        client.subscribe(RESPONSES_TOPIC);

        return client;
    }

    private void handleDiscovery(String deviceId) {
        System.out.println("📡 New ESP32 detected: " + deviceId);
        // Optionally: store the device ID in a database
    }

    private void handleMonitoring(String message) throws JsonMappingException, JsonProcessingException {
        System.out.println("📊 Monitoring data received: " + message);
        MonitoringData monitoringData = objectMapper.readValue(message, MonitoringData.class);
        Date ts = monitoringData.getTimestamp();

        // Assume the device booted 10 seconds ago relative to now.
        long currentTimeMs = System.currentTimeMillis();
        long assumedBootTimeMs = currentTimeMs - (10 * 1000); // subtract 10 seconds

        // The timestamp from the package is in microseconds (relative to boot)
        // Convert that timestamp to milliseconds.
        long relativeTimeMs = ts.getTime() / 1000;

        // Approximate the package sending datetime as:
        // (assumed boot time) + (relative time from package)
        long sendingTimeMs = assumedBootTimeMs + relativeTimeMs;
        Date sendingDate = new Date(sendingTimeMs);

        // Update the monitoring data with the approximated sending datetime
        monitoringData.setTimestamp(sendingDate);
        // Normalize the SSID to remove diacritical marks and replace special characters
        String normalizedSsid = Normalizer.normalize(monitoringData.getSsid(), Normalizer.Form.NFD);

        // Replace all non-alphanumeric characters (including accents, spaces, and
        // others) with "_"
        normalizedSsid = normalizedSsid.replaceAll("[^a-zA-Z0-9_]", "_");

        // Update the SSID
        System.out.println("Updated SSID: " + normalizedSsid);
        monitoringData.setSsid(normalizedSsid);

        System.out.println("Approximate package sending datetime: " + sendingDate);
        monitoringDataService.saveMonitoringData(monitoringData);
    }

    private void handleResponse(String response) {
        System.out.println("✅ ESP32 Response: " + response);
    }

    public void publishMessage(String topic, String payload) {
        try {
            MqttClient client = mqttClient();
            MqttMessage message = new MqttMessage(payload.getBytes());
            message.setQos(1);
            message.setRetained(false);
            client.publish(topic, message);
            System.out.println("📤 Published to " + topic + ": " + payload);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}
