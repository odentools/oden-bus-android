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
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class TimeActivity extends AppCompatActivity implements Runnable{

    private boolean allFlag = false;
    private HashMap<String, Integer> map;

    ProgressDialog progressDialog;
    Thread thread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Loading now");
        progressDialog.setMessage("Please wait 5 seconds");
        //progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        // 現在のintentを取得する
        Intent intent = getIntent();

        // intentから指定キーの文字列を取得する
        String Route = intent.getStringExtra("Route");
        int id = intent.getIntExtra("id", -1);

        // Windowのタイトルを変更する
        setTitle(Route);

        // 現在の時刻を取得
        Date date = new Date();
        // 表示形式を設定
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy'/'MM'/'dd");

        map = new HashMap<String, Integer>();

        // activity_time.xmlに設定したコンポーネントをid指定で取得します。
        Button allButton = (Button) findViewById(R.id.allButton);
        final Spinner noticeSpinner = (Spinner) findViewById(R.id.noticeSpinner);

        // アダプタを生成してリストビューへセット
        ArrayAdapter<String> adapter = new ArrayAdapter<String>
                (this, android.R.layout.simple_list_item_1, new ArrayList());
        ListView listView = (ListView) findViewById(R.id.listView2);
        listView.setAdapter(adapter);

        // 通知時間を変更するアダプター
        final ArrayAdapter<CharSequence> noticeAdapter = ArrayAdapter.createFromResource(this,
                R.array.noticeTimeList, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        noticeSpinner.setAdapter(noticeAdapter);
        final int num = noticeAdapter.getCount();
        final String noticeTimeDefault = getString(R.string.noticeTimeDefault);
        for (int i = 0; i < num; i ++) {
            if (noticeAdapter.getItem(i).equals(noticeTimeDefault) == true) {
                noticeSpinner.setSelection(i); // 選択初期設定
                break;
            }
        }


        final GetTime task = new GetTime(adapter, getApplicationContext(), Route);

        try {
            URL url = new URL("https://bus.oden.oecu.jp/api/1.3.1/Dias.json?route_id=" + id + "&date=" + "2016/05/28");
            //sdf.format(date) );
            task.execute(url);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }


        // allボタン押した時全部の通知
        assert allButton != null;
        allButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (allFlag) {
                    allFlag = false;
                    task.setAllButton(allFlag);
                    Toast.makeText(getApplicationContext(), "全ての通知をoffにしました",
                            Toast.LENGTH_LONG).show();
                } else {
                    allFlag = true;
                    task.setAllButton(allFlag);
                    Toast.makeText(getApplicationContext(), "全ての通知をonにしました",
                            Toast.LENGTH_LONG).show();
                    ;
                }
            }
        });

        //リスト項目がクリックされた時の処理
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> list, View view, int position, long id) {

                // クリックされたポジションをGetTime.javaに渡す
                boolean flag = task.setClickId(position);
                if (flag) {
                    Toast.makeText(getApplicationContext(), "通知offにしました",
                            Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), "通知onにしました",
                            Toast.LENGTH_LONG).show();
                }

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

        noticeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            // 通知時間を変更するSpinnerが選択された時の処理
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Spinner spinParent = (Spinner) parent;
                String myItem = (String) spinParent.getSelectedItem();
                int myPosition = spinParent.getSelectedItemPosition();

                String [] noticeTime = myItem.split("@");
                String [] timeString;
                int minute;

                if(noticeTime[1].contains("分")){
                    timeString = noticeTime[1].split("分");
                    minute = Integer.parseInt(timeString[0]);
                }else{
                    timeString = noticeTime[1].split("時間");
                    minute = Integer.parseInt(timeString[0]) * 60;
                }

                task.setNoticeTime(minute);

                Toast.makeText(getApplicationContext(),
                        "通知時間を" + myItem + "に設定しました" , Toast.LENGTH_SHORT).show();

            }

            // 通知時間を変更するSpinnerが選択されなかった時の処理
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Toast.makeText(getApplicationContext(), "nothing", Toast.LENGTH_SHORT).show();
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
