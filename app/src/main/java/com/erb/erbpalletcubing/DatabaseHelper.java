package com.erb.erbpalletcubing;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * DatabaseHelper - Manages SQLite database for cubing data
 * Enterprise-grade with comprehensive error handling
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";

    // Database Info
    private static final String DATABASE_NAME = "ErbCubingDB.db";
    private static final int DATABASE_VERSION = 1;

    // Table Name
    public static final String TABLE_CUBING_DATA = "CubingData";

    // Column Names (18 columns as per specification)
    public static final String COLUMN_TIMESTAMP = "Timestamp";
    public static final String COLUMN_TERMINAL = "Terminal";
    public static final String COLUMN_RECEIVER = "Receiver";
    public static final String COLUMN_TRAILER_NUMBER = "TrailerNumber";
    public static final String COLUMN_PRO_NUMBER_INCOMING = "PRO_Number_Incoming";
    public static final String COLUMN_PRO_PREFIX = "PRO_Prefix";
    public static final String COLUMN_PRO_NUMBER_ERB = "PRO_Number_Erb";
    public static final String COLUMN_FREIGHT_TYPE = "FreightType";
    public static final String COLUMN_TEMP1 = "Temp1";
    public static final String COLUMN_TEMP2 = "Temp2";
    public static final String COLUMN_EXPECTED_PALLETS_PRO = "ExpectedPalletsPRO";
    public static final String COLUMN_PALLET_SEQUENCE = "PalletSequence";
    public static final String COLUMN_PALLET_HEIGHT = "PalletHeight";
    public static final String COLUMN_CONDITION = "Condition";
    public static final String COLUMN_OSD_REASON = "OSD_Reason";
    public static final String COLUMN_OSD_QUANTITY = "OSD_Quantity";
    public static final String COLUMN_OSD_QUANTITY_TYPE = "OSD_QuantityType";
    public static final String COLUMN_STATUS = "Status";

    // SQL Create Table Statement
    private static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + TABLE_CUBING_DATA + " (" +
                    COLUMN_TIMESTAMP + " TEXT NOT NULL, " +
                    COLUMN_TERMINAL + " TEXT NOT NULL, " +
                    COLUMN_RECEIVER + " TEXT NOT NULL, " +
                    COLUMN_TRAILER_NUMBER + " TEXT NOT NULL, " +
                    COLUMN_PRO_NUMBER_INCOMING + " TEXT NOT NULL, " +
                    COLUMN_PRO_PREFIX + " TEXT NOT NULL, " +
                    COLUMN_PRO_NUMBER_ERB + " TEXT NOT NULL, " +
                    COLUMN_FREIGHT_TYPE + " TEXT NOT NULL, " +
                    COLUMN_TEMP1 + " TEXT NOT NULL, " +
                    COLUMN_TEMP2 + " TEXT, " +  // Nullable
                    COLUMN_EXPECTED_PALLETS_PRO + " INTEGER NOT NULL, " +
                    COLUMN_PALLET_SEQUENCE + " INTEGER NOT NULL, " +
                    COLUMN_PALLET_HEIGHT + " INTEGER NOT NULL, " +
                    COLUMN_CONDITION + " TEXT NOT NULL, " +
                    COLUMN_OSD_REASON + " TEXT, " +  // Nullable
                    COLUMN_OSD_QUANTITY + " INTEGER, " +  // Nullable
                    COLUMN_OSD_QUANTITY_TYPE + " TEXT, " +  // Nullable
                    COLUMN_STATUS + " TEXT NOT NULL)";

    // SQL Drop Table Statement
    private static final String SQL_DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_CUBING_DATA;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL(SQL_CREATE_TABLE);
            Log.d(TAG, "Database table created successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error creating database table: " + e.getMessage(), e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            db.execSQL(SQL_DROP_TABLE);
            onCreate(db);
            Log.d(TAG, "Database upgraded from version " + oldVersion + " to " + newVersion);
        } catch (Exception e) {
            Log.e(TAG, "Error upgrading database: " + e.getMessage(), e);
        }
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    /**
     * Insert a pallet record into the database
     * @return Row ID of inserted record, or -1 if error
     */
    public long insertPalletRecord(
            String terminal,
            String receiver,
            String trailerNumber,
            String proNumberIncoming,
            String proPrefix,
            String proNumberErb,
            String freightType,
            String temp1,
            String temp2,
            int expectedPalletsPro,
            int palletSequence,
            int palletHeight,
            String condition,
            String osdReason,
            Integer osdQuantity,
            String osdQuantityType) {

        SQLiteDatabase db = null;
        long result = -1;

        try {
            db = this.getWritableDatabase();
            ContentValues values = new ContentValues();

            // Required fields
            values.put(COLUMN_TIMESTAMP, getCurrentTimestamp());
            values.put(COLUMN_TERMINAL, terminal);
            values.put(COLUMN_RECEIVER, receiver);
            values.put(COLUMN_TRAILER_NUMBER, trailerNumber);
            values.put(COLUMN_PRO_NUMBER_INCOMING, proNumberIncoming);
            values.put(COLUMN_PRO_PREFIX, proPrefix);
            values.put(COLUMN_PRO_NUMBER_ERB, proNumberErb);
            values.put(COLUMN_FREIGHT_TYPE, freightType);
            values.put(COLUMN_TEMP1, temp1);
            values.put(COLUMN_EXPECTED_PALLETS_PRO, expectedPalletsPro);
            values.put(COLUMN_PALLET_SEQUENCE, palletSequence);
            values.put(COLUMN_PALLET_HEIGHT, palletHeight);
            values.put(COLUMN_CONDITION, condition);
            values.put(COLUMN_STATUS, "NEW");

            // Nullable fields
            if (temp2 != null && !temp2.trim().isEmpty()) {
                values.put(COLUMN_TEMP2, temp2);
            } else {
                values.putNull(COLUMN_TEMP2);
            }

            if (osdReason != null && !osdReason.trim().isEmpty()) {
                values.put(COLUMN_OSD_REASON, osdReason);
            } else {
                values.putNull(COLUMN_OSD_REASON);
            }

            if (osdQuantity != null) {
                values.put(COLUMN_OSD_QUANTITY, osdQuantity);
            } else {
                values.putNull(COLUMN_OSD_QUANTITY);
            }

            if (osdQuantityType != null && !osdQuantityType.trim().isEmpty()) {
                values.put(COLUMN_OSD_QUANTITY_TYPE, osdQuantityType);
            } else {
                values.putNull(COLUMN_OSD_QUANTITY_TYPE);
            }

            result = db.insert(TABLE_CUBING_DATA, null, values);

            if (result != -1) {
                Log.d(TAG, "Pallet record inserted successfully. Row ID: " + result);
            } else {
                Log.e(TAG, "Failed to insert pallet record");
            }

        } catch (Exception e) {
            Log.e(TAG, "Error inserting pallet record: " + e.getMessage(), e);
            result = -1;
        }

        return result;
    }

    /**
     * Get all records for a specific trailer
     */
    public List<CubingRecord> getRecordsByTrailer(String trailerNumber) {
        List<CubingRecord> records = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = this.getReadableDatabase();
            cursor = db.query(
                    TABLE_CUBING_DATA,
                    null,
                    COLUMN_TRAILER_NUMBER + " = ?",
                    new String[]{trailerNumber},
                    null,
                    null,
                    COLUMN_PALLET_SEQUENCE + " ASC"
            );

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    CubingRecord record = cursorToRecord(cursor);
                    if (record != null) {
                        records.add(record);
                    }
                } while (cursor.moveToNext());
            }

            Log.d(TAG, "Retrieved " + records.size() + " records for trailer: " + trailerNumber);

        } catch (Exception e) {
            Log.e(TAG, "Error querying records by trailer: " + e.getMessage(), e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return records;
    }

    /**
     * Delete all records for a specific trailer
     */
    public int deleteByTrailerNumber(String trailerNumber) {
        SQLiteDatabase db = null;
        int deletedRows = 0;

        try {
            db = this.getWritableDatabase();
            deletedRows = db.delete(
                    TABLE_CUBING_DATA,
                    COLUMN_TRAILER_NUMBER + " = ?",
                    new String[]{trailerNumber}
            );

            Log.d(TAG, "Deleted " + deletedRows + " records for trailer: " + trailerNumber);

        } catch (Exception e) {
            Log.e(TAG, "Error deleting records by trailer: " + e.getMessage(), e);
        }

        return deletedRows;
    }

    /**
     * Delete all records for a specific PRO number
     * Used for "ABANDON PRO" functionality
     */
    public int deleteByProNumber(String trailerNumber, String proNumberIncoming) {
        SQLiteDatabase db = null;
        int deletedRows = 0;

        try {
            db = this.getWritableDatabase();
            deletedRows = db.delete(
                    TABLE_CUBING_DATA,
                    COLUMN_TRAILER_NUMBER + " = ? AND " + COLUMN_PRO_NUMBER_INCOMING + " = ?",
                    new String[]{trailerNumber, proNumberIncoming}
            );

            Log.d(TAG, "Deleted " + deletedRows + " records for PRO: " + proNumberIncoming);

        } catch (Exception e) {
            Log.e(TAG, "Error deleting records by PRO: " + e.getMessage(), e);
        }

        return deletedRows;
    }

    /**
     * Delete all records in the database
     */
    public int deleteAllRecords() {
        SQLiteDatabase db = null;
        int deletedRows = 0;

        try {
            db = this.getWritableDatabase();
            deletedRows = db.delete(TABLE_CUBING_DATA, null, null);

            Log.d(TAG, "Deleted all records. Total: " + deletedRows);

        } catch (Exception e) {
            Log.e(TAG, "Error deleting all records: " + e.getMessage(), e);
        }

        return deletedRows;
    }

    /**
     * Get count of records for a specific trailer
     */
    public int getRecordCountByTrailer(String trailerNumber) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        int count = 0;

        try {
            db = this.getReadableDatabase();
            cursor = db.rawQuery(
                    "SELECT COUNT(*) FROM " + TABLE_CUBING_DATA +
                            " WHERE " + COLUMN_TRAILER_NUMBER + " = ?",
                    new String[]{trailerNumber}
            );

            if (cursor != null && cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }

        } catch (Exception e) {
            Log.e(TAG, "Error getting record count: " + e.getMessage(), e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return count;
    }

    /**
     * Get count of records for a specific PRO
     */
    public int getRecordCountByPro(String proNumberIncoming) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        int count = 0;

        try {
            db = this.getReadableDatabase();
            cursor = db.rawQuery(
                    "SELECT COUNT(*) FROM " + TABLE_CUBING_DATA +
                            " WHERE " + COLUMN_PRO_NUMBER_INCOMING + " = ?",
                    new String[]{proNumberIncoming}
            );

            if (cursor != null && cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }

        } catch (Exception e) {
            Log.e(TAG, "Error getting PRO record count: " + e.getMessage(), e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return count;
    }

    /**
     * Convert cursor to CubingRecord object
     */
    private CubingRecord cursorToRecord(Cursor cursor) {
        try {
            CubingRecord record = new CubingRecord();

            record.timestamp = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP));
            record.terminal = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TERMINAL));
            record.receiver = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RECEIVER));
            record.trailerNumber = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TRAILER_NUMBER));
            record.proNumberIncoming = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRO_NUMBER_INCOMING));
            record.proPrefix = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRO_PREFIX));
            record.proNumberErb = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRO_NUMBER_ERB));
            record.freightType = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FREIGHT_TYPE));
            record.temp1 = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TEMP1));

            int temp2Index = cursor.getColumnIndexOrThrow(COLUMN_TEMP2);
            record.temp2 = cursor.isNull(temp2Index) ? null : cursor.getString(temp2Index);

            record.expectedPalletsPro = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_EXPECTED_PALLETS_PRO));
            record.palletSequence = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PALLET_SEQUENCE));
            record.palletHeight = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PALLET_HEIGHT));
            record.condition = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONDITION));

            int osdReasonIndex = cursor.getColumnIndexOrThrow(COLUMN_OSD_REASON);
            record.osdReason = cursor.isNull(osdReasonIndex) ? null : cursor.getString(osdReasonIndex);

            int osdQuantityIndex = cursor.getColumnIndexOrThrow(COLUMN_OSD_QUANTITY);
            record.osdQuantity = cursor.isNull(osdQuantityIndex) ? null : cursor.getInt(osdQuantityIndex);

            int osdQuantityTypeIndex = cursor.getColumnIndexOrThrow(COLUMN_OSD_QUANTITY_TYPE);
            record.osdQuantityType = cursor.isNull(osdQuantityTypeIndex) ? null : cursor.getString(osdQuantityTypeIndex);

            record.status = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STATUS));

            return record;

        } catch (Exception e) {
            Log.e(TAG, "Error converting cursor to record: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Get current timestamp in the required format
     */
    private String getCurrentTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }

    /**
     * Inner class to represent a cubing record
     */
    public static class CubingRecord {
        public String timestamp;
        public String terminal;
        public String receiver;
        public String trailerNumber;
        public String proNumberIncoming;
        public String proPrefix;
        public String proNumberErb;
        public String freightType;
        public String temp1;
        public String temp2;
        public int expectedPalletsPro;
        public int palletSequence;
        public int palletHeight;
        public String condition;
        public String osdReason;
        public Integer osdQuantity;
        public String osdQuantityType;
        public String status;
    }
}