package com.example.ht13a009.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class MainActivity extends Activity {

    private TextView textView;
    private MyHttpConnection task;
    private HashMap<String,Integer> map;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // activity_main.xmlに設定したコンポーネントをid指定で取得します。
        // アダプタを生成してリストビューへセット
        ArrayAdapter<String> adapter = new ArrayAdapter<String>
                (this, android.R.layout.simple_list_item_1, new ArrayList());
        ListView listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(adapter);
        map = new HashMap<String, Integer>();

        // 路線リストを取得してリストビューへ表示
        task = new MyHttpConnection(adapter, map);

        try {
            URL url = new URL("https://bus.oden.oecu.jp/api/1.3.1/Routes.json");
            // 非同期処理
            task.execute(url);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        //リスト項目がクリックされた時の処理
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Set entries = map.entrySet();
                ListView listView = (ListView) parent;
                String item = (String) listView.getItemAtPosition(position);
                Intent intent = new Intent(MainActivity.this, TimeActivity.class);
                String key = null;
                int valueId = -1;

                for(Iterator iterator = entries.iterator(); iterator.hasNext();){
                    Map.Entry entry = (Map.Entry)iterator.next();
                    if( item.equals(entry.getKey()) ){

                        key = (String)entry.getKey();
                        valueId = (Integer) entry.getValue();

                        //intent.putExtra("key", "value"); でTimeActivityに文字を送る
                        //intent.putExtra("Route", (String) entry.getKey());
                        //intent.putExtra( "id", (Integer) entry.getValue());
                        //startActivity(intent);

                        Toast.makeText(getApplicationContext(), " id" + id,
                                Toast.LENGTH_LONG).show();

                    }else{
                        //Toast.makeText(getApplicationContext(), " entry.getKey()" + entry.getKey(),
                          //      Toast.LENGTH_LONG).show();
                    }

                    //intent.putExtra("key", "value"); でTimeActivityに文字を送る
                    intent.putExtra("Route", key);
                    intent.putExtra( "id", (Integer) valueId);
                    startActivity(intent);

                }

                /*
                ListView listView = (ListView) parent;
                String item = (String) listView.getItemAtPosition(position);
                Toast.makeText(getApplicationContext(), item + " id" + id,
                        Toast.LENGTH_LONG).show();
                */
            }
        });

        listView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            //リスト項目が選択された時の処理
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ListView listView = (ListView) parent;
                String item = (String) listView.getItemAtPosition(position);
                Toast.makeText(getApplicationContext(), item + " selected",
                        Toast.LENGTH_LONG).show();
            }
            //リスト項目がなにも選択されていない時の処理
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Toast.makeText(getApplicationContext(), "no item selected",
                        Toast.LENGTH_LONG).show();
            }
        });

        //リスト項目が長押しされた時の処理
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                ListView listView = (ListView) parent;
                String item = (String) listView.getItemAtPosition(position);
                Toast.makeText(getApplicationContext(), item + " long clicked",
                        Toast.LENGTH_LONG).show();
                return false;
            }
        });

    }
}
