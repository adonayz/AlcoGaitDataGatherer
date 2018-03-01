package edu.wpi.alcogaitdatagatherer.ui.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.box.androidsdk.content.BoxApiFile;
import com.box.androidsdk.content.BoxApiFolder;
import com.box.androidsdk.content.BoxConfig;
import com.box.androidsdk.content.auth.BoxAuthentication;
import com.box.androidsdk.content.models.BoxSession;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

import edu.wpi.alcogaitdatagatherer.R;
import edu.wpi.alcogaitdatagatherer.models.Gender;
import edu.wpi.alcogaitdatagatherer.models.TestSubject;
import edu.wpi.alcogaitdatagatherer.ui.adapters.SurveyListAdapter;

public class HomeActivity extends AppCompatActivity implements BoxAuthentication.AuthListener{

    private SurveyListAdapter surveyListAdapter;
    private ListView surveyListView;
    private LinkedList<File> surveyFiles;
    public static final String FILE_SHOULD_START_WITH = "ID_";
    //static final String FILE_SHOULD_END_WITH = ".csv";
    private static final int READ_WRITE_PERMISSION_CODE = 1000;
    private static final int RESUME = 1;
    private static final int DELETE = 2;

    private static final String CLIENT_ID = "jqkqfexx2sdtk8fd145dwfexr851drh3";
    private static final String CLIENT_SECRET = "NjaaG4NrOjCFFpvRn2gSFr5YtEuiReCl";
    private static final String REDIRECT_URI = "https://localhost";

    private BoxSession boxSession;
    private BoxSession oldBoxSession;

    private BoxApiFolder mFolderApi;
    private BoxApiFile mFileApi;

    private FloatingActionButton uab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        surveyListView = (ListView) findViewById(R.id.surveyList);

        surveyFiles = new LinkedList<>();

        readFiles();

        surveyListAdapter = new SurveyListAdapter(this, surveyFiles, surveyListView);

        surveyListView.setAdapter(surveyListAdapter);

        registerForContextMenu(surveyListView);

        requestPermissions();

        if (isBoxPreferenceEnabled()) {
            configureBoxClient();
            initializeBoxSession();
        }

