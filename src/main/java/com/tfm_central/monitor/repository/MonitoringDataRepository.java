package com.tfm_central.monitor.repository;

import java.util.List;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import com.tfm_central.monitor.model.MonitoringData;

@Repository
public interface MonitoringDataRepository extends ElasticsearchRepository<MonitoringData, Long> {
    //List<MonitoringData> findByDeviceIdAndDataType(String deviceId, String dataType);
}
