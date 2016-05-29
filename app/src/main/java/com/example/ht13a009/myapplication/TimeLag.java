package com.example.ht13a009.myapplication;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

// 時間差を計算するclass
class TimeLag {

    String[] time;

    public TimeLag(String[] time){
        this.time = time;
    }

    public String[] TimeCalculation() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

        Calendar a = Calendar.getInstance();
        Calendar b = Calendar.getInstance();

        // 現在の時刻を取得
        Date date = new Date();
        Calendar now = Calendar.getInstance();
        now.setTime(date);

        // タイムゾーンの設定
        /*
        TimeZone tz = TimeZone.getTimeZone("Asia/Tokyo");
        now.setTimeZone(tz);
        */

        // リストにセットする時間
        a.set(Calendar.HOUR_OF_DAY, Integer.parseInt(time[0]));
        a.set(Calendar.MINUTE, Integer.parseInt(time[1]));
        a.set(Calendar.SECOND, 00);

        // 差分を求めてUTC+9:00を引く

        long lag;

        if( a.getTimeInMillis() <= now.getTimeInMillis()){
            lag = 0  - b.getTimeZone().getRawOffset();
        }else{
            lag = a.getTimeInMillis() - now.getTimeInMillis() - b.getTimeZone().getRawOffset();
        }

        b.setTimeInMillis(lag);

        // 表示
        String[] timeLag = sdf.format(b.getTime()).split(":");

        return timeLag;
    }
}
