package com.tassadar.weartran;

import android.graphics.Color;
import android.media.Image;
import android.support.wearable.view.WearableListView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class DeparturesAdapter extends WearableListView.Adapter {

    public static class DepartureViewHolder extends WearableListView.ViewHolder {
        private TextView m_arrival;
        private TextView m_departure;
        private TextView m_extraInfo;
        private ImageView m_icon;

        public DepartureViewHolder(View v) {
            super(v);
            m_departure = (TextView)v.findViewById(R.id.departure);
            m_arrival = (TextView)v.findViewById(R.id.arrival);
            m_extraInfo = (TextView)v.findViewById(R.id.extraInfo);
            m_icon = (ImageView)v.findViewById(R.id.icon);
        }

        public void setData(Departure dep, boolean ambient) {
            m_departure.setText(dep.departure);
            m_arrival.setText(dep.arrival);
            m_extraInfo.setText(dep.extraInfo);

            m_departure.setTextColor(!ambient ? Color.BLACK : Color.WHITE);
            m_arrival.setTextColor(!ambient ? Color.BLACK : Color.WHITE);
            m_extraInfo.setTextColor(!ambient ? 0xFF757575 : 0xFFFFFFFF);
            m_icon.setVisibility(!ambient ? View.VISIBLE : View.GONE);
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
        ((DepartureViewHolder)holder).setData(m_departures[position], m_ambientMode);
    }

    @Override
    public int getItemCount() {
        return m_departures.length;
    }

    public void setAmbient(boolean ambient) {
        if(m_ambientMode == ambient)
            return;
        m_ambientMode = ambient;
        this.notifyDataSetChanged();
    }

    private Departure[] m_departures;
    private boolean m_ambientMode = false;
}
