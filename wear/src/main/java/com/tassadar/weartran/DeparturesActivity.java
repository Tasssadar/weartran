package com.tassadar.weartran;

import android.graphics.Color;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.WearableListView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.tassadar.weartran.api.GetDeparturesTask;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;


public class DeparturesActivity extends WearableActivity implements GetDeparturesTask.OnCompleteListener {
    private static final String GET_DEPARTURES_PATH = "/get-departures";
    private static final String DEPARTURES_RES_PATH = "/departures-res";
    private static final String TAG = "Weartran:DeparturesAct";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_departures);

        if(savedInstanceState != null && savedInstanceState.containsKey("departures")) {
            m_departures = savedInstanceState.getByteArray("departures");
            updateTime();
            parseDepartures();
        }

        requestDepartures();
    }

    @Override
    protected void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out);
        out.putByteArray("departures", m_departures);
    }

    private void requestDepartures() {
        if(m_departures != null) {
            updateTime();
            return;
        }

        Bundle extras = getIntent().getExtras();
        if(extras == null || !extras.containsKey("from") || !extras.containsKey("to"))
            return;

        String dp = extras.getString("dp");
        String from = extras.getString("from");
        String to = extras.getString("to");
        Log.i(TAG, "Handling departures request for path " + from + " -> " + to);
        GetDeparturesTask task = new GetDeparturesTask(this);
        task.execute(dp, from, to);
    }

    @Override
    public void departuresRetreived(byte[] data) {
        m_departures = data;
        if(m_departures == null || m_departures.length == 0 || !parseDepartures()) {
            setError(getString(R.string.dep_failed));
            m_departures = null;
        } else {
            updateTime();
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

    private void fillDeparturesList(Departure[] departures) {
        WearableListView lst = (WearableListView) findViewById(R.id.departuresList);
        lst.setClickable(false);
        lst.setAdapter(new DeparturesAdapter(departures));
        lst.setVisibility(View.VISIBLE);

        View v = findViewById(R.id.progressbar);
        v.setVisibility(View.GONE);
        v = findViewById(R.id.time);
        v.setVisibility(View.VISIBLE);
        v = findViewById(R.id.error);
        v.setVisibility(View.GONE);

        setAmbientEnabled();
    }

    private boolean parseDepartures() {
        ByteArrayInputStream bs = null;
        ObjectInputStream out = null;
        try {
            bs = new ByteArrayInputStream(m_departures);
            out = new ObjectInputStream(bs);

            String from = null;
            String to = null;

            Bundle extras = getIntent().getExtras();
            if(extras != null) {
                from = extras.getString("from", null);
                to = extras.getString("to", null);
                if(from != null)
                    from = from.toLowerCase();
                if(to != null)
                    to = to.toLowerCase();
            }

            final int count = out.readInt();
            if(count == 0) {
                return false;
            }

            final Departure[] departures = new Departure[count];
            final SimpleDateFormat fmt = new SimpleDateFormat("HH:mm");
            for(int i = 0; i < count; ++i) {
                Departure dep = new Departure();
                Date depTime = (Date)out.readObject();
                Date arrTime = (Date)out.readObject();
                dep.departure = fmt.format(depTime);
                dep.arrival = fmt.format(arrTime);

                StringBuilder extraInfo = new StringBuilder();
                final int trainCnt = out.readInt() - 1;
                extraInfo.append("# ");
                for(int tr = 0; tr < trainCnt; ++tr)
                    extraInfo.append(out.readUTF()).append(", ");
                if(trainCnt >= 0)
                    extraInfo.append(out.readUTF());
                extraInfo.append("\n");

                final String depStation = out.readUTF().toLowerCase();
                final String arrStation = out.readUTF().toLowerCase();

                if(from != null && !depStation.equals(from))
                    dep.departure += "*";

                if(to != null && !arrStation.equals(to))
                    dep.arrival += "*";

                long durationMin = TimeUnit.MINUTES.convert(arrTime.getTime() - depTime.getTime(), TimeUnit.MILLISECONDS);
                if(durationMin >= 60)  {
                    extraInfo.append(durationMin/60)
                            .append("h ");
                    durationMin = durationMin%60;
                }
                extraInfo.append(durationMin)
                        .append(" min");
                dep.extraInfo = extraInfo.toString();

                dep.delayQuery = out.readUTF();
                dep.delayMinutes = out.readInt();

                departures[i] = dep;
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    fillDeparturesList(departures);
                }
            });
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        }  finally {
            if(out != null) { try { out.close(); } catch(IOException e) { e.printStackTrace(); } }
            if(bs != null) { try { bs.close(); } catch(IOException e) { e.printStackTrace(); } }
        }
        return true;
    }

    @Override
    public void onEnterAmbient(Bundle ambientDetails) {
        super.onEnterAmbient(ambientDetails);

        View l = findViewById(R.id.main_layout);
        l.setBackgroundColor(Color.BLACK);

        TextView t = (TextView)l.findViewById(R.id.time);
        t.setTextColor(Color.WHITE);

        WearableListView lst = (WearableListView) findViewById(R.id.departuresList);
        DeparturesAdapter adapter = (DeparturesAdapter) lst.getAdapter();
        if(adapter != null) {
            adapter.setAmbient(true);
        }
    }

    @Override
    public void onExitAmbient() {
        super.onExitAmbient();

        View l = findViewById(R.id.main_layout);
        l.setBackgroundColor(Color.WHITE);

        TextView t = (TextView)l.findViewById(R.id.time);
        t.setTextColor(Color.BLACK);

        WearableListView lst = (WearableListView) findViewById(R.id.departuresList);
        DeparturesAdapter adapter = (DeparturesAdapter) lst.getAdapter();
        if(adapter != null) {
            adapter.setAmbient(false);
        }

        updateTime();
    }

    @Override
    public void onUpdateAmbient() {
        super.onUpdateAmbient();
        updateTime();
    }

    private SimpleDateFormat m_timeFmt = new SimpleDateFormat("HH:mm");
    private byte[] m_departures;
}
