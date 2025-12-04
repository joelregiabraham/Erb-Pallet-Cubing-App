package com.erb.erbpalletcubing;

import android.util.Log;

/**
 * ValidationHelper - Centralized input validation utilities
 * All business logic for data validation
 */
public class ValidationHelper {

    private static final String TAG = "ValidationHelper";

    // Temperature constraints
    private static final int TEMP_MIN = -15;  // Minimum temperature in Fahrenheit
    private static final int TEMP_MAX = 35;   // Maximum temperature in Fahrenheit

    // PRO number constraints
    private static final int PRO_LENGTH = 10;  // PRO numbers must be exactly 10 digits
    private static final int PRO_PREFIX_LENGTH = 3;  // First 3 digits

    /**
     * Validate Terminal ID (numeric, non-empty)
     */
    public static boolean isValidTerminalId(String input) {
        if (input == null || input.trim().isEmpty()) {
            Log.w(TAG, "Terminal ID is empty");
            return false;
        }

        String trimmed = input.trim();
        
        // Check if numeric
        if (!trimmed.matches("\\d+")) {
            Log.w(TAG, "Terminal ID is not numeric: " + trimmed);
            return false;
        }

        return true;
    }

    /**
     * Validate Receiver ID (numeric, non-empty)
     */
    public static boolean isValidReceiverId(String input) {
        if (input == null || input.trim().isEmpty()) {
            Log.w(TAG, "Receiver ID is empty");
            return false;
        }

        String trimmed = input.trim();
        
        // Check if numeric
        if (!trimmed.matches("\\d+")) {
            Log.w(TAG, "Receiver ID is not numeric: " + trimmed);
            return false;
        }

        return true;
    }

    /**
     * Validate Trailer Number (alphanumeric, non-empty)
     */
    public static boolean isValidTrailerNumber(String input) {
        if (input == null || input.trim().isEmpty()) {
            Log.w(TAG, "Trailer number is empty");
            return false;
        }

        String trimmed = input.trim();
        
        // Allow alphanumeric characters
        if (!trimmed.matches("[a-zA-Z0-9]+")) {
            Log.w(TAG, "Trailer number contains invalid characters: " + trimmed);
            return false;
        }

        return true;
    }

    /**
     * Validate PRO Number (exactly 10 digits)
     */
    public static boolean isValidProNumber(String input) {
        if (input == null || input.trim().isEmpty()) {
            Log.w(TAG, "PRO number is empty");
            return false;
        }

        String trimmed = input.trim();
        
        // Must be exactly 10 digits
        if (!trimmed.matches("\\d{10}")) {
            Log.w(TAG, "PRO number must be exactly 10 digits: " + trimmed);
            return false;
        }

        return true;
    }

    /**
     * Validate pallet count (positive integer)
     */
    public static boolean isValidPalletCount(String input) {
        if (input == null || input.trim().isEmpty()) {
            Log.w(TAG, "Pallet count is empty");
            return false;
        }

        try {
            int count = Integer.parseInt(input.trim());
            
            if (count <= 0) {
                Log.w(TAG, "Pallet count must be positive: " + count);
                return false;
            }
            
            if (count > 999) {
                Log.w(TAG, "Pallet count too large (max 999): " + count);
                return false;
            }

            return true;
            
        } catch (NumberFormatException e) {
            Log.w(TAG, "Pallet count is not a valid integer: " + input);
            return false;
        }
    }

    /**
     * Validate temperature (range: -15 to 35 Fahrenheit)
     * Accepts negative numbers and decimals
     */
    public static boolean isValidTemperature(String input) {
        if (input == null || input.trim().isEmpty()) {
            Log.w(TAG, "Temperature is empty");
            return false;
        }

        try {
            // Parse as double to handle decimals
            double temp = Double.parseDouble(input.trim());
            
            if (temp < TEMP_MIN || temp > TEMP_MAX) {
                Log.w(TAG, "Temperature out of range (" + TEMP_MIN + " to " + 
                      TEMP_MAX + "): " + temp);
                return false;
            }

            return true;
            
        } catch (NumberFormatException e) {
            Log.w(TAG, "Temperature is not a valid number: " + input);
            return false;
        }
    }

    /**
     * Validate pallet height (positive integer, in inches)
     */
    public static boolean isValidPalletHeight(String input) {
        if (input == null || input.trim().isEmpty()) {
            Log.w(TAG, "Pallet height is empty");
            return false;
        }

        try {
            int height = Integer.parseInt(input.trim());
            
            if (height <= 0) {
                Log.w(TAG, "Pallet height must be positive: " + height);
                return false;
            }
            
            if (height > 999) {
                Log.w(TAG, "Pallet height too large (max 999): " + height);
                return false;
            }

            return true;
            
        } catch (NumberFormatException e) {
            Log.w(TAG, "Pallet height is not a valid integer: " + input);
            return false;
        }
    }

    /**
     * Validate quantity (positive integer)
     */
    public static boolean isValidQuantity(String input) {
        if (input == null || input.trim().isEmpty()) {
            Log.w(TAG, "Quantity is empty");
            return false;
        }

        try {
            int quantity = Integer.parseInt(input.trim());
            
            if (quantity <= 0) {
                Log.w(TAG, "Quantity must be positive: " + quantity);
                return false;
            }
            
            if (quantity > 9999) {
                Log.w(TAG, "Quantity too large (max 9999): " + quantity);
                return false;
            }

            return true;
            
        } catch (NumberFormatException e) {
            Log.w(TAG, "Quantity is not a valid integer: " + input);
            return false;
        }
    }

