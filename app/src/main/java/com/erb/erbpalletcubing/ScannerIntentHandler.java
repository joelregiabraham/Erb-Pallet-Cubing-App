package com.erb.erbpalletcubing;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

/**
 * ScannerIntentHandler - Handles barcode scanner broadcasts from CT50
 * Wraps BroadcastReceiver with callback interface for easy integration
 */
public class ScannerIntentHandler {

    private static final String TAG = "ScannerIntentHandler";

    // CT50 Scanner Intent Actions
    private static final String INTENT_ACTION_PRIMARY = "com.erb.erbpalletcubing.SCAN";
    private static final String INTENT_ACTION_HONEYWELL = "com.honeywell.decode.intent.action.SCAN";
    private static final String INTENT_ACTION_DATAWEDGE = "com.symbol.datawedge.data";

    private Context context;
    private ScanCallback callback;
    private BroadcastReceiver receiver;
    private boolean isRegistered = false;

    /**
     * Callback interface for scan results
     */
    public interface ScanCallback {
        void onScanSuccess(String barcode, String barcodeType);
        void onScanError(String error);
    }

    public ScannerIntentHandler(Context context) {
        this.context = context;
    }

    /**
     * Register scanner and start listening for scan events
     */
    public void registerScanner(ScanCallback callback) {
        if (isRegistered) {
            Log.w(TAG, "Scanner already registered");
            return;
        }

        this.callback = callback;

        try {
            receiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    handleScanIntent(intent);
                }
            };

            // Register multiple intent filters to catch different scanner formats
            IntentFilter filter1 = new IntentFilter(INTENT_ACTION_PRIMARY);
            IntentFilter filter2 = new IntentFilter(INTENT_ACTION_HONEYWELL);
            IntentFilter filter3 = new IntentFilter(INTENT_ACTION_DATAWEDGE);

            context.registerReceiver(receiver, filter1);
            context.registerReceiver(receiver, filter2);
            context.registerReceiver(receiver, filter3);

            isRegistered = true;
            Log.d(TAG, "Scanner registered successfully");

        } catch (Exception e) {
            Log.e(TAG, "Error registering scanner: " + e.getMessage(), e);
            if (callback != null) {
                callback.onScanError("Failed to register scanner: " + e.getMessage());
            }
        }
    }

    /**
     * Unregister scanner and stop listening
     */
    public void unregisterScanner() {
        if (!isRegistered || receiver == null) {
            return;
        }

        try {
            context.unregisterReceiver(receiver);
            isRegistered = false;
            Log.d(TAG, "Scanner unregistered successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error unregistering scanner: " + e.getMessage(), e);
        }
    }

    /**
     * Handle scan intent and extract barcode data
     */
    private void handleScanIntent(Intent intent) {
        if (intent == null || callback == null) {
            return;
        }

        String scannedData = null;
        String barcodeType = null;

        try {
            // Method 1: DataWedge standard intent extras
            scannedData = intent.getStringExtra("com.symbol.datawedge.data_string");
            barcodeType = intent.getStringExtra("com.symbol.datawedge.label_type");

            // Method 2: Honeywell specific extras
            if (scannedData == null) {
                scannedData = intent.getStringExtra("data");
                barcodeType = intent.getStringExtra("codeId");
            }

            // Method 3: Generic barcode data
            if (scannedData == null) {
                scannedData = intent.getStringExtra("SCAN_BARCODE1");
                barcodeType = intent.getStringExtra("SCAN_BARCODE_TYPE");
            }

            // Method 4: Check for barcodeData
            if (scannedData == null) {
                scannedData = intent.getStringExtra("barcodeData");
            }

            if (scannedData != null && !scannedData.trim().isEmpty()) {
                final String finalData = scannedData.trim();
                final String finalType = (barcodeType != null) ? barcodeType : "Unknown";

                // Execute callback on UI thread
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onScanSuccess(finalData, finalType);
                    }
                });

                Log.d(TAG, "Scan successful: " + finalData + " (Type: " + finalType + ")");
            } else {
                Log.w(TAG, "Scan received but no data found");
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onScanError("No barcode data found");
                    }
                });
            }

        } catch (Exception e) {
            Log.e(TAG, "Error handling scan intent: " + e.getMessage(), e);
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    callback.onScanError("Error processing scan: " + e.getMessage());
                }
            });
        }
    }

    /**
     * Check if scanner is currently registered
     */
    public boolean isRegistered() {
        return isRegistered;
    }
}
