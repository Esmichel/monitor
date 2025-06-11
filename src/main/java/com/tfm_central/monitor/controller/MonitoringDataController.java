package com.tfm_central.monitor.controller;

import java.util.Collections;
import java.util.List;

import com.tfm_central.monitor.model.ConfigurableParameter;
import com.tfm_central.monitor.service.ConfigurationService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MonitoringDataController {

    @Autowired
    private ConfigurationService configurationService;

    @GetMapping("/esp32/configurable-params")
    public ResponseEntity<List<ConfigurableParameter>> getConfigurableParameters(
            @RequestParam(required = false) String deviceId) {
        try {
            String resolvedDeviceId = (deviceId == null || deviceId.isEmpty()) ? "FFFFFFFFFFFF" : deviceId;

            System.out.println("Asked for config from device: " + resolvedDeviceId);
            List<ConfigurableParameter> params = configurationService.requestParametersFromESP32(resolvedDeviceId);
            return ResponseEntity.ok(params);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.emptyList());
        }
    }
}
