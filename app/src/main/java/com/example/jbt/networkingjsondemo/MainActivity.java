package com.example.jbt.networkingjsondemo;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    final static String LOG_CAT = "Networking Json App";
    final static String FIELD_NAME = "title";

    private EditText addressET;
    private Button getBtn;
    private ListView outputLV;

    private ArrayAdapter<String> adapter;

    private ProgressDialog pDialog;
    public static final int progress_bar_type = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        addressET = (EditText) findViewById(R.id.addressEditText);
        getBtn = (Button)findViewById(R.id.getButton);
        outputLV = (ListView)findViewById(R.id.listView);

        adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1);
        outputLV.setAdapter(adapter);

        getBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {

                    URL url;
                    url = new URL("http://" + addressET.getText().toString());
                    new WebJsonAsyncTask().execute(url);

                } catch (MalformedURLException e) {

                    Log.e(LOG_CAT, e.getMessage());
                    Toast.makeText(MainActivity.this, "invalid URL", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case progress_bar_type:
                pDialog = new ProgressDialog(this);
                pDialog.setMessage("Downloading file. Please wait...");
                pDialog.setIndeterminate(false);
                pDialog.setMax(100);
                pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                pDialog.setCancelable(true);
                pDialog.show();
                return pDialog;
            default:
                return null;
        }
    }

    public class WebJsonAsyncTask extends AsyncTask<URL, String, ArrayList<String>>
    {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showDialog(progress_bar_type);
        }

        @Override
        protected void onPostExecute(ArrayList<String> list) {

            dismissDialog(progress_bar_type);

            if(list == null)
                return;

            adapter.clear();
            adapter.addAll(list);

        }

        protected void onProgressUpdate(String... progress) {
            pDialog.setProgress(Integer.parseInt(progress[0]));
        }

        @Override
        protected ArrayList<String> doInBackground(URL... params) {

            ArrayList<String> list = new ArrayList<>();
            HttpURLConnection con = null;

            try {

                URL url = params[0];

                con = (HttpURLConnection)url.openConnection();

                if (con.getResponseCode() != HttpURLConnection.HTTP_OK)
                    return null;

                BufferedReader r = new BufferedReader(new InputStreamReader(con.getInputStream()));

                int lenghtOfFile = 26919; //con.getContentLength();
                long total = 0;

                String result = "", line;

                while((line = r.readLine()) != null) {
                    total += line.length();
                    publishProgress(""+(int)((total*100)/lenghtOfFile));
                    result += line;
                }

                JSONArray array = new JSONArray(result);
                for (int i=0; i< array.length(); i++)
                {
                    JSONObject user = array.getJSONObject(i);

                    if (user.has(FIELD_NAME))
                        list.add(user.getString(FIELD_NAME));
                }

            } catch (IOException e) {

                Log.e(LOG_CAT, e.getMessage());
                Toast.makeText(MainActivity.this, "cannot open connection", Toast.LENGTH_SHORT).show();

            } catch (JSONException e) {

                Log.e(LOG_CAT, e.getMessage());
                Toast.makeText(MainActivity.this, "Json Exception", Toast.LENGTH_SHORT).show();

            } finally {

                if (con != null)
                    con.disconnect();
            }

            return list;
        }
    }
}
