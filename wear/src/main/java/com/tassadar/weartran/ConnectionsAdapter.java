package com.tassadar.weartran;

import android.support.wear.widget.WearableRecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tassadar.weartran.api.Connection;

public class ConnectionsAdapter extends WearableRecyclerView.Adapter<ConnectionsAdapter.ConnectionViewHolder> {
    // seznam ids & coords from places.go script
    private static final Connection[] stations = {
            new Connection("IDSJMK", "Životského", "Křídlovická",
                    0x00e810c6, 49.190680, 16.634235, 0x00e8123a, 49.185304, 16.602648),
            new Connection("IDSJMK", "Křídlovická", "Životského",
                    0x00e8123a, 49.185304, 16.602648, 0x00e810c6, 49.190680, 16.634235),
            new Connection("IDSJMK", "Tržní", "Chrlická",
                    0x00e80ee1, 49.189576, 16.630827, 0x00e810a2, 49.144132, 16.665113),
            new Connection("IDSJMK", "Chrlická", "Životského",
                    0x00e810a2, 49.144132, 16.665113, 0x00e810c6, 49.190680, 16.634235),
            new Connection("IDSJMK", "Úzká", "Chrlická",
                    0x00e81216, 49.190028, 16.614611, 0x00e810a2, 49.144132, 16.665113),
            new Connection("IDSJMK", "Chrlická", "Úzká",
                    0x00e810a2, 49.144132, 16.665113, 0x00e81216, 49.190028, 16.614611),
            new Connection("IDSJMK", "Životského", "Hlavní nádraží",
                    0x00e810c6, 49.190680, 16.634235, 0x00e80ffe, 49.191152, 16.613029),
            new Connection("IDSJMK", "Hlavní nádraží", "Životského",
                    0x00e80ffe, 49.191152, 16.613029, 0x00e810c6, 49.190680, 16.634235),
            new Connection("IDSJMK", "Životského", "Švermova",
                    0x00e810c6, 49.190680, 16.634235, 0x00e80f8b, 49.169228, 16.573183),
            new Connection("IDSJMK", "Švermova", "Životského",
                    0x00e80f8b, 49.169228, 16.573183, 0x00e810c6, 49.190680, 16.634235),
            new Connection("IDSJMK", "Hlavní nádraží", "Švermova",
                    0x00e80ffe, 49.191152, 16.613029, 0x00e80f8b, 49.169228, 16.573183),
            new Connection("IDSJMK", "Vojtova", "Švermova",
                    0x00e80ad4, 49.182720, 16.600519, 0x00e80f8b, 49.169228, 16.573183),
            new Connection("VlakBusCZ", "Brno hl.n.", "Praha hl.n.",
                    0x00e81a31, 49.190588, 16.612799, 0x00e82004, 50.083016, 14.436977),
            new Connection("VlakBusCZ", "Praha hl.n.", "Brno hl.n.",
                    0x00e82004, 50.083016, 14.436977, 0x00e81a31, 49.190588, 16.612799),
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

        public Connection getConnection() {
            return m_conn;
        }
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
