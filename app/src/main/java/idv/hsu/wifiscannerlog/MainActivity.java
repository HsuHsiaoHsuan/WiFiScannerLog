package idv.hsu.wifiscannerlog;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    WifiManager wifiManager;
    WifiScanReceiver wifiScanReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        wifiScanReceiver = new WifiScanReceiver();

        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(wifiScanReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

        wifiManager.setWifiEnabled(true);
        wifiManager.startScan();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(wifiScanReceiver);
    }

    private class WifiScanReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            List<ScanResult> scanResults = wifiManager.getScanResults();

            Log.d(TAG, "result size: " + scanResults.size());

            List<String> bssidList = new ArrayList<>();

            for (int x=0; x<scanResults.size(); x++) {
                System.out.println(
                        "BSSID: " + scanResults.get(x).BSSID + ", " +
                        "SSID: " + scanResults.get(x).SSID + ", " +
                        "capabilities: " + scanResults.get(x).capabilities + ", " +
                        "describeContents: " + scanResults.get(x).describeContents() + ", " +
                        "frequency: " + scanResults.get(x).frequency + ", " +
                        "level: " + scanResults.get(x).level + ", " //+
                );
            }


        }
    }
}
