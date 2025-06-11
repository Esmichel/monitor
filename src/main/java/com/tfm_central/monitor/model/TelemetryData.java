package com.tfm_central.monitor.model;

import java.util.Date;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Document(indexName = "esp32_telemetry")
public class TelemetryData {

    @Id
    private String id = UUID.randomUUID().toString();

    @JsonProperty("timestamp")
    @Field(type = FieldType.Date, format = {}, pattern = "epoch_millis||strict_date_optional_time_nanos")
    private Date timestamp;

    @JsonProperty("sender_id")
    @Field(type = FieldType.Keyword)
    private String senderId;

    // HEAP STATUS
    @JsonProperty("total_free_bytes")
    @Field(type = FieldType.Long)
    private Long totalFreeBytes;

    @JsonProperty("total_free_blocks")
    @Field(type = FieldType.Integer)
    private Integer totalFreeBlocks;

    @JsonProperty("total_largest_block")
    @Field(type = FieldType.Long)
    private Long totalLargestBlock;

    @JsonProperty("heap_min_free")
    @Field(type = FieldType.Long)
    private Long heapMinFree;

    @JsonProperty("heap_allocated_blocks")
    @Field(type = FieldType.Integer)
    private Integer heapAllocatedBlocks;

    @JsonProperty("heap_fragmentation")
    @Field(type = FieldType.Integer)
    private Integer heapFragmentation;

    @JsonProperty("int_free_bytes")
    @Field(type = FieldType.Long)
    private Long intFreeBytes;

    @JsonProperty("int_free_blocks")
    @Field(type = FieldType.Integer)
    private Integer intFreeBlocks;

    @JsonProperty("int_largest_block")
    @Field(type = FieldType.Long)
    private Long intLargestBlock;

    // UPTIME
    @JsonProperty("uptime_sec")
    @Field(type = FieldType.Long)
    private Long uptimeSec;

    // FreeRTOS Tasks
    @JsonProperty("freertos_task_count")
    @Field(type = FieldType.Integer)
    private Integer freertosTaskCount;

    @JsonProperty("stack_min_free_current_task")
    @Field(type = FieldType.Integer)
    private Integer stackMinFreeCurrentTask;

    // WIFI CONNECTION INFO
    @JsonProperty("connected_ssid")
    @Field(type = FieldType.Keyword)
    private String connectedSsid;

    @JsonProperty("ap_mac")
    @Field(type = FieldType.Keyword)
    private String apMac;

    @JsonProperty("rssi")
    @Field(type = FieldType.Integer)
    private Integer rssi;

    @JsonProperty("wifi_channel")
    @Field(type = FieldType.Integer)
    private Integer wifiChannel;

    @JsonProperty("auth_mode")
    @Field(type = FieldType.Keyword)
    private String authMode;

    // IP INFO
    @JsonProperty("ip_addr")
    @Field(type = FieldType.Ip)
    private String ipAddr;

    @JsonProperty("gateway")
    @Field(type = FieldType.Ip)
    private String gateway;

    @JsonProperty("netmask")
    @Field(type = FieldType.Ip)
    private String netmask;

}
