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

/**
 * PalletDetailActivity - Pallet entry screen with OS&D handling
 * Phase 4: Forward-only workflow with verification dialog
 */
public class PalletDetailActivity extends AppCompatActivity {

    private static final String TAG = "PalletDetailActivity";

    private TextView tvProgress;
    private EditText etPalletHeight;
    private Button btnConditionOk;
    private Button btnConditionOsd;
    private LinearLayout panelOsd;
    private Button btnSelectReason;
    private EditText etCustomReason;
    private EditText etOsdQuantity;
    private Button btnQuantityCases;
    private Button btnQuantityPallets;
    private Button btnSaveAndNext;
    private Button btnAbandonPro;

    private SessionManager sessionManager;
    private DatabaseHelper dbHelper;
    private ToggleButtonGroup toggleCondition;
    private ToggleButtonGroup toggleQuantityType;

    private String selectedReason = null;
    private boolean isSaving = false;  // Duplicate prevention flag

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pallet_detail);

        // Initialize managers
        sessionManager = new SessionManager(this);
        dbHelper = new DatabaseHelper(this);

        // Check if user is logged in
        if (!sessionManager.isLoggedIn()) {
            navigateToLogin();
            return;
        }

        // Initialize views
        initializeViews();

        // Setup condition toggle
        setupConditionToggle();

        // Setup quantity type toggle
        setupQuantityTypeToggle();

        // Setup reason selection
        setupReasonSelection();

        // Setup validation
        setupValidation();

        // Setup buttons
        setupButtons();

        // Update progress display
        updateProgress();
    }

    private void initializeViews() {
        tvProgress = findViewById(R.id.tvProgress);
        etPalletHeight = findViewById(R.id.etPalletHeight);
        btnConditionOk = findViewById(R.id.btnConditionOk);
        btnConditionOsd = findViewById(R.id.btnConditionOsd);
        panelOsd = findViewById(R.id.panelOsd);
        btnSelectReason = findViewById(R.id.btnSelectReason);
        etCustomReason = findViewById(R.id.etCustomReason);
        etOsdQuantity = findViewById(R.id.etOsdQuantity);
        btnQuantityCases = findViewById(R.id.btnQuantityCases);
        btnQuantityPallets = findViewById(R.id.btnQuantityPallets);
        btnSaveAndNext = findViewById(R.id.btnSaveAndNext);
        btnAbandonPro = findViewById(R.id.btnAbandonPro);
    }

    private void setupConditionToggle() {
        // Create toggle group for OK / OS&D
        toggleCondition = new ToggleButtonGroup(this);
        toggleCondition.addButton(btnConditionOk, "OK");
        toggleCondition.addButton(btnConditionOsd, "OS&D");

        // Set colors
        toggleCondition.setSelectedColor(0xFF4CAF50);  // Green for OK
        toggleCondition.setUnselectedColor(0xFFE0E0E0);

        // Set selection change listener
        toggleCondition.setOnSelectionChangeListener(new ToggleButtonGroup.OnSelectionChangeListener() {
            @Override
            public void onSelectionChanged(String selection) {
                handleConditionSelection(selection);
            }
        });

        // Override selected color for OS&D button specifically
        btnConditionOsd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleCondition.selectButton(btnConditionOsd, "OS&D");
                // Change to orange for OS&D
                btnConditionOsd.setBackgroundColor(0xFFFF9800);
                btnConditionOsd.setTextColor(0xFFFFFFFF);
            }
        });
    }

    private void setupQuantityTypeToggle() {
        // Create toggle group for CASES / PALLETS
        toggleQuantityType = new ToggleButtonGroup(this);
        toggleQuantityType.addButton(btnQuantityCases, "Cases");
        toggleQuantityType.addButton(btnQuantityPallets, "Pallets");

        // CRITICAL FIX: Add listener to update button state when selection changes
        toggleQuantityType.setOnSelectionChangeListener(new ToggleButtonGroup.OnSelectionChangeListener() {
            @Override
            public void onSelectionChanged(String selection) {
                // Trigger validation update when quantity type is selected
                updateSaveButtonState();
            }
        });
    }

    private void handleConditionSelection(String condition) {
        if ("OS&D".equalsIgnoreCase(condition)) {
            // Show OS&D panel
            panelOsd.setVisibility(View.VISIBLE);
        } else {
            // Hide OS&D panel
            panelOsd.setVisibility(View.GONE);
            // Clear OS&D fields
            clearOsdFields();
        }

        updateSaveButtonState();
    }

    private void setupReasonSelection() {
        btnSelectReason.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showReasonDialog();
            }
        });
    }

    private void showReasonDialog() {
        final String[] reasons = {"Damaged", "Short", "Over", "Misloaded", "Other"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select OS&D Reason");
        builder.setSingleChoiceItems(reasons, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                selectedReason = reasons[which];
                btnSelectReason.setText(selectedReason);

                // Show custom reason field if "Other" selected
                if ("Other".equalsIgnoreCase(selectedReason)) {
                    etCustomReason.setVisibility(View.VISIBLE);
                } else {
                    etCustomReason.setVisibility(View.GONE);
                    etCustomReason.setText("");
                }

                updateSaveButtonState();
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
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
                updateSaveButtonState();
            }
        };

        etPalletHeight.addTextChangedListener(validationWatcher);
        etOsdQuantity.addTextChangedListener(validationWatcher);
        etCustomReason.addTextChangedListener(validationWatcher);
    }

    private void updateSaveButtonState() {
        boolean isValid = validateAllFields();

        btnSaveAndNext.setEnabled(isValid && !isSaving);
        btnSaveAndNext.setAlpha(isValid && !isSaving ? 1.0f : 0.5f);
    }

    private boolean validateAllFields() {
        // Validate pallet height
        String heightStr = etPalletHeight.getText().toString().trim();
        if (!ValidationHelper.isValidPalletHeight(heightStr)) {
            return false;
        }

        // Validate condition selected
        if (!toggleCondition.hasSelection()) {
            return false;
        }

        String condition = toggleCondition.getSelectedValue();

        // If OS&D selected, validate OS&D fields
        if ("OS&D".equalsIgnoreCase(condition)) {
            // Validate reason selected
            if (selectedReason == null || selectedReason.trim().isEmpty()) {
                return false;
            }

            // If "Other" selected, validate custom reason entered
            if ("Other".equalsIgnoreCase(selectedReason)) {
                String customReason = etCustomReason.getText().toString().trim();
                if (customReason.isEmpty()) {
                    return false;
                }
            }

            // Validate quantity
            String quantityStr = etOsdQuantity.getText().toString().trim();
            if (!ValidationHelper.isValidQuantity(quantityStr)) {
                return false;
            }

            // Validate quantity type selected
            if (!toggleQuantityType.hasSelection()) {
                return false;
            }
        }

        return true;
    }

    private void setupButtons() {
        // Save & Next Pallet Button
        btnSaveAndNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showVerificationDialog();
            }
        });

        // Abandon PRO Button
        btnAbandonPro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAbandonProDialog();
            }
        });
    }

    private void showVerificationDialog() {
        // Build verification message
        StringBuilder message = new StringBuilder();

        int currentIndex = sessionManager.getCurrentPalletIndex();
        int expectedPallets = sessionManager.getExpectedPallets();

        message.append("Save Pallet ").append(currentIndex).append(" of ").append(expectedPallets).append("?\n\n");

        String height = etPalletHeight.getText().toString().trim();
        message.append("Height: ").append(height).append("\"\n");

        String condition = toggleCondition.getSelectedValue();
        message.append("Condition: ").append(condition);

        if ("OS&D".equalsIgnoreCase(condition)) {
            message.append(" - ").append(selectedReason);

            if ("Other".equalsIgnoreCase(selectedReason)) {
                String customReason = etCustomReason.getText().toString().trim();
                message.append(" (").append(customReason).append(")");
            }

            String quantity = etOsdQuantity.getText().toString().trim();
            String quantityType = toggleQuantityType.getSelectedValue();
            message.append("\nQuantity: ").append(quantity).append(" ").append(quantityType);
        }

        // Show dialog
        new AlertDialog.Builder(this)
                .setTitle("Verify Pallet Data")
                .setMessage(message.toString())
                .setPositiveButton("SAVE & CONTINUE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        performSave();
                    }
                })
                .setNegativeButton("EDIT", null)
                .show();
    }

    private void performSave() {
        // CRITICAL: Disable button immediately to prevent duplicates
        isSaving = true;
        btnSaveAndNext.setEnabled(false);
        btnSaveAndNext.setAlpha(0.5f);

        try {
            // Get all values
            String heightStr = etPalletHeight.getText().toString().trim();
            int palletHeight = Integer.parseInt(heightStr);
            String condition = toggleCondition.getSelectedValue();

            // Get session data
            String terminal = sessionManager.getTerminalId();
            String receiver = sessionManager.getReceiverId();
            String trailerNumber = sessionManager.getCurrentTrailer();
            String proNumber = sessionManager.getCurrentPro();
            String freightType = sessionManager.getFreightType();
            String temp1 = sessionManager.getTemp1();
            String temp2 = sessionManager.getTemp2();
            int expectedPallets = sessionManager.getExpectedPallets();
            int currentIndex = sessionManager.getCurrentPalletIndex();

            // Extract PRO prefix and Erb
            String proPrefix = ValidationHelper.extractProPrefix(proNumber);
            String proErb = ValidationHelper.extractProErb(proNumber);

            // OS&D fields (nullable)
            String osdReason = null;
            Integer osdQuantity = null;
            String osdQuantityType = null;

            if ("OS&D".equalsIgnoreCase(condition)) {
                // Build OS&D reason (with "Other: " prefix if applicable)
                if ("Other".equalsIgnoreCase(selectedReason)) {
                    String customReason = etCustomReason.getText().toString().trim();
                    osdReason = "Other: " + customReason;
                } else {
                    osdReason = selectedReason;
                }

                String quantityStr = etOsdQuantity.getText().toString().trim();
                osdQuantity = Integer.parseInt(quantityStr);
                osdQuantityType = toggleQuantityType.getSelectedValue();
            }

            // Insert into database
            long rowId = dbHelper.insertPalletRecord(
                    terminal,
                    receiver,
                    trailerNumber,
                    proNumber,
                    proPrefix,
                    proErb,
                    freightType,
                    temp1,
                    temp2,
                    expectedPallets,
                    currentIndex,
                    palletHeight,
                    condition,
                    osdReason,
                    osdQuantity,
                    osdQuantityType
            );

            if (rowId != -1) {
                // Success! Check if last pallet
                if (currentIndex >= expectedPallets) {
                    // LAST PALLET - Increment and CLEAR resume state
                    sessionManager.setCurrentPalletIndex(currentIndex + 1);
                    sessionManager.clearResumeState();  // Critical: Don't resume to invalid state

                    // Navigate to Summary immediately
                    navigateToSummary();
                } else {
                    // NOT LAST PALLET - Increment and save resume state
                    sessionManager.setCurrentPalletIndex(currentIndex + 1);
                    sessionManager.saveResumeState("pallet_detail", null);

                    // Reset form for next pallet (immediate, no delay)
                    Toast.makeText(this, "Pallet saved!", Toast.LENGTH_SHORT).show();
                    resetForm();
                    isSaving = false;  // Re-enable for next pallet
                }
            } else {
                // Insert failed - re-enable button
                Toast.makeText(this, "Error saving pallet. Please try again.",
                        Toast.LENGTH_LONG).show();
                isSaving = false;
                btnSaveAndNext.setEnabled(true);
                btnSaveAndNext.setAlpha(1.0f);
            }

        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            isSaving = false;
            btnSaveAndNext.setEnabled(true);
            btnSaveAndNext.setAlpha(1.0f);
        }
    }

    private void resetForm() {
        // Clear all input fields
        etPalletHeight.setText("");
        etOsdQuantity.setText("");
        etCustomReason.setText("");

        // Reset toggles
        toggleCondition.clearSelection();
        toggleQuantityType.clearSelection();

        // Hide OS&D panel
        panelOsd.setVisibility(View.GONE);
        etCustomReason.setVisibility(View.GONE);

        // Reset reason
        selectedReason = null;
        btnSelectReason.setText("Select Reason");

        // Update progress
        updateProgress();

        // Focus on pallet height
        etPalletHeight.requestFocus();

        // Update button state
        updateSaveButtonState();
    }

    private void clearOsdFields() {
        etOsdQuantity.setText("");
        etCustomReason.setText("");
        etCustomReason.setVisibility(View.GONE);
        selectedReason = null;
        btnSelectReason.setText("Select Reason");
        toggleQuantityType.clearSelection();
    }

    private void updateProgress() {
        int currentIndex = sessionManager.getCurrentPalletIndex();
        int expectedPallets = sessionManager.getExpectedPallets();

        String progressText = "PALLET " + currentIndex + " of " + expectedPallets;
        tvProgress.setText(progressText);
    }

    private void showAbandonProDialog() {
        String currentPro = sessionManager.getCurrentPro();

        new AlertDialog.Builder(this)
                .setTitle("Abandon PRO")
                .setMessage("Abandon PRO " + currentPro + "? All pallet data for this PRO will be deleted.")
                .setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        performAbandonPro();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void performAbandonPro() {
        String currentTrailer = sessionManager.getCurrentTrailer();
        String currentPro = sessionManager.getCurrentPro();

        try {
            // Delete all pallet records for current PRO
            int deletedRows = dbHelper.deleteByProNumber(currentTrailer, currentPro);

            // Reset pallet index to 1
            sessionManager.setCurrentPalletIndex(1);

            Toast.makeText(this, "PRO abandoned. " + deletedRows + " records deleted.",
                    Toast.LENGTH_SHORT).show();

            // Navigate back to PRO Header
            navigateToProHeader();

        } catch (Exception e) {
            Toast.makeText(this, "Error abandoning PRO: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void navigateToProHeader() {
        Intent intent = new Intent(this, ProHeaderActivity.class);
        startActivity(intent);
        finish();
    }

    private void navigateToSummary() {
        // TODO: Phase 5 - uncomment when SummaryActivity is created
        // Intent intent = new Intent(this, SummaryActivity.class);
        // startActivity(intent);
        // finish();

        // For now, show toast
        Toast.makeText(this, "All pallets complete! SummaryActivity not yet implemented (Phase 5)",
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void onBackPressed() {
        // Show confirmation before going back
        // IMPORTANT: Delete PRO pallets to prevent partial data in export
        new AlertDialog.Builder(this)
                .setTitle("Go Back?")
                .setMessage("Return to PRO Header? This will delete ALL pallet data for this PRO.")
                .setPositiveButton("Delete & Go Back", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        performBackButtonDelete();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Delete PRO pallets when going back (prevents partial data in export)
     */
    private void performBackButtonDelete() {
        String currentTrailer = sessionManager.getCurrentTrailer();
        String currentPro = sessionManager.getCurrentPro();

        try {
            // Delete all pallet records for current PRO
            int deletedRows = dbHelper.deleteByProNumber(currentTrailer, currentPro);

            // Reset pallet index to 1
            sessionManager.setCurrentPalletIndex(1);

            if (deletedRows > 0) {
                Toast.makeText(this, deletedRows + " pallets deleted.",
                        Toast.LENGTH_SHORT).show();
            }

            // Navigate back to PRO Header
            navigateToProHeader();

        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
            // Still navigate back even on error
            navigateToProHeader();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Close database
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}