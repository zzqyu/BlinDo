package com.blindo.app.lockscreen;

import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.speech.tts.TextToSpeech;

public class ScreenReceiver extends BroadcastReceiver {

    TextToSpeech tts;
    private KeyguardManager km = null;
    private KeyguardManager.KeyguardLock keyLock =  null;
    boolean screen = false;

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            if (km == null)
                km = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
            if(keyLock == null)
                keyLock = km.newKeyguardLock(Context.KEYGUARD_SERVICE);
            disableKeyguard();

            Intent changeIntent = new Intent(context, LockScreenActivity.class);
            changeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(changeIntent);
        }
    }

    public void disableKeyguard() {
        keyLock.disableKeyguard();
    }
}
