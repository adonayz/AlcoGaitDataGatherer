package edu.wpi.alcogaitdatagatherer;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.view.Window;
import android.view.WindowManager;

import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by Adonay on 12/1/2017.
 */

public class SaveWalkHolderToCSVTask extends AsyncTask<Void, Integer, Void> {
    private int savedSamples = 0;
    private ProgressDialog dialog;
    private String mFolderName;
    private TestSubject testSubject;
    private Window window;
    private final String[] space = {""};
    private SensorRecorder sensorRecorder;

    SaveWalkHolderToCSVTask(SensorRecorder sensorRecorder, String mFolderName, TestSubject testSubject, Window window) {
        this.mFolderName = mFolderName;
        this.testSubject = testSubject;
        this.window = window;
        dialog = new ProgressDialog(window.getContext());
        this.sensorRecorder = sensorRecorder;

    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        dialog.setTitle("Saving to phone internal storage");
        dialog.setTitle("Writing data to " + mFolderName);
        dialog.setIndeterminate(false);
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setProgress(savedSamples);
        int max = testSubject.getCurrentWalkHolder().getSampleSize();
        dialog.setMax(max);
        dialog.show();
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    @Override
    protected Void doInBackground(Void... voids) {
        String fileName = mFolderName + File.separator + "walk_" + testSubject.getCurrentWalkHolder().getWalkNumber() + ".csv";
        File f = new File(fileName);
        try {
            CSVWriter writer;
            if (f.exists() && !f.isDirectory()) {
                writer = new CSVWriter(new FileWriter(fileName));
            } else {
                FileWriter mFileWriter = new FileWriter(fileName, true);
                writer = new CSVWriter(mFileWriter);
            }

            String testSubjectTitle[] = {"Subject ID", "Gender", "Age", "Weight", "Height(in feet and inches)"};

            String testSubjectInformation[] = {testSubject.getSubjectID(), testSubject.getGender().toString(),
                    String.valueOf(testSubject.getAge()), String.valueOf(testSubject.getWeight()) + " lbs",
                    String.valueOf(testSubject.getHeightFeet()) + "' " + String.valueOf(testSubject.getHeightInches()) + "''"};

            writer.writeNext(testSubjectTitle);
            writer.writeNext(testSubjectInformation);
            writer.writeNext(space);

            for (WalkType walkType : WalkType.values()) {
                if (testSubject.getCurrentWalkHolder().hasWalk(walkType)) {
                    writer.writeNext(new String[]{walkType.toString()});
                    writer.writeNext(space);
                    for (String[] data : testSubject.getCurrentWalkHolder().get(walkType).toCSVFormat()) {
                        writer.writeNext(data);
                        publishProgress(++savedSamples);
                    }

                    writer.writeNext(space);
                    writer.writeNext(space);
                    writer.writeNext(space);
                    writer.writeNext(space);
                }
            }

               /* String messageTitle[] = {"Report Message"};
                writer.writeNext(messageTitle);
                String reportMessage[] = {testSubject.getReportMessage()};
                writer.writeNext(reportMessage);*/

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        dialog.setProgress(values[0]);
        dialog.setMessage("Saving Walk Number " + testSubject.getCurrentWalkHolder().getWalkNumber());
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
        dialog.dismiss();
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        sensorRecorder.incrementWalkNumber();
    }
}