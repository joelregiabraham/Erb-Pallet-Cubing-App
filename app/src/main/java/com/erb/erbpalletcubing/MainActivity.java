package com.erb.erbpalletcubing;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Phase1Test";
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize helpers
        dbHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);

        // Run tests immediately on launch
        runPhase1Tests();

        // Show quick feedback
        Toast.makeText(this, "Phase 1 components loaded successfully!",
                Toast.LENGTH_SHORT).show();
    }

    private void runPhase1Tests() {
        testDatabase();
        testSessionManager();
        testValidation();

        Toast.makeText(this, "Phase 1 tests complete! Check Logcat",
                Toast.LENGTH_LONG).show();
    }

    private void testDatabase() {
        Log.d(TAG, "=== DATABASE TESTS ===");

        // Test 1: Insert
        long rowId = dbHelper.insertPalletRecord(
                "001", "23146", "401252", "1234567890",
                "123", "4567890", "Fresh", "35", null,
                5, 1, 72, "OK", null, null, null
        );
        Log.d(TAG, "✓ Inserted row ID: " + rowId);

        // Test 2: Query
        List<DatabaseHelper.CubingRecord> records =
                dbHelper.getRecordsByTrailer("401252");
        Log.d(TAG, "✓ Retrieved " + records.size() + " records");

        // Test 3: Count
        int count = dbHelper.getRecordCountByTrailer("401252");
        Log.d(TAG, "✓ Total count: " + count);

        // Test 4: Delete
        int deleted = dbHelper.deleteByTrailerNumber("401252");
        Log.d(TAG, "✓ Deleted " + deleted + " rows");
    }

    private void testSessionManager() {
        Log.d(TAG, "=== SESSION MANAGER TESTS ===");

        // Test login
        sessionManager.loginUser("001", "23146");
        Log.d(TAG, "✓ Logged in: " + sessionManager.isLoggedIn());
        Log.d(TAG, "✓ Terminal: " + sessionManager.getTerminalId());
        Log.d(TAG, "✓ Receiver: " + sessionManager.getReceiverId());

        // Test work context
        sessionManager.setCurrentTrailer("401252");
        sessionManager.setCurrentPro("1234567890");
        Log.d(TAG, "✓ Trailer: " + sessionManager.getCurrentTrailer());
        Log.d(TAG, "✓ PRO: " + sessionManager.getCurrentPro());

        // Test resume state
        HashMap<String, String> data = new HashMap<>();
        data.put("test", "value");
        sessionManager.saveResumeState("test_screen", data);
        Log.d(TAG, "✓ Resume screen: " + sessionManager.getResumeScreen());

        // Cleanup
        sessionManager.logout();
        Log.d(TAG, "✓ Logged out: " + !sessionManager.isLoggedIn());
    }

    private void testValidation() {
        Log.d(TAG, "=== VALIDATION TESTS ===");

        // Terminal ID
        Log.d(TAG, "✓ Valid terminal: " +
                ValidationHelper.isValidTerminalId("001"));
        Log.d(TAG, "✓ Invalid terminal: " +
                !ValidationHelper.isValidTerminalId("ABC"));

        // PRO Number
        Log.d(TAG, "✓ Valid PRO: " +
                ValidationHelper.isValidProNumber("1234567890"));
        Log.d(TAG, "✓ Invalid PRO: " +
                !ValidationHelper.isValidProNumber("123"));

        // PRO Extraction
        String prefix = ValidationHelper.extractProPrefix("1234567890");
        String erb = ValidationHelper.extractProErb("1234567890");
        Log.d(TAG, "✓ PRO Prefix: " + prefix + " (should be 123)");
        Log.d(TAG, "✓ PRO Erb: " + erb + " (should be 4567890)");

        // Temperature
        Log.d(TAG, "✓ Valid temp 35: " +
                ValidationHelper.isValidTemperature("35"));
        Log.d(TAG, "✓ Valid temp -15: " +
                ValidationHelper.isValidTemperature("-15"));
        Log.d(TAG, "✓ Invalid temp 50: " +
                !ValidationHelper.isValidTemperature("50"));

        // Temperature formatting
        String display = ValidationHelper.formatTemperatureForDisplay("35");
        String export = ValidationHelper.formatTemperatureForExport("-10");
        Log.d(TAG, "✓ Display format: " + display + " (should be 35°F)");
        Log.d(TAG, "✓ Export format: " + export + " (should be -10F)");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}