package edu.wpi.alcogaitdatagatherer;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;

import static android.content.Context.SENSOR_SERVICE;
import static android.hardware.Sensor.TYPE_GYROSCOPE;

/**
 * Created by Adonay on 9/11/2017.
 */

public class GaitRecorder implements SensorEventListener {

    private String mFileName = null;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mGyroscope;
    private boolean isRecording;
    private CSVWriter writer;
    private Context context;
    private TextView accDataDisplay;
    private TextView gyroDataDisplay;
    private LinkedList<String[]> listOfData;
    private int countAttempts;
    private TextView countAttemptsTextView;
    private LinkedList<LinkedList<String[]>> completeDataSetFromSurvey;
    private final static int DELAYINMILLISECONDS = 1;

    GaitRecorder(Context context, String mFileName, TextView accDataDisplay, TextView gyroDataDisplay, TextView countAttemptsTextView) {
        this.context = context;
        this.mSensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);
        this.mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        this.mGyroscope = mSensorManager.getDefaultSensor(TYPE_GYROSCOPE);
        this.mFileName = mFileName;
        isRecording = false;
        listOfData = new LinkedList<>();
        completeDataSetFromSurvey = new LinkedList<>();


        this.countAttemptsTextView = countAttemptsTextView;
        countAttempts = 0;
        updateAttemptCount();

        this.accDataDisplay = accDataDisplay;
        this.gyroDataDisplay = gyroDataDisplay;
        this.accDataDisplay.setText("");
        this.gyroDataDisplay.setText("");
    }

    public void registerListeners() {
        mSensorManager.registerListener(this, mAccelerometer, DELAYINMILLISECONDS * 1000);
        mSensorManager.registerListener(this, mGyroscope, DELAYINMILLISECONDS * 1000);
    }

    public void unregisterListeners() {
        mSensorManager.unregisterListener(this);
    }


    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) {
            /*String gaitData[] = {sensorEvent.sensor.getName(), " X: " + sensorEvent.values[0], " Y: " + sensorEvent.values[1], " Z: " + sensorEvent.values[2]};
            dataDisplay.setText(gaitData.toString());*/
            return;
        }

        if (isRecording) {
            String sensorName = sensorEvent.sensor.getName();
            Date date = new Date();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            String gaitData[] = {sensorName, " X: " + sensorEvent.values[0], " Y: " + sensorEvent.values[1], " Z: " + sensorEvent.values[2], simpleDateFormat.format(date)};

            listOfData.add(gaitData);

            String sensorData = sensorName + " X: " + sensorEvent.values[0] + " Y: " + sensorEvent.values[1] + " Z: " + sensorEvent.values[2];

            if(Sensor.STRING_TYPE_ACCELEROMETER.equals(sensorEvent.sensor.getStringType())){
                accDataDisplay.setText(sensorData);
            }else if(Sensor.STRING_TYPE_GYROSCOPE.equals(sensorEvent.sensor.getStringType())){
                gyroDataDisplay.setText(sensorData);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public void startRecording() {
        isRecording = true;
    }

    public void stopRecording() {
        isRecording = false;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        countAttempts++;
                        updateAttemptCount();
                        completeDataSetFromSurvey.add(listOfData);
                        listOfData = new LinkedList<>();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        listOfData = new LinkedList<>();
                        break;
                }
            }
        };

        builder.setMessage("Do you want to keep data from this session? (" + listOfData.size() + " results)").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();

    }

    public void writeToCSV(LinkedList<String[]> listOfData) {
        File f = new File(mFileName);
        try {
            if (f.exists() && !f.isDirectory()) {
                writer = new CSVWriter(new FileWriter(mFileName));
            } else {
                FileWriter mFileWriter = new FileWriter(mFileName, true);
                writer = new CSVWriter(mFileWriter);
            }

            for (String[] data : listOfData) {
                writer.writeNext(data);
            }

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Sensor getmAccelerometer() {
        return mAccelerometer;
    }

    public Sensor getmGyroscope() {
        return mGyroscope;
    }

    private void updateAttemptCount(){
        countAttemptsTextView.setText("Walk number " + countAttempts);
    }

    public void restartDataCollection(final EditText bacInput, final Button beginButton){
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        restart();
                        bacInput.setHint("Enter BAC");
                        beginButton.setText("Begin");
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        break;
                }
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Do you want to remove all data and restart data collection from scratch? (" + completeDataSetFromSurvey.size() + " attempts/sessions of data collected)").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();

    }

    public void restart(){
        isRecording = false;
        listOfData = new LinkedList<>();
        completeDataSetFromSurvey = new LinkedList<>();

        countAttempts = 0;
        updateAttemptCount();

        this.accDataDisplay.setText("");
        this.gyroDataDisplay.setText("");
    }

    public void saveSessions(){
        for (LinkedList<String[]> los: completeDataSetFromSurvey){
            writeToCSV(los);
        }
    }
}
