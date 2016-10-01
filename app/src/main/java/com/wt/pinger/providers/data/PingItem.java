package com.wt.pinger.providers.data;

import android.support.annotation.Nullable;

import com.hivedi.console.Console;
import com.hivedi.era.ERA;
import com.wt.pinger.BuildConfig;
import com.wt.pinger.proto.DataFieldAnnotation;
import com.wt.pinger.proto.ItemProto;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Kenumir on 2016-09-07.
 *
 * ----------------- sample lines to parse ----------------------
 * From 192.168.1.112: icmp_seq=1 Destination Host Unreachable
 * ping: unknown host wpx.lll
 * From 192.168.1.5 icmp_seq=2 Destination Host Unreachable
 * From 172.1.0.1: icmp_seq=1 Destination Net Unreachable
 * connect: Network is unreachable
 * ping: icmp open socket: Operation not permitted
 * From 10.20.10.1: icmp_seq=7 Destination Net Prohibited
 * From 210.213.128.37 icmp_seq=11792 Time to live exceeded
 * From 178.134.240.62: icmp_seq=1 Packet filtered
 * connect: Invalid argument
 * From 192.168.43.47: icmp_seq=2 Redirect Network(New nexthop: 192.168.43.210)
 * From 100.100.100.1 icmp_seq=10 Dest Unreachable, Bad Code: 9
 * From 192.168.1.1: icmp_seq=116 Source Quench
 * wrong data byte #12 should be 0xc but was 0x45
 * 40 bytes from 125.5.3.74: icmp_seq=14790 ttl=54 (truncated)
 * From 172.20.1.19: icmp_seq=3527 Redirect Host(New nexthop: 172.20.1.88)
 * From 10.22.132.1: icmp_seq=919 Destination Port Unreachable
 * 13 bytes from 112.215.88.25: icmp_seq=65 ttl=57
 * 9 bytes from 10.1.89.130: icmp_seq=3 ttl=254
 * 9 bytes from sin01s15-in-f19.1e100.net (173.194.117.51): icmp_seq=571 ttl=55
 * #40	54 64 f9 0 0 36 1 69 1 8 8 8 8 a 6d 9c
 * From 172.25.254.10: icmp_seq=6382 Destination Host Prohibited
 * From kul06s07-in-f19.1e100.net (173.194.120.115) icmp_seq=1705 Frag reassembly time exceeded
 * rtt min/avg/max/mdev = 15.204/46.458/167.677/60.614 ms
 * ----------------------------------
 */
public class PingItem extends ItemProto {

    public static final String FIELD_TTL = "ttl";
    public static final String FIELD_SEQ = "seq";
    public static final String FIELD_TIME = "time";
    public static final String FIELD_INFO = "info";
    public static final String FIELD_TIMESTAMP = "timestamp";
    public static final String FIELD_ADDRESS_ID = "addressId";

    private static final Pattern pingPattern = Pattern.compile("(\\w+)=([^\\s]+)");

    @DataFieldAnnotation public Long timestamp;
    @DataFieldAnnotation public Integer ttl;
    @DataFieldAnnotation public Integer seq;
    @DataFieldAnnotation public Double time;
    @DataFieldAnnotation public String info;
    @DataFieldAnnotation public Long addressId;

    private static int parseInt(String s) {
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            return 0;
        }
    }

    private static double parseDouble(String s) {
        try {
            return Double.parseDouble(s);
        } catch (Exception e) {
            return 0D;
        }
    }

    @Nullable
    public static PingItem parse(@Nullable String line, @Nullable Long addressId) {
        if (BuildConfig.DEBUG) {
            Console.logd("PARSE: " + line);
        }
        if (line == null || line.trim().length() == 0 || line.startsWith("PING ")) {
            return null;
        }
        if (line.contains("min") && line.contains("avg") && line.contains("max")) {
            PingItem res = new PingItem();
            res.info = line.trim();
            res.timestamp = System.currentTimeMillis();
            return res;
        }
        if (line.contains("statistics")) {
            PingItem res = new PingItem();
            res.info = line.trim();
            res.timestamp = System.currentTimeMillis();
            return res;
        }
        try {
            PingItem res = new PingItem();
            Matcher m = pingPattern.matcher(line);
            while (m.find()) {
                switch (m.group(1)) {
                    case "seq":
                    case "icmp_seq":
                        res.seq = parseInt(m.group(2));
                        break;
                    case "ttl":
                        res.ttl = parseInt(m.group(2));
                        break;
                    case "time":
                        res.time = parseDouble(m.group(2));
                        break;
                }
            }
            res.timestamp = System.currentTimeMillis();
            res.addressId = addressId;

            if(res.time == null || res.time == 0D) {
                if (res.seq != null && res.seq > 0) {
                    String seq = "seq=" + res.seq;
                    int pos = line.indexOf(seq);
                    if (pos > -1) {
                        res.info = line.substring(pos + seq.length()).trim();
                    } else {
                        res.info = line;
                    }
                } else {
                    res.info = line;
                }
            }

            return res;
        } catch (Exception e) {
            ERA.logException(new Exception("Parse error. Line=" + line, e));
            if (BuildConfig.DEBUG) {
                Console.loge("Parse error: " + e, e);
            }
        }
        return null;
    }

    public boolean isDataValid() {
        return info == null;
    }

}
