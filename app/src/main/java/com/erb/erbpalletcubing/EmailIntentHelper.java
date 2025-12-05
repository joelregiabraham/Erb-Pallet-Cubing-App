package com.erb.erbpalletcubing;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import androidx.core.content.FileProvider;

import java.io.File;

/**
 * EmailIntentHelper - Send CSV files via email
 * Phase 5: Export & Email
 * Uses FileProvider for secure file sharing (Android 6.0+)
 */
public class EmailIntentHelper {

    private static final String TAG = "EmailIntentHelper";
    private static final String FILE_PROVIDER_AUTHORITY = "com.erb.erbpalletcubing.fileprovider";

    /**
     * Send CSV file via email intent
     * 
     * @param context Application context
     * @param csvFile File to attach
     * @param trailerNumber Trailer number for email subject
     * @return true if email intent launched successfully, false otherwise
     */
    public static boolean sendCsvEmail(Context context, File csvFile, String trailerNumber) {
        try {
            // Validate file exists
            if (csvFile == null || !csvFile.exists()) {
                Log.e(TAG, "CSV file does not exist");
                return false;
            }

            Log.d(TAG, "Preparing email for file: " + csvFile.getAbsolutePath());
            Log.d(TAG, "File size: " + csvFile.length() + " bytes");

            // Create content URI using FileProvider (secure for Android 6.0+)
            Uri fileUri = FileProvider.getUriForFile(
                    context,
                    FILE_PROVIDER_AUTHORITY,
                    csvFile
            );

            Log.d(TAG, "FileProvider URI: " + fileUri.toString());

            // Create email intent
            Intent emailIntent = new Intent(Intent.ACTION_SEND);
            emailIntent.setType("text/csv");
            
            // Set email subject
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Cubing Data - Trailer " + trailerNumber);
            
            // Set email body (simplified as per user request)
            emailIntent.putExtra(Intent.EXTRA_TEXT, "Attached cubing data for trailer " + trailerNumber);
            
            // Attach CSV file
            emailIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
            
            // Grant read permission to receiving app (e.g., Gmail)
            emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            // Create chooser to let user pick email app
            Intent chooser = Intent.createChooser(emailIntent, "Send CSV via Email");

            // Verify there's an app to handle the intent
            if (emailIntent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(chooser);
                Log.d(TAG, "Email intent launched successfully");
                return true;
            } else {
                Log.e(TAG, "No email app found");
                return false;
            }

        } catch (IllegalArgumentException e) {
            Log.e(TAG, "File not in FileProvider path: " + e.getMessage(), e);
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Error launching email intent: " + e.getMessage(), e);
            return false;
        }
    }
}
