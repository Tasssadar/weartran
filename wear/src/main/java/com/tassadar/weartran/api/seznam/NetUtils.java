package com.tassadar.weartran.api.seznam;

import cz.seznam.anuc.AnucClient;
import cz.seznam.anuc.AnucConfig;
import cz.seznam.anuc.AnucPair;
import cz.seznam.anuc.CallResponse;
import cz.seznam.anuc.MapAnucStruct;
import cz.seznam.anuc.ResponseData;
import cz.seznam.anuc.exceptions.AnucDataException;
import cz.seznam.anuc.exceptions.AnucException;
import cz.seznam.anuc.frpc.FrpcConnectionWrapper;
import cz.seznam.anuc.frpc.FrpcResponseData;
import cz.seznam.anuc.unmarschaller.AbstractDataUnmarschaller;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

public class NetUtils {

    public static class FrpcUnmarschaller extends AbstractDataUnmarschaller {
        private InputStream mInputStream;

        public FrpcUnmarschaller() {
            super();
        }

        public boolean checkContentType(String contentType) {
            return "application/x-frpc".equals(contentType);
        }

        public void fillRequestHeaders(Map arg3, Map arg4) {
            arg3.put("Accept", "application/x-frpc");
        }

        private int read() throws AnucDataException {
            int v1;
            try {
                v1 = this.mInputStream.read();
                if(v1 != -1) {
                    return v1;
                }
            }
            catch(IOException v2) {
                throw new AnucDataException("Error when reading data for unmarschaller: \n" + v2, ((
                        Throwable)v2));
            }

            throw new AnucDataException("Error when reading data for unmarschaller: No data in input stream\n",
                    null);
        }

        private int read(int offset) throws AnucDataException {
            Throwable v3 = null;
            int v4 = -1;
            int v1;
            for(v1 = 0; v1 < offset; ++v1) {
                if(this.read() == v4) {
                    throw new AnucDataException("Error when reading data for unmarschaller", v3);
                }
            }

            int v2 = this.read();
            if(v2 == v4) {
                throw new AnucDataException("Error when reading data for unmarschaller", v3);
            }

            return v2;
        }

        private Object[] unmarschallArray(int data) throws AnucException {
            int v3 = data & 7;
            int v2 = 0;
            int v0;
            for(v0 = 0; v0 <= v3; ++v0) {
                v2 |= this.read() << (v0 << 3);
            }

            Object[] v4 = new Object[v2];
            int v1;
            for(v1 = 0; v1 < v2; ++v1) {
                v4[v1] = this.unmarschallData();
            }

            return v4;
        }

        private ByteBuffer unmarschallBinary(int data) throws AnucDataException {
            int v3 = data & 7;
            int v2 = 0;
            int v0 = 0;
            while(v0 <= v3) {
                v2 |= this.read() << (v0 << 3);
                ++v0;
            }

            ByteBuffer v4 = null;
            if(v2 > 0) {
                v4 = ByteBuffer.allocate(v2);
                int v1 = 0;

                while(v1 < v2) {
                    v4.put(((byte)this.read()));
                    ++v1;
                }
                return v4;
            }

            return v4;
        }

        private Boolean unmarschallBoolean(int data) throws AnucDataException {
            boolean v0 = true;
            if((data & 1) != 1) {
                v0 = false;
            }

            return Boolean.valueOf(v0);
        }

        public Object unmarschallData() throws AnucException {
            int v0 = this.read();
            if(v0 == 202) {
                v0 = this.read(3);
            }

            switch(v0 & 248) {
                case 16: {
                    return this.unmarschallBoolean(v0);
                }
                case 24: {
                    return this.unmarschallDouble(v0);
                }
                case 32: {
                    return this.unmarschallString(v0);
                }
                case 40: {
                    return  this.unmarschallDateTime(v0);
                }
                case 48: {
                    return  this.unmarschallBinary(v0);
                }
                case 56: {
                    return this.unmarschallInt(v0);
                }
                case 64: {
                    return  Long.valueOf(-this.unmarschallInt(v0).longValue());
                }
                case 80: {
                    return  this.unmarschallStruct(v0);
                }
                case 88: {
                    return this.unmarschallArray(v0);
                }
                case 96: {
                    return  null;
                }
                case 112: {
                    return this.unmarschallData();
                }
                case 120: {
                    return this.unmarschallFault(v0);
                }
            }

            throw new AnucDataException("Error in unmarschalling: uknown frpc data type! " + (v0 & 248),
                    null);
        }

        public ResponseData unmarschallData(InputStream inputStream, String contentEncoding) throws
                AnucException {
            this.mInputStream = inputStream;
            Object v1 = this.unmarschallData();
            if((v1 instanceof Object[])) {
                HashMap v0 = new HashMap();
                ((Map)v0).put("results", v1);
                HashMap v1_1 = v0;
            }

            return new FrpcResponseData(MapAnucStruct.fromHashMap(((HashMap)v1)));
        }

        private GregorianCalendar unmarschallDateTime(int data) throws AnucDataException {
            long v8 = 0;
            this.read();
            int v2;
            for(v2 = 0; v2 <= 3; ++v2) {
                v8 |= ((long)(this.read() << (v2 << 3)));
            }

            this.read();
            this.read();
            this.read();
            this.read();
            this.read();
            GregorianCalendar v11 = new GregorianCalendar();
            v11.setTimeInMillis(1000 * v8);
            return v11;
        }

        private Double unmarschallDouble(int data) throws AnucDataException {
            long v0 = 0;
            int v2;
            for(v2 = 0; v2 < 8; ++v2) {
                v0 |= (((long)this.read())) << (v2 << 3);
            }

            return Double.valueOf(Double.longBitsToDouble(v0));
        }

        private HashMap unmarschallFault(int data) throws AnucException {
            HashMap v0 = new HashMap();
            v0.put("status", this.unmarschallData());
            v0.put("statusMessage", this.unmarschallData());
            return v0;
        }

        private Long unmarschallInt(int data) throws AnucDataException {
            int v1 = data & 7;
            long v2 = 0;
            int v0;
            for(v0 = 0; v0 <= v1; ++v0) {
                v2 |= (((long)this.read())) << (v0 << 3);
            }

            return Long.valueOf(v2);
        }

        private String unmarschallString(int data) throws AnucDataException {
            String v7;
            int v3 = data & 7;
            int v2 = 0;
            int v0;
            for(v0 = 0; v0 <= v3; ++v0) {
                v2 |= this.read() << (v0 << 3);
            }

            ByteBuffer v6 = ByteBuffer.allocate(v2);
            int v4;
            for(v4 = 0; v4 < v2; ++v4) {
                v6.put(((byte)this.read()));
            }

            try {
                v7 = new String(v6.array(), "UTF-8");
            }
            catch(UnsupportedEncodingException v5) {
                v7 = "Chyba pri enkodovani";
            }

            return v7;
        }

        private HashMap unmarschallStruct(int data) throws AnucException {
            String v9;
            int v5 = data & 7;
            int v2 = 0;
            int v6;
            for(v6 = 0; v6 <= v5; ++v6) {
                v2 |= this.read() << (v6 << 3);
            }

            HashMap v8 = new HashMap();
            int v0;
            for(v0 = 0; v0 < v2; ++v0) {
                int v4 = this.read();
                ByteBuffer v3 = ByteBuffer.allocate(v4);
                int v1;
                for(v1 = 0; v1 < v4; ++v1) {
                    v3.put(((byte)this.read()));
                }

                try {
                    v9 = new String(v3.array(), "UTF-8");
                }
                catch(UnsupportedEncodingException v7) {
                    v9 = "Chyba pri enkodovani";
                }

                v8.put(v9, this.unmarschallData());
            }

            return v8;
        }
    }

    public static AnucConfig anucConfig = null;

    static {
        NetUtils.anucConfig = new AnucConfig();
        NetUtils.anucConfig.setAttemptCount(1);
        NetUtils.anucConfig.setReadTimeout(15000);
    }

    public NetUtils() {
        super();
    }

    public static MapAnucStruct callFrpc(String url, String method, Object[] params) throws AnucException {
        CallResponse resp = new AnucClient(NetUtils.anucConfig, new FrpcConnectionWrapper(method, params), new FrpcUnmarschaller())
                .post(url);
        return ((FrpcResponseData)resp.data).data;
    }

    public static String getApiUrl() {
        return "http://mapy.cz/routept";
    }
}
