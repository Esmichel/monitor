package com.tfm_central.monitor.service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

@Service
public class DeviceService {

    private final Map<String, Instant> activeDevices = new ConcurrentHashMap<>();
    private final Duration timeout = Duration.ofSeconds(60);
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    @PostConstruct
    public void startCleanupTask() {
        scheduler.scheduleAtFixedRate(() -> {
            Instant now = Instant.now();
            activeDevices.entrySet().removeIf(entry -> {
                boolean expired = Duration.between(entry.getValue(), now).compareTo(timeout) > 0;
                if (expired) {
                    System.out.println("Dispositivo inactivo eliminado: " + entry.getKey());
                }
                return expired;
            });
        }, 0, 30, TimeUnit.SECONDS);
    }

    public void updateHeartbeat(String deviceId) {

        Instant now = Instant.now();
        activeDevices.put(deviceId, now);
    }

    public List<String> getActiveDevices() {
        Instant now = Instant.now();
        return activeDevices.entrySet().stream()
                .filter(entry -> Duration.between(entry.getValue(), now).compareTo(timeout) <= 0)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
    
}