        FloatingActionButton aab = (FloatingActionButton) findViewById(R.id.aab);
        aab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this, SurveyFormActivity.class);
                startActivity(intent);
            }
        });

        uab = (FloatingActionButton) findViewById(R.id.uab);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.list_context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.resume_gathering:
                listContextSelectDialog(RESUME, info.position);
                return true;
            case R.id.delete_gathering:
                listContextSelectDialog(DELETE, info.position);
                readFiles();
                surveyListAdapter.notifyDataSetChanged();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    public void requestPermissions(){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        READ_WRITE_PERMISSION_CODE);
            }
        }

    }



    public void readFiles(){
        String baseDir = android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/AlcoGaitDataGatherer/";

        File alcoGaitDirectory = new File(baseDir);
        alcoGaitDirectory.mkdir();

        File[] allFilesFromDir = alcoGaitDirectory.listFiles();

        surveyFiles.clear();

        try{
            for(File file: allFilesFromDir){
                String fileName = file.getName();
                if (fileName.length() == 6 && file.isDirectory() && fileName.startsWith(FILE_SHOULD_START_WITH)) {
                    surveyFiles.add(file);
                }
                /*if(fileName.length() > 7){
                    if(fileName.substring(0, FILE_SHOULD_START_WITH.length()).equals(FILE_SHOULD_START_WITH)
                            && fileName.substring(fileName.length() - FILE_SHOULD_END_WITH.length(), fileName.length()).equals(FILE_SHOULD_END_WITH)){
                        surveyFiles.add(file);
                    }
                }*/
            }
        }catch (NullPointerException e){
            e.printStackTrace();
        }

        Collections.sort(surveyFiles, new Comparator<File>() {
            @Override
            public int compare(File f1, File f2) {
                return Long.valueOf(f2.lastModified()).compareTo(f1.lastModified());
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case READ_WRITE_PERMISSION_CODE : {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    readFiles();
                    surveyListAdapter.notifyDataSetChanged();

                } else {
                    // permission denied by user. disable
                }
                return;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent i = new Intent(this, SettingsActivity.class);
            startActivity(i);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void configureBoxClient(){
        BoxConfig.CLIENT_ID = CLIENT_ID;
        BoxConfig.CLIENT_SECRET = CLIENT_SECRET;
        BoxConfig.REDIRECT_URL = REDIRECT_URI;
    }

    private void initializeBoxSession(){
        boxSession = new BoxSession(this);
        boxSession.setSessionAuthListener(this);
        boxSession.authenticate(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == 0){

        }
    }

    @Override
    public void onRefreshed(BoxAuthentication.BoxAuthenticationInfo info) {
    }

    @Override
    public void onAuthCreated(BoxAuthentication.BoxAuthenticationInfo info) {
        mFolderApi = new BoxApiFolder(boxSession);
        mFileApi = new BoxApiFile(boxSession);
        //loadRootFolder();
    }

    @Override
    public void onAuthFailure(BoxAuthentication.BoxAuthenticationInfo info, Exception ex) {
        if (ex != null) {
            //clearAdapter();
        } else if (info == null && oldBoxSession != null) {
            boxSession = oldBoxSession;
            boxSession.setSessionAuthListener(this);
            oldBoxSession = null;
            onAuthCreated(boxSession.getAuthInfo());
        }
    }

    @Override
    public void onLoggedOut(BoxAuthentication.BoxAuthenticationInfo info, Exception ex) {
        //clearAdapter();
        initializeBoxSession();
    }

    private void showToast(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(HomeActivity.this, text, Toast.LENGTH_LONG).show();
            }
        });
    }

    public boolean isBoxPreferenceEnabled() {
        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        return SP.getBoolean(getString(R.string.box_integration_preference), false);
    }

    @Override
    public void onResume(){
        super.onResume();
        readFiles();
        if (isBoxPreferenceEnabled()) {
            uab.setVisibility(View.VISIBLE);
            uab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    surveyListAdapter.syncWithBox(mFileApi);
                }
            });
        } else {
            uab.setVisibility(View.GONE);
        }
        surveyListAdapter.notifyDataSetChanged();
    }

    public void listContextSelectDialog(int selection, int position) {
        File currentFile = surveyFiles.get(position);
        String subjectID = currentFile.getName().substring(HomeActivity.FILE_SHOULD_START_WITH.length(),
                currentFile.getName().length()).trim();
        subjectID.trim();

        DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    if (selection == RESUME) {
                        if (currentFile.isDirectory()) {
                            File reportFile = null;
                            int maxWalks = 0;

                            for (File child : currentFile.listFiles()) {
                                if (child.getName().equals("report.txt")) {
                                    reportFile = child;
                                }
                                if (child.getName().startsWith("walk_")) {
                                    int walkNumber = Integer.parseInt(child.getName().substring(5, child.getName().length()).trim());
                                    if (walkNumber > maxWalks) {
                                        maxWalks = walkNumber;
                                    }
                                }
                            }
                            if (reportFile == null) {
                                showContextErrorDialog(RESUME);
                                return;
                            }
                            TestSubject testSubject = extractTestSubjectInfo(reportFile, subjectID);
                            testSubject.setStartingWalkNumber(maxWalks + 1);

                            Intent intent = new Intent(HomeActivity.this, DataGatheringActivity.class);
                            intent.putExtra("test_subject", testSubject);
                            startActivity(intent);
                        }
                    } else if (selection == DELETE) {
                        if (!deleteDirectory(currentFile)) {
                            showContextErrorDialog(DELETE);
                        }
                        this.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(currentFile)));
                        readFiles();
                        surveyListAdapter.notifyDataSetChanged();
                    }

                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    break;
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String title = "";
        String message = "Are you sure that you want to ";

        if (selection == RESUME) {
            title += "Resume Data Gathering";
            message += "resume gathering data for Subject ID " + subjectID + "?";
        } else if (selection == DELETE) {
            title += "Delete Gathered Data";
            message += "delete all the data gathered from Subject ID " + subjectID + "?";
        }

        builder.setTitle(title);
        builder.setMessage(message).setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();

    }

    TestSubject extractTestSubjectInfo(File file, String subjectID) {
        Gender gender = null;
        int age = 0;
        double weight = 0;
        int inches = 0;
        int feet = 0;

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("Gender:")) {
                    gender = Gender.getEnum(line.substring(8, line.length()).trim());
                } else if (line.startsWith("Age:")) {
                    age = Integer.valueOf(line.substring(5, line.length()).trim());
                } else if (line.startsWith("Weight:")) {
                    weight = Double.valueOf(line.substring(8, line.length()).trim());
                } else if (line.startsWith("Height(ft and inches):")) {
                    String temp = line.substring(23, line.length()).trim();
                    for (int i = 1; i < temp.length(); i++) {
                        if (temp.charAt(i) == '\'') {
                            inches = Integer.valueOf(temp.substring(0, i).trim());
                            feet = Integer.valueOf(temp.substring(i + 2, temp.length() - 2).trim());
                            break;
                        }

                    }
                }
            }
            br.close();
        } catch (IOException e) {
            //You'll need to add proper error handling here
        }

        return new TestSubject(subjectID, gender, age, weight, inches, feet);
    }

    public void showContextErrorDialog(int selection) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        if (selection == RESUME) {
            alert.setTitle("Resume Error");
            alert.setMessage("Could not resume data gathering for this Subject ID.");
        } else if (selection == DELETE) {
            alert.setTitle("Delete Error");
            alert.setMessage("Could not delete this data set. Delete it manually.");
        }
        alert.setPositiveButton("OK", null);
        alert.show();
    }

    public boolean deleteDirectory(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            if (files == null) {
                return true;
            }
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    deleteDirectory(files[i]);
                } else {
                    files[i].delete();
                }
                this.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(files[i])));
            }
        }

        return (path.delete());
    }

}
