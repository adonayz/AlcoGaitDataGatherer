package edu.wpi.alcogaitdatagatherer;

import android.app.AlertDialog;
import android.app.ProgressDialog;
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
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.wearable.DataMap;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;

import edu.wpi.alcogaitdatagatherercommon.CommonValues;

import static android.content.Context.SENSOR_SERVICE;
import static android.hardware.Sensor.TYPE_ACCELEROMETER;
import static android.hardware.Sensor.TYPE_GYROSCOPE;

/**
 * Created by Adonay on 9/11/2017.
 */

public class SensorRecorder implements SensorEventListener{

    private DataGatheringActivity gatheringActivity;
    private CSVWriter writer;
    private TestSubject testSubject;
    private Walk walk;

    private TextView walkNumberDisplay;
    private TextView walkLogDisplay;
    private Window window;

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mGyroscope;

    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    private final String[] space = {""};
    private final int MAXLOGS = 5;
    private String mFileName = null;
    private boolean isRecording;
    private Double BAC;
    private int currentWalkNumber;
    private String TAG = "SensorRecorder";
    private DialogInterface.OnClickListener dialogClickListener;
    private boolean isInDialogMode = false;

    SensorRecorder(DataGatheringActivity gatheringActivity, String mFileName, TestSubject testSubject, TextView walkNumberDisplay, TextView walkLogDisplay) {
        this.gatheringActivity = gatheringActivity;
        this.testSubject = testSubject;
        this.mSensorManager = (SensorManager) gatheringActivity.getSystemService(SENSOR_SERVICE);
        this.mAccelerometer = mSensorManager.getDefaultSensor(TYPE_ACCELEROMETER);
        this.mGyroscope = mSensorManager.getDefaultSensor(TYPE_GYROSCOPE);
        this.mFileName = mFileName;
        isRecording = false;

        this.walkNumberDisplay = walkNumberDisplay;
        this.walkLogDisplay = walkLogDisplay;
        currentWalkNumber = 1;
        updateWalkNumberDisplay();
    }

    public void registerListeners() {
        mSensorManager.registerListener(this, mAccelerometer, CommonValues.DELAYINMILLISECONDS * 1000);
        mSensorManager.registerListener(this, mGyroscope, CommonValues.DELAYINMILLISECONDS * 1000);
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
            Date date = new Date();
            String sensorData[] = {sensorName, String.valueOf(sensorEvent.values[0]), String.valueOf(sensorEvent.values[1]), String.valueOf(sensorEvent.values[2]), simpleDateFormat.format(date)};

            if(sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
                walk.addAccelerometerData(sensorData);
            }else if(sensorEvent.sensor.getType() == Sensor.TYPE_GYROSCOPE){
                walk.addGyroscopeData(sensorData);
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
        walk = new Walk(currentWalkNumber, BAC);
    }

    void stopRecording(final EditText bacInput) {
        isRecording = false;
        unregisterListeners();
        walkLogDisplay.setVisibility(View.VISIBLE);

        AlertDialog.Builder builder = new AlertDialog.Builder(gatheringActivity);

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
                .setNegativeButton("No", dialogClickListener).show();
    }

    public void writeToCSV(final Window window) {
        this.window = window;
        new SaveDataToCSVTask().execute(testSubject);

    }

    private void updateWalkNumberDisplay(){
        walkNumberDisplay.setText("Walk Number " + (currentWalkNumber));
    }

    void restartDataCollection(final EditText bacInput){
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        bacInput.setText("");
                        restart();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        break;
                }
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(gatheringActivity);
        builder.setTitle("Restart");
        builder.setMessage("Do you want to delete all walks and restart data collection from scratch? (" + testSubject.getAllWalksFromSubject().size() + " walk(s) recorded)").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();

    }

    private void restart(){
        isRecording = false;
        walk = null;
        testSubject.clearWalkData();
        currentWalkNumber = 1;
        updateWalkNumberDisplay();
        updateWalkLogDisplay();
        walkLogDisplay.setVisibility(View.GONE);
    }

