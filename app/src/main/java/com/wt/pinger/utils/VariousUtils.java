package com.wt.pinger.utils;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.hivedi.era.ERA;

import java.security.MessageDigest;
import java.util.Locale;

public class VariousUtils {

    @NonNull
    @SuppressLint("PackageManagerGetSignatures")
    public static String getAppFingerprint(@NonNull Context ctx) {
        final PackageManager pm = ctx.getPackageManager();
        try {
            /*
             * Może niestety zdazyć się błąd typu:
             * RuntimeException Package manager has died
             * Caused By: android.os.DeadObjectException
             * Pojawia sie on gdy PackageManager z jakiegos powodu zawiesił się,
             * mogła spowodować to inna aplikacja, w naszym przypadku zapytanie ograniczone
             * jest do minimum, pytamy tylko o nasz package + sygantury więc mniej już nie można
             */
            PackageInfo info = pm.getPackageInfo(ctx.getPackageName(), PackageManager.GET_SIGNATURES);
            String[] appCert = new String[info.signatures.length];
            int i = 0;
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                StringBuilder s = new StringBuilder();
                for (byte b : md.digest()) {
                    s.append(":").append(String.format("%02x", b));
                }
                appCert[i] = s.substring(1).toUpperCase(Locale.getDefault());
                i++;
            }
            return flatArray(appCert, ", ");
        } catch (Throwable e) {
            ERA.logException(e);
            return "Error " + e;
        }
    }

    @NonNull
    public static String getAppInstallerPackage(@NonNull Context ctx) {
        try {
            String res = ctx.getPackageManager().getInstallerPackageName(ctx.getPackageName());
            return TextUtils.isEmpty(res) ? "Empty" : res;
        } catch (Throwable e) {
            return "Error " + e;
        }
    }

    public static boolean isTestLabDevice(@NonNull Context ctx) {
        try {
            // INFO sprawdzenie czy to urządzenie to test lab
            // https://stackoverflow.com/a/43598581/959086
            // https://firebase.google.com/docs/test-lab/android/android-studio#modify_instrumented_test_behavior_for_testlab
            String testLabSetting = Settings.System.getString(ctx.getContentResolver(), "firebase.test.lab");
            return "true".equals(testLabSetting);
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isBackgroundRestricted(@NonNull Context ctx) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ActivityManager am = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
            if (am != null) {
                return am.isBackgroundRestricted();
            }
        }
        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    @NonNull
    public static String getAppStandbyBucketName(@NonNull Context ctx) {
        try {
            UsageStatsManager sm = (UsageStatsManager) ctx.getSystemService(Context.USAGE_STATS_SERVICE);
            if (sm != null) {
                int bucketInt = sm.getAppStandbyBucket();
                String bucket = "Unknown (" + bucketInt + ")";
                switch (bucketInt) {
                    case UsageStatsManager.STANDBY_BUCKET_ACTIVE:
                        bucket = "BUCKET_ACTIVE";
                        break;
                    case UsageStatsManager.STANDBY_BUCKET_RARE:
                        bucket = "BUCKET_RARE";
                        break;
                    case UsageStatsManager.STANDBY_BUCKET_WORKING_SET:
                        bucket = "BUCKET_WORKING_SET";
                        break;
                    case UsageStatsManager.STANDBY_BUCKET_FREQUENT:
                        bucket = "BUCKET_FREQUENT";
                        break;
                }
                return bucket;
            } else {
                return "UsageStatsManager is NULL";
            }
        } catch (Exception e) {
            return "Error " + e;
        }
    }

    public static String flatArray(String[] s, String separator) {
        StringBuilder res = new StringBuilder();
        if (s != null && s.length > 0) {
            for(String i : s) {
                res.append(i).append(separator == null ? "" : separator);
            }
            res = new StringBuilder(res.substring(0, res.length() - (separator == null ? 0 : separator.length())));
        }
        return res.toString();
    }

}
