package com.wt.pinger.providers.data;

import android.database.Cursor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Created by Kenumir on 2016-08-12.
 *
 */
public class NetworkInfo {

    public static final String COL_ID = "_id";
    public static final String COL_NAME = "name";
    public static final String COL_MTU = "mtu";
    public static final String COL_MAC = "mac";
    public static final String COL_IPV4 = "ipv4";
    public static final String COL_IPV6 = "ipv6";

    public long _id;
    public String name;
    public String mtu;
    public String mac;
    public String ipv4;
    public String ipv6;

    public String getIP() {
        String ip = "";
        if (ipv4.length() > 0) {
            ip += "IPv4: " + ipv4;
        }
        if (ipv6.length() > 0) {
            if (ip.length() > 0) {
                ip += "\n";
            }
            ip += "IPv6: " + ipv6;
        }
        return ip;
    }

    @Nullable
    public static NetworkInfo fromCursor(@NonNull Cursor c) {
        NetworkInfo res = null;
        if (!c.isAfterLast() && !c.isBeforeFirst() && !c.isClosed()) {
            res = new NetworkInfo();
            res._id = c.getLong(c.getColumnIndex(COL_ID));
            res.name = c.getString(c.getColumnIndex(COL_NAME));
            res.mtu = c.getString(c.getColumnIndex(COL_MTU));
            res.mac = c.getString(c.getColumnIndex(COL_MAC));
            res.ipv4 = c.getString(c.getColumnIndex(COL_IPV4));
            res.ipv6 = c.getString(c.getColumnIndex(COL_IPV6));
        }
        return res;
    }
}
