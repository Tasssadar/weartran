package com.tassadar.weartran.api;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import com.tassadar.weartran.WeartranApp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.util.Date;

public class GetDeparturesTask extends AsyncTask<String, Void, byte[]> {

    public interface OnCompleteListener {
        public void departuresRetreived(byte[] out);
    }

    public GetDeparturesTask(OnCompleteListener listener) {
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

        IdosApi.DepartureInfo[] dep = api.getDepartures(args[0], args[1], args[2], new Date(), 9, true);
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
                out.writeUTF(i.depStation);
                out.writeUTF(i.arrStation);
                out.writeUTF(i.delayQuery != null ? i.delayQuery : "");
                out.writeInt(i.delayMinutes);
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
        l.departuresRetreived(result);
    }
    private WeakReference<OnCompleteListener> m_listener;
}
