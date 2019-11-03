package com.blindo.app.lockscreen;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.blindo.app.lockscreen.LockScreenActivity;

/**
 * Created by JeongGyu on 2015-08-18.
 */
public class BootBroadCastReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {

		if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
			Intent i = new Intent(context, LockScreenActivity.class);
			i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(i);
		}
	}
}
// 매니페스트 파일에 추가
//<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
/*<activity
			android:name=".lockscreen.LockScreenActivity"
			android:excludeFromRecents="true"
			android:label="Blind 잠금화면" >


		</activity>*/