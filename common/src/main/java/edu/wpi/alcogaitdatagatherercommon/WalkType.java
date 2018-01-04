package edu.wpi.alcogaitdatagatherercommon;

/**
 * Created by Adonay on 11/21/2017.
 */

public enum WalkType {
    NORMAL("NORMAL WALK", "NORMAL_WALK"),
    HEEL_TO_TOE("HEEL TO TOE", "HEEL_TO_TOE"),
    STANDING_ON_ONE_FOOT("STANDING ON ONE FOOT", "STANDING_ON_ONE_FOOT"),
    NYSTAGMUS("NYSTAGMUS", "NYSTAGMUS");

    String walkTypeString;
    String walkTypeStringNoSpace;

    WalkType(String walkTypeString, String walkTypeStringNoSpace) {
        this.walkTypeString = walkTypeString;
        this.walkTypeStringNoSpace = walkTypeStringNoSpace;
    }

    @Override
    public String toString() {
        return walkTypeString;
    }

    public String toNoSpaceString() {
        return walkTypeStringNoSpace;
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
