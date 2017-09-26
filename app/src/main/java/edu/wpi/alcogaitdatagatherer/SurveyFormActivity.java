package edu.wpi.alcogaitdatagatherer;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;

import java.util.Calendar;

public class SurveyFormActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_survey_form);

        Toolbar toolbar = (Toolbar) findViewById(R.id.survey_toolbar);
        toolbar.setTitle("Add Test Subject");
        setSupportActionBar(toolbar);

        final EditText dateInput = (EditText) findViewById(R.id.dateInput);

        dateInput.setKeyListener(null);

        final Calendar myCalendar = Calendar.getInstance();

        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                dateInput.setText(monthOfYear + "/" + dayOfMonth + "/" + year);
            }

        };

        dateInput.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                new DatePickerDialog(SurveyFormActivity.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        final EditText subjectIDInput = (EditText) findViewById(R.id.subjectIDInput);

        AppCompatTextView nextButton = (AppCompatTextView) findViewById(R.id.nextButton);

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String subjectID = subjectIDInput.getText().toString();
                if(!subjectID.trim().isEmpty()){
                    Intent intent = new Intent(SurveyFormActivity.this, DataGatheringActivity.class);
                    intent.putExtra("subject_id", subjectID);
                    startActivity(intent);
                }
            }
        });
    }

}