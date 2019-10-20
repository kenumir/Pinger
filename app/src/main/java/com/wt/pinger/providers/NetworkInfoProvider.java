package com.wt.pinger.providers;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;

import androidx.annotation.NonNull;

import com.wt.pinger.providers.data.NetworkInfo;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

public class NetworkInfoProvider extends ContentProvider {

    private static final String PROVIDER_AUTHORITY = "com.wt.pinger.providers.networkinfo";
    public static final Uri URI_CONTENT = Uri.parse("content://" + PROVIDER_AUTHORITY + "/");

    public NetworkInfoProvider() {
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        // Implement this to handle requests to delete one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public String getType(@NonNull Uri uri) {
        // TODO: Implement this to handle requests for the MIME type of the data
        // at the given URI.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        // TODO: Implement this to handle requests to insert a new row.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public boolean onCreate() {
        // TODO: Implement this to initialize your content provider on startup.
        return true;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        if (uri.equals(URI_CONTENT)) {

            try {
                MatrixCursor res = new MatrixCursor(new String[]{NetworkInfo.COL_ID, NetworkInfo.COL_NAME, NetworkInfo.COL_MTU, NetworkInfo.COL_MAC, NetworkInfo.COL_IPV4, NetworkInfo.COL_IPV6});
                List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
                long _id = 1;
                for (NetworkInterface intf : interfaces) {
                    String name = intf.getName();

                    /**
                     * wlan - wlan0 - wifi
                     * rmnet - rmnet_... - mobile
                     */

                    if (intf.isUp() && name.startsWith("wlan") || name.startsWith("rmnet")) {
                        List<InetAddress> addrs = Collections.list(intf.getInetAddresses());

                        if (addrs.size() == 0) {
                            continue;
                        }

                        String macAdr = null;
                        byte[] mac = intf.getHardwareAddress();
                        if (mac != null) {
                            StringBuilder buf = new StringBuilder();
                            for (byte b : mac) {
                                buf.append(String.format(":%02X", b));
                            }
                            macAdr = buf.substring(1);
                        }

                        String ipv4 = "", ipv6 = "";
                        for (InetAddress a : addrs) {
                            String hostnameAddress = a.getHostAddress();
                            if (hostnameAddress.indexOf(':') < 0) {
                                ipv4 += "," + hostnameAddress;
                            } else {
                                int percentChar = hostnameAddress.indexOf('%');
                                ipv6 += "," + (percentChar < 0 ? hostnameAddress.toUpperCase() : hostnameAddress.substring(0, percentChar).toUpperCase());
                            }
                        }

                        if (ipv4.length() > 0) {
                            ipv4 = ipv4.substring(1);
                        }

                        if (ipv6.length() > 0) {
                            ipv6 = ipv6.substring(1);
                        }

                        res.addRow(new Object[]{
                                _id,
                                name,
                                intf.getMTU(),
                                macAdr,
                                ipv4,
                                ipv6
                        });
                    }
                    _id++;
                }

                return res;
            } catch (Exception ignore) {
                return null;
            }
        }
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        // TODO: Implement this to handle requests to update one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
