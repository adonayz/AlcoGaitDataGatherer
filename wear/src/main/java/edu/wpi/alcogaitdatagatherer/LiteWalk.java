package edu.wpi.alcogaitdatagatherer;

import java.util.LinkedList;

import edu.wpi.alcogaitdatagatherercommon.WalkType;

import static android.hardware.Sensor.TYPE_ACCELEROMETER;
import static android.hardware.Sensor.TYPE_GYROSCOPE;
import static android.hardware.Sensor.TYPE_HEART_RATE;

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

    public void addSensorData(int sensorType, String[] sensorData) {
        if (sensorType == TYPE_HEART_RATE) {
            addHeartRateData(sensorData);
        } else if (sensorType == TYPE_ACCELEROMETER) {
            addWatchAccelerometerData(sensorData);
        } else if (sensorType == TYPE_GYROSCOPE) {
            addWatchGyroscopeData(sensorData);
        }
    }

    public void resetWalk() {
        watchAccelerometerDataList = new LinkedList<>();
        watchGyroscopeDataList = new LinkedList<>();
        heartRateDataList = new LinkedList<>();
    }

    public int getSampleSize() {
        return watchAccelerometerDataList.size() + watchGyroscopeDataList.size() + heartRateDataList.size();
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
