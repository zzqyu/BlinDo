package com.blindo.app;

import android.content.Context;
import android.os.Bundle;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

public class TwoItemPageActivity extends GestureActivity {
	private String TAG = "TwoItemPageActivity";
	private ArrayList<String> eleTextList= new ArrayList<String>();
	private int[] index = {0, 1};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		init();
	}
	public void init(){

	}

	public void setIndex(int index1, int index2){
		index[0] = index1;
		index[1] = index2;
	}
	public int[] getIndex(){
		return index;
	}
	public void setEleTextList(ArrayList<String> eleTextList){
		this.eleTextList = eleTextList;
		if(eleTextList.size()%2 == 1){ // 항목 개수가 홀수 일 때, Empty! 라는 문자열을 추가 해 준다.
			eleTextList.add("Empty!");
		}
	}
	public ArrayList<String> getEleTextList(){
		return eleTextList;
	}

	public void setPage(){

	}
	public void whenSwipeLeft() { // 다음 페이지로 넘길 때
		index[0]+=2;
		if(index[0] >= getEleTextList().size())
			index[0] = 0;
		index[1]=index[0] + 1;
		setPage();
	}
	public void whenSwipeRight() { // 이전 페이지로 넘길 때
		index[0] -= 2;
		if(index[0] < 0)
			index[0] = getEleTextList().size() + index[0];
		index[1]=index[0]+1;
		setPage();
	}
	public void whenSwipeUp() {
	}
	public void whenSwipeDown() {
	}
	public void whenLongPressed(){

	}
	public void whenDoubleTap(){

	}

}
