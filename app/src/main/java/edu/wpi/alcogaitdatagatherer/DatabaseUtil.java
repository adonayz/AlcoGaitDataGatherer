package edu.wpi.alcogaitdatagatherer;

import android.annotation.TargetApi;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;

/**
 * Created by Adonay on 11/21/2017.
 */

public class DatabaseUtil extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "AlcoGaitDataGathererDatabase";

    private static final String TEST_SUBJECT_TABLE_NAME = "TEST_SUBJECT";
    private static final String WALK_TABLE_NAME = "WALK";
    private static final String ACCELEROMETER_TABLE_NAME = "ACCELEROMETER";
    private static final String GYROSCOPE_TABLE_NAME = "GYROSCOPE";
    private static final String COMPASS_TABLE_NAME = "COMPASS";
    private static final String HEART_RATE_TABLE_NAME = "HEART_RATE";

    private static final String CREATE_TABLE = "CREATE TABLE ";

    private static final String TEST_SUBJECT_TABLE_FIELDS = " (SID, GENDER, AGE, WEIGHT, HEIGHT_FEET, HEIGHT_INCHES)";
    private static final String WALK_TABLE_FIELDS = " (WID, SID, WALK_NUMBER, WALK_TYPE, IS_SUCCESSFUL)";
    private static final String ACCELEROMETER_TABLE_FIELDS = " (AID, X, Y, Z, TIMESTAMP, WID, SOURCE_TYPE)";
    private static final String GYROSCOPE_TABLE_FIELDS = " (GID, X, Y, Z, TIMESTAMP, WID, SOURCE_TYPE)";
    private static final String COMPASS_TABLE_FIELDS = " (CID, AZIMUTH, PITCH, ROLL, TIMESTAMP, WID)";
    private static final String HEART_RATE_TABLE_FIELDS = " (HRID, BPM, TIMESTAMP, WID)";

    public DatabaseUtil(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TEST_SUBJECT_TABLE = CREATE_TABLE + TEST_SUBJECT_TABLE_NAME +
                " (SID INTEGER PRIMARY KEY AUTOINCREMENT, GENDER TEXT, AGE INTEGER, WEIGHT REAL, HEIGHT_FEET INTEGER," +
                " HEIGHT_INCHES INTEGER)";
        String CREATE_WALK_TABLE = CREATE_TABLE + WALK_TABLE_NAME +
                " (WID INTEGER PRIMARY KEY AUTOINCREMENT, SID INTEGER, WALK_NUMBER INTEGER, WALK_TYPE TEXT, IS_SUCCESSFUL TEXT)," +
                " CHECK (WALK_TYPE IN ('NORMAL', 'HEEL_TO_TOE', 'STANDING_ON_ONE_FOOT', 'NYSTAGMUS'))," +
                " CHECK (IS_SUCCESSFUL IN ('SUCCESSFUL', 'REPORTED'))," +
                " FOREIGN KEY (SID) REFERENCES TEST_SUBJECT(SID) ON DELETE CASCADE)";
        String CREATE_ACCELEROMETER_TABLE = CREATE_TABLE + ACCELEROMETER_TABLE_NAME +
                " (AID INTEGER PRIMARY KEY AUTOINCREMENT, X REAL, Y REAL, Z REAL, TIMESTAMP TEXT, WID INTEGER, SOURCE_TYPE TEXT," +
                " CHECK (SOURCE_TYPE IN ('WATCH', 'PHONE'))," +
                " FOREIGN KEY (WID) REFERENCES WALK(WID) ON DELETE CASCADE)";
        String CREATE_GYROSCOPE_TABLE = CREATE_TABLE + GYROSCOPE_TABLE_NAME +
                " (GID INTEGER PRIMARY KEY AUTOINCREMENT, X REAL, Y REAL, Z REAL, TIMESTAMP TEXT, WID INTEGER, SOURCE_TYPE TEXT," +
                " CHECK (SOURCE_TYPE IN ('WATCH', 'PHONE'))," +
                " FOREIGN KEY (WID) REFERENCES WALK(WID) ON DELETE CASCADE)";
        String CREATE_COMPASS_TABLE = CREATE_TABLE + COMPASS_TABLE_NAME +
                " (CID INTEGER PRIMARY KEY AUTOINCREMENT, AZIMUTH REAL, PITCH REAL, ROLL REAL, TIMESTAMP TEXT, WID INTEGER," +
                " FOREIGN KEY (WID) REFERENCES WALK(WID) ON DELETE CASCADE)";
        String CREATE_HEART_RATE_TABLE = CREATE_TABLE + HEART_RATE_TABLE_NAME +
                " (HRID INTEGER PRIMARY KEY AUTOINCREMENT, BPM INTEGER, TIMESTAMP TEXT, WID INTEGER," +
                " FOREIGN KEY (WID) REFERENCES WALK(WID) ON DELETE CASCADE)";

        db.execSQL(CREATE_TEST_SUBJECT_TABLE);
        db.execSQL(CREATE_WALK_TABLE);
        db.execSQL(CREATE_ACCELEROMETER_TABLE);
        db.execSQL(CREATE_GYROSCOPE_TABLE);
        db.execSQL(CREATE_COMPASS_TABLE);
        db.execSQL(CREATE_HEART_RATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + TEST_SUBJECT_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + WALK_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + ACCELEROMETER_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + GYROSCOPE_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + COMPASS_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + HEART_RATE_TABLE_NAME);
        onCreate(db);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (!db.isReadOnly()) {
            setForeignKeyConstraintsEnabled(db);
        }
    }

    private void setForeignKeyConstraintsEnabled(SQLiteDatabase db) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            setForeignKeyConstraintsEnabledPreJellyBean(db);
        } else {
            setForeignKeyConstraintsEnabledPostJellyBean(db);
        }
    }

    private void setForeignKeyConstraintsEnabledPreJellyBean(SQLiteDatabase db) {
        db.execSQL("PRAGMA foreign_keys = 1");
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void setForeignKeyConstraintsEnabledPostJellyBean(SQLiteDatabase db) {
        db.setForeignKeyConstraintsEnabled(true);
    }
}
