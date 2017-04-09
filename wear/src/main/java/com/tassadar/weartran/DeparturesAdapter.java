package com.tassadar.weartran;

import android.graphics.Color;
import android.support.wearable.view.WearableRecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.tassadar.weartran.api.Connection;
import com.tassadar.weartran.api.DepartureInfo;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class DeparturesAdapter extends WearableRecyclerView.Adapter<DeparturesAdapter.DepartureViewHolder> {

    public static class DepartureViewHolder extends WearableRecyclerView.ViewHolder {
        private TextView m_arrival;
        private TextView m_departure;
        private TextView m_delay;
        private TextView m_extraInfo;
        private ImageView m_icon;

        private static final SimpleDateFormat fmt = new SimpleDateFormat("HH:mm");

        public DepartureViewHolder(View v) {
            super(v);
            m_departure = (TextView)v.findViewById(R.id.departure);
            m_arrival = (TextView)v.findViewById(R.id.arrival);
            m_delay = (TextView)v.findViewById(R.id.delay);
            m_extraInfo = (TextView)v.findViewById(R.id.extraInfo);
            m_icon = (ImageView)v.findViewById(R.id.icon);
        }

        public void setData(DepartureInfo dep, boolean ambient) {
            String departure = fmt.format(dep.depTime);
            String arrival = fmt.format(dep.arrTime);

            if(dep.depStationDifferent)
                departure += "*";
            if(dep.arrStationDifferent)
                arrival += "*";

            m_departure.setText(departure);
            m_arrival.setText(arrival);

            StringBuilder extraInfo = new StringBuilder();
            extraInfo.append("# ");
            for(int i = 0; i < dep.trains.length; ++i) {
                extraInfo.append(dep.trains[i]);
                if(i+1 < dep.trains.length)
                    extraInfo.append(", ");
            }
            extraInfo.append("\n");

            long durationMin = TimeUnit.MINUTES.convert(dep.arrTime.getTime() - dep.depTime.getTime(), TimeUnit.MILLISECONDS);
            if(durationMin >= 60)  {
                extraInfo.append(durationMin/60)
                        .append("h ");
                durationMin = durationMin%60;
            }
            extraInfo.append(durationMin)
                    .append(" min");

            m_extraInfo.setText(extraInfo.toString());

            m_departure.setTextColor(!ambient ? Color.BLACK : Color.WHITE);
            m_arrival.setTextColor(!ambient ? Color.BLACK : Color.WHITE);
            m_extraInfo.setTextColor(!ambient ? 0xFF757575 : 0xFFFFFFFF);
            m_icon.setVisibility(!ambient ? View.VISIBLE : View.GONE);

            if(dep.delayMinutes > 0) {
                m_delay.setText(String.format("%d min", dep.delayMinutes));
                m_delay.setTextColor(!ambient ? 0xFFE64A19 : Color.WHITE);
                m_delay.setVisibility(View.VISIBLE);
            } else {
                m_delay.setVisibility(View.GONE);
            }
        }
    }

    public DeparturesAdapter(List<DepartureInfo> departures) {
        super();
        m_departures = departures;
    }

    @Override
    public DepartureViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.departures_list_it, parent, false);
        return new DepartureViewHolder(v);
    }

    @Override
    public void onBindViewHolder(DepartureViewHolder holder, int position) {
        holder.setData(m_departures.get(position), m_ambientMode);
    }

    @Override
    public int getItemCount() {
        return m_departures.size();
    }

    public void setAmbient(boolean ambient) {
        if(m_ambientMode == ambient)
            return;
        m_ambientMode = ambient;
        this.notifyDataSetChanged();
    }

    private List<DepartureInfo> m_departures;
    private boolean m_ambientMode;
}
