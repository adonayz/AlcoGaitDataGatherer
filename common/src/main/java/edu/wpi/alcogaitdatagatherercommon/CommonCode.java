package edu.wpi.alcogaitdatagatherercommon;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Created by Adonay on 9/27/2017.
 */

public class CommonCode {
    public static final String WEAR_DISCOVERY_NAME = "record_gait_data";
    public static final int DELAY_IN_MILLISECONDS = 5;
    public static final int RECORD_TIME_IN_SECONDS = 60;
    public static final String START_RECORDING_PATH = "/start_recording";
    public static final String STOP_RECORDING_PATH = "/stop_recording";
    public static final String WEAR_MESSAGE_PATH = "/message";
    public static final String WEAR_HOME_ACTIVITY_PATH = "/start/WearHomeActivity";
    public static final String REDO_PREVIOUS_WALK_PATH = "/redo_previous_walk";
    public static final String STOP_RECORDING = "stop_recording";
    public static final String SAVE_WALKS = "save_walks";
    public static final String SAVE_WALKS_ACK = "save_walks_acknowledgement";
    public static final String RESTART = "restart_survey";
    public static final String CHECK_IF_APP_OPEN = "check_if_app_open";
    public static final String APP_OPEN_ACK = "acknowledgement";
    public static final String RESTART_ACK = "restart_acknowledgement";
    public static final String REDO_PREVIOUS_WALK_ACK = "redo_walk_acknowledgement";
    public static final String WEARABLE_DISCONNECTED = "wearable_disconnected";
    public static final String WEAR_CSV_FILE_CHANNEL_PATH = "/channel_for_wear_csv_file";
    public static final String START_ACK = "start_acknowledgement";
    public static final String STOP_ACK = "stop_acknowledgement";
    public static final String OPEN_APP = "open_app";
    public static final String REQUEST_WEARABLE_DATA_SAMPLE_SIZE = "request_sample_size";
    public static final long TRANSFER_FINISHED_LONG = 4145646541563468518L;
    public static final String TRANSFER_FINISHED_STRING = "finished";
    public static final String REFRESH_CONNECTION = "refresh_connection";
    public static final String WATCH_FILE_ACK = "watch_file_ack";


    public static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.SSS", Locale.US);

    public static String[] generatePrintableSensorData(String sensorName, float[] values, int accuracy, long timestamp) {
        int i = 0;

        String[] result = new String[values.length + 3];
        result[i++] = sensorName;
        for (; i <= values.length; i++) {
            result[i] = String.valueOf(values[i - 1]);
        }

        result[i++] = String.valueOf(accuracy);

        //result[i] = simpleDateFormat.format(new Date(getCurrentTimeFromSensor(timestamp)));
        result[i] = "";

        return result;
    }

    /*private static long getCurrentTimeFromSensor(long sensorTimestamp) {
        return ((((new Date()).getTime() * 1000000L) - System.nanoTime()) + sensorTimestamp) / 1000000L;
    }*/
}
