package edu.wpi.alcogaitdatagatherer;

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
import android.widget.EditText;
import android.widget.RadioButton;

import java.util.Locale;

public class SurveyFormActivity extends AppCompatActivity{

    private boolean isIDUnavailable;
    private EditText subjectIDInput;
    private RadioButton maleRadioButton;
    private RadioButton femaleRadioButton;
    private EditText ageInput;
    private EditText weightInput;
    private EditText heightFeetInput;
    private EditText heightInchesInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_survey_form);

        Toolbar toolbar = (Toolbar) findViewById(R.id.survey_toolbar);
        toolbar.setTitle("Add Test Subject");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        isIDUnavailable = false;

        subjectIDInput = (EditText) findViewById(R.id.subjectIDInput);
        maleRadioButton = (RadioButton) findViewById(R.id.maleRadio);
        femaleRadioButton = (RadioButton) findViewById(R.id.femaleRadio);
        ageInput = (EditText) findViewById(R.id.ageInput);
        weightInput = (EditText) findViewById(R.id.weightInput);
        heightFeetInput = (EditText) findViewById(R.id.heightFeetInput);
        heightInchesInput = (EditText) findViewById(R.id.heightInchesInput);

        subjectIDInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (isIDUnavailable = SurveyListAdapter.getSavedIDs().contains(editable.toString().trim().replaceFirst("^0+(?!$)", ""))) {
                    subjectIDInput.setError("This ID already exists. Please enter a valid ID.");
                }else{
                    subjectIDInput.setError(null);
                }
            }
        });

        heightInchesInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (!editable.toString().trim().isEmpty()) {
                    if (Integer.parseInt(editable.toString().trim()) > 11) {
                        heightInchesInput.setError("Invalid. 11 Inches Maximum");
                    } else {
                        heightInchesInput.setError(null);
                    }
                }
            }
        });


        AppCompatTextView submitButton = (AppCompatTextView) findViewById(R.id.submitButton);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String subjectID = String.format(Locale.US, "%04d", Integer.parseInt(subjectIDInput.getText().toString().trim())).trim();
                String age = ageInput.getText().toString().trim();
                String weight = weightInput.getText().toString().trim();
                String heightFeet = heightFeetInput.getText().toString().trim();
                String heightInches = heightInchesInput.getText().toString().trim();

                EditText[] editTexts = {subjectIDInput, ageInput, weightInput, heightFeetInput, heightInchesInput};
                for(EditText anEditText: editTexts){
                    if(anEditText.getText().toString().trim().isEmpty()){
                        anEditText.setError("Please enter data");
                    }
                }

                if(!subjectID.isEmpty() && !age.isEmpty() && !weight.isEmpty() && !heightFeet.isEmpty() &&
                        !heightInches.isEmpty() && (maleRadioButton.isChecked() || femaleRadioButton.isChecked()) && !isIDUnavailable){

                    TestSubject testSubject;
                    Gender gender;

                    if(maleRadioButton.isChecked()){
                        gender = Gender.MALE;
                    }else{
                        gender = Gender.FEMALE;
                    }

                    testSubject = new TestSubject(subjectID, gender, Integer.valueOf(age), Double.valueOf(weight), Integer.valueOf(heightFeet), Integer.valueOf(heightInches));

                    Intent intent = new Intent(SurveyFormActivity.this, DataGatheringActivity.class);
                    intent.putExtra("test_subject", testSubject);
                    startActivity(intent);
                }
            }
        });

        ageInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                getWindow().setSoftInputMode(
                        WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
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
