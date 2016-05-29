package com.example.ht13a009.myapplication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements Runnable {

    private MyHttpConnection task;
    private HashMap<String, Integer> map;

    ProgressDialog progressDialog;
    Thread thread;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Loading now");
        progressDialog.setMessage("Please wait 5 seconds");
        //progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

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
                String routeName = (String) listView.getItemAtPosition(position);
                Intent intent = new Intent(MainActivity.this, TimeActivity.class);

                //intent.putExtra("key", "value"); でTimeActivityに文字を送る
                intent.putExtra("Route", routeName);
                intent.putExtra("id", map.get(routeName));
                startActivity(intent);

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

        progressDialog.show();

        thread = new Thread(this);
        thread.start();

    }

    // progressDialog
    @Override
    public void run() {
        try {
            thread.sleep(5000);
        } catch (InterruptedException e) {
            // TODO 自動生成された catch ブロック
            e.printStackTrace();
        }
        progressDialog.dismiss();
        handler.sendEmptyMessage(0);
    }

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            Toast.makeText(getApplicationContext(), "slept 5 seconds",
                    Toast.LENGTH_LONG).show();
        }
    };
}
