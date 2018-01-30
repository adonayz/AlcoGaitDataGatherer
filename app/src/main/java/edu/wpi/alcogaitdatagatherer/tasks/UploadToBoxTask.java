package edu.wpi.alcogaitdatagatherer.tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.box.androidsdk.content.BoxApiFile;
import com.box.androidsdk.content.BoxConstants;
import com.box.androidsdk.content.BoxException;
import com.box.androidsdk.content.listeners.ProgressListener;
import com.box.androidsdk.content.models.BoxEntity;
import com.box.androidsdk.content.models.BoxError;
import com.box.androidsdk.content.models.BoxFile;
import com.box.androidsdk.content.requests.BoxRequestsFile;

import java.io.File;
import java.net.HttpURLConnection;
import java.util.ArrayList;

import edu.wpi.alcogaitdatagatherer.ui.views.CustomSurveyView;

/**
 * Created by Adonay on 1/4/2018.
 */

public class UploadToBoxTask extends AsyncTask<Void, Integer, Void> {
    private CustomSurveyView customSurveyView;
    private File uploadFile;
    private BoxApiFile mFileApi;
    private static final String TAG = "UploadToBoxTask";

    public UploadToBoxTask(CustomSurveyView customSurveyView, File uploadFile, BoxApiFile mFileApi) {
        this.customSurveyView = customSurveyView;
        this.uploadFile = uploadFile;
        this.mFileApi = mFileApi;
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            final BoxRequestsFile.UploadFile request = mFileApi.getUploadRequest(uploadFile, BoxConstants.ROOT_FOLDER_ID);
            request.setProgressListener(new ProgressListener() {
                @Override
                public void onProgressChanged(long numBytes, long totalBytes) {
                    publishProgress((int) (100 * (numBytes / totalBytes)));
                }
            });
            final BoxFile uploadFileInfo = request.send();
            Log.d(TAG, "Uploaded " + uploadFileInfo.getName());
            //loadRootFolder();
        } catch (BoxException e) {
            BoxError error = e.getAsBoxError();
            if (error != null && error.getStatus() == HttpURLConnection.HTTP_CONFLICT) {
                ArrayList<BoxEntity> conflicts = error.getContextInfo().getConflicts();
                if (conflicts != null && conflicts.size() == 1 && conflicts.get(0) instanceof BoxFile) {
                    //uploadNewVersion((BoxFile) conflicts.get(0), position, adapter);
                    publishProgress(100);
                    return null;
                }
            }
            Log.d(TAG, "Upload failed");
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... integers) {
        customSurveyView.onProgressUpdate(integers[0]);
    }

    @Override
    protected void onPostExecute(Void param) {
        if (customSurveyView.getDonutProgress().getProgress() == 100) {
            customSurveyView.displayUploadComplete();
        } else {
            customSurveyView.displayUploadError();
        }
    }
}