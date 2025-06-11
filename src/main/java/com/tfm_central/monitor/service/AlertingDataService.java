package com.tfm_central.monitor.service;

import org.springframework.stereotype.Service;

import com.tfm_central.monitor.model.AlertingData;
import com.tfm_central.monitor.repository.AlertingRepository;

@Service
public class AlertingDataService {
    private final AlertingRepository alertRepository;

    public AlertingDataService(AlertingRepository repository) {
        this.alertRepository = repository;
    }

    /*
     * public List<MonitoringData> getMonitoringDataByDeviceId(String deviceId) {
     * return dataRepository.findByDeviceIdAndDataType(deviceId,
     * "deauth_packet_count");
     * }
     */

    public void saveAlertingData(AlertingData data) {
        alertRepository.save(data);
    }
}
