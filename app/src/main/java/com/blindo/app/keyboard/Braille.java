package com.blindo.app.keyboard;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by JeongGyu on 2015-07-26.
 */
public class Braille {
	// ㄱ  ㄲ  ㄴ  ㄷ  ㄸ  ㄹ  ㅁ  ㅂ  ㅃ  ㅅ  ㅆ  ㅇ  ㅈ  ㅉ  ㅊ  ㅋ  ㅌ  ㅍ  ㅎ
	private static final int[] UNI_CHO = {
			12593, 12594, 12596, 12599, 12600, 12601, 12609, 12610,
			12611, 12613, 12614, 12615, 12616, 12617, 12618, 12619,
			12620, 12621, 12622};

	// ㅏ  ㅐ  ㅑ  ㅒ  ㅓ  ㅔ  ㅕ  ㅖ  ㅗ  ㅘ  ㅙ  ㅚ  ㅛ  ㅜ  ㅝ  ㅞ  ㅟ  ㅠ  ㅡ  ㅢ  ㅣ
	private static final int[] UNI_JUNG = {
			12623, 12624, 12625, 12626, 12627, 12628, 12629, 12630,
			12631, 12632, 12633, 12634, 12635, 12636, 12637, 12638,
			12639, 12640, 12641, 12642, 12643};
	//  ㄱ  ㄲ  ㄳ  ㄴ  ㄵ  ㄶ  ㄷ  ㄹ  ㄺ  ㄻ  ㄼ  ㄽ  ㄾ  ㄿ  ㅀ  ㅁ  ㅂ  ㅄ  ㅅ  ㅆ  ㅇ  ㅈ  ㅊ  ㅋ  ㅌ  ㅍ  ㅎ
	private static final int[] UNI_JONG = {
			12593, 12594, 12595, 12596, 12597, 12598, 12599, 12601,
			12602, 12603, 12604, 12605, 12606, 12607, 12608, 12609,
			12610, 12612, 12613, 12614, 12615, 12616, 12618, 12619,
			12620, 12621, 12622};

	// ㄱ  ㄲ  ㄴ  ㄷ  ㄸ  ㄹ  ㅁ  ㅂ  ㅃ  ㅅ  ㅆ  ㅇ  ㅈ  ㅉ  ㅊ  ㅋ  ㅌ  ㅍ  ㅎ
	private static final int[] BRAILLE_KEYCODE_CHO = {
			4, 404, 14, 24, 24024, 5, 15, 45,
			45045, 6, 606, 1245, 46, 46046, 56, 124,
			125, 145, 245};

	// ㅏ  ㅐ  ㅑ  ㅒ  ㅓ  ㅔ  ㅕ  ㅖ
	//  ㅗ  ㅘ  ㅙ  ㅚ  ㅛ  ㅜ  ㅝ  ㅞ
	//  ㅟ  ㅠ  ㅡ  ㅢ  ㅣ
	private static final int[] BRAILLE_KEYCODE_JUNG = {
			126, 1235, 345, 34501235, 234, 1345, 156, 34,
			136, 1236, 123601235, 13456, 346, 134, 1234, 123401235,
			13401235, 146, 246, 2456, 135};

	//  ㄱ  ㄲ  ㄳ  ㄴ  ㄵ  ㄶ  ㄷ  ㄹ  ㄺ  ㄻ  ㄼ  ㄽ  ㄾ  ㄿ  ㅀ
	//  ㅁ  ㅂ  ㅄ  ㅅ  ㅆ  ㅇ  ㅈ  ㅊ  ㅋ  ㅌ  ㅍ  ㅎ
	private static final int[] BRAILLE_KEYCODE_JONG = {
			1, 101, 103, 25, 25013, 250356, 35, 2, 201, 2026, 2012, 203, 20236, 20256, 20356,
			26, 12, 1203, 3, 303, 2356, 13, 23, 235, 236, 256, 356};

	private static final int[] BRAILLE_KEYCODE_NUMBER = { //0~9
			245, 1, 12, 14, 145, 15, 124, 1245, 125, 24};

