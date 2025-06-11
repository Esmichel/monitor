package com.tfm_central.monitor.model;

import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ArpEntry {

    @JsonProperty("ip")
    @Field(type = FieldType.Ip)
    private String ip;

    @JsonProperty("mac")
    @Field(type = FieldType.Keyword)
    private String mac;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

}
