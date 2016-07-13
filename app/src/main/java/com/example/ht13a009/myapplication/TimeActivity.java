package com.example.ht13a009.myapplication;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    private SharedPreferences prefs;
    private ListView listView;

    private boolean allFlag = false;
    private String myItem;
    private static final int ACTION_ID = 0;

    private TimeArrayAdapter adapter;
    private GetTime task;
    private String Route;
    private int routeId;

    private String stringYear = null;
    private String stringMonth = null;
    private String stringDay = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time);

        TextView textView = (TextView) findViewById(R.id.textView);
        // テキストビューのテキストを設定します
        textView.setText("   all通知：");

        TextView textView2 = (TextView) findViewById(R.id.textView2);
        // テキストビューのテキストを設定します
        textView2.setText(" 通知時間：");

        // 現在のintentを取得する
        Intent intent = getIntent();

        // intentから指定キーの文字列を取得する
        Route = intent.getStringExtra("Route");
        routeId = intent.getIntExtra("id", -1);

        // Windowのタイトルを変更する
        setTitle(Route);

        // activity_time.xmlに設定したコンポーネントをid指定で取得します。
        Button allButton = (Button) findViewById(R.id.allButton);
        final Spinner noticeSpinner = (Spinner) findViewById(R.id.noticeSpinner);

        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean ShinobuArrivalTime = prefs.getBoolean("ShinobuArrivalTime", false);
        boolean ShinobuOnly = prefs.getBoolean("ShinobuOnly", false);

        // アダプタを生成してリストビューへセット
        adapter = new TimeArrayAdapter(this, R.layout.row, ShinobuArrivalTime, ShinobuOnly, Route, routeId);
        listView = (ListView) findViewById(R.id.listView2);
        listView.setAdapter(adapter);

        //String defalutNoticeTime = prefs.getString("defalutNoticeTime", "@10分");
        myItem = prefs.getString("defalutNoticeTime", "@10分");

        // 通知時間を変更するアダプター
        final ArrayAdapter<CharSequence> noticeAdapter = ArrayAdapter.createFromResource(this,
                R.array.noticeTimeList, R.layout.spinner_item);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        noticeSpinner.setAdapter(noticeAdapter);
        final int num = noticeAdapter.getCount();
        for (int i = 0; i < num; i++) {
            if (noticeAdapter.getItem(i).equals(myItem)) {
                noticeSpinner.setSelection(i); // 選択初期設定
                break;
            }
        }

        loadTimeList();

        // allボタン押した時全部の通知
        assert allButton != null;
        allButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (allFlag) {
                    allFlag = false;

                    // ベルのアイコンをlistViewの全てのviewで非表示
                    for (int i = 0; i < listView.getChildCount(); i++) {
                       adapter.setAlarm(i, false);
                    }

                    Toast.makeText(getApplicationContext(), "全ての通知をoff",
                            Toast.LENGTH_LONG).show();

                } else {
                    allFlag = true;

                    // ベルのアイコンをlistViewの全てのviewで表示
                    for (int i = 0; i < listView.getChildCount(); i++) {
                        adapter.setAlarm(i, true);
                    }

                    Toast.makeText(getApplicationContext(), "全ての通知をon",
                            Toast.LENGTH_LONG).show();

                }

            }
        });

        //リスト項目がクリックされた時の処理
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> list, View view, int position, long id) {

                if (adapter.toggleAlarm(position)) {
                    Toast.makeText(getApplicationContext(), myItem + "前に通知します", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), myItem + "前の通知はやめました", Toast.LENGTH_LONG).show();
                }

            }
        });

        listView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            //リスト項目が選択された時の処理
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            }

            //リスト項目がなにも選択されていない時の処理
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //リスト項目が長押しされた時の処理
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                return false;
            }
        });

        noticeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            // 通知時間を変更するSpinnerが選択された時の処理
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Spinner spinParent = (Spinner) parent;
                myItem = (String) spinParent.getSelectedItem();
                int myPosition = spinParent.getSelectedItemPosition();

                int minute = changeStoI(myItem);

                adapter.setPreNoticeTime(minute);

                Toast.makeText(getApplicationContext(),
                        myItem, Toast.LENGTH_SHORT).show();

            }

            // 通知時間を変更するSpinnerが選択されなかった時の処理
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Toast.makeText(getApplicationContext(), "nothing", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // カレンダーアイコンの追加
        MenuItem actionItem = menu.add(Menu.NONE, ACTION_ID, Menu.NONE, "Action Button");
        actionItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        actionItem.setIcon(R.drawable.calendar);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case ACTION_ID:

                DialogFragment newFragment = new DatePick();
                newFragment.show(getSupportFragmentManager(), "datePicker");

                return true;
        }
        return false;
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

        stringYear = String.valueOf(year);
        stringMonth = String.valueOf(monthOfYear + 1);
        stringDay = String.valueOf(dayOfMonth);

        loadTimeList();
        adapter.setDate(stringYear, stringMonth, stringDay);

    }

    public void loadTimeList() {

        if(task != null){
            task.cancel(true);
        }

        task = new GetTime(adapter);

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
            URL url;
            if (stringYear != null && stringMonth != null && stringDay != null) {
                url = new URL("https://bus.oden.oecu.jp/api/1.3.1/Dias.json?route_id=" + routeId + "&date="
                        + stringYear + "/" + stringMonth + "/" + stringDay);
            } else {
                url = new URL("https://bus.oden.oecu.jp/api/1.3.1/Dias.json?route_id=" + routeId + "&date=" + sdf.format(date));
            }

            task.execute(url, ui_handler, finally_runnable);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

    }

    public int changeStoI(String s) {

        int i;
        String[] stringNumber;
        String[] sArr = s.split("@");

        if (sArr[1].contains("分")) {
            stringNumber = sArr[1].split("分");
            i = Integer.parseInt(stringNumber[0]);
        } else {
            stringNumber = sArr[1].split("時間");
            i = Integer.parseInt(stringNumber[0]) * 60;
        }

        return i;
    }

}
