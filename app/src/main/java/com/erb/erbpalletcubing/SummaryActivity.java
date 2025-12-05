package com.erb.erbpalletcubing;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.util.List;

/**
 * SummaryActivity - Display completed PROs and export functionality
 * Phase 5: Export & Email
 */
public class SummaryActivity extends AppCompatActivity {

    private static final String TAG = "SummaryActivity";
    private static final int MAX_VISIBLE_PROS = 4; // Show 4 PROs initially

    // UI Components
    private TextView tvTitle;
    private LinearLayout proListContainer;
    private Button btnViewAll;
    private Button btnAddAnotherPro;
    private Button btnExportTrailer;
    private Button btnDeleteAndStartNew;

    // Data
    private SessionManager sessionManager;
    private DatabaseHelper dbHelper;
    private String trailerNumber;
    private List<DatabaseHelper.ProSummary> allPros;
    private boolean hasExported = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);

        // Initialize
        sessionManager = new SessionManager(this);
        dbHelper = new DatabaseHelper(this);

        // Get trailer number from Intent (passed from PalletDetailActivity)
        trailerNumber = getIntent().getStringExtra("TRAILER_NUMBER");

        // Fallback: Try SessionManager if Intent is empty (for resume scenarios)
        if (trailerNumber == null || trailerNumber.trim().isEmpty()) {
            trailerNumber = sessionManager.getCurrentTrailer();
            android.util.Log.w(TAG, "Trailer not in Intent, got from SessionManager: " + trailerNumber);
        } else {
            android.util.Log.d(TAG, "Trailer from Intent: " + trailerNumber);
        }

        // Save resume state for this screen
        sessionManager.saveResumeState("summary", null);

        // Initialize views
        initializeViews();

        // Load PRO data
        loadProData();

        // Setup button listeners
        setupButtonListeners();
    }

    private void initializeViews() {
        tvTitle = findViewById(R.id.tvTitle);
        proListContainer = findViewById(R.id.proListContainer);
        btnViewAll = findViewById(R.id.btnViewAll);
        btnAddAnotherPro = findViewById(R.id.btnAddAnotherPro);
        btnExportTrailer = findViewById(R.id.btnExportTrailer);
        btnDeleteAndStartNew = findViewById(R.id.btnDeleteAndStartNew);

        // Initially hide DELETE button (shown after export)
        btnDeleteAndStartNew.setVisibility(View.GONE);
    }

    private void loadProData() {
        // Validate trailer number first
        if (trailerNumber == null || trailerNumber.trim().isEmpty()) {
            android.util.Log.e(TAG, "Trailer number is null or empty!");
            Toast.makeText(this, "Error: No trailer number found. Returning to PRO Header.",
                    Toast.LENGTH_LONG).show();

            // Navigate to PRO Header to start over
            Intent intent = new Intent(this, ProHeaderActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        android.util.Log.d(TAG, "Loading PROs for trailer: " + trailerNumber);

        // Get all PROs for this trailer
        allPros = dbHelper.getAllProsForTrailer(trailerNumber);

        android.util.Log.d(TAG, "Found " + (allPros != null ? allPros.size() : 0) + " PROs");

        if (allPros == null || allPros.isEmpty()) {
            android.util.Log.e(TAG, "No PRO data found for trailer: " + trailerNumber);
            Toast.makeText(this, "Error: No PRO data found for trailer " + trailerNumber +
                    ". Returning to PRO Header.", Toast.LENGTH_LONG).show();

            // Navigate to PRO Header to start over
            Intent intent = new Intent(this, ProHeaderActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        // Set title
        tvTitle.setText("TRAILER " + trailerNumber + " COMPLETE ✓");

        // Display PROs (initially show up to MAX_VISIBLE_PROS)
        displayPros(false);
    }

    private void displayPros(boolean showAll) {
        // Clear existing views
        proListContainer.removeAllViews();

        int prosToShow = showAll ? allPros.size() : Math.min(MAX_VISIBLE_PROS, allPros.size());

        // Add PRO items
        for (int i = 0; i < prosToShow; i++) {
            DatabaseHelper.ProSummary pro = allPros.get(i);
            TextView proView = new TextView(this);

            String proText = (i + 1) + ": PRO " + pro.proNumber + " • " + pro.palletCount +
                    " Pallet" + (pro.palletCount != 1 ? "s" : "");

            proView.setText(proText);
            proView.setTextSize(18);
            proView.setPadding(16, 16, 16, 16);

            proListContainer.addView(proView);
        }

        // Show/hide "View All" button
        if (allPros.size() > MAX_VISIBLE_PROS) {
            if (showAll) {
                btnViewAll.setText("SHOW LESS");
                btnViewAll.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        displayPros(false);
                    }
                });
            } else {
                btnViewAll.setText("VIEW ALL (" + allPros.size() + " PROs)");
                btnViewAll.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        displayPros(true);
                    }
                });
            }
            btnViewAll.setVisibility(View.VISIBLE);
        } else {
            btnViewAll.setVisibility(View.GONE);
        }
    }

    private void setupButtonListeners() {
        // Add Another PRO
        btnAddAnotherPro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addAnotherPro();
            }
        });

        // Export Trailer (or Re-Export)
        btnExportTrailer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exportTrailer();
            }
        });

        // Delete Data & Start New (only visible after export)
        btnDeleteAndStartNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteDataAndStartNew();
            }
        });
    }

    /**
     * Add another PRO for the same trailer
     */
    private void addAnotherPro() {
        // KEEP trailer context - DON'T clear it!
        // Only clear PRO-specific session data
        sessionManager.setCurrentPro("");
        sessionManager.setExpectedPallets(0);
        sessionManager.setCurrentPalletIndex(1);
        sessionManager.setFreightType("");
        sessionManager.setTemp1("");
        sessionManager.setTemp2("");

        // Save new resume state for PRO header (trailer is still in SessionManager)
        sessionManager.saveResumeState("pro_header", null);

        Toast.makeText(this, "Ready for next PRO for trailer " + trailerNumber, Toast.LENGTH_SHORT).show();

        // Navigate to PRO Header - pass trailer for extra safety
        Intent intent = new Intent(this, ProHeaderActivity.class);
        intent.putExtra("TRAILER_NUMBER", trailerNumber);
        startActivity(intent);
        finish();
    }

    /**
     * Export trailer data to CSV and send via email
     */
    private void exportTrailer() {
        // Show progress dialog
        new ExportTask().execute();
    }

    /**
     * AsyncTask to generate CSV and launch email (with progress dialog)
     */
    private class ExportTask extends AsyncTask<Void, Void, File> {
        private ProgressDialog progressDialog;
        private String errorMessage;

        @Override
        protected void onPreExecute() {
            // Show progress dialog
            progressDialog = new ProgressDialog(SummaryActivity.this);
            progressDialog.setMessage("Generating CSV...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected File doInBackground(Void... voids) {
            try {
                // Generate CSV
                File csvFile = CsvExporter.generateCsv(SummaryActivity.this, trailerNumber);
                return csvFile;
            } catch (Exception e) {
                errorMessage = e.getMessage();
                return null;
            }
        }

        @Override
        protected void onPostExecute(File csvFile) {
            // Dismiss progress dialog
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }

            if (csvFile != null && csvFile.exists()) {
                // CSV generated successfully
                Toast.makeText(SummaryActivity.this,
                        "CSV generated: " + csvFile.getName(),
                        Toast.LENGTH_SHORT).show();

                // Launch email intent
                boolean emailLaunched = EmailIntentHelper.sendCsvEmail(
                        SummaryActivity.this, csvFile, trailerNumber);

                if (emailLaunched) {
                    // Email intent launched successfully
                    // Update UI to post-export state
                    updateToPostExportState();
                } else {
                    // No email app found, but CSV is still generated
                    Toast.makeText(SummaryActivity.this,
                            "No email app found. CSV saved to Downloads/CubingReports",
                            Toast.LENGTH_LONG).show();

                    // Still mark as exported
                    updateToPostExportState();
                }
            } else {
                // CSV generation failed
                Toast.makeText(SummaryActivity.this,
                        "Failed to generate CSV. Please try again.",
                        Toast.LENGTH_LONG).show();

                if (errorMessage != null) {
                    Toast.makeText(SummaryActivity.this,
                            "Error: " + errorMessage,
                            Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    /**
     * Update UI to post-export state
     */
    private void updateToPostExportState() {
        hasExported = true;

        // Change button text
        btnExportTrailer.setText("RE-EXPORT TRAILER");

        // Show DELETE button
        btnDeleteAndStartNew.setVisibility(View.VISIBLE);
    }

    /**
     * Delete all data for current trailer and start new
     */
    private void deleteDataAndStartNew() {
        new AlertDialog.Builder(this)
                .setTitle("⚠️ Delete All Data?")
                .setMessage("Delete all data for trailer " + trailerNumber + "?\n\n" +
                        "This cannot be undone.")
                .setPositiveButton("DELETE & START NEW", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        performDelete();
                    }
                })
                .setNegativeButton("CANCEL", null)
                .show();
    }

    /**
     * Actually perform the deletion
     */
    private void performDelete() {
        try {
            // Delete all records for this trailer
            int deletedRows = dbHelper.deleteByTrailerNumber(trailerNumber);

            // Clear trailer and PRO session data (keep Terminal/Receiver - user still logged in)
            sessionManager.setCurrentTrailer("");
            sessionManager.setCurrentPro("");
            sessionManager.setExpectedPallets(0);
            sessionManager.setCurrentPalletIndex(1);
            sessionManager.setFreightType("");
            sessionManager.setTemp1("");
            sessionManager.setTemp2("");

            // Clear resume state
            sessionManager.clearResumeState();

            Toast.makeText(this,
                    "Trailer data deleted. " + deletedRows + " records removed. Ready for next trailer.",
                    Toast.LENGTH_SHORT).show();

            // Navigate to Trailer Activity
            Intent intent = new Intent(this, TrailerActivity.class);
            startActivity(intent);
            finish();

        } catch (Exception e) {
            Toast.makeText(this, "Error deleting data: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onBackPressed() {
        // Disable back button - force explicit navigation
        Toast.makeText(this, "Please use the buttons to navigate", Toast.LENGTH_SHORT).show();
    }
}