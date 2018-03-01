package edu.wpi.alcogaitdatagatherer.models;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;

import edu.wpi.alcogaitdatagatherercommon.WalkType;

/**
 * Created by Adonay on 11/30/2017.
 */

public class WalkHolder implements Serializable {
    private int walkNumber;
    private int BAC;
    private HashMap<WalkType, Walk> walkMap;
    private WalkType[] allowedWalkTypes;

    public WalkHolder(int walkNumber) {
        this.walkNumber = walkNumber;
        walkMap = new HashMap<>();
        allowedWalkTypes = WalkType.values();
    }

    public int getWalkNumber() {
        return walkNumber;
    }

    public Walk get(WalkType walkType) {
        return walkMap.get(walkType);
    }

    public WalkHolder addWalk(Walk walk) {
        walkMap.put(walk.getWalkType(), walk);
        return this;
    }

    public WalkHolder removeWalk(WalkType walkType) {
        walkMap.remove(walkType);
        return this;
    }

    public WalkType getNextWalkType() {
        for (WalkType walkType : allowedWalkTypes) {
            if (!walkMap.containsKey(walkType)) {
                return walkType;
            }
        }
        return null;
    }

    public WalkType getPreviousWalkType(WalkType currentWalkType) {
        WalkType prevWalkType = null;
        for (WalkType walkType : allowedWalkTypes) {
            if (walkType == currentWalkType) {
                return prevWalkType;
            }
            prevWalkType = walkType;
        }
        return prevWalkType;
    }

    public boolean hasWalk(WalkType walkType) {
        return walkMap.containsKey(walkType);
    }

    public int getSampleSize() {
        int total = 0;
        for (WalkType walkType : allowedWalkTypes) {
            if (walkMap.containsKey(walkType)) {
                total += walkMap.get(walkType).getSampleSize();
            }
        }
        return total;
    }

    public WalkHolder setAllowedWalkTypes(int numOfWalkTypesAllowed) {
        if (numOfWalkTypesAllowed > 0 && numOfWalkTypesAllowed < 5) {
            allowedWalkTypes = Arrays.copyOfRange(WalkType.values(), 0, numOfWalkTypesAllowed);
        } else {
            allowedWalkTypes = WalkType.values();
        }
        return this;
    }
}
