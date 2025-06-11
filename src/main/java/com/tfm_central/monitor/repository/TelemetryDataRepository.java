package com.tfm_central.monitor.repository;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;
import com.tfm_central.monitor.model.TelemetryData;

@Repository
public interface TelemetryDataRepository extends ElasticsearchRepository<TelemetryData, Long> {
    //List<TelemetryData> findByDeviceIdAndDataType(String deviceId, String dataType);
}
