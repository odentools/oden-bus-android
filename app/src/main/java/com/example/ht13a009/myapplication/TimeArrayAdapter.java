package com.example.ht13a009.myapplication;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Formatter;

public class TimeArrayAdapter extends ArrayAdapter<JSONObject> {

    private Context mContext;
    private LayoutInflater mInflater;
    private Handler mHandler;

    //通知時間
    private int preNoticeTime;
    private int noticeTime;

    private boolean ShinobuArrivalTime;
    private boolean ShinobuOnly;

    private String route;
    private int id;

    private String year = null;
    private String month = null;
    private String day = null;

    /**
     * コンストラクタ
     * @param context コンテキスト
     * @param resource リストの行のレイアウト
     */
    public TimeArrayAdapter(Context context, int resource, boolean shinobu_arrival_time, boolean shinobu_only, String Route, int routeid) {
        super(context, resource);

        mContext = context;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ShinobuArrivalTime = shinobu_arrival_time;
        ShinobuOnly = shinobu_only;
        route = Route;
        id = routeid;

        // 定期更新を開始
        mHandler = new Handler();
        setUpdateRunnable();

    }


    /**
     * リスト表示を1秒ごとに更新するためのメソッド
     */
    private void setUpdateRunnable() {

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {

                // 便リストから古い便を削除
                while (0 < getCount()) {

                    JSONObject data = getItem(0);
                    if (!data.isNull("dummy")) { // ダミーのオブジェクトなら何もしない
                        break;
                    }
                    
                    
                    // 通知
                    JSONObject[] dataArr = new  JSONObject[getCount()];
                    for(int i=0; i < getCount(); i++){
                        dataArr[i] = getItem(i);
                    }
                    startAlarm(dataArr);

                    
                    // 出発日時を確認
                    Date date_dep = null;
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
                        date_dep = sdf.parse(data.getString("departureDate"));
                    } catch (Exception e) {
                        e.printStackTrace();
                        break;
                    }

                    // 古い便かどうかを取得
                    long rem_time = date_dep.getTime() - System.currentTimeMillis();
                    if (rem_time < 0) {
                        // 古い便ならばリストから削除
                        remove(data);
                    } else {
                        break;
                    }
                }

                // 営業終了時
                if (getCount() == 0) {
                    // ダミーのオブジェクトを作成
                    JSONObject obj = new JSONObject();
                    try {
                        obj.put("dummy", true);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    // 配列へ追加
                    add(obj);
                }

                // 便リストの更新をリクエスト
                TimeArrayAdapter.this.notifyDataSetChanged();

                // 再度，Runnableをセット
                setUpdateRunnable();

            }

        }, 1000);
    }


    /**
     * 通知を便に設定する
     */
    public void setAlarm(int position, boolean val) {

        try {

            getItem(position).put("alarm", val);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        notifyDataSetChanged();

    }


    /**
     * 通知を便に設定する (オン・オフ切り替え)
     */
    public boolean toggleAlarm(int position) {

        boolean val = false;

        try {

            if (getItem(position).isNull("alarm")) {
                // 有効にする
                val = true;
            } else {
                // 有効・無効を切り替える
                val = !getItem(position).getBoolean("alarm");
            }

            getItem(position).put("alarm", val);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        notifyDataSetChanged();

        // 新しい値を返す
        return val;

    }

    /**
     * 通知開始
     */
    public void startAlarm(JSONObject[] datas){

        final int notificationId = 001;

        for (int i = 0; i < getCount(); i++){

            if (datas[i].isNull("departureDate")) continue;

            Date date_dep = null;
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
                date_dep = sdf.parse(datas[i].getString("departureDate"));
            } catch (Exception e) {
                e.printStackTrace();
                break;
            }

            long rem_time = date_dep.getTime() - System.currentTimeMillis();
            noticeTime = (int)rem_time / 1000;

            try {
                // デバッグ用
                //if (!datas[i].isNull("alarm") && datas[i].getBoolean("alarm") && noticeTime == ( (60 * 7 + 25) * 60)){
                    //System.out.println("!datas[" + i + "].isNull(\"alarm\"):" + !datas[i].isNull("alarm"));
                    //System.out.println("datas[" + i + "].getBoolean(\"alarm\"): " + datas[i].getBoolean("alarm"));
                if (!datas[i].isNull("alarm") && datas[i].getBoolean("alarm") && noticeTime == (preNoticeTime * 60)){

                    Intent intent = new Intent(mContext, TimeActivity.class);

                    intent.putExtra("Route", route);
                    intent.putExtra("id", id);

                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);

                    PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                    NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext);
                    builder.setContentIntent(pendingIntent);
                    builder.setSmallIcon(R.mipmap.oecu_bus_background_transmission);
                    builder.setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.oecu_bus));
                    // アイコンの背景色
                    builder.setColor(ContextCompat.getColor(mContext, R.color.colorPrimary));

                    if(preNoticeTime >= 60){
                        builder.setContentTitle(route);
                        builder.setContentText("バス発車まで残り" + preNoticeTime / 60 + "時間です．");
                    }else{
                        builder.setContentTitle(route);
                        builder.setContentText("バス発車まで残り" + preNoticeTime + "分です．");
                    }

                    // 通知時の音・バイブ・ライト
                    builder.setDefaults(Notification.DEFAULT_SOUND
                            | Notification.DEFAULT_VIBRATE
                            | Notification.DEFAULT_LIGHTS);

                    // タップするとキャンセル(消える)
                    builder.setAutoCancel(true);

                    NotificationManagerCompat manager = NotificationManagerCompat.from(mContext);
                    manager.notify(notificationId, builder.build());
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * リスト項目のViewを生成するメソッド
     * @param position
     * @param convertView
     * @param parent
     * @return 生成されたView
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        //final int notificationId = 001;

        // 項目を取得
        JSONObject data = getItem(position);

        if (convertView == null || data.isNull("dummy")) {
            // Viewを生成
            convertView = mInflater.inflate(R.layout.row, null);
        }

        if (!data.isNull("dummy")) { // ダミーオブジェクトならば

            // 営業終了表示を行う
            convertView = mInflater.inflate(R.layout.sales_end, null);
            TextView tv_sales_end = (TextView)convertView.findViewById(R.id.sales_end);

            Date date_now = new Date();
            SimpleDateFormat sdf_now = new SimpleDateFormat("yyyy'/'MM'/'dd");

            String pickerDate = year + "/" + month + "/" + day;
            System.out.println("pickerDate:" + pickerDate);
            System.out.println("sdf_now.format(date_now): " + sdf_now.format(date_now));
            System.out.println("compareTo: " + sdf_now.format(date_now).compareTo(pickerDate));

            if (sdf_now.format(date_now).compareTo(pickerDate) >= 0 || (year == null && month == null & day == null) ){
                tv_sales_end.setText("本日の営業は終了しました");
            }else {
                tv_sales_end.setText("未実装です");
            }

            return convertView;

        }

        // 日時のパース
        Date date_dep = null, date_arv = null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
            // 出発日時
            date_dep = sdf.parse(data.getString("departureDate"));
            // 到着日時
            date_arv = sdf.parse(data.getString("arrivalDate"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (date_dep == null) {
            return convertView;
        }

        long rem_time = date_dep.getTime() - System.currentTimeMillis();

        // 表示 - 出発時間
        Formatter fm = new Formatter();
        String time_dep = fm.format("%02d:%02d", date_dep.getHours(), date_dep.getMinutes()).toString();
        TextView tv_time = (TextView)convertView.findViewById(R.id.row_textview_time);
        tv_time.setText(time_dep);

        int hour = ((int)rem_time/ 1000 / 3600);
        int minute = ((int)rem_time / 1000 % 3600 / 60);
        int second = ((int)rem_time / 1000 % 3600 % 60);

        // 表示 - 残り時間
        Formatter fm_rem = new Formatter();
        String rem_time_formatter = fm_rem.format("%2d:%02d:%02d", hour, minute, second).toString();
        TextView tv_rem = (TextView)convertView.findViewById(R.id.row_textview_rem_time);
        tv_rem.setText("@" + rem_time_formatter);

        // 表示 - 忍経由
        TextView tv_shinobu = (TextView)convertView.findViewById(R.id.row_textview_shinobu);

        // 表示 - 忍到着時間
        TextView tv_shinobu_arrival = (TextView)convertView.findViewById(R.id.row_textview_shinobu_arrival);
        Formatter fm_shinobu_arrival = new Formatter();
        String shinobu_arrival = fm_shinobu_arrival.format("%02d:%02d", date_dep.getHours(), date_dep.getMinutes() + 10).toString();

        try {
            if(data.getString("note").equals("忍ケ丘経由")){
                tv_shinobu.setText("忍");
                if(ShinobuArrivalTime){
                    tv_shinobu_arrival.setText("忍着:" + shinobu_arrival);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // 表示 - 通知
        ImageView iv_alarm = (ImageView)convertView.findViewById(R.id.row_imageview);
        try {
            if (!data.isNull("alarm") && data.getBoolean("alarm")) {
                iv_alarm.setVisibility(View.VISIBLE);
            } else {
                iv_alarm.setVisibility(View.INVISIBLE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return convertView;

    }


    /**
     * 項目の追加
     * @param data オブジェクト
     */
    @Override
    public void add(JSONObject data) {

        try {
            if (data.getString("note").equals("null") && ShinobuOnly) {
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        super.add(data);

    }

    // 通知時間
    public void setPreNoticeTime(int setMinute) {
        preNoticeTime = setMinute;
    }

    // DatePickerから取ってきた日付，営業終了か未実装か判断する為のもの
    public void setDate(String stringYear, String stringMonth, String stringDay){
        year = stringYear;
        month = stringMonth;
        day = stringDay;

        if(Integer.parseInt(month) < 10){
            month = "0" + month;
        }
        if(Integer.parseInt(day) < 10){
            day = "0" + day;
        }

    }
}
