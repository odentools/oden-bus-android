package com.example.ht13a009.myapplication;

import android.os.AsyncTask;
import android.widget.ArrayAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

class MyHttpConnection extends AsyncTask<URL, Void, String> {

    ArrayAdapter<String> mAdapter;
    Map<String, Integer> map = new HashMap<String, Integer>();

    public MyHttpConnection(ArrayAdapter<String> adapter , HashMap<String , Integer> hashMap) {
        super();
        mAdapter = adapter;
        map = hashMap;
    }

    @Override
    protected String doInBackground(URL... url) {
        String result = "";
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) url[0].openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.connect();
            //int resp = conn.getResponseCode();
            // respを使っていろいろ
            result = readIt(conn.getInputStream());
        } catch(IOException e) {
            e.printStackTrace();
        } finally {
            if(conn != null) {
                conn.disconnect();
            }
        }
        return result;
    }

    public String readIt(InputStream stream) throws IOException {
        StringBuffer sb = new StringBuffer();
        String line;
        BufferedReader br = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
        while((line = br.readLine()) != null){
            sb.append(line);
        }
        try {
            stream.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    @Override
    protected void onPostExecute(String resp){

        ArrayList<String> arr = new ArrayList<>();

        try {

            JSONObject json = new JSONObject(resp);

            JSONArray datas = json.getJSONArray("Route");

            for (int i = 0; i < datas.length(); i++) {
                JSONObject data = datas.getJSONObject(i);

                arr.add(data.getString("routePrefix") + " " + data.getString("routeName"));
                // ルート名とid番号を連想配列に格納
                map.put(data.getString("routePrefix") + " "
                        + data.getString("routeName"), data.getInt("id"));

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        mAdapter.clear();
        mAdapter.addAll(arr);
    }
}