package edu.wpi.alcogaitdatagatherer;

import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.box.androidsdk.content.BoxApiFile;
import com.box.androidsdk.content.BoxConstants;
import com.box.androidsdk.content.BoxException;
import com.box.androidsdk.content.listeners.ProgressListener;
import com.box.androidsdk.content.models.BoxEntity;
import com.box.androidsdk.content.models.BoxError;
import com.box.androidsdk.content.models.BoxFile;
import com.box.androidsdk.content.requests.BoxRequestsFile;
import com.github.lzyzsd.circleprogress.DonutProgress;

import java.io.File;
import java.net.HttpURLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Adonay on 9/26/2017.
 */

public class SurveyListAdapter extends BaseAdapter {

    private LayoutInflater inflater;
    private static LinkedList<File> files;
    private BoxApiFile mFileApi;
    private HomeActivity homeActivity;
    private ListView listView;

    public class ViewHolder {
        TextView fileIDTextView;
        TextView dateModifiedTextView;
        DonutProgress fileUploadProgress;
        CustomSurveyView customSurveyView;
    }

    public SurveyListAdapter(HomeActivity homeActivity, LinkedList<File> files, ListView listView) {
        this.homeActivity = homeActivity;
        inflater = LayoutInflater.from(homeActivity);
        this.files = files;
        this.listView = listView;
    }

    public int getCount() {
        return files.size();
    }

    public File getItem(int position) {
        return files.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        File file = files.get(position);
        ViewHolder holder = null;
        if(convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.survey_list_view, null);
            holder.customSurveyView = (CustomSurveyView) convertView.findViewById(R.id.customSurveyView);
            holder.fileUploadProgress = holder.customSurveyView.getDonutProgress();
            holder.fileIDTextView = holder.customSurveyView.getFileIDTextView();
            holder.dateModifiedTextView = holder.customSurveyView.getDateModifiedTextView();
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        String fileName = file.getName().substring(HomeActivity.FILE_SHOULD_START_WITH.length(), file.getName().length());

        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy   hh:mm a");
        String lastDateModified = sdf.format(file.lastModified());


        holder.fileIDTextView.setText("Subject ID " + fileName.trim());
        holder.dateModifiedTextView.setText(lastDateModified);

        return convertView;
    }

    public View getViewByPosition(int pos) {
        final int firstListItemPosition = listView.getFirstVisiblePosition();
        final int lastListItemPosition = firstListItemPosition + listView.getChildCount() - 1;

        if (pos < firstListItemPosition || pos > lastListItemPosition ) {
            return listView.getAdapter().getView(pos, null, listView);
        } else {
            final int childIndex = pos - firstListItemPosition;
            return listView.getChildAt(childIndex);
        }
    }

    public void syncWithBox(BoxApiFile mFileApi){
        this.mFileApi = mFileApi;

        ExecutorService executor = Executors.newFixedThreadPool(5);
        for (int i = 0; i < files.size(); i++) {
            ViewHolder holder = (ViewHolder) getViewByPosition(i).getTag();
            holder.fileUploadProgress.setVisibility(View.VISIBLE);
            new UploadToBoxTask(this, holder.customSurveyView, files.get(i)).executeOnExecutor(executor);
        }
        executor.shutdown();
    }

    private class UploadToBoxTask extends AsyncTask<Void, Integer, Void>{

        SurveyListAdapter surveyListAdapter;
        CustomSurveyView customSurveyView;
        File uploadFile;

        public UploadToBoxTask(SurveyListAdapter surveyListAdapter, CustomSurveyView customSurveyView, File uploadFile){
            this.surveyListAdapter = surveyListAdapter;
            this.customSurveyView = customSurveyView;
            this.uploadFile = uploadFile;
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                final BoxRequestsFile.UploadFile request = mFileApi.getUploadRequest(uploadFile, BoxConstants.ROOT_FOLDER_ID);
                request.setProgressListener(new ProgressListener() {
                    @Override
                    public void onProgressChanged(long numBytes, long totalBytes) {
                        publishProgress((int) (100 * (numBytes/totalBytes)));
                    }
                });
                final BoxFile uploadFileInfo = request.send();
                showToast("Uploaded " + uploadFileInfo.getName());
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
                showToast("Upload failed");
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... integers){
            customSurveyView.onProgressUpdate(integers[0]);
        }

        @Override
        protected void onPostExecute(Void param){
            if(customSurveyView.getDonutProgress().getProgress() == 100){
                customSurveyView.displayUploadComplete();
            }else{
                customSurveyView.displayUploadError();
            }
        }
    }

    public static LinkedList<String> getSavedIDs() {
        LinkedList<String> result = new LinkedList<>();
        for (File file : files) {
            String idOnly = file.getName().substring(HomeActivity.FILE_SHOULD_START_WITH.length(), file.getName().length());
            result.add(idOnly.replaceFirst("^0+(?!$)", ""));
        }
        return result;
    }

    private void showToast(final String text) {
        homeActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(homeActivity, text, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /*private Thread uploadSampleFile(final int position) {
        final File uploadFile = files.get(position);
        return new Thread() {
            @Override
            public void run() {
                try {

                    final BoxRequestsFile.UploadFile request = mFileApi.getUploadRequest(uploadFile, BoxConstants.ROOT_FOLDER_ID);
                    request.setProgressListener(new ProgressListener() {
                        @Override
                        public void onProgressChanged(long numBytes, long totalBytes) {
                            progress[position] = (int) (100 * (numBytes/totalBytes));
                        }
                    });
                    final BoxFile uploadFileInfo = request.send();
                    showToast("Uploaded " + uploadFileInfo.getName());
                    //loadRootFolder();
                } catch (BoxException e) {
                    BoxError error = e.getAsBoxError();
                    if (error != null && error.getStatus() == HttpURLConnection.HTTP_CONFLICT) {
                        ArrayList<BoxEntity> conflicts = error.getContextInfo().getConflicts();
                        if (conflicts != null && conflicts.size() == 1 && conflicts.get(0) instanceof BoxFile) {
                            //uploadNewVersion((BoxFile) conflicts.get(0), position, adapter);
                            progress[position] = 100;
                            return;
                        }
                    }
                    showToast("Upload failed");
                    e.printStackTrace();
                } finally {
                    // Show that download has finished
                }
            }
        };
    }

    private void uploadNewVersion(final BoxFile file, final int position) {
        final File uploadFile = files.get(position);
        new Thread() {
            @Override
            public void run() {
                try {
                    BoxRequestsFile.UploadNewVersion request = mFileApi.getUploadNewVersionRequest(uploadFile, file.getId());
                    request.setProgressListener(new ProgressListener() {
                        @Override
                        public void onProgressChanged(long numBytes, long totalBytes) {
                            progress[position] = (int) (100 * (numBytes/totalBytes));
                        }
                    });
                    final BoxFile uploadFileVersionInfo = request.send();
                    showToast("Uploaded new version of " + uploadFileVersionInfo.getName());
                } catch (BoxException e) {
                    e.printStackTrace();
                    showToast("Upload failed");
                } finally {
                    // Show that download has finished
                }
            }
        }.start();
    }*/
}