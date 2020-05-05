package com.example.newsreader;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {

    ListView newsList;
    ArrayList<String>news=new ArrayList<String>();
    ArrayList<String>articleUrl=new ArrayList<String>();
    ArrayAdapter<String> adapter;

    public class DownloadTask extends AsyncTask<String,Void,ArrayList<String>>{

        @Override
        protected ArrayList<String> doInBackground(String... urls) {
            String result="";
            URL url;
            HttpsURLConnection urlConnection;
            InputStream in;
            InputStreamReader reader;
            try{
                url=new URL(urls[0]);
                urlConnection= (HttpsURLConnection) url.openConnection();
                in=urlConnection.getInputStream();
                reader=new InputStreamReader(in);
                int data=reader.read();
                while(data!=-1){
                    result+=(char)data;
                    data=reader.read();
                }
            } catch (Exception e){
                e.printStackTrace();
                return null;
            }
            try {
                JSONArray jsonArray = new JSONArray(result);
                int n=jsonArray.length();
                if(n>20) n=20;
                for(int i=0;i<n;i++){
                    String id=jsonArray.getString(i),json="";
                    url=new URL("https://hacker-news.firebaseio.com/v0/item/"+id+".json?print=pretty");
                    urlConnection= (HttpsURLConnection) url.openConnection();
                    in=urlConnection.getInputStream();
                    reader=new InputStreamReader(in);
                    int data=reader.read();
                    while(data!=-1){
                        json+=(char)data;
                        data=reader.read();
                    }
//                    System.out.println(json);
                    JSONObject jsonObject=new JSONObject(json);
                    if(!jsonObject.isNull("title") && !jsonObject.isNull("url")){
                        String ur,tit;
                        ur=jsonObject.getString("url");
                        tit=jsonObject.getString("title");
                        articleUrl.add(ur);
                        news.add(tit);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
//            System.out.println(news);
            return news;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        newsList=findViewById(R.id.newsList);
        DownloadTask task=new DownloadTask();
        try {
            news=task.execute("https://hacker-news.firebaseio.com/v0/topstories.json?print=pretty").get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        adapter=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,news);
        newsList.setAdapter(adapter);
        newsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent=new Intent(getApplicationContext(),WebActivity.class);
                intent.putExtra("url",articleUrl.get(position));
                startActivity(intent);
            }
        });
    }
}
