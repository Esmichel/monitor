package com.tfm_central.monitor.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tfm_central.monitor.config.MqttConfig;
import com.tfm_central.monitor.model.EvilTwinWhitelist;
import com.tfm_central.monitor.model.WhitelistUpdateDto;
import com.tfm_central.monitor.service.EvilTwinWhitelistService;

import org.springframework.web.bind.annotation.RequestBody;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/whitelist")
public class WhitelistController {

    @Autowired
    private MqttConfig mqttService;
    private final EvilTwinWhitelistService whitelistService;

    public WhitelistController(EvilTwinWhitelistService whitelistService) {
        this.whitelistService = whitelistService;
    }

    @GetMapping("/registeredtwins")
    public ResponseEntity<List<EvilTwinWhitelist>> getEvilTwinList() {
        return ResponseEntity.ok(whitelistService.getEvilTwinList());
    }

    @DeleteMapping("/remove")
    public ResponseEntity<Void> removeFromWhitelist(@RequestBody Map<String, String> payload) {
        String ssid = payload.get("ssid");
        String mac = payload.get("mac");
        if (ssid == null || mac == null) {
            return ResponseEntity.badRequest().build();
        }
        whitelistService.removeFromWhitelist(ssid, mac);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/add")
    public ResponseEntity<Void> addToWhitelist(@RequestBody EvilTwinWhitelist entry) {
        whitelistService.addToWhitelist(entry.getSsid(), entry.getMac());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/mac/update")
    public ResponseEntity<String> updateWhitelistStatus(@Valid @RequestBody WhitelistUpdateDto dto) {

        String mac = dto.getMac();
        Boolean whitelisted = dto.getWhitelisted();

        if (!mac.matches("^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$")) {
            return ResponseEntity.badRequest().body("Formato de dirección MAC inválido.");
        }

        for (EvilTwinWhitelist item : whitelistService.getEvilTwinList()) {
            if (item.getMac().equalsIgnoreCase(mac)) {
                item.setWhitelisted(whitelisted);
                return ResponseEntity.ok("Estado actualizado.");
            }
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Dispositivo no encontrado.");
    }

    // Opcional: Enviar la whitelist a los dispositivos por MQTT
    @PostMapping("/send")
    public ResponseEntity<Void> sendWhitelistToDevices() {
        List<EvilTwinWhitelist> list = whitelistService.getEvilTwinList()
                .stream()
                .filter(EvilTwinWhitelist::getWhitelisted)
                .toList();

        try {
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(list);
            mqttService.publishMessage("sistema/whitelist", json);
            return ResponseEntity.ok().build();
        } catch (JsonProcessingException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
