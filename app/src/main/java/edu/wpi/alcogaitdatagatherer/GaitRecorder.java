package edu.wpi.alcogaitdatagatherer;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
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
    private LinkedList<String[]> listOfData;
    private int countAttempts;
    private TextView walkNumberDisplay;
    private LinkedList<LinkedList<String[]>> completeDataSetFromSurvey;
    private final static int DELAYINMILLISECONDS = 1;
    private TestSubject testSubject;
    private Double BAC;

    GaitRecorder(Context context, String mFileName, TestSubject testSubject, TextView walkNumberDisplay) {
        this.context = context;
        this.testSubject = testSubject;
        this.mSensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);
        this.mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        this.mGyroscope = mSensorManager.getDefaultSensor(TYPE_GYROSCOPE);
        this.mFileName = mFileName;
        isRecording = false;
        listOfData = new LinkedList<>();
        completeDataSetFromSurvey = new LinkedList<>();

        this.walkNumberDisplay = walkNumberDisplay;
        countAttempts = 0;
        updateWalkCount();
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

            //String sensorData = sensorName + " X: " + sensorEvent.values[0] + " Y: " + sensorEvent.values[1] + " Z: " + sensorEvent.values[2];
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    void startRecording(Double BAC) {
        this.BAC = BAC;

        isRecording = true;
    }

    void stopRecording(final EditText bacInput, final TextView previousBACDisplay) {
        isRecording = false;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        LinkedList<String[]> spaceAndWalkInformation = new LinkedList<>();

                        String[] walkInformation = {"Walk Number " + (countAttempts + 1), "BAC = " + BAC};
                        String[] space1 = {""};

                        spaceAndWalkInformation.add(walkInformation);
                        spaceAndWalkInformation.add(space1);
                        spaceAndWalkInformation.addAll(listOfData);

                        completeDataSetFromSurvey.add(spaceAndWalkInformation);
                        listOfData = new LinkedList<>();
                        countAttempts++;

                        updateWalkCount();
                        String prevBACValueString = bacInput.getText().toString().trim();
                        Double prevBACValue = Double.parseDouble(prevBACValueString);
                        previousBACDisplay.setVisibility(View.VISIBLE);
                        previousBACDisplay.setText("Previous BAC value = " + prevBACValue);
                        bacInput.setText(String.valueOf(prevBACValue + 1));
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        listOfData = new LinkedList<>();
                        break;
                }
            }
        };

        builder.setTitle("Confirm Walk");
        builder.setMessage("Do you want to keep data from this walk? (" + listOfData.size() + " results) If you choose 'No' you will repeat this walk. \n(Walk Number " + (countAttempts + 1) + ")").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();

    }

    public void writeToCSV(final ProgressBar progressBar, final Window window) {
        new AsyncTask<LinkedList<LinkedList<String[]>>, Integer, Void>(){
            @Override
            protected void onPreExecute(){
                super.onPreExecute();
                progressBar.setVisibility(View.VISIBLE);
                window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            }

            @Override
            protected Void doInBackground(LinkedList<LinkedList<String[]>>... linkedLists) {
                File f = new File(mFileName);
                try {
                    if (f.exists() && !f.isDirectory()) {
                        writer = new CSVWriter(new FileWriter(mFileName));
                    } else {
                        FileWriter mFileWriter = new FileWriter(mFileName, true);
                        writer = new CSVWriter(mFileWriter);
                    }

                    for (LinkedList<String[]> los: linkedLists[0]){
                        for (String[] data : los) {
                            writer.writeNext(data);
                        }

                        String[] space1 = {""};
                        String[] space2 = {""};
                        String[] space3 = {""};
                        String[] space4 = {""};
                        String[] space5 = {""};
                        String[] space6 = {""};
                        String[] space7 = {""};
                        String[] space8 = {""};

                        writer.writeNext(space1);
                        writer.writeNext(space2);
                        writer.writeNext(space3);
                        writer.writeNext(space4);
                        writer.writeNext(space5);
                        writer.writeNext(space6);
                        writer.writeNext(space7);
                        writer.writeNext(space8);
                    }

                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

           /* @Override
            protected void onProgressUpdate(Integer... values) {
                //pDialog.setMessage("please wait..."+ values[0]);
                //pDialog.show();
            }*/

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
                progressBar.setVisibility(View.GONE);
                Intent intent = new Intent(context, HomeActivity.class);
                context.startActivity(intent);
                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            }
        }.execute(completeDataSetFromSurvey);

    }

    private void updateWalkCount(){
        walkNumberDisplay.setText("Walk Number " + (countAttempts + 1));
    }

    void restartDataCollection(final EditText bacInput, final Button beginButton){
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
        builder.setTitle("Restart");
        builder.setMessage("Do you want to remove all walks and restart data collection from scratch? (" + completeDataSetFromSurvey.size() + " walk(s) recorded)").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();

    }

    private void restart(){
        isRecording = false;
        listOfData = new LinkedList<>();
        completeDataSetFromSurvey = new LinkedList<>();

        countAttempts = 0;
        updateWalkCount();
    }
}
