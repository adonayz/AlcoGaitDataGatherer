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
import android.widget.FrameLayout;
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

import edu.wpi.alcogaitdatagatherercommon.CommonCode;

public class WearHomeActivity extends WearableActivity implements MessageClient.OnMessageReceivedListener, CapabilityClient.OnCapabilityChangedListener, SensorEventListener {

    private TextView instructionTextView;
    private TextView countdownTextView;
    private CountDownTimer countDownTimer;
    private FrameLayout progressBarHolder;
    private TextView wearConnectProgressUpdateTextView;

    private AlphaAnimation inAnimation;
    private AlphaAnimation outAnimation;

    private SensorManager mSensorManager;
    private Sensor mHeartRateSensor;
    private Sensor mAccelerometer;
    private Sensor mGyroscope;

    private String currentWalkType;
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

        setupTimer();

        recordedSamples = 0;
    }

    private void initViews(){
        instructionTextView = findViewById(R.id.instruction);
        countdownTextView = findViewById(R.id.countdown);
        progressBarHolder = findViewById(R.id.progressBarHolder);
        wearConnectProgressUpdateTextView = findViewById(R.id.wearConnectProgressUpdateTextView);
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
                CommonCode.WEAR_DISCOVERY_NAME);
        Wearable.getMessageClient(this).addListener(this);
    }

    @Override
    protected void onPause() {
        if (isRecording) {
            stopRecording();
        }
        unregisterSensorListeners();
        notifyPhone(CommonCode.WEAR_MESSAGE_PATH, CommonCode.WEARABLE_DISCONNECTED);
        Wearable.getCapabilityClient(this).removeListener(
                this,
                CommonCode.WEAR_DISCOVERY_NAME);
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
            //CommonCode.generatePrintableSensorData(sensorEvent.sensor.getName(),sensorEvent.values, sensorEvent.accuracy, sensorEvent.timestamp);
            sendSensorData(sensorEvent.sensor.getType(), sensorEvent.sensor.getName(), sensorEvent.values, sensorEvent.accuracy, sensorEvent.timestamp);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    public void sendSensorData(final int sensorType, final String sensorName, final float[] values, final int accuracy, final long timestamp) {
        Thread sendingThread = new Thread(() -> {
            PutDataMapRequest dataMap = PutDataMapRequest.create(CommonCode.SENSOR_PATH + String.valueOf(sensorType));

            dataMap.getDataMap().putString(CommonCode.SENSOR_NAME, sensorName);
            dataMap.getDataMap().putFloatArray(CommonCode.VALUES, values);
            dataMap.getDataMap().putInt(CommonCode.ACCURACY, accuracy);
            dataMap.getDataMap().putLong(CommonCode.TIMESTAMP, timestamp);
            dataMap.getDataMap().putString(CommonCode.WALK_TYPE_INFO, currentWalkType);

            PutDataRequest putDataRequest = dataMap.asPutDataRequest();

            Wearable.getDataClient(WearHomeActivity.this).putDataItem(putDataRequest).addOnSuccessListener(dataItem -> {
                Log.v("WearHomeActivty", "Sent sensor data.");
                if (timestamp == CommonCode.TRANSFER_FINISHED_LONG && sensorName.equals(CommonCode.TRANSFER_FINISHED_STRING)) {
                    stopProgressBar();
                    executorService.shutdown();
                } else {
                    recordedSamples++;
                }
            });
        });

        executorService.submit(sendingThread);
    }

    private void startRecording(String walkType) {
        if(!isRecording){
            isRecording = true;
            currentWalkType = walkType;
            recordedSamples = 0;
            executorService = Executors.newSingleThreadExecutor();
            registerSensorListeners();
            instructionTextView.setText(R.string.walk_instructions_detail);
            countdownTextView.setVisibility(View.VISIBLE);
            countDownTimer.start();
        }
    }

    private void stopRecording(){
        if(isRecording) {
            isRecording = false;
            startProgressBar();
            wearConnectProgressUpdateTextView.setText("Samples sent " + String.valueOf(recordedSamples));
            sendSensorData(12456345, CommonCode.TRANSFER_FINISHED_STRING, new float[1], noti_counter++, CommonCode.TRANSFER_FINISHED_LONG);
            notifyPhone(CommonCode.STOP_RECORDING_PATH, String.valueOf(recordedSamples));
            unregisterSensorListeners();
            instructionTextView.setText(R.string.use_phone_instruction);
            countdownTextView.setVisibility(View.GONE);
            countDownTimer.cancel();
        }
    }

    private void setupTimer(){
        countDownTimer = new CountDownTimer(CommonCode.RECORD_TIME_IN_SECONDS * 1000, 1000) {

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
            if (messageEvent.getPath().equalsIgnoreCase(CommonCode.WEAR_MESSAGE_PATH)) {
                switch (new String(messageEvent.getData())) {
                    case CommonCode.REDO_PREVIOUS_WALK:
                        break;
                    case CommonCode.RESTART:
                        break;
                    case CommonCode.REFRESH_CONNECTION:
                        new CheckPhoneReachability().execute();
                        break;
                    case CommonCode.WEARABLE_DISCONNECTED:
                        showToast("PHONE DISCONNECTED");
                        break;
                    case CommonCode.CHECK_IF_APP_OPEN:
                        notifyPhone(CommonCode.WEAR_MESSAGE_PATH, CommonCode.APP_OPEN_ACK);
                        stopProgressBar();
                        showToast("CONNECTION REQUEST RECEIVED");
                        break;
                    case CommonCode.APP_OPEN_ACK:
                        stopProgressBar();
                        showToast("ACK_CONFIRMED");
                        break;
                    case CommonCode.STOP_RECORDING:
                        stopRecording();
                    default:
                        break;
                }
            } else if (messageEvent.getPath().equalsIgnoreCase(CommonCode.START_RECORDING_PATH)) {
                startRecording(new String(messageEvent.getData()));
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
                                CommonCode.WEAR_DISCOVERY_NAME, CapabilityClient.FILTER_REACHABLE));
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
            return capabilityInfo;
        }

        @Override
        protected void onPostExecute(CapabilityInfo result) {
            if (result != null) {
                if (result.getNodes().size() > 0) {
                    showToast("PHONE IS REACHABLE");
                    wearConnectProgressUpdateTextView.setText("Awaiting Watch Response");
                    notifyPhone(CommonCode.WEAR_MESSAGE_PATH, CommonCode.CHECK_IF_APP_OPEN);
                    return;
                }
            }
            showToast("COULD NOT FIND PHONE APP");
        }
    }

    private void showToast(final String text) {
        runOnUiThread(() -> Toast.makeText(WearHomeActivity.this, text, Toast.LENGTH_LONG).show());
    }

    public void requestPermissions() {
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
        } else {
            setupSensors();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case BODY_SENSOR_PERMISSION_CODE: {
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
}
