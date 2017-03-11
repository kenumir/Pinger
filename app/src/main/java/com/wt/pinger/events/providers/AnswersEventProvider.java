package com.wt.pinger.events.providers;

import android.support.annotation.NonNull;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.kenumir.eventclip.proto.EventClipProvider;
import com.kenumir.eventclip.proto.EventParam;
import com.kenumir.eventclip.proto.UserParam;
import com.wt.pinger.events.EventNames;

/**
 * Created by Kenumir on 2017-03-11.
 *
 */

public class AnswersEventProvider extends EventClipProvider {

    @Override
    public void deliver(EventParam eventParam) {
        switch(eventParam.getName()) {
            case EventNames.REPLAIO_AD_CLICKED:
                Answers.getInstance().logCustom(new CustomEvent(EventNames.REPLAIO_AD_CLICKED));
                break;
        }
    }

    @Override
    public void userProperty(@NonNull UserParam userParam) {

    }

}
