package edu.wpi.alcogaitdatagatherer;

import java.io.Serializable;
import java.util.LinkedList;

/**
 * Created by Adonay on 9/27/2017.
 */

public class TestSubject implements Serializable {
    private String subjectID;
    private Gender gender;
    private int age;
    private double weight;
    private int heightFeet;
    private int heightInches;
    private LinkedList<Walk> successfulWalks;
    private LinkedList<Walk> reportedWalks;
    private String reportMessage;

    public TestSubject(String subjectID, Gender gender, int age, double weight, int heightFeet, int heightInches) {
        this.subjectID = subjectID;
        this.gender = gender;
        this.age = age;
        this.weight = weight;
        this.heightFeet = heightFeet;
        this.heightInches = heightInches;
        successfulWalks = new LinkedList<>();
        reportedWalks = new LinkedList<>();
        reportMessage = "";
    }

    public String getSubjectID() {
        return subjectID;
    }

    public Gender getGender() {
        return gender;
    }

    public int getAge() {
        return age;
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
        successfulWalks.add(walk);
    }

    public LinkedList<Walk> getSuccessfulWalks() {
        return successfulWalks;
    }

    public void setSuccessfulWalks(LinkedList<Walk> successfulWalks) {
        this.successfulWalks = successfulWalks;
    }

    public LinkedList<Walk> getReportedWalks() {
        return reportedWalks;
    }

    public void setReportedWalks(LinkedList<Walk> reportedWalks) {
        this.reportedWalks = reportedWalks;
    }

    public String getReportMessage() {
        return reportMessage;
    }

    public void setReportMessage(String reportMessage) {
        this.reportMessage = reportMessage;
    }

    public void clearWalkData(){
        successfulWalks.clear();
    }

    public Walk removeLastWalk(){
        return successfulWalks.removeLast();
    }
}
