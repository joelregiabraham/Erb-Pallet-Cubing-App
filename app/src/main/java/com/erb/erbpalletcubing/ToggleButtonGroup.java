package com.erb.erbpalletcubing;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * ToggleButtonGroup - Custom component for single-selection toggle buttons
 * Used for Freight Type selection (FRESH | FROZEN | DUAL)
 */
public class ToggleButtonGroup extends LinearLayout {

    private static final String TAG = "ToggleButtonGroup";

    private List<Button> buttons;
    private Button selectedButton;
    private OnSelectionChangeListener listener;
    private int selectedColor;
    private int unselectedColor;
    private int selectedTextColor;
    private int unselectedTextColor;

    public interface OnSelectionChangeListener {
        void onSelectionChanged(String selection);
    }

    public ToggleButtonGroup(Context context) {
        super(context);
        init();
    }

    public ToggleButtonGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ToggleButtonGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        buttons = new ArrayList<>();
        setOrientation(HORIZONTAL);

        // Default colors
        selectedColor = 0xFF1976D2;      // Blue
        unselectedColor = 0xFFE0E0E0;    // Light gray
        selectedTextColor = 0xFFFFFFFF;   // White
        unselectedTextColor = 0xFF333333; // Dark gray
    }

    /**
     * Add a button to the toggle group
     */
    public void addButton(Button button, final String value) {
        if (button == null) {
            return;
        }

        buttons.add(button);
        
        // Set initial style
        updateButtonStyle(button, false);

        // Set click listener
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                selectButton((Button) v, value);
            }
        });
    }

    /**
     * Select a button programmatically
     */
    public void selectButton(Button button, String value) {
        if (button == null || button == selectedButton) {
            return;
        }

        // Unselect previous button
        if (selectedButton != null) {
            updateButtonStyle(selectedButton, false);
        }

        // Select new button
        selectedButton = button;
        updateButtonStyle(selectedButton, true);

        // Notify listener
        if (listener != null) {
            listener.onSelectionChanged(value);
        }
    }

    /**
     * Select button by value
     */
    public void selectByValue(String value) {
        for (int i = 0; i < buttons.size(); i++) {
            Button button = buttons.get(i);
            String buttonText = button.getText().toString().trim();
            
            if (buttonText.equalsIgnoreCase(value)) {
                selectButton(button, value);
                break;
            }
        }
    }

    /**
     * Get currently selected value
     */
    public String getSelectedValue() {
        if (selectedButton != null) {
            return selectedButton.getText().toString().trim();
        }
        return null;
    }

    /**
     * Check if any button is selected
     */
    public boolean hasSelection() {
        return selectedButton != null;
    }

    /**
     * Clear selection
     */
    public void clearSelection() {
        if (selectedButton != null) {
            updateButtonStyle(selectedButton, false);
            selectedButton = null;
        }
    }

    /**
     * Update button visual style
     */
    private void updateButtonStyle(Button button, boolean selected) {
        if (button == null) {
            return;
        }

        if (selected) {
            button.setBackgroundColor(selectedColor);
            button.setTextColor(selectedTextColor);
            button.setAlpha(1.0f);
        } else {
            button.setBackgroundColor(unselectedColor);
            button.setTextColor(unselectedTextColor);
            button.setAlpha(0.7f);
        }
    }

    /**
     * Set selection change listener
     */
    public void setOnSelectionChangeListener(OnSelectionChangeListener listener) {
        this.listener = listener;
    }

    /**
     * Set selected button color
     */
    public void setSelectedColor(int color) {
        this.selectedColor = color;
        if (selectedButton != null) {
            updateButtonStyle(selectedButton, true);
        }
    }

    /**
     * Set unselected button color
     */
    public void setUnselectedColor(int color) {
        this.unselectedColor = color;
        for (Button button : buttons) {
            if (button != selectedButton) {
                updateButtonStyle(button, false);
            }
        }
    }

    /**
     * Disable all buttons
     */
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        for (Button button : buttons) {
            button.setEnabled(enabled);
        }
    }
}
