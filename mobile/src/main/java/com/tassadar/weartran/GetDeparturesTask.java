package com.tassadar.weartran;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class GetDeparturesTask extends AsyncTask<String, Void, String> {

    public interface OnCompleteListener {
        public void departuresRetreived(String reqId, String out);
    }

    public GetDeparturesTask(String id, OnCompleteListener listener) {
        m_reqId = id;
        m_listener = new WeakReference<>(listener);
    }

    @Override
    protected String doInBackground(String... args) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(WeartranApp.ctx());
        String savedSessionId = pref.getString("IdosApiSession", null);
        IdosApi api = new IdosApi(savedSessionId, new IdosApiCredentials());

        if(savedSessionId == null && !api.login())
            return null;

        IdosApi.DepartureInfo[] out = api.getDepartures(args[0], args[1], new Date(), 3);
        if(out == null || out.length == 0)
            return null;

        final SimpleDateFormat fmt = new SimpleDateFormat("HH:mm");
        StringBuilder result = new StringBuilder();
        for(IdosApi.DepartureInfo i : out) {
            result.append("[");
            for(int y = 0; y < i.trains.length-1; ++y)
                result.append(i.trains[y]).append(", ");
            if(i.trains.length >= 1)
                result.append(i.trains[i.trains.length-1]);
            result.append("]");

            try {
                Date dep = IdosApi.DEPARTURES_TIME_FMT.parse(i.depTime);
                Date arr = IdosApi.DEPARTURES_TIME_FMT.parse(i.arrTime);
                result.append("  ")
                    .append(fmt.format(dep))
                    .append(" -> ")
                    .append(fmt.format(arr));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            result.append("\n");
        }

        if(savedSessionId == null || !savedSessionId.equals(api.getSessionId())) {
            SharedPreferences.Editor e = pref.edit();
            e.putString("IdosApiSession", api.getSessionId());
            e.apply();
        }

        return result.toString();
    }

    protected void onPostExecute(String result) {
        OnCompleteListener l = m_listener.get();
        if(l != null) {
            l.departuresRetreived(m_reqId, result != null ? result : "");
        }
    }

    private String m_reqId;
    private WeakReference<OnCompleteListener> m_listener;
}
