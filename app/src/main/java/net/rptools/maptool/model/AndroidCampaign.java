package net.rptools.maptool.model;

import java.util.Map;

public class AndroidCampaign {
    public GUID id = new GUID();
    public Map<GUID, AndroidZone> zones;
    public AndroidCampaignProperties campaignProperties;


    public AndroidCampaign(){}

    public AndroidCampaign(GUID id, Map<GUID, AndroidZone> zones, AndroidCampaignProperties campaignProperties) {
        this.id = id;
        this.zones = zones;
        this.campaignProperties = campaignProperties;
    }
}
