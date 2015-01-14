package com.tassadar.weartran;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

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

    public void onMessageReceived (final MessageEvent ev) {
        if(ev.getPath().equals(GET_DEPARTURES_PATH)) {
            String data[] = new String(ev.getData()).split("\\n");
            final String from = data[0];
            final String to = data[1];

            GetDeparturesTask task = new GetDeparturesTask(ev.getSourceNodeId(), this);
            task.execute(from, to);
        }
    }

    @Override
    public void departuresRetreived(final String reqId, final String out) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Wearable.MessageApi.sendMessage(m_api, reqId, DEPARTURES_RES_PATH, out.getBytes());
            }
        }).start();
    }

    private GoogleApiClient m_api;
}
