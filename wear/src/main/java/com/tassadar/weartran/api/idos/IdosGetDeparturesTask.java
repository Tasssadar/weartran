package com.tassadar.weartran.api.idos;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.tassadar.weartran.WeartranApp;
import com.tassadar.weartran.api.Connection;
import com.tassadar.weartran.api.DepartureInfo;
import com.tassadar.weartran.api.GetDeparturesTask;

import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.List;

public class IdosGetDeparturesTask extends GetDeparturesTask implements IdosApi.DeparturesBlockListener {
    private static final String TAG = "Weartran:GetDepartures";

    public IdosGetDeparturesTask(OnCompleteListener listener) {
        super(listener);
    }

    @Override
    protected Boolean doInBackground(Object... args) {
        final Connection c = (Connection)args[0];
        final Date when = (Date)args[1];

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(WeartranApp.ctx());
        String savedSessionId = pref.getString("IdosApiSession", null);

        // See comment about credentials in IdosApi.java
        IdosApi api = new IdosApi(savedSessionId, new IdosApiCredentials());

        long start = System.currentTimeMillis();
        if(savedSessionId == null && !api.login())
            return false;
        Log.i(TAG, "Login took " + (System.currentTimeMillis() - start) + "ms");

        DepartureInfo[] dep = api.getDepartures(c, when, GetDeparturesTask.MAX_DEPARTURES, true, this);
        if(dep == null || dep.length == 0)
            return false;

        if(savedSessionId == null || !savedSessionId.equals(api.getSessionId())) {
            SharedPreferences.Editor e = pref.edit();
            e.putString("IdosApiSession", api.getSessionId());
            e.apply();
        }
        return true;
    }

    @Override
    public void onDeparturesBlockFetched(List<DepartureInfo> block) {
        this.publishProgress(block);
    }
}
