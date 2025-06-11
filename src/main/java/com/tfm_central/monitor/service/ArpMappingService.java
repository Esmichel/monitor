package com.tfm_central.monitor.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.tfm_central.monitor.model.ARPMapping;
import com.tfm_central.monitor.model.ARP_Data;
import com.tfm_central.monitor.repository.ARPMappingRepository;
// import com.tfm_central.monitor.repository.ArpScanResultRepository;

@Service
public class ArpMappingService {
    private final ARPMappingRepository arpRepository;

    public ArpMappingService(ARPMappingRepository repository) {
        this.arpRepository = repository;
    }

    /*
     * public List<MonitoringData> getMonitoringDataByDeviceId(String deviceId) {
     * return dataRepository.findByDeviceIdAndDataType(deviceId,
     * "deauth_packet_count");
     * }
     */
    public void saveMappings(List<ARPMapping> mappings) {
        arpRepository.saveAll(mappings);
    }

    // public void saveArpData(ARP_Data data) {
    //     arpRepository.save(data);
    // }
}