package edu.wpi.alcogaitdatagatherer;

import java.io.Serializable;
import java.util.LinkedList;

/**
 * Created by Adonay on 9/29/2017.
 */

public class Walk implements Serializable {

    private int walkNumber;
    private double BAC;
    private LinkedList<String[]> phoneAccelerometerDataList;
    private LinkedList<String[]> phoneGyroscopeDataList;
    private LinkedList<String[]> watchAccelerometerDataList;
    private LinkedList<String[]> watchGyroscopeDataList;
    private LinkedList<String[]> heartRateDataList;
    private LinkedList<String[]> compassDataList;
    private int samplesCollected;

    public Walk(int walkNumber, double BAC) {
        this.walkNumber = walkNumber;
        this.BAC = BAC;
        phoneAccelerometerDataList = new LinkedList<>();
        phoneGyroscopeDataList = new LinkedList<>();
        watchAccelerometerDataList = new LinkedList<>();
        watchGyroscopeDataList = new LinkedList<>();
        heartRateDataList = new LinkedList<>();
        compassDataList = new LinkedList<>();
    }

    public int getWalkNumber() {
        return walkNumber;
    }

    public double getBAC() {
        return BAC;
    }

    public void addPhoneAccelerometerData(String[] sensorData) {
        this.phoneAccelerometerDataList.add(sensorData);
    }

    public void addPhoneGyroscopeData(String[] sensorData) {
        this.phoneGyroscopeDataList.add(sensorData);
    }

    public void addWatchAccelerometerData(String[] sensorData) {
        this.watchAccelerometerDataList.add(sensorData);
    }

    public void addWatchGyroscopeData(String[] sensorData) {
        this.watchGyroscopeDataList.add(sensorData);
    }

    public void addHeartRateData(String[] sensorData) {
        this.heartRateDataList.add(sensorData);
    }

    public void addCompasData(String[] compassData) {
        this.compassDataList.add(compassData);
    }

    public int getSampleSize(){
        return phoneAccelerometerDataList.size() + phoneGyroscopeDataList.size() + compassDataList.size() + watchAccelerometerDataList.size() + watchGyroscopeDataList.size() + heartRateDataList.size();
    }

    public LinkedList<String[]> toCSVFormat(){
        final String[] SPACE = {""};
        final String[] PHONE_ACCELEROMETER_TITLE = {"ACCELEROMETER DATA (PHONE)"};
        final String[] PHONE_GYROSCOPE_TITLE = {"GYROSCOPE DATA (PHONE)"};
        final String[] WATCH_ACCELEROMETER_TITLE = {"ACCELEROMETER DATA (WATCH)"};
        final String[] WATCH_GYROSCOPE_TITLE = {"GYROSCOPE DATA (WATCH)"};
        final String[] COMPASS_TITLE = {"COMPASS (PHONE)"};
        final String[] HEART_RATE_TITLE = {"HEART RATE DATA (WATCH)"};

        final String[] A_G_TABLE_HEADER = {"Sensor Name", "X", "Y", "Z", "Timestamp"};
        final String[] HEART_RATE_TABLE_HEADER = {"Sensor Name", "Rate (BPM)", "Timestamp"};
        final String[] COMPASS_TABLE_HEADER = {"Derived Data", "Azimuth", "Pitch", "Roll", "Timestamp"};

        LinkedList<String[]> csvFormat = new LinkedList<>();

        String[] walkInformation = {"Walk Number " + walkNumber, "BAC = " + BAC};

        csvFormat.add(walkInformation);
        csvFormat.add(SPACE);
        csvFormat.add(PHONE_ACCELEROMETER_TITLE);
        csvFormat.add(A_G_TABLE_HEADER);
        csvFormat.addAll(phoneAccelerometerDataList);
        csvFormat.add(SPACE);
        csvFormat.add(PHONE_GYROSCOPE_TITLE);
        csvFormat.add(A_G_TABLE_HEADER);
        csvFormat.addAll(phoneGyroscopeDataList);
        csvFormat.add(SPACE);
        csvFormat.add(COMPASS_TITLE);
        csvFormat.add(COMPASS_TABLE_HEADER);
        csvFormat.addAll(compassDataList);
        csvFormat.add(SPACE);
        csvFormat.add(WATCH_ACCELEROMETER_TITLE);
        csvFormat.add(A_G_TABLE_HEADER);
        csvFormat.addAll(watchAccelerometerDataList);
        csvFormat.add(SPACE);
        csvFormat.add(WATCH_GYROSCOPE_TITLE);
        csvFormat.add(A_G_TABLE_HEADER);
        csvFormat.addAll(watchGyroscopeDataList);
        csvFormat.add(SPACE);
        csvFormat.add(HEART_RATE_TITLE);
        csvFormat.add(HEART_RATE_TABLE_HEADER);
        csvFormat.addAll(heartRateDataList);

        return csvFormat;
    }


}
