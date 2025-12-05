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

    // ==================== DUPLICATE PRO DETECTION METHODS (Phase 3 Update) ====================

    /**
     * Count existing pallets for a specific PRO in a trailer
     * Used to detect duplicate PRO entries
     */
    public int countPalletsForPro(String trailerNumber, String proNumber) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        int count = 0;

        try {
            db = this.getReadableDatabase();
            cursor = db.rawQuery(
                    "SELECT COUNT(*) FROM " + TABLE_CUBING_DATA +
                            " WHERE " + COLUMN_TRAILER_NUMBER + " = ?" +
                            " AND " + COLUMN_PRO_NUMBER_INCOMING + " = ?",
                    new String[]{trailerNumber, proNumber}
            );

            if (cursor != null && cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }

            Log.d(TAG, "Pallet count for PRO " + proNumber + ": " + count);

        } catch (Exception e) {
            Log.e(TAG, "Error counting pallets for PRO: " + e.getMessage(), e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return count;
    }

    /**
     * Get maximum pallet sequence number for a specific PRO
     * Used when continuing an existing PRO
     */
    public int getMaxPalletSequence(String trailerNumber, String proNumber) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        int maxSequence = 0;

        try {
            db = this.getReadableDatabase();
            cursor = db.rawQuery(
                    "SELECT MAX(" + COLUMN_PALLET_SEQUENCE + ") FROM " + TABLE_CUBING_DATA +
                            " WHERE " + COLUMN_TRAILER_NUMBER + " = ?" +
                            " AND " + COLUMN_PRO_NUMBER_INCOMING + " = ?",
                    new String[]{trailerNumber, proNumber}
            );

            if (cursor != null && cursor.moveToFirst()) {
                maxSequence = cursor.getInt(0);
            }

            Log.d(TAG, "Max pallet sequence for PRO " + proNumber + ": " + maxSequence);

        } catch (Exception e) {
            Log.e(TAG, "Error getting max pallet sequence: " + e.getMessage(), e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return maxSequence;
    }

    /**
     * Get existing PRO data for pre-filling fields when continuing
     * Returns freight type, temps, and expected pallets from first pallet of PRO
     */
    public ProData getExistingProData(String trailerNumber, String proNumber) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        ProData proData = null;

        try {
            db = this.getReadableDatabase();
            cursor = db.query(
                    TABLE_CUBING_DATA,
                    new String[]{
                            COLUMN_FREIGHT_TYPE,
                            COLUMN_TEMP1,
                            COLUMN_TEMP2,
                            COLUMN_EXPECTED_PALLETS_PRO
                    },
                    COLUMN_TRAILER_NUMBER + " = ? AND " + COLUMN_PRO_NUMBER_INCOMING + " = ?",
                    new String[]{trailerNumber, proNumber},
                    null, null, null, "1"  // LIMIT 1
            );

            if (cursor != null && cursor.moveToFirst()) {
                proData = new ProData();
                proData.freightType = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FREIGHT_TYPE));
                proData.temp1 = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TEMP1));

                int temp2Index = cursor.getColumnIndexOrThrow(COLUMN_TEMP2);
                proData.temp2 = cursor.isNull(temp2Index) ? null : cursor.getString(temp2Index);

                proData.expectedPalletsPro = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_EXPECTED_PALLETS_PRO));

                Log.d(TAG, "Retrieved existing PRO data: " + proData.freightType + ", " +
                        proData.temp1 + ", Expected: " + proData.expectedPalletsPro);
            }

        } catch (Exception e) {
            Log.e(TAG, "Error getting existing PRO data: " + e.getMessage(), e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return proData;
    }

    /**
     * Update all existing pallet rows for a PRO with new data
     * Used when user modifies PRO details while continuing
     */
    public int updateProData(String trailerNumber, String proNumber,
                             int expectedPallets, String freightType,
                             String temp1, String temp2) {
        SQLiteDatabase db = null;
        int updatedRows = 0;

        try {
            db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(COLUMN_EXPECTED_PALLETS_PRO, expectedPallets);
            values.put(COLUMN_FREIGHT_TYPE, freightType);
            values.put(COLUMN_TEMP1, temp1);

            if (temp2 != null && !temp2.isEmpty()) {
                values.put(COLUMN_TEMP2, temp2);
            } else {
                values.putNull(COLUMN_TEMP2);
            }

            updatedRows = db.update(
                    TABLE_CUBING_DATA,
                    values,
                    COLUMN_TRAILER_NUMBER + " = ? AND " + COLUMN_PRO_NUMBER_INCOMING + " = ?",
                    new String[]{trailerNumber, proNumber}
            );

            Log.d(TAG, "Updated " + updatedRows + " rows for PRO: " + proNumber);

        } catch (Exception e) {
            Log.e(TAG, "Error updating PRO data: " + e.getMessage(), e);
        }

        return updatedRows;
    }

    /**
     * Helper class to hold PRO data for pre-filling
     */
    public static class ProData {
        public String freightType;
        public String temp1;
        public String temp2;
        public int expectedPalletsPro;
    }

    // ==================== END DUPLICATE PRO DETECTION METHODS ====================

    // ==================== SUMMARY SCREEN METHODS (Phase 5) ====================

    /**
     * Get all unique PROs for a trailer with their pallet counts
     * Used by SummaryActivity to display list of PROs
     */
    public List<ProSummary> getAllProsForTrailer(String trailerNumber) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        List<ProSummary> proList = new ArrayList<>();

        Log.d(TAG, "getAllProsForTrailer called with trailer: " + trailerNumber);

        try {
            db = this.getReadableDatabase();

            // DIAGNOSTIC: Check total row count
            Cursor countCursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_CUBING_DATA, null);
            if (countCursor != null && countCursor.moveToFirst()) {
                int totalRows = countCursor.getInt(0);
                Log.d(TAG, "Total rows in database: " + totalRows);
                countCursor.close();
            }

            // DIAGNOSTIC: Show all trailer numbers in database
            Cursor trailerCursor = db.rawQuery(
                    "SELECT DISTINCT " + COLUMN_TRAILER_NUMBER + " FROM " + TABLE_CUBING_DATA, null);
            if (trailerCursor != null) {
                Log.d(TAG, "Trailers in database: " + trailerCursor.getCount());
                if (trailerCursor.moveToFirst()) {
                    do {
                        String dbTrailer = trailerCursor.getString(0);
                        Log.d(TAG, "Found trailer: '" + dbTrailer + "' (length: " + dbTrailer.length() + ")");
                        Log.d(TAG, "Searching for: '" + trailerNumber + "' (length: " + trailerNumber.length() + ")");
                        Log.d(TAG, "Match: " + dbTrailer.equals(trailerNumber));
                    } while (trailerCursor.moveToNext());
                }
                trailerCursor.close();
            }

            // Query to get unique PROs with count
            String query = "SELECT " + COLUMN_PRO_NUMBER_INCOMING +
                    ", COUNT(*) as pallet_count" +
                    " FROM " + TABLE_CUBING_DATA +
                    " WHERE " + COLUMN_TRAILER_NUMBER + " = ?" +
                    " GROUP BY " + COLUMN_PRO_NUMBER_INCOMING +
                    " ORDER BY MIN(" + COLUMN_TIMESTAMP + ")"; // Order by first entry time

            Log.d(TAG, "Executing query: " + query);
            Log.d(TAG, "With trailer parameter: '" + trailerNumber + "'");

            cursor = db.rawQuery(query, new String[]{trailerNumber});

            Log.d(TAG, "Cursor count: " + (cursor != null ? cursor.getCount() : 0));

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    ProSummary summary = new ProSummary();
                    summary.proNumber = cursor.getString(0);
                    summary.palletCount = cursor.getInt(1);
                    proList.add(summary);
                    Log.d(TAG, "Added PRO: " + summary.proNumber + " with " + summary.palletCount + " pallets");
                } while (cursor.moveToNext());
            } else {
                Log.w(TAG, "Cursor is null or empty for trailer: " + trailerNumber);
            }

            Log.d(TAG, "Retrieved " + proList.size() + " PROs for trailer: " + trailerNumber);

        } catch (Exception e) {
            Log.e(TAG, "Error getting PROs for trailer: " + e.getMessage(), e);
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return proList;
    }

    /**
     * Get all records for a specific trailer (for CSV export)
     */
    public List<CubingRecord> getAllRecordsForTrailer(String trailerNumber) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        List<CubingRecord> records = new ArrayList<>();

        try {
            db = this.getReadableDatabase();
            cursor = db.query(
                    TABLE_CUBING_DATA,
                    null, // All columns
                    COLUMN_TRAILER_NUMBER + " = ?",
                    new String[]{trailerNumber},
                    null, null,
                    COLUMN_PRO_NUMBER_INCOMING + ", " + COLUMN_PALLET_SEQUENCE // Order by PRO then sequence
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
            Log.e(TAG, "Error getting records for trailer: " + e.getMessage(), e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return records;
    }

    /**
     * Helper class for PRO summary info
     */
    public static class ProSummary {
        public String proNumber;
        public int palletCount;
    }

    // ==================== END SUMMARY SCREEN METHODS ====================

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