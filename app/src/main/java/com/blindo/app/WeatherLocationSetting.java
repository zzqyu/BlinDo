package com.blindo.app;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.camera2.params.ColorSpaceTransform;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.NetworkOnMainThreadException;
import android.os.StrictMode;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.ActionBarActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


public class WeatherLocationSetting extends TwoItemPageActivity {
	private String TAG = "날씨 위치 설정";
	private TextToSpeech tts; //TextToSpeech 객체
	private TwoItemLayout twoItemLayout; //TwoItemLayout 객체

	private String selectMode = "sido";

	private ArrayList<String> codeList = new ArrayList<>();
	private ArrayList<String> xList = new ArrayList<>();
	private ArrayList<String> yList = new ArrayList<>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}
	public void init() { // 액티비티의 메인 역할을 하는 메소드
		tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
					public void onInit(int status) {
			}
		});
		if(!PublicMethods.network_check(this)) {
			tts.speak("인터넷을 켜주세요", TextToSpeech.QUEUE_FLUSH, null);
			try {
				Thread.sleep(2000);
			}
			catch (InterruptedException e){}
			finish();
		}
		else{
			tts.speak("시도를 설정하세요", TextToSpeech.QUEUE_FLUSH, null);
		}
		setTitle(TAG); //액션바의 타이틀 설정
		getSupportActionBar().hide();
		setContentView(R.layout.activity_two_item_page); //레이아웃을 가져온다

		twoItemLayout = (TwoItemLayout) findViewById(R.id.til); //twoItemLayout에 설정함
		twoItemLayout.setBackgroundResource(R.drawable.background);
		twoItemLayout.setOnTouchListener(this);
		twoItemLayout.setOnLongClickListener(this);

		sidoPage();
	}
	public void sidoPage(){
		String sidoUrl = "http://www.kma.go.kr/DFSROOT/POINT/DATA/top.json.txt";
		String[] sidoHtml = editHtml(sidoUrl);
		if(sidoHtml[0].equals("Error :")){
			Toast.makeText(this, "기상청에서 정보를 받아 올 수 없습니다. 설정 종료합니다.", Toast.LENGTH_SHORT);
			try {
				Thread.sleep(1000);
			}
			catch (InterruptedException e){}
			finish();
		}
		ArrayList<String> sidoName = new ArrayList<>();
		for(int i = 0; i < (sidoHtml.length) / 2; i++){
			codeList.add(sidoHtml[2 * i]);
			sidoName.add(sidoHtml[2 * i + 1]);
		}
		setEleTextList(sidoName);
		setIndex(0, 1);
		tts.speak("", TextToSpeech.QUEUE_ADD, null);
		tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
			public void onInit(int status) {
				setPage();
			}
		});
		setPage();
	}
	public void sigunguPage(String code){
		selectMode = "sigungu";
		codeList.clear();
		String sigunguUrl1 = "http://www.kma.go.kr/DFSROOT/POINT/DATA/mdl.";
		String sigunguUrl2 = ".json.txt";
		String[] sigunguHtml = editHtml(sigunguUrl1 + code + sigunguUrl2);
		if(sigunguHtml[0].equals("Error :")){
			Toast.makeText(this, "기상청에서 정보를 받아 올 수 없습니다. 설정 종료합니다.", Toast.LENGTH_SHORT);
			try {
				Thread.sleep(1000);
			}
			catch (InterruptedException e){}
			finish();
		}
		ArrayList<String> sigunguName = new ArrayList<>();
		for(int i = 0; i < (sigunguHtml.length) / 2; i++){
			codeList.add(sigunguHtml[2 * i]);
			sigunguName.add(sigunguHtml[2 * i + 1]);
		}

		setEleTextList(sigunguName);
		setIndex(0, 1);
		tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
			public void onInit(int status) {
				setPage();
			}
		});
		setPage();
	}
	public void eupmyeondongPage(String code){
		selectMode = "eupmyeondong";
		codeList.clear();
		String eupmyeondongUrl1 = "http://www.kma.go.kr/DFSROOT/POINT/DATA/leaf.";
		String eupmyeondongUrl2 = ".json.txt";
		String[] eupmyeondongHtml = editHtml(eupmyeondongUrl1 + code + eupmyeondongUrl2);
		if(eupmyeondongHtml[0].equals("Error :")){
			Toast.makeText(this, "기상청에서 정보를 받아 올 수 없습니다. 설정 종료합니다.", Toast.LENGTH_SHORT);
			try {
				Thread.sleep(1000);
			}
			catch (InterruptedException e){}
			finish();
		}
		ArrayList<String> eupmyeondongName = new ArrayList<>();
		for(int i = 0; i < (eupmyeondongHtml.length) / 4; i++){
			codeList.add(eupmyeondongHtml[4 * i]);
			eupmyeondongName.add(eupmyeondongHtml[4 * i + 1]);
			xList.add(eupmyeondongHtml[4 * i + 2]);
			yList.add(eupmyeondongHtml[4 * i + 3]);
		}

		setEleTextList(eupmyeondongName);
		setIndex(0, 1);
		tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
			public void onInit(int status) {
				setPage();
			}
		});
		setPage();
	}

	public String[] editHtml(String addr){
		//
		String str = PublicMethods.DownloadHtml(addr);
		if(str.contains("Error :")){
			String[] result = {"0"};
			return result;
		}

		str = str.replace("[", "").replace("]", "");
		str = str.replace("\"", "");
		str = str.replace("code:", "").replace("value:", "");
		str = str.replace("{", "").replace("}", "");
		String[] list = str.split(",");
		return  list;
	}

	public void fileSave(String url) {
		String ext = Environment.getExternalStorageState();
		String mSdPath;
		if (ext.equals(Environment.MEDIA_MOUNTED)) {
			mSdPath = Environment.getExternalStorageDirectory()
					.getAbsolutePath();
		} else {
			mSdPath = Environment.MEDIA_UNMOUNTED;
		}

		String path = mSdPath + "/android/data/com.blindo.app/" ; // 파일이 저장될 경로
		String fileName = "weatherURL.txt"; // 파일 이름
		String text = url; // 저장될 내용

		try {
			File dir = new File(path, "weather");
			dir.mkdir();

			File file = new File(dir, fileName);

			FileOutputStream fos = new FileOutputStream(file);
			fos.write(text.getBytes());
			fos.close();
			tts.speak("날씨 위치 설정 되었습니다.", TextToSpeech.QUEUE_FLUSH, null);
			finish();
		} catch (IOException e) {
			e.printStackTrace();
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
		tts.speak(getEleTextList().get(getIndex()[0]) + "로 설정합니다.", TextToSpeech.QUEUE_FLUSH, null);
		if(selectMode.equals("sido")) {
			tts.speak("시군구를 설정하세요", TextToSpeech.QUEUE_ADD, null);
			sigunguPage(codeList.get(getIndex()[0]));
		}
		else if(selectMode.equals("sigungu")){
			tts.speak("읍면동를 설정하세요", TextToSpeech.QUEUE_ADD, null);
			eupmyeondongPage(codeList.get(getIndex()[0]));
		}
		else if(selectMode.equals("eupmyeondong")){
			fileSave("http://www.kma.go.kr/wid/queryDFS.jsp?gridx=" + xList.get(getIndex()[0]) + "&gridy=" + yList.get(getIndex()[0]));
		}
	}
	public void whenSwipeDown() {

		if(getEleTextList().get(getIndex()[1]).equals("Empty!")){
			//두번째 항목의 이름을 안내 한다
			tts.speak("항목이 없습니다", TextToSpeech.QUEUE_FLUSH, null);

		}
		else{
			tts.speak(getEleTextList().get(getIndex()[1]) + "로 설정합니다.", TextToSpeech.QUEUE_FLUSH, null);
			if(selectMode.equals("sido")) {
				tts.speak("시군구를 설정하세요", TextToSpeech.QUEUE_ADD, null);
				sigunguPage(codeList.get(getIndex()[1]));
			}
			else if(selectMode.equals("sigungu")){
				tts.speak("읍면동를 설정하세요", TextToSpeech.QUEUE_ADD, null);
				eupmyeondongPage(codeList.get(getIndex()[1]));
			}
			else if(selectMode.equals("eupmyeondong")){
				fileSave("http://www.kma.go.kr/wid/queryDFS.jsp?gridx=" + xList.get(getIndex()[1]) + "&gridy=" + yList.get(getIndex()[1]));
			}
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
