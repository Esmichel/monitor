package com.tfm_central.monitor.service;

import com.tfm_central.monitor.model.EvilTwinWhitelist;
import java.util.ArrayList;

import org.springframework.stereotype.Service;

@Service
public class EvilTwinWhitelistService {

    ArrayList<EvilTwinWhitelist> evilTwinWhitelistArrayList = new ArrayList<>();
    EvilTwinWhitelist evilTwinWhitelist;

    public void addToWhitelist(String ssid, String mac) {
        if (!isWhitelisted(ssid, mac)) {

            evilTwinWhitelist = new EvilTwinWhitelist();
            evilTwinWhitelist.setSsid(ssid);
            evilTwinWhitelist.setMac(mac);
            evilTwinWhitelistArrayList.add(evilTwinWhitelist);
        }
    }

    public void removeFromWhitelist(String ssid, String mac) {
        evilTwinWhitelistArrayList.removeIf(entry -> entry.getSsid().equals(ssid) && entry.getMac().equals(mac));
    }

    public boolean isWhitelisted(String ssid, String mac) {
        return evilTwinWhitelistArrayList.stream()
                .anyMatch(entry -> entry.getSsid().equals(ssid) && entry.getMac().equals(mac));
    }

    public void setWhitelisted(int index, boolean whitelisted) {
        if (index >= 0 && index < evilTwinWhitelistArrayList.size()) {
            EvilTwinWhitelist item = evilTwinWhitelistArrayList.get(index);
            item.setWhitelisted(whitelisted);
        }
    }

    public ArrayList<EvilTwinWhitelist> getEvilTwinList() {
        return evilTwinWhitelistArrayList;
    }

}
