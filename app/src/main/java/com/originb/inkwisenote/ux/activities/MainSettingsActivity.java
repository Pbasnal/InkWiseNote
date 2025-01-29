package com.originb.inkwisenote.ux.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.originb.inkwisenote.data.notedata.PageSettings;
import com.originb.inkwisenote.modules.LayoutEngine;

public class MainSettingsActivity extends AppCompatActivity {

    private SeekBar seekBar;
    private TextView pageGapText;
    private int startValue = 10;
    private int endValue = 100;
    private int interval = 10;

    private PageSettings pageSettings;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(createUi());//(R.layout.activity_main_settings);

//        seekBar = findViewById(R.id.page_line_gap_slider);
//        pageGapText = findViewById(R.id.page_line_gap_value_text);
//
//        pageSettings = Repositories.getInstance().getPageSettings();
//
//        // Set the range
//        int numberOfSteps = (endValue - startValue) / interval;
//        seekBar.setMax(numberOfSteps);
//
//        // Set initial value
//        int initialProgress = calculateProgressFromValue(50); // Example initial value
//        seekBar.setProgress(initialProgress);
//        pageGapText.setText(String.valueOf(50));
//
//        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//            @Override
//            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                int value = getValueFromProgress(progress);
//                pageGapText.setText(String.valueOf(value));
//                pageSettings.pageLineGap = value;
//            }
//
//            @Override
//            public void onStartTrackingTouch(SeekBar seekBar) {
//            }
//
//            @Override
//            public void onStopTrackingTouch(SeekBar seekBar) {
//            }
//        });
    }

    private ViewGroup createUi() {

        return LayoutEngine.linearLayout(this, LinearLayout.VERTICAL)
                .addView(LayoutEngine::createSeekbar)
                .addView(LayoutEngine::createTextView)
                .build();
    }

    private int getValueFromProgress(int progress) {
        return startValue + (progress * interval);
    }

    private int calculateProgressFromValue(int value) {
        return (value - startValue) / interval;
    }

    public static void getIntent(Context context) {
        Intent intent = new Intent(context, MainSettingsActivity.class);
        context.startActivity(intent);
    }
}
