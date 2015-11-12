package idv.hsu.wifiscannerlog;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.util.Log;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import au.com.bytecode.opencsv.CSVReader;
import de.greenrobot.event.EventBus;
import idv.hsu.wifiscannerlog.data.LogDbHelper;
import idv.hsu.wifiscannerlog.data.LogDbSchema;
import idv.hsu.wifiscannerlog.event.Event_CsvImportOk;

public class ImportCsvService extends IntentService {
    private static final String TAG = ImportCsvService.class.getSimpleName();
    private static final boolean D = true;

    private LogDbHelper dbHelper;

    public ImportCsvService() {
        super(TAG);

        dbHelper = new LogDbHelper(this);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String next[] = {};
        List<String[]> list = new ArrayList<String[]>();
        try {
            CSVReader reader = new CSVReader(new InputStreamReader(getAssets().open("oui.csv")));
            for (;;) {
                next = reader.readNext();
                if (next != null) {
                    list.add(next);
                    for (int x = 0; x < next.length; x++) {
                        ContentValues values = new ContentValues();
                        values.put(LogDbSchema.MAC, next[1]);
                        values.put(LogDbSchema.MANUFACTURE, next[2]);
                        values.put(LogDbSchema.ADDRESS, next[3]);
                        dbHelper.insertManufactureData(values);
                    }
                } else {
                    if (D) {
                        Log.d(TAG, "Import CSV data finish!");
                    }
                    EventBus.getDefault().post(new Event_CsvImportOk());
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        dbHelper.close();
    }
}
