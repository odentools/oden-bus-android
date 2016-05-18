package com.example.ht13a009.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class TimeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time);

        // 現在のintentを取得する
        Intent intent = getIntent();

        // intentから指定キーの文字列を取得する
        String Route = intent.getStringExtra("Route");
        int id = intent.getIntExtra("id", -1);

        // 現在の時刻を取得
        Date date = new Date();
        // 表示形式を設定
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy'/'MM'/'dd");

        // activity_main.xmlに設定したコンポーネントをid指定で取得します。
        // アダプタを生成してリストビューへセット
        ArrayAdapter<String> adapter = new ArrayAdapter<String>
                (this, android.R.layout.simple_list_item_1, new ArrayList());
        ListView listView = (ListView) findViewById(R.id.listView2);
        listView.setAdapter(adapter);

        getTime task = new getTime(adapter);

        try {
            //sdf.format(date)
            URL url = new URL("https://bus.oden.oecu.jp/api/1.3.1/Dias.json?route_id=1&date=2016/05/19" );
            task.execute(url);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

    }


}
