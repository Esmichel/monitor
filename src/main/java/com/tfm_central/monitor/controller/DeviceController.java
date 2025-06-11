package com.tfm_central.monitor.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tfm_central.monitor.config.MqttConfig;
import com.tfm_central.monitor.model.ConfigurableParameter;
import com.tfm_central.monitor.service.DeviceService;

@RestController
@RequestMapping("/api/devices")
public class DeviceController {

    @Autowired
    private MqttConfig mqttService;
    private final DeviceService deviceService;

    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @GetMapping("/active")
    public List<String> getActiveDevices() {
        return deviceService.getActiveDevices();
    }

    private String serializeValue(Object value, String type) {
        if (value == null)
            return "null";

        switch (type) {
            case "int":
            case "long":
            case "float":
            case "double":
            case "bool":
                return value.toString();
            default:
                return "\"" + value.toString() + "\"";
        }
    }

    @PostMapping("/save-config/{deviceId}")
    public ResponseEntity<ResponseMessage> saveConfiguration(@PathVariable String deviceId,
            @RequestBody List<ConfigurableParameter> parameters) {
        System.out.println("Saving configuration for device: " + deviceId);
        String topic = "sistema/configuracion/" + deviceId;

        StringBuilder payload = new StringBuilder("{ \"deviceId\": \"")
                .append(deviceId)
                .append("\", \"config_params\": [");

        for (ConfigurableParameter param : parameters) {
            payload.append("{")
                    .append("\"name\": \"").append(param.getName()).append("\",")
                    .append("\"value\": ").append(serializeValue(param.getDefaultValue(), param.getType())).append(",")
                    .append("\"type\": \"").append(param.getType()).append("\"")
                    .append("},");
        }

        payload.deleteCharAt(payload.length() - 1).append("]}");

        mqttService.publishMessage(topic, payload.toString());

        return ResponseEntity.ok(new ResponseMessage("Configuración enviada con éxito"));
    }

    public static class ResponseMessage {
        private String message;

        public ResponseMessage(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

}
