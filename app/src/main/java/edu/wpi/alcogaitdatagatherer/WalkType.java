package edu.wpi.alcogaitdatagatherer;

/**
 * Created by Adonay on 11/21/2017.
 */

public enum WalkType {
    NORMAL("NORMAL WALK"),
    HEEL_TO_TOE("HEEL TO TOE"),
    STANDING_ON_ONE_FOOT("STANDING ON ONE FOOT"),
    NYSTAGMUS("NYSTAGMUS");

    String walkTypeString;

    WalkType(String walkTypeString) {
        this.walkTypeString = walkTypeString;
    }

    @Override
    public String toString() {
        return walkTypeString;
    }

    public WalkType next(WalkType prevWalkType) {
        for (int i = 0; i < WalkType.values().length - 1; i++) {
            if (prevWalkType == WalkType.values()[i]) {
                return WalkType.values()[i + 1];
            }
        }
        return null;
    }

}
