package com.tassadar.weartran;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;


public class DeparturesActivity extends Activity implements MessageApi.MessageListener, GoogleApiClient.ConnectionCallbacks {
    private static final String GET_DEPARTURES_PATH = "/get-departures";
    private static final String DEPARTURES_RES_PATH = "/departures-res";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_departures);
        m_api = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addApi(Wearable.API)
                .build();
    }

    @Override
    protected void onResume() {
        super.onResume();
        m_api.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Wearable.MessageApi.removeListener(m_api, this);
        m_api.disconnect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Wearable.MessageApi.addListener(m_api, this);

        Bundle extras = getIntent().getExtras();
        if(extras == null || !extras.containsKey("from") || !extras.containsKey("to"))
            return;

        String req = extras.getString("from") + "\n" + extras.getString("to");
        sendMessageToCompanion(GET_DEPARTURES_PATH, req.getBytes());
    }

    private void sendMessageToCompanion(final String path, final byte[] data) {
        Wearable.NodeApi.getConnectedNodes(m_api).setResultCallback(
            new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                @Override
                public void onResult(NodeApi.GetConnectedNodesResult getConnectedNodesResult) {
                    for (final Node node : getConnectedNodesResult.getNodes()) {
                        Wearable.MessageApi.sendMessage(m_api, node.getId(), path, data);
                    }
                }
            }
        );

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onMessageReceived(MessageEvent ev) {
        if(ev.getPath().equals(DEPARTURES_RES_PATH)) {
            String res = new String(ev.getData());
            if(res.isEmpty())
                res = "Failed to load departures.";
            final String dep = res;
            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    View v = findViewById(R.id.progressbar);
                    v.setVisibility(View.GONE);
                    ((TextView)findViewById(R.id.output)).setText(dep);
                    v = findViewById(R.id.scrollView);
                    v.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    private GoogleApiClient m_api;
}
