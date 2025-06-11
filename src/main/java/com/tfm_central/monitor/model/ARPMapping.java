package com.tfm_central.monitor.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Date;
import java.util.UUID;

@Document(indexName = "arp_mappings")
public class ARPMapping {

    @Id
    private String id = UUID.randomUUID().toString();

    @Field(type = FieldType.Date, format = {}, pattern = "epoch_millis||strict_date_optional_time_nanos")
    private Date timestamp;

    @Field(type = FieldType.Keyword)
    private String sender_id;

    @Field(type = FieldType.Keyword)
    private String mac;

    @Field(type = FieldType.Keyword)
    private String type;

    @Field(type = FieldType.Ip)
    private String ip;

    public ARPMapping() {
    }

    public ARPMapping(Date timestamp, String sender_id, String type, String mac, String ip) {
        this.timestamp = timestamp;
        this.sender_id = sender_id;
        this.type = type;
        this.mac = mac;
        this.ip = ip;
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

    public String getSender_id() {
        return sender_id;
    }

    public void setSender_id(String sender_id) {
        this.sender_id = sender_id;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

}
