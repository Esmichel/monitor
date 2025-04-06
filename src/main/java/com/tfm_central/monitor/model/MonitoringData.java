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
@Document(indexName = "wifi_packets")
@JsonIgnoreProperties(ignoreUnknown = true) // Ignore unexpected JSON fields
public class MonitoringData {

    @Id
    @JsonProperty("packet_hash")
    private Long packetHash;

    @JsonProperty("sender_id")
    @Field(type = FieldType.Text, fielddata = true) // Allows aggregation & sorting
    private String senderId;

    @JsonProperty("packet_id")
    private String packetId;

    @JsonProperty("timestamp")
    @Field(type = FieldType.Date, format = {}, pattern = "epoch_millis||strict_date_optional_time_nanos") 
    private Date timestamp;

    @JsonProperty("frame_type")
    @Field(type = FieldType.Keyword)
    private String frameType;

    @JsonProperty("frame_subtype")
    @Field(type = FieldType.Keyword)
    private String frameSubtype;

    @JsonProperty("src_mac")
    @Field(type = FieldType.Keyword)
    private String srcMac;

    @JsonProperty("dst_mac")
    @Field(type = FieldType.Keyword)
    private String dstMac;

    @JsonProperty("signal_strength")
    @Field(type = FieldType.Integer)
    private int signalStrength;

    @JsonProperty("channel")
    @Field(type = FieldType.Text, fielddata = true)
    private int channel;

    @JsonProperty("protocol")
    @Field(type = FieldType.Keyword)
    private String protocol;

    @JsonProperty("src_port")
    @Field(type = FieldType.Integer)
    private int srcPort;

    @JsonProperty("dst_port")
    @Field(type = FieldType.Integer)
    private int dstPort;

    @JsonProperty("is_broadcast")
    @Field(type = FieldType.Boolean)
    private boolean isBroadcast;

    @JsonProperty("ssid_length")
    @Field(type = FieldType.Short)
    private short ssidLength;

    @JsonProperty("ssid")
    @Field(type = FieldType.Text, fielddata = true) // Allows aggregation & sorting
    private String ssid;

    @JsonProperty("payload")
    @Field(type = FieldType.Object)
    private Payload payload;

    @Data
    public static class Payload {
        @JsonProperty("length")
        @Field(type = FieldType.Integer)
        private int length;

        @JsonProperty("payload_data")
        @Field(type = FieldType.Keyword) // Use Text if full-text search is needed
        private String payloadData;
    }
}
