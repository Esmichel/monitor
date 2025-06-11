package com.tfm_central.monitor.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@RestController
@RequestMapping("/internal")
public class GrafanaApiKeyController {

    @Value("${grafana.admin.user:admin}")
    private String grafanaAdminUser;

    @Value("${grafana.admin.password:admin}")
    private String grafanaAdminPassword;

    @Value("${grafana.host:http://grafana:3000}")
    private String grafanaHost;

    @PostMapping("/generate-api-key")
    public ResponseEntity<String> generateApiKey(@RequestParam(defaultValue = "dashboard-key") String name) {
        RestTemplate restTemplate = new RestTemplate();

        String url = UriComponentsBuilder
                .fromUriString(grafanaHost + "/api/auth/keys")
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(grafanaAdminUser, grafanaAdminPassword);
        headers.setContentType(MediaType.APPLICATION_JSON);

        String jsonBody = """
            {
                "name": "%s",
                "role": "Viewer",
                "secondsToLive": 86400
            }
            """.formatted(name);

        HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                return ResponseEntity.ok((String) response.getBody().get("key"));
            } else {
                return ResponseEntity.status(response.getStatusCode()).body("Error creating API key");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to connect to Grafana: " + e.getMessage());
        }
    }
}
