package com.blindo.app.keyboard;

import android.content.Context;
import android.inputmethodservice.InputMethodService;

import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import android.view.inputmethod.EditorInfo;

import com.blindo.app.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


/**
 * Created by JeongGyu on 2015-08-01.
 */
public class BrailleKeyboardService extends InputMethodService  {

	private int dpWidth;
	private int dpHeight;

	private TouchBoardView mInputView;
	private GestureDetector mGestureDetector;

	private boolean shiftMod = false;
	private String isLanguege = "korean"; // korean, english, number

	private ArrayList<Integer> pointList;
	private ArrayList<Integer> jamoList;
	private ArrayList<Integer> jamoPointList;
	private ArrayList<Coordinate> coordinateList;

	private int temp1Uni;
	private int temp1BrailleCode;
	private int temp2Uni;
	private int temp2BrailleCode;
	private boolean isDoubleJong = false;

	private String inputText = "";

	private TextToSpeech tts;

	// 키보드 생성. 폭이 바뀐 경우만 재생성한다.
	@Override
	public void onInitializeInterface() {//1
		Log.i("onInitializeInterface", "");
		tts = new TextToSpeech(this, new TextToSpeech.OnInitListener(){
			public void onInit(int status) {
				Log.i("onInit", "" + status);

			}
		});

	}
	// 입력뷰 생성하고 영문 키보드로 초기화
	@Override
	public View onCreateInputView() {//2

		DisplayMetrics dm = getApplicationContext().getResources().getDisplayMetrics();
		dpWidth = dm.widthPixels;
		dpHeight = dm.heightPixels;
		mInputView = (TouchBoardView) getLayoutInflater().inflate(R.layout.touch_board, null);
		mGestureDetector = new GestureDetector(new CustomGesture());

		mInputView.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				return mGestureDetector.onTouchEvent(event);
			}
		});
		mInputView.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {

				return false;
			}
		});

		coordinateList = new ArrayList<Coordinate>();
		pointList = new ArrayList<Integer>();
		jamoList = new ArrayList<Integer>();
		jamoPointList = new ArrayList<Integer>();
		return mInputView;
	}
	// 입력 시작시 초기화 - 특별히 초기화할 내용이 없음
	@Override
	public void onStartInput(EditorInfo attribute, boolean restarting) {
		super.onStartInput(attribute, restarting);
		Log.i("onStartInput", "");
	}

	// 입력 끝 - 키보드를 닫는다.
	@Override
	public void onFinishInput() {
		super.onFinishInput();
		Log.i("onFinishInput", "");
	}

	@Override
	public void onStartInputView(EditorInfo attribute, boolean restarting) {
		super.onStartInputView(attribute, restarting);
		Log.i("onStartInputView", restarting + " : " + attribute.fieldName);
		tts.speak("키보드를 시작합니다.", TextToSpeech.QUEUE_ADD, null);
		coordinateList = new ArrayList<Coordinate>();
		pointList = new ArrayList<Integer>();
		jamoList = new ArrayList<Integer>();
		jamoPointList = new ArrayList<Integer>();
		inputText = "";
		isLanguege = "korean";
		tts.speak("한글", TextToSpeech.QUEUE_ADD, null);
	}

	private void keyDownUp(int keyEventCode) {
		getCurrentInputConnection().sendKeyEvent(
				new KeyEvent(KeyEvent.ACTION_DOWN, keyEventCode));
		getCurrentInputConnection().sendKeyEvent(
				new KeyEvent(KeyEvent.ACTION_UP, keyEventCode));
	}
	public boolean isSameRange(ArrayList<Coordinate> cdnList, char xORy){
		double avr = 0;
		if(xORy == 'x'){
			avr = (cdnList.get(0).getX()
					+ cdnList.get(cdnList.size() - 1).getX()) / 2;
		}
		else if(xORy == 'y'){
			avr = (cdnList.get(0).getY()
					+ cdnList.get(cdnList.size() - 1).getY()) / 2;
		}

		boolean result = false;
		double SAME_RANGE = dpHeight / 9.5;

		int cnt = 0;
		for (Coordinate k : cdnList) {
			if(xORy == 'x'){
				if (Math.abs(avr - k.getX()) <= SAME_RANGE) cnt++;
			}
			else{
				if (Math.abs(avr - k.getY()) <= SAME_RANGE) cnt++;
			}
		}
		if (cnt == cdnList.size()) result = true;
		return result;
	}
	public void coordinateNum1(){
		if (dpHeight / 3 >= coordinateList.get(0).getY())
			if (dpWidth / 2 >= coordinateList.get(0).getX())
				pointList.add(1);
			else
				pointList.add(4);
		else if (dpHeight * 2 / 3 >= coordinateList.get(0).getY()
				&& dpHeight / 3 < coordinateList.get(0).getY())
			if (dpWidth / 2 >= coordinateList.get(0).getX())
				pointList.add(2);
			else
				pointList.add(5);
		else if (dpHeight * 2 / 3 < coordinateList.get(0).getY())
			if (dpWidth / 2 >= coordinateList.get(0).getX())
				pointList.add(3);
			else
				pointList.add(6);
	}
	public void coordinateNum2(boolean isVertical, boolean isHorizontal, double avrX, double avrY){
		Collections.sort(coordinateList, lowToHighY);
		int[] point = new int[2];
		if (isHorizontal && !isVertical) {
			if (dpHeight / 3 >= coordinateList.get(0).getY()) {
				point[0] = 1;
				point[1] = 4;
			} else if (dpHeight * 2 / 3 >= coordinateList.get(0).getY()
					&& dpHeight / 3 < coordinateList.get(0).getY()) {
				point[0] = 2;
				point[1] = 5;
			} else if (dpHeight * 2 / 3 < coordinateList.get(0).getY()) {
				point[0] = 3;
				point[1] = 6;
			}
		} else if (!isHorizontal && isVertical) {
			if (coordinateList.get(1).getY() - coordinateList.get(0).getY() <= dpHeight / 3) {
				if (avrY <= dpHeight / 2) {
					point[0] = 1;
					point[1] = 2;
				} else {
					point[0] = 2;
					point[1] = 3;
				}
			} else {
				point[0] = 1;
				point[1] = 3;
			}
			if (avrX >= dpWidth / 2) {
				point[0] += 3;
				point[1] += 3;
			}
		} else {
			if (coordinateList.get(1).getY() - coordinateList.get(0).getY() <= dpHeight / 3) {
				if (avrY <= dpHeight / 2) {
					point[0] = 1;
					point[1] = 5;
				} else {
					point[0] = 2;
					point[1] = 6;
				}
			} else {
				point[0] = 1;
				point[1] = 6;
			}
			if (coordinateList.get(1).getX() - coordinateList.get(0).getX() < 0) {
				point[0] += 3;
				point[1] -= 3;
			}
		}
		pointList.add(point[0]);
		pointList.add(point[1]);
	}

	public void coordinateNum3(boolean isVertical, double avrX, double avrY){
		Collections.sort(coordinateList, lowToHighY);
		int[] point = new int[3];
		int realAvrY = (coordinateList.get(0).getY() + coordinateList.get(1).getY() + coordinateList.get(2).getY()) / 3;
		int SAME_RANGE = (int) dpHeight / 10;

		if (realAvrY >= dpHeight * 3.3 / 8 && realAvrY <= dpHeight * 4.7 / 8) {
			if (isVertical) {
				if (dpWidth / 2 >= avrX) {
					int[] temp = {1, 2, 3};
					point = temp;
				} else {
					int[] temp = {4, 5, 6};
					point = temp;
				}
			} else {
				if (Math.abs((avrX - coordinateList.get(1).getX())
						- (avrX - coordinateList.get(2).getX())) < SAME_RANGE) {
					if (avrX < coordinateList.get(0).getX()) {
						int[] temp = {2, 3, 4};
						point = temp;
					} else {
						int[] temp = {1, 5, 6};
						point = temp;
					}
				} else if (Math.abs((avrX - coordinateList.get(0).getX())
						- (avrX - coordinateList.get(2).getX())) < SAME_RANGE) {
					if (avrX < coordinateList.get(1).getX()) {
						int[] temp = {1, 3, 5};
						point = temp;
					} else {
						int[] temp = {2, 4, 6};
						point = temp;
					}
				} else if (Math.abs((avrX - coordinateList.get(0).getX())
						- (avrX - coordinateList.get(1).getX())) < SAME_RANGE) {
					if (avrX < coordinateList.get(2).getX()) {
						int[] temp = {1, 2, 6};
						point = temp;
					} else {
						int[] temp = {3, 4, 5};
						point = temp;
					}
				}
			}
		} else {
			double[] circumscription = circumscriptionCenter(coordinateList);
			double[] inscribed = inscribedCenter(coordinateList);
			if (circumscription[1] >= dpHeight * 3 / 8 && circumscription[1] <= dpHeight * 5 / 8) { //외접원의 중심이 中
				if (inscribed[0] <= avrX && inscribed[1] <= avrY) {
					int[] temp = {1, 3, 4};
					point = temp;
				} else if (inscribed[0] <= avrX && inscribed[1] > avrY) {
					int[] temp = {1, 3, 6};
					point = temp;
				} else if (inscribed[0] > avrX && inscribed[1] <= avrY) {
					int[] temp = {1, 4, 6};
					point = temp;
				} else if (inscribed[0] > avrX && inscribed[1] > avrY) {
					int[] temp = {3, 4, 6};
					point = temp;
				}
			} else { //외접원의 중심이 上
				if (inscribed[0] <= avrX && inscribed[1] <= avrY) {
					int[] temp = {1, 2, 4};
					point = temp;
				} else if (inscribed[0] <= avrX && inscribed[1] > avrY) {
					int[] temp = {1, 2, 5};
					point = temp;
				} else if (inscribed[0] > avrX && inscribed[1] <= avrY) {
					int[] temp = {1, 4, 5};
					point = temp;
				} else if (inscribed[0] > avrX && inscribed[1] > avrY) {
					int[] temp = {2, 4, 5};
					point = temp;
				}
				if (circumscription[1] > dpHeight * 5 / 8) { //외접원의 중심이 下
					for (int i = 0; i < point.length; i++)
						point[i] += 1;
				}
			}
		}
		pointList.add(point[0]);
		pointList.add(point[1]);
		pointList.add(point[2]);
	}
	public void coordinateNum4(){
		int coordinateNum = coordinateList.size();
		ArrayList<Coordinate> cdnList1 = new ArrayList<Coordinate>(), cdnList2 = new ArrayList<Coordinate>();

		Collections.sort(coordinateList, lowToHighX); //x좌표 순 정렬
		cdnList1.add(coordinateList.get(0));
		cdnList1.add(coordinateList.get(1));

		cdnList2.add(coordinateList.get(2));
		cdnList2.add(coordinateList.get(3));

		double avrDistanceY = (Math.abs(cdnList1.get(0).getY() - cdnList1.get(1).getY()) + Math.abs(cdnList2.get(0).getY() - cdnList2.get(1).getY())) / 2;

		boolean isSameX1 = isSameRange(cdnList1, 'x');
		boolean isSameX2 = isSameRange(cdnList2, 'x');

		Collections.sort(coordinateList, lowToHighY); //y좌표 순 정렬
		cdnList1.clear();
		cdnList2.clear();

		cdnList1.add(coordinateList.get(0));
		cdnList1.add(coordinateList.get(1));

		cdnList2.add(coordinateList.get(2));
		cdnList2.add(coordinateList.get(3));

		double avrDistanceX = (Math.abs(cdnList1.get(0).getX() - cdnList1.get(1).getX()) + Math.abs(cdnList2.get(0).getX() - cdnList2.get(1).getX())) / 2;

		boolean isSameY1 = isSameRange(cdnList1, 'x');
		boolean isSameY2 = isSameRange(cdnList2, 'x');

		// x, y 좌표 평균 구하기
		double avrX = 0.0, avrY = 0.0;
		for (Coordinate k : coordinateList) {
			avrX += k.getX();
			avrY += k.getY();
		}
		avrX /= coordinateNum;
		avrY /= coordinateNum;

		if(isSameX1 && isSameX2 && isSameY1 && isSameY2 && coordinateNum == 4){
			Log.i("4point", "1");
			if(isLanguege.equals("english")){
				if(avrDistanceY >= avrDistanceX * 1.5){
					pointList.add(1);
					pointList.add(3);
					pointList.add(4);
					pointList.add(6);
				}
				else{
					pointList.add(1);
					pointList.add(2);
					pointList.add(4);
					pointList.add(5);
					if(dpHeight / 2 < avrY){
						for (int i = 0; i < 4; i++)
							pointList.set(i, pointList.get(i) + 1);
					}
				}
			}
			else{
				pointList.add(1);
				pointList.add(2);
				pointList.add(4);
				pointList.add(5);
				if(dpHeight / 2 < avrY){
					for (int i = 0; i < 4; i++)
						pointList.set(i, pointList.get(i) + 1);
				}
			}

		}
		else{
			Log.i("4point", "2");
			double avrMaxMinY = (coordinateList.get(coordinateNum-1).getY() + coordinateList.get(0).getY()) / 2;
			double adjustMinY = coordinateList.get(0).getY() -  dpHeight / 10;
			double rangeUnit = (avrMaxMinY - adjustMinY) / 3 * 2;

			for(int i = 0; i < coordinateNum; i++){
				if (coordinateList.get(i).getX() >= avrX)
					coordinateList.get(i).setPositX(true);

				int y = coordinateList.get(i).getY();
				if(y <= adjustMinY + rangeUnit)
					coordinateList.get(i).setrangeNum(1);
				else if(y > adjustMinY + rangeUnit * 2)
					coordinateList.get(i).setrangeNum(3);
				else
					coordinateList.get(i).setrangeNum(2);
			}
			Comparator<Coordinate> lowToHighRange = new Comparator<Coordinate>() {
				public int compare(final Coordinate o1, final Coordinate o2) {
					int firstValue = 0;
					int secondValue = 0;

					firstValue = (int) o1.getrangeNum();
					secondValue = (int) o2.getrangeNum();

					// 오름차순 정렬
					if (firstValue < secondValue) {
						return -1;
					} else if (firstValue > secondValue) {
						return 1;
					} else  {
						return 0;
					}
				}
			};
			Collections.sort(coordinateList, lowToHighRange);
			for (Coordinate k : coordinateList)
				pointList.add(k.getrangeNum());

			int code = Braille.pointToBrailleCode(pointList);
			if (code == 1346){
				if(dpHeight / 2.75 > avrY) {
					pointList.clear();
					pointList.add(1);
					pointList.add(2);
					pointList.add(4);
					pointList.add(5);
				}
				if(dpHeight - (dpHeight / 2.75) < avrY){
					for (int i = 0; i < 4; i++)
						pointList.set(i, pointList.get(i) + 1);
				}
			}
		}
	}

	public void coordinateToPointNum() {
		int coordinateNum = coordinateList.size();

		if (coordinateNum == 1) {
			coordinateNum1();
		}
		else {

			boolean isVertical,  isHorizontal;

			Collections.sort(coordinateList, lowToHighX);

			isVertical = isSameRange(coordinateList, 'x');

			double avrX = (coordinateList.get(0).getX()
					+ coordinateList.get(coordinateList.size() - 1).getX()) / 2;

			Collections.sort(coordinateList, lowToHighY);

			isHorizontal = isSameRange(coordinateList, 'y');

			double avrY = (coordinateList.get(0).getY()
					+ coordinateList.get(coordinateList.size() - 1).getY()) / 2;

			if (coordinateNum == 2) {
				coordinateNum2(isVertical, isHorizontal, avrX, avrY);
			} else if (coordinateNum == 3) {
				coordinateNum3(isVertical, avrX, avrY);

			} else if (coordinateNum == 4 || coordinateNum == 5) {
				coordinateNum4();
			}
		}
		if (!pointList.isEmpty())
			Log.i("code", "" + Braille.pointToBrailleCode(pointList));
		if (!coordinateList.isEmpty())
			coordinateList.clear();

	}

	public double[] circumscriptionCenter(ArrayList<Coordinate> coordinateList) {
		//점의 좌표
		double x1 = coordinateList.get(0).getX();
		double x2 = coordinateList.get(1).getX();
		double x3 = coordinateList.get(2).getX();
		double y1 = coordinateList.get(0).getY();
		double y2 = coordinateList.get(1).getY();
		double y3 = coordinateList.get(2).getY();

		//두 수직 이등분선의 기울기를 입력
		double d1 = (x2 - x1) / (y2 - y1);
		double d2 = (x3 - x2) / (y3 - y2);

		//원의 중점을 구함
		double cx = ((y3 - y1) + (x2 + x3) * d2 - (x1 + x2) * d1) / (2 * (d2 - d1));
		double cy = -d1 * (cx - (x1 + x2) / 2) + (y1 + y2) / 2;
		double[] centerPoint = {cx, cy};
		return centerPoint;
	}

	public double[] inscribedCenter(ArrayList<Coordinate> coordinateList) {
		// 선분의 중심
		double x1 = (coordinateList.get(0).getX() + coordinateList.get(1).getX()) / 2;
		double x2 = (coordinateList.get(1).getX() + coordinateList.get(2).getX()) / 2;
		double x3 = (coordinateList.get(2).getX() + coordinateList.get(0).getX()) / 2;
		double y1 = (coordinateList.get(0).getY() + coordinateList.get(1).getY()) / 2;
		double y2 = (coordinateList.get(1).getY() + coordinateList.get(2).getY()) / 2;
		double y3 = (coordinateList.get(2).getY() + coordinateList.get(0).getY()) / 2;

		//두 수직 이등분선의 기울기를 입력
		double d1 = (x2 - x1) / (y2 - y1);
		double d2 = (x3 - x2) / (y3 - y2);

		//원의 중점을 구함
		double cx = ((y3 - y1) + (x2 + x3) * d2 - (x1 + x2) * d1) / (2 * (d2 - d1));
		double cy = -d1 * (cx - (x1 + x2) / 2) + (y1 + y2) / 2;
		double[] centerPoint = {cx, cy};
		return centerPoint;
	}

	public void changeNumber(){
		if (isLanguege.equals("number")) {
			isLanguege = "korean";
			tts.speak("한글", TextToSpeech.QUEUE_FLUSH, null);
			jamoList.clear();
			jamoPointList.clear();
			pointList.clear();
			coordinateList.clear();
		} else {
			isLanguege = "number";
			tts.speak("숫자", TextToSpeech.QUEUE_FLUSH, null);
			jamoList.clear();
			jamoPointList.clear();
			pointList.clear();
			coordinateList.clear();
		}
	}
	public void changeEnglish(){
		if (isLanguege.equals("english")) {
			isLanguege = "korean";
			tts.speak("한글", TextToSpeech.QUEUE_FLUSH, null);
			pointList.clear();
			jamoList.clear();
			jamoPointList.clear();
			pointList.clear();
			coordinateList.clear();
		} else {
			isLanguege = "english";
			tts.speak("영어", TextToSpeech.QUEUE_FLUSH, null);
			pointList.clear();
			jamoList.clear();
			jamoPointList.clear();
			pointList.clear();
			coordinateList.clear();
		}
	}

	public void swipeUp() {
		Log.i("swipeUp", "");
		tts.speak("입력한 내용을 지우고, 키보드를 종료합니다", TextToSpeech.QUEUE_FLUSH, null);
		if(inputText.length()>0){
			for(int i = 0; i < inputText.length(); i++){
				keyDownUp(KeyEvent.KEYCODE_DEL);
			}
		}
		inputText = "";
		pointList.clear();
		jamoPointList.clear();
		coordinateList.clear();
		jamoList.clear();
		requestHideSelf(0);
		Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		vibe.vibrate(35);
		Log.i("InPut", inputText);
	}

	public void swipeDown() {
		Log.i("swipeDown", "");
		if(isLanguege.equals("korean") && Braille.hangeulCombine(jamoList) != 0) {
			getCurrentInputConnection().commitText
						(String.valueOf((char) Braille.hangeulCombine(jamoList)), 1);
			inputText += String.valueOf((char) Braille.hangeulCombine(jamoList));
		}

		if(inputText.length() > 0){
			tts.speak(inputText + "을 입력합니다", TextToSpeech.QUEUE_FLUSH, null);
		}
		else{
			tts.speak("입력한 내용이 없습니다.", TextToSpeech.QUEUE_FLUSH, null);
		}
		keyDownUp(KeyEvent.KEYCODE_ENTER);
		requestHideSelf(0);
		Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		vibe.vibrate(35);
		Log.i("InPut", inputText);
		onFinishInput();
	}

	public void swipeLeft() {
		Log.i("swipeLeft", "");
		if(isLanguege.equals("korean") && !jamoList.isEmpty() && Braille.hangeulCombine(jamoList) != 0) {
			getCurrentInputConnection().commitText
					(String.valueOf((char) Braille.hangeulCombine(jamoList)), 1);
			inputText += String.valueOf((char) Braille.hangeulCombine(jamoList));
			inputText = inputText.substring(0, inputText.length() - 1);
		}
		else {
			if(inputText.length()>0)
			inputText = inputText.substring(0, inputText.length() - 1);
		}
		keyDownUp(KeyEvent.KEYCODE_DEL);
		jamoList.clear();
		jamoPointList.clear();
		pointList.clear();
		tts.speak("지우기", TextToSpeech.QUEUE_FLUSH, null);
		Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		vibe.vibrate(35);
		Log.i("InPut", inputText);
	}


	public void swipeRight() {
		Log.i("swipeRight", "");
		Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		vibe.vibrate(35);

		if (coordinateList.isEmpty()) {
			if((Braille.hangeulCombine(jamoList) != 0 && isLanguege.equals("korean"))) {
				getCurrentInputConnection().commitText
						(String.valueOf((char) Braille.hangeulCombine(jamoList)), 1);
				inputText += (String.valueOf((char) Braille.hangeulCombine(jamoList)) + " ");
			}
			else{
				inputText += (" ");
			}
			keyDownUp(KeyEvent.KEYCODE_SPACE);
			tts.speak("space", TextToSpeech.QUEUE_FLUSH, null);
			jamoList.clear();
			jamoPointList.clear();
			pointList.clear();
			Log.i("InPut", inputText);
			return;
		}
		coordinateToPointNum();
		String input = "";
		if (Braille.pointToBrailleCode(pointList) == Braille.BRAILLE_KEYCODE_CNG_NUMBER) {//수표 눌렀을때
			changeNumber();
			return;
		} else if (Braille.pointToBrailleCode(pointList) == Braille.BRAILLE_KEYCODE_CNG_ENG) {//영어표 눌렀을때
			changeEnglish();
			return;
		}
		else { //일반 점자 입력했을 때
			if (isLanguege.equals("korean")) {
				int jamoListSize = jamoList.size();
				int korUNI = Braille.inputKorean(pointList, shiftMod);
				if (korUNI != 0) {
					jamoList.add(korUNI);
					jamoPointList.add(Braille.pointToBrailleCode(pointList));
					jamoListSize = jamoList.size();

					Log.i("BEFORE", "BEFORE");
					for (int i = 0; i < jamoList.size(); i++)
						Log.i("jamoList " + i, String.valueOf((char) (int) jamoList.get(i)) + "");
				}
				else{
					if(jamoListSize > 0) {
						jamoList.remove(jamoList.size() - 1);
						jamoPointList.remove(jamoPointList.size() - 1);
					}
					coordinateList.clear();
					pointList.clear();
					tts.speak("잘못입력하였습니다", TextToSpeech.QUEUE_FLUSH, null);
					return ;
				}

				if(jamoListSize == 2){
					//둘 다 자음일 때
					if(! Braille.isMoeum(jamoList.get(0)) && ! Braille.isMoeum(jamoList.get(1))){
						getCurrentInputConnection().commitText("" + (char)(int)jamoList.get(0), 1);
						inputText += ("" + (char)(int)jamoList.get(0));
						jamoList.set(0, jamoList.get(1));
						jamoList.remove(1);
						jamoPointList.set(0, jamoPointList.get(1));
						jamoPointList.remove(1);
						Log.i("2개일때", "1");
					}
					else{
						if(Braille.hangeulCombine(jamoList) == 0){
							jamoList.remove(jamoList.size() - 1);
							jamoPointList.remove(jamoPointList.size() - 1);
							pointList.clear();
							coordinateList.clear();
							tts.speak("잘못입력하였습니다", TextToSpeech.QUEUE_FLUSH, null);
							Log.i("2개일때", "2");
							return;
						}
						else Log.i("2개일때", "3");
					}
				}
				else if (jamoListSize == 3){
					//세번째 게 모음일 때
					if(Braille.isMoeum(jamoList.get(2))){
						int combineJamo = Braille.jamoCombain
								(jamoPointList.get(jamoListSize - 2), jamoPointList.get(jamoListSize - 1));
						if(combineJamo > 0){//겹모음
							jamoList.set(1, combineJamo);
							jamoList.remove(2);
							jamoPointList.set(1, Braille.uniToBrailleCode(combineJamo));
							jamoPointList.remove(2);
							Log.i("3개일때", "1");
						}
						else{
							getCurrentInputConnection().commitText
									(String.valueOf((char) Braille.hangeulCombine(jamoList)), 1);
							inputText += (String.valueOf((char) Braille.hangeulCombine(jamoList)));
							jamoList.set(0, jamoList.get(2));
							jamoList.remove(2);
							jamoList.remove(1);
							jamoPointList.set(0, jamoPointList.get(2));
							jamoPointList.remove(2);
							jamoPointList.remove(1);
							Log.i("3개일때", "2");
						}
					}
					else{ //세번째 게 자음
						Log.i("세번째 게 자음: ", !Braille.isJong(jamoList.get(2)) + "");
						if(!Braille.isJong(jamoList.get(2))){ //종성자음이 아닐 때
							int temp1 = jamoList.get(2);
							int temp2 = jamoPointList.get(2);
							jamoList.remove(2);
							jamoPointList.remove(2);
							getCurrentInputConnection().commitText
									(String.valueOf((char) Braille.hangeulCombine(jamoList)), 1);
							inputText += (String.valueOf((char) Braille.hangeulCombine(jamoList)));
							jamoList.clear();
							jamoPointList.clear();
							jamoList.add(temp1);
							jamoPointList.add(temp2);
							Log.i("3개일때", "3");
						}
						else Log.i("3개일때", "4");
					}
				}
				else if (jamoListSize == 4){
					//네번째가 모음
					if(Braille.isMoeum(jamoList.get(3))){
						if(isDoubleJong){ // 세번째 자가 겹받침일 때
							int temp1 = jamoList.get(3);
							int temp2 = jamoPointList.get(3);

							jamoList.remove(3);
							jamoPointList.remove(3);
							jamoList.set(2, temp1Uni);
							jamoPointList.set(2, temp1BrailleCode);
							getCurrentInputConnection().commitText(
									String.valueOf((char) Braille.hangeulCombine(jamoList)), 1);
							inputText += (String.valueOf((char) Braille.hangeulCombine(jamoList)));

							jamoList.set(0, temp2Uni);
							jamoList.set(1, temp1);
							jamoList.remove(2);
							jamoPointList.set(0, temp2BrailleCode);
							jamoPointList.set(1, temp2);
							jamoPointList.remove(2);
							isDoubleJong = false;
							Log.i("4개일때", "1");
						}
						else {
							int temp1 = jamoList.get(2);
							int temp2 = jamoList.get(3);
							int temp3 = jamoPointList.get(2);
							int temp4 = jamoPointList.get(3);
							jamoList.remove(3);
							jamoList.remove(2);
							jamoPointList.remove(3);
							jamoPointList.remove(2);
							getCurrentInputConnection().commitText
									(String.valueOf((char) Braille.hangeulCombine(jamoList)), 1);
							inputText += (String.valueOf((char) Braille.hangeulCombine(jamoList)));

							jamoList.set(0, temp1);
							jamoList.set(1, temp2);
							jamoPointList.set(0, temp3);
							jamoPointList.set(1, temp4);
							Log.i("4개일때", "2");
						}
					}
					else{ // 자음
						int combineJamo = Braille.jamoCombain
								(jamoPointList.get(jamoListSize - 2), jamoPointList.get(jamoListSize - 1));

						if (combineJamo > 0) {//겹받침
							temp1Uni = jamoList.get(2);
							temp2Uni = jamoList.get(3);
							jamoList.set(2, combineJamo);
							jamoList.remove(3);
							temp1BrailleCode = jamoPointList.get(2);
							temp2BrailleCode = jamoPointList.get(3);
							jamoPointList.set(2, Braille.uniToBrailleCode(combineJamo));
							temp2BrailleCode = jamoPointList.get(3);
							jamoPointList.remove(3);
							isDoubleJong = true;
							Log.i("temp1Uni", "" + (char) temp1Uni);
							Log.i("temp2Uni", "" + (char) temp2Uni);
							Log.i("4개일때", "3");
						} else {
							int temp = jamoList.get(3);
							jamoList.remove(3);
							getCurrentInputConnection().commitText
									(String.valueOf((char) Braille.hangeulCombine(jamoList)), 1);
							inputText += (String.valueOf((char) Braille.hangeulCombine(jamoList)));

							jamoList.set(0, temp);
							jamoList.remove(2);
							jamoList.remove(1);
							jamoPointList.set(0, jamoPointList.get(3));
							jamoPointList.remove(3);
							jamoPointList.remove(2);
							jamoPointList.remove(1);
							isDoubleJong = false;
							Log.i("4개일때", "4");
						}
					}
				}
				input = String.valueOf((char) Braille.hangeulCombine(jamoList));

			} else if (isLanguege.equals("number")) {
				if(Braille.inputNumber(pointList) != 0) {
					input = String.valueOf((char) Braille.inputNumber(pointList));
					getCurrentInputConnection().commitText(input, 1);
					inputText += input;
				}
				else{
					pointList.clear();
					tts.speak("잘못입력하였습니다.", TextToSpeech.QUEUE_FLUSH, null);
					return;
				}
			} else if (isLanguege.equals("english")) {
				if(Braille.inputEng(pointList, shiftMod) != 0) {
					input = String.valueOf((char) Braille.inputEng(pointList, shiftMod));
					getCurrentInputConnection().commitText(input, 1);
					inputText += input;
				}
				else{
					pointList.clear();
					tts.speak("잘못입력하였습니다.", TextToSpeech.QUEUE_FLUSH, null);
					return;
				}
			}
			if (isLanguege.equals("korean") && jamoList.size() == 1) {
				tts.speak(Braille.jamoName(jamoList.get(0)), TextToSpeech.QUEUE_FLUSH, null);
				if (Braille.isMoeum(jamoList.get(0))) {
					input = Braille.jamoName(jamoList.get(0));
					jamoList.add(jamoList.get(0));
					jamoPointList.add(Braille.pointToBrailleCode(pointList));
					jamoList.set(0, (int) 'ㅇ');
					jamoPointList.set(0, 1245);
				}
			} else {
				tts.speak(input, TextToSpeech.QUEUE_FLUSH, null);
			}
		}
		Log.i("AFTER", "AFTER");
		for (int i = 0; i < jamoList.size(); i++)
			Log.i("jamoList " + i, String.valueOf((char) (int) jamoList.get(i)) + "");
		Log.i("inputText", inputText);
		pointList.clear();
		if (shiftMod)
			shiftMod = false;

		Log.i("InPut", inputText);
	}

	public void whenSingleTapUp(MotionEvent me) {
		Log.i("onSingleTapUp", (int) me.getX() + ", " + (int) me.getY());
		Coordinate temp = new Coordinate((int) me.getX(), (int) me.getY());
		coordinateList.add(temp);
		Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		vibe.vibrate(35);
	}

	public void whenDoubleTap(MotionEvent e) {
		Log.i("onDoubleTap", e.getX() + ", " + e.getY());
		coordinateList.remove(coordinateList.size()-1);
		shiftMod = true;
		tts.speak("Shift", TextToSpeech.QUEUE_FLUSH, null);
		Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		vibe.vibrate(35);
	}

	public void whenLongPressed() {
		Log.i("LongPress", "");
		if(isLanguege.equals("korean"))
			inputText += (String.valueOf((char) Braille.hangeulCombine(jamoList)));

		if(inputText.replace(" ", "").length() == 0)
			tts.speak("입력한 내용이 없습니다.", TextToSpeech.QUEUE_FLUSH, null);
		else {
			tts.speak(inputText, TextToSpeech.QUEUE_FLUSH, null);
			inputText = inputText.substring(0, inputText.length()-1);
		}
		Log.i("inputText", inputText);
		Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		vibe.vibrate(35);
	}

	Comparator<Coordinate> lowToHighX = new Comparator<Coordinate>() {
		public int compare(final Coordinate o1, final Coordinate o2) {
			int firstValue = 0;
			int secondValue = 0;

			firstValue = (int) o1.getX();
			secondValue = (int) o2.getX();

			// 오름차순 정렬
			if (firstValue < secondValue) {
				return -1;
			} else if (firstValue > secondValue) {
				return 1;
			} else /* if (firstValue == secondValue) */ {
				return 0;
			}
		}
	};
	Comparator<Coordinate> lowToHighY = new Comparator<Coordinate>() {
		public int compare(final Coordinate o1, final Coordinate o2) {
			int firstValue = 0;
			int secondValue = 0;

			firstValue = (int) o1.getY();
			secondValue = (int) o2.getY();

			// 오름차순 정렬
			if (firstValue < secondValue) {
				return -1;
			} else if (firstValue > secondValue) {
				return 1;
			} else /* if (firstValue == secondValue) */ {
				return 0;
			}
		}
	};

	private class Coordinate {
		private int x;
		private int y;
		private boolean positX = false;
		private int rangeNum = 0;

		public Coordinate(int x, int y) {
			this.x = x;
			this.y = y;
		}

		public int getX() {
			return x;
		}

		public int getY() {
			return y;
		}

		public boolean getPositX() {
			return positX;
		}

		public int getrangeNum() {
			return rangeNum;
		}

		public void setPositX(boolean positX) {
			this.positX = positX;
		}

		public void setrangeNum(int rangeNum) {
			this.rangeNum = rangeNum;
			if (positX) this.rangeNum = rangeNum + 3;
		}
	}

	private class CustomGesture extends GestureDetector.SimpleOnGestureListener {
		private int dpWidth;
		private int dpHeight;
		private int swipe_Min_Distance = 200;
		private int swipe_Max_Distance_X = 1000;
		private int swipe_Max_Distance_Y = 1000;
		private int swipe_Min_Velocity = 50;

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
		                       float velocityY) {
			DisplayMetrics dm = getApplicationContext().getResources().getDisplayMetrics();
			dpWidth = dm.widthPixels;
			dpHeight = dm.heightPixels;
			swipe_Min_Distance = (int) (dpWidth * 5 / 39);
			swipe_Max_Distance_X = (int) (dpWidth * 4 / 9);
			swipe_Max_Distance_Y = (int) (dpHeight * 13 * 48);

			final float xDistance = Math.abs(e1.getX() - e2.getX());
			final float yDistance = Math.abs(e1.getY() - e2.getY());

			if (xDistance > this.swipe_Max_Distance_X || yDistance > this.swipe_Max_Distance_Y)
				return false;

			velocityX = Math.abs(velocityX);
			velocityY = Math.abs(velocityY);
			boolean result = false;

			if (velocityX > this.swipe_Min_Velocity && xDistance > this.swipe_Min_Distance) {
				if (e1.getX() > e2.getX()) // right to left
					BrailleKeyboardService.this.swipeLeft();
				else
					BrailleKeyboardService.this.swipeRight();
				result = true;
			} else if (velocityY > this.swipe_Min_Velocity && yDistance > this.swipe_Min_Distance) {
				if (e1.getY() > e2.getY()) // bottom to up
					BrailleKeyboardService.this.swipeUp();
				else
					BrailleKeyboardService.this.swipeDown();
				result = true;
			}

			return result;
		}

		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			BrailleKeyboardService.this.whenSingleTapUp(e);
			return false;
		}

		@Override
		public boolean onDoubleTap(MotionEvent e) {
			BrailleKeyboardService.this.whenDoubleTap(e);
			return false;
		}

		@Override
		public void onLongPress(MotionEvent e) {
			Log.i("onLongPress", (int) e.getX() + ", " + (int) e.getY());
			BrailleKeyboardService.this.whenLongPressed();
		}

	}
}