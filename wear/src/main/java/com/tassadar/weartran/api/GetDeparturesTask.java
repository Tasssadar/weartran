package com.tassadar.weartran.api;

import android.os.AsyncTask;

import com.tassadar.weartran.api.idos.IdosGetDeparturesTask;
import com.tassadar.weartran.api.seznam.SeznamGetDeparturesTask;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * Created by tassadar on 9.4.17.
 */

public abstract class GetDeparturesTask extends AsyncTask<Object, List<DepartureInfo>, Boolean> {
    public interface OnCompleteListener {
        void departuresRetreived(List<DepartureInfo> departures);
        void allDeparturesRetreived(boolean success);
    }

    public static final int MAX_DEPARTURES = 3;

    public static final int API_IDOS = 0;
    public static final int API_SEZNAM = 1;

    public static final int API_DEFAULT = API_SEZNAM;

    public static GetDeparturesTask create(int api, OnCompleteListener listener) {
        switch(api) {
            case API_IDOS:
                return new IdosGetDeparturesTask(listener);
            case API_SEZNAM:
                return new SeznamGetDeparturesTask(listener);
        }
        return null;
    }

    protected WeakReference<OnCompleteListener> m_listener;

    public GetDeparturesTask(OnCompleteListener listener) {
        m_listener = new WeakReference<>(listener);
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
}
