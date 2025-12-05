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
     * Saves to: Downloads/CubingReports/CubingData_{TrailerNumber}.csv
     * 
     * @param context Application context
     * @param trailerNumber Trailer number to export
     * @return File object if successful, null if failed
     */
    public static File generateCsv(Context context, String trailerNumber) {
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        File csvFile = null;

        try {
            // 1. Check if external storage is writable
            if (!isExternalStorageWritable()) {
                Log.e(TAG, "External storage not writable");
                return null;
            }

            // 2. Create CubingReports folder in Downloads
            File downloadsDir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS);
            File reportDir = new File(downloadsDir, EXPORT_FOLDER);
            
            if (!reportDir.exists()) {
                boolean created = reportDir.mkdirs();
                if (!created) {
                    Log.e(TAG, "Failed to create directory: " + reportDir.getAbsolutePath());
                    return null;
                }
                Log.d(TAG, "Created directory: " + reportDir.getAbsolutePath());
            }

            // 3. Create CSV file
            String fileName = "CubingData_" + trailerNumber + ".csv";
            csvFile = new File(reportDir, fileName);
            Log.d(TAG, "Creating CSV file: " + csvFile.getAbsolutePath());

            // 4. Query database for all records
            List<DatabaseHelper.CubingRecord> records = dbHelper.getAllRecordsForTrailer(trailerNumber);
            
            if (records == null || records.isEmpty()) {
                Log.e(TAG, "No records found for trailer: " + trailerNumber);
                return null;
            }

            Log.d(TAG, "Retrieved " + records.size() + " records for export");

            // 5. Write CSV content
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
            Log.e(TAG, "Error creating CSV file: " + e.getMessage(), e);
            return null;
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error during CSV generation: " + e.getMessage(), e);
            return null;
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
     * Check if external storage is writable
     */
    private static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    /**
     * Get export folder path for display purposes
     */
    public static String getExportFolderPath() {
        File downloadsDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS);
        File reportDir = new File(downloadsDir, EXPORT_FOLDER);
        return reportDir.getAbsolutePath();
    }
}
