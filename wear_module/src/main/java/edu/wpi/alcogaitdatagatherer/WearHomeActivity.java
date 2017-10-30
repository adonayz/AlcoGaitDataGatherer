package edu.wpi.alcogaitdatagatherer;

import android.content.Context;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.wearable.activity.WearableActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class WearHomeActivity extends WearableActivity implements MessageApi.MessageListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    private static final String WEAR_MESSAGE_PATH = "/message";
    private static final int RECORD_TIME_IN_SECONDS = 60;
    private TextView instructionTextView;
    private LinearLayout bacInfoLayout;
    private ImageButton startAndStopButton;
    private TextView countdownTextView;
    private EditText bacInput;
    private GoogleApiClient mGoogleApiClient;
    private CountDownTimer countDownTimer;
    private boolean isRecording = false;

    private static final SimpleDateFormat AMBIENT_DATE_FORMAT =
            new SimpleDateFormat("HH:mm", Locale.US);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wear_home);
        setAmbientEnabled();

        initViews();

        connecClientForPhone();

        setupTimer();

        configureButtons();
    }

    private void initViews(){
        instructionTextView = (TextView) findViewById(R.id.instruction);
        bacInfoLayout = (LinearLayout) findViewById(R.id.bacInfoLayout);
        bacInput = (EditText) findViewById(R.id.bacInput);
        startAndStopButton = (ImageButton) findViewById(R.id.startStopButton);
        countdownTextView = (TextView) findViewById(R.id.countdown);
    }

    private void configureButtons(){
        startAndStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(bacInput.getText().toString().trim().isEmpty()){
                    bacInput.setError("Enter BAC before recording");
                }else{
                    if(!isRecording){
                        startRecording();
                    }else{
                        stopRecording();
                    }
                }
            }
        });
    }

    private void startRecording(){
        if(!isRecording){
            isRecording = true;
            startAndStopButton.setImageResource(R.drawable.ic_action_stop);
            instructionTextView.setText(R.string.instructions_while_recording);
            bacInfoLayout.setVisibility(View.GONE);
            countdownTextView.setVisibility(View.VISIBLE);
            countDownTimer.start();
        }
    }

    private void stopRecording(){
        if(isRecording) {
            isRecording = false;
            startAndStopButton.setImageResource(R.drawable.ic_action_start);
            instructionTextView.setText(R.string.update_instruction);
            bacInfoLayout.setVisibility(View.VISIBLE);
            countdownTextView.setVisibility(View.GONE);
            countDownTimer.cancel();
        }
    }

    private void connecClientForPhone(){
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();
    }

    private void setupTimer(){
        countDownTimer = new CountDownTimer(RECORD_TIME_IN_SECONDS * 1000, 1000) {

            public void onTick(long millisUntilFinished) {
                countdownTextView.setText(String.valueOf(millisUntilFinished / 1000));

                if((millisUntilFinished / 1000) == 30){
                    ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
                    toneGen1.startTone(ToneGenerator.TONE_CDMA_ONE_MIN_BEEP,1000);
                    Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    v.vibrate(1000);
                }
            }

            public void onFinish() {
                stopRecording();
            }
        };
    }

    private void updateDisplay() {

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

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Wearable.MessageApi.addListener(mGoogleApiClient, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        /*runOnUiThread( new Runnable() {
            @Override
            public void run() {
                if( messageEvent.getPath().equalsIgnoreCase( WEAR_MESSAGE_PATH ) ) {
                    mAdapter.add( new String( messageEvent.getData() ) );
                    mAdapter.notifyDataSetChanged();
                }
            }
        });*/
    }
}
