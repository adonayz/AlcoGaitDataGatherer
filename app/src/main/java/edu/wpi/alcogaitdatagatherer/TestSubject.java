package edu.wpi.alcogaitdatagatherer;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
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
    private WalkHolder currentWalkHolder;
    private LinkedList<Boolean> booleanWalksList; // changed from Walk Object to Integer in order to decrease memory usage
    private HashMap<Integer, Integer> sampleSizeMap;
    //private LinkedList<Walk> successfulWalks;
    //private LinkedList<Walk> reportedWalks;
    private String reportMessage;

    public TestSubject(String subjectID, Gender gender, int age, double weight, int heightFeet, int heightInches) {
        this.subjectID = subjectID;
        this.gender = gender;
        this.age = age;
        this.weight = weight;
        this.heightFeet = heightFeet;
        this.heightInches = heightInches;
        this.booleanWalksList = new LinkedList<>();
        this.sampleSizeMap = new HashMap<>();
        //successfulWalks = new LinkedList<>();
        //reportedWalks = new LinkedList<>();
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

    public String getReportMessage() {
        return reportMessage;
    }

    public void setReportMessage(String reportMessage) {
        this.reportMessage = reportMessage;
    }

    public LinkedList<Boolean> getBooleanWalksList() {
        return booleanWalksList;
    }

    public void setBooleanWalksList(LinkedList<Boolean> list) {
        booleanWalksList = list;
    }

    public HashMap<Integer, Integer> getSampleSizeMap() {
        return sampleSizeMap;
    }

    public WalkHolder getCurrentWalkHolder() {
        return currentWalkHolder;
    }

    public void setCurrentWalkHolder(WalkHolder walkHolder) {
        currentWalkHolder = walkHolder;
    }

    public void addNewWalkHolder(WalkHolder currentWalkHolder) {
        addSamplesSize(this.currentWalkHolder.getWalkNumber(), this.currentWalkHolder.getSampleSize());
        this.currentWalkHolder = currentWalkHolder;
        booleanWalksList.add(false);
    }

    public void replaceWalkHolder(WalkHolder currentWalkHolder) {
        sampleSizeMap.remove(currentWalkHolder.getWalkNumber());
        this.currentWalkHolder = currentWalkHolder;
    }

    public int getTotalSampleSize() {
        int total = 0;
        for (Iterator<Integer> it = sampleSizeMap.keySet().iterator(); it.hasNext(); ) {
            total += sampleSizeMap.get(it.next());
        }
        return total;
    }

    public void addSamplesSize(int walkNumber, int sampleSize) {
        sampleSizeMap.put(walkNumber, sampleSize);
    }

    // HUGE DESIGN (DATA STRUCTURE) CHANGES TO DECREASE MEMORY USAGE
    /*public void addWalk(Walk walk){
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

    public void clearWalkData(){
        successfulWalks.clear();
    }

    public Walk removeLastWalk(){
        return successfulWalks.removeLast();
    }*/
}
