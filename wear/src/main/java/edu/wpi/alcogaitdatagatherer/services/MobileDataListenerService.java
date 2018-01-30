package edu.wpi.alcogaitdatagatherer.services;

import android.content.Intent;
import android.support.annotation.NonNull;

import com.google.android.gms.wearable.MessageClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import edu.wpi.alcogaitdatagatherer.ui.WearHomeActivity;
import edu.wpi.alcogaitdatagatherercommon.CommonCode;

/**
 * Created by Adonay on 10/27/2017.
 */

public class MobileDataListenerService extends WearableListenerService implements MessageClient.OnMessageReceivedListener {

    @Override
    public void onCreate() {
        super.onCreate();
        Wearable.getMessageClient(this).addListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Wearable.getMessageClient(this).removeListener(this);
    }

    @Override
    public void onMessageReceived(@NonNull MessageEvent messageEvent) {
        super.onMessageReceived(messageEvent);
        if (messageEvent.getPath().equalsIgnoreCase(CommonCode.WEAR_HOME_ACTIVITY_PATH) || CommonCode.OPEN_APP.equalsIgnoreCase(new String(messageEvent.getData()))) {
            Intent intent = new Intent(this , WearHomeActivity.class);
            startActivity(intent);
        }
    }
}