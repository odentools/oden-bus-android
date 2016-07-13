package com.example.ht13a009.myapplication;

import android.os.AsyncTask;
import android.os.Handler;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

class GetTime extends AsyncTask<Object, Void, String> {

    private TimeArrayAdapter mAdapter;

    // パラメータ[2] - UI制御用ハンドラ
    private Handler mHandler = null;
    // パラメータ[3] - 完了処理として実行したいRunnable
    private Runnable finallyRunnable = null;

    public GetTime(TimeArrayAdapter adapter) {
        super();
        mAdapter = adapter;
    }

    @Override
    protected String doInBackground(Object... objects) {
        String result = "";
        HttpURLConnection conn = null;
        URL url = (URL) objects[0];
        mHandler = (Handler) objects[1];
        finallyRunnable = (Runnable) objects[2];

        try {

            conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.connect();

            //int resp = conn.getResponseCode();
            // respを使っていろいろ
            result = readIt(conn.getInputStream());
        } catch (IOException e) {

            e.printStackTrace();
        } finally {
            if (conn != null) {
                conn.disconnect();
            }

        }
        return result;
    }

    public String readIt(InputStream stream) throws IOException {
        StringBuffer sb = new StringBuffer();
        String line;
        BufferedReader br = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        try {
            stream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    @Override
    protected void onPostExecute(final String resp) {

        try {

            // JSONをパース
            JSONObject json = new JSONObject(resp);

            // JSONからDia配列を取得
            JSONArray datas = json.getJSONArray("Dia");

            // ArrayAdapterに要素を追加
            mAdapter.clear();
            for (int i = 0, l = datas.length(); i < l; i++) {
                JSONObject data = datas.getJSONObject(i);
                mAdapter.add(data);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        // 完了時の処理を実行
        if (finallyRunnable != null){
            mHandler.post(finallyRunnable);
        }

    }

}
