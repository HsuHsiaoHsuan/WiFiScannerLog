package idv.hsu.wifiscannerlog.data;

//TODO https://en.wikipedia.org/wiki/List_of_WLAN_channels
public enum EnumChannels implements EnumConverter {
    G24_CH_1(2412),
    G24_CH_2(2417),
    G24_CH_3(2422),
    G24_CH_4(2427),
    G24_CH_5(2432),
    G24_CH_6(2437),
    G24_CH_7(2442),
    G24_CH_8(2447),
    G24_CH_9(2452),
    G24_CH_10(2457),
    G24_CH_11(2462),
    G24_CH_12(2467),
    G24_CH_13(2472),
    G24_CH_14(2484);

    private final int value;

    EnumChannels(int value) {
        this.value = value;
    }

    public int convert() {
        return value;
    }
}
