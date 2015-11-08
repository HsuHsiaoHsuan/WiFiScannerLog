package idv.hsu.wifiscannerlog.data;

public class AccessPoint {

    private String bssid;
    private String ssid;
    private String capabilities;
    private String frequency;
    private String level;

    public AccessPoint (String bssid, String ssid, String capabilities, String frequency, String levle) {
        this.bssid = bssid;
        this.ssid = ssid;
        this.capabilities = capabilities;
        this.frequency = frequency;
        this.level = levle;
    }

    public String getBssid() {
        return bssid;
    }

    public String getSsid() {
        return ssid;
    }

    public String getCapabilities() {
        return capabilities;
    }

    public String getFrequency() {
        return frequency;
    }

    public String getLevel() {
        return level;
    }
}
