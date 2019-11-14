package com.wt.pinger.proto;

import androidx.appcompat.app.AppCompatDelegate;

public @interface UserTheme {
    int DEFAULT = AppCompatDelegate.MODE_NIGHT_UNSPECIFIED;
    int FOLLOW_SYSTEM = 1;
    int FOLLOW_BATTERY = 2;
    int LIGHT = 3;
    int DARK = 4;
}
