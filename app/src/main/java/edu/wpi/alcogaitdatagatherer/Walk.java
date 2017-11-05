package edu.wpi.alcogaitdatagatherer;

import java.util.LinkedList;

/**
 * Created by Adonay on 9/29/2017.
 */

public class Walk {

    private int walkNumber;
    private double BAC;
    private LinkedList<String[]> accelerometerDataList;
    private LinkedList<String[]> gyroscopeDataList;
    private LinkedList<String[]> heartRateDataList;

    public Walk(int walkNumber, double BAC) {
        this.walkNumber = walkNumber;
        this.BAC = BAC;
        accelerometerDataList = new LinkedList<>();
        gyroscopeDataList = new LinkedList<>();
        heartRateDataList = new LinkedList<>();
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

    public LinkedList<String[]> getAccelerometerDataList() {
        return accelerometerDataList;
    }

    public LinkedList<String[]> getGyroscopeDataList() {
        return gyroscopeDataList;
    }

    public LinkedList<String[]> getHeartRateDataList() {
        return heartRateDataList;
    }

    public void addAccelerometerData(String[] sensorData) {
        this.accelerometerDataList.add(sensorData);
    }

    public void addGyroscopeData(String[] sensorData) {
        this.gyroscopeDataList.add(sensorData);
    }

    public void addHeartRateData(String[] heartRateData){
        this.heartRateDataList.add(heartRateData);
    }

    public int getSampleSize(){
        return accelerometerDataList.size() + gyroscopeDataList.size() + heartRateDataList.size();
    }

    public LinkedList<String[]> toCSVFormat(){
        final String[] SPACE = {""};
        final String[] ACCELEROMETER_TITLE = {"ACCELEROMETER DATA"};
        final String[] GYROSCOPE_TITLE = {"GYROSCOPE DATA"};
        final String[] HEART_RATE_TITLE = {"HEART RATE DATA"};

        String[] TABLE_HEADER_1 = {"Sensor Name", "X", "Y", "Z", "Timestamp"};
        String[] TABLE_HEADER_2 = {"Sensor Name", "Beats Per Minute", "Timestamp"};

        LinkedList<String[]> csvFormat = new LinkedList<>();

        String[] walkInformation = {"Walk Number " + walkNumber, "BAC = " + BAC};

        csvFormat.add(walkInformation);
        csvFormat.add(SPACE);
        csvFormat.add(ACCELEROMETER_TITLE);
        csvFormat.add(TABLE_HEADER_1);
        csvFormat.addAll(accelerometerDataList);
        csvFormat.add(SPACE);
        csvFormat.add(GYROSCOPE_TITLE);
        csvFormat.add(TABLE_HEADER_1);
        csvFormat.addAll(gyroscopeDataList);
        csvFormat.add(SPACE);
        csvFormat.add(HEART_RATE_TITLE);
        csvFormat.add(TABLE_HEADER_2);
        csvFormat.addAll(heartRateDataList);

        return csvFormat;
    }


}
