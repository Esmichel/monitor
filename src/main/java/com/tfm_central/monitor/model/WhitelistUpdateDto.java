package com.tfm_central.monitor.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class WhitelistUpdateDto {
    @NotBlank(message = "La MAC no puede estar vacía.")
    private String mac;

    @NotNull(message = "El campo 'whitelisted' no puede ser nulo.")
    private Boolean whitelisted;

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