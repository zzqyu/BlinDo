package com.blindo.app;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.NetworkOnMainThreadException;
import android.os.StrictMode;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by JeongGyu on 2015-08-20.
 */
public class PublicMethods {
	public static String[] CHO_LIST = {"ㄱ", "ㄲ", "ㄴ", "ㄷ", "ㄸ", "ㄹ",
			"ㅁ", "ㅂ", "ㅃ", "ㅅ", "ㅆ", "ㅇ", "ㅈ", "ㅉ","ㅊ", "ㅋ", "ㅌ", "ㅍ", "ㅎ"};

	private static ArrayList<String> phoneBookNumList = new ArrayList<String>();
	private static ArrayList<String> phoneBookNameList = new ArrayList<String>();

	private static ArrayList<String> misscallName = new ArrayList<String>();
	private static ArrayList<String> misscallNumber = new ArrayList<String>();

	private static String missMessageName0 = "";
	private static String missMessageNumber0 = "";

	private static int alarmList0 = 0;
	private static int alarmList1 = 0;

	public static String choSeparation(String keyword){
		String result = "";
		for(int i = 0; i < keyword.length(); i++){
			int in_char = keyword.charAt(i);

			if(in_char >= 44032 && in_char <= 55203){
				in_char = in_char - 44032;
				result += CHO_LIST[in_char / 588];
			}
			else{
				result += " ";
			}
		}
		return result;
	}

	public static String phoneNumToHangeul(String phoneNumber){
		String[] hangeul = {"공", "일", "이", "삼", "사", "오", "육", "칠", "팔", "구"};
		String result = "";
		for(int i = 0; i < phoneNumber.length(); i++){
			if(phoneNumber.charAt(i) >= '0' && phoneNumber.charAt(i) <= '9')
				result += (hangeul[Integer.valueOf(phoneNumber.charAt(i) + "")] + "");
			else
				result += phoneNumber.charAt(i);
		}
		return result;
	}
	public static Boolean network_check(Context context) {
		ConnectivityManager manager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo mobile = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		NetworkInfo wifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

		if (mobile.isConnected() || wifi.isConnected()) {
			return true;
		}
		else {
			return false;
		}
	}

