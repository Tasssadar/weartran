package com.tassadar.weartran;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class MainActivity extends Activity implements AdapterView.OnItemClickListener {

    private static final String[] stations = {
            "Švermova -> Hrnčířská",
            "Hrnčířská -> Švermova",
            "Švermova -> Česká",
            "Švermova -> Hlavní nádraží",
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView v = (ListView)findViewById(R.id.stationList);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.station_list_it, stations);
        v.setAdapter(adapter);
        v.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        String[] tok = stations[i].split(" -> ");
        Intent intent = new Intent(this, DeparturesActivity.class);
        intent.putExtra("from", tok[0]);
        intent.putExtra("to", tok[1]);
        startActivity(intent);
    }
}
