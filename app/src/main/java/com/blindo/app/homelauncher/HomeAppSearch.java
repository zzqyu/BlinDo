package com.blindo.app.homelauncher;

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

public class HomeAppSearch extends TwoItemPageActivity { //예제, TwoItemPageActivity 상속
	private String TAG = "HomeAppSearch";
	private String searchKeyword;
	private ArrayList<ResolveInfo> myAppList = new ArrayList<ResolveInfo>();

	private TwoItemLayout twoItemLayout; //TwoItemLayout 객체

	private int selectIndex = 0;

	private TextToSpeech tts; //TextToSpeech 객체

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState); // TwoItemPageActivity의 onCreate에서 init()를 호출한다.


	}

	public void init(){ // 액티비티의 메인 역할을 하는 메소드
		setTitle(TAG); //액션바의 타이틀 설정
		getSupportActionBar().hide();
		setContentView(R.layout.activity_search); //레이아웃을 가져온다

		twoItemLayout = (TwoItemLayout) findViewById(R.id.til); //twoItemLayout에 설정함
		twoItemLayout.setBackgroundResource(R.drawable.background);

		tts = new TextToSpeech(this, new TextToSpeech.OnInitListener(){
			public void onInit(int status) {
				Log.i("onInit", "" + status);
				tts.speak("검색어를 입력하세요", TextToSpeech.QUEUE_FLUSH, null);
			}
		});


		SearchView searchView = (SearchView) findViewById(R.id.searchView);
		searchView.setQueryHint("검색");
		searchView.setIconifiedByDefault(false);
		searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

			public boolean onQueryTextChange(String newText) {
				return true;
			}

			public boolean onQueryTextSubmit(String query) {
				if (query.length() > 0) {
					setSearchKeyword(query);
					search();
				}
				return true;
			}
		});
	}
	public ArrayList<ResolveInfo> getMyAppList(){
		return myAppList;
	}
	public TwoItemLayout getTwoItemLayout(){
		return twoItemLayout;
	}
	public void search(){
		setSearchTarget();
		ArrayList<ResolveInfo> result = new ArrayList<ResolveInfo>();
		String appName;
		final ArrayList<String> appNameList = new ArrayList<String>();

		for(ResolveInfo k : myAppList){
			appName = "" + k.loadLabel(this.getPackageManager());
			if(appName.contains(getSearchKeyword())  //일반검색
					|| PublicMethods.choSeparation(appName).contains(getSearchKeyword()) //초성검색
					|| appName.toUpperCase().contains(getSearchKeyword().toUpperCase()) // 대문자로 바꿔 검색
					) {
				result.add(k);
				appNameList.add(appName);
			}
		}

		myAppList.clear();

		for(ResolveInfo k : result){
			myAppList.add(k);
		}

		setEleTextList(appNameList);
		if(myAppList.size() > 0){
			twoItemLayout.setOnTouchListener(this);
			twoItemLayout.setOnLongClickListener(this);
			tts = new TextToSpeech(this, new TextToSpeech.OnInitListener(){
				public void onInit(int status) {
					Log.i("onInit", "" + status);
					if(getEleTextList().get(getEleTextList().size()-1).equals("Empty!")) {
						tts.speak(myAppList.size() - 1 + "개의 검색 결과가 있습니다.", TextToSpeech.QUEUE_FLUSH, null);
						tts.speak(appNameList.get(0), TextToSpeech.QUEUE_ADD, null);
					}
					else {
						tts.speak(myAppList.size() + "개의 검색 결과가 있습니다.", TextToSpeech.QUEUE_FLUSH, null);
						tts.speak(appNameList.get(0) + ", " + appNameList.get(1), TextToSpeech.QUEUE_ADD, null);
					}
				}
			});
			//홀수일 때
			if(myAppList.size() % 2 == 1)
				myAppList.add(myAppList.get(myAppList.size()-1));
			setIndex(0, 1);
			setPage();

		}
		else {
			tts.speak("검색 결과가 없습니다.", TextToSpeech.QUEUE_FLUSH, null);
		}

	}

	public void setSearchTarget(){
		Intent intent = new Intent(Intent.ACTION_MAIN, null);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		myAppList = (ArrayList<ResolveInfo>)getPackageManager().queryIntentActivities(intent, 0);

		Comparator<ResolveInfo> comparator = new Comparator<ResolveInfo>(){
			public int compare(final ResolveInfo o1, final ResolveInfo o2) {
				int res = String.CASE_INSENSITIVE_ORDER.compare(o1.loadLabel(getPackageManager()) + "", o2.loadLabel(getPackageManager()) + "");
				if (res == 0) {
					res = (o1.loadLabel(getPackageManager()) + "").compareTo(o2.loadLabel(getPackageManager()) + "");
				}
				return res;
			}
		} ;
		Collections.sort(myAppList, comparator);
	}
	public void setPage(){ //페이지를 세팅하는 메소드
		Log.i("앱이름", myAppList.get(getIndex()[0]).loadLabel(this.getPackageManager()) + "");
		String[] pageElement = {myAppList.get(getIndex()[0]).loadLabel(this.getPackageManager()) + "", myAppList.get(getIndex()[1]).loadLabel(this.getPackageManager()) +""};

		if(myAppList.get(getIndex()[0]).equals(myAppList.get(getIndex()[1]))
				||getEleTextList().get(getIndex()[1]).equals("Empty!")){// 마지막 항목이 비어있을 때
			pageElement[1] = "";
		}
		tts.speak(pageElement[0] + ", " + pageElement[1], TextToSpeech.QUEUE_FLUSH, null);
		twoItemLayout.setThisPage(pageElement);
	}


	public void whenSwipeUp() {
		//첫번째 항목의 이름을 안내 한다
		tts.speak(myAppList.get(getIndex()[0]).loadLabel(this.getPackageManager()) + "실행합니다", TextToSpeech.QUEUE_FLUSH, null);
		ActivityInfo clickedActivityInfo =
				myAppList.get(getIndex()[0]).activityInfo;
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		intent.setClassName(
				clickedActivityInfo.applicationInfo.packageName,
				clickedActivityInfo.name);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
				Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
		startActivity(intent);
		finish();

	}
	public void whenSwipeDown() {
		//두번째 항목의 이름을 안내 한다
		if (!myAppList.get(getIndex()[0]).equals(myAppList.get(getIndex()[1]))) { // 항목이 비어있지 않을 때만 실행 된다.
			tts.speak(myAppList.get(getIndex()[1]).loadLabel(this.getPackageManager()) + "실행합니다", TextToSpeech.QUEUE_FLUSH, null);
			ActivityInfo clickedActivityInfo =
					myAppList.get(getIndex()[1]).activityInfo;
			Intent intent = new Intent(Intent.ACTION_MAIN);
			intent.addCategory(Intent.CATEGORY_LAUNCHER);
			intent.setClassName(
					clickedActivityInfo.applicationInfo.packageName,
					clickedActivityInfo.name);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
					Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
			startActivity(intent);
			finish();
		} else {
			//두번째 항목이 비어있을 때
			tts.speak("항목이 없습니다.", TextToSpeech.QUEUE_FLUSH, null);
		}


	}
	public void whenLongPressed(){}
	public void whenDoubleTap(){}


	public void setSearchKeyword(String query){
		searchKeyword = query;
	}
	public String getSearchKeyword(){
		return searchKeyword;
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
