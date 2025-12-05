package com.erb.erbpalletcubing;

import android.content.Context;
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
     */
    public static File generateCsv(Context context, String trailerNumber) {
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        File csvFile = null;

        Log.d(TAG, "=== CSV EXPORT START ===");
        Log.d(TAG, "Trailer: " + trailerNumber);

        try {
            // 1. Use app-specific external storage
            File reportDir = new File(context.getExternalFilesDir(null), EXPORT_FOLDER);

            if (!reportDir.exists()) {
                boolean created = reportDir.mkdirs();
                if (!created) {
                    // Fallback to internal storage
                    reportDir = new File(context.getFilesDir(), EXPORT_FOLDER);
                    if (!reportDir.exists()) {
                        reportDir.mkdirs();
                    }
                }
            }

            // 2. Create CSV file
            String fileName = "CubingData_" + trailerNumber + ".csv";
            csvFile = new File(reportDir, fileName);

            // 3. Query database for all records
            List<DatabaseHelper.CubingRecord> records = dbHelper.getAllRecordsForTrailer(trailerNumber);

            if (records == null || records.isEmpty()) {
                Log.e(TAG, "No records found for trailer: " + trailerNumber);
                return null;
            }

            // 4. Write CSV content
            FileWriter writer = new FileWriter(csvFile);

            // --- UPDATED HEADER ROW ---
            // Changed order: Expected Pallets and Pallet Number moved BEFORE Freight Type/Temps
            writer.append("Timestamp,Terminal,Receiver,Trailer Number,PRO Number Incoming,PRO Prefix,PRO Number Erb,Expected Pallets for PRO,Pallet Number,Freight Type,Temp1,Temp2,Pallet Height,Condition,OS&D Reason,OS&D Quantity,OS&D Quantity Type,Status\n");

            // Write data rows
            for (DatabaseHelper.CubingRecord record : records) {
                // Standard details
                writer.append(escapeCSV(record.timestamp)).append(",");
                writer.append(escapeCSV(record.terminal)).append(",");
                writer.append(escapeCSV(record.receiver)).append(",");
                writer.append(escapeCSV(record.trailerNumber)).append(",");
                writer.append(escapeCSV(record.proNumberIncoming)).append(",");
                writer.append(escapeCSV(record.proPrefix)).append(",");
                writer.append(escapeCSV(record.proNumberErb)).append(",");

                // --- UPDATED DATA ORDER ---

                // 1. Expected Pallets for PRO
                writer.append(String.valueOf(record.expectedPalletsPro)).append(",");

                // 2. Pallet Number (Sequence)
                writer.append(String.valueOf(record.palletSequence)).append(",");

                // 3. Freight Type
                writer.append(escapeCSV(record.freightType)).append(",");

                // 4. Temp 1
                writer.append(formatTemperature(record.temp1)).append(",");

                // 5. Temp 2
                writer.append(formatTemperature(record.temp2)).append(",");

                // ---------------------------

                // Pallet Height
                writer.append(String.valueOf(record.palletHeight)).append(",");

                // Condition
                writer.append(escapeCSV(record.condition)).append(",");

                // OS&D fields
                writer.append(record.osdReason != null ? escapeCSV(record.osdReason) : "N/A").append(",");
                writer.append(record.osdQuantity != null ? String.valueOf(record.osdQuantity) : "N/A").append(",");
                writer.append(record.osdQuantityType != null ? escapeCSV(record.osdQuantityType) : "N/A").append(",");

                // Status
                writer.append(escapeCSV(record.status)).append("\n");
            }

            writer.flush();
            writer.close();

            Log.d(TAG, "CSV file created successfully: " + csvFile.getAbsolutePath());
            return csvFile;

        } catch (IOException e) {
            Log.e(TAG, "IOException: " + e.getMessage());
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Format temperature for CSV export
     */
    private static String formatTemperature(String temp) {
        if (temp == null || temp.trim().isEmpty()) {
            return "N/A";
        }
        return temp.trim() + "F";
    }

    /**
     * Escape CSV value
     */
    private static String escapeCSV(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    public static String getExportFolderPath(Context context) {
        File reportDir = new File(context.getExternalFilesDir(null), EXPORT_FOLDER);
        return reportDir.getAbsolutePath();
    }
}