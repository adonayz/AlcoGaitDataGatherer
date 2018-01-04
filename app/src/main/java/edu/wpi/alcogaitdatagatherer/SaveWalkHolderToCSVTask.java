package edu.wpi.alcogaitdatagatherer;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.widget.EditText;

import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import edu.wpi.alcogaitdatagatherercommon.WalkType;

/**
 * Created by Adonay on 12/1/2017.
 */

public class SaveWalkHolderToCSVTask extends AsyncTask<Void, Integer, Boolean> {
    private int savedSamples = 0;
    private ProgressDialog dialog;
    private String mFolderName;
    private TestSubject testSubject;
    private final String[] space = {""};
    private SensorRecorder sensorRecorder;
    private EditText bacInput;

    SaveWalkHolderToCSVTask(SensorRecorder sensorRecorder, String mFolderName, EditText bacInput) {
        this.sensorRecorder = sensorRecorder;
        this.mFolderName = mFolderName;
        this.testSubject = sensorRecorder.getTestSubject();
        this.dialog = new ProgressDialog(bacInput.getContext());
        this.bacInput = bacInput;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        dialog.setCancelable(false);
        dialog.setTitle("Saving to phone internal storage");
        dialog.setTitle("Writing data to " + mFolderName);
        dialog.setIndeterminate(false);
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setProgress(0);
        dialog.setMax(100);
        dialog.show();
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        String fileName = mFolderName + File.separator + "phone.csv";
        File f = new File(fileName);
        try {
            CSVWriter writer;
            FileWriter mFileWriter;
            if (f.exists() && !f.isDirectory()) {
                mFileWriter = new FileWriter(fileName, false);
            } else {
                mFileWriter = new FileWriter(fileName);
            }

            writer = new CSVWriter(mFileWriter);

            String testSubjectTitle[] = {"Subject ID", "Gender", "Age", "Weight", "Height(feet and inches)"};

            String testSubjectInformation[] = {testSubject.getSubjectID(), testSubject.getGender().toString(),
                    String.valueOf(testSubject.getAge()), String.valueOf(testSubject.getWeight()) + " lbs",
                    String.valueOf(testSubject.getHeightFeet()) + "' " + String.valueOf(testSubject.getHeightInches()) + "''"};

            writer.writeNext(testSubjectTitle);
            writer.writeNext(testSubjectInformation);
            writer.writeNext(space);

            int percentProgress = 0;
            int max = testSubject.getCurrentWalkHolder().getSampleSize();
            for (WalkType walkType : WalkType.values()) {
                if (testSubject.getCurrentWalkHolder().hasWalk(walkType)) {
                    writer.writeNext(new String[]{walkType.toString()});
                    writer.writeNext(space);
                    for (String[] data : testSubject.getCurrentWalkHolder().get(walkType).toCSVFormat()) {
                        writer.writeNext(data);
                        if ((++savedSamples / max) * 100 > percentProgress) {
                            publishProgress((savedSamples / max) * 100);
                        }
                        percentProgress = (savedSamples / max) * 100;
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
            return false;
        }
        return true;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        dialog.setProgress(values[0]);
        dialog.setMessage("Saving Walk Number " + testSubject.getCurrentWalkHolder().getWalkNumber());
    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);
        dialog.dismiss();
        if (result) {
            sensorRecorder.incrementWalkNumber();
            bacInput.setEnabled(true);
            bacInput.setText("");
        } else {
            //show file save error dialog
            AlertDialog.Builder alert = new AlertDialog.Builder(bacInput.getContext());
            alert.setTitle("Save Error");
            alert.setMessage("An error occurred while saving the data to file. Would you like to try saving again?");
            alert.setPositiveButton("YES", (dialogInterface, i) -> new SaveWalkHolderToCSVTask(sensorRecorder, mFolderName, bacInput).execute());
            alert.setNegativeButton("NO", null);
            alert.show();
        }
    }
}