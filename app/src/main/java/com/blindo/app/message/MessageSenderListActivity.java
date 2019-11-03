package com.blindo.app.message;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.KeyEvent;

import com.blindo.app.PublicMethods;
import com.blindo.app.R;
import com.blindo.app.TwoItemLayout;
import com.blindo.app.TwoItemPageActivity;

import java.util.ArrayList;

/**
 * Created by 밍죠 on 2015-08-08.
 */
public class MessageSenderListActivity extends TwoItemPageActivity {
    private  ArrayList<String> senderList = new ArrayList<String>();
    private ArrayList<String> phoneBookNumList = new ArrayList<String>();
    private ArrayList<String> phoneBookNameList = new ArrayList<String>();
    private TwoItemLayout twoItemLayout; //TwoItemLayout 객체
    private TextToSpeech tts; //TextToSpeech 객체


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void onResume(){
        super.onResume();
        if(getEleTextList().size()>2)
            setPage();
    }

    public void init(){ // 액티비티의 메인 역할을 하는 메소드
        getSupportActionBar().setTitle("메시지");
        setContentView(R.layout.activity_two_item_page);
        twoItemLayout = (TwoItemLayout) findViewById(R.id.til);

        //제스처를 위해 아래 두개 항목은 꼭 추가해야 한다.
        twoItemLayout.setOnTouchListener(this);
        twoItemLayout.setOnLongClickListener(this);
        addToListSendMessage(); //문자메시지 불러오기 되면 setSender로 바꿔줄것!
        setSenders();
        setEleTextList(senderList);
        if(getEleTextList().size()>0){
            PublicMethods.setAddrNumName(MessageSenderListActivity.this);
            phoneBookNumList = PublicMethods.getPhoneBookNumList();
            phoneBookNameList = PublicMethods.getPhoneBookNameList();
        }

        tts = new TextToSpeech(MessageSenderListActivity.this, new TextToSpeech.OnInitListener(){
            public void onInit(int status) {
                Log.i("onInit", "" + status);
                setPage();
            }
        });

        setPage(); //이 메소드를 통해 처음 시작시 페이지를 설정
    }

    public void setSenders() {
        Uri allMessage = Uri.parse("content://sms");
        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(allMessage, new String[]{"address"}, null, null, null);

        cur.moveToFirst();
        while (!cur.isAfterLast()) {
            String address = cur.getString(cur.getColumnIndex(cur.getColumnName(0)));
            if(senderList.indexOf(address) == -1){
                senderList.add(address);
            }
            cur.moveToNext();
        }
        cur.close();

    }


    public void addToListSendMessage(){
        String missMessage = PublicMethods.missMessage(this);
        String[] missMessageList = missMessage.split("\n");
        int cnt = 0;
        String ment = "";
        if(missMessageList.length > 0){

            for(String k: missMessageList) {
                if(k.contains("|")) {
                    ment += (k.split("\\|")[0] + ", ");
                    cnt++;
                }
            }
        }
        if(cnt > 0)
            senderList.add("미확인문자" + cnt + "건, " + ment);
        senderList.add("문자보내기");
    }


    public void setPage(){ //페이지를 세팅하는 메소드

        //페이지에 출력할 항목 추가
        String[] pageElement = {getEleTextList().get(getIndex()[0]), getEleTextList().get(getIndex()[1])};
        if(pageElement[1].equals("Empty!")){// 마지막 항목이 비어있을 때
            pageElement[1] = "";
        }
        for(int i = 0; i<2; i++) {
            int index = phoneBookNumList.indexOf(pageElement[i]);
            if (index >= 0)
                pageElement[i] = phoneBookNameList.get(index);
        }
        if(tts.isSpeaking())
            tts.stop();
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
            startActivity(intent);
        }
        else if(getEleTextList().get(getIndex()[0]).contains("미확인문자")){

        }
        else{
            String name = getEleTextList().get(getIndex()[0]);

            int index = phoneBookNumList.indexOf(name);
            if (index >= 0)
                name = phoneBookNameList.get(index);

            tts.speak(name + ", 대화방 실행합니다", TextToSpeech.QUEUE_FLUSH, null);
            Intent intent = new Intent(this, MessageTextListActivity.class);
            intent.putExtra("number", getEleTextList().get(getIndex()[0]));
            intent.putExtra("name", name);
            startActivity(intent);
        }
    }

    public void whenSwipeDown() {
        if(tts.isSpeaking())
            tts.stop();
        if (!getEleTextList().get(getIndex()[1]).equals("Empty!")) { // 항목이 비어있지 않을 때만 실행 된다.
            if(getEleTextList().get(getIndex()[1]).equals("문자보내기")){
                tts.speak("문자보내기 실행합니다", TextToSpeech.QUEUE_FLUSH, null);
                Intent intent = new Intent(this, SendMessageActivity.class);
                startActivity(intent);
            }
            else{
                String name = getEleTextList().get(getIndex()[1]);

                int index = phoneBookNumList.indexOf(name);
                if (index >= 0)
                    name = phoneBookNameList.get(index);

                tts.speak(name + ", 대화방 실행합니다", TextToSpeech.QUEUE_FLUSH, null);
                Intent intent = new Intent(this, MessageTextListActivity.class);
                intent.putExtra("number", getEleTextList().get(getIndex()[1]));
                intent.putExtra("name", name);
                startActivity(intent);
            }

        }
        else{
            //두번째 항목이 비어있을 때
            tts.speak("항목이 없습니다.", TextToSpeech.QUEUE_FLUSH, null);
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
