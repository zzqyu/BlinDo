package com.blindo.app;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.KeyEvent;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by JeongGyu on 2015-08-28.
 */
public class AppPreferenceActivity extends TwoItemPageActivity {
	private String TAG = "AppPreferenceActivity";
	private TextToSpeech tts; //TextToSpeech 객체
	private TwoItemLayout twoItemLayout; //TwoItemLayout 객체

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState); // TwoItemPageActivity의 onCreate에서 init()를 호출한다.


	}

	public void init() { // 액티비티의 메인 역할을 하는 메소드
		setTitle(TAG); //액션바의 타이틀 설정
		getSupportActionBar().hide();
		setContentView(R.layout.activity_two_item_page); //레이아웃을 가져온다

		twoItemLayout = (TwoItemLayout) findViewById(R.id.til); //twoItemLayout에 설정함
		twoItemLayout.setBackgroundResource(R.drawable.background);
		twoItemLayout.setOnTouchListener(this);
		twoItemLayout.setOnLongClickListener(this);

		ArrayList<String> settingName = new ArrayList<>();
		settingName.add("바로가기1 설정");
		settingName.add("바로가기2 설정");
		settingName.add("바로가기3 설정");
		if (isWeatherBGM()) {
			settingName.add("날씨BGM이 켜져있습니다.");
			settingName.add("날씨위치설정");
		} else {
			settingName.add("날씨BGM이 꺼져있습니다.");
		}


		setEleTextList(settingName);

		tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
			public void onInit(int status) {
				Log.i("onInit", "" + status);
				setPage();
			}
		});
		setIndex(0, 1);
		setPage();
	}

	public boolean isWeatherBGM() {
		File files1 = new File("sdcard/android/data/com.blindo.app/Preference"); // 앱 이름|패키지 이름 파일로 있는 디렉토리

		final File[] fileList = files1.listFiles();
		if (fileList[0].getName().equals("on")) {
			return true;
		} else {
			return false;
		}
	}

	public void changrWeatherBGM() {
		File addfile;
		File files1 = new File("sdcard/android/data/com.blindo.app/Preference"); // 앱 이름|패키지 이름 파일로 있는 디렉토리
		File[] fileList = files1.listFiles();
		if (!isWeatherBGM()) {
			for (File k : fileList) {
				if (k.getName().equals("off")) {
					k.delete();
				}
			}
			addfile = new File("sdcard/android/data/com.blindo.app/Preference/on");
		} else {
			for (File k : fileList) {
				if (k.getName().equals("on")) {
					k.delete();
				}
			}
			addfile = new File("sdcard/android/data/com.blindo.app/Preference/off");
		}
		try {
			FileOutputStream fos1 = new FileOutputStream(addfile);
			fos1.close();
		} catch (IOException e) {
		}
	}

	public void setPage(){ //페이지를 세팅하는 메소드

		//페이지에 출력할 항목 추가
		String[] pageElement = {getEleTextList().get(getIndex()[0]), getEleTextList().get(getIndex()[1])};


		if(pageElement[1].equals("Empty!")){// 마지막 항목이 비어있을 때
			pageElement[1] = "";
		}

		//음성안내
		tts.speak(pageElement[0] + ", " + pageElement[1], TextToSpeech.QUEUE_FLUSH, null);

		//레이아웃에 두개 항목 추가
		twoItemLayout.setThisPage(pageElement);
	}

	public void whenSwipeUp() {
		//첫번째 항목의 이름을 안내 한다
		tts.speak(getEleTextList().get(getIndex()[0]) + "실행합니다", TextToSpeech.QUEUE_FLUSH, null);
		if(getEleTextList().get(getIndex()[0]).contains("바로가기")){
			Intent intentSubActivity = new Intent(AppPreferenceActivity.this, LockShortcutSetting.class);
			intentSubActivity.putExtra("shortcutNum", getIndex()[0]);
			startActivity(intentSubActivity);
			finish();
		}
		else{ // 날씨제공위치
			Intent intentSubActivity = new Intent(AppPreferenceActivity.this, WeatherLocationSetting.class);
			startActivity(intentSubActivity);
		}


	}
	public void whenSwipeDown() {
		if(getEleTextList().get(getIndex()[1]).contains("바로가기")){
			//두번째 항목의 이름을 안내 한다
			tts.speak(getEleTextList().get(getIndex()[1]) + "실행합니다", TextToSpeech.QUEUE_FLUSH, null);
			Intent intentSubActivity = new Intent(AppPreferenceActivity.this, LockShortcutSetting.class);
			intentSubActivity.putExtra("shortcutNum", getIndex()[1]);
			startActivity(intentSubActivity);
			finish();
		}
		else if(getEleTextList().get(getIndex()[1]).equals("Empty!")){
			//두번째 항목의 이름을 안내 한다
			tts.speak("항목이 없습니다", TextToSpeech.QUEUE_FLUSH, null);

		}
		else{ // 날씨BGM
			if(isWeatherBGM()){
				tts.speak("날씨BGM을 끕니다.", TextToSpeech.QUEUE_FLUSH, null);
			}
			else{
				tts.speak("날씨BGM을 켭니다.", TextToSpeech.QUEUE_FLUSH, null);
			}
			changrWeatherBGM();
			Intent intentSubActivity = new Intent(AppPreferenceActivity.this, AppPreferenceActivity.class);
			startActivity(intentSubActivity);
			finish();
		}


	}



	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK){
			if(tts.isSpeaking())
				tts.stop();
			finish();
			return false;
		}
		return false;
	}

}
