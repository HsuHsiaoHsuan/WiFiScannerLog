package idv.hsu.wifiscannerlog;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.location.Location;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import idv.hsu.wifiscannerlog.data.AccessPoint;
import idv.hsu.wifiscannerlog.data.LogDbHelper;
import idv.hsu.wifiscannerlog.data.LogDbSchema;
//import idv.hsu.wifiscannerlog.event.Event_CsvImportOk;

public class MainActivity extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        LocationListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final boolean D = true;

    // Wifi
    WifiManager wifiManager;
    WifiScanReceiver wifiScanReceiver;

    // UI
    private ExpandableListView list;
    private MainListAdapter adapter;
    private List<String> groupList = new ArrayList<String>();
    private List<List<AccessPoint>> childList = new ArrayList<List<AccessPoint>>();
    private LogDbHelper dbHelper;
    private TextView tv_location;

    // Location
    private GoogleApiClient mGoogleApiClient;
//    private Location mLastLocation;
    private Location mCurrentLocation;

    // Google Api Client
    private static final int REQUEST_RESOLVE_ERROR = 1001; // Request code to use when launching the resolution activity
    private static final String DIALOG_ERROR = "dialog_error"; // Unique tag for the error dialog fragment
    private boolean mResolvingError = false; // Bool to track whether the app is already resolving an error
    private static final String STATE_RESOLVING_ERROR = "resolving_error";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mResolvingError = savedInstanceState != null
                && savedInstanceState.getBoolean(STATE_RESOLVING_ERROR, false);
        buildGoogleApiClient();
        mGoogleApiClient.connect();

        dbHelper = new LogDbHelper(this);
        try {
            dbHelper.create();
        } catch (IOException e) {
            e.printStackTrace();
            throw new Error("Unable to copy database.");
        }
        dbHelper.open();
        dbHelper.getWritableDatabase();

        wifiScanReceiver = new WifiScanReceiver();
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        list = (ExpandableListView) findViewById(R.id.list);
        adapter = new MainListAdapter(getLayoutInflater(), groupList, childList);
        list.setAdapter(adapter);

        list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                int itemType = ExpandableListView.getPackedPositionType(id);

                if (itemType == ExpandableListView.PACKED_POSITION_TYPE_GROUP) { // long press on a group
                    int groupPosition = ExpandableListView.getPackedPositionGroup(id);
                    Toast.makeText(MainActivity.this, groupList.get(groupPosition), Toast.LENGTH_SHORT).show();
                    // It's favor
                    if (dbHelper.isBssidSaved(groupList.get(groupPosition))) {
                        int result = dbHelper.delete(groupList.get(groupPosition));
                        if (result > 0) {
                            ((ImageView) view.findViewById(R.id.iv_favor)).setImageResource(R.drawable.ic_favorite_black_24dp);
                            Toast.makeText(MainActivity.this, "del favor ok.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // It's not favor.
                        List<AccessPoint> tmpList = childList.get(groupPosition);
                        for (int x = 0; x < tmpList.size(); x++) {
                            if (D) {
                                System.out.println("WTF! " + tmpList.get(x).getSsid());
                            }
                            AccessPoint tmpAp = tmpList.get(x);
                            ContentValues values = new ContentValues();
                            values.put(LogDbSchema.BSSID, tmpAp.getBssid());
                            values.put(LogDbSchema.SSID, tmpAp.getSsid());
                            values.put(LogDbSchema.CAPABILITIES, tmpAp.getCapabilities());
                            values.put(LogDbSchema.FREQUENCY, tmpAp.getFrequency());
                            values.put(LogDbSchema.LEVEL, tmpAp.getLevel());
                            values.put(LogDbSchema.TIME, System.currentTimeMillis());
                            values.put(LogDbSchema.LOCATION, mCurrentLocation.toString());
                            long result = dbHelper.insertTrackingBSSID(values);
                            if (result != -1) {
                                Toast.makeText(MainActivity.this, "add favor ok.", Toast.LENGTH_SHORT).show();
                                ((ImageView) view.findViewById(R.id.iv_favor)).setImageResource(R.drawable.ic_favorite_red_24dp);
                            }
                        }
                    }
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

        tv_location = (TextView) findViewById(R.id.tv_location);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_RESOLVE_ERROR) {
            mResolvingError = false;
            if (resultCode == RESULT_OK) {
                // Make sure the app is not already connected or attempting to connect
                if (!mGoogleApiClient.isConnecting() &&
                        !mGoogleApiClient.isConnected()) {
                    mGoogleApiClient.connect();
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(wifiScanReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

        wifiManager.setWifiEnabled(true);
        wifiManager.startScan();

        if (mGoogleApiClient.isConnected()) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(wifiScanReceiver);
        if (mGoogleApiClient.isConnected()) {
            stopLocationUpdates();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_RESOLVING_ERROR, mResolvingError);
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbHelper.close();
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (D) {
            Log.d(TAG, "GoogleClientApi, onConnected");
        }
//        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {
        if (D) {
            Log.d(TAG, "onConnectionSuspended: " + i);
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        if (D) {
            Log.d(TAG, "onConnectionFailed: " + result.getErrorMessage());
        }
        if (mResolvingError) {
            // Already attempting to resolve an error.
            return;
        } else if (result.hasResolution()) {
            try {
                mResolvingError = true;
                result.startResolutionForResult(this, REQUEST_RESOLVE_ERROR);
            } catch (IntentSender.SendIntentException e) {
                // There was an error with the resolution intent. Try again.
                mGoogleApiClient.connect();
            }
        } else {
            // Show dialog using GoogleApiAvailability.getErrorDialog()
            showErrorDialog(result.getErrorCode());
            mResolvingError = true;
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        if (D) {
            Log.d(TAG, "CurrentLocation: " + mCurrentLocation.toString());
        }
        tv_location.setText(mCurrentLocation.toString());
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

    protected synchronized void buildGoogleApiClient() {
        if (D) {
            Log.d(TAG, "buildGoogleApiClient");
        }
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    protected void startLocationUpdates() {
        if (D) {
            Log.d(TAG, "startLocationUpdates");
        }
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }

    /* Creates a dialog for an error message */
    private void showErrorDialog(int errorCode) {
        // Create a fragment for the error dialog
        ErrorDialogFragment dialogFragment = new ErrorDialogFragment();
        // Pass the error that should be displayed
        Bundle args = new Bundle();
        args.putInt(DIALOG_ERROR, errorCode);
        dialogFragment.setArguments(args);
        dialogFragment.show(getSupportFragmentManager(), "errordialog");
    }

    /* Called from ErrorDialogFragment when the dialog is dismissed. */
    public void onDialogDismissed() {
        mResolvingError = false;
    }

    /* A fragment to display an error dialog */
    public static class ErrorDialogFragment extends DialogFragment {
        public ErrorDialogFragment() { }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Get the error code and retrieve the appropriate dialog
            int errorCode = this.getArguments().getInt(DIALOG_ERROR);
            return GoogleApiAvailability.getInstance().getErrorDialog(
                    this.getActivity(), errorCode, REQUEST_RESOLVE_ERROR);
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            ((MainActivity) getActivity()).onDialogDismissed();
        }
    }
}