package com.wt.pinger.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.Html;
import android.text.Spanned;

import androidx.annotation.NonNull;

import com.hivedi.console.Console;
import com.wt.pinger.BuildConfig;

/**
 * Created by Kenumir on 2016-08-12.
 *
 */
public class SystemCompat {

    public static void copyToClipboard(@NonNull Context ctx, String data) {
        if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
            android.text.ClipboardManager clipboard = (android.text.ClipboardManager) ctx.getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setText(data);
        } else {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) ctx.getSystemService(Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("Copied Text", data);
            clipboard.setPrimaryClip(clip);
        }
    }

    /**
     * add try catch for error handling
     * @param ctx app context
     * @param url url to open
     * @return true if no error while open, false otherwise
     */
    public static boolean openInBrowser(@NonNull Context ctx, @NonNull String url) {
        try {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            ctx.startActivity(browserIntent);
            return true;
        } catch (Exception ignore) {
            if (BuildConfig.DEBUG) {
                Console.loge("VariousHelper.openInBrowser: " + ignore.toString(), ignore);
            }
        }
        return false;
    }

    public static Spanned toHtml(String s) {
        if(android.os.Build.VERSION.SDK_INT >= 24) {
            return Html.fromHtml(s, Html.FROM_HTML_MODE_LEGACY);
        } else {
            return Html.fromHtml(s);
        }
    }
}
