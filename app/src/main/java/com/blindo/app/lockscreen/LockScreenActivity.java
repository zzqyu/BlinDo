package com.blindo.app.lockscreen;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.blindo.app.GestureActivity;
import com.blindo.app.PublicMethods;
import com.blindo.app.R;
import com.blindo.app.message.MessageSenderListActivity;
import com.blindo.app.message.SendMessageActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;

public class LockScreenActivity extends GestureActivity{
    private TextToSpeech tts;
    private boolean isScreenOn = false;

    private String[] time = new String[6];
    private String weather = "날씨정보없음";
    private int[] AlarmList = {0, 0, 0};
    private String message = "";
    private String call = "";

    private boolean isMissCallCallingMode = false;
    private boolean isMissMessageSandingMode = false;

    private String[] appNameList = new String[3];
    private String[] appPackageNameList = new String[3];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("onCreate", "onCreate");
        getSupportActionBar().hide();
        setContentView(R.layout.activity_lock_screen);
        setShrotcut();
        LinearLayout abc = (LinearLayout)findViewById(R.id.ll);
        abc.setOnTouchListener(this);
        abc.setOnLongClickListener(this);
        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener(){ // tts설정
            public void onInit(int status) { // tts 설정될 때 자동으로 실행되는 메소드
                Log.i("onInit", "" + status);
                Thread time_thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        mHandler.sendMessage(Message.obtain(mHandler, 1));
                    }
                }); //자식쓰레드 생성
                time_thread.start();
                PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
                isScreenOn = pm.isScreenOn();
                setTimeWidget();
                if (isScreenOn){
                    speakTime();
                }
                if (PublicMethods.network_check(LockScreenActivity.this) == true) {
                    if (isScreenOn && isWeatherBGM()) setWeather();
                }
            }
        });
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Intent intent = new Intent(this, ScreenService.class);
        startService(intent);

    }

    public Handler mHandler = new Handler(){ // 핸들러 처리부분
        public void handleMessage(Message msg){ // 메시지를 받는부분
            switch(msg.what){ // 메시지 처리
                case 1:
                    setMissNotiWidget();
                    if (isScreenOn)
                        speakNoti();
                    break;
            }
        }
    };
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i("onDestroy", "onDestroy");
        if(tts!=null && tts.isSpeaking())
            tts.stop();
    }

    public boolean isWeatherBGM(){
        boolean result =false;
        File files = new File("sdcard/android/data/com.blindo.app/Preference"); // 앱 이름|패키지 이름 파일로 있는 디렉토리

        if (!files.exists()) { // 원하는 경로에 폴더가 있는지 확인
            Log.i("!files.exists()", "!files.exists()");
            files.mkdirs();
            isWeatherBGM();
        }
        String[] fileList = files.list();
        if(fileList.length == 0){
            Log.i("fileList.length == 0", "fileList.length == 0");
            File addfile=new File("sdcard/android/data/com.blindo.app/Preference/on");
            try{
                FileOutputStream fos1 = new FileOutputStream(addfile);
                fos1.close();
            }catch (IOException e){}
            return true;
        }
        if(fileList[0].equals("on")){
            result = true;
        }
        return result;
    }


    public void setShrotcut(){
        Log.i("setShrotcut", "setShrotcut");
        File files = new File("sdcard/android/data/com.blindo.app/lockShortcut"); // 앱 이름|패키지 이름 파일로 있는 디렉토리

        if (!files.exists()) { // 원하는 경로에 폴더가 있는지 확인
            files.mkdirs();
        }
        if(files.list().length != 3){
            File defaultfile1 = new File("sdcard/android/data/com.blindo.app/lockShortcut/0|전화|com.android.dialer");
            File defaultfile2 = new File("sdcard/android/data/com.blindo.app/lockShortcut/1|문자|com.android.mms");
            File defaultfile3 = new File("sdcard/android/data/com.blindo.app/lockShortcut/2|카카오톡|com.kakao.talk");

            if(!defaultfile1.exists()){
                try {
                    FileOutputStream fos = new FileOutputStream(defaultfile1);
                    fos.close();
                } catch (IOException e) {}
            }
            if(!defaultfile2.exists()){
                try {
                    FileOutputStream fos = new FileOutputStream(defaultfile2);
                    fos.close();
                } catch (IOException e) {}
            }
            if(!defaultfile3.exists()){
                try {
                    FileOutputStream fos = new FileOutputStream(defaultfile3);
                    fos.close();
                } catch (IOException e) {}
            }
            setShrotcut();
        }
        String[] fileName = files.list();
        for (int i = 0; i < 3; i++) {
            int index = Integer.parseInt(fileName[i].split("\\|")[0]);
            appNameList[index] = fileName[i].split("\\|")[1];
            appPackageNameList[index] = fileName[i].split("\\|")[2];
        }
    }

    public void setTime(){

        int calYear, calMonth, calDay, calHour, calMinute, calNoon;

        Calendar c = Calendar.getInstance();
        String noon = "";

        calYear = c.get(Calendar.YEAR);
        calMonth = c.get(Calendar.MONTH)+1;
        calDay = c.get(Calendar.DAY_OF_MONTH);
        calHour = c.get(Calendar.HOUR_OF_DAY);
        calNoon = c.get(Calendar.AM_PM);
        calMinute = c.get(Calendar.MINUTE);

        if(calNoon == 0)
            noon = "오전";
        else
        {
            noon = "오후";
            calHour -= 12;
        }

        time[0] = Integer.toString(calYear);
        time[1] = Integer.toString(calMonth);
        time[2] = Integer.toString(calDay);
        time[3] = noon;
        time[4] = Integer.toString(calHour);
        time[5] = Integer.toString(calMinute);


    }
    public void setWeather(){
        String ext = Environment.getExternalStorageState();
        String mSdPath;
        if (ext.equals(Environment.MEDIA_MOUNTED)) {
            mSdPath = Environment.getExternalStorageDirectory()
                    .getAbsolutePath();
        } else {
            mSdPath = Environment.MEDIA_UNMOUNTED;
        }

        String path = mSdPath + "/android/data/com.blindo.app/weather/weatherURL.txt";

        String str, str1="";

        try {
            FileInputStream fis = new FileInputStream(path); // loadPath는 txt파일의 경로
            BufferedReader bufferReader = new BufferedReader(new InputStreamReader(fis));


            while( (str = bufferReader.readLine()) != null )	// str에 txt파일의 한 라인을 읽어온다
                str1 += str;


        }catch (IOException ex){
            str1 = "http://www.kma.go.kr/wid/queryDFS.jsp?gridx=61&gridy=128";
        }
        Log.i("url", str1);

        String url = PublicMethods.DownloadHtml(str1);
        String[] array = url.split("\n");
        ArrayList<String> abcd = new ArrayList<String>();

        int i = 0;
        int start = 0;
        for(String k: array)
        {
            if(k.contains("<hour>"))
                start = i;
            i++;
            abcd.add(k);
        }

        weather = abcd.get(start+7);
        weather = weather.replace("<wfKor>", "").replace("</wfKor>", "").replace(" ", "");

        TextView text1 = (TextView) this.findViewById(R.id.WeatherView);
        text1.setText(weather);

        MediaPlayer sun;
        MediaPlayer rain;
        MediaPlayer snow;

        //***********여기 9줄?만 추가했어염***********
        sun = MediaPlayer.create(this, R.raw.sun);
        rain = MediaPlayer.create(this, R.raw.rain);
        snow = MediaPlayer.create(this, R.raw.snow);

        if (weather.equals("비"))
            rain.start();
        else if (weather.equals("눈"))
            snow.start();
        else
            sun.start();
    }

    public void setTimeWidget() {
        setTime();
        TextView dayView = (TextView)findViewById(R.id.DayView);
        TextView noonView = (TextView)findViewById(R.id.NoonView);
        TextView timeView = (TextView)findViewById(R.id.TimeView);
        dayView.setText(String.format("%4d년 %2d월 %2d일",
                Integer.parseInt(time[0]), Integer.parseInt(time[1]),  Integer.parseInt(time[2])));
        noonView.setText(time[3]);
        timeView.setText(String.format("%02d : %02d", Integer.parseInt(time[4]), Integer.parseInt(time[5])));

    }
    public void setMissNotiWidget() {
        TextView dayView = (TextView)findViewById(R.id.DayView);
        TextView noonView = (TextView)findViewById(R.id.NoonView);
        TextView timeView = (TextView)findViewById(R.id.TimeView);
        dayView.setText(String.format("%4d년 %2d월 %2d일",
                Integer.parseInt(time[0]), Integer.parseInt(time[1]), Integer.parseInt(time[2])));
        noonView.setText(time[3]);
        timeView.setText(String.format("%02d : %02d", Integer.parseInt(time[4]), Integer.parseInt(time[5])));

        call = PublicMethods.missCall(this);
        message = PublicMethods.missMessage(this);
        TextView getcall = (TextView)findViewById(R.id.missCall);
        Log.i("call1", call);
        getcall.setText(call);
        TextView getmessage = (TextView)findViewById(R.id.missMessage);
        getmessage.setText(message);
    }

    public void speakTime(){
        String speak = time[1] + "월" + time[2] + "일  " + time[3] + ","
                + time[4] + "시" + time[5] + "분 입니다. ";
        tts.speak(speak, TextToSpeech.QUEUE_FLUSH, null);
    }
    public void speakNoti(){
        String missedCall = "";
        String missedMessage = "";
        AlarmList[0] = PublicMethods.getAlarmList0();
        AlarmList[1] = PublicMethods.getAlarmList1();
        if (AlarmList[0] != 0 | AlarmList[1] != 0 | AlarmList[2] !=0)
        {
            if (AlarmList[0] != 0) {
                missedCall = missedCall + "부재중전화, " + AlarmList[0] + "건, ";
                missedCall = missedCall + call.replace("/", "").replace(":", "시").replace("\n", "분\n");
            }

            if (AlarmList[1] != 0)
            {
                missedMessage = "미확인문자, " + AlarmList[1] + "건, ";
                missedMessage = missedMessage + message;
            }
        }

        tts.speak(missedCall + missedMessage, TextToSpeech.QUEUE_ADD, null);
    }
    @Override
    public void whenSwipeLeft() {
        if(isMissCallCallingMode || isMissMessageSandingMode){

        }
        else {
            shortcutRun(0);
        }
    }
    public void whenSwipeRight() {
        if(isMissCallCallingMode || isMissMessageSandingMode){

        }
        else {
            shortcutRun(2);
        }
    }
    public void whenSwipeUp() {
        if(tts.isSpeaking())
            tts.stop();

        if(isMissCallCallingMode){
            Intent intent2 =  new Intent(this, LockScreenActivity.class);
            startActivity(intent2);
            Intent intent1 = new Intent(Intent.ACTION_MAIN);
            intent1.addCategory(Intent.CATEGORY_HOME);
            startActivity(intent1);
            startActivity(new Intent(Intent.ACTION_CALL,
                    Uri.parse("tel: " + PublicMethods.getMisscallNumber().get(0))));
            isMissMessageSandingMode = false;
            finish();
        }
        else if(isMissMessageSandingMode){
            Intent intent2 =  new Intent(this, LockScreenActivity.class);
            startActivity(intent2);
            Intent intent1 = new Intent(Intent.ACTION_MAIN);
            intent1.addCategory(Intent.CATEGORY_HOME);
            startActivity(intent1);
            Intent intent = new Intent(this, SendMessageActivity.class);
            intent.putExtra("name", PublicMethods.getMissMessageName0());
            intent.putExtra("phone", PublicMethods.getMissMessageNumber0());
            startActivity(intent);
            finish();
        }
        else {
            shortcutRun(1);
        }
    }
    public void whenSwipeDown() {
        if(isMissCallCallingMode){
            isMissCallCallingMode = false;
            AlarmList[0] = 0;
            shortcutDial();
        }
        else if(isMissMessageSandingMode){
            isMissMessageSandingMode = false;
            AlarmList[1] = 0;
            shortcutMessage();
        }
        else {
            if(tts.isSpeaking())
                tts.stop();
            tts.speak("잠금 해제합니다.", TextToSpeech.QUEUE_FLUSH, null);
            Intent intent2 =  new Intent(this, LockScreenActivity.class);
            startActivity(intent2);
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            startActivity(intent);
            try{
                Thread.sleep(1000);
                finish();
                if(tts.isSpeaking())
                    tts.stop();
            }catch(Exception e){}
        }
    }

    public void shortcutDial(){
        if(tts.isSpeaking())
            tts.stop();
        //AlarmList[0] = PublicMethods.getAlarmList0();
        if(AlarmList[0] > 0){
            tts.speak(PublicMethods.getMisscallName().get(0) + ", 전화하겠습니까, 예, 아니오", TextToSpeech.QUEUE_FLUSH, null);
            isMissCallCallingMode = true;
        }
        else{
            AlarmList[0] = PublicMethods.getAlarmList0();
            Intent intent2 =  new Intent(this, LockScreenActivity.class);
            startActivity(intent2);
            Intent intent1 = new Intent(Intent.ACTION_MAIN);
            intent1.addCategory(Intent.CATEGORY_HOME);
            startActivity(intent1);
            tts.speak("전화 실행합니다.", TextToSpeech.QUEUE_FLUSH, null);
            Intent intent = new Intent(Intent.ACTION_DIAL);
            startActivity(intent);
            try{
                Thread.sleep(1000);
                finish();
            }catch(Exception e){}
        }
    }

    public void shortcutMessage(){
        if(tts.isSpeaking())
            tts.stop();
        //AlarmList[1] = PublicMethods.getAlarmList1();
        if(AlarmList[1] > 0){
            tts.speak(PublicMethods.getMissMessageName0() + "에 문자보내겠습니까?, 예, 아니오", TextToSpeech.QUEUE_FLUSH, null);
            isMissMessageSandingMode = true;
        }
        else{
            AlarmList[1] = PublicMethods.getAlarmList1();
            tts.speak("문자 실행합니다.", TextToSpeech.QUEUE_FLUSH, null);
            Intent intent2 =  new Intent(this, LockScreenActivity.class);
            startActivity(intent2);
            Intent intent1 = new Intent(Intent.ACTION_MAIN);
            intent1.addCategory(Intent.CATEGORY_HOME);
            startActivity(intent1);
            Intent intentSubActivity = new Intent(this, MessageSenderListActivity.class);
            startActivity(intentSubActivity);
            try{
                Thread.sleep(1000);
                finish();
            }catch(Exception e){}
        }
    }
    public void shortcutElse(int index){
        if(tts.isSpeaking())
            tts.stop();
        tts.speak(appNameList[index] + "실행합니다.", TextToSpeech.QUEUE_FLUSH, null);
        Intent intent2 =  new Intent(this, LockScreenActivity.class);
        startActivity(intent2);
        Intent intent1 = new Intent(Intent.ACTION_MAIN);
        intent1.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent1);
        Intent intent = getPackageManager().getLaunchIntentForPackage(appPackageNameList[index]);
        startActivity(intent);
        try{
            Thread.sleep(1000);
            finish();
        }catch(Exception e){}
    }
    public void shortcutRun(int index){
        if(appPackageNameList[index].equals("com.android.dialer")
                || appPackageNameList[index].equals("com.blindo.app.PhoneActivity")
                || appNameList[index].equals("전화")
                || appNameList[index].equals("Bilnd 전화")){
            shortcutDial();
        }
        else if (appPackageNameList[index].equals("com.android.mms")
                || appPackageNameList[index].equals("com.blindo.app.MessageSenderListActivity")
                || appNameList[index].equals("메시지")
                || appNameList[index].equals("문자")
                || appNameList[index].equals("Bilnd 메시지")){
            shortcutMessage();
        }
        else{
            shortcutElse(index);
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                return true;
            default:
                return false;
        }

    }


}