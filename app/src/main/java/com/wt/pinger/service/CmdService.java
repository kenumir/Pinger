package com.wt.pinger.service;

import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.hivedi.console.Console;
import com.wt.pinger.BuildConfig;
import com.wt.pinger.providers.CmdContentProvider;
import com.wt.pinger.utils.BusProvider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class CmdService extends Service {

    private static final String PARAM_CMD = "cmd";

    private static final String ACTION_CMD = "com.wt.pinger.action.CMD";
    private static final String ACTION_CHECK = "com.wt.pinger.action.CHECK";

    public static void executeCmd(Context ctx, String cmd) {
        Intent it = new Intent(ctx, CmdService.class);
        it.setAction(ACTION_CMD);
        it.putExtra(PARAM_CMD, cmd);
        ctx.startService(it);
    }

    public static void checkService(Context ctx) {
        Intent it = new Intent(ctx, CmdService.class);
        it.setAction(ACTION_CHECK);
        ctx.startService(it);
    }

    public static final int CMD_MSG_CHECK = 1;

    public static class CmdServiceMessage {
        public int type = 0;
        public Object data;
        public CmdServiceMessage(int t) {
            type = t;
        }
        public CmdServiceMessage setData(Object d) {
            data = d;
            return this;
        }

        public boolean getDataAsBool(boolean defaultValue) {
            if (data instanceof Boolean) {
                return (boolean) data;
            }
            return defaultValue;
        }
    }

    private final Object processThreadSync = new Object();
    private ProcessThread mProcessThread;

    public CmdService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (action != null) {
                switch (action) {
                    case ACTION_CMD:
                        synchronized (processThreadSync) {
                            if (mProcessThread != null) {
                                mProcessThread.stopProcess();
                                mProcessThread = null;
                            } else {
                                if (intent.hasExtra(PARAM_CMD)) {
                                    final String cmd = intent.getStringExtra(PARAM_CMD);

                                    mProcessThread = new ProcessThread(cmd, new OnProcessListener() {
                                        @Override
                                        public void onStart() {
                                            BusProvider.getInstance().post(new CmdServiceMessage(CMD_MSG_CHECK).setData(true));
                                            if (BuildConfig.DEBUG) {
                                                Console.logd("onStart");
                                            }
                                            getContentResolver().delete(CmdContentProvider.URI_CONTENT, null, null);
                                        }

                                        @Override
                                        public void onResult(String out) {
                                            if (BuildConfig.DEBUG) {
                                                Console.logd("onResult: " + out);
                                            }
                                            ContentValues cv = new ContentValues();
                                            cv.put("data", out);
                                            getContentResolver().insert(CmdContentProvider.URI_CONTENT, cv);
                                        }

                                        @Override
                                        public void onFinish() {
                                            mProcessThread = null;
                                            if (BuildConfig.DEBUG) {
                                                Console.logd("onFinish");
                                            }
                                            getContentResolver().notifyChange(CmdContentProvider.URI_CONTENT, null);
                                            BusProvider.getInstance().post(new CmdServiceMessage(CMD_MSG_CHECK).setData(false));
                                        }
                                    });
                                    mProcessThread.start();
                                }
                            }
                        }
                        break;
                    case ACTION_CHECK:
                        BusProvider.getInstance().post(new CmdServiceMessage(CMD_MSG_CHECK).setData(isWorking()));
                        break;
                }
            }
        }
        return START_NOT_STICKY;
    }

    private boolean isWorking() {
        synchronized (processThreadSync) {
            return mProcessThread != null && mProcessThread.isWorking();
        }
    }

    private interface OnProcessListener {
        void onStart();
        void onResult(String out);
        void onFinish();
    }

    private static class ProcessThread extends Thread {

        private String cmd;
        private Process process;
        private OnProcessListener mOnProcessListener;
        private boolean working = false;

        public ProcessThread(@NonNull String cmd, @Nullable OnProcessListener listener) {
            this.cmd = cmd;
            this.mOnProcessListener = listener;
        }

        @Override
        public void run() {
            working = true;
            if (mOnProcessListener != null) {
                mOnProcessListener.onStart();
            }
            try {
                process = Runtime.getRuntime().exec(cmd);
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                line = reader.readLine();
                if (line != null) {
                    if (mOnProcessListener != null) {
                        mOnProcessListener.onResult(line);
                    }
                    while ((line = reader.readLine()) != null) {
                        if (mOnProcessListener != null) {
                            mOnProcessListener.onResult(line);
                        }
                    }
                } else {
                    BufferedReader readerError = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                    while ((line = readerError.readLine()) != null) {
                        if (mOnProcessListener != null) {
                            mOnProcessListener.onResult(line);
                        }
                    }
                    readerError.close();
                }

                reader.close();
                process.destroy();
            } catch (IOException e) {
                String error = e.toString();
                if (error.toLowerCase().contains("permission denied")) {
                    if (mOnProcessListener != null) {
                        mOnProcessListener.onResult("Permission denied");
                    }
                }
            } catch (Exception e) {
                if (mOnProcessListener != null) {
                    mOnProcessListener.onResult("Execution error");
                }
            }

            working = false;
            if (mOnProcessListener != null) {
                mOnProcessListener.onFinish();
            }
        }

        public void stopProcess() {
            if (process != null) {
                process.destroy();
            }
        }

        public boolean isWorking() {
            return working;
        }
    }
}
