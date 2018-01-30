package edu.wpi.alcogaitdatagatherer.models;

/**
 * Created by Adonay on 9/27/2017.
 */

public enum Gender {
    MALE("Male"),
    FEMALE("Female");

    String genderString;

    Gender(String genderString){
        this.genderString = genderString;
    }

    @Override
    public String toString(){
        return genderString;
    }
}
