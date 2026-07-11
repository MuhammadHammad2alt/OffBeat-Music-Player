package com.example.offbeatmusicplayer;

import android.media.audiofx.Equalizer;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Color;
import android.view.Gravity;

import androidx.appcompat.app.AppCompatActivity;

public class EqualizerActivity extends AppCompatActivity {

    Equalizer equalizer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int audioSessionId = getIntent().getIntExtra("audioSessionId", 0);

        if (audioSessionId == 0) {
            Toast.makeText(this, "Audio session not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Root layout
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.parseColor("#2D1B69"));
        root.setPadding(40, 60, 40, 40);

        // Title
        TextView title = new TextView(this);
        title.setText("Equalizer");
        title.setTextColor(Color.WHITE);
        title.setTextSize(24);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setPadding(0, 0, 0, 40);
        root.addView(title);

        try {
            equalizer = new Equalizer(0, audioSessionId);
            equalizer.setEnabled(true);

            short bands = equalizer.getNumberOfBands();
            short[] range = equalizer.getBandLevelRange();
            short minLevel = range[0];
            short maxLevel = range[1];

            String[] labels = {"60 Hz", "230 Hz", "910 Hz", "3.6 KHz", "14 KHz"};

            for (short i = 0; i < bands; i++) {

                final short bandIndex = i;

                LinearLayout row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setGravity(Gravity.CENTER_VERTICAL);
                row.setPadding(0, 16, 0, 16);

                // Label
                TextView label = new TextView(this);
                label.setText(i < labels.length ? labels[i] : "Band " + i);
                label.setTextColor(Color.parseColor("#CCB8FF"));
                label.setTextSize(13);
                label.setMinWidth(160);

                // SeekBar
                SeekBar seekBar = new SeekBar(this);
                LinearLayout.LayoutParams params =
                        new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
                seekBar.setLayoutParams(params);

                seekBar.setMax(maxLevel - minLevel);
                seekBar.setProgress(equalizer.getBandLevel(i) - minLevel);

                // Value text
                TextView dbText = new TextView(this);
                dbText.setTextColor(Color.WHITE);
                dbText.setTextSize(12);
                dbText.setMinWidth(100);
                dbText.setGravity(Gravity.END);
                dbText.setText((equalizer.getBandLevel(i) / 100) + " dB");

                seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        if (fromUser) {
                            short level = (short) (progress + minLevel);
                            equalizer.setBandLevel(bandIndex, level);
                            dbText.setText((level / 100) + " dB");
                        }
                    }

                    @Override public void onStartTrackingTouch(SeekBar seekBar) {}
                    @Override public void onStopTrackingTouch(SeekBar seekBar) {}
                });

                row.addView(label);
                row.addView(seekBar);
                row.addView(dbText);
                root.addView(row);

                // Divider
                View divider = new View(this);
                LinearLayout.LayoutParams dParams =
                        new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT, 1);
                divider.setLayoutParams(dParams);
                divider.setBackgroundColor(Color.parseColor("#44FFFFFF"));
                root.addView(divider);
            }

            // Presets title
            TextView presetTitle = new TextView(this);
            presetTitle.setText("Presets");
            presetTitle.setTextColor(Color.parseColor("#CCB8FF"));
            presetTitle.setTextSize(16);
            presetTitle.setPadding(0, 40, 0, 16);
            root.addView(presetTitle);

            LinearLayout presetRow = new LinearLayout(this);
            presetRow.setOrientation(LinearLayout.HORIZONTAL);

            short presetCount = equalizer.getNumberOfPresets();

            for (short i = 0; i < presetCount; i++) {

                final short presetIndex = i;

                TextView btn = new TextView(this);
                btn.setText(equalizer.getPresetName(i));
                btn.setTextColor(Color.WHITE);
                btn.setPadding(20, 16, 20, 16);
                btn.setBackground(getBackground());

                LinearLayout.LayoutParams p =
                        new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT);
                p.setMargins(0, 0, 16, 0);
                btn.setLayoutParams(p);

                btn.setOnClickListener(v -> {
                    equalizer.usePreset(presetIndex);
                    Toast.makeText(this,
                            "Preset: " + equalizer.getPresetName(presetIndex),
                            Toast.LENGTH_SHORT).show();
                    recreate();
                });

                presetRow.addView(btn);
            }

            android.widget.HorizontalScrollView scroll =
                    new android.widget.HorizontalScrollView(this);
            scroll.addView(presetRow);
            root.addView(scroll);

        } catch (Exception e) {
            TextView error = new TextView(this);
            error.setText("Equalizer not supported on this device");
            error.setTextColor(Color.WHITE);
            root.addView(error);
        }

        android.widget.ScrollView scrollView = new android.widget.ScrollView(this);
        scrollView.addView(root);
        setContentView(scrollView);
    }

    private android.graphics.drawable.GradientDrawable getBackground() {
        android.graphics.drawable.GradientDrawable bg =
                new android.graphics.drawable.GradientDrawable();
        bg.setCornerRadius(30);
        bg.setColor(Color.parseColor("#7C4DCC"));
        return bg;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (equalizer != null) {
            equalizer.release();
        }
    }
}