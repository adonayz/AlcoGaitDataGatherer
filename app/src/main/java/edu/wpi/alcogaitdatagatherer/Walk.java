package edu.wpi.alcogaitdatagatherer;

import java.io.Serializable;
import java.util.LinkedList;

import edu.wpi.alcogaitdatagatherercommon.WalkType;

/**
 * Created by Adonay on 9/29/2017.
 */

public class Walk implements Serializable {
    private int walkNumber;
    private double BAC;
    private WalkType walkType;
    private LinkedList<String[]> phoneAccelerometerDataList;
    private LinkedList<String[]> phoneGyroscopeDataList;
    private LinkedList<String[]> compassDataList;
    private int watchSampleSize = 0;

    public Walk(int walkNumber, double BAC, WalkType walkType) {
        this.walkNumber = walkNumber;
        this.BAC = BAC;
        this.walkType = walkType;
        phoneAccelerometerDataList = new LinkedList<>();
        phoneGyroscopeDataList = new LinkedList<>();
        compassDataList = new LinkedList<>();
    }

    public WalkType getWalkType() {
        return walkType;
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

    public void addCompassData(String[] compassData) {
        this.compassDataList.add(compassData);
    }

    public int getSampleSize(){
        return phoneAccelerometerDataList.size() + phoneGyroscopeDataList.size() + compassDataList.size() + watchSampleSize;
    }

    public void addWatchSampleSize(int sampleSize) {
        this.watchSampleSize = sampleSize;
    }

    public LinkedList<String[]> toCSVFormat(){
        final String[] SPACE = {""};
        final String[] PHONE_ACCELEROMETER_TITLE = {"ACCELEROMETER DATA (PHONE)"};
        final String[] PHONE_GYROSCOPE_TITLE = {"GYROSCOPE DATA (PHONE)"};
        final String[] COMPASS_TITLE = {"COMPASS (PHONE)"};

        final String[] A_G_TABLE_HEADER = {"Sensor Name", "X", "Y", "Z", "Accuracy", "Timestamp"};
        final String[] COMPASS_TABLE_HEADER = {"Derived Data", "Azimuth", "Pitch", "Roll", "Accuracy", "Timestamp"};

        LinkedList<String[]> csvFormat = new LinkedList<>();

        String[] walkInformation = {"BAC = " + BAC};

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

        return csvFormat;
    }


}
