package edu.wpi.alcogaitdatagatherer;

import android.app.AlertDialog;
import android.app.ProgressDialog;
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


    private final String[] space = {""};
    private final int MAXLOGSFORLOG = 5;

    private String mFileName = null;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mGyroscope;
    private boolean isRecording;
    private CSVWriter writer;
    private Context context;
    private Walk walk;
    private int currentWalkNumber;
    private TextView walkNumberDisplay;
    private TextView walkLogDisplay;
    private final static int DELAYINMILLISECONDS = 1;
    private TestSubject testSubject;
    private Double BAC;
    private Window window;

    GaitRecorder(Context context, String mFileName, TestSubject testSubject, TextView walkNumberDisplay, TextView walkLogDisplay) {
        this.context = context;
        this.testSubject = testSubject;
        this.mSensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);
        this.mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        this.mGyroscope = mSensorManager.getDefaultSensor(TYPE_GYROSCOPE);
        this.mFileName = mFileName;
        isRecording = false;

        this.walkNumberDisplay = walkNumberDisplay;
        this.walkLogDisplay = walkLogDisplay;
        currentWalkNumber = 1;
        updateWalkNumberDisplay();
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

            walk.addSensorData(gaitData);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    void startRecording(Double BAC) {
        walkLogDisplay.setVisibility(View.GONE);

        this.BAC = BAC;
        walk = new Walk(currentWalkNumber, BAC);

        isRecording = true;
    }

    void stopRecording(final EditText bacInput) {
        isRecording = false;
        walkLogDisplay.setVisibility(View.VISIBLE);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
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
        builder.setMessage("Do you want to keep data from this walk? (" + walk.getSensorDataList().size() + " results) If you choose 'No' you will repeat this walk. \n(Walk Number " + (currentWalkNumber) + ")").setPositiveButton("Yes", dialogClickListener)
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
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
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
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Re-Do Walk");
        builder.setMessage("Do you want re-do the previous walk? (Walk Number " + testSubject.getAllWalksFromSubject().getLast().getWalkNumber() + ")").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();

    }


    void updateWalkLogDisplay (){
        LinkedList<Walk> walks = testSubject.getAllWalksFromSubject();
        LinkedList<Walk> walksToDisplay = new LinkedList<>();
        int j = 0;

        for(int i = walks.size(); ((j < MAXLOGSFORLOG) && (j<walks.size())); i--){
            walksToDisplay.add(walks.get(i-1));
            j++;
        }

        String walkLog;
        walkLog = "Walk Log (Last " + MAXLOGSFORLOG + " Walks):";

        for(Walk aWalk: walksToDisplay){
            walkLog+="\nBAC "+ aWalk.getBAC() + " was recorded for Walk Number " + aWalk.getWalkNumber();
        }

        walkLogDisplay.setText(walkLog);
    }


    private class SaveDataToCSVTask extends AsyncTask<TestSubject, Integer, Void>{
        ProgressDialog dialog = new ProgressDialog(context);
        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            dialog.setMessage("Saving data to " + mFileName);
            dialog.setIndeterminate(false);
            dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            dialog.setProgress(0);
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

                String testSubjectTitle []= {"Subject ID", "Gender", "Birth Date", "Weight", "Height(in feet and inches)"};

                TestSubject currentTestSubject = allTestSubjects [0];

                String testSubjectInformation [] = {currentTestSubject.getSubjectID(), currentTestSubject.getGender().toString(),
                        currentTestSubject.getBirthDate(), String.valueOf(currentTestSubject.getWeight()) + " lbs",
                        String.valueOf(currentTestSubject.getHeightFeet()) + "' " + String.valueOf(currentTestSubject.getHeightInches()) + "''"};

                writer.writeNext(testSubjectTitle);
                writer.writeNext(testSubjectInformation);
                writer.writeNext(space);
                writer.writeNext(space);

                for (Walk aWalk: currentTestSubject.getAllWalksFromSubject()){

                    for (String[] data : aWalk.toCSVFormat()) {
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
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            dialog.dismiss();
            Intent intent = new Intent(context, HomeActivity.class);
            context.startActivity(intent);
            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }
    }


}
