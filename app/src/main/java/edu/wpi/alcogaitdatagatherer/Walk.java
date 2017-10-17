package edu.wpi.alcogaitdatagatherer;

import java.util.LinkedList;

/**
 * Created by Adonay on 9/29/2017.
 */

public class Walk {

    private int walkNumber;
    private double BAC;
    private LinkedList<String[]> sensorDataList;

    public Walk(int walkNumber, double BAC) {
        this.walkNumber = walkNumber;
        this.BAC = BAC;
        sensorDataList = new LinkedList<>();
    }

    public int getWalkNumber() {
        return walkNumber;
    }

    public void setWalkNumber(int walkNumber) {
        this.walkNumber = walkNumber;
    }

    public double getBAC() {
        return BAC;
    }

    public void setBAC(double BAC) {
        this.BAC = BAC;
    }

    public LinkedList<String[]> getSensorDataList() {
        return sensorDataList;
    }

    public void setSensorDataList(LinkedList<String[]> sensorDataList) {
        this.sensorDataList = sensorDataList;
    }

    public void addSensorData(String[] sensorData) {
        this.sensorDataList.add(sensorData);
    }

    public LinkedList<String[]> toCSVFormat(){
        final String[] space = {""};
        LinkedList<String[]> csvFormat = new LinkedList<>();

        String[] walkInformation = {"Walk Number " + walkNumber, "BAC = " + BAC};

        csvFormat.add(walkInformation);
        csvFormat.add(space);
        csvFormat.addAll(sensorDataList);

        return csvFormat;
    }
}
