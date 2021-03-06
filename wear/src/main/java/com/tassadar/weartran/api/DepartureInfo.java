package com.tassadar.weartran.api;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

public class DepartureInfo {
    public DepartureInfo() {
        delayMinutes = -1; // not loaded
    }

    public Date depTime;
    public Date arrTime;
    public boolean depStationDifferent;
    public boolean arrStationDifferent;
    public String[] trains;
    public int delayMinutes;

    public String idosDelayQuery;
    public int idosConnId;

    public static byte[] serialize(List<DepartureInfo> departures) {
        ByteArrayOutputStream bs = null;
        ObjectOutputStream out = null;
        try {
            bs = new ByteArrayOutputStream();
            out = new ObjectOutputStream(bs);
            out.writeInt(departures.size());

            for(DepartureInfo i : departures) {
                out.writeObject(i.depTime);
                out.writeObject(i.arrTime);
                out.writeInt(i.trains.length);
                for(String tr : i.trains) {
                    out.writeUTF(tr);
                }
                out.writeBoolean(i.depStationDifferent);
                out.writeBoolean(i.arrStationDifferent);
                out.writeInt(i.delayMinutes);
            }
            out.close();
            out = null;
            return bs.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(out != null) { try { out.close(); } catch(IOException e) { e.printStackTrace(); } }
            if(bs != null) { try { bs.close(); } catch(IOException e) { e.printStackTrace(); } }
        }
        return null;
    }

    public static void deserialize(byte[] data, List<DepartureInfo> departures) {
        ByteArrayInputStream bs = null;
        ObjectInputStream in = null;
        try {
            bs = new ByteArrayInputStream(data);
            in = new ObjectInputStream(bs);

            final int count = in.readInt();
            for(int i = 0; i < count; ++i) {
                DepartureInfo inf = new DepartureInfo();
                inf.depTime = (Date)in.readObject();
                inf.arrTime = (Date)in.readObject();

                final int trainsCount = in.readInt();
                inf.trains = new String[trainsCount];
                for(int x = 0; x < trainsCount; ++x) {
                    inf.trains[x] = in.readUTF();
                }

                inf.depStationDifferent = in.readBoolean();
                inf.arrStationDifferent = in.readBoolean();;
                inf.delayMinutes = in.readInt();
            }

        } catch (IOException|ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            if(in != null) { try { in.close(); } catch(IOException e) { e.printStackTrace(); } }
            if(bs != null) { try { bs.close(); } catch(IOException e) { e.printStackTrace(); } }
        }
    }
}
