package com.tfm_central.monitor.repository;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import com.tfm_central.monitor.model.AlertingData;

public interface AlertingRepository extends ElasticsearchRepository<AlertingData, Long> {
    
}
