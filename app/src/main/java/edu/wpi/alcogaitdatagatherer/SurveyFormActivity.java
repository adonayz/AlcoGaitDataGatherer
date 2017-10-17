package edu.wpi.alcogaitdatagatherer;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;

import java.util.Calendar;

public class SurveyFormActivity extends AppCompatActivity {

    private boolean isIDUnavailable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_survey_form);

        Toolbar toolbar = (Toolbar) findViewById(R.id.survey_toolbar);
        toolbar.setTitle("Add Test Subject");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        isIDUnavailable = false;

        final EditText subjectIDInput = (EditText) findViewById(R.id.subjectIDInput);
        final RadioButton maleRadioButton = (RadioButton) findViewById(R.id.maleRadio);
        final RadioButton femaleRadioButton = (RadioButton) findViewById(R.id.femaleRadio);
        final EditText dateInput = (EditText) findViewById(R.id.dateInput);
        final EditText weightInput = (EditText) findViewById(R.id.weightInput);
        final EditText heightFeetInput = (EditText) findViewById(R.id.heightFeetInput);
        final EditText heightInchesInput = (EditText) findViewById(R.id.heightInchesInput);

        subjectIDInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(isIDUnavailable = SurveyListAdapter.getSavedIDs().contains(editable.toString().trim())){
                    subjectIDInput.setError("This ID already exists. Please enter a valid ID.");
                }else{
                    subjectIDInput.setError(null);
                }
            }
        });

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
                dateInput.setError(null);
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


        AppCompatTextView submitButton = (AppCompatTextView) findViewById(R.id.submitButton);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String subjectID = subjectIDInput.getText().toString().trim();
                String date = dateInput.getText().toString().trim();
                String weight = weightInput.getText().toString().trim();
                String heightFeet = heightFeetInput.getText().toString().trim();
                String heightInches = heightInchesInput.getText().toString().trim();

                EditText[] editTexts = {subjectIDInput, dateInput, weightInput, heightFeetInput, heightInchesInput};
                for(EditText anEditText: editTexts){
                    if(anEditText.getText().toString().trim().isEmpty()){
                        anEditText.setError("Please enter data");
                    }
                }

                if(!subjectID.isEmpty() && !date.isEmpty() && !weight.isEmpty() && !heightFeet.isEmpty() &&
                        !heightInches.isEmpty() && (maleRadioButton.isChecked() || femaleRadioButton.isChecked()) && !isIDUnavailable){

                    TestSubject testSubject;
                    Gender gender;

                    if(maleRadioButton.isChecked()){
                        gender = Gender.MALE;
                    }else{
                        gender = Gender.FEMALE;
                    }

                    testSubject = new TestSubject(subjectID, gender, date, Double.valueOf(weight), Integer.valueOf(heightFeet), Integer.valueOf(heightInches));

                    Intent intent = new Intent(SurveyFormActivity.this, DataGatheringActivity.class);
                    intent.putExtra("test_subject", testSubject);
                    startActivity(intent);
                }
            }
        });

        weightInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
               getWindow().setSoftInputMode(
                        WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
            }
        });

        heightFeetInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                getWindow().setSoftInputMode(
                        WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                onBackPressed();
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
