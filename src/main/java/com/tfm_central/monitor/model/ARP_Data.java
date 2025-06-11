package com.tfm_central.monitor.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;
import java.util.List;

@Document(indexName = "arp_mappings")
public class ARP_Data {

    @Id
    private String id;

    @JsonProperty("entries")
    @Field(type = FieldType.Nested, includeInParent = true)
    private List<ArpEntry> entries;

    @JsonProperty("timestamp")
    @Field(type = FieldType.Date, format = {}, pattern = "epoch_millis||strict_date_optional_time_nanos")
    private Date timestamp;

    @JsonProperty("sender_id")
    @Field(type = FieldType.Text, fielddata = true) // Allows aggregation & sorting
    private String senderId;

    @JsonProperty("type")
    @Field(type = FieldType.Keyword, fielddata = true) // Allows aggregation & sorting
    private String type;
    
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public List<ArpEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<ArpEntry> entries) {
        this.entries = entries;
    }

    // Constructors, getters, setters
}
