package com.tassadar.weartran.api.seznam;

import android.util.Log;

import com.tassadar.weartran.api.Connection;
import com.tassadar.weartran.api.DepartureInfo;
import com.tassadar.weartran.api.GetDeparturesTask;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import cz.seznam.anuc.AnucArray;
import cz.seznam.anuc.AnucStruct;
import cz.seznam.anuc.MapAnucStruct;
import cz.seznam.anuc.exceptions.AnucException;

/**
 * Created by tassadar on 9.4.17.
 */

public class SeznamGetDeparturesTask extends GetDeparturesTask {
    private static final String TAG = "SeznamGetDep";
    public SeznamGetDeparturesTask(OnCompleteListener listener) {
        super(listener);
    }

    private Map getPlaceParam(long stopId, double x, double y) {
        HashMap place = new HashMap();
        if(stopId != 0) {
            place.put("source", "pubt");
            place.put("id", stopId);
        }
        place.put("x", x);
        place.put("y", y);
        return place;
    }

    private Map getPlaceParam(Connection c, boolean from) {
        if(from) {
            return getPlaceParam(c.sznFromId, c.sznFromX, c.sznFromY);
        } else {
            return getPlaceParam(c.sznToId, c.sznToX, c.sznToY);
        }
    }

    @Override
    protected Boolean doInBackground(Connection... connections) {
        Connection c = connections[0];
        MapAnucStruct resp = null;
        ArrayList<DepartureInfo> res = new ArrayList<>();
        try {
            Calendar time = Calendar.getInstance(TimeZone.getTimeZone("Europe/Prague"));
            HashMap params = new HashMap();
            params.put("start", getPlaceParam(c, true));
            params.put("end", getPlaceParam(c, false));
            params.put("index", 0);
            params.put("count", 9);

            HashMap flags = new HashMap();
            flags.put("unixt", 1);
            flags.put("mobile", 1);
            flags.put("geometry", 0);

            long start = System.currentTimeMillis();
            resp = NetUtils.callFrpc(NetUtils.getApiUrl(), "getRoutesOpt", new Object[]{
                    time, true, params, flags,
            });

            Log.i(TAG, "Took " + (System.currentTimeMillis() - start) + "ms");

            ArrayList<String> trains = new ArrayList<>();

            AnucArray routes = resp.getArray("routes");
            for(int i = 0; i < routes.getLength(); ++i) {
                AnucStruct r = routes.getStruct(i);
                AnucArray items = r.getArray("items");

                trains.clear();
                int delayMinutes = 0;
                for(int x = 0; x < items.getLength(); ++x) {
                    AnucStruct it = items.getStruct(x);
                    if(it.containsKey("routeName")) {
                        trains.add(it.getString("routeName"));
                    }

                    if(it.containsKey("info")) {
                        AnucArray info = it.getArray("info");
                        for(int z = 0; z < info.getLength(); ++z) {
                            it = info.getStruct(z);
                            if(it.containsKey("delay")) {
                                delayMinutes += it.getLong("delay")/60;
                            }
                        }
                    }
                }

                DepartureInfo dep = new DepartureInfo();
                dep.depTime = new Date(r.getLong("departureMinute")*1000);
                dep.arrTime = new Date(r.getLong("arrivalMinute")*1000);
                dep.depStationDifferent = r.getLong("startStopID") != c.sznFromId;
                dep.arrStationDifferent = r.getLong("endStopID") != c.sznToId;
                dep.trains = trains.toArray(new String[trains.size()]);
                dep.delayMinutes = delayMinutes;
                res.add(dep);
            }
        } catch (AnucException e) {
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            if(resp != null && resp.containsKey("statusMessage")) {
                Log.e(TAG, "statusmessage: " + resp.getString("statusMessage"));
            }
            e.printStackTrace();
            return false;
        } finally {
            if(res.size() != 0)
                publishProgress(res);
        }
        return true;
    }
}
