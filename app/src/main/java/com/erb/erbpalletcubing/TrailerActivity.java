package com.erb.erbpalletcubing;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;

/**
 * TrailerActivity - Home screen for entering trailer number
 * Shows user info, handles sign out, cancel trailer, and navigation
 */
public class TrailerActivity extends AppCompatActivity {

    private static final String TAG = "TrailerActivity";

    private TextView tvUserInfo;
    private TextView tvResumeIndicator;
    private EditText etTrailerNumber;
    private Button btnNext;
    private Button btnSignOut;
    private Button btnCancelTrailer;

    private SessionManager sessionManager;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trailer);

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

        // Display user info
        displayUserInfo();

        // Check for resume state
        checkResumeState();

        // Setup validation
        setupValidation();

        // Setup buttons
        setupButtons();
    }

    private void initializeViews() {
        tvUserInfo = findViewById(R.id.tvUserInfo);
        tvResumeIndicator = findViewById(R.id.tvResumeIndicator);
        etTrailerNumber = findViewById(R.id.etTrailerNumber);
        btnNext = findViewById(R.id.btnNext);
        btnSignOut = findViewById(R.id.btnSignOut);
        btnCancelTrailer = findViewById(R.id.btnCancelTrailer);
    }

    private void displayUserInfo() {
        String terminal = sessionManager.getTerminalId();
        String receiver = sessionManager.getReceiverId();
        
        String userInfo = "User: T-" + terminal + " | R-" + receiver;
        tvUserInfo.setText(userInfo);
    }

    private void checkResumeState() {
        String currentTrailer = sessionManager.getCurrentTrailer();
        
        if (currentTrailer != null && !currentTrailer.isEmpty()) {
            // User has a trailer in progress
            tvResumeIndicator.setVisibility(View.VISIBLE);
            tvResumeIndicator.setText("⚠ Trailer in progress: " + currentTrailer);
            btnCancelTrailer.setVisibility(View.VISIBLE);
            
            // Pre-fill trailer number
            etTrailerNumber.setText(currentTrailer);
        } else {
            // No trailer in progress
            tvResumeIndicator.setVisibility(View.GONE);
            btnCancelTrailer.setVisibility(View.GONE);
        }
    }

    private void setupValidation() {
        // Enable/disable Next button based on trailer number validity
        etTrailerNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                updateNextButtonState();
            }
        });

        // Initial button state
        updateNextButtonState();
    }

    private void updateNextButtonState() {
        String trailerNumber = etTrailerNumber.getText().toString().trim();
        boolean isValid = ValidationHelper.isValidTrailerNumber(trailerNumber);

        btnNext.setEnabled(isValid);
        btnNext.setAlpha(isValid ? 1.0f : 0.5f);
    }

    private void setupButtons() {
        // Next Button
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleNextButton();
            }
        });

        // Sign Out Button
        btnSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSignOutDialog();
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

    private void handleNextButton() {
        String trailerNumber = etTrailerNumber.getText().toString().trim();

        // Validate trailer number
        if (!ValidationHelper.isValidTrailerNumber(trailerNumber)) {
            Toast.makeText(this, "Invalid trailer number (alphanumeric only)",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Save trailer to session
        sessionManager.setCurrentTrailer(trailerNumber);

        // Save resume state
        HashMap<String, String> resumeData = new HashMap<>();
        resumeData.put("trailer", trailerNumber);
        sessionManager.saveResumeState("trailer", resumeData);

        Toast.makeText(this, "Trailer saved: " + trailerNumber, Toast.LENGTH_SHORT).show();

        // Navigate to PRO Header Activity
        navigateToProHeader();
    }

    private void showSignOutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Sign Out")
                .setMessage("Are you sure you want to sign out? This will delete ALL unsaved data.")
                .setPositiveButton("Sign Out", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        performSignOut();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void performSignOut() {
        try {
            // Delete all database records
            int deletedRows = dbHelper.deleteAllRecords();

            // Clear session
            sessionManager.logout();

            Toast.makeText(this, "Signed out. " + deletedRows + " records deleted.",
                    Toast.LENGTH_SHORT).show();

            // Navigate to login
            navigateToLogin();

        } catch (Exception e) {
            Toast.makeText(this, "Error signing out: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
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

            // Refresh screen
            etTrailerNumber.setText("");
            checkResumeState();

        } catch (Exception e) {
            Toast.makeText(this, "Error cancelling trailer: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void navigateToProHeader() {
        // TODO: Phase 3 - Replace with ProHeaderActivity
        // Intent intent = new Intent(this, ProHeaderActivity.class);
        // startActivity(intent);
        
        // For now, show toast
        Toast.makeText(this, "ProHeaderActivity not yet implemented (Phase 3)",
                Toast.LENGTH_LONG).show();
        
        // TODO: Remove this line in Phase 3
        // finish();
    }

    @Override
    public void onBackPressed() {
        // Show confirmation before exiting to login
        new AlertDialog.Builder(this)
                .setTitle("Exit to Login?")
                .setMessage("Return to login screen? Your session will remain active.")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        navigateToLogin();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh resume state in case user returned from another activity
        checkResumeState();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}
