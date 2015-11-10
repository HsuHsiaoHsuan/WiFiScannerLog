package idv.hsu.wifiscannerlog.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class LogDbHelper extends SQLiteOpenHelper {
    private static final String TAG = LogDbHelper.class.getSimpleName();
    private static final boolean D = true;

    private SQLiteDatabase db;
    private Context mContext;

    private static final int VERSION = 1;

    public LogDbHelper(Context context) {
        super(context, LogDbSchema.TABLE_LOG, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        this.db = db;
        this.db.execSQL(
                "CREATE TABLE " + LogDbSchema.TABLE_LOG + "(" +
                        LogDbSchema.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        LogDbSchema.BSSID + " TEXT, " +
                        LogDbSchema.SSID + " TEXT, " +
                        LogDbSchema.CAPABILITIES + " TEXT, " +
                        LogDbSchema.FREQUENCY + " TEXT, " +
                        LogDbSchema.LEVEL + " TEXT, " +
                        LogDbSchema.TIME + " TEXT, " +
                        LogDbSchema.LOCATION + " TEXT);"
// http://stackoverflow.com/questions/754684/how-to-insert-a-sqlite-record-with-a-datetime-set-to-now-in-android-applicatio
        );
//        this.db.execSQL(
//                "CREATE TABLE " + LogDbSchema.TABLE_TRACE_BSSID + "(" +
//                        LogDbSchema.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
//                        LogDbSchema.BSSID + " TEXT NOT NULL);"
//        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + LogDbSchema.TABLE_LOG);
        onCreate(db);
    }

    public long insertTrackingBSSID(ContentValues values) {
        db = getWritableDatabase();
        long result = db.insert(LogDbSchema.TABLE_LOG, null, values);

        return result;
    }

//    public long insertFavorBSSID

    public Cursor queryAll() {
        db = getReadableDatabase();
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
        db = getReadableDatabase();
        Cursor cursor = db.query(LogDbSchema.TABLE_LOG,
                new String[] {LogDbSchema.BSSID,
                        LogDbSchema.SSID,
                        LogDbSchema.CAPABILITIES,
                        LogDbSchema.FREQUENCY,
                        LogDbSchema.LEVEL,
                        LogDbSchema.TIME,
                        LogDbSchema.LOCATION}, LogDbSchema.BSSID + "=?", new String[] {bssid}, null, null, null);
        if (cursor != null) {
            if(D) Log.d(TAG, "query(id) : cursor not null, count: " + cursor.getCount());
            if(cursor.moveToFirst()) {
                if(D) { Log.d(TAG, "return true"); }
                cursor.close();
                return true;
            } else {
                if(D) { Log.d(TAG, "return false"); }
                cursor.close();
                return false;
            }
        } else {
            if(D) Log.d(TAG, "query(id) : cursor null");
            cursor.close();
            return false;
        }
    }

    public int delete(String bssid) {
        db = getWritableDatabase();
        int result = db.delete(LogDbSchema.TABLE_LOG, LogDbSchema.BSSID + "=?", new String[]{bssid});
        db.close();
        return result;
    }

    public void close() {
        if (db != null) {
            db.close();
        }
        super.close();
    }
}
