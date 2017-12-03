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

}