package edu.wpi.alcogaitdatagatherer;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import edu.wpi.alcogaitdatagatherercommon.CommonValues;

public class WearHomeActivity extends WearableActivity implements MessageApi.MessageListener, NodeApi.NodeListener,GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, SensorEventListener {

    private TextView instructionTextView;
    private LinearLayout bacInfoLayout;
    private ImageButton startAndStopButton;
    private TextView countdownTextView;
    private EditText bacInput;
    private GoogleApiClient mGoogleApiClient;
    private CountDownTimer countDownTimer;
    private boolean isRecording = false;
    private boolean isPhoneConnected = false;

    private SensorManager mSensorManager;
    private Sensor mHeartRateSensor;

    private static final int BODY_SENSOR_PERMISSION_CODE = 1010;
    private static final int TIMEOUT = 15000;
    private String prevBACInput = "";

    private static final SimpleDateFormat AMBIENT_DATE_FORMAT =
            new SimpleDateFormat("HH:mm", Locale.US);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wear_home);
        setAmbientEnabled();

        requestPermissions();

        connectClientForPhone();

        initViews();

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
                        startRecording(bacInput.getText().toString().trim(), false);
                    }else{
                        stopRecording();
                    }
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerSensorListeners();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterSensorListeners();
    }

    @Override
    protected void onStop() {

        if (null != mGoogleApiClient && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
        unregisterSensorListeners();
        notifyMobile(CommonValues.WEAR_MESSAGE_PATH, CommonValues.WEARABLE_DISCONNECTED);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterSensorListeners();
        notifyMobile(CommonValues.WEAR_MESSAGE_PATH, CommonValues.WEARABLE_DISCONNECTED);
    }

    private void setupSensors(){
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        
        if(mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE) != null){
            mHeartRateSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
        }else{
            showToast("NO HEART RATE SENSOR");
        }
    }

    private void registerSensorListeners(){
        mSensorManager.registerListener(this, mHeartRateSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void unregisterSensorListeners(){
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if(isRecording &&  sensorEvent.sensor.getType() == Sensor.TYPE_HEART_RATE){
            sendSensorData(sensorEvent.sensor.getName(), sensorEvent.accuracy, sensorEvent.timestamp, sensorEvent.values);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public void sendSensorData(final String sensorType, final int accuracy, final long timestamp, final float[] values){
        new Thread( new Runnable() {
            @Override
            public void run() {

                PutDataMapRequest dataMap = PutDataMapRequest.create(CommonValues.SENSOR_PATH + sensorType);

                dataMap.getDataMap().putInt(CommonValues.ACCURACY, accuracy);
                dataMap.getDataMap().putLong(CommonValues.TIMESTAMP, timestamp);
                dataMap.getDataMap().putFloatArray(CommonValues.VALUES, values);

                PutDataRequest putDataRequest = dataMap.asPutDataRequest();

                // CHECK IF GOOGLE CLIENT IS CONNECTED
                if(isClientConnected()){
                    Wearable.DataApi.putDataItem(mGoogleApiClient, putDataRequest).setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                        @Override
                        public void onResult(DataApi.DataItemResult dataItemResult) {
                            Log.v("WearHomeActivty", "Sending sensor data: " + dataItemResult.getStatus().isSuccess());
                            if(dataItemResult.getStatus().isSuccess()){
                                // show sucess
                            }
                        }
                    });
                }

            }
        }).start();
    }

    private boolean isClientConnected(){
        if (mGoogleApiClient.isConnected()) {
            return true;
        }

        ConnectionResult result = mGoogleApiClient.blockingConnect(TIMEOUT, TimeUnit.MILLISECONDS);

        return result.isSuccess();
    }

    private void startRecording(String bacInputText, boolean isActionFromPhone){
        if(!isRecording){
            isRecording = true;
            registerSensorListeners();
            if(!isActionFromPhone){
                notifyMobile(CommonValues.START_RECORDING_PATH, bacInputText);
            }
            prevBACInput = bacInputText;
            startAndStopButton.setImageResource(R.drawable.ic_action_stop);
            instructionTextView.setText(R.string.instructions_while_recording);
            bacInfoLayout.setVisibility(View.GONE);
            countdownTextView.setVisibility(View.VISIBLE);
            countDownTimer.start();
        }
    }

    private void stopRecording(){
        if(isRecording) {
            unregisterSensorListeners();
            notifyMobile(CommonValues.WEAR_MESSAGE_PATH, CommonValues.STOP_RECORDING);
            isRecording = false;
            startAndStopButton.setImageResource(R.drawable.ic_action_start);
            instructionTextView.setText(R.string.update_instruction);
            bacInfoLayout.setVisibility(View.VISIBLE);
            countdownTextView.setVisibility(View.GONE);
            countDownTimer.cancel();

            Double BAC = Double.valueOf(prevBACInput);
            bacInput.setText(String.valueOf(BAC + 1));


            showToast("BAC input updated for next walk");
        }
    }

    private void connectClientForPhone(){
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();
    }

    private void setupTimer(){
        countDownTimer = new CountDownTimer(CommonValues.RECORD_TIME_IN_SECONDS * 1000, 1000) {

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






    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Wearable.MessageApi.addListener(mGoogleApiClient, this);
        notifyMobile(CommonValues.WEAR_MESSAGE_PATH, CommonValues.WEARABLE_CONNECTED);
    }

    @Override
    public void onConnectionSuspended(int i) {
        isPhoneConnected = false;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        isPhoneConnected = false;

    }

    @Override
    public void onMessageReceived(final MessageEvent messageEvent) {
        runOnUiThread( new Runnable() {
            @Override
            public void run() {
                if(messageEvent.getPath().equalsIgnoreCase( CommonValues.WEAR_MESSAGE_PATH) ) {
                   switch (new String(messageEvent.getData())){
                       case CommonValues.STOP_RECORDING:
                           stopRecording();
                           break;
                       case CommonValues.CONFIRM_WALK:
                           break;
                       case CommonValues.REDO_PREVIOUS_WALK:
                           break;
                       case CommonValues.RESTART_SURVEY:
                           break;
                       case CommonValues.SAVE_SURVEY:
                           break;
                       case CommonValues.WEARABLE_DISCONNECTED:
                           isPhoneConnected = false;
                           break;
                       case CommonValues.WEARABLE_CONNECTED:
                           if(!isPhoneConnected){
                               isPhoneConnected = true;
                               notifyMobile(CommonValues.WEAR_MESSAGE_PATH, CommonValues.WEARABLE_CONNECTED);
                               showToast("Phone Connected");
                           }
                           break;
                       default:
                           break;
                   }
                }else if(messageEvent.getPath().equalsIgnoreCase( CommonValues.START_RECORDING_PATH) ) {
                    startRecording(new String(messageEvent.getData()),true);
                }
            }
        });
    }


    private void notifyMobile(final String path, final String text){
        new Thread( new Runnable() {
            @Override
            public void run() {
                NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
                for(Node node : nodes.getNodes()) {
                    MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(
                            mGoogleApiClient, node.getId(), path, text.getBytes() ).await();
                }
            }
        }).start();
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

    private void showToast(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(WearHomeActivity.this, text, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onPeerConnected(Node node) {
        isPhoneConnected = true;
    }

    @Override
    public void onPeerDisconnected(Node node) {
        isPhoneConnected = false;
        showToast("Phone Disconnected");
    }

    public void requestPermissions(){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.BODY_SENSORS)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.BODY_SENSORS)) {

            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.BODY_SENSORS},
                        BODY_SENSOR_PERMISSION_CODE);
            }
        }else{
            setupSensors();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case BODY_SENSOR_PERMISSION_CODE : {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setupSensors();
                } else {
                    // permission denied by user. disable
                }
                return;
            }
        }
    }
}
