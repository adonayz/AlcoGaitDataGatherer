package edu.wpi.alcogaitdatagatherer;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.wearable.DataMap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Locale;

import edu.wpi.alcogaitdatagatherercommon.CommonValues;

import static android.content.Context.SENSOR_SERVICE;
import static android.hardware.Sensor.TYPE_ACCELEROMETER;
import static android.hardware.Sensor.TYPE_GYROSCOPE;
import static android.hardware.Sensor.TYPE_HEART_RATE;
import static android.hardware.Sensor.TYPE_MAGNETIC_FIELD;

/**
 * Created by Adonay on 9/11/2017.
 */

public class SensorRecorder implements SensorEventListener {

    private TestSubject testSubject;
    private Walk walk;

    private TextView walkNumberDisplay;
    private TextView walkLogDisplay;
    private Button startButton;
    private Window window;

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mGyroscope;
    private Sensor mMagnetometer;

    private float[] accelVal;
    private float[] gyroVal;
    private float[] magVal;
    private LinkedList<Walk> logQueue;

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.SSS", Locale.US);
    private final int MAXLOGS = 5;
    private String mFolderName = null;
    private boolean isRecording;
    private Double BAC;
    private int currentWalkNumber;
    private WalkType currentWalkType;
    private String TAG = "SensorRecorder";
    private static final float ALPHA = 0.15f;
    private DialogInterface.OnClickListener dialogClickListener;

    SensorRecorder(DataGatheringActivity gatheringActivity, String mFolderName, TestSubject testSubject, TextView walkNumberDisplay, TextView walkLogDisplay, Button startButton) {
        this.testSubject = testSubject;
        this.mSensorManager = (SensorManager) gatheringActivity.getSystemService(SENSOR_SERVICE);
        this.mAccelerometer = mSensorManager.getDefaultSensor(TYPE_ACCELEROMETER);
        this.mGyroscope = mSensorManager.getDefaultSensor(TYPE_GYROSCOPE);
        this.mMagnetometer = mSensorManager.getDefaultSensor(TYPE_MAGNETIC_FIELD);
        this.mFolderName = mFolderName;
        isRecording = false;

        this.walkNumberDisplay = walkNumberDisplay;
        this.walkLogDisplay = walkLogDisplay;
        this.startButton = startButton;
        logQueue = new LinkedList<Walk>();
        currentWalkNumber = 1;
        currentWalkType = WalkType.NORMAL;
        updateWalkNumberDisplay();
        testSubject.setCurrentWalkHolder(new WalkHolder(currentWalkNumber));
        testSubject.setWalkTypeAmount(walkNumberDisplay.getContext());
    }

    public void registerListeners() {
        mSensorManager.registerListener(this, mAccelerometer, CommonValues.DELAYINMILLISECONDS * 1000);
        mSensorManager.registerListener(this, mGyroscope, CommonValues.DELAYINMILLISECONDS * 1000);
        mSensorManager.registerListener(this, mMagnetometer, CommonValues.DELAYINMILLISECONDS * 1000);
    }

