package edu.wpi.alcogaitdatagatherer;

import android.content.Intent;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

/**
 * Created by Adonay on 10/27/2017.
 */

public class WearDataListenerService extends WearableListenerService {
    public static final String WEAR_HOME_ACTIVITY_PATH = "/start/WearHomeActivity";
    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        super.onMessageReceived(messageEvent);
        if(messageEvent.getPath().equals(WEAR_HOME_ACTIVITY_PATH)){
            Intent intent = new Intent(this , WearHomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }}