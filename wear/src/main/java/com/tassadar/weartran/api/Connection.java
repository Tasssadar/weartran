package com.tassadar.weartran.api;

import java.io.Serializable;

/**
 * Created by tassadar on 9.4.17.
 */

public class Connection implements Serializable {
    public Connection(String idosDp, String from, String to,
                      long fromId, double fromX, double fromY, long toId, double toX, double toY) {
        this.from = from;
        this.to = to;
        this.idosDp = idosDp;
        this.sznFromId = fromId;
        this.sznFromX = fromX;
        this.sznFromY = fromY;
        this.sznToId = toId;
        this.sznToX = toX;
        this.sznToY = toY;
    }

    public String from;
    public String to;
    public String idosDp;
    public long sznFromId;
    public long sznToId;
    public double sznFromX;
    public double sznFromY;
    public double sznToX;
    public double sznToY;
}
