package com.blindo.app.message;

import android.content.Intent;
import android.speech.tts.TextToSpeech;

import com.blindo.app.PublicMethods;
import com.blindo.app.phone.PhoneActivity;

/**
 * Created by 밍죠 on 2015-08-08.
 */
public class MessageRecipientSearch extends PhoneActivity {
    private String recipientName;
    private String recipientPhoneNumber;

    @Override
    public void whenNumTtsMent(){
        recipientName = getPhoneNumber();
        recipientPhoneNumber = recipientName;
        tts.speak("보내는 곳 " + PublicMethods.phoneNumToHangeul(recipientPhoneNumber) + ", 맞습니까?"
                , TextToSpeech.QUEUE_FLUSH, null);
    }

    @Override
    public void whenSwipeUp() {
        if(!isYN) { //yn 모드가 아닐 때
            //첫번째 항목의 이름을 안내 한다
            recipientPhoneNumber = phoneList.get(getIndex()[0]);
            recipientName = nameList.get(getIndex()[0]);
            tts.speak("보내는 곳 " + recipientName + ". 맞습니까?", TextToSpeech.QUEUE_FLUSH, null);
            isYN = true;
            setYNPage();
        }
        else{  //yn 모드 일 때
            isYN = false;
            Intent intent = new Intent(MessageRecipientSearch.this, SendMessageActivity.class);
            intent.putExtra("name", this.recipientName);
            intent.putExtra("phone", this.recipientPhoneNumber);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void whenSwipeDown() {
        if(!isYN) { //yn 모드가 아닐 때
            //두번째 항목의 이름을 안내 한다
            if (getEleTextList().get(getIndex()[1]).equals("Empty!")) { // 항목이 비어있지 않을 때만 실행 된다.
                //두번째 항목이 비어있을 때
                tts.speak("항목이 없습니다.", TextToSpeech.QUEUE_FLUSH, null);

            }
            else{
                recipientPhoneNumber = phoneList.get(getIndex()[1]);
                recipientName = nameList.get(getIndex()[1]);
                tts.speak("보내는 곳 " + recipientName + ", 맞습니까?", TextToSpeech.QUEUE_FLUSH, null);
                isYN = true;
                setYNPage();
            }
        }
        else{  //yn 모드 일 때
            isYN = false;
            Intent intent = new Intent(this, MessageRecipientSearch.class);
            startActivity(intent);
            tts.stop();
            finish();
        }
    }
    @Override
    public void firstMent(){
        tts.speak("이름 또는 전화번호를 입력해주세요", TextToSpeech.QUEUE_FLUSH, null);
    }
}
