package com.tassadar.weartran;

import android.graphics.Color;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.CurvedChildLayoutManager;
import android.support.wearable.view.WearableRecyclerView;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.tassadar.weartran.api.DepartureInfo;
import com.tassadar.weartran.api.GetDeparturesTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class DeparturesActivity extends WearableActivity implements GetDeparturesTask.OnCompleteListener {
    private static final String GET_DEPARTURES_PATH = "/get-departures";
    private static final String DEPARTURES_RES_PATH = "/departures-res";
    private static final String TAG = "Weartran:DeparturesAct";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_departures);

        m_departures = new ArrayList<>();

        m_adapter = new DeparturesAdapter(m_departures, getIntent().getStringExtra("from"), getIntent().getStringExtra("to"));
        WearableRecyclerView lst = (WearableRecyclerView) findViewById(R.id.departuresList);
        lst.setCenterEdgeItems(true);
        if(getResources().getConfiguration().isScreenRound())
            lst.setLayoutManager(new ResizingCurvedLayoutManager(this));
        else
            lst.setLayoutManager(new CurvedChildLayoutManager(this));
        lst.setAdapter(m_adapter);

        if(savedInstanceState != null && savedInstanceState.containsKey("departures")) {
            DepartureInfo.deserialize(savedInstanceState.getByteArray("departures"), m_departures);
            fillDeparturesList(true);
            updateTime();
        } else {
            requestDepartures();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out);
        if(!m_departures.isEmpty())
            out.putByteArray("departures", DepartureInfo.serialize(m_departures));
    }

    private void requestDepartures() {
        if(!m_departures.isEmpty()) {
            updateTime();
            return;
        }

        Bundle extras = getIntent().getExtras();
        if(extras == null || !extras.containsKey("from") || !extras.containsKey("to"))
            return;

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        String dp = extras.getString("dp");
        String from = extras.getString("from");
        String to = extras.getString("to");
        Log.i(TAG, "Handling departures request for path " + from + " -> " + to);
        GetDeparturesTask task = new GetDeparturesTask(this);
        task.execute(dp, from, to);
    }

    @Override
    public void departuresRetreived(List<DepartureInfo> departures) {
        m_departures.addAll(departures);
        if(m_departures.isEmpty()) {
            setError(getString(R.string.dep_failed));
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    fillDeparturesList(false);
                }
            });
            updateTime();
        }
    }

    @Override
    public void allDeparturesRetreived(boolean success) {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if(success) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    fillDeparturesList(true);
                }
            });
            updateTime();
        } else {
            setError(getString(R.string.dep_failed));
        }
    }

    private void setError(final String text) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                View v = findViewById(R.id.progressbar);
                v.setVisibility(View.GONE);
                TextView t = (TextView) findViewById(R.id.error);
                t.setText(text);
                t.setVisibility(View.VISIBLE);
            }
        });
    }

    private void updateTime() {
        TextView v = (TextView)findViewById(R.id.time);
        v.setText(m_timeFmt.format(new Date()));
    }

    private void fillDeparturesList(boolean complete) {
        m_adapter.notifyDataSetChanged();

        WearableRecyclerView lst = (WearableRecyclerView) findViewById(R.id.departuresList);
        lst.setClickable(false);
        lst.setVisibility(View.VISIBLE);

        View v = findViewById(R.id.progressbar);
        v.setVisibility(complete ? View.GONE : View.VISIBLE);
        v = findViewById(R.id.time);
        v.setVisibility(View.VISIBLE);
        v = findViewById(R.id.error);
        v.setVisibility(View.GONE);

        setAmbientEnabled();
    }

    @Override
    public void onEnterAmbient(Bundle ambientDetails) {
        super.onEnterAmbient(ambientDetails);

        View l = findViewById(R.id.main_layout);
        l.setBackgroundColor(Color.BLACK);

        TextView t = (TextView)l.findViewById(R.id.time);
        t.setTextColor(Color.WHITE);

        m_adapter.setAmbient(true);
    }

    @Override
    public void onExitAmbient() {
        super.onExitAmbient();

        View l = findViewById(R.id.main_layout);
        l.setBackgroundColor(Color.WHITE);

        TextView t = (TextView)l.findViewById(R.id.time);
        t.setTextColor(Color.BLACK);

        m_adapter.setAmbient(false);

        updateTime();
    }

    @Override
    public void onUpdateAmbient() {
        super.onUpdateAmbient();
        updateTime();
    }

    private SimpleDateFormat m_timeFmt = new SimpleDateFormat("HH:mm");
    private ArrayList<DepartureInfo> m_departures;
    private DeparturesAdapter m_adapter;
}
