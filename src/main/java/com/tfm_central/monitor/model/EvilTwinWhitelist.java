package com.tfm_central.monitor.model;

public class EvilTwinWhitelist {

    private String ssid;
    private String mac;
    private Boolean whitelisted = false;

    public EvilTwinWhitelist() {
    }

    public EvilTwinWhitelist(String ssid, String mac) {
        this.ssid = ssid;
        this.mac = mac;
    }

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public Boolean getWhitelisted() {
        return whitelisted;
    }

    public void setWhitelisted(Boolean whitelisted) {
        this.whitelisted = whitelisted;
    }
}
