package com.example.ht13a009.myapplication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class TimeActivity extends AppCompatActivity {

    private HashMap<String, Integer> map;
    private SharedPreferences prefs;
    private ImageView appInfoImage;
    private ListView listView;

    private boolean allFlag = false;
    private boolean singleFlag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time);

        TextView textView = (TextView) findViewById(R.id.textView);
        // テキストビューのテキストを設定します
        textView.setText("all通知：");

        TextView textView2 = (TextView) findViewById(R.id.textView2);
        // テキストビューのテキストを設定します
        textView2.setText(" 通知時間：");

        // 現在のintentを取得する
        Intent intent = getIntent();

        // intentから指定キーの文字列を取得する
        String Route = intent.getStringExtra("Route");
        int id = intent.getIntExtra("id", -1);

        // Windowのタイトルを変更する
        setTitle(Route);

        map = new HashMap<String, Integer>();

        // activity_time.xmlに設定したコンポーネントをid指定で取得します。
        Button allButton = (Button) findViewById(R.id.allButton);
        final Spinner noticeSpinner = (Spinner) findViewById(R.id.noticeSpinner);

        // アダプタを生成してリストビューへセット
        ArrayAdapter<String> adapter = new ArrayAdapter<String>
                (this, R.layout.row, R.id.row_textview1);
        listView = (ListView) findViewById(R.id.listView2);
        listView.setAdapter(adapter);

        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String defalutNoticeTime = prefs.getString("defalutNoticeTime", "@10分");

        // 通知時間を変更するアダプター
        final ArrayAdapter<CharSequence> noticeAdapter = ArrayAdapter.createFromResource(this,
                R.array.noticeTimeList, R.layout.spinner_item);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        noticeSpinner.setAdapter(noticeAdapter);
        final int num = noticeAdapter.getCount();
        for (int i = 0; i < num; i ++) {
            if (noticeAdapter.getItem(i).equals(defalutNoticeTime)) {
                noticeSpinner.setSelection(i); // 選択初期設定
                break;
            }
        }

        final GetTime task = new GetTime(adapter, getApplicationContext(), Route);
        loadTimeList(task, id);

        // allボタン押した時全部の通知
        assert allButton != null;
        allButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (allFlag) {
                    allFlag = false;
                    task.setAllButton(allFlag);

                    // ベルのアイコンをlistViewの全てのviewで表示
                    for(int i = 0; i < listView.getChildCount(); i++){
                        LinearLayout linearLayout = (LinearLayout)listView.getChildAt(i);
                        linearLayout.findViewById(R.id.row_imageview).setVisibility(View.INVISIBLE);
                    }

                    Toast.makeText(getApplicationContext(), "全ての通知をoff",
                            Toast.LENGTH_LONG).show();
                } else {
                    allFlag = true;
                    task.setAllButton(allFlag);
                    task.setClickIdAll();

                    // ベルのアイコンをlistViewの全てのviewで表示
                    for(int i = 0; i < listView.getChildCount(); i++){
                        LinearLayout linearLayout = (LinearLayout)listView.getChildAt(i);
                        linearLayout.findViewById(R.id.row_imageview).setVisibility(View.VISIBLE);
                        linearLayout.performClick();
                    }

                    Toast.makeText(getApplicationContext(), "全ての通知をon",
                            Toast.LENGTH_LONG).show();
                    ;
                }
            }
        });

        //リスト項目がクリックされた時の処理
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> list, View view, int position, long id) {

                System.out.println("position: " + position);

                // クリックされたポジションをGetTime.javaに渡す
                singleFlag = task.setClickId(position);
                if (singleFlag) {

                    // アイコンを非表示
                    appInfoImage = (ImageView)view.findViewById(R.id.row_imageview);
                    appInfoImage.setVisibility(View.INVISIBLE);
                    // 色を白色に変更
                    //view.setBackgroundColor(getResources().getColor(R.color.white));

                    Toast.makeText(getApplicationContext(), "通知off",
                            Toast.LENGTH_LONG).show();
                } else {

                    // アイコンを表示
                    appInfoImage = (ImageView)view.findViewById(R.id.row_imageview);
                    appInfoImage.setVisibility(View.VISIBLE);
                    // 色を薄水色に変更
                    //view.setBackgroundColor(getResources().getColor(R.color.lightBlue));

                    Toast.makeText(getApplicationContext(), "通知on",
                            Toast.LENGTH_LONG).show();
                }

            }
        });

        listView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            //リスト項目が選択された時の処理
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                /*
                ListView listView = (ListView) parent;
                String item = (String) listView.getItemAtPosition(position);
                Toast.makeText(getApplicationContext(), item + " selected",
                        Toast.LENGTH_LONG).show();
                */

            }

            //リスト項目がなにも選択されていない時の処理
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

                /*
                Toast.makeText(getApplicationContext(), "no item selected",
                        Toast.LENGTH_LONG).show();
                */

            }
        });

        //リスト項目が長押しされた時の処理
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                /*
                ListView listView = (ListView) parent;
                String item = (String) listView.getItemAtPosition(position);
                Toast.makeText(getApplicationContext(), item + " long clicked",
                        Toast.LENGTH_LONG).show();
                */

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

                int minute = changeStoI(myItem);

                task.setNoticeTime(minute);

                Toast.makeText(getApplicationContext(),
                        myItem  , Toast.LENGTH_SHORT).show();

            }

            // 通知時間を変更するSpinnerが選択されなかった時の処理
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Toast.makeText(getApplicationContext(), "nothing", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void loadTimeList(final GetTime task, int id){

        // 現在の時刻を取得
        Date date = new Date();
        // 表示形式を設定
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy'/'MM'/'dd");

        // ダイアログを表示
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setIndeterminate(true);
        dialog.setTitle("Loading now...");
        dialog.setMessage("Please wait a moment");
        dialog.show();

        // 取得処理を開始
        final Handler ui_handler = new Handler();

        Runnable finally_runnable = new Runnable() {
            @Override
            public void run() {
                dialog.hide();
            }
        };

        try {
            URL url = new URL("https://bus.oden.oecu.jp/api/1.3.1/Dias.json?route_id=" + id + "&date=" + sdf.format(date));
            boolean ShinobuArrivalTime = prefs.getBoolean("ShinobuArrivalTime",false);
            boolean ShinobuOnly = prefs.getBoolean("ShinobuOnly", false);

            task.execute(url, TimeActivity.this, ui_handler, finally_runnable, ShinobuArrivalTime, ShinobuOnly);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

    }

    public int changeStoI(String s){

        int i;
        String [] stringNumber;
        String[] sArr = s.split("@");

        if(sArr[1].contains("分")){
            stringNumber = sArr[1].split("分");
            i = Integer.parseInt(stringNumber[0]);
        }else{
            stringNumber = sArr[1].split("時間");
            i = Integer.parseInt(stringNumber[0]) * 60;
        }

        return i;
    }

}
