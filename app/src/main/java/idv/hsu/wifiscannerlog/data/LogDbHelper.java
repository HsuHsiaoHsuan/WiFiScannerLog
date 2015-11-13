package idv.hsu.wifiscannerlog.data;

import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class LogDbHelper extends SQLiteOpenHelper {
    private static final String TAG = LogDbHelper.class.getSimpleName();
    private static final boolean D = true;

    private boolean onCreated = false;
    private boolean onUpgraded = false;

    private SQLiteDatabase db;
    private Context mContext;
    String DB_PATH = "";

    private static final int VERSION = 1;

    public LogDbHelper(Context context) {
        super(context, LogDbSchema.TABLE_LOG, null, VERSION);
        mContext = context;
        DB_PATH = context.getDatabasePath(LogDbSchema.DB_NAME).toString();
    }

    public void create() throws IOException {
        boolean check = checkDatabase();

        SQLiteDatabase db_read = null;

        db_read = this.getWritableDatabase();
        db_read.close();
        try {
            if (!check) {
                copyDataBase();
            }
        } catch (IOException ioe) {
            throw new Error("Error copying database");
        }
    }

    private boolean checkDatabase() {
        SQLiteDatabase db_check = null;
        try {
            db_check = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
        } catch (SQLiteException sqle) {

        }
        if (db_check != null) {
            db_check.close();
        }
        return (db_check != null) ? true : false;
    }

    private void copyDataBase() throws IOException {
        InputStream inputStream = mContext.getAssets().open(LogDbSchema.DB_NAME);
        OutputStream outputStream = new FileOutputStream(DB_PATH);
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, length);
        }
        outputStream.flush();
        outputStream.close();
        inputStream.close();
    }

    public void open() throws SQLiteException {
        db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        if (D) {
            Log.d(TAG, "onCreate");
        }
        onCreated = true;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (D) {
            Log.d(TAG, "onUpgrade");
        }
        onUpgraded = true;
    }

    public long insertTrackingBSSID(ContentValues values) {
        long result = db.insert(LogDbSchema.TABLE_LOG, null, values);

        return result;
    }

    public Cursor queryAll() {
        Cursor cursor = db.query(LogDbSchema.TABLE_LOG,
                new String[] {LogDbSchema.BSSID,
                              LogDbSchema.SSID,
                              LogDbSchema.CAPABILITIES,
                              LogDbSchema.FREQUENCY,
                              LogDbSchema.LEVEL,
                              LogDbSchema.TIME,
                              LogDbSchema.LOCATION}, null, null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            db.close();
            return cursor;
        }
        db.close();
        return  null;
    }

    public boolean isBssidSaved(String bssid) {
        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

        if (c.moveToFirst()) {
            while ( !c.isAfterLast() ) {
                System.out.println("NAME:" + c.getString(0));
                c.moveToNext();
            }
        }

        Cursor cursor = db.query(LogDbSchema.TABLE_LOG,
                new String[] {LogDbSchema.BSSID,
                        LogDbSchema.SSID,
                        LogDbSchema.CAPABILITIES,
                        LogDbSchema.FREQUENCY,
                        LogDbSchema.LEVEL,
                        LogDbSchema.TIME,
                        LogDbSchema.LOCATION}, LogDbSchema.BSSID + "=?", new String[] {bssid}, null, null, null);
        if (cursor != null) {
            if(cursor.moveToFirst()) {
                if(D) { Log.d(TAG, "is Favor"); }
                cursor.close();
                return true;
            } else {
                if(D) { Log.d(TAG, "not Favor"); }
                cursor.close();
                return false;
            }
        } else {
            if(D) Log.d(TAG, "query(id) : cursor null");
            cursor.close();
            return false;
        }
    }

    public String queryManufacture(String mac) {
        String query = mac.replace(":", "").substring(0, 6).toUpperCase();
        Cursor cursor = db.query(LogDbSchema.TABLE_MANUFACTURE,
                new String[] {LogDbSchema.MAC, LogDbSchema.MANUFACTURE},
                LogDbSchema.MAC + "=?", new String[] {query}, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                return cursor.getString(cursor.getColumnIndexOrThrow(LogDbSchema.MANUFACTURE));
            } else {
                return "";
            }
        } else {
            return "";
        }
    }

    public int delete(String bssid) {
        int result = db.delete(LogDbSchema.TABLE_LOG, LogDbSchema.BSSID + "=?", new String[]{bssid});
        db.close();
        return result;
    }

    @Override
    public synchronized void close() {
        if (db != null) {
            db.close();
        }
        super.close();
    }
}
