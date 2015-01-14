package com.tassadar.weartran;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.view.WearableListView;

public class MainActivity extends Activity implements WearableListView.ClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        WearableListView v = (WearableListView)findViewById(R.id.stationList);
        v.setAdapter(new ConnectionsAdapter());
        v.setClickListener(this);
    }
    @Override
    public void onClick(WearableListView.ViewHolder viewHolder) {
        ConnectionsAdapter.ConnectionViewHolder h = (ConnectionsAdapter.ConnectionViewHolder)viewHolder;

        Intent intent = new Intent(this, DeparturesActivity.class);
        intent.putExtra("from", h.getFromStop());
        intent.putExtra("to", h.getToStop());
        startActivity(intent);
    }

    @Override
    public void onTopEmptyRegionClick() {

    }
}
