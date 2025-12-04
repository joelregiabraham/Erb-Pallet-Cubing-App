package com.erb.erbpalletcubing;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * SessionManager - Manages user session and resume state
 * Handles login, logout, and crash recovery via SharedPreferences
 */
public class SessionManager {

    private static final String TAG = "SessionManager";
    private static final String PREF_NAME = "ErbCubingSession";
    private static final int PRIVATE_MODE = 0;

    // Session Keys
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_TERMINAL_ID = "terminalId";
    private static final String KEY_RECEIVER_ID = "receiverId";

    // Resume State Keys
    private static final String KEY_RESUME_SCREEN = "resumeScreen";
    private static final String KEY_RESUME_DATA_PREFIX = "resumeData_";

    // Current Work Context Keys
    private static final String KEY_CURRENT_TRAILER = "currentTrailer";
    private static final String KEY_CURRENT_PRO = "currentPro";
    private static final String KEY_CURRENT_PALLET_INDEX = "currentPalletIndex";
    private static final String KEY_EXPECTED_PALLETS = "expectedPallets";
    private static final String KEY_FREIGHT_TYPE = "freightType";
    private static final String KEY_TEMP1 = "temp1";
    private static final String KEY_TEMP2 = "temp2";

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("Context cannot be null");
        }
        prefs = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = prefs.edit();
    }

    // ==================== Login/Logout Methods ====================

    /**
     * Log in user with Terminal ID and Receiver ID
     */
    public void loginUser(String terminalId, String receiverId) {
        try {
            editor.putBoolean(KEY_IS_LOGGED_IN, true);
            editor.putString(KEY_TERMINAL_ID, terminalId);
            editor.putString(KEY_RECEIVER_ID, receiverId);
            editor.apply();
            Log.d(TAG, "User logged in: Terminal=" + terminalId + ", Receiver=" + receiverId);
        } catch (Exception e) {
            Log.e(TAG, "Error logging in user: " + e.getMessage(), e);
        }
    }

    /**
     * Check if user is logged in
     */
    public boolean isLoggedIn() {
        try {
            return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
        } catch (Exception e) {
            Log.e(TAG, "Error checking login status: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * Get Terminal ID
     */
    public String getTerminalId() {
        try {
            return prefs.getString(KEY_TERMINAL_ID, null);
        } catch (Exception e) {
            Log.e(TAG, "Error getting terminal ID: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Get Receiver ID
     */
    public String getReceiverId() {
        try {
            return prefs.getString(KEY_RECEIVER_ID, null);
        } catch (Exception e) {
            Log.e(TAG, "Error getting receiver ID: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Log out user and clear all session data
     */
    public void logout() {
        try {
            editor.clear();
            editor.apply();
            Log.d(TAG, "User logged out, all session data cleared");
        } catch (Exception e) {
            Log.e(TAG, "Error logging out: " + e.getMessage(), e);
        }
    }

    // ==================== Resume State Methods ====================

    /**
     * Save resume state with screen name and data
     * @param screen Screen identifier (e.g., "trailer", "pro_header", "pallet_detail")
     * @param data Key-value pairs of data to save
     */
    public void saveResumeState(String screen, HashMap<String, String> data) {
        try {
            editor.putString(KEY_RESUME_SCREEN, screen);

            if (data != null) {
                for (Map.Entry<String, String> entry : data.entrySet()) {
                    editor.putString(KEY_RESUME_DATA_PREFIX + entry.getKey(), entry.getValue());
                }
            }

            editor.apply();
            Log.d(TAG, "Resume state saved: screen=" + screen);
        } catch (Exception e) {
            Log.e(TAG, "Error saving resume state: " + e.getMessage(), e);
        }
    }

    /**
     * Get resume screen identifier
     */
    public String getResumeScreen() {
        try {
            return prefs.getString(KEY_RESUME_SCREEN, null);
        } catch (Exception e) {
            Log.e(TAG, "Error getting resume screen: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Get all resume state data
     */
    public HashMap<String, String> getResumeState() {
        HashMap<String, String> data = new HashMap<>();
        
        try {
            Map<String, ?> allPrefs = prefs.getAll();
            
            for (Map.Entry<String, ?> entry : allPrefs.entrySet()) {
                if (entry.getKey().startsWith(KEY_RESUME_DATA_PREFIX)) {
                    String key = entry.getKey().substring(KEY_RESUME_DATA_PREFIX.length());
                    String value = entry.getValue() != null ? entry.getValue().toString() : null;
                    data.put(key, value);
                }
            }
            
            Log.d(TAG, "Retrieved resume state data: " + data.size() + " entries");
        } catch (Exception e) {
            Log.e(TAG, "Error getting resume state: " + e.getMessage(), e);
        }
        
        return data;
    }

    /**
     * Clear resume state (but keep login session)
     */
    public void clearResumeState() {
        try {
            // Remove resume screen
            editor.remove(KEY_RESUME_SCREEN);

            // Remove all resume data keys
            Map<String, ?> allPrefs = prefs.getAll();
            for (String key : allPrefs.keySet()) {
                if (key.startsWith(KEY_RESUME_DATA_PREFIX)) {
                    editor.remove(key);
                }
            }

            // Clear current work context
            editor.remove(KEY_CURRENT_TRAILER);
            editor.remove(KEY_CURRENT_PRO);
            editor.remove(KEY_CURRENT_PALLET_INDEX);
            editor.remove(KEY_EXPECTED_PALLETS);
            editor.remove(KEY_FREIGHT_TYPE);
            editor.remove(KEY_TEMP1);
            editor.remove(KEY_TEMP2);

            editor.apply();
            Log.d(TAG, "Resume state cleared");
        } catch (Exception e) {
            Log.e(TAG, "Error clearing resume state: " + e.getMessage(), e);
        }
    }

    // ==================== Current Work Context Methods ====================

    /**
     * Set current trailer number
     */
    public void setCurrentTrailer(String trailerNumber) {
        try {
            editor.putString(KEY_CURRENT_TRAILER, trailerNumber);
            editor.apply();
            Log.d(TAG, "Current trailer set: " + trailerNumber);
        } catch (Exception e) {
            Log.e(TAG, "Error setting current trailer: " + e.getMessage(), e);
        }
    }

    /**
     * Get current trailer number
     */
    public String getCurrentTrailer() {
        try {
            return prefs.getString(KEY_CURRENT_TRAILER, null);
        } catch (Exception e) {
            Log.e(TAG, "Error getting current trailer: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Set current PRO number
     */
    public void setCurrentPro(String proNumber) {
        try {
            editor.putString(KEY_CURRENT_PRO, proNumber);
            editor.apply();
            Log.d(TAG, "Current PRO set: " + proNumber);
        } catch (Exception e) {
            Log.e(TAG, "Error setting current PRO: " + e.getMessage(), e);
        }
    }

    /**
     * Get current PRO number
     */
    public String getCurrentPro() {
        try {
            return prefs.getString(KEY_CURRENT_PRO, null);
        } catch (Exception e) {
            Log.e(TAG, "Error getting current PRO: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Set current pallet index (1-based)
     */
    public void setCurrentPalletIndex(int index) {
        try {
            editor.putInt(KEY_CURRENT_PALLET_INDEX, index);
            editor.apply();
            Log.d(TAG, "Current pallet index set: " + index);
        } catch (Exception e) {
            Log.e(TAG, "Error setting current pallet index: " + e.getMessage(), e);
        }
    }

    /**
     * Get current pallet index (1-based, returns 0 if not set)
     */
    public int getCurrentPalletIndex() {
        try {
            return prefs.getInt(KEY_CURRENT_PALLET_INDEX, 0);
        } catch (Exception e) {
            Log.e(TAG, "Error getting current pallet index: " + e.getMessage(), e);
            return 0;
        }
    }

    /**
     * Set expected pallets count
     */
    public void setExpectedPallets(int count) {
        try {
            editor.putInt(KEY_EXPECTED_PALLETS, count);
            editor.apply();
            Log.d(TAG, "Expected pallets set: " + count);
        } catch (Exception e) {
            Log.e(TAG, "Error setting expected pallets: " + e.getMessage(), e);
        }
    }

    /**
     * Get expected pallets count
     */
    public int getExpectedPallets() {
        try {
            return prefs.getInt(KEY_EXPECTED_PALLETS, 0);
        } catch (Exception e) {
            Log.e(TAG, "Error getting expected pallets: " + e.getMessage(), e);
            return 0;
        }
    }

    /**
     * Set freight type
     */
    public void setFreightType(String freightType) {
        try {
            editor.putString(KEY_FREIGHT_TYPE, freightType);
            editor.apply();
            Log.d(TAG, "Freight type set: " + freightType);
        } catch (Exception e) {
            Log.e(TAG, "Error setting freight type: " + e.getMessage(), e);
        }
    }

    /**
     * Get freight type
     */
    public String getFreightType() {
        try {
            return prefs.getString(KEY_FREIGHT_TYPE, null);
        } catch (Exception e) {
            Log.e(TAG, "Error getting freight type: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Set temperature 1
     */
    public void setTemp1(String temp) {
        try {
            editor.putString(KEY_TEMP1, temp);
            editor.apply();
            Log.d(TAG, "Temp1 set: " + temp);
        } catch (Exception e) {
            Log.e(TAG, "Error setting temp1: " + e.getMessage(), e);
        }
    }

    /**
     * Get temperature 1
     */
    public String getTemp1() {
        try {
            return prefs.getString(KEY_TEMP1, null);
        } catch (Exception e) {
            Log.e(TAG, "Error getting temp1: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Set temperature 2 (nullable for DUAL freight type)
     */
    public void setTemp2(String temp) {
        try {
            if (temp != null && !temp.trim().isEmpty()) {
                editor.putString(KEY_TEMP2, temp);
            } else {
                editor.remove(KEY_TEMP2);
            }
            editor.apply();
            Log.d(TAG, "Temp2 set: " + temp);
        } catch (Exception e) {
            Log.e(TAG, "Error setting temp2: " + e.getMessage(), e);
        }
    }

    /**
     * Get temperature 2
     */
    public String getTemp2() {
        try {
            return prefs.getString(KEY_TEMP2, null);
        } catch (Exception e) {
            Log.e(TAG, "Error getting temp2: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Increment pallet index and return new value
     */
    public int incrementPalletIndex() {
        int currentIndex = getCurrentPalletIndex();
        int newIndex = currentIndex + 1;
        setCurrentPalletIndex(newIndex);
        return newIndex;
    }

    /**
     * Check if there are more pallets to process
     */
    public boolean hasMorePallets() {
        int currentIndex = getCurrentPalletIndex();
        int expectedPallets = getExpectedPallets();
        return currentIndex < expectedPallets;
    }

    /**
     * Get user info string for display
     */
    public String getUserInfoString() {
        String terminal = getTerminalId();
        String receiver = getReceiverId();
        
        if (terminal != null && receiver != null) {
            return "Terminal: " + terminal + " | Receiver: " + receiver;
        }
        
        return "Not logged in";
    }
}
