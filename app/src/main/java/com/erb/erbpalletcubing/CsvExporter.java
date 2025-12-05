package com.erb.erbpalletcubing;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * CsvExporter - Generate CSV files from cubing data
 * Phase 5: Export & Email
 */
public class CsvExporter {

    private static final String TAG = "CsvExporter";
    private static final String EXPORT_FOLDER = "CubingReports";

    /**
     * Generate CSV file for a trailer
     * Saves to: App-specific external storage (no permission needed on Android 6.0+)
     * Path: /storage/emulated/0/Android/data/com.erb.erbpalletcubing/files/CubingReports/
     *
     * @param context Application context
     * @param trailerNumber Trailer number to export
     * @return File object if successful, null if failed
     */
    public static File generateCsv(Context context, String trailerNumber) {
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        File csvFile = null;

        Log.d(TAG, "=== CSV EXPORT START ===");
        Log.d(TAG, "Trailer: " + trailerNumber);

        try {
            // 1. Use app-specific external storage (no permission needed!)
            // This directory is accessible via USB and automatically cleaned on uninstall
            File reportDir = new File(context.getExternalFilesDir(null), EXPORT_FOLDER);

            Log.d(TAG, "Report directory: " + reportDir.getAbsolutePath());

            if (!reportDir.exists()) {
                Log.d(TAG, "Directory doesn't exist, creating...");
                boolean created = reportDir.mkdirs();
                if (!created) {
                    Log.e(TAG, "Failed to create directory: " + reportDir.getAbsolutePath());

                    // Fallback to internal storage
                    Log.d(TAG, "Trying internal storage fallback...");
                    reportDir = new File(context.getFilesDir(), EXPORT_FOLDER);
                    Log.d(TAG, "Fallback directory: " + reportDir.getAbsolutePath());

                    if (!reportDir.exists()) {
                        created = reportDir.mkdirs();
                        if (!created) {
                            Log.e(TAG, "Failed to create fallback directory");
                            return null;
                        }
                    }
                }
                Log.d(TAG, "Created directory: " + reportDir.getAbsolutePath());
            } else {
                Log.d(TAG, "Directory already exists");
            }

            // 2. Create CSV file
            String fileName = "CubingData_" + trailerNumber + ".csv";
            csvFile = new File(reportDir, fileName);
            Log.d(TAG, "Creating CSV file: " + csvFile.getAbsolutePath());

            // 3. Query database for all records
            Log.d(TAG, "Querying database for trailer: " + trailerNumber);
            List<DatabaseHelper.CubingRecord> records = dbHelper.getAllRecordsForTrailer(trailerNumber);

            if (records == null) {
                Log.e(TAG, "getAllRecordsForTrailer returned NULL!");
                return null;
            }

            if (records.isEmpty()) {
                Log.e(TAG, "No records found for trailer: " + trailerNumber);
                return null;
            }

            Log.d(TAG, "Retrieved " + records.size() + " records for export");

            // 4. Write CSV content
            FileWriter writer = new FileWriter(csvFile);

            // Write header row (with spaces instead of underscores)
            writer.append("Timestamp,Terminal,Receiver,Trailer Number,PRO Number Incoming,PRO Prefix,PRO Number Erb,Freight Type,Temp1,Temp2,Expected Pallets for PRO,Pallet Number,Pallet Height,Condition,OS&D Reason,OS&D Quantity,OS&D Quantity Type,Status\n");

            // Write data rows
            for (DatabaseHelper.CubingRecord record : records) {
                writer.append(escapeCSV(record.timestamp)).append(",");
                writer.append(escapeCSV(record.terminal)).append(",");
                writer.append(escapeCSV(record.receiver)).append(",");
                writer.append(escapeCSV(record.trailerNumber)).append(",");
                writer.append(escapeCSV(record.proNumberIncoming)).append(",");
                writer.append(escapeCSV(record.proPrefix)).append(",");
                writer.append(escapeCSV(record.proNumberErb)).append(",");
                writer.append(escapeCSV(record.freightType)).append(",");

                // Temperature 1 - append "F"
                writer.append(formatTemperature(record.temp1)).append(",");

                // Temperature 2 - append "F" or "N/A" if null
                writer.append(formatTemperature(record.temp2)).append(",");

                writer.append(String.valueOf(record.expectedPalletsPro)).append(",");
                writer.append(String.valueOf(record.palletSequence)).append(",");
                writer.append(String.valueOf(record.palletHeight)).append(",");
                writer.append(escapeCSV(record.condition)).append(",");

                // OS&D fields - "N/A" if null
                writer.append(record.osdReason != null ? escapeCSV(record.osdReason) : "N/A").append(",");
                writer.append(record.osdQuantity != null ? String.valueOf(record.osdQuantity) : "N/A").append(",");
                writer.append(record.osdQuantityType != null ? escapeCSV(record.osdQuantityType) : "N/A").append(",");

                writer.append(escapeCSV(record.status)).append("\n");
            }

            writer.flush();
            writer.close();

            Log.d(TAG, "CSV file created successfully: " + csvFile.getAbsolutePath());
            Log.d(TAG, "File size: " + csvFile.length() + " bytes");

            return csvFile;

        } catch (IOException e) {
            Log.e(TAG, "========== CSV EXPORT FAILED ==========");
            Log.e(TAG, "IOException: " + e.getMessage());
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            Log.e(TAG, "========== CSV EXPORT FAILED ==========");
            Log.e(TAG, "Unexpected error: " + e.getMessage());
            e.printStackTrace();
            return null;
        } finally {
            Log.d(TAG, "=== CSV EXPORT END ===");
        }
    }

    /**
     * Format temperature for CSV export
     * "35" -> "35F", "-10" -> "-10F", null -> "N/A"
     */
    private static String formatTemperature(String temp) {
        if (temp == null || temp.trim().isEmpty()) {
            return "N/A";
        }
        return temp.trim() + "F";
    }

    /**
     * Escape CSV value - wrap in quotes if contains comma, quote, or newline
     */
    private static String escapeCSV(String value) {
        if (value == null) {
            return "";
        }

        // If value contains comma, quote, or newline, wrap in quotes and escape quotes
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }

        return value;
    }

    /**
     * Get export folder path for display purposes
     * @param context Application context
     * @return Path to export folder
     */
    public static String getExportFolderPath(Context context) {
        File reportDir = new File(context.getExternalFilesDir(null), EXPORT_FOLDER);
        return reportDir.getAbsolutePath();
    }
}