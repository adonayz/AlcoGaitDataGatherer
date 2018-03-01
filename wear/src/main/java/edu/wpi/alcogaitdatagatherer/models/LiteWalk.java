package edu.wpi.alcogaitdatagatherer.models;

import java.util.LinkedList;

import edu.wpi.alcogaitdatagatherercommon.Readings;
import edu.wpi.alcogaitdatagatherercommon.WalkType;

/**
 * Created by Adonay on 12/31/2017.
 */

public class LiteWalk {
    private WalkType walkType;
    private LinkedList<String[]> watchAccelerometerDataList;
    private LinkedList<String[]> watchGyroscopeDataList;
    private LinkedList<String[]> heartRateDataList;

    public LiteWalk(WalkType walkType) {
        this.walkType = walkType;
        watchAccelerometerDataList = new LinkedList<>();
        watchGyroscopeDataList = new LinkedList<>();
        heartRateDataList = new LinkedList<>();
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

    public void addSensorData(Readings readings) {
        addWatchAccelerometerData(readings.getAccelerometer());
        addWatchGyroscopeData(readings.getGyroscope());
        if (readings.getHeartRate() != null) {
            addHeartRateData(readings.getHeartRate());
        }
    }

    public void resetWalk() {
        watchAccelerometerDataList = new LinkedList<>();
        watchGyroscopeDataList = new LinkedList<>();
        heartRateDataList = new LinkedList<>();
    }

    public int getSampleSize() {
        int result = 0;
        if (watchAccelerometerDataList != null) {
            result = result + watchAccelerometerDataList.size();
        }
        if (watchGyroscopeDataList != null) {
            result = result + watchGyroscopeDataList.size();
        }
        if (heartRateDataList != null) {
            result = result + heartRateDataList.size();
        }
        return result;
    }

    public WalkType getWalkType() {
        return walkType;
    }

    public LinkedList<String[]> toCSVFormat() {
        final String[] SPACE = {""};
        final String[] WATCH_ACCELEROMETER_TITLE = {"ACCELEROMETER DATA (WATCH)"};
        final String[] WATCH_GYROSCOPE_TITLE = {"GYROSCOPE DATA (WATCH)"};
        final String[] HEART_RATE_TITLE = {"HEART RATE DATA (WATCH)"};

        final String[] A_G_TABLE_HEADER = {"Sensor Name", "X", "Y", "Z", "Accuracy", "Timestamp"};
        final String[] HEART_RATE_TABLE_HEADER = {"Sensor Name", "Rate (BPM)", "Accuracy", "Timestamp"};

        LinkedList<String[]> csvFormat = new LinkedList<>();

        /*String[] walkInformation = {"Walk Number " + walkNumber, "BAC = " + BAC};

        csvFormat.add(walkInformation);
        csvFormat.add(SPACE);*/
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
