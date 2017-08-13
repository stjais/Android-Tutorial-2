package com.exampledemo.parsaniahardik.jsonparsing;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;
import org.json.JSONException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private ParseContent parseContent;
    private final int jsoncode = 1;
    private ListView listView;
    private ArrayList<PlayersModel> playersModelArrayList;
    private CustomeAdapter customeAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        parseContent = new ParseContent(this);
        listView = (ListView) findViewById(R.id.lv);

        try {
            parseJson();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void parseJson() throws IOException, JSONException {

        if (!AndyUtils.isNetworkAvailable(MainActivity.this)) {
            Toast.makeText(MainActivity.this, "Internet is required!", Toast.LENGTH_SHORT).show();
            return;
        }
        AndyUtils.showSimpleProgressDialog(MainActivity.this);
        new AsyncTask<Void, Void, String>(){
            protected String doInBackground(Void[] params) {
                String response="";
                HashMap<String, String> map=new HashMap<>();
                try {
                    HttpRequest req = new HttpRequest(AndyConstants.ServiceType.URL);
                    response = req.prepare(HttpRequest.Method.POST).withData(map).sendAndReadString();
                } catch (Exception e) {
                    response=e.getMessage();
                }
                return response;
            }
            protected void onPostExecute(String result) {
                //do something with response
                Log.d("newwwss",result);
                onTaskCompleted(result,jsoncode);
            }
        }.execute();
    }

    public void onTaskCompleted(String response, int serviceCode) {
        Log.d("responsejson", response.toString());
        switch (serviceCode) {
            case jsoncode:

                if (parseContent.isSuccess(response)) {
                    AndyUtils.removeSimpleProgressDialog();  //will remove progress dialog
                    playersModelArrayList = parseContent.getInfo(response);
                    customeAdapter = new CustomeAdapter(this,playersModelArrayList);
                    listView.setAdapter(customeAdapter);

                }else {
                    Toast.makeText(MainActivity.this, parseContent.getErrorCode(response), Toast.LENGTH_SHORT).show();
                }
        }
    }
}
