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
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.CapabilityClient;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.MessageClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import edu.wpi.alcogaitdatagatherercommon.CommonValues;

public class WearHomeActivity extends WearableActivity implements MessageClient.OnMessageReceivedListener, CapabilityClient.OnCapabilityChangedListener, SensorEventListener {

    private TextView instructionTextView;
    private LinearLayout bacInfoLayout;
    private ImageButton startAndStopButton;
    private TextView countdownTextView;
    private EditText bacInput;
    private CountDownTimer countDownTimer;
    private FrameLayout progressBarHolder;
    private TextView wearConnectProgressUpdateTextView;

    private AlphaAnimation inAnimation;
    private AlphaAnimation outAnimation;

    private SensorManager mSensorManager;
    private Sensor mHeartRateSensor;
    private Sensor mAccelerometer;
    private Sensor mGyroscope;

    private String prevBACInput = "";
    private int recordedSamples = 0; // for debugging purposes
    private int noti_counter = 0; // count how many times the watch had to send a finished request to phone till it responds
    private boolean isRecording = false;
    private ExecutorService executorService;

    private static final int BODY_SENSOR_PERMISSION_CODE = 1010;
    private static final int TIMEOUT = 15000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wear_home);
        setAmbientEnabled();

        requestPermissions();

        initViews();

        configureButtons();

        setupTimer();

        recordedSamples = 0;
    }

    private void initViews(){
        instructionTextView = findViewById(R.id.instruction);
        bacInfoLayout = findViewById(R.id.bacInfoLayout);
        bacInput = findViewById(R.id.bacInput);
        startAndStopButton = findViewById(R.id.startStopButton);
        countdownTextView = findViewById(R.id.countdown);
        progressBarHolder = findViewById(R.id.progressBarHolder);
        wearConnectProgressUpdateTextView = findViewById(R.id.wearConnectProgressUpdateTextView);
    }

    private void configureButtons(){
        startAndStopButton.setOnClickListener(view -> {
            if (bacInput.getText().toString().trim().isEmpty()) {
                bacInput.setError("Enter BAC before recording");
            } else {
                if (!isRecording) {
                    startRecording(bacInput.getText().toString().trim(), false);
                } else {
                    stopRecording();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        new CheckPhoneReachability().execute();
        if (isRecording) {
            registerSensorListeners();
        }
        Wearable.getCapabilityClient(this).addListener(
                this,
                CommonValues.WEAR_DISCOVERY_NAME);
        Wearable.getMessageClient(this).addListener(this);
    }

    @Override
    protected void onPause() {
        if (isRecording) {
            stopRecording();
        }
        unregisterSensorListeners();
        notifyPhone(CommonValues.WEAR_MESSAGE_PATH, CommonValues.WEARABLE_DISCONNECTED);
        Wearable.getCapabilityClient(this).removeListener(
                this,
                CommonValues.WEAR_DISCOVERY_NAME);
        Wearable.getMessageClient(this).removeListener(this);
        super.onPause();
    }

    private void setupSensors(){
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        if (mSensorManager == null) {
            showToast("Sensor Manager not working");
        } else {
            if (mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE) != null) {
                mHeartRateSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
            } else {
                showToast("NO HEART RATE SENSOR");
            }

            if (mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
                mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            } else {
                showToast("NO ACCELEROMETER SENSOR");
            }

            if (mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null) {
                mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
            } else {
                showToast("NO GYROSCOPE SENSOR");
            }
        }
    }

    private void registerSensorListeners(){
        mSensorManager.registerListener(this, mHeartRateSensor, SensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(this, mGyroscope, SensorManager.SENSOR_DELAY_FASTEST);
    }

    private void unregisterSensorListeners(){
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (isRecording) {
            sendSensorData(sensorEvent.sensor.getType(), sensorEvent.sensor.getName(), sensorEvent.accuracy, sensorEvent.timestamp, sensorEvent.values);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    public void sendSensorData(final int sensorType, final String sensorName, final int accuracy, final long timestamp, final float[] values) {
        Thread sendingThread = new Thread(() -> {

            PutDataMapRequest dataMap = PutDataMapRequest.create(CommonValues.SENSOR_PATH + String.valueOf(sensorType));

            dataMap.getDataMap().putString(CommonValues.SENSOR_NAME, sensorName);
            dataMap.getDataMap().putInt(CommonValues.ACCURACY, accuracy);
            dataMap.getDataMap().putLong(CommonValues.TIMESTAMP, timestamp);
            dataMap.getDataMap().putFloatArray(CommonValues.VALUES, values);

            PutDataRequest putDataRequest = dataMap.asPutDataRequest();

            Wearable.getDataClient(WearHomeActivity.this).putDataItem(putDataRequest).addOnSuccessListener(dataItem -> {
                Log.v("WearHomeActivty", "Sent sensor data.");
                if (timestamp == CommonValues.TRANSFER_FINISHED_LONG && sensorName.equals(CommonValues.TRANSFER_FINISHED_STRING)) {
                    stopProgressBar();
                    executorService.shutdown();
                } else {
                    recordedSamples++;
                }
            });
        });

        executorService.submit(sendingThread);
    }

    private void startRecording(String bacInputText, boolean isActionFromPhone){
        if(!isRecording){
            isRecording = true;
            recordedSamples = 0;
            executorService = Executors.newSingleThreadExecutor();
            registerSensorListeners();
            if(!isActionFromPhone){
                notifyPhone(CommonValues.START_RECORDING_PATH, bacInputText);
            }
            prevBACInput = bacInputText;
            bacInput.setText(bacInputText);
            startAndStopButton.setImageResource(R.drawable.ic_action_stop);
            instructionTextView.setText(R.string.instructions_recording_before_30_seconds);
            bacInfoLayout.setVisibility(View.GONE);
            countdownTextView.setVisibility(View.VISIBLE);
            countDownTimer.start();
        }
    }

    private void stopRecording(){
        if(isRecording) {
            isRecording = false;
            startProgressBar();
            wearConnectProgressUpdateTextView.setText("Samples sent " + String.valueOf(recordedSamples));
            sendSensorData(12456345, CommonValues.TRANSFER_FINISHED_STRING, noti_counter++, CommonValues.TRANSFER_FINISHED_LONG, new float[1]);
            notifyPhone(CommonValues.STOP_RECORDING_PATH, String.valueOf(recordedSamples));
            unregisterSensorListeners();
            startAndStopButton.setImageResource(R.drawable.ic_action_start);
            //instructionTextView.setText(R.string.update_instruction);
            bacInfoLayout.setVisibility(View.VISIBLE);
            countdownTextView.setVisibility(View.GONE);
            countDownTimer.cancel();

            //Double BAC = Double.valueOf(prevBACInput);

            showToast("update input for next walk");
        }
    }

    private void setupTimer(){
        countDownTimer = new CountDownTimer(CommonValues.RECORD_TIME_IN_SECONDS * 1000, 1000) {

            public void onTick(long millisUntilFinished) {
                countdownTextView.setText(String.valueOf(millisUntilFinished / 1000));
            }

            public void onFinish() {
                ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
                toneGen1.startTone(ToneGenerator.TONE_CDMA_ONE_MIN_BEEP, 1000);
                Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                v.vibrate(1000);
                stopRecording();
            }
        };
    }

    @Override
    public void onMessageReceived(final MessageEvent messageEvent) {
        runOnUiThread(() -> {
            if (messageEvent.getPath().equalsIgnoreCase(CommonValues.WEAR_MESSAGE_PATH)) {
                switch (new String(messageEvent.getData())) {
                    case CommonValues.REDO_PREVIOUS_WALK:
                        break;
                    case CommonValues.RESTART:
                        break;
                    case CommonValues.REFRESH_CONNECTION:
                        new CheckPhoneReachability().execute();
                        break;
                    case CommonValues.WEARABLE_DISCONNECTED:
                        showToast("PHONE DISCONNECTED");
                        break;
                    case CommonValues.CHECK_IF_APP_OPEN:
                        notifyPhone(CommonValues.WEAR_MESSAGE_PATH, CommonValues.APP_OPEN_ACK);
                        stopProgressBar();
                        showToast("CONNECTION REQUEST RECEIVED");
                        break;
                    case CommonValues.APP_OPEN_ACK:
                        stopProgressBar();
                        showToast("ACK_CONFIRMED");
                        break;
                    case CommonValues.STOP_RECORDING:
                        stopRecording();
                    default:
                        break;
                }
            } else if (messageEvent.getPath().equalsIgnoreCase(CommonValues.START_RECORDING_PATH)) {
                startRecording(new String(messageEvent.getData()), true);
            }
        });
    }

    @Override
    public void onCapabilityChanged(@NonNull CapabilityInfo capabilityInfo) {
        if (capabilityInfo.getNodes().size() > 0) {
            showToast("WATCH IS CONNECTED");
        } else {
            showToast("WATCH IS DISCONNECTED");
        }
    }

    private void notifyPhone(final String path, final String text) {
        new Thread(() -> {
            for (String nodeID : getNodes()) {
                Wearable.getMessageClient(WearHomeActivity.this).sendMessage(
                        nodeID, path, text.getBytes());
            }
        }).start();
    }

    private Collection<String> getNodes() {
        HashSet<String> results = new HashSet<>();
        List<Node> nodes;
        try {
            nodes = Tasks.await(Wearable.getNodeClient(this).getConnectedNodes());
            for (Node node : nodes) {
                results.add(node.getId());
            }
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return results;
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
        runOnUiThread(() -> Toast.makeText(WearHomeActivity.this, text, Toast.LENGTH_LONG).show());
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
            }
        }
    }

    private void startProgressBar() {
        inAnimation = new AlphaAnimation(0f, 1f);
        inAnimation.setDuration(200);
        progressBarHolder.setAnimation(inAnimation);
        progressBarHolder.setVisibility(View.VISIBLE);
    }

    private void stopProgressBar() {
        outAnimation = new AlphaAnimation(1f, 0f);
        outAnimation.setDuration(200);
        progressBarHolder.setAnimation(outAnimation);
        progressBarHolder.setVisibility(View.GONE);
    }

    private class CheckPhoneReachability extends AsyncTask<Void, Void, CapabilityInfo> {
        @Override
        protected void onPreExecute() {
            startProgressBar();
            wearConnectProgressUpdateTextView.setText(R.string.searching);
        }

        @Override
        protected CapabilityInfo doInBackground(Void... voids) {
            CapabilityInfo capabilityInfo = null;
            try {
                capabilityInfo = Tasks.await(
                        Wearable.getCapabilityClient(WearHomeActivity.this).getCapability(
                                CommonValues.WEAR_DISCOVERY_NAME, CapabilityClient.FILTER_REACHABLE));
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
            return capabilityInfo;
        }

        @Override
        protected void onPostExecute(CapabilityInfo result) {
            if (result != null) {
                if (result.getNodes().size() > 0) {
                    showToast("PHONE IS CONNECTED");
                    wearConnectProgressUpdateTextView.setText("Awaiting Response");
                    notifyPhone(CommonValues.WEAR_MESSAGE_PATH, CommonValues.CHECK_IF_APP_OPEN);
                    return;
                }
            }
            showToast("COULD NOT FIND PHONE APP");
        }
    }
}
