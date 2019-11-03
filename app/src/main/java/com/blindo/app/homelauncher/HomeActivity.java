package com.blindo.app.homelauncher;

import java.io.*;

import android.app.ActivityManager;
import android.app.Instrumentation;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Telephony;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;

import com.blindo.app.AppPreferenceActivity;
import com.blindo.app.R;
import com.blindo.app.TwoItemLayout;
import com.blindo.app.TwoItemPageActivity;
import com.blindo.app.WeatherLocationSetting;
import com.blindo.app.lockscreen.LockScreenActivity;
import com.blindo.app.message.MessageSenderListActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class HomeActivity extends TwoItemPageActivity { //예제, TwoItemPageActivity 상속
	private String TAG = "HomeActivity";
	private ArrayList<String> eleTextList = new ArrayList<String>(); //앱 이름|패키지 이름
	private ArrayList<String> appName = new ArrayList<String>(); //앱 이름
	private ArrayList<String> packageName = new ArrayList<String>(); //패키지 이름

	private TwoItemLayout twoItemLayout; //TwoItemLayout 객체

	public int jh = 0; //추후 항목 추가를 실행할지 Empty를 실행할지 구분
	private boolean isYN = false; //예 아니오 화면 출력 여부
	private boolean isDelete = false; //삭제 모드 확인
	private int selectIndex = 0;
	private TextToSpeech tts; //TextToSpeech 객체


	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState); // TwoItemPageActivity의 onCreate에서 init()를 호출한다.
	}
	public void onResume(){
		super.onResume();
		setPage();
	}

	public void init() { // 액티비티의 메인 역할을 하는 메소드
		LockScreenRunBackground();
		setTitle(TAG); //액션바의 타이틀 설정
		getSupportActionBar().hide();
		setContentView(R.layout.activity_two_item_page); //레이아웃을 가져온다

		twoItemLayout = (TwoItemLayout) findViewById(R.id.til); //twoItemLayout에 설정함
		twoItemLayout.setBackgroundResource(R.drawable.background);

		//제스처를 위해 아래 두개 항목은 꼭 추가해야 한다.
		twoItemLayout.setOnTouchListener(this);
		twoItemLayout.setOnLongClickListener(this);

		loadHomeItem(); //본 액티비티에서 사용할 항목 리스트를 외부(파일 등)불러오거나 설정하는 메소드
		// 예. 메시지 같은경우는 setSender()가 이에 해당함, 홈같은 경우는 loadHomeItem() 메소드


		setEleTextList(eleTextList); //본 액티비티에서 사용할 리스트를 상위클래스의 항목리스트에 설정한다.

		tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() { // tts설정
			public void onInit(int status) { // tts 설정될 때 자동으로 실행되는 메소드
				Log.i("onInit", "" + status);
				setPage();  //이 메소드를 통해 처음 시작시 페이지를 설정하고, 음성안내를 한다.
			}
		});
		setPage(); //이 메소드를 통해 처음 시작시 페이지를 설정
	}

	public void LockScreenRunBackground() {
		int cnt = 0;
		ActivityManager activityManager = (ActivityManager) this.getSystemService(this.ACTIVITY_SERVICE);
		List<ActivityManager.RunningTaskInfo> info = activityManager.getRunningTasks(5);
		for (Iterator iterator = info.iterator(); iterator.hasNext();)  {

			ActivityManager.RunningTaskInfo runningTaskInfo = (ActivityManager.RunningTaskInfo) iterator.next();
			Log.i("baseActivity", runningTaskInfo.baseActivity.getClassName());
			if(runningTaskInfo.baseActivity.getClassName().equals("com.blindo.app.lockscreen.LockScreenActivity")){
				cnt++;
			}
		}
		if(cnt == 0){
			Intent intent1 = new Intent(this, LockScreenActivity.class);
			intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
			startActivity(intent1);
			finish();
		}
	}
	public void loadHomeItem() {//본 액티비티에서 사용할 항목 리스트를 외부(파일 등)불러오거나 설정하는 메소드
		File files = new File("sdcard/android/data/com.blindo.app/homelist"); // 앱 이름|패키지 이름 파일로 있는 디렉토리

		File defaultfile1 = new File("sdcard/android/data/com.blindo.app/homelist/전화,com.android.dialer");
		File defaultfile2 = new File("sdcard/android/data/com.blindo.app/homelist/문자,com.android.mms");
		File defaultfile3 = new File("sdcard/android/data/com.blindo.app/homelist/카카오톡,com.kakao.talk");
		File defaultfile4 = new File("sdcard/android/data/com.blindo.app/homelist/Facebook,com.facebook.katana");

		if (!files.exists())  // 원하는 경로에 폴더가 있는지 확인
			files.mkdirs();

		if (files.listFiles().length > 0) {
			for (File file : files.listFiles()) {
				eleTextList.add(file.getName());
				jh += 1;
			}

			eleTextList.add("잠금화면설정,Empty");
			eleTextList.add("항목 추가,Empty");

			if (eleTextList.size() % 2 == 1) { //홀수일 때 Empty
				eleTextList.add("Empty,Empty");
			}
		} else {
			try {
				FileOutputStream fos1 = new FileOutputStream(defaultfile1);
				FileOutputStream fos2 = new FileOutputStream(defaultfile2);
				FileOutputStream fos3 = new FileOutputStream(defaultfile3);
				FileOutputStream fos4 = new FileOutputStream(defaultfile4);

				fos1.close();
				fos2.close();
				fos3.close();
				fos4.close();

				loadHomeItem();
			} catch (IOException e) {
			}
		}
		for (int i = 0; i < eleTextList.size(); i++) {
			String[] arr = eleTextList.get(i).split(",");
			appName.add(arr[0]);
			packageName.add(arr[1]);
		}
	}
	public void setYNPage() {  //yn 모드 일때 화면 세팅
		String[] pageElement = {"예", "아니오"};
		if (pageElement[1].equals("Empty")) {// 마지막 항목이 비어있을 때
			pageElement[1] = "";
		}
		//음성안내
		tts.speak(pageElement[0] + "  " + pageElement[1], TextToSpeech.QUEUE_ADD, null);

		//레이아웃에 두개 항목 추가
		twoItemLayout.setThisPage(pageElement);
	}
	// 오버라이딩 시작 ====================================================================
	public void setPage() { //페이지를 세팅하는 메소드

		String[] pageElement = {appName.get(getIndex()[0]), appName.get(getIndex()[1])};
		if (pageElement[1].equals("Empty")) {// 마지막 항목이 비어있을 때
			pageElement[1] = "";
		}

		//음성안내
		tts.speak(pageElement[0] + "  " + pageElement[1], TextToSpeech.QUEUE_FLUSH, null);

		//레이아웃에 두개 항목 추가
		twoItemLayout.setThisPage(pageElement);
	}

	public void whenSwipeLeft() { // 다음 페이지로 넘길 때 ,이건 안건들여도 됨
		if (!isYN) {  //yn 모드가 아닐 때만 제스처 실행
			super.whenSwipeLeft();
		}
	}

	public void whenSwipeRight() { // 이전 페이지로 넘길 때 ,이건 안건들여도 됨
		if (!isYN) {  //yn 모드가 아닐 때만 제스처 실행
			super.whenSwipeRight();
		}
	}

	public void whenSwipeUp() {
		if (!isYN) { //yn 모드가 아닐 때
			selectIndex = 0;
			if (isDelete) {//삭제 모드일 때
				tts.speak("" + appName.get(getIndex()[0]) + "삭제 하시겠습니까?", TextToSpeech.QUEUE_FLUSH, null);
				isYN = true;
				setYNPage();
			} else {
				tts.speak("" + appName.get(getIndex()[0]) + "실행합니다", TextToSpeech.QUEUE_FLUSH, null);
				Intent intent1 = new Intent(Intent.ACTION_MAIN);
				intent1.addCategory(Intent.CATEGORY_HOME);
				startActivity(intent1);
				if (appName.get(getIndex()[0]).equals("항목 추가")) {
					Intent intentSubActivity = new Intent(this, HomeAppAdd.class);
					startActivity(intentSubActivity);
				}
				else if (appName.get(getIndex()[0]).equals("잠금화면설정")) {
					Intent intentSubActivity = new Intent(this, AppPreferenceActivity.class);
					startActivity(intentSubActivity);
				}
				else if (packageName.get(getIndex()[0]).equals("com.android.dialer")) {
					Intent intentSubActivity = new Intent(Intent.ACTION_DIAL);
					startActivity(intentSubActivity);

				} else if (packageName.get(getIndex()[0]).equals("com.android.mms")) {
					Intent intentSubActivity = new Intent(this, MessageSenderListActivity.class);
					startActivity(intentSubActivity);
				}
				else {
					Intent intent = this.getPackageManager().getLaunchIntentForPackage(packageName.get(getIndex()[0]));
					startActivity(intent);
				}

				try {
					Thread.sleep(1000);
					finish();
				} catch (Exception e) {}
			}

		} else {  //yn 모드일 때
			tts.speak(appName.get(getIndex()[selectIndex]) + " 삭제했습니다.", TextToSpeech.QUEUE_FLUSH, null);
			// 파일 추가
			File removefile = new File("sdcard/android/data/com.blindo.app/homelist/" +
					appName.get(getIndex()[selectIndex]) + "," + packageName.get(getIndex()[selectIndex]));
			removefile.delete();
			isYN = false;
			isDelete = false;
			Intent intentSubActivity = new Intent(this, HomeActivity.class);
			startActivity(intentSubActivity);
			finish();
		}
	}

	public void whenSwipeDown() {
		if (!isYN) { //yn 모드가 아닐 때
			selectIndex = 1;
			if (isDelete) {
				tts.speak("" + appName.get(getIndex()[1]) + "삭제 하시겠습니까?", TextToSpeech.QUEUE_FLUSH, null);
				isYN = true;
				setYNPage();
			} else {
				if ( appName.get(getIndex()[1]).equals("Empty")) { // 항목이 비어있지 않을 때만 실행 된다.
					tts.speak("항목이 없습니다.", TextToSpeech.QUEUE_FLUSH, null);
				}
				else{
					tts.speak("" + appName.get(getIndex()[1]) + "실행합니다", TextToSpeech.QUEUE_FLUSH, null);
					Intent intent1 = new Intent(Intent.ACTION_MAIN);
					intent1.addCategory(Intent.CATEGORY_HOME);
					startActivity(intent1);
					if (appName.get(getIndex()[1]).equals("항목 추가")) { //항목 추가 실행
						Intent intentSubActivity = new Intent(HomeActivity.this, HomeAppAdd.class);
						startActivity(intentSubActivity);
					}
					else if (appName.get(getIndex()[1]).equals("잠금화면설정")) {
						Intent intentSubActivity = new Intent(this, AppPreferenceActivity.class);
						startActivity(intentSubActivity);
					}
					else if (packageName.get(getIndex()[1]).equals("com.android.dialer")) {
						Intent intentSubActivity = new Intent(Intent.ACTION_DIAL);
						startActivity(intentSubActivity);
					} else if (appName.get(getIndex()[1]).equals("문자")) {
						Intent intentSubActivity = new Intent(this, MessageSenderListActivity.class);
						startActivity(intentSubActivity);
					}
					else {
						Intent intent = this.getPackageManager().getLaunchIntentForPackage(packageName.get(getIndex()[1]));
						startActivity(intent);
					}

					try {
						Thread.sleep(1000);
						finish();
					} catch (Exception e) {}
				}
			}

		} else {  //yn 모드 일 때
			tts.speak(appName.get(getIndex()[selectIndex]) + "삭제 취소합니다", TextToSpeech.QUEUE_FLUSH, null);
			isYN = false;
			isDelete = false;
			setPage();
		}
	}

	public void whenLongPressed() {
		isDelete = true;
		tts.speak("삭제 모드 실행합니다.", TextToSpeech.QUEUE_FLUSH, null);
	}

	public void whenDoubleTap() {

		tts.speak("앱 검색을 실행합니다.", TextToSpeech.QUEUE_FLUSH, null);
		Intent intent1 = new Intent(Intent.ACTION_MAIN);
		intent1.addCategory(Intent.CATEGORY_HOME);
		startActivity(intent1);
		Intent intentSubActivity = new Intent(this, HomeAppSearch.class);
		startActivity(intentSubActivity);
		finish();
	}
	// 오버라이딩 끝  ====================================================================

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
			case KeyEvent.KEYCODE_BACK:
				return true;
			default:
				return false;
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		menu.add(0, 0, Menu.NONE, "잠금화면설정");
		menu.add(0, 1, Menu.NONE, "시스템 설정");

		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
			case 0:
				Intent intentSubActivity0 = new Intent(this, AppPreferenceActivity.class);
				startActivity(intentSubActivity0);
				break;

			case 1:
				Intent intentSubActivity1 = this.getPackageManager().getLaunchIntentForPackage("com.android.settings");
				startActivity(intentSubActivity1);
				break;


			default:
				break;
		}

		return super.onOptionsItemSelected(item);
	}
}