package com.wt.pinger.utils;

import android.os.Process;
import android.support.annotation.Nullable;

import com.wt.pinger.providers.data.PingItem;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Created by Kenumir on 2016-08-12.
 *
 */
public class PingProgram {

    private static String pingExec = "ping";
    public static void setPingExec(String e) {
        pingExec = e;
    }

    public static boolean hasValidPingExec() {
        return pingExec != null;
    }

    public static class Builder {

        /**
         Usage: ping [-aAbBdDfhLnOqrRUvV] [-c count] [-i interval] [-I interface]
                [-m mark] [-M pmtudisc_option] [-l preload] [-p pattern] [-Q tos]
                [-s packetsize] [-S sndbuf] [-t ttl] [-T timestamp_option]
                [-w deadline] [-W timeout] [hop1 ...] destination
         */

        private String address;
        private int count = 0; // [-c count]
        private int interval = 0; // [-i interval]
        private int packet = 0; // [-s packetsize]
        private OnPingListener listener;

        public Builder address(String a) {
            address = a;
            return this;
        }

        public Builder count(int c) {
            count = c;
            return this;
        }

        public Builder packetSize(int c) {
            packet = c;
            return this;
        }

        public Builder interval(int c) {
            interval = c;
            return this;
        }

        public Builder listener(OnPingListener l) {
            listener = l;
            return this;
        }

        public PingProgram build() {
            return new PingProgram(address, count, interval, packet)
                .setOnPingListener(listener);
        }
    }

    public interface OnPingListener {
        void onStart();
        void onResult(@Nullable PingItem data);
        void onError(String er);
        void onFinish();
    }

    private String address;
    private int count = 0;
    private int interval = 0;
    private int packet = 0;
    private java.lang.Process process;
    private OnPingListener mOnPingListener;

    private PingProgram(String a, int c, int i, int p) {
        address = a;
        count = c;
        interval = i;
        packet = p;
    }

    public PingProgram setOnPingListener(OnPingListener l) {
        mOnPingListener = l;
        return this;
    }

    public void start() {
        new Thread(){
            @Override
            public void run() {
                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                Thread.currentThread().setName("Ping thread");

                if (mOnPingListener != null) {
                    mOnPingListener.onStart();
                }

                StringBuilder sb = new StringBuilder();
                sb.append(pingExec);
                if (count > 0) {
                    sb.append(" -c ").append(count);
                }
                if (interval > 0) {
                    sb.append(" -i ").append(interval);
                }
                if (packet > 0) {
                    sb.append(" -s ").append(packet);
                }
                sb.append(" ").append(address);

                try {
                    process = Runtime.getRuntime().exec(sb.toString());
                    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    String line;
                    line = reader.readLine();
                    if (line != null) {
                        if (mOnPingListener != null) {
                            mOnPingListener.onResult(PingItem.parse(line, null));
                        }
                        while ((line = reader.readLine()) != null) {
                            if (mOnPingListener != null) {
                                mOnPingListener.onResult(PingItem.parse(line, null));
                            }
                        }
                    } else {
                        BufferedReader readerError = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                        String er = "";
                        while ((line = readerError.readLine()) != null) {
                            er += line + "\n";
                        }
                        if (mOnPingListener != null && er.trim().length() > 0) {
                            mOnPingListener.onError(er.trim());
                        }
                        readerError.close();
                    }

                    reader.close();
                    process.destroy();
                } catch (Exception e) {
                    if (mOnPingListener != null) {
                        mOnPingListener.onError(null);
                    }
                }

                if (mOnPingListener != null) {
                    mOnPingListener.onFinish();
                }
            }
        }.start();
    }

    public void terminate() {
        if (process != null) {
            process.destroy();
        }
    }

}
