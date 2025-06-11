package com.tfm_central.monitor.config;

import org.eclipse.paho.client.mqttv3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import com.tfm_central.monitor.model.ARPMapping;
import com.tfm_central.monitor.model.ARP_Data;
import com.tfm_central.monitor.model.AlertingData;
import com.tfm_central.monitor.model.MonitoringData;
import com.tfm_central.monitor.model.TelemetryData;
import com.tfm_central.monitor.service.AlertingDataService;
import com.tfm_central.monitor.service.ArpMappingService;
import com.tfm_central.monitor.service.DeviceService;
import com.tfm_central.monitor.service.MonitoringDataService;
import com.tfm_central.monitor.service.TelemetryDataService;
import com.tfm_central.monitor.service.EvilTwinWhitelistService;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.text.Normalizer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import org.json.JSONObject;

@Configuration
public class MqttConfig {

    @Autowired
    MonitoringDataService monitoringDataService;
    @Autowired
    ArpMappingService arpMappingService;
    @Autowired
    DeviceService deviceService;
    @Autowired
    EvilTwinWhitelistService evilTwinWhitelistService;
    @Autowired
    AlertingDataService alertingDataService;
    @Autowired
    TelemetryDataService telemetryDataService;

    MqttClient client;

    private final ObjectMapper objectMapper = new ObjectMapper();
    @Value("${mqtt.client.server-uri}")
    private String brokerUri;
    @Value("${mqtt.client.username}")
    private String mqttUsername;
    @Value("${mqtt.client.password}")
    private String mqttPassword;

    // --- TLS/SSL properties ---
    @Value("${mqtt.ssl.key-store}")
    private String keyStorePath;
    @Value("${mqtt.ssl.key-store-password}")
    private String keyStorePassword;
    @Value("${mqtt.ssl.key-store-type}")
    private String keyStoreType;

    @Value("${mqtt.ssl.trust-store}")
    private String trustStorePath;
    @Value("${mqtt.ssl.trust-store-password}")
    private String trustStorePassword;
    @Value("${mqtt.ssl.trust-store-type}")
    private String trustStoreType;

    // private static final String BROKER_URI = "tcp://rabbitmq:1883";
    private static final String CLIENT_ID = "SpringBootClient";
    // private static final String MQTT_USERNAME = "admin";
    // private static final String MQTT_PASSWORD = "admin";

    private static final String DISCOVERY_TOPIC = "sistema/nuevos_dispositivos/+";
    private static final String MONITORING_TOPIC = "sistema/monitores/+";
    private static final String COMMANDS_TOPIC = "sistema/comandos/+";
    private static final String CONFIG_TOPIC = "sistema/configuracion/+";
    private static final String BROADCAST_TOPIC = "sistema/broadcast";
    private static final String RESPONSES_TOPIC = "sistema/respuestas/+";
    private static final String ALERTS_TOPIC = "sistema/alertas/+";
    private static final String ARP_TOPIC = "sistema/arptable/+";
    private static final String ESP_MONITORING_TOPIC = "sistema/telemetria/+";

    @Bean
    public MqttClient mqttClient() throws MqttException, Exception {
        client = new MqttClient(brokerUri, CLIENT_ID);
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(false);
        options.setAutomaticReconnect(true);
        options.setKeepAliveInterval(30);
        options.setConnectionTimeout(10);
        options.setUserName(mqttUsername);
        options.setPassword(mqttPassword.toCharArray());
        options.setSocketFactory(buildSocketFactory());

        client.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectionLost(Throwable cause) {
                System.err.println("MQTT connection lost: " + cause.getMessage());
            }

            @Override
            public void messageArrived(String topic, MqttMessage message)
                    throws JsonMappingException, JsonProcessingException {
                String payload = new String(message.getPayload());
                // System.out.println("Received MQTT message on topic: " + topic);
                // System.out.println("Payload: " + payload);

                if (topic.startsWith("sistema/nuevos_dispositivos/")) {
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
                } else if (topic.startsWith("sistema/alertas/")) {
                    handleAlert(payload);
                } else if (topic.startsWith("sistema/arptable/")) {
                    handleARP(payload);
                } else if (topic.startsWith("sistema/telemetria/")) {
                    handleTelemetry(payload);
                } else {
                    System.out.println("Unhandled topic: " + topic);
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                System.out.println("Message delivered successfully");
            }

            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                System.out.println("MQTT connect complete (reconnect=" + reconnect + ")");

                try {
                    client.subscribe(DISCOVERY_TOPIC);
                    client.subscribe(MONITORING_TOPIC);
                    client.subscribe(RESPONSES_TOPIC);
                    client.subscribe(ARP_TOPIC);
                    client.subscribe(ALERTS_TOPIC);
                    client.subscribe(ESP_MONITORING_TOPIC);
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }

        });

        client.connect(options);

