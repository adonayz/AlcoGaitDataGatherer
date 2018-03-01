package edu.wpi.alcogaitdatagatherer.tasks;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.media.MediaScannerConnection;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.EditText;

import com.opencsv.CSVWriter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;

import edu.wpi.alcogaitdatagatherer.models.SensorRecorder;
import edu.wpi.alcogaitdatagatherer.models.TestSubject;
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
    private String rootFolder;

    public SaveWalkHolderToCSVTask(SensorRecorder sensorRecorder, String mFolderName, EditText bacInput) {
        this.sensorRecorder = sensorRecorder;
        this.mFolderName = mFolderName;
        this.rootFolder = mFolderName;
        this.testSubject = sensorRecorder.getTestSubject();
        this.dialog = new ProgressDialog(bacInput.getContext());
        this.bacInput = bacInput;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mFolderName = mFolderName + File.separator + "phone";
        File phoneRoot = new File(mFolderName);
        phoneRoot.mkdirs();
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
        try {
            /*String testSubjectTitle[] = {"Subject ID", "Gender", "Age", "Weight", "Height(ft and inches)"};

            String testSubjectInformation[] = {testSubject.getSubjectID(), testSubject.getGender().toString(),
                    String.valueOf(testSubject.getAge()), String.valueOf(testSubject.getWeight()) + " lbs",
                    String.valueOf(testSubject.getHeightFeet()) + "' " + String.valueOf(testSubject.getHeightInches()) + "''"};

            writer.writeNext(testSubjectTitle);
            writer.writeNext(testSubjectInformation);
            writer.writeNext(space);*/

            //saveBacAsFile();

            double percentProgress = 0;
            int max = testSubject.getCurrentWalkHolder().getSampleSize();
            for (WalkType walkType : WalkType.values()) {
                if (testSubject.getCurrentWalkHolder().hasWalk(walkType)) {
                    String walkTypeFolderName = mFolderName + File.separator + walkType.toNoSpaceString();
                    File walkTypeRoot = new File(walkTypeFolderName);
                    walkTypeRoot.mkdirs();
                    LinkedList<LinkedList<String[]>> CSVFormat = testSubject.getCurrentWalkHolder().get(walkType).toCSVFormat();
                    File file;
                    for (int i = 0; i < CSVFormat.size(); i++) {
                        String fileName = walkTypeFolderName + File.separator;
                        if (i == 0) {
                            fileName = fileName + "accelerometer.csv";
                        } else if (i == 1) {
                            fileName = fileName + "gyroscope.csv";
                        } else if (i == 2) {
                            fileName = fileName + "compass.csv";
                        }
                        file = new File(fileName);

                        CSVWriter writer;
                        FileWriter mFileWriter;
                        if (file.exists() && !file.isDirectory()) {
                            mFileWriter = new FileWriter(fileName, false);
                        } else {
                            mFileWriter = new FileWriter(fileName);
                        }

                        writer = new CSVWriter(mFileWriter);

                        writer.writeAll(CSVFormat.get(i));

                        /*if (((++savedSamples/max) * 100) > percentProgress) {
                            percentProgress = (savedSamples / max) * 100;
                            publishProgress((int)percentProgress);
                        }*/
                        writer.close();
                        MediaScannerConnection.scanFile(bacInput.getContext(), new String[]{file.getAbsolutePath()}, null, null);
                    }

                }
            }

               /* String messageTitle[] = {"Report Message"};
                writer.writeNext(messageTitle);
                String reportMessage[] = {testSubject.getReportMessage()};
                writer.writeNext(reportMessage);*/
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

    private void saveBacAsFile() {
        final File bac_file = new File(rootFolder, "bac_info.txt");

        try {
            if (!bac_file.exists()) {
                bac_file.createNewFile();
            }
            FileWriter fileWriter = new FileWriter(bac_file, true);
            BufferedWriter bufferWriter = new BufferedWriter(fileWriter);

            bufferWriter.append("BAC = ");
            bufferWriter.append(String.valueOf(testSubject.getCurrentWalkHolder().get(WalkType.NORMAL).getBAC()));
            bufferWriter.append("\n");
            bufferWriter.close();

            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
        MediaScannerConnection.scanFile(bacInput.getContext(), new String[]{bac_file.getAbsolutePath()}, null, null);
    }
}