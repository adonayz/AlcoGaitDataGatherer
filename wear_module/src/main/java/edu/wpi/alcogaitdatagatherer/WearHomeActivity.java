package edu.wpi.alcogaitdatagatherer;

import android.content.Context;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.support.wearable.activity.WearableActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class WearHomeActivity extends WearableActivity {

    private static final int RECORD_TIME_IN_SECONDS = 60;
    private TextView instructionTextView;
    private LinearLayout bacInfoLayout;
    private ImageButton button;

    WearDataListenerService dataListenerService = new WearDataListenerService();

    private static final SimpleDateFormat AMBIENT_DATE_FORMAT =
            new SimpleDateFormat("HH:mm", Locale.US);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wear_home);
        setAmbientEnabled();
        instructionTextView = (TextView) findViewById(R.id.instruction);
        bacInfoLayout = (LinearLayout) findViewById(R.id.bacInfoLayout);
        button = (ImageButton) findViewById(R.id.startStopButton);

        final TextView countdown = (TextView) findViewById(R.id.countdown);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                instructionTextView.setVisibility(View.GONE);
                bacInfoLayout.setVisibility(View.GONE);

                countdown.setVisibility(View.VISIBLE);
                final CountDownTimer count = new CountDownTimer(RECORD_TIME_IN_SECONDS * 1000, 1000) {

                    public void onTick(long millisUntilFinished) {
                        countdown.setText(String.valueOf(millisUntilFinished / 1000));

                        if((millisUntilFinished / 1000) == 30){
                            ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
                            toneGen1.startTone(ToneGenerator.TONE_CDMA_ONE_MIN_BEEP,1000);
                            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                            v.vibrate(1000);
                        }
                    }

                    public void onFinish() {
                    }
                };
            }
        });

    }

    @Override
    public void onEnterAmbient(Bundle ambientDetails) {
        super.onEnterAmbient(ambientDetails);
        updateDisplay();
    }

    @Override
    public void onUpdateAmbient() {
        super.onUpdateAmbient();
        updateDisplay();
    }

    @Override
    public void onExitAmbient() {
        updateDisplay();
        super.onExitAmbient();
    }

    private void updateDisplay() {

    }
}
