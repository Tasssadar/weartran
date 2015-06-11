package com.tassadar.weartran;

import android.support.wearable.view.WearableListView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ConnectionsAdapter extends WearableListView.Adapter {
    private static class Connection {
        public Connection(final String dp, final String from, final String to) {
            this.dp = dp;
            this.from = from;
            this.to = to;
        }

        public String dp;
        public String from;
        public String to;
    };

    private static final Connection[] stations = {
            new Connection("IDSJMK", "Švermova", "Hrnčířská"),
            new Connection("IDSJMK", "Hrnčířská", "Švermova"),
            new Connection("IDSJMK", "Švermova", "Vojtova"),
            new Connection("IDSJMK", "Vojtova", "Švermova"),
            new Connection("IDSJMK", "Švermova", "Hlavní nádraží"),
            new Connection("IDSJMK", "Hlavní nádraží", "Švermova"),
            new Connection("IDSJMK", "Švermova", "Česká"),
            new Connection("IDSJMK", "Česká", "Švermova"),
            new Connection("VlakBusCZ", "Brno hl.n.", "Praha hl.n."),
            new Connection("VlakBusCZ", "Praha hl.n.", "Brno hl.n."),
    };

    public static class ConnectionViewHolder extends WearableListView.ViewHolder {
        private TextView m_from;
        private TextView m_to;
        private Connection m_conn;

        public ConnectionViewHolder(View v) {
            super(v);
            m_from = (TextView)v.findViewById(R.id.from);
            m_to = (TextView)v.findViewById(R.id.to);
        }

        public void setStops(final Connection c) {
            m_from.setText(c.from);
            m_to.setText(c.to);
            m_conn = c;
        }

        public String getFromStop() {
            return m_conn.from;
        }

        public String getToStop() {
            return m_conn.to;
        }

        public String getDp() { return m_conn.dp; }
    }

    @Override
    public WearableListView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.connections_list_it, parent, false);
        return new ConnectionViewHolder(v);
    }

    @Override
    public void onBindViewHolder(WearableListView.ViewHolder holder, int position) {
        ((ConnectionViewHolder)holder).setStops(stations[position]);
    }

    @Override
    public int getItemCount() {
        return stations.length;
    }
}
