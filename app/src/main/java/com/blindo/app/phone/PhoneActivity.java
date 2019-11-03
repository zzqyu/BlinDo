package com.blindo.app.phone;

//import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.ContactsContract;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.SearchView;

import com.blindo.app.R;
import com.blindo.app.PublicMethods;
import com.blindo.app.TwoItemLayout;
import com.blindo.app.TwoItemPageActivity;
import com.blindo.app.lockscreen.LockScreenActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.regex.Pattern;

/**
 * Created by 밍죠 on 2015-08-08.
 */
public class PhoneActivity extends TwoItemPageActivity {
    private String TAG = "PhoneActivity";
    private String phoneNumberName;
    private String phoneNumber;
    public boolean isYN = false; //예 아니오 화면 출력 여부
    private String searchKeyword;

    public ArrayList<String> nameList = new ArrayList<String>();
    public ArrayList<String> phoneList = new ArrayList<String>();

    private TwoItemLayout twoItemLayout; //TwoItemLayout 객체
    public TextToSpeech tts; //TextToSpeech 객체

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); // TwoItemPageActivity의 onCreate에서 init()를 호출한다.
    }
    @Override
    public void init(){ // 액티비티의 메인 역할을 하는 메소드
        setTitle(TAG); //액션바의 타이틀 설정
        getSupportActionBar().hide();
        setContentView(R.layout.activity_search); //레이아웃을 가져온다

        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            public void onInit(int status) {
                firstMent();
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

                    if (isPhoneNumber(searchKeyword)) {
                        phoneNumber = getSearchKeyword();
                        Log.i("phoneNumber", phoneNumber);
                        twoItemLayout = (TwoItemLayout) findViewById(R.id.til); //twoItemLayout에 설정함
                        //제스처를 위해 아래 두개 항목은 꼭 추가해야 한다.
                        twoItemLayout.setOnTouchListener(PhoneActivity.this);
                        twoItemLayout.setOnLongClickListener(PhoneActivity.this);
                        whenNumTtsMent();
                        isYN = true;
                        setYNPage();
                    } else {
                        search(searchKeyword);
                        setEleTextList(nameList);
                        if (getEleTextList().size() == 0)
                            tts.speak("검색결과가 없습니다", TextToSpeech.QUEUE_FLUSH, null);
                        else {
                            setIndex(0, 1);

                            twoItemLayout = (TwoItemLayout) findViewById(R.id.til); //twoItemLayout에 설정함

                            //제스처를 위해 아래 두개 항목은 꼭 추가해야 한다.
                            twoItemLayout.setOnTouchListener(PhoneActivity.this);
                            twoItemLayout.setOnLongClickListener(PhoneActivity.this);

                            setPage();
                        }
                    }
                }
                return true;
            }
        });
    }
    public String getPhoneNumber(){
        return phoneNumber;
    }

    public void whenNumTtsMent(){
        tts.speak(PublicMethods.phoneNumToHangeul(phoneNumber) + "번에 전화거시겠습니까?", TextToSpeech.QUEUE_FLUSH, null);
    }

    public boolean isPhoneNumber(String key){
        if(Pattern.matches("^[0-9]+$", key)) {
            phoneNumber = key;
            return true;
        }else
            return false;
    }

    public void call(){
        Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibe.vibrate(35);
        tts.speak("전화연결합니다.", TextToSpeech.QUEUE_FLUSH, null);
        startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel: " + phoneNumber)));
        finish();
    }

    public void search(String key) {
        class Adress{
            String name;
            String tel;

            Adress(String name, String tel){
                this.name = name;
                this.tel = tel;
            }
        }
        ArrayList<Adress> adressList = new ArrayList<Adress>();
        nameList.clear();
        phoneList.clear();
        ContentResolver cr = getContentResolver();
        Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        int ididx = cursor.getColumnIndex(ContactsContract.Contacts._ID);
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            String id = cursor.getString(ididx); // 한 사람의 아이디
            // 이름 가져오는 부분
            String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

            if(name.contains(key) //일반검색
                    || PublicMethods.choSeparation(name).contains(key) //초성검색
                    || name.toUpperCase().contains(key.toUpperCase()) // 대문자로 바꿔 검색
                    ) {
                // 전화번호 가져오는 부분
                Cursor numCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{id}, null);
                numCur.moveToFirst();
                String tel= numCur.getString(numCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                Adress adress = new Adress(name, tel);
                adressList.add(adress);
            }
            cursor.moveToNext();
        }

        //이름 순 정렬
        Comparator<Adress> comparator = new Comparator<Adress>(){
            public int compare(final Adress o1, final Adress o2) {
                int res = String.CASE_INSENSITIVE_ORDER.compare(o1.name, o2.name);
                if (res == 0) {
                    res = o1.name.compareTo(o2.name);
                }
                return res;
            }
        } ;
        Collections.sort(adressList, comparator);

        for(Adress k: adressList){
            nameList.add(k.name);
            phoneList.add(k.tel);
        }


        final int finalAddCount = adressList.size();
        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            public void onInit(int status) {
                if(finalAddCount == 0)
                    tts.speak("검색 결과가 없습니다", TextToSpeech.QUEUE_FLUSH, null);
                else if (finalAddCount == 1)
                    tts.speak(Integer.toString(finalAddCount) + "개의 결과가 있습니다. " + nameList.get(0), TextToSpeech.QUEUE_ADD, null);
                else
                    tts.speak(Integer.toString(finalAddCount) + "개의 결과가 있습니다. " + nameList.get(0) + ", " + nameList.get(1) , TextToSpeech.QUEUE_ADD, null);

            }
        });

    }

    public void setSearchKeyword(String query){
        searchKeyword = query;
    }

    public String getSearchKeyword(){
        return searchKeyword;
    }

    public void setYNPage(){  //yn 모드 일때 화면 세팅
        String[] pageElement = {"예", "아니오"};
        if(pageElement[1].equals("Empty")){// 마지막 항목이 비어있을 때
            pageElement[1] = "";
        }
        //음성안내
        tts.speak(pageElement[0] + "  " + pageElement[1], TextToSpeech.QUEUE_ADD, null);

        //레이아웃에 두개 항목 추가
        twoItemLayout.setThisPage(pageElement);
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
        Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibe.vibrate(35);
        if(! isYN) { //yn 모드가 아닐 때
            //첫번째 항목의 이름을 안내 한다
            phoneNumber = phoneList.get(getIndex()[0]);
            phoneNumberName = nameList.get(getIndex()[0]);
            tts.speak(phoneNumberName + "에게 전화하시겠습니까?", TextToSpeech.QUEUE_FLUSH, null);
            isYN = true;
            setYNPage();
        }
        else{  //yn 모드 일 때
            isYN = false;
            call();
        }
    }

    public void whenSwipeDown() {
        Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibe.vibrate(35);
        if(! isYN) { //yn 모드가 아닐 때
            //두번째 항목의 이름을 안내 한다
            if (!getEleTextList().get(getIndex()[1]).equals("Empty!")) { // 항목이 비어있지 않을 때만 실행 된다.
                phoneNumber = phoneList.get(getIndex()[1]);
                phoneNumberName = nameList.get(getIndex()[1]);
                tts.speak(phoneNumberName + "에게 전화하시겠습니까?", TextToSpeech.QUEUE_FLUSH, null);
                isYN = true;
                setYNPage();
            }
            else{
                //두번째 항목이 비어있을 때
                tts.speak("항목이 없습니다.", TextToSpeech.QUEUE_FLUSH, null);
            }
        }
        else{  //yn 모드 일 때
            isYN = false;
            tts.speak("취소합니다", TextToSpeech.QUEUE_FLUSH, null);
            Intent intentSubActivity = new Intent(this, this.getClass());
            startActivity(intentSubActivity);
            finish();
        }
    }
    public void firstMent(){
        Log.i("firstMent",PublicMethods.missCall(PhoneActivity.this));
        String missCall = PublicMethods.missCall(PhoneActivity.this);
        String[] missCallList = missCall.split("\n");
        if(missCallList.length > 0 && missCall.replace(" ", "").length()>0){
            tts.speak("부재중 전화, " + missCallList.length + "건 있습니다.", TextToSpeech.QUEUE_FLUSH, null);
            for(int i=0; i<missCallList.length; i++){
                tts.speak(missCallList[i].split("/")[0] + ", ", TextToSpeech.QUEUE_ADD, null);
            }
            tts.speak("이름 또는 전화번호를 입력해주세요", TextToSpeech.QUEUE_ADD, null);
        }
        else{
            tts.speak("이름 또는 전화번호를 입력해주세요", TextToSpeech.QUEUE_FLUSH, null);
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
