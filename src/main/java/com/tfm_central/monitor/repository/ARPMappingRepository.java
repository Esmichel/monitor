package com.tfm_central.monitor.repository;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import com.tfm_central.monitor.model.ARPMapping;
import com.tfm_central.monitor.model.ARP_Data;

public interface ARPMappingRepository extends ElasticsearchRepository<ARPMapping, String> {
    // Puedes añadir consultas personalizadas si lo necesitas
}