	public static String DownloadHtml(String addr) {
		StrictMode.ThreadPolicy pol = new StrictMode.ThreadPolicy.Builder()
				.permitNetwork().build();
		StrictMode.setThreadPolicy(pol);
		StringBuilder html = new StringBuilder();
		try {
			URL url = new URL(addr);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			if (conn != null) {
				conn.setConnectTimeout(10000);
				conn.setUseCaches(false);
				if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
					BufferedReader br = new BufferedReader(
							new InputStreamReader(conn.getInputStream()));
					for (;;) {
						String line = br.readLine();
						if (line == null)
							break;
						html.append(line + '\n');
					}
					br.close();
				}
				conn.disconnect();
			}
		} catch (NetworkOnMainThreadException e) {
			return "Error : 메인 스레드 네트워크 작업 에러 - " + e.getMessage();
		} catch (Exception e) {
			return "Error : " + e.getMessage();
		}
		return html.toString();
	}

	public static void setAddrNumName(Context context){

		ContentResolver cr = context.getContentResolver();
		Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, new String[]{"_id", "display_name"}, null, null, null);

		cur.moveToFirst();
		while (!cur.isAfterLast()) {
			String id = cur.getString(cur.getColumnIndex(cur.getColumnName(0))); // 한 사람의 아이디
			Cursor numCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{id}, null);
			numCur.moveToFirst();
			String tel = numCur.getString(numCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
			numCur.close();
			String name = cur.getString(cur.getColumnIndex(cur.getColumnName(1)));

			phoneBookNumList.add(tel.replace("-", ""));
			phoneBookNameList.add(name);
			cur.moveToNext();
		}
		cur.close();
	}
	public static ArrayList<String> getPhoneBookNumList(){
		return phoneBookNumList;
	}

	public static ArrayList<String> getPhoneBookNameList(){
		return phoneBookNameList;
	}

	public static ArrayList<String> getMisscallName(){
		return misscallName;
	}
	public static ArrayList<String> getMisscallNumber(){
		return misscallNumber;
	}

	public static String getMissMessageName0(){
		return missMessageName0;
	}
	public static String getMissMessageNumber0(){
		return missMessageNumber0;
	}

	public static int getAlarmList0(){
		return alarmList0;
	}
	public static int getAlarmList1(){
		return alarmList1;
	}

	public static String missCall(Context context) {
		String call = "";
		ContentResolver cr = context.getContentResolver();
		Cursor cursor = cr.query(CallLog.Calls.CONTENT_URI,
				new String[]{"number", "type", "date", "name"}, null, null, null);

		int number = cursor.getColumnIndex(cursor.getColumnName(0));
		int type = cursor.getColumnIndex(cursor.getColumnName(1));
		int date = cursor.getColumnIndex(cursor.getColumnName(2));
		int name = cursor.getColumnIndex(cursor.getColumnName(3));

		int cnt = 0;

		misscallName = new ArrayList<String>();
		misscallNumber = new ArrayList<String>();
		ArrayList<Integer> misscallHour = new ArrayList<Integer>();
		ArrayList<Integer> misscallMinute = new ArrayList<Integer>();

		if(cursor.getCount() == 0)
			return "";

		if (cursor.moveToFirst()) {
			do {
				String callDate = cursor.getString(date);
				Date callDayTime = new Date(Long.valueOf(callDate));
				String callType = cursor.getString(type);
				int dircode = Integer.parseInt(callType);
				long nowTime = System.currentTimeMillis();

				if (nowTime - callDayTime.getTime() < 86400552.963539) {
					if (dircode == CallLog.Calls.MISSED_TYPE)
						cnt++;
				} else
					break;
			} while (cursor.moveToNext());
		}

		cursor.moveToPrevious();
		if (cnt > 0) {
			do {
				String callDate = cursor.getString(date);
				Date callDayTime = new Date(Long.valueOf(callDate));

				String addrName = cursor.getString(name);
				String phNumber = cursor.getString(number);
				String callType = cursor.getString(type);

				String dayList = String.format("%s", callDayTime);
				int dircode = Integer.parseInt(callType);

				if (addrName == null)
					addrName = phNumber;

				if (dircode == CallLog.Calls.MISSED_TYPE) {
					misscallName.add(addrName);
					misscallNumber.add(phNumber);
					misscallHour.add(Integer.parseInt(dayList.substring(11, 13)));
					misscallMinute.add(Integer.parseInt(dayList.substring(14, 16)));

				} else if (dircode == CallLog.Calls.OUTGOING_TYPE || dircode == CallLog.Calls.INCOMING_TYPE) {
					int index = -1;
					index = misscallNumber.indexOf(phNumber);
					if (index != -1) {
						if (misscallHour.get(index) * 60 + misscallMinute.get(index)
								<= Integer.parseInt(dayList.substring(11, 13)) * 60 + Integer.parseInt(dayList.substring(14, 16))) {
							misscallName.remove(index);
							misscallNumber.remove(index);
							misscallHour.remove(index);
							misscallMinute.remove(index);
							if (misscallNumber.indexOf(phNumber) != -1)
								continue;
						}
					}
				}
			} while (cursor.moveToPrevious());

		}

		cursor.close();

		alarmList0 = 0;
		for (int i = 0; i < misscallNumber.size(); i++) {
			alarmList0++;
			call = call + misscallName.get(i) + "/" + misscallHour.get(i) +
					":" + misscallMinute.get(i) + "\n";
		}
		Log.i("call1", call + "");
		Log.i("alarmList0", alarmList0 + "");
		return call;
	}

	public static String missMessage(Context context) {

		String message = "";
		ArrayList<String> phoneBookNumList = new ArrayList<String>();
		ArrayList<String> phoneBookNameList = new ArrayList<String>();
		ArrayList<String> sms = new ArrayList<String>();
		Uri allMessage = Uri.parse("content://sms");
		ContentResolver contentresolver = context.getContentResolver();

		Cursor cursor = contentresolver.query(allMessage,
				new String[]{"address", "body", "read", "date", "type"}, null, null, "date DESC");

		int cnt = 0;
		if (cursor.moveToFirst()) {
			do {
				long read = cursor.getLong(cursor.getColumnIndex(cursor.getColumnName(2)));
				String callDate = cursor.getString(cursor.getColumnIndex(cursor.getColumnName(3)));
				Date callDayTime = new Date(Long.valueOf(callDate));
				long nowTime = System.currentTimeMillis();

				if (nowTime - callDayTime.getTime() < 43200276.4817695){
					if(read == 0)
						cnt++;
					cursor.moveToNext();
				}
				else
					break;
			} while (cursor.moveToNext());
		}
		cursor.moveToPrevious();
		if(cnt > 0){
			cnt = 0;
			do {
				String address = cursor.getString(cursor.getColumnIndex(cursor.getColumnName(0)));
				String body = cursor.getString(cursor.getColumnIndex(cursor.getColumnName(1)));
				long read = cursor.getLong(cursor.getColumnIndex(cursor.getColumnName(2)));
				String type = cursor.getString(cursor.getColumnIndex(cursor.getColumnName(4)));

				if (read == 0 && cnt == 0){
					PublicMethods.setAddrNumName(context);
					phoneBookNumList = PublicMethods.getPhoneBookNumList();
					phoneBookNameList = PublicMethods.getPhoneBookNameList();
				}
				if (read == 0) {
					int index = phoneBookNumList.indexOf(address);
					if(cnt == 0){
						missMessageNumber0 = address;
						missMessageName0 = address;
					}
					if(index == -1){
						sms.add(String.format("%s|%s", address, body));
					}
					else{
						sms.add(String.format("%s|%s", phoneBookNameList.get(index), body));
						if(cnt == 0) missMessageName0 = phoneBookNameList.get(index);
					}
				}
				if (read == 0 && cnt == 0){
					cnt++;
				}

				if(type.equals("2") || type.equals("4")){
					int index = -1;
					for(int i = 0; i< sms.size(); i++){
						int index1 = phoneBookNumList.indexOf(address);
						if(sms.get(i).contains(address)||sms.get(i).contains(phoneBookNameList.get(index1))){
							index = i;
							break;
						}
					}
					if(index != -1){
						sms.remove(index);
						index = -1;
						for(int i = 0; i< sms.size(); i++){
							int index1 = phoneBookNumList.indexOf(address);
							if(sms.get(i).contains(address)||sms.get(i).contains(phoneBookNameList.get(index1))){
								index = i;
								break;
							}
						}
						if(index != -1)
							continue;
					}
				}
			} while (cursor.moveToPrevious());

		}

		cursor.close();
		alarmList1 = sms.size();
		for(int i=0; i<alarmList1; i++)
			message = message + sms.get(i) + "\n";

		return message;
	}
}
