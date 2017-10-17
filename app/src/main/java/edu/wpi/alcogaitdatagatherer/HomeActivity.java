package edu.wpi.alcogaitdatagatherer;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

public class HomeActivity extends AppCompatActivity {

    private SurveyListAdapter surveyListAdapter;
    private ListView surveyListView;
    private LinkedList<File> surveyFiles;
    static final String FILE_SHOULD_START_WITH = "ID_";
    static final String FILE_SHOULD_END_WITH = ".csv";
    private static final int READ_WRITE_PERMISSION_CODE = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        surveyListView = (ListView) findViewById(R.id.surveyList);

        surveyFiles = new LinkedList<>();

        readFiles();

        surveyListAdapter = new SurveyListAdapter(this, surveyFiles);

        surveyListView.setAdapter(surveyListAdapter);

        requestPermissions();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.aab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this, SurveyFormActivity.class);
                startActivity(intent);
            }
        });
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
                if(fileName.length() > 7){
                    if(fileName.substring(0, FILE_SHOULD_START_WITH.length()).equals(FILE_SHOULD_START_WITH)
                            && fileName.substring(fileName.length() - FILE_SHOULD_END_WITH.length(), fileName.length()).equals(FILE_SHOULD_END_WITH)){
                        surveyFiles.add(file);
                    }
                }
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
