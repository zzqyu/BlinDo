package com.blindo.app.message;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.blindo.app.GestureActivity;

/**
 * Created by 밍죠 on 2015-08-08.
 */
public class SendMessageActivity extends GestureActivity {
    private String message="";
    private String recipientName="";
    private String recipientPhoneNumber="";

    private int activityType = 0;

    private static int TO_SENDER_LIST = 0;
    private static int TO_TEXT_LIST = 1;

    private boolean messageConfirm = false;

    private TextToSpeech tts; //TextToSpeech 객체
    private EditText editText;

    private InputMethodManager mgr;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    public void init(){ // 액티비티의 메인 역할을 하는 메소드
        getSupportActionBar().setTitle("문자보내기");

        setRecipient();
        if(activityType == TO_SENDER_LIST){
            Intent intent = new Intent(SendMessageActivity.this, MessageRecipientSearch.class);
            startActivity(intent);
            finish();
        }
        View view = new View(this);
        setContentView(view);

        editText = new EditText(this);
        editText.setBackgroundColor(0xffffff);
        editText.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        editText.setGravity(Gravity.TOP);
        editText.setTextSize(20);
        editText.setInputType(0);
        editText.setOnTouchListener(this);
        editText.setOnLongClickListener(this);
        addContentView(editText, editText.getLayoutParams());

        mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener(){ // tts설정
            public void onInit(int status) { // tts 설정될 때 자동으로 실행되는 메소드
            }
        });
        whenSwipeDown();

    }

    public void setRecipient(){
        Intent intent = getIntent();
        this.recipientPhoneNumber = intent.getStringExtra("phone");
        this.recipientName = intent.getStringExtra("name");
        if(this.recipientName != null) {
            getSupportActionBar().setSubtitle(this.recipientName);
            activityType = 1;
        }
    }


    public void whenSwipeUp(){
        if(messageConfirm){
            try {
                sendSmsMessage(recipientPhoneNumber, message);
                tts.speak("메시지 전송되었습니다.", TextToSpeech.QUEUE_FLUSH, null);
                finish();
            } catch (Exception e1) {
                tts.speak("메시지 전송실패했습니다. 다시 시도합니다.", TextToSpeech.QUEUE_FLUSH, null);
                try {
                    sendSmsMessage(recipientPhoneNumber, message);
                    tts.speak("메시지 전송되었습니다.", TextToSpeech.QUEUE_FLUSH, null);
                    finish();
                } catch (Exception e2) {
                    tts.speak("메시지 전송실패했습니다. 다시 시도합니다.", TextToSpeech.QUEUE_FLUSH, null);
                }
            }
        }

    }

    public void whenSwipeDown(){
        tts.speak("내용을 입력하세요", TextToSpeech.QUEUE_FLUSH, null);
        editText.requestFocus();
        mgr.showSoftInput(editText, InputMethodManager.SHOW_FORCED);
    }

    public void whenDoubleTap(){
        mgr.hideSoftInputFromInputMethod(editText.getWindowToken(), 0);

        this.message = editText.getText() + "";
        Log.i("messagelength", message.length() + "");
        int messageSize = message.length();
        if(messageSize>0){
            messageConfirm = true;
            tts.speak("입력한 메시지, " + message + " , 입니다.", TextToSpeech.QUEUE_FLUSH, null);
            tts.speak(", 위로 swipe하면 전송,   아래로 swipe하면 메시지 수정합니다. ", TextToSpeech.QUEUE_ADD, null);
        }

        else{
            tts.speak("입력한 내용이 없습니다.", TextToSpeech.QUEUE_FLUSH, null);
            try {
                Thread.sleep(1000);
                if(tts.isSpeaking())
                    Thread.sleep(1000);
                whenSwipeDown();
            }
            catch (Exception e){}
        }


    }

    protected void sendSmsMessage(String address, String message) throws Exception {
        if (message.length()>0 && address.length()>0){
            SmsManager sms = SmsManager.getDefault();
            sms.sendTextMessage(address, null, message, null, null);
        }
    }
}
