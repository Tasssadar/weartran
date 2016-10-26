package com.tassadar.weartran;

import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.concurrent.TimeUnit;

public class DataService extends WearableListenerService implements GetDeparturesTask.OnCompleteListener {
    private static final String TAG = "Weartran:DataService";
    private static final String GET_DEPARTURES_PATH = "/get-departures";
    private static final String DEPARTURES_RES_PATH = "/departures-res";

    @Override
    public void onCreate() {
        super.onCreate();

        m_api = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
        m_api.connect();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        m_api.disconnect();
    }

    public void onMessageReceived (final MessageEvent ev) {
        if(ev.getPath().equals(GET_DEPARTURES_PATH)) {
            String data[] = new String(ev.getData()).split("\\n");
            final String dp = data[0];
            final String from = data[1];
            final String to = data[2];

            Log.i(TAG, "Handling departures request for path " + from + " -> " + to);
            GetDeparturesTask task = new GetDeparturesTask(ev.getSourceNodeId(), this);
            task.execute(dp, from, to);
        }
    }

    @Override
    public void departuresRetreived(final String reqId, final byte[] out) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                GoogleApiClient api = new GoogleApiClient.Builder(DataService.this)
                        .addApi(Wearable.API)
                        .build();

                ConnectionResult res = api.blockingConnect(5, TimeUnit.SECONDS);
                if(!res.isSuccess()) {
                    Log.i(TAG, "Failed to connect to google play wearable api: " + res.getErrorMessage());
                    return;
                }

                Log.i(TAG, "Sending result to the watch " + reqId + " " + api.isConnected());
                Wearable.MessageApi.sendMessage(api, reqId, DEPARTURES_RES_PATH, out);
                api.disconnect();
            }
        }).start();
    }

    private GoogleApiClient m_api;
}
