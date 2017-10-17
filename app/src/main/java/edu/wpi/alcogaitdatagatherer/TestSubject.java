package edu.wpi.alcogaitdatagatherer;

import java.io.Serializable;
import java.util.LinkedList;

/**
 * Created by Adonay on 9/27/2017.
 */

public class TestSubject implements Serializable {
    private String subjectID;
    private Gender gender;
    private String birthDate;
    private double weight;
    private int heightFeet;
    private int heightInches;
    private LinkedList<Walk> allWalksFromSubject;

    public TestSubject(String subjectID, Gender gender, String birthDate, double weight, int heightFeet, int heightInches) {
        this.subjectID = subjectID;
        this.gender = gender;
        this.birthDate = birthDate;
        this.weight = weight;
        this.heightFeet = heightFeet;
        this.heightInches = heightInches;
        allWalksFromSubject = new LinkedList<>();
    }

    public String getSubjectID() {
        return subjectID;
    }

    public Gender getGender() {
        return gender;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public double getWeight() {
        return weight;
    }

    public int getHeightFeet() {
        return heightFeet;
    }

    public int getHeightInches() {
        return heightInches;
    }

    public void addWalk(Walk walk){
        allWalksFromSubject.add(walk);
    }

    public LinkedList<Walk> getAllWalksFromSubject() {
        return allWalksFromSubject;
    }

    public void clearWalkData(){
        allWalksFromSubject.clear();
    }

    public Walk removeLastWalk(){
        return allWalksFromSubject.removeLast();
    }
}
