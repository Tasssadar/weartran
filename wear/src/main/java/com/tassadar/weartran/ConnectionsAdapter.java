package com.tassadar.weartran;

import android.support.wearable.view.WearableListView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ConnectionsAdapter extends WearableListView.Adapter {

    private static final String[][] stations = {
            { "Švermova", "Hrnčířská"},
            { "Hrnčířská", "Švermova" },
            { "Švermova", "Česká" },
            { "Česká", "Švermova" },
            { "Švermova", "Hlavní nádraží" },
            { "Hlavní nádraží", "Švermova" },
            { "Hlavní nádraží šíleně dlouhá zastávka", "Švermova" },
    };

    public static class ConnectionViewHolder extends WearableListView.ViewHolder {
        private TextView m_from;
        private TextView m_to;
        public ConnectionViewHolder(View v) {
            super(v);
            m_from = (TextView)v.findViewById(R.id.from);
            m_to = (TextView)v.findViewById(R.id.to);
        }

        public void setStops(final String from, final String to) {
            m_from.setText(from);
            m_to.setText(to);
        }

        public String getFromStop() {
            return m_from.getText().toString();
        }

        public String getToStop() {
            return m_to.getText().toString();
        }
    }

    @Override
    public WearableListView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.connections_list_it, parent, false);
        return new ConnectionViewHolder(v);
    }

    @Override
    public void onBindViewHolder(WearableListView.ViewHolder holder, int position) {
        ((ConnectionViewHolder)holder).setStops(stations[position][0], stations[position][1]);
    }

    @Override
    public int getItemCount() {
        return stations.length;
    }
}
