package com.tassadar.weartran;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.support.wear.widget.WearableLinearLayoutManager;
import android.support.wearable.activity.WearableActivity;
import android.support.wear.widget.WearableRecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.tassadar.weartran.api.Connection;
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

        m_adapter = new DeparturesAdapter(m_departures);
        WearableRecyclerView lst = (WearableRecyclerView) findViewById(R.id.departuresList);
        lst.setEdgeItemsCenteringEnabled(true);
        m_layoutManager = new WearableLinearLayoutManager(this);
        if(getResources().getConfiguration().isScreenRound())
            m_layoutManager.setLayoutCallback(new ResizingLayoutCallback());
        lst.setLayoutManager(m_layoutManager);
        lst.setAdapter(m_adapter);
        lst.addOnScrollListener(m_scrollListener);

        updateTime();

        if(savedInstanceState != null && savedInstanceState.containsKey("departures")) {
            DepartureInfo.deserialize(savedInstanceState.getByteArray("departures"), m_departures);
            fillDeparturesList(true);
        } else {
            requestDepartures(new Date());
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out);
        if(!m_departures.isEmpty()) {
            out.putByteArray("departures", DepartureInfo.serialize(m_departures));
        }
    }

    private void requestDepartures(Date when) {
        Bundle extras = getIntent().getExtras();
        if(m_loadingDepartures || extras == null || !extras.containsKey("connection"))
            return;

        m_loadingDepartures = true;

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Connection c = (Connection)extras.getSerializable("connection");
        Log.i(TAG, "Handling departures request for path " + c.from + " -> " + c.to + " at " + when.toString());
        GetDeparturesTask task = GetDeparturesTask.create(GetDeparturesTask.API_DEFAULT, this);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, c, when);

        View v = findViewById(R.id.progressbar);
        v.setVisibility(View.VISIBLE);
    }

    private void requestMoreDepartures() {
        if(m_loadingDepartures || m_departures.size() == 0)
            return;

        final DepartureInfo last = m_departures.get(m_departures.size()-1);
        requestDepartures(new Date(last.depTime.getTime() + 60*1000));
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
        m_loadingDepartures = false;

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
        v = findViewById(R.id.error);
        v.setVisibility(View.GONE);

        setAmbientEnabled();
    }

    @Override
    public void onEnterAmbient(Bundle ambientDetails) {
        super.onEnterAmbient(ambientDetails);

        View l = findViewById(R.id.main_layout);
        l.setBackgroundColor(Color.BLACK);

        m_adapter.setAmbient(true);
    }

    @Override
    public void onExitAmbient() {
        super.onExitAmbient();

        TypedValue clr = new TypedValue();
        getTheme().resolveAttribute(android.R.attr.windowBackground, clr, true);
        View l = findViewById(R.id.main_layout);
        l.setBackgroundColor(clr.data);

        m_adapter.setAmbient(false);

        updateTime();
    }

    @Override
    public void onUpdateAmbient() {
        super.onUpdateAmbient();
        updateTime();
    }

    private final OnScrollListener m_scrollListener = new OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            if(m_layoutManager.findLastVisibleItemPosition() == m_adapter.getItemCount()-1) {
                requestMoreDepartures();
            }
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

        }
    };

    private SimpleDateFormat m_timeFmt = new SimpleDateFormat("HH:mm");
    private ArrayList<DepartureInfo> m_departures;
    private DeparturesAdapter m_adapter;
    private WearableLinearLayoutManager m_layoutManager;
    private boolean m_loadingDepartures;
}
