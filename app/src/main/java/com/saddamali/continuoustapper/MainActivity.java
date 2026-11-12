package com.saddamali.continuoustapper;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(28, 36, 28, 28);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        TextView title = new TextView(this);
        title.setText("Continuous Tapper");
        title.setTextSize(26);
        root.addView(title);
        TextView info = new TextView(this);
        info.setText("Enable the accessibility service to show the floating tap selector and controls over other apps.");
        info.setTextSize(16);
        info.setPadding(0, 24, 0, 24);
        root.addView(info);
        Button settings = new Button(this);
        settings.setText("OPEN ACCESSIBILITY SETTINGS");
        settings.setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)));
        root.addView(settings);
        TextView note = new TextView(this);
        note.setText("After enabling Continuous Tapper, return to your app. Drag the red target onto the screen location you want to tap. Use START and STOP in the floating panel.");
        note.setPadding(0, 24, 0, 0);
        root.addView(note);
        setContentView(root);
    }
}
