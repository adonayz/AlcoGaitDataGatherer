package edu.wpi.alcogaitdatagatherer;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import java.io.File;
import java.util.LinkedList;

public class HomeActivity extends AppCompatActivity {

    private ListView surveyListView;
    private LinkedList<File> surveyFiles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        surveyListView = (ListView) findViewById(R.id.surveyList);

        String baseDir = android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/AlcoGaitDataGatherer/";

        File alcoGaitDirectory = new File(baseDir);
        alcoGaitDirectory.mkdir();

        File[] allFilesFromDir = alcoGaitDirectory.listFiles();

        String fileShouldStartWith = "ID_";
        String fileShouldEndWith = ".csv";

        surveyFiles = new LinkedList<>();

        for(File file: allFilesFromDir){
            String fileName = file.getName();
            if(fileName.length() > 7){
                if(fileName.substring(0, fileShouldStartWith.length()).equals(fileShouldStartWith)
                        && fileName.substring(fileName.length() - fileShouldEndWith.length(), fileName.length()).equals(fileShouldEndWith)){
                    surveyFiles.add(file);
                }
            }
        }

        SurveyListAdapter surveyListAdapter = new SurveyListAdapter(this, surveyFiles);

        surveyListView.setAdapter(surveyListAdapter);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this, SurveyFormActivity.class);
                startActivity(intent);
            }
        });
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
