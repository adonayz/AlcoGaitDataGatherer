package edu.wpi.alcogaitdatagatherer.ui.activities;

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

import edu.wpi.alcogaitdatagatherer.models.Gender;
import edu.wpi.alcogaitdatagatherer.R;
import edu.wpi.alcogaitdatagatherer.models.TestSubject;
import edu.wpi.alcogaitdatagatherer.ui.adapters.SurveyListAdapter;

public class SurveyFormActivity extends AppCompatActivity{

    private boolean isIDUnavailable;
    private boolean allowSubmission = false;
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

        subjectIDInput = findViewById(R.id.subjectIDInput);
        maleRadioButton = findViewById(R.id.maleRadio);
        femaleRadioButton = findViewById(R.id.femaleRadio);
        ageInput = findViewById(R.id.ageInput);
        weightInput = findViewById(R.id.weightInput);
        heightFeetInput = findViewById(R.id.heightFeetInput);
        heightInchesInput = findViewById(R.id.heightInchesInput);

        subjectIDInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (!editable.toString().trim().isEmpty()) {
                    if (isIDUnavailable = SurveyListAdapter.getSavedIDs().contains(editable.toString().trim().replaceFirst("^0+(?!$)", ""))) {
                        subjectIDInput.setError("This ID already exists. Please enter a valid ID.");
                    } else if (Integer.parseInt(editable.toString().trim()) < 101 || Integer.parseInt(editable.toString().trim()) > 996) {
                        subjectIDInput.setError("Subject ID has to be between 101 and 996");
                    } else {
                        subjectIDInput.setError(null);
                    }
                }
            }
        });

        ageInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (!editable.toString().trim().isEmpty()) {
                    if (Integer.parseInt(editable.toString().trim()) < 21 || Integer.parseInt(editable.toString().trim()) > 65) {
                        ageInput.setError("Invalid. 21 Minimum. 65 Maximum.");
                    } else {
                        ageInput.setError(null);
                    }
                }
            }
        });

        weightInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String weightString = editable.toString().trim();
                if (!weightString.isEmpty()) {
                    if (weightString.startsWith(".")) {
                        weightString = "0" + weightString;
                    }
                    if (Double.parseDouble(weightString) < 85 || Double.parseDouble(weightString) > 230) {
                        weightInput.setError("Invalid. 85 Minimum. 230 Maximum.");
                    } else {
                        weightInput.setError(null);
                    }
                }
            }
        });

        heightFeetInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (!editable.toString().trim().isEmpty()) {
                    if (Integer.parseInt(editable.toString().trim()) < 4 || Integer.parseInt(editable.toString().trim()) > 7) {
                        heightFeetInput.setError("Invalid. 4 Feet Minimum. 7 Feet Maximum.");
                    } else {
                        heightFeetInput.setError(null);
                    }
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
                EditText[] editTexts = {subjectIDInput, ageInput, weightInput, heightFeetInput, heightInchesInput};
                for(EditText anEditText: editTexts){
                    if(anEditText.getText().toString().trim().isEmpty()){
                        anEditText.setError("Please enter data");
                    }
                }

                if (subjectIDInput.getError() == null && ageInput.getError() == null && weightInput.getError() == null && heightFeetInput.getError() == null
                        && heightInchesInput.getError() == null && (maleRadioButton.isChecked() || femaleRadioButton.isChecked())) {
                    String subjectID = String.format(Locale.US, "%03d", Integer.parseInt(subjectIDInput.getText().toString().trim())).trim();
                    String age = ageInput.getText().toString().trim();
                    String weight = weightInput.getText().toString().trim();
                    if (weight.startsWith(".")) {
                        weight = "0" + weight;
                    }
                    String heightFeet = heightFeetInput.getText().toString().trim();
                    String heightInches = heightInchesInput.getText().toString().trim();

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
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
