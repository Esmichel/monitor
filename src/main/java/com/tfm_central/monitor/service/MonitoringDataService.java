package com.tfm_central.monitor.service;

import org.springframework.stereotype.Service;

import com.tfm_central.monitor.model.MonitoringData;
import com.tfm_central.monitor.repository.MonitoringDataRepository;

@Service
public class MonitoringDataService {
    private final MonitoringDataRepository dataRepository;

    public MonitoringDataService(MonitoringDataRepository repository) {
        this.dataRepository = repository;
    }

    /*public List<MonitoringData> getMonitoringDataByDeviceId(String deviceId) {
        return dataRepository.findByDeviceIdAndDataType(deviceId, "deauth_packet_count");
    }*/

    public void saveMonitoringData(MonitoringData data) {
        dataRepository.save(data);
    }
}

