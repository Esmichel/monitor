package com.tfm_central.monitor.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tfm_central.monitor.config.PendingResponses;
import com.tfm_central.monitor.model.ConfigurableParameter;

import org.eclipse.paho.client.mqttv3.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.*;

@Service
public class ConfigurationService {

    private final MqttClient mqttClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ConfigurationService(MqttClient mqttClient) {
        this.mqttClient = mqttClient;
    }

    public List<ConfigurableParameter> requestParametersFromESP32(String deviceId) {
        String requestTopic = "sistema/configuracion/" + deviceId;
        String responseTopic = "sistema/respuestas/" + deviceId;
        String messagePayload = String.format(
                "{\"type\":\"get_config\",\"response_topic\":\"%s\"}",
                responseTopic);

        CompletableFuture<String> responseFuture = new CompletableFuture<>();
        PendingResponses.add(deviceId, responseFuture);

        try {
            mqttClient.publish(requestTopic, new MqttMessage(messagePayload.getBytes()));
            String wrappedJson = responseFuture.get(30, TimeUnit.SECONDS);
            ConfigurableParameter[] params = objectMapper.readValue(wrappedJson, ConfigurableParameter[].class);
            return Arrays.asList(params);
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        } finally {
            PendingResponses.remove(deviceId);
        }
    }
}