package com.tassadar.weartran;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.util.Date;

public class GetDeparturesTask extends AsyncTask<String, Void, byte[]> {

    public interface OnCompleteListener {
        public void departuresRetreived(String reqId, byte[] out);
    }

    public GetDeparturesTask(String id, OnCompleteListener listener) {
        m_reqId = id;
        m_listener = new WeakReference<>(listener);
    }

    @Override
    protected byte[] doInBackground(String... args) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(WeartranApp.ctx());
        String savedSessionId = pref.getString("IdosApiSession", null);

        // See comment about credentials in IdosApi.java
        IdosApi api = new IdosApi(savedSessionId, new IdosApiCredentials());

        if(savedSessionId == null && !api.login())
            return null;

        IdosApi.DepartureInfo[] dep = api.getDepartures(args[0], args[1], new Date(), 9);
        if(dep == null || dep.length == 0)
            return null;

        if(savedSessionId == null || !savedSessionId.equals(api.getSessionId())) {
            SharedPreferences.Editor e = pref.edit();
            e.putString("IdosApiSession", api.getSessionId());
            e.apply();
        }

        ByteArrayOutputStream bs = null;
        ObjectOutputStream out = null;
        try {
            bs = new ByteArrayOutputStream();
            out = new ObjectOutputStream(bs);
            out.writeInt(dep.length);

            for(IdosApi.DepartureInfo i : dep) {
                out.writeObject(IdosApi.DEPARTURES_TIME_FMT.parse(i.depTime));
                out.writeObject(IdosApi.DEPARTURES_TIME_FMT.parse(i.arrTime));
                out.writeInt(i.trains.length);
                for(String tr : i.trains) {
                    out.writeUTF(tr);
                }
            }
            out.close();
            out = null;
            return bs.toByteArray();
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        } finally {
            if(out != null) { try { out.close(); } catch(IOException e) { e.printStackTrace(); } }
            if(bs != null) { try { bs.close(); } catch(IOException e) { e.printStackTrace(); } }
        }
        return null;
    }

    protected void onPostExecute(byte[] result) {
        OnCompleteListener l = m_listener.get();
        if(l == null)
            return;
        l.departuresRetreived(m_reqId, result);
    }

    private String m_reqId;
    private WeakReference<OnCompleteListener> m_listener;
}