	private static final int[] BRAILLE_KEYCODE_ENG = { //a to z
			1, 12, 14, 145, 15, 124, 1245, 125, 24, 245, 13, 123, 134, 1345,
			135, 1234, 12345, 1235, 234, 2345, 136, 1236, 2456, 1346, 13456, 1356};

	public static final int BRAILLE_KEYCODE_CNG_NUMBER = 3456;


	public static final int BRAILLE_KEYCODE_CNG_ENG = 356;


	private static final String[] CHO_NAME = {"기역", "쌍기역", "니은", "디귿", "쌍디귿", "리을", "미음", "비읍", "쌍비읍",
			"시옷", "쌍시옷", "이응", "지읒", "쌍지읒", "치읓", "키읔", "티읕", "피읖", "히흫"};
	private static final String[] JUNG_NAME = {"아", "애", "야", "얘", "어", "에", "여", "예", "오", "와", "왜", "외", "요",
			"우", "워", "웨", "위", "유", "으", "의", "이"};



	public static int inputKorean(ArrayList<Integer> pointList, boolean shiftMod){

		ArrayList<Integer> hanguelBrailleKeyCodeList = new ArrayList<Integer>();
		for(int temp : BRAILLE_KEYCODE_CHO)
			hanguelBrailleKeyCodeList.add(temp);

		for(int temp : BRAILLE_KEYCODE_JUNG)
			hanguelBrailleKeyCodeList.add(temp);

		for(int temp : BRAILLE_KEYCODE_JONG)
			hanguelBrailleKeyCodeList.add(temp);

		ArrayList<Integer> hanguelUniList = new ArrayList<Integer>();
		for(int temp : UNI_CHO)
			hanguelUniList.add(temp);

		for(int temp : UNI_JUNG)
			hanguelUniList.add(temp);

		for(int temp : UNI_JONG)
			hanguelUniList.add(temp);

		int code = pointToBrailleCode(pointList);
		if(shiftMod)
			code = Integer.parseInt(code + "0" + code);

		int hangeulIndex = hanguelBrailleKeyCodeList.indexOf(code);
		if(hangeulIndex == -1)
			return 0;
		else
			return hanguelUniList.get(hangeulIndex);
	}

	public static int inputNumber(ArrayList<Integer> pointList){

		ArrayList<Integer> numberBrailleKeyCodeList = new ArrayList<Integer>();
		for(int temp : BRAILLE_KEYCODE_NUMBER)
			numberBrailleKeyCodeList.add(temp);

		int code = pointToBrailleCode(pointList);

		int numberIndex = numberBrailleKeyCodeList.indexOf(code);

		if(numberIndex == -1)
			return 0;
		else
			return 48 + numberIndex;
	}

	public static int inputEng (ArrayList<Integer> pointList, boolean shiftMod){

		ArrayList<Integer> engBrailleKeyCodeList = new ArrayList<Integer>();
		for(int temp : BRAILLE_KEYCODE_ENG)
			engBrailleKeyCodeList.add(temp);

		int code = pointToBrailleCode(pointList);
		Log.i("inputEng", "code: " + code);
		int engIndex = engBrailleKeyCodeList.indexOf(code);
		Log.i("inputEng", "engIndex: " + engIndex);
		if(engIndex == -1)
			return 0;
		else if (shiftMod)
			return 65 + engIndex;
		else
			return 97 + engIndex;
	}