    public void unregisterListeners() {
        mSensorManager.unregisterListener(this);
    }


    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) {
            return;
        }

        if (isRecording) {
            String sensorName = sensorEvent.sensor.getName();

            if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                accelVal = lowPass(sensorEvent.values.clone(), accelVal);
                walk.addPhoneAccelerometerData(generatePrintableSensorData(sensorName, accelVal, sensorEvent.timestamp));
            }
            if (sensorEvent.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                gyroVal = lowPass(sensorEvent.values.clone(), gyroVal);
                walk.addPhoneGyroscopeData(generatePrintableSensorData(sensorName, gyroVal, sensorEvent.timestamp));
            }
            if (sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                magVal = lowPass(sensorEvent.values.clone(), magVal);
            }
            if (accelVal != null && magVal != null) {
                float R[] = new float[9];
                float I[] = new float[9];
                boolean success = SensorManager.getRotationMatrix(R, I, accelVal, magVal);
                if (success) {
                    float compassVal[] = new float[3];
                    SensorManager.getOrientation(R, compassVal);
                    walk.addCompasData(generatePrintableSensorData("Compass", compassVal, sensorEvent.timestamp));
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    void startRecording(Double BAC) {
        isRecording = true;
        registerListeners();
        walkLogDisplay.setVisibility(View.GONE);
        this.BAC = BAC;
        if (currentWalkType != null) {
            walk = new Walk(testSubject.getCurrentWalkHolder().getWalkNumber(), BAC, currentWalkType);
        }
    }

    boolean stopRecording() {
        isRecording = false;
        unregisterListeners();
        walkLogDisplay.setVisibility(View.VISIBLE);

        testSubject.setCurrentWalkHolder(testSubject.getCurrentWalkHolder().addWalk(walk));

        currentWalkType = testSubject.getCurrentWalkHolder().getNextWalkType();
        if (currentWalkType != null) {
            updateWalkLogDisplay(true);
            updateWalkNumberDisplay();
            return true;
        } else {
            startButton.setText("Finish Walk");
            return false;
        }

        /*AlertDialog.Builder builder = new AlertDialog.Builder(gatheringActivity);

        dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        testSubject.addWalk(walk);

                        updateWalkLogDisplay();
                        bacInput.setText(String.valueOf(BAC + 1));

                        currentWalkNumber++;
                        updateWalkNumberDisplay();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        break;
                }
            }
        };

        builder.setTitle("Confirm Walk");
        builder.setMessage("Do you want to keep data from this walk? (" + walk.getSampleSize() + " samples) If you choose 'No' you will repeat this walk. \n(Walk Number " + (currentWalkNumber) + ")").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();*/
    }

    public void saveCurrentWalkNumberToCSV(final Window window) {
        this.window = window;
        new SaveWalkHolderToCSVTask(this, mFolderName, testSubject, window).execute();
    }

    private void updateWalkNumberDisplay() {
        walkNumberDisplay.setText("Walk Number " + (currentWalkNumber) + " : " + currentWalkType.toString());
    }

    void restartCurrentWalkNumber(final EditText bacInput) {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        bacInput.setText("");
                        bacInput.setEnabled(true);
                        restartWalkHolder();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        break;
                }
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(bacInput.getContext());
        builder.setTitle("Restart");
        builder.setMessage("Do you want to remove all walks for the current walk number? (Walk Number " + testSubject.getCurrentWalkHolder().getWalkNumber() + ") (" + testSubject.getCurrentWalkHolder().getSampleSize() + " samples recorded)").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();

    }

    private void restartWalkHolder() {
        isRecording = false;
        testSubject.replaceWalkHolder(new WalkHolder(currentWalkNumber));
        currentWalkNumber = testSubject.getCurrentWalkHolder().getWalkNumber();
        currentWalkType = testSubject.getCurrentWalkHolder().getNextWalkType();
        updateWalkNumberDisplay();
        clearWalkLog();
        testSubject.setWalkTypeAmount(walkNumberDisplay.getContext());
        walk = testSubject.getCurrentWalkHolder().get(currentWalkType);
        walkLogDisplay.setVisibility(View.GONE);
    }

    void reDoWalk(final EditText bacInput) {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        testSubject.setCurrentWalkHolder(testSubject.getCurrentWalkHolder().removeWalk(walk.getWalkType()));
                        bacInput.setText(String.valueOf(walk.getBAC()));
                        currentWalkType = walk.getWalkType();
                        updateWalkNumberDisplay();
                        //update walk log
                        logQueue.removeLast();
                        updateWalkLogDisplay(false);
                        if (currentWalkType != WalkType.NORMAL) {
                            walk = testSubject.getCurrentWalkHolder().get(testSubject.getCurrentWalkHolder().getPreviousWalkType(currentWalkType));
                        } else {
                            bacInput.setEnabled(true);
                            walk = null;
                        }
                        startButton.setText("START WALK");
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                }
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(bacInput.getContext());
        builder.setTitle("Re-Do Walk");
        builder.setMessage("Do you want re-do the previous walk? (Walk Number " + testSubject.getCurrentWalkHolder().getWalkNumber() +
                " : " + testSubject.getCurrentWalkHolder().getPreviousWalkType(currentWalkType) + ")").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();

    }


    void updateWalkLogDisplay(boolean addNewWalk) {
        if (addNewWalk) {
            if (logQueue.size() >= MAXLOGS) {
                logQueue.removeFirst();
            }

            logQueue.add(walk);
        }

        String walkLog;
        walkLog = "Walk Log (Last " + MAXLOGS + " Walks):";
        for (int i = logQueue.size() - 1; i >= 0; i--) {
            Walk aWalk = logQueue.get(i);
            walkLog += "\nBAC " + aWalk.getBAC() + " was recorded for Walk Number " + aWalk.getWalkNumber() + " : " + aWalk.getWalkType().toString();
        }

        walkLogDisplay.setText(walkLog);
    }

    void clearWalkLog() {
        logQueue.clear();
        walkLogDisplay.setText("");
    }

    public void addWearableSensorData(int sensorType, DataMap dataMap) {
        if (isRecording) {
            String[] sensorData;
            String sensorName = dataMap.getString(CommonValues.SENSOR_NAME);
            float[] values = dataMap.getFloatArray(CommonValues.VALUES);
            long timestamp = dataMap.getLong(CommonValues.TIMESTAMP);

            if (values.length > 0) {
                if (sensorType == TYPE_HEART_RATE) {
                    sensorData = generatePrintableSensorData(sensorName, values, timestamp);
                    walk.addHeartRateData(sensorData);
                } else if (sensorType == TYPE_ACCELEROMETER) {
                    sensorData = generatePrintableSensorData(sensorName, values, timestamp);
                    walk.addWatchAccelerometerData(sensorData);
                } else if (sensorType == TYPE_GYROSCOPE) {
                    sensorData = generatePrintableSensorData(sensorName, values, timestamp);
                    walk.addWatchGyroscopeData(sensorData);
                }
            }
        }
    }

    public boolean isRecording() {
        return isRecording;
    }

    public TestSubject getTestSubject() {
        return testSubject;
    }

    public void setTestSubject(TestSubject testSubject) {
        this.testSubject = testSubject;
    }

    private float[] lowPass(float[] input, float[] output) {
        if (output == null) return input;

        for (int i = 0; i < input.length; i++) {
            output[i] = output[i] + ALPHA * (input[i] - output[i]);
        }
        return output;
    }

    private String[] generatePrintableSensorData(String sensorName, float[] values, long timestamp) {
        String[] result = new String[values.length + 2];
        result[0] = sensorName;
        int i;
        for (i = 1; i <= values.length; i++) {
            result[i] = String.valueOf(values[i - 1]);
        }

        result[i] = simpleDateFormat.format(new Date(getCurrentTimeFromSensor(timestamp)));

        return result;
    }

    private long getCurrentTimeFromSensor(long sensorTimestamp) {
        return ((((new Date()).getTime() * 1000000L) - System.nanoTime()) + sensorTimestamp) / 1000000L;
    }

    /*private void showToast(final String text) {
        gatheringActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(gatheringActivity, text, Toast.LENGTH_LONG).show();
            }
        });
    }*/

    public void incrementWalkNumber() {
        currentWalkNumber++;
        testSubject.addNewWalkHolder(new WalkHolder(currentWalkNumber));
        currentWalkType = testSubject.getCurrentWalkHolder().getNextWalkType();
        updateWalkNumberDisplay();
        testSubject.setWalkTypeAmount(walkNumberDisplay.getContext());
        startButton.setText("START WALK");
    }

    public void saveWalkReport() {
        final File file = new File(mFolderName, "report.txt");

        // Save your stream, don't forget to flush() it before closing it.

        try {
            file.createNewFile();
            FileOutputStream fOut = new FileOutputStream(file);
            OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);

            myOutWriter.append("Reported Walk Numbers:\n");
            boolean hasReportedWalks = false;
            for (int i = 0; i < testSubject.getBooleanWalksList().size(); i++) {
                if (testSubject.getBooleanWalksList().get(i)) {
                    myOutWriter.append(String.valueOf(i + 1));
                    if (i != testSubject.getBooleanWalksList().size() - 1 && testSubject.getBooleanWalksList().size() > 1) {
                        myOutWriter.append(", ");
                    }
                    hasReportedWalks = true;
                }
            }
            if (!hasReportedWalks) {
                myOutWriter.append("None");
            }

            myOutWriter.append("\n\nReport Message:\n");
            myOutWriter.append(testSubject.getReportMessage() + "\n");

            myOutWriter.close();

            fOut.flush();
            fOut.close();
        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    public WalkType getCurrentWalkType() {
        return currentWalkType;
    }
}