    void reDoWalk(final EditText bacInput){
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        Walk prevWalk = testSubject.removeLastWalk();
                        bacInput.setText(String.valueOf(prevWalk.getBAC()));
                        currentWalkNumber = prevWalk.getWalkNumber();
                        updateWalkNumberDisplay();
                        updateWalkLogDisplay();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        break;
                }
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(gatheringActivity);
        builder.setTitle("Re-Do Walk");
        builder.setMessage("Do you want re-do the previous walk? (Walk Number " + testSubject.getAllWalksFromSubject().getLast().getWalkNumber() + ")").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();

    }


    void updateWalkLogDisplay (){
        LinkedList<Walk> walks = testSubject.getAllWalksFromSubject();
        LinkedList<Walk> walksToDisplay = new LinkedList<>();
        int j = 0;

        for(int i = walks.size(); ((j < MAXLOGS) && (j<walks.size())); i--){
            walksToDisplay.add(walks.get(i-1));
            j++;
        }

        String walkLog;
        walkLog = "Walk Log (Last " + MAXLOGS + " Walks):";

        for(Walk aWalk: walksToDisplay){
            walkLog+="\nBAC "+ aWalk.getBAC() + " was recorded for Walk Number " + aWalk.getWalkNumber();
        }

        walkLogDisplay.setText(walkLog);
    }

    private class SaveDataToCSVTask extends AsyncTask<TestSubject, Integer, Void>{
        ProgressDialog dialog = new ProgressDialog(gatheringActivity);
        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            dialog.setTitle("Saving to phone internal storage");
            dialog.setTitle("Writing data to " + mFileName);
            dialog.setIndeterminate(false);
            dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            dialog.setProgress(0);
            dialog.setMax(testSubject.getAllWalksFromSubject().size());
            dialog.show();
            window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }

        @Override
        protected Void doInBackground(TestSubject... allTestSubjects) {
            File f = new File(mFileName);
            try {
                if (f.exists() && !f.isDirectory()) {
                    writer = new CSVWriter(new FileWriter(mFileName));
                } else {
                    FileWriter mFileWriter = new FileWriter(mFileName, true);
                    writer = new CSVWriter(mFileWriter);
                }

                String testSubjectTitle []= {"Subject ID", "Gender", "Age", "Weight", "Height(in feet and inches)"};

                TestSubject currentTestSubject = allTestSubjects [0];

                String testSubjectInformation [] = {currentTestSubject.getSubjectID(), currentTestSubject.getGender().toString(),
                        String.valueOf(currentTestSubject.getAge()), String.valueOf(currentTestSubject.getWeight()) + " lbs",
                        String.valueOf(currentTestSubject.getHeightFeet()) + "' " + String.valueOf(currentTestSubject.getHeightInches()) + "''"};

                writer.writeNext(testSubjectTitle);
                writer.writeNext(testSubjectInformation);
                writer.writeNext(space);

                LinkedList<Walk> allWalks = currentTestSubject.getAllWalksFromSubject();

                for (int i = 0; i < allWalks.size(); i++){
                    publishProgress(i);
                    for (String[] data : allWalks.get(i).toCSVFormat()) {
                        writer.writeNext(data);
                    }

                    writer.writeNext(space);
                    writer.writeNext(space);
                    writer.writeNext(space);
                    writer.writeNext(space);
                    writer.writeNext(space);
                    writer.writeNext(space);
                    writer.writeNext(space);
                    writer.writeNext(space);
                }

                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            dialog.setProgress(values[0]);
            dialog.setMessage("Saving Walk" + (values[0] + 1));
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            dialog.dismiss();
            Intent intent = new Intent(gatheringActivity, HomeActivity.class);
            gatheringActivity.startActivity(intent);
            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }
    }

    public void addHeartRateData(String sensorType, DataMap dataMap){
        if(isRecording){
            unpackSensorData(sensorType, dataMap);
        }
    }

    private void unpackSensorData(String sensorType, DataMap dataMap) {
        //int accuracy = dataMap.getInt(CommonValues.ACCURACY);
        long timestamp = dataMap.getLong(CommonValues.TIMESTAMP);
        float[] values = dataMap.getFloatArray(CommonValues.VALUES);
        String [] heartRateData = new String[values.length + 2];

        //heartRateData[0] = mSensorManager.getDefaultSensor(sensorType).getName();
        heartRateData[0] = sensorType;

        int i;
        for(i = 1; i < values.length; i++){
            heartRateData[i] = String.valueOf(values[i]);
        }

        heartRateData[i+1] = simpleDateFormat.format(timestamp);

        if(heartRateData.length > 0){
            walk.addHeartRateData(heartRateData);
        }
    }

    public boolean isRecording(){
        return isRecording;
    }

    private void showToast(final String text) {
        gatheringActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(gatheringActivity, text, Toast.LENGTH_LONG).show();
            }
        });
    }
}
