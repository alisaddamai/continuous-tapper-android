package com.saddamali.continuoustapper;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.view.accessibility.AccessibilityEvent;

public class TapAccessibilityService extends AccessibilityService {
    private static final int DOUBLE_TAP_GAP_MS = 100;
    private static final int TAP_DURATION_MS = 45;
    private WindowManager wm;
    private View panel, target;
    private TextView status, speedLabel;
    private Handler handler;
    private boolean running;
    private int intervalMs = 350;
    private int panelX = 24, panelY = 120, targetX = 450, targetY = 700;

    private final Runnable tapLoop = new Runnable() {
        @Override public void run() {
            if (!running) return;
            performDoubleTap();
            handler.postDelayed(this, intervalMs);
        }
    };

    @Override public void onServiceConnected() {
        super.onServiceConnected();
        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        handler = new Handler(Looper.getMainLooper());
        showFloatingControls();
    }

    private WindowManager.LayoutParams overlayParams(int x, int y, int w, int h) {
        WindowManager.LayoutParams p = new WindowManager.LayoutParams(
            w, h, WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);
        p.gravity = Gravity.TOP | Gravity.LEFT;
        p.x = x; p.y = y;
        return p;
    }

    private void showFloatingControls() {
        if (panel != null || wm == null) return;
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(14, 10, 14, 10);
        box.setBackgroundColor(0xEE172033);

        TextView heading = new TextView(this);
        heading.setText("TAPPER  •  drag this panel");
        heading.setTextColor(Color.WHITE);
        heading.setTypeface(null, Typeface.BOLD);
        heading.setTextSize(13);
        box.addView(heading);
        heading.setOnTouchListener(new DragListener(true));

        status = new TextView(this);
        status.setText("Status: STOPPED");
        status.setTextColor(0xFFFFD166);
        status.setPadding(0, 8, 0, 4);
        box.addView(status);

        LinearLayout buttons = new LinearLayout(this);
        Button start = new Button(this);
        start.setText("START");
        start.setOnClickListener(v -> startTapping());
        Button stop = new Button(this);
        stop.setText("STOP");
        stop.setOnClickListener(v -> stopTapping());
        buttons.addView(start, new LinearLayout.LayoutParams(0, -2, 1));
        buttons.addView(stop, new LinearLayout.LayoutParams(0, -2, 1));
        box.addView(buttons);

        speedLabel = new TextView(this);
        speedLabel.setText("Double tap cycle: 350 ms");
        speedLabel.setTextColor(Color.WHITE);
        box.addView(speedLabel);
        SeekBar speed = new SeekBar(this);
        speed.setMax(750);
        speed.setProgress(100);
        speed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar s, int progress, boolean fromUser) {
                intervalMs = 250 + progress;
                speedLabel.setText("Double tap cycle: " + intervalMs + " ms");
            }
            @Override public void onStartTrackingTouch(SeekBar s) {}
            @Override public void onStopTrackingTouch(SeekBar s) {}
        });
        box.addView(speed);

        Button close = new Button(this);
        close.setText("HIDE CONTROLS");
        close.setOnClickListener(v -> hideControls());
        box.addView(close);

        panel = box;
        wm.addView(panel, overlayParams(panelX, panelY, -2, -2));
        showTarget();
    }

    private void showTarget() {
        if (target != null || wm == null) return;
        TextView dot = new TextView(this);
        dot.setText("⊙");
        dot.setTextSize(38);
        dot.setTextColor(Color.WHITE);
        dot.setGravity(Gravity.CENTER);
        dot.setBackgroundColor(0xAAE53935);
        dot.setOnTouchListener(new DragListener(false));
        target = dot;
        wm.addView(target, overlayParams(targetX, targetY, 76, 76));
    }

    private class DragListener implements View.OnTouchListener {
        private final boolean isPanel;
        private float downRawX, downRawY;
        private int startX, startY;
        DragListener(boolean panelView) { isPanel = panelView; }
        @Override public boolean onTouch(View v, MotionEvent e) {
            WindowManager.LayoutParams lp = (WindowManager.LayoutParams) v.getLayoutParams();
            if (e.getAction() == MotionEvent.ACTION_DOWN) {
                downRawX = e.getRawX(); downRawY = e.getRawY();
                startX = lp.x; startY = lp.y;
                return true;
            }
            if (e.getAction() == MotionEvent.ACTION_MOVE) {
                lp.x = startX + (int)(e.getRawX() - downRawX);
                lp.y = startY + (int)(e.getRawY() - downRawY);
                if (isPanel) { panelX = lp.x; panelY = lp.y; }
                else { targetX = lp.x; targetY = lp.y; }
                wm.updateViewLayout(v, lp);
                return true;
            }
            return e.getAction() == MotionEvent.ACTION_UP;
        }
    }

    private void startTapping() {
        if (running) return;
        running = true;
        status.setText("Status: RUNNING, DOUBLE TAPPING");
        status.setTextColor(0xFF7CFC98);
        handler.removeCallbacks(tapLoop);
        tapLoop.run();
    }

    private void stopTapping() {
        running = false;
        if (handler != null) handler.removeCallbacks(tapLoop);
        if (status != null) {
            status.setText("Status: STOPPED");
            status.setTextColor(0xFFFFD166);
        }
    }

    private void performDoubleTap() {
        if (target == null || !running) return;
        int[] location = new int[2];
        target.getLocationOnScreen(location);
        Path firstTap = new Path();
        float x = location[0] + target.getWidth() / 2f;
        float y = location[1] + target.getHeight() / 2f;
        firstTap.moveTo(x, y);
        Path secondTap = new Path();
        secondTap.moveTo(x, y);

        GestureDescription.Builder gesture = new GestureDescription.Builder();
        gesture.addStroke(new GestureDescription.StrokeDescription(firstTap, 0, TAP_DURATION_MS));
        gesture.addStroke(new GestureDescription.StrokeDescription(secondTap, DOUBLE_TAP_GAP_MS, TAP_DURATION_MS));
        dispatchGesture(gesture.build(), null, null);
    }

    private void hideControls() {
        stopTapping();
        if (panel != null) { wm.removeView(panel); panel = null; }
        if (target != null) { wm.removeView(target); target = null; }
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {}
    @Override public void onInterrupt() { stopTapping(); }
    @Override public void onDestroy() {
        stopTapping();
        if (wm != null) {
            if (panel != null) wm.removeView(panel);
            if (target != null) wm.removeView(target);
        }
        panel = null; target = null;
        super.onDestroy();
    }
}
