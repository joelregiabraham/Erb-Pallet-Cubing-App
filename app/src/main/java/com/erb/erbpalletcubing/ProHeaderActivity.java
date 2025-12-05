package com.erb.erbpalletcubing;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;

/**
 * ProHeaderActivity - PRO number entry and shipment configuration
 * Handles scanner input, freight type selection, and temperature entry
 */
public class ProHeaderActivity extends AppCompatActivity {

    private static final String TAG = "ProHeaderActivity";

    private TextView tvUserInfo;
    private EditText etProNumber;
    private EditText etExpectedPallets;
    private EditText etTemp1;
    private EditText etTemp2;
    private TextView tvTemp2Label;
    private Button btnStartPallets;
    private Button btnCancelTrailer;

    private Button btnFresh;
    private Button btnFrozen;
    private Button btnDual;

    private SessionManager sessionManager;
    private DatabaseHelper dbHelper;
    private ScannerIntentHandler scannerHandler;
    private ToggleButtonGroup toggleFreightType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pro_header);

        // Initialize managers
        sessionManager = new SessionManager(this);
        dbHelper = new DatabaseHelper(this);
        scannerHandler = new ScannerIntentHandler(this);

        // Check if user is logged in
        if (!sessionManager.isLoggedIn()) {
            navigateToLogin();
            return;
        }

        // Initialize views
        initializeViews();

        // Display user info
        displayUserInfo();

        // Setup scanner
        setupScanner();

        // Setup freight type toggle
        setupFreightTypeToggle();

        // Setup temperature formatting
        setupTemperatureFormatting();

        // Setup validation
        setupValidation();

        // Setup buttons
        setupButtons();

        // Check for resume state
        checkResumeState();
    }

    private void initializeViews() {
        tvUserInfo = findViewById(R.id.tvUserInfo);
        etProNumber = findViewById(R.id.etProNumber);
        etExpectedPallets = findViewById(R.id.etExpectedPallets);
        etTemp1 = findViewById(R.id.etTemp1);
        etTemp2 = findViewById(R.id.etTemp2);
        tvTemp2Label = findViewById(R.id.tvTemp2Label);
        btnStartPallets = findViewById(R.id.btnStartPallets);
        btnCancelTrailer = findViewById(R.id.btnCancelTrailer);

        btnFresh = findViewById(R.id.btnFresh);
        btnFrozen = findViewById(R.id.btnFrozen);
        btnDual = findViewById(R.id.btnDual);
    }

    private void displayUserInfo() {
        String terminal = sessionManager.getTerminalId();
        String receiver = sessionManager.getReceiverId();
        String trailer = sessionManager.getCurrentTrailer();

        String userInfo = "User: T-" + terminal + " | R-" + receiver + " | Trailer: " + trailer;
        tvUserInfo.setText(userInfo);
    }

    private void setupScanner() {
        // Register scanner with callback
        scannerHandler.registerScanner(new ScannerIntentHandler.ScanCallback() {
            @Override
            public void onScanSuccess(String barcode, String barcodeType) {
                handleScannedBarcode(barcode, barcodeType);
            }

            @Override
            public void onScanError(String error) {
                Toast.makeText(ProHeaderActivity.this,
                        "Scan error: " + error,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleScannedBarcode(String barcode, String barcodeType) {
        // Validate PRO number format (10 digits)
        if (ValidationHelper.isValidProNumber(barcode)) {
            etProNumber.setText(barcode);
            Toast.makeText(this, "PRO scanned: " + barcode, Toast.LENGTH_SHORT).show();

            // Auto-focus to next field
            etExpectedPallets.requestFocus();
        } else {
            Toast.makeText(this,
                    "Invalid PRO number. Must be 10 digits. Scanned: " + barcode,
                    Toast.LENGTH_LONG).show();
        }
    }

    private void setupFreightTypeToggle() {
        // Create toggle button group
        toggleFreightType = new ToggleButtonGroup(this);

        // Add buttons to toggle group
        toggleFreightType.addButton(btnFresh, "Fresh");
        toggleFreightType.addButton(btnFrozen, "Frozen");
        toggleFreightType.addButton(btnDual, "Dual");

        // Set selection change listener
        toggleFreightType.setOnSelectionChangeListener(new ToggleButtonGroup.OnSelectionChangeListener() {
            @Override
            public void onSelectionChanged(String selection) {
                handleFreightTypeSelection(selection);
            }
        });
    }

    private void handleFreightTypeSelection(String freightType) {
        if ("Dual".equalsIgnoreCase(freightType)) {
            // Show Temp 2 field
            tvTemp2Label.setVisibility(View.VISIBLE);
            etTemp2.setVisibility(View.VISIBLE);
        } else {
            // Hide Temp 2 field
            tvTemp2Label.setVisibility(View.GONE);
            etTemp2.setVisibility(View.GONE);
            etTemp2.setText(""); // Clear value
        }

        updateStartButtonState();
    }

    private void setupTemperatureFormatting() {
        // Format Temp 1 with °F suffix
        etTemp1.addTextChangedListener(new TextWatcher() {
            private boolean isFormatting = false;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (isFormatting) {
                    return;
                }

                isFormatting = true;

                String text = s.toString();

                // Remove existing °F if present
                if (text.endsWith("°F")) {
                    text = text.substring(0, text.length() - 2).trim();
                }

                // Add °F if not empty and is valid number
                if (!text.isEmpty()) {
                    try {
                        // Validate it's a number
                        Double.parseDouble(text);
                        s.clear();
                        s.append(text + "°F");
                    } catch (NumberFormatException e) {
                        // Invalid number, don't add °F
                    }
                }

                isFormatting = false;
            }
        });

        // Format Temp 2 with °F suffix
        etTemp2.addTextChangedListener(new TextWatcher() {
            private boolean isFormatting = false;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (isFormatting) {
                    return;
                }

                isFormatting = true;

                String text = s.toString();

                // Remove existing °F if present
                if (text.endsWith("°F")) {
                    text = text.substring(0, text.length() - 2).trim();
                }

                // Add °F if not empty and is valid number
                if (!text.isEmpty()) {
                    try {
                        // Validate it's a number
                        Double.parseDouble(text);
                        s.clear();
                        s.append(text + "°F");
                    } catch (NumberFormatException e) {
                        // Invalid number, don't add °F
                    }
                }

                isFormatting = false;
            }
        });
    }

    private void setupValidation() {
        // Add text watchers to all fields
        TextWatcher validationWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                updateStartButtonState();
            }
        };

        etProNumber.addTextChangedListener(validationWatcher);
        etExpectedPallets.addTextChangedListener(validationWatcher);
        etTemp1.addTextChangedListener(validationWatcher);
        etTemp2.addTextChangedListener(validationWatcher);
    }

    private void updateStartButtonState() {
        boolean isValid = validateAllFields();

        btnStartPallets.setEnabled(isValid);
        btnStartPallets.setAlpha(isValid ? 1.0f : 0.5f);
    }

    private boolean validateAllFields() {
        String proNumber = etProNumber.getText().toString().trim();
        String expectedPallets = etExpectedPallets.getText().toString().trim();
        String temp1 = getTemperatureValue(etTemp1.getText().toString());
        String freightType = toggleFreightType.getSelectedValue();

        // Validate PRO number (10 digits)
        if (!ValidationHelper.isValidProNumber(proNumber)) {
            return false;
        }

        // Validate expected pallets
        if (!ValidationHelper.isValidPalletCount(expectedPallets)) {
            return false;
        }

        // Validate freight type selected
        if (freightType == null) {
            return false;
        }

        // Validate Temp 1
        if (!ValidationHelper.isValidTemperature(temp1)) {
            return false;
        }

        // If DUAL selected, validate Temp 2
        if ("Dual".equalsIgnoreCase(freightType)) {
            String temp2 = getTemperatureValue(etTemp2.getText().toString());
            if (!ValidationHelper.isValidTemperature(temp2)) {
                return false;
            }
        }

        return true;
    }

    private void setupButtons() {
        // Start Pallets Button
        btnStartPallets.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleStartPallets();
            }
        });

        // Cancel Trailer Button
        btnCancelTrailer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCancelTrailerDialog();
            }
        });
    }

    private void handleStartPallets() {
        // Get all values
        String proNumber = etProNumber.getText().toString().trim();
        String expectedPalletsStr = etExpectedPallets.getText().toString().trim();
        String freightType = toggleFreightType.getSelectedValue();
        String temp1 = getTemperatureValue(etTemp1.getText().toString());
        String temp2 = null;

        if ("Dual".equalsIgnoreCase(freightType)) {
            temp2 = getTemperatureValue(etTemp2.getText().toString());
        }

        // Final validation
        if (!validateAllFields()) {
            Toast.makeText(this, "Please fill all required fields correctly",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int expectedPallets = Integer.parseInt(expectedPalletsStr);
            String trailer = sessionManager.getCurrentTrailer();

            // ===== DUPLICATE PRO CHECK (Phase 3 Update) =====
            int existingPalletCount = dbHelper.countPalletsForPro(trailer, proNumber);

            if (existingPalletCount > 0) {
                // Duplicate PRO detected!
                showDuplicateProDialog(proNumber, existingPalletCount, expectedPallets,
                        freightType, temp1, temp2);
                return; // Stop here, wait for user decision
            }
            // ===== END DUPLICATE PRO CHECK =====

            // No duplicate, proceed with normal flow
            proceedToStartPallets(proNumber, expectedPallets, freightType, temp1, temp2);

        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Proceed to start pallets (extracted from handleStartPallets for reuse)
     */
    private void proceedToStartPallets(String proNumber, int expectedPallets,
                                       String freightType, String temp1, String temp2) {
        try {
            // Extract PRO prefix and Erb
            String proPrefix = ValidationHelper.extractProPrefix(proNumber);
            String proErb = ValidationHelper.extractProErb(proNumber);

            // Save to SessionManager
            sessionManager.setCurrentPro(proNumber);
            sessionManager.setExpectedPallets(expectedPallets);
            sessionManager.setFreightType(freightType);
            sessionManager.setTemp1(temp1);
            sessionManager.setTemp2(temp2);
            sessionManager.setCurrentPalletIndex(1); // Start at pallet 1

            // Save resume state
            HashMap<String, String> resumeData = new HashMap<>();
            resumeData.put("pro", proNumber);
            resumeData.put("expectedPallets", String.valueOf(expectedPallets));
            resumeData.put("freightType", freightType);
            resumeData.put("temp1", temp1);
            if (temp2 != null) {
                resumeData.put("temp2", temp2);
            }
            sessionManager.saveResumeState("pro_header", resumeData);

            Toast.makeText(this, "PRO saved. Starting pallet entry...",
                    Toast.LENGTH_SHORT).show();

            // Navigate to Pallet Detail Activity
            navigateToPalletDetail();

        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Extract numeric temperature value from formatted string
     * "35°F" -> "35", "-10°F" -> "-10"
     */
    private String getTemperatureValue(String formatted) {
        if (formatted == null || formatted.trim().isEmpty()) {
            return "";
        }

        String value = formatted.trim();

        // Remove °F suffix if present
        if (value.endsWith("°F")) {
            value = value.substring(0, value.length() - 2).trim();
        }

        return value;
    }

    private void checkResumeState() {
        String currentPro = sessionManager.getCurrentPro();

        if (currentPro != null && !currentPro.isEmpty()) {
            // Resuming mid-PRO entry - pre-fill fields
            etProNumber.setText(currentPro);

            int expectedPallets = sessionManager.getExpectedPallets();
            if (expectedPallets > 0) {
                etExpectedPallets.setText(String.valueOf(expectedPallets));
            }

            String freightType = sessionManager.getFreightType();
            if (freightType != null) {
                toggleFreightType.selectByValue(freightType);
            }

            String temp1 = sessionManager.getTemp1();
            if (temp1 != null && !temp1.isEmpty()) {
                etTemp1.setText(temp1);
            }

            String temp2 = sessionManager.getTemp2();
            if (temp2 != null && !temp2.isEmpty()) {
                etTemp2.setText(temp2);
            }
        }
    }

    private void showCancelTrailerDialog() {
        String currentTrailer = sessionManager.getCurrentTrailer();

        new AlertDialog.Builder(this)
                .setTitle("Cancel Trailer")
                .setMessage("Cancel trailer " + currentTrailer + "? All data for this trailer will be deleted.")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        performCancelTrailer();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void performCancelTrailer() {
        String currentTrailer = sessionManager.getCurrentTrailer();

        if (currentTrailer == null || currentTrailer.isEmpty()) {
            Toast.makeText(this, "No trailer to cancel", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Delete trailer data from database
            int deletedRows = dbHelper.deleteByTrailerNumber(currentTrailer);

            // Clear resume state
            sessionManager.clearResumeState();

            Toast.makeText(this, "Trailer cancelled. " + deletedRows + " records deleted.",
                    Toast.LENGTH_SHORT).show();

            // Navigate back to Trailer Activity
            navigateToTrailer();

        } catch (Exception e) {
            Toast.makeText(this, "Error cancelling trailer: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    // ==================== DUPLICATE PRO DETECTION METHODS (Phase 3 Update) ====================

    /**
     * Show dialog when duplicate PRO is detected
     */
    private void showDuplicateProDialog(final String proNumber, final int existingCount,
                                        final int newExpectedPallets, final String freightType,
                                        final String temp1, final String temp2) {
        new AlertDialog.Builder(this)
                .setTitle("⚠️ PRO Already Exists!")
                .setMessage("PRO: " + proNumber + "\n" +
                        "Current: " + existingCount + " pallets entered\n\n" +
                        "What would you like to do?")
                .setPositiveButton("CONTINUE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // User wants to add more pallets
                        continueExistingPro(proNumber, existingCount, newExpectedPallets,
                                freightType, temp1, temp2);
                    }
                })
                .setNegativeButton("RESTART", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // User wants to delete and start over
                        restartPro(proNumber, newExpectedPallets, freightType, temp1, temp2);
                    }
                })
                .setCancelable(false) // Force user to choose
                .show();
    }

    /**
     * Continue existing PRO - validate and update with new values
     */
    private void continueExistingPro(String proNumber, int existingCount,
                                     int newExpectedPallets, String freightType,
                                     String temp1, String temp2) {
        // Validate: new expected pallets must be >= existing count
        if (newExpectedPallets < existingCount) {
            showInvalidExpectedPalletsDialog(proNumber, existingCount, newExpectedPallets);
            return;
        }

        String trailer = sessionManager.getCurrentTrailer();

        try {
            // Update all existing rows with new PRO data
            int updatedRows = dbHelper.updateProData(trailer, proNumber, newExpectedPallets,
                    freightType, temp1, temp2);

            // Get max sequence to continue from
            int maxSequence = dbHelper.getMaxPalletSequence(trailer, proNumber);

            // Set session to continue from next pallet
            sessionManager.setCurrentPalletIndex(maxSequence + 1);
            sessionManager.setExpectedPallets(newExpectedPallets);
            sessionManager.setCurrentPro(proNumber);
            sessionManager.setFreightType(freightType);
            sessionManager.setTemp1(temp1);
            sessionManager.setTemp2(temp2);

            // Save resume state
            HashMap<String, String> resumeData = new HashMap<>();
            resumeData.put("pro", proNumber);
            resumeData.put("expectedPallets", String.valueOf(newExpectedPallets));
            resumeData.put("freightType", freightType);
            resumeData.put("temp1", temp1);
            if (temp2 != null) {
                resumeData.put("temp2", temp2);
            }
            sessionManager.saveResumeState("pro_header", resumeData);

            Toast.makeText(this,
                    "Continuing PRO. Updated " + updatedRows + " pallets. Starting at pallet " + (maxSequence + 1),
                    Toast.LENGTH_SHORT).show();

            // Navigate to pallet entry
            navigateToPalletDetail();

        } catch (Exception e) {
            Toast.makeText(this, "Error continuing PRO: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Restart PRO - delete existing and start fresh
     */
    private void restartPro(final String proNumber, final int expectedPallets,
                            final String freightType, final String temp1, final String temp2) {
        String trailer = sessionManager.getCurrentTrailer();
        final int existingCount = dbHelper.countPalletsForPro(trailer, proNumber);

        // Show confirmation
        new AlertDialog.Builder(this)
                .setTitle("⚠️ Delete Existing Data?")
                .setMessage("This will delete " + existingCount + " pallets for\n" +
                        "PRO " + proNumber + "\n\n" +
                        "Are you sure?")
                .setPositiveButton("DELETE & RESTART", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        performRestart(proNumber, expectedPallets, freightType, temp1, temp2, existingCount);
                    }
                })
                .setNegativeButton("CANCEL", null)
                .show();
    }

    /**
     * Perform the actual restart after confirmation
     */
    private void performRestart(String proNumber, int expectedPallets, String freightType,
                                String temp1, String temp2, int existingCount) {
        String trailer = sessionManager.getCurrentTrailer();

        try {
            // Delete all pallets for this PRO
            int deletedRows = dbHelper.deleteByProNumber(trailer, proNumber);

            Toast.makeText(this, deletedRows + " pallets deleted. Starting fresh.",
                    Toast.LENGTH_SHORT).show();

            // Proceed normally with fresh start
            proceedToStartPallets(proNumber, expectedPallets, freightType, temp1, temp2);

        } catch (Exception e) {
            Toast.makeText(this, "Error restarting PRO: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Show error when new expected pallets is less than existing count
     */
    private void showInvalidExpectedPalletsDialog(final String proNumber,
                                                  final int existingCount,
                                                  final int newExpectedPallets) {
        new AlertDialog.Builder(this)
                .setTitle("⚠️ Invalid Expected Pallets!")
                .setMessage("Existing: " + existingCount + " pallets\n" +
                        "You entered: " + newExpectedPallets + " pallets\n\n" +
                        "Expected pallets cannot be less than existing count.")
                .setPositiveButton("GO BACK", null) // Just close dialog
                .setNegativeButton("ABANDON PRO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        abandonPro(proNumber);
                    }
                })
                .show();
    }

    /**
     * Abandon PRO - delete all existing pallets
     */
    private void abandonPro(String proNumber) {
        String trailer = sessionManager.getCurrentTrailer();

        try {
            int deletedRows = dbHelper.deleteByProNumber(trailer, proNumber);

            Toast.makeText(this, "PRO abandoned. " + deletedRows + " pallets deleted.",
                    Toast.LENGTH_SHORT).show();

            // Stay on this screen, user can re-enter PRO details

        } catch (Exception e) {
            Toast.makeText(this, "Error abandoning PRO: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    // ==================== END DUPLICATE PRO DETECTION METHODS ====================

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void navigateToTrailer() {
        Intent intent = new Intent(this, TrailerActivity.class);
        startActivity(intent);
        finish();
    }

    private void navigateToPalletDetail() {
        Intent intent = new Intent(this, PalletDetailActivity.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        // Show confirmation before going back
        new AlertDialog.Builder(this)
                .setTitle("Go Back?")
                .setMessage("Return to Trailer screen? Current PRO data will be saved.")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        navigateToTrailer();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Unregister scanner
        if (scannerHandler != null) {
            scannerHandler.unregisterScanner();
        }

        // Close database
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}