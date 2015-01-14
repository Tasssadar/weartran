package com.tassadar.weartran;

import android.util.Log;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.SoapFault;
import org.ksoap2.SoapFault12;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class IdosApi {
    private static final String TAG = "Weartran:IdosApi";

    private static final String NAMESPACE = "http://ttws.chaps.cz/TT";
    private static final String URL = "http://ttws.timetable.cz/TT.asmx";


    public static abstract class Credentials {
        public abstract String getLogin();
        public abstract String getPassword();
    }

    public IdosApi(String oldSessionId, Credentials credentials) {
        m_sessionId = oldSessionId;
        m_credentials = credentials;
        m_transport = new HttpTransportSE(URL);
        m_transport.setXmlVersionTag("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
    }

    private Object callMethod(SoapObject request) throws XmlPullParserException, IOException {
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER12);
        envelope.dotNet = true; // lol
        envelope.implicitTypes = true;
        envelope.setOutputSoapObject(request);
        m_transport.call(NAMESPACE + "/" + request.getName(), envelope);
        return envelope.bodyIn;
    }

    public boolean login() {
        SoapObject request = new SoapObject(NAMESPACE, "Login");
        request.addProperty("sUserName", m_credentials.getLogin());
        request.addProperty("sPassword", m_credentials.getPassword());

        try {
            Object res = callMethod(request);
            if(res instanceof SoapFault12) {
                Log.e(TAG, "Login failed: " + ((SoapFault)res).faultstring);
                return false;
            }
            SoapObject loginres = (SoapObject) ((SoapObject)res).getProperty("LoginResult");
            m_sessionId = loginres.getPropertyAsString("sSessionID");
            Log.i(TAG, "Got session id: " + m_sessionId);
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean logoff() {
        if(m_sessionId == null)
            return true;

        SoapObject logoff = new SoapObject(NAMESPACE, "Logoff");
        logoff.addProperty("sSessionID", m_sessionId);
        try {
            Object res = callMethod(logoff);
            if(res instanceof SoapFault12) {
                Log.e(TAG, "Failed to logoff: " + ((SoapFault)res).faultstring);
                return false;
            }
        }  catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private SoapObject createStationInfo(final String tagName, final String station) {
        SoapObject tag = new SoapObject("", tagName);
        SoapObject VirtListItemInfo = new SoapObject("", "VirtListItemInfo");
        VirtListItemInfo.addAttribute("sName", "!" + station);
        tag.addSoapObject(VirtListItemInfo);
        return tag;
    }

    public SoapObject buildConnection2Req(final String from, final String to, final Date date, boolean isDepTime, int maxCount) {
        SoapObject request = new SoapObject(NAMESPACE, "Connection2");
        request.addProperty("sSessionID", m_sessionId);
        //request.addProperty("sUserDesc", "Pubtran.xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx");
        request.addProperty("iRemMask", 0);
        request.addProperty("iTTDetails", 2101264);
        request.addProperty("iMaxCount", maxCount);
        request.addProperty("sCombID", "IDSJMK"); // dopravni podnik

        request.addSoapObject(createStationInfo("aoFrom", from));
        request.addSoapObject(createStationInfo("aoTo", to));

        SimpleDateFormat fmtdate = new SimpleDateFormat("dd.MM.yyyy");
        SimpleDateFormat fmttime = new SimpleDateFormat("HH:mm");
        request.addProperty("sDate", fmtdate.format(date));
        request.addProperty("sTime", fmttime.format(date));
        request.addProperty("iAlgorithm", 1);
        request.addProperty("bIsDepTime", isDepTime);

        // wtf
        SoapObject aiTypeID = new SoapObject("", "aiTypeID");
        final int vals[] = {
            155, 301, 302, 201, 151, 312, 202, 311, 309, 300, 307, 154, 306,
            400, 156, 200, 303, 308, 304, 100, 152, 305, 153, 310, 150
        };
        for(int i : vals)
            aiTypeID.addProperty("i", i);
        request.addSoapObject(aiTypeID);

        SoapObject oParms = new SoapObject("", "oParms");
        oParms.addAttribute("bLowDeckConn", false);
        oParms.addAttribute("bLowDeckConnTr", false);
        oParms.addAttribute("iMaxArcLengthCity", 10);
        oParms.addAttribute("bUseSearchCoors", true);
        oParms.addAttribute("bLimitOnTarget", true);
        oParms.addAttribute("bLowDeckConnValid", true);
        oParms.addAttribute("bStopsInVia", true);
        oParms.addAttribute("iDeltaMaxType", 0);
        oParms.addAttribute("iDeltaPMax", 250);
        oParms.addAttribute("iMaxChange", 4);
        oParms.addAttribute("iIntervalDirect", 180);
        oParms.addAttribute("iIntervalChanges", 120);
        oParms.addAttribute("iDeltaMMax", 10000);
        oParms.addAttribute("iMaxArcLength", 60);
        oParms.addAttribute("iFailRetryDirect", 5);
        oParms.addAttribute("iFailRetryChanges", 2);
        oParms.addProperty("aiMaxTime", 60);
        oParms.addProperty("aiMinTime", 1);
        oParms.addProperty("abDefaultMinTime", true);
        request.addSoapObject(oParms);

        request.addProperty("iSearchMode", 0);
        request.addProperty("iTTInfoDetails", 1);
        request.addProperty("iConnHandle", -1);
        return request;
    }

    public String getSessionId() {
        return m_sessionId;
    }

    public static class DepartureInfo {
        String depTime;
        String arrTime;
        String[] trains;
        int connId;
    }

    public final static SimpleDateFormat DEPARTURES_TIME_FMT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    private int parseDeparturesInfo(SoapObject conns, ArrayList<DepartureInfo> output) {
        final int cnt = conns.getPropertyCount();
        int res = 0;
        for(int i = 0; i < cnt; ++i) {
            Object po = conns.getProperty(i);
            if(!(po instanceof SoapObject) || !((SoapObject) po).hasProperty("aoTrains"))
                continue;

            DepartureInfo info = new DepartureInfo();
            ArrayList<String> trains = new ArrayList<>();

            SoapObject connInfo = (SoapObject)po;
            final int cntInfo = connInfo.getPropertyCount();
            for(int y = 0; y < cntInfo; ++y) {
                po = connInfo.getProperty(y);
                if(!(po instanceof SoapObject) || !((SoapObject) po).hasProperty("oTrain"))
                    continue;

                SoapObject tr = (SoapObject)po;
                if(info.depTime == null)
                    info.depTime = tr.getAttributeAsString("dtDateTime1");
                info.arrTime = tr.getAttributeAsString("dtDateTime2");
                trains.add(((SoapObject) tr.getProperty("oTrain")).getAttributeAsString("sNum1"));
            }

            if(!trains.isEmpty()) {
                info.trains = new String[trains.size()];
                for (int t = 0; t < info.trains.length; ++t)
                    info.trains[t] = trains.get(t);
                info.connId = Integer.parseInt(connInfo.getAttributeAsString("iID"));
                output.add(info);
                ++res;

                Log.i(TAG, "Got departure " + info.depTime + " -> " + info.arrTime + ", trains " + trains);
            }
        }
        return res;
    }

    private SoapObject getConnectionsPage(int connHandle, int prevConnId, boolean prev, int maxCount) throws IOException, XmlPullParserException {
        SoapObject request = new SoapObject(NAMESPACE, "GetConnectionsPage");
        request.addProperty("sSessionID", m_sessionId);
        //request.addProperty("sUserDesc", "Pubtran.xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx");
        request.addProperty("iRemMask", 0);
        request.addProperty("iTTDetails", 2101264);
        request.addProperty("iMaxCount", maxCount);
        request.addProperty("sCombID", "IDSJMK"); // dopravni podnik
        request.addProperty("iConnHandle", connHandle);
        request.addProperty("bPrevConn", prev);
        request.addProperty("iConnID", prevConnId);
        Object o = callMethod(request);
        if(o instanceof SoapFault12) {
            Log.e(TAG, "Failed to call GetConnectionsPage: " + ((SoapFault12) o).faultstring);
            return null;
        }
        return (SoapObject)o;
    }

    public DepartureInfo[] getDepartures(final String from, final String to, final Date date, int count) {
        ArrayList<DepartureInfo> output = new ArrayList<>();
        try {
            Object res = null;
            for (int i = 0; i < 2; ++i) {
                SoapObject req = buildConnection2Req(from, to, date, true, count);
                res = callMethod(req);
                if (res instanceof SoapFault12) {
                    SoapFault f = (SoapFault12) res;
                    if (i == 0 && f.faultcode.equals("CL1001")) {
                        login();
                    } else {
                        Log.e(TAG, "Failed to get departures: " + f.faultstring);
                        return null;
                    }
                }
            }

            SoapObject connRes = (SoapObject) ((SoapObject)res).getProperty("Connection2Result");
            Log.i(TAG, "Got Connection2Result: " + connRes.toString());

            if(!connRes.hasProperty("oConnInfo")) {
                Log.e(TAG, "Connection2Result doesn't have oConnInfo, bad stop names?");
                return null;
            }

            SoapObject oConnInfo = (SoapObject) connRes.getProperty("oConnInfo");
            parseDeparturesInfo(oConnInfo, output);

            if(!output.isEmpty()) {
                int connHandle = Integer.parseInt(((SoapObject) connRes.getProperty("oConnInfo")).getAttributeAsString("iHandle"));
                for (int attempts = 0; output.size() < count && attempts < 50; ++attempts) {
                    SoapObject page = getConnectionsPage(connHandle,
                            output.get(output.size()-1).connId, false,
                            Math.min(3, count - output.size()));

                    if(page == null)
                        break;

                    page = (SoapObject) page.getProperty("GetConnectionsPageResult");
                    if(parseDeparturesInfo(page, output) == 0)
                        break;

                    connHandle = Integer.parseInt(page.getAttributeAsString("iHandle"));
                }
            }
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
            return null;
        }

        DepartureInfo[] outFinal = new DepartureInfo[output.size()];
        for(int i = 0; i < outFinal.length; ++i)
            outFinal[i] = output.get(i);
        return outFinal;
    }

    private String m_sessionId;
    private Credentials m_credentials;
    private HttpTransportSE m_transport;
}
