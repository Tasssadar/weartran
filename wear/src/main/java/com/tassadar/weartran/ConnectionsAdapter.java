package com.tassadar.weartran;

import android.support.wearable.view.WearableListView;
import android.support.wearable.view.WearableRecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ConnectionsAdapter extends WearableRecyclerView.Adapter<ConnectionsAdapter.ConnectionViewHolder> {
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
            new Connection("IDSJMK", "Životského", "Vojtova"),
            new Connection("IDSJMK", "Vojtova", "Životského"),
            new Connection("IDSJMK", "Životského", "Hlavní nádraží"),
            new Connection("IDSJMK", "Hlavní nádraží", "Životského"),
            new Connection("IDSJMK", "Životského", "Švermova"),
            new Connection("IDSJMK", "Švermova", "Životského"),
            new Connection("IDSJMK", "Hlavní nádraží", "Švermova"),
            new Connection("IDSJMK", "Vojtova", "Švermova"),
            new Connection("VlakBusCZ", "Brno hl.n.", "Praha hl.n."),
            new Connection("VlakBusCZ", "Praha hl.n.", "Brno hl.n."),
    };

    public static class ConnectionViewHolder extends WearableRecyclerView.ViewHolder {
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

    private final ItemClickListener m_listener;

    ConnectionsAdapter(ItemClickListener listener) {
        m_listener = listener;
    }

    @Override
    public ConnectionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.connections_list_it, parent, false);
        final ConnectionViewHolder holder = new ConnectionViewHolder(v);
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                m_listener.onItemClick(holder);
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(ConnectionViewHolder holder, int position) {
        holder.setStops(stations[position]);
    }

    @Override
    public int getItemCount() {
        return stations.length;
    }
}
