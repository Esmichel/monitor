package com.tfm_central.monitor.config;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class PendingResponses {
    private static final ConcurrentHashMap<String, CompletableFuture<String>> pending = new ConcurrentHashMap<>();

    public static void add(String deviceId, CompletableFuture<String> future) {
        pending.put(deviceId, future);
    }

    public static CompletableFuture<String> get(String deviceId) {
        return pending.get(deviceId);
    }

    public static CompletableFuture<String> remove(String deviceId) {
        return pending.remove(deviceId);
    }

    public static boolean has(String deviceId) {
        return pending.containsKey(deviceId);
    }
}
