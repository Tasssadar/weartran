package com.tassadar.weartran;

import android.support.wearable.view.WearableListView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class DeparturesAdapter extends WearableListView.Adapter {

    public static class DepartureViewHolder extends WearableListView.ViewHolder {
        private TextView m_arrival;
        private TextView m_departure;
        private TextView m_extraInfo;

        public DepartureViewHolder(View v) {
            super(v);
            m_departure = (TextView)v.findViewById(R.id.departure);
            m_arrival = (TextView)v.findViewById(R.id.arrival);
            m_extraInfo = (TextView)v.findViewById(R.id.extraInfo);
        }

        public void setData(Departure dep) {
            m_departure.setText(dep.departure);
            m_arrival.setText(dep.arrival);
            m_extraInfo.setText(dep.extraInfo);
        }
    }

    public DeparturesAdapter(Departure[] departures) {
        super();
        m_departures = departures;
    }

    @Override
    public WearableListView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.departures_list_it, parent, false);
        return new DepartureViewHolder(v);
    }

    @Override
    public void onBindViewHolder(WearableListView.ViewHolder holder, int position) {
        ((DepartureViewHolder)holder).setData(m_departures[position]);
    }

    @Override
    public int getItemCount() {
        return m_departures.length;
    }

    private Departure[] m_departures;
}