    /**
     * Format temperature for display with °F symbol
     * Example: "35" -> "35°F", "-10" -> "-10°F"
     */
    public static String formatTemperatureForDisplay(String temp) {
        if (temp == null || temp.trim().isEmpty()) {
            return "";
        }

        String trimmed = temp.trim();
        
        // Remove any existing degree symbols or F
        trimmed = trimmed.replace("°F", "").replace("°", "").replace("F", "").trim();
        
        return trimmed + "°F";
    }

    /**
     * Format temperature for CSV export with F suffix (no degree symbol)
     * Example: "35" -> "35F", "-10" -> "-10F"
     */
    public static String formatTemperatureForExport(String temp) {
        if (temp == null || temp.trim().isEmpty()) {
            return "";
        }

        String trimmed = temp.trim();
        
        // Remove any existing degree symbols or F
        trimmed = trimmed.replace("°F", "").replace("°", "").replace("F", "").trim();
        
        return trimmed + "F";
    }

    /**
     * Extract PRO prefix (first 3 digits)
     * Example: "1234567890" -> "123"
     */
    public static String extractProPrefix(String proNumber) {
        if (proNumber == null || proNumber.length() < PRO_PREFIX_LENGTH) {
            Log.e(TAG, "Cannot extract PRO prefix from invalid PRO: " + proNumber);
            return "";
        }

        String trimmed = proNumber.trim();
        
        if (trimmed.length() != PRO_LENGTH) {
            Log.e(TAG, "PRO number must be exactly " + PRO_LENGTH + " digits: " + trimmed);
            return "";
        }

        return trimmed.substring(0, PRO_PREFIX_LENGTH);
    }

    /**
     * Extract PRO Erb (remaining 7 digits after prefix)
     * Example: "1234567890" -> "4567890"
     */
    public static String extractProErb(String proNumber) {
        if (proNumber == null || proNumber.length() < PRO_LENGTH) {
            Log.e(TAG, "Cannot extract PRO Erb from invalid PRO: " + proNumber);
            return "";
        }

        String trimmed = proNumber.trim();
        
        if (trimmed.length() != PRO_LENGTH) {
            Log.e(TAG, "PRO number must be exactly " + PRO_LENGTH + " digits: " + trimmed);
            return "";
        }

        return trimmed.substring(PRO_PREFIX_LENGTH);
    }

    /**
     * Get temperature range string for display
     */
    public static String getTemperatureRangeString() {
        return TEMP_MIN + "°F to " + TEMP_MAX + "°F";
    }

    /**
     * Get minimum temperature
     */
    public static int getMinTemperature() {
        return TEMP_MIN;
    }

    /**
     * Get maximum temperature
     */
    public static int getMaxTemperature() {
        return TEMP_MAX;
    }

    /**
     * Validate and parse integer safely
     * @param input Input string
     * @param defaultValue Default value if parsing fails
     * @return Parsed integer or default value
     */
    public static int parseIntSafely(String input, int defaultValue) {
        if (input == null || input.trim().isEmpty()) {
            return defaultValue;
        }

        try {
            return Integer.parseInt(input.trim());
        } catch (NumberFormatException e) {
            Log.w(TAG, "Failed to parse integer: " + input + ", using default: " + defaultValue);
            return defaultValue;
        }
    }

    /**
     * Clean numeric input (remove non-digit characters except minus and decimal)
     */
    public static String cleanNumericInput(String input) {
        if (input == null) {
            return "";
        }

        // Allow digits, minus sign (at start only), and decimal point
        return input.replaceAll("[^0-9.-]", "");
    }

    /**
     * Validate custom OS&D reason (non-empty if "Other" is selected)
     */
    public static boolean isValidCustomReason(String input) {
        if (input == null || input.trim().isEmpty()) {
            Log.w(TAG, "Custom OS&D reason is empty");
            return false;
        }

        String trimmed = input.trim();
        
        if (trimmed.length() < 3) {
            Log.w(TAG, "Custom OS&D reason too short (min 3 characters): " + trimmed);
            return false;
        }

        return true;
    }

    /**
     * Get validation error message for temperature
     */
    public static String getTemperatureErrorMessage() {
        return "Temperature must be between " + TEMP_MIN + "°F and " + TEMP_MAX + "°F";
    }

    /**
     * Get validation error message for PRO number
     */
    public static String getProNumberErrorMessage() {
        return "PRO number must be exactly " + PRO_LENGTH + " digits";
    }

    /**
     * Get validation error message for pallet count
     */
    public static String getPalletCountErrorMessage() {
        return "Pallet count must be a positive number (1-999)";
    }

    /**
     * Get validation error message for pallet height
     */
    public static String getPalletHeightErrorMessage() {
        return "Pallet height must be a positive number (1-999 inches)";
    }

    /**
     * Get validation error message for quantity
     */
    public static String getQuantityErrorMessage() {
        return "Quantity must be a positive number (1-9999)";
    }
}