        return client;
    }

    private SSLSocketFactory buildSocketFactory() throws Exception {
        KeyStore ts = KeyStore.getInstance(trustStoreType);
        try (InputStream in = new ClassPathResource(trustStorePath).getInputStream()) {
            ts.load(in, trustStorePassword.toCharArray());
        }
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(
                TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(ts);

        KeyStore ks = KeyStore.getInstance(keyStoreType);
        try (InputStream in = new ClassPathResource(keyStorePath).getInputStream()) {
            ks.load(in, keyStorePassword.toCharArray());
        }
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(
                KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(ks, keyStorePassword.toCharArray());

        SSLContext ctx = SSLContext.getInstance("TLS");
        ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
        return ctx.getSocketFactory();
    }

    private void handleDiscovery(String message) {
        // String payload = new String(message.getPayload(), StandardCharsets.UTF_8);
        // System.out.println("Heartbeat recived: " + message);
        JSONObject json = new JSONObject(message);
        String deviceId = json.getString("device_id");
        deviceService.updateHeartbeat(deviceId);
    }

    private void handleMonitoring(String message) throws JsonMappingException, JsonProcessingException {
        // System.out.println("Monitoring data received: " + message);
        MonitoringData monitoringData = objectMapper.readValue(message, MonitoringData.class);
        Date ts = monitoringData.getTimestamp();

        long currentTimeMs = System.currentTimeMillis();
        long assumedBootTimeMs = currentTimeMs - (10 * 1000);
        long relativeTimeMs = ts.getTime() / 1000;
        long sendingTimeMs = assumedBootTimeMs + relativeTimeMs;
        Date sendingDate = new Date(sendingTimeMs);

        monitoringData.setTimestamp(sendingDate);
        String normalizedSsid = Normalizer.normalize(monitoringData.getSsid(), Normalizer.Form.NFD);

        normalizedSsid = normalizedSsid.replaceAll("[^a-zA-Z0-9_]", "_");
        // System.out.println("Updated SSID: " + normalizedSsid);
        monitoringData.setSsid(normalizedSsid);

        // System.out.println("Approximate package sending datetime: " + sendingDate);
        monitoringDataService.saveMonitoringData(monitoringData);
    }

    private void handleResponse(String response) {
        // System.out.println("ESP32 Response: " + response);
        String deviceId = null;
        try {
            JsonNode root = objectMapper.readTree(response);
            deviceId = root.get("deviceId").asText();
            String type = root.get("type").asText();

            if ("config_params".equals(type)) {
                CompletableFuture<String> future = PendingResponses.get(deviceId);
                System.out.println("PendingResponses: " + future.toString());
                if (future != null && !future.isDone()) {
                    System.out.println("Completing future for deviceId: " + deviceId);
                    future.complete(root.get("payload").toString());
                    System.out.println(">>> Future completed successfully");
                }
            }

        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } finally {
            PendingResponses.remove(deviceId);
        }
    }

    public void publishMessage(String topic, String payload) {
        try {
            MqttMessage message = new MqttMessage(payload.getBytes());
            message.setQos(1);
            message.setRetained(false);
            client.publish(topic, message);
            System.out.println("Published to " + topic + ": " + payload);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void handleARP(String message) {
        // System.out.println("RAW ARP table received: " + message);

        ARP_Data arpData;
        try {
            arpData = objectMapper.readValue(message, ARP_Data.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return;
        }

        Date deviceTs = arpData.getTimestamp();
        long nowMs = System.currentTimeMillis();
        long assumedBootTimeMs = nowMs - (10 * 1000);
        long relMs = deviceTs.getTime() / 1000;
        Date sendDate = new Date(assumedBootTimeMs + relMs);
        System.out.println("Computed sending datetime: " + sendDate);

        List<ARPMapping> docs = arpData.getEntries().stream()
                .map(e -> new ARPMapping(
                        sendDate,
                        arpData.getSenderId(),
                        arpData.getType(),
                        e.getMac(),
                        e.getIp()))
                .collect(Collectors.toList());

        arpMappingService.saveMappings(docs);
        // System.out.println("Indexed " + docs.size() + " ARP mappings");
    }

    private void handleAlert(String message) {
        // System.out.println("Alert data received: " + message);

        AlertingData alertingData = null;
        try {
            alertingData = objectMapper.readValue(message, AlertingData.class);
        } catch (JsonMappingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Date ts = alertingData.getTimestamp();

        long currentTimeMs = System.currentTimeMillis();
        long assumedBootTimeMs = currentTimeMs - (10 * 1000);
        long relativeTimeMs = ts.getTime() / 1000;
        long sendingTimeMs = assumedBootTimeMs + relativeTimeMs;
        Date sendingDate = new Date(sendingTimeMs);

        alertingData.setTimestamp(sendingDate);
        String normalizedSsid = Normalizer.normalize(alertingData.getSsid(), Normalizer.Form.NFD);

        normalizedSsid = normalizedSsid.replaceAll("[^a-zA-Z0-9_]", "_");
        // System.out.println("Updated SSID: " + normalizedSsid);
        alertingData.setSsid(normalizedSsid);

        // System.out.println("Approximate package sending datetime: " + sendingDate);
        alertingDataService.saveAlertingData(alertingData);

        if (alertingData.getAttackType().contains("Evil Twin")) {
            evilTwinWhitelistService.addToWhitelist(
                    alertingData.getSsid(), alertingData.getSrcMac());
        }
    }

    private void handleTelemetry(String message) throws JsonMappingException, JsonProcessingException {
        // Parse JSON al objeto TelemetryData
        TelemetryData telemetryData = objectMapper.readValue(message, TelemetryData.class);

        // timestamp viene en ISO8601 y se mapea directamente a Date, no es relativo
        Date ts = telemetryData.getTimestamp();

        // Si quieres puedes validar o ajustar el timestamp aquí, pero asumo que está
        // bien

        // Normalizar el SSID conectado si quieres (por ejemplo para indexación
        // consistente)
        String connectedSsid = telemetryData.getConnectedSsid();
        if (connectedSsid != null) {
            String normalizedSsid = Normalizer.normalize(connectedSsid, Normalizer.Form.NFD);
            normalizedSsid = normalizedSsid.replaceAll("[^a-zA-Z0-9_]", "_");
            telemetryData.setConnectedSsid(normalizedSsid);
        }
        long nowMs = System.currentTimeMillis();
        Date sendingDate = new Date(nowMs);
        telemetryData.setTimestamp(sendingDate);

        // Guardar en Elasticsearch (suponiendo un servicio inyectado
        // telemetryDataService)
        telemetryDataService.saveTelemetryData(telemetryData);
    }

}