	public static int pointToBrailleCode(ArrayList<Integer> pointList){
		Collections.sort(pointList);
		int result = 0;
		int j=0;
		for(int i=pointList.size() - 1; i>=0 ; i--){
			result = result + pointList.get(j) * (int)Math.pow(10, i);
			j++;
		}
		return result;
	}
	public static int hangeulCombine(ArrayList<Integer> jamoList) {
		int x = 0;
		if(jamoList.size() == 1){
			return jamoList.get(0);
		}
		else if(jamoList.isEmpty())
			return 0;
		else if (jamoList.size() == 2){
			ArrayList<Integer> choUniList = new ArrayList<Integer>();
			for(int temp : UNI_CHO)
				choUniList.add(temp);
			ArrayList<Integer> jungUniList = new ArrayList<Integer>();
			for(int temp : UNI_JUNG)
				jungUniList.add(temp);

			x = (choUniList.indexOf(jamoList.get(0)) * 21 * 28)
					+ (jungUniList.indexOf(jamoList.get(1)) * 28);
		}
		else if (jamoList.size() == 3){
			ArrayList<Integer> choUniList = new ArrayList<Integer>();
			for(int temp : UNI_CHO)
				choUniList.add(temp);
			ArrayList<Integer> jungUniList = new ArrayList<Integer>();
			for(int temp : UNI_JUNG)
				jungUniList.add(temp);
			ArrayList<Integer> jongUniList = new ArrayList<Integer>();
			for(int temp : UNI_JONG)
				jongUniList.add(temp);

			x = (choUniList.indexOf(jamoList.get(0)) * 21 * 28)
					+ (jungUniList.indexOf(jamoList.get(1)) * 28)
					+ (jongUniList.indexOf(jamoList.get(2)) + 1);
		}
		x = x + 0xAC00;
		if(x < '가' && x > '힣')
			x = 0;
		return x;
	}
	public static boolean isMoeum(int unicode) {
		boolean result = false;
		if(unicode >= (int)'ㅏ' && unicode <= (int)'ㅣ') {
			result = true;
		}
		return result;
	}
	public static int uniToBrailleCode(int uni){
		ArrayList<Integer> hanguelBrailleKeyCodeList = new ArrayList<Integer>();
		for(int temp : BRAILLE_KEYCODE_CHO)
			hanguelBrailleKeyCodeList.add(temp);

		for(int temp : BRAILLE_KEYCODE_JUNG)
			hanguelBrailleKeyCodeList.add(temp);

		for(int temp : BRAILLE_KEYCODE_JONG)
			hanguelBrailleKeyCodeList.add(temp);

		ArrayList<Integer> hanguelUniList = new ArrayList<Integer>();
		for(int temp : UNI_CHO)
			hanguelUniList.add(temp);

		for(int temp : UNI_JUNG)
			hanguelUniList.add(temp);

		for(int temp : UNI_JONG)
			hanguelUniList.add(temp);

		int code = hanguelUniList.indexOf(uni);
		if(code == -1)
			return 0;
		else
			return code;

	}
	public static boolean isJong(int uniCode){
		ArrayList<Integer> hanguelUniList = new ArrayList<Integer>();
		for(int temp : UNI_JONG)
			hanguelUniList.add(temp);
		int result = hanguelUniList.indexOf(uniCode);
		if(result == -1)
			return false;
		return true;
	}
	public static String jamoName(int unicode){
		ArrayList<Integer> jamoUni = new ArrayList<Integer>();
		for(int k: UNI_CHO){
			jamoUni.add(k);
		}
		int index = jamoUni.indexOf(unicode);
		if(jamoUni.indexOf(unicode) == -1) {
			jamoUni.clear();
			for (int k : UNI_JUNG) {
				jamoUni.add(k);
			}
			index = jamoUni.indexOf(unicode);
			return JUNG_NAME[index];
		}
		else{
			return CHO_NAME[index];
		}
	}

	public static int jamoCombain(int jamoUni1, int jamoUni2){
		ArrayList<Integer> hanguelBrailleKeyCodeList = new ArrayList<Integer>();
		for(int temp : BRAILLE_KEYCODE_CHO)
			hanguelBrailleKeyCodeList.add(temp);

		for(int temp : BRAILLE_KEYCODE_JUNG)
			hanguelBrailleKeyCodeList.add(temp);

		for(int temp : BRAILLE_KEYCODE_JONG)
			hanguelBrailleKeyCodeList.add(temp);

		ArrayList<Integer> hanguelUniList = new ArrayList<Integer>();
		for(int temp : UNI_CHO)
			hanguelUniList.add(temp);

		for(int temp : UNI_JUNG)
			hanguelUniList.add(temp);

		for(int temp : UNI_JONG)
			hanguelUniList.add(temp);

		int code = Integer.parseInt(jamoUni1 + "0" + jamoUni2);

		int hangeulIndex = hanguelBrailleKeyCodeList.indexOf(code);
		Log.i("hangeulIndex : ", hangeulIndex + "");
		if(hangeulIndex == -1)
			return 0;
		else
			return hanguelUniList.get(hangeulIndex);
	}
}
