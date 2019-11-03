package com.blindo.app.homelauncher;

import java.io.*;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.SearchView;

import com.blindo.app.R;
import com.blindo.app.PublicMethods;
import com.blindo.app.TwoItemLayout;
import com.blindo.app.TwoItemPageActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.io.File;
import java.io.FileOutputStream;

public class HomeAppAdd extends HomeAppSearch { //예제, TwoItemPageActivity 상속
	private String TAG = "HomeAppAdd";
	private boolean isYN = false; //예 아니오 화면 출력 여부
	private int selectIndex = 0;

	private TextToSpeech tts; //TextToSpeech 객체

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		tts = new TextToSpeech(this, new TextToSpeech.OnInitListener(){
			public void onInit(int status) {
				Log.i("onInit", "" + status);
			}
		});
	}
	public void setYNPage(){  //yn 모드 일때 화면 세팅
		String[] pageElement = {"예", "아니오"};
		if(pageElement[1].equals("Empty")){// 마지막 항목이 비어있을 때
			pageElement[1] = "";
		}
		//음성안내
		tts.speak(pageElement[0] + "  " + pageElement[1], TextToSpeech.QUEUE_ADD, null);

		//레이아웃에 두개 항목 추가
		getTwoItemLayout().setThisPage(pageElement);
	}
	public void whenSwipeLeft() { // 다음 페이지로 넘길 때 ,이건 안건들여도 됨
		if(! isYN)  //yn 모드가 아닐 때만 제스처 실행
			super.whenSwipeLeft();
	}
	public void whenSwipeRight() { // 이전 페이지로 넘길 때 ,이건 안건들여도 됨
		if(! isYN)  //yn 모드가 아닐 때만 제스처 실행
			super.whenSwipeRight();
	}

	public void whenSwipeUp() {
		if(! isYN) { //yn 모드가 아닐 때
			//첫번째 항목의 이름을 안내 한다
			selectIndex=0;
			tts.speak(getMyAppList().get(getIndex()[selectIndex]).loadLabel(this.getPackageManager()) + "추가 하시겠습니까?", TextToSpeech.QUEUE_FLUSH, null);
			isYN= true;
			setYNPage();
		}
		else{  //yn 모드일 때
			tts.speak(getMyAppList().get(getIndex()[selectIndex]).loadLabel(this.getPackageManager()) + "추가했습니다", TextToSpeech.QUEUE_FLUSH, null);
			ActivityInfo clickedActivityInfo =
					getMyAppList().get(getIndex()[selectIndex]).activityInfo;
			Intent intent = new Intent(Intent.ACTION_MAIN);
			intent.addCategory(Intent.CATEGORY_LAUNCHER);
			intent.setClassName(
					clickedActivityInfo.applicationInfo.packageName,
					clickedActivityInfo.name);
			// 파일 추가
			File addfile=new File("sdcard/android/data/com.blindo.app/homelist/"+
					getMyAppList().get(getIndex()[selectIndex]).loadLabel(this.getPackageManager())+","+clickedActivityInfo.applicationInfo.packageName);
			try{
				FileOutputStream fos1 = new FileOutputStream(addfile);
				fos1.close();
			}catch (IOException e){}
			isYN = false;

			Intent intentSubActivity = new Intent(HomeAppAdd.this, HomeActivity.class);
			startActivity(intentSubActivity);
			finish();
		}
	}
	public void whenSwipeDown() {
		if(! isYN) { //yn 모드가 아닐 때
			//두번째 항목의 이름을 안내 한다
			selectIndex=1;
			if (!getMyAppList().get(getIndex()[0]).equals(getMyAppList().get(getIndex()[1]))) { // 항목이 비어있지 않을 때만 실행 된다.
				tts.speak(getMyAppList().get(getIndex()[1]).loadLabel(this.getPackageManager()) + "추가 하시겠습니까?", TextToSpeech.QUEUE_FLUSH, null);
				isYN=true;
				setYNPage();
			}
			else{
				//두번째 항목이 비어있을 때
				tts.speak("항목이 없습니다.", TextToSpeech.QUEUE_FLUSH, null);
			}
		}
		else{  //yn 모드 일 때
			tts.speak("취소합니다", TextToSpeech.QUEUE_FLUSH, null);
			isYN = false;
			Intent intentSubActivity = new Intent(HomeAppAdd.this, HomeActivity.class);
			startActivity(intentSubActivity);
			finish();
		}

	}
}
