package idv.hsu.wifiscannerlog;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import idv.hsu.wifiscannerlog.data.AccessPoint;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final boolean D = true;

    WifiManager wifiManager;
    WifiScanReceiver wifiScanReceiver;

    private ExpandableListView list;
    private MainListAdapter adapter;
    private List<String> groupList = new ArrayList<String>();
    private List<List<AccessPoint>> childList = new ArrayList<List<AccessPoint>>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        wifiScanReceiver = new WifiScanReceiver();

        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        list = (ExpandableListView) findViewById(R.id.list);
        adapter = new MainListAdapter(getLayoutInflater(), groupList, childList);
        list.setAdapter(adapter);

        list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                int itemType = ExpandableListView.getPackedPositionType(id);

                if (itemType == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
                    int groupPosition = ExpandableListView.getPackedPositionGroup(id);
                    Toast.makeText(MainActivity.this, groupList.get(groupPosition), Toast.LENGTH_SHORT).show();

                    return true;
                }
                return false;
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Re-scan wifi signal...", Snackbar.LENGTH_SHORT)
                        .setAction("Action", null).show();
                wifiManager.startScan();
            }
        });
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
            groupList.clear();
            childList.clear();

            for (int x=0; x<scanResults.size(); x++) {
                ScanResult result = scanResults.get(x);

                int idx = groupList.indexOf(result.BSSID);
                if (idx != -1) { // already has it.
                    childList.get(idx).add(new AccessPoint(result.BSSID, result.SSID, result.capabilities, result.frequency, result.level));
                } else {
                    groupList.add(result.BSSID);
//                    idx = groupList.indexOf(result.BSSID);
                    idx = groupList.size() - 1;
                    ArrayList<AccessPoint> tmp = new ArrayList<>();
                    tmp.add(new AccessPoint(result.BSSID, result.SSID, result.capabilities, result.frequency, result.level));
                    childList.add(idx, tmp);
                }

                if (D) {
                System.out.println(
                        "BSSID: " + scanResults.get(x).BSSID + ", " +
                        "SSID: " + scanResults.get(x).SSID + ", " +
                        "capabilities: " + scanResults.get(x).capabilities + ", " +
                        "describeContents: " + scanResults.get(x).describeContents() + ", " +
                        "frequency: " + scanResults.get(x).frequency + ", " +
                        "level: " + scanResults.get(x).level + ", " //+
                );}
            }
            adapter.notifyDataSetChanged();
        }
    }
}
