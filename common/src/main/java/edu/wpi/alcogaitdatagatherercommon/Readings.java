package edu.wpi.alcogaitdatagatherercommon;

import java.util.Date;

/**
 * Created by Adonay on 2/26/2018.
 */

public class Readings {
    private String[] accelerometer;
    private String[] gyroscope;
    private String[] compass;
    private String[] heartRate;

    public Readings() {
        accelerometer = null;
        gyroscope = null;
        compass = null;
        heartRate = null;
    }

    public String[] getAccelerometer() {
        return accelerometer;
    }

    public void setAccelerometer(String[] accelerometer) {
        this.accelerometer = accelerometer;
    }

    public String[] getGyroscope() {
        return gyroscope;
    }

    public void setGyroscope(String[] gyroscope) {
        this.gyroscope = gyroscope;
    }

    public String[] getCompass() {
        return compass;
    }

    public void setCompass(String[] compass) {
        this.compass = compass;
    }

    public boolean isPhoneReady() {
        return (accelerometer != null && gyroscope != null && compass != null);
    }

    public boolean isWatchReady() {
        return (accelerometer != null && gyroscope != null);
    }

    public String[] getHeartRate() {
        return heartRate;
    }

    public void setHeartRate(String[] heartRate) {
        this.heartRate = heartRate;
    }

    public void updateTime() {
        String[][] dataSet = {accelerometer, gyroscope, compass, heartRate};
        String currentTime = CommonCode.simpleDateFormat.format(new Date(System.currentTimeMillis()));
        for (String[] data : dataSet) {
            if (data != null) {
                data[data.length - 1] = currentTime;
            }
        }
    }
}
