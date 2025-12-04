package com.erb.erbpalletcubing;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/**
 * LoginActivity - User authentication with Terminal ID and Receiver ID
 * Implements resume logic to skip login if already logged in
 */
public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private EditText etTerminalId;
    private EditText etReceiverId;
    private Button btnStartShift;
    private NumericKeypadView numericKeypad;
    private TextView tvKeypadLabel;

    private SessionManager sessionManager;
    private EditText currentActiveField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize SessionManager
        sessionManager = new SessionManager(this);

        // Check for resume state - skip login if already logged in
        if (checkResumeState()) {
            return;  // Activity finished, navigated to resume screen
        }

        // Initialize views
        initializeViews();

        // Setup numeric keypads
        setupNumericKeypads();

        // Setup validation
        setupValidation();

        // Setup start shift button
        setupStartShiftButton();
    }

    /**
     * Check if user is already logged in and should resume
     * @return true if resumed to another screen, false if should show login
     */
    private boolean checkResumeState() {
        if (sessionManager.isLoggedIn()) {
            String resumeScreen = sessionManager.getResumeScreen();

            if (resumeScreen != null) {
                // User has a resume state - navigate to appropriate screen
                Toast.makeText(this, "Resuming session...", Toast.LENGTH_SHORT).show();
                navigateToResumeScreen(resumeScreen);
                return true;
            } else {
                // User is logged in but no resume state - go to trailer screen
                navigateToTrailerActivity();
                return true;
            }
        }

        return false;
    }

    /**
     * Navigate to the appropriate screen based on resume state
     */
    private void navigateToResumeScreen(String screen) {
        Intent intent;

        switch (screen) {
            case "trailer":
                intent = new Intent(this, TrailerActivity.class);
                break;

            case "pro_header":
                // TODO: Phase 3 - uncomment when ProHeaderActivity is created
                // intent = new Intent(this, ProHeaderActivity.class);
                // break;
                // For now, go to trailer
                intent = new Intent(this, TrailerActivity.class);
                break;

            case "pallet_detail":
                // TODO: Phase 4 - uncomment when PalletDetailActivity is created
                // intent = new Intent(this, PalletDetailActivity.class);
                // break;
                // For now, go to trailer
                intent = new Intent(this, TrailerActivity.class);
                break;

            default:
                // Unknown screen, go to trailer
                intent = new Intent(this, TrailerActivity.class);
                break;
        }

        startActivity(intent);
        finish();
    }

    private void initializeViews() {
        etTerminalId = findViewById(R.id.etTerminalId);
        etReceiverId = findViewById(R.id.etReceiverId);
        btnStartShift = findViewById(R.id.btnStartShift);
        numericKeypad = findViewById(R.id.numericKeypad);
        tvKeypadLabel = findViewById(R.id.tvKeypadLabel);
    }

    private void setupNumericKeypads() {
        // Disable system keyboard for both fields
        disableSystemKeyboard(etTerminalId);
        disableSystemKeyboard(etReceiverId);

        // Configure keypad
        numericKeypad.setAllowNegative(false);
        numericKeypad.setAllowDecimal(false);

        // Start with Terminal ID field
        switchKeypadToField(etTerminalId);

        // Set up field click listeners to switch keypad target
        etTerminalId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchKeypadToField(etTerminalId);
            }
        });

        etReceiverId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchKeypadToField(etReceiverId);
            }
        });

        // Done listener - switch to next field or attempt login
        numericKeypad.setOnDoneListener(new NumericKeypadView.OnDoneListener() {
            @Override
            public void onDone(String value) {
                if (currentActiveField == etTerminalId) {
                    // Validate Terminal ID
                    if (ValidationHelper.isValidTerminalId(value)) {
                        // Switch to Receiver ID field
                        switchKeypadToField(etReceiverId);
                    } else {
                        Toast.makeText(LoginActivity.this,
                                "Please enter a valid Terminal ID (numeric)",
                                Toast.LENGTH_SHORT).show();
                    }
                } else if (currentActiveField == etReceiverId) {
                    // Validate Receiver ID and attempt login
                    if (validateAndLogin()) {
                        navigateToTrailerActivity();
                    }
                }
            }
        });
    }

    /**
     * Disable system keyboard for an EditText
     */
    private void disableSystemKeyboard(EditText editText) {
        editText.setShowSoftInputOnFocus(false);
        editText.setTextIsSelectable(true);
    }

    /**
     * Switch keypad to target a specific field
     */
    private void switchKeypadToField(EditText field) {
        currentActiveField = field;
        numericKeypad.setTargetEditText(field);

        // Make sure system keyboard stays hidden
        disableSystemKeyboard(field);
        field.requestFocus();

        // Update label to show which field is active
        if (field == etTerminalId) {
            tvKeypadLabel.setText("▲ Enter Terminal ID");
        } else if (field == etReceiverId) {
            tvKeypadLabel.setText("▲ Enter Receiver ID");
        }
    }

    private void setupValidation() {
        // Add text watchers to enable/disable start button
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

        etTerminalId.addTextChangedListener(validationWatcher);
        etReceiverId.addTextChangedListener(validationWatcher);

        // Initial button state
        updateStartButtonState();
    }

    private void updateStartButtonState() {
        String terminalId = etTerminalId.getText().toString().trim();
        String receiverId = etReceiverId.getText().toString().trim();

        boolean isValid = ValidationHelper.isValidTerminalId(terminalId) &&
                ValidationHelper.isValidReceiverId(receiverId);

        btnStartShift.setEnabled(isValid);
        btnStartShift.setAlpha(isValid ? 1.0f : 0.5f);
    }

    private void setupStartShiftButton() {
        btnStartShift.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateAndLogin()) {
                    navigateToTrailerActivity();
                }
            }
        });
    }

    /**
     * Validate inputs and perform login
     * @return true if login successful, false otherwise
     */
    private boolean validateAndLogin() {
        String terminalId = etTerminalId.getText().toString().trim();
        String receiverId = etReceiverId.getText().toString().trim();

        // Validate Terminal ID
        if (!ValidationHelper.isValidTerminalId(terminalId)) {
            Toast.makeText(this, "Invalid Terminal ID (must be numeric)",
                    Toast.LENGTH_SHORT).show();
            etTerminalId.requestFocus();
            return false;
        }

        // Validate Receiver ID
        if (!ValidationHelper.isValidReceiverId(receiverId)) {
            Toast.makeText(this, "Invalid Receiver ID (must be numeric)",
                    Toast.LENGTH_SHORT).show();
            etReceiverId.requestFocus();
            return false;
        }

        // Perform login
        try {
            sessionManager.loginUser(terminalId, receiverId);
            Toast.makeText(this, "Logged in successfully!", Toast.LENGTH_SHORT).show();
            return true;
        } catch (Exception e) {
            Toast.makeText(this, "Login failed: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
            return false;
        }
    }

    private void navigateToTrailerActivity() {
        Intent intent = new Intent(this, TrailerActivity.class);
        startActivity(intent);
        finish();  // Don't allow back to login screen
    }

    @Override
    public void onBackPressed() {
        // Exit app when back button pressed on login screen
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up resources if needed
    }
}