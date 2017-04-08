package com.tassadar.weartran.api;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.tassadar.weartran.WeartranApp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

public class GetDeparturesTask extends AsyncTask<String, List<DepartureInfo>, Boolean> implements IdosApi.DeparturesBlockListener {
    private static final String TAG = "Weartran:GetDepartures";

    public interface OnCompleteListener {
        void departuresRetreived(List<DepartureInfo> departures);
        void allDeparturesRetreived(boolean success);
    }

    public GetDeparturesTask(OnCompleteListener listener) {
        m_listener = new WeakReference<>(listener);
    }

    @Override
    protected Boolean doInBackground(String... args) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(WeartranApp.ctx());
        String savedSessionId = pref.getString("IdosApiSession", null);

        // See comment about credentials in IdosApi.java
        IdosApi api = new IdosApi(savedSessionId, new IdosApiCredentials());

        long start = System.currentTimeMillis();
        if(savedSessionId == null && !api.login())
            return false;
        Log.i(TAG, "Login took " + (System.currentTimeMillis() - start) + "ms");

        DepartureInfo[] dep = api.getDepartures(args[0], args[1], args[2], new Date(), 9, true, this);
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

    @Override
    protected void onProgressUpdate(List<DepartureInfo>... departures) {
        OnCompleteListener l = m_listener.get();
        if(l == null)
            return;
        l.departuresRetreived(departures[0]);
    }

    @Override
    protected void onPostExecute(Boolean res) {
        OnCompleteListener l = m_listener.get();
        if(l == null)
            return;
        l.allDeparturesRetreived(res);
    }

    private WeakReference<OnCompleteListener> m_listener;
}
