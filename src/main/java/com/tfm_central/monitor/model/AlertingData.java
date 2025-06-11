package com.tfm_central.monitor.model;

import java.util.Date;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@Document(indexName = "alert_packets")
@JsonIgnoreProperties(ignoreUnknown = true) // Ignore unexpected JSON fields
public class AlertingData {

    @Id
    @JsonProperty("alert_hash")
    private Long packetHash;

    @JsonProperty("sender_id")
    @Field(type = FieldType.Text, fielddata = true) // Allows aggregation & sorting
    private String senderId;

    @JsonProperty("timestamp")
    @Field(type = FieldType.Date, format = {}, pattern = "epoch_millis||strict_date_optional_time_nanos") 
    private Date timestamp;

    @JsonProperty("src_mac")
    @Field(type = FieldType.Keyword)
    private String srcMac;

    @JsonProperty("dst_mac")
    @Field(type = FieldType.Keyword)
    private String dstMac;

    @JsonProperty("signal_strength")
    @Field(type = FieldType.Integer)
    private int signalStrength;

    @JsonProperty("ssid")
    @Field(type = FieldType.Text, fielddata = true) // Allows aggregation & sorting
    private String ssid;

    @JsonProperty("attack_type")
    @Field(type = FieldType.Keyword)
    private String attackType;

}
