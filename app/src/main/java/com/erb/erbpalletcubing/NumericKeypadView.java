package com.erb.erbpalletcubing;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Vibrator;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.LinearLayout;

/**
 * NumericKeypadView - Custom glove-friendly numeric keypad
 * Supports digits, negative numbers, decimals, and backspace
 */
public class NumericKeypadView extends LinearLayout {

    private static final String TAG = "NumericKeypadView";
    private static final int BUTTON_HEIGHT_DP = 70;  // Glove-friendly height
    private static final int VIBRATE_DURATION_MS = 50;

    private EditText targetEditText;
    private boolean allowNegative = false;
    private boolean allowDecimal = false;
    private Vibrator vibrator;
    private OnDoneListener onDoneListener;

    // Buttons
    private Button btn1, btn2, btn3, btn4, btn5, btn6, btn7, btn8, btn9, btn0;
    private Button btnDecimal, btnNegative, btnClear, btnBackspace, btnDone;

    public interface OnDoneListener {
        void onDone(String value);
    }

    public NumericKeypadView(Context context) {
        super(context);
        init(context, null);
    }

    public NumericKeypadView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public NumericKeypadView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        setOrientation(VERTICAL);
        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

        // Read custom attributes if provided
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.NumericKeypadView);
            allowNegative = a.getBoolean(R.styleable.NumericKeypadView_allowNegative, false);
            allowDecimal = a.getBoolean(R.styleable.NumericKeypadView_allowDecimal, false);
            a.recycle();
        }

        createKeypad();
    }

    private void createKeypad() {
        Context context = getContext();
        int buttonHeightPx = dpToPx(BUTTON_HEIGHT_DP);

        // Create grid layout for number buttons (3 columns)
        GridLayout numberGrid = new GridLayout(context);
        numberGrid.setColumnCount(3);
        numberGrid.setRowCount(4);

        // Create number buttons (1-9, 0)
        btn1 = createNumberButton("1", buttonHeightPx);
        btn2 = createNumberButton("2", buttonHeightPx);
        btn3 = createNumberButton("3", buttonHeightPx);
        btn4 = createNumberButton("4", buttonHeightPx);
        btn5 = createNumberButton("5", buttonHeightPx);
        btn6 = createNumberButton("6", buttonHeightPx);
        btn7 = createNumberButton("7", buttonHeightPx);
        btn8 = createNumberButton("8", buttonHeightPx);
        btn9 = createNumberButton("9", buttonHeightPx);
        btnDecimal = createNumberButton(".", buttonHeightPx);
        btn0 = createNumberButton("0", buttonHeightPx);
        btnNegative = createNumberButton("-", buttonHeightPx);

        // Add buttons to grid (calculator layout)
        numberGrid.addView(btn1);
        numberGrid.addView(btn2);
        numberGrid.addView(btn3);
        numberGrid.addView(btn4);
        numberGrid.addView(btn5);
        numberGrid.addView(btn6);
        numberGrid.addView(btn7);
        numberGrid.addView(btn8);
        numberGrid.addView(btn9);
        numberGrid.addView(allowDecimal ? btnDecimal : createEmptySpace());
        numberGrid.addView(btn0);
        numberGrid.addView(allowNegative ? btnNegative : createEmptySpace());

        addView(numberGrid);

        // Create action buttons row
        LinearLayout actionRow = new LinearLayout(context);
        actionRow.setOrientation(HORIZONTAL);
        actionRow.setWeightSum(3);

        btnClear = createActionButton("CLEAR", buttonHeightPx);
        btnBackspace = createActionButton("⌫", buttonHeightPx);
        btnDone = createActionButton("DONE", buttonHeightPx);

        actionRow.addView(btnClear);
        actionRow.addView(btnBackspace);
        actionRow.addView(btnDone);

        addView(actionRow);

        // Initially hide special buttons if not enabled
        if (!allowDecimal) {
            btnDecimal.setVisibility(GONE);
        }
        if (!allowNegative) {
            btnNegative.setVisibility(GONE);
        }
    }

    private Button createNumberButton(final String digit, int heightPx) {
        Button button = new Button(getContext());
        button.setText(digit);
        button.setTextSize(24);
        button.setAllCaps(false);
        
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 0;
        params.height = heightPx;
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        params.setMargins(4, 4, 4, 4);
        button.setLayoutParams(params);

        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onNumberButtonClick(digit);
            }
        });

        return button;
    }

    private Button createActionButton(String label, int heightPx) {
        Button button = new Button(getContext());
        button.setText(label);
        button.setTextSize(18);
        button.setAllCaps(true);
        
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                0, heightPx, 1f);
        params.setMargins(4, 4, 4, 4);
        button.setLayoutParams(params);

        if (label.equals("CLEAR")) {
            button.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClearClick();
                }
            });
        } else if (label.equals("⌫")) {
            button.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackspaceClick();
                }
            });
        } else if (label.equals("DONE")) {
            button.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    onDoneClick();
                }
            });
        }

        return button;
    }

    private View createEmptySpace() {
        View space = new View(getContext());
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 0;
        params.height = dpToPx(BUTTON_HEIGHT_DP);
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        space.setLayoutParams(params);
        return space;
    }

    private void onNumberButtonClick(String digit) {
        vibrate();
        
        if (targetEditText == null) {
            Log.w(TAG, "No target EditText set");
            return;
        }

        String currentText = targetEditText.getText().toString();
        
        // Handle decimal point
        if (digit.equals(".")) {
            if (!allowDecimal) {
                return;
            }
            // Only allow one decimal point
            if (currentText.contains(".")) {
                return;
            }
            // If empty, prepend with 0
            if (currentText.isEmpty()) {
                targetEditText.append("0.");
                return;
            }
        }
        
        // Handle negative sign
        if (digit.equals("-")) {
            if (!allowNegative) {
                return;
            }
            // Toggle negative sign at the beginning
            if (currentText.startsWith("-")) {
                targetEditText.setText(currentText.substring(1));
            } else {
                targetEditText.setText("-" + currentText);
            }
            targetEditText.setSelection(targetEditText.getText().length());
            return;
        }
        
        // Append digit
        targetEditText.append(digit);
    }

    private void onClearClick() {
        vibrate();
        
        if (targetEditText != null) {
            targetEditText.setText("");
        }
    }

    private void onBackspaceClick() {
        vibrate();
        
        if (targetEditText == null) {
            return;
        }

        String currentText = targetEditText.getText().toString();
        if (!currentText.isEmpty()) {
            targetEditText.setText(currentText.substring(0, currentText.length() - 1));
            targetEditText.setSelection(targetEditText.getText().length());
        }
    }

    private void onDoneClick() {
        vibrate();
        
        if (onDoneListener != null && targetEditText != null) {
            String value = targetEditText.getText().toString();
            onDoneListener.onDone(value);
        }
    }

    private void vibrate() {
        try {
            if (vibrator != null && vibrator.hasVibrator()) {
                vibrator.vibrate(VIBRATE_DURATION_MS);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error vibrating: " + e.getMessage());
        }
    }

    private int dpToPx(int dp) {
        float density = getContext().getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    // ==================== Public API ====================

    /**
     * Set the target EditText that this keypad will input to
     */
    public void setTargetEditText(EditText editText) {
        this.targetEditText = editText;
        
        if (editText != null) {
            // Disable system keyboard
            editText.setInputType(InputType.TYPE_NULL);
            editText.setFocusable(true);
            editText.setFocusableInTouchMode(true);
        }
    }

    /**
     * Set whether negative numbers are allowed
     */
    public void setAllowNegative(boolean allow) {
        this.allowNegative = allow;
        
        if (btnNegative != null) {
            btnNegative.setVisibility(allow ? VISIBLE : GONE);
        }
    }

    /**
     * Set whether decimal numbers are allowed
     */
    public void setAllowDecimal(boolean allow) {
        this.allowDecimal = allow;
        
        if (btnDecimal != null) {
            btnDecimal.setVisibility(allow ? VISIBLE : GONE);
        }
    }

    /**
     * Set the done listener
     */
    public void setOnDoneListener(OnDoneListener listener) {
        this.onDoneListener = listener;
    }

    /**
     * Get the current value from the target EditText
     */
    public String getValue() {
        if (targetEditText != null) {
            return targetEditText.getText().toString();
        }
        return "";
    }

    /**
     * Clear the target EditText
     */
    public void clear() {
        if (targetEditText != null) {
            targetEditText.setText("");
        }
    }

    /**
     * Set a value in the target EditText
     */
    public void setValue(String value) {
        if (targetEditText != null) {
            targetEditText.setText(value);
            targetEditText.setSelection(targetEditText.getText().length());
        }
    }
}
