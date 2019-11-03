package com.blindo.app.message;

import android.app.SearchManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Contacts;
import android.provider.Telephony;
import android.speech.tts.TextToSpeech;
import android.telephony.SmsMessage;
import android.util.Log;
import android.view.KeyEvent;

import com.blindo.app.R;
import com.blindo.app.TwoItemLayout;
import com.blindo.app.TwoItemPageActivity;

import java.util.ArrayList;

/**
 * Created by 밍죠 on 2015-08-08.
 */
public class MessageTextListActivity extends TwoItemPageActivity {
    private ArrayList<String> messageTextList = new ArrayList<String>();
    private String senderName;
    private String senderNumber;
    private TwoItemLayout twoItemLayout; //TwoItemLayout 객체
    private TextToSpeech tts; //TextToSpeech 객체

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    public void onResume(){
        super.onResume();
        setPage();
    }

    public void init(){ // 액티비티의 메인 역할을 하는 메소드
        Intent intent = new Intent(this.getIntent());
        senderName = intent.getExtras().getString("name");
        senderNumber = intent.getExtras().getString("number");

        getSupportActionBar().setTitle(senderName);
        if(!senderName.equals(senderNumber))
            getSupportActionBar().setSubtitle(senderNumber);

        setContentView(R.layout.activity_two_item_page);
        twoItemLayout = (TwoItemLayout) findViewById(R.id.til);

        //제스처를 위해 아래 두개 항목은 꼭 추가해야 한다.
        twoItemLayout.setOnTouchListener(this);
        twoItemLayout.setOnLongClickListener(this);

        addToListSendMessage(); //문자메시지 불러오기 되면 setSender로 바꿔줄것!
        setSenders();
        setEleTextList(messageTextList);

        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener(){
            public void onInit(int status) {
                Log.i("onInit", "" + status);
                setPage();
            }
        });

        setPage(); //이 메소드를 통해 처음 시작시 페이지를 설정
    }

    public void setSenders(){
        Uri allMessage = Uri.parse("content://sms");
        String[] tag = {"address", "body", "type", "read"};
        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(allMessage, tag, null, null, null);

        cur.moveToFirst();

        while (!cur.isAfterLast()) {
            String address = cur.getString(cur.getColumnIndex(cur.getColumnName(0)));

            if(address.equals(senderNumber)) {
                String body = cur.getString(cur.getColumnIndex(cur.getColumnName(1)));
                String type = cur.getString(cur.getColumnIndex(cur.getColumnName(2)));
                long read = cur.getLong(cur.getColumnIndex(cur.getColumnName(3)));

                if(type.equals("1"))
                    type = "받음";
                else if (type.equals("2") || type.equals("4"))
                    type = "보냄";
                else if (type.equals("5"))
                    type = "실패문자";
                else if (type.equals("6"))
                    type = "예약문자";
                else
                    type = "";
                Log.i(senderName + "type", type);
                messageTextList.add(type + ", " + body);
            }
            cur.moveToNext();
        }
        cur.close();
    }

    public void addToListSendMessage(){
        messageTextList.add("문자보내기");
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
        if(tts.isSpeaking())
            tts.stop();
        if(getEleTextList().get(getIndex()[0]).equals("문자보내기")){
            tts.speak("문자보내기 실행합니다", TextToSpeech.QUEUE_FLUSH, null);
            Intent intent = new Intent(this, SendMessageActivity.class);
            intent.putExtra("name", this.senderName);
            intent.putExtra("phone", this.senderNumber);
            startActivity(intent);
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
