package com.tfm_central.monitor.service;

import org.springframework.stereotype.Service;

import com.tfm_central.monitor.model.TelemetryData;
import com.tfm_central.monitor.repository.TelemetryDataRepository;

@Service
public class TelemetryDataService {
    private final TelemetryDataRepository telemetryRepository;

    public TelemetryDataService(TelemetryDataRepository repository) {
        this.telemetryRepository = repository;
    }

    /*public List<MonitoringData> getMonitoringDataByDeviceId(String deviceId) {
        return dataRepository.findByDeviceIdAndDataType(deviceId, "deauth_packet_count");
    }*/

    public void saveTelemetryData(TelemetryData data) {
        telemetryRepository.save(data);
    }
}

