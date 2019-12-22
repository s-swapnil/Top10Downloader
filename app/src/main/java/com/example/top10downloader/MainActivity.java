package com.example.top10downloader;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "Myactivity";
    private String feedURL="http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topfreeapplications/limit=%d/xml";
    private int feedLimit=10;
    private ListView listApps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listApps=findViewById(R.id.xmlListView);


    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id=item.getItemId();


        switch (id)
        {
            case R.id.mnuFree:
                feedURL="http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topfreeapplications/limit=%d/xml";
                break;
            case R.id.mnuPaid:
                feedURL="http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/toppaidapplications/limit=%d/xml";
                break;
            case R.id.mnuSongs:
                feedURL="http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topsongs/limit=%d/xml";
                break;

            case R.id.mnu10:
            case R.id.mnu25:
                if (!item.isChecked())
                {
                    item.setChecked(true);
                    feedLimit=35-feedLimit;
                    break;
                }
                default:
                    return super.onOptionsItemSelected(item);
        }
        downloadURL(String.format(feedURL,feedLimit));
        return true;

    }
    private void downloadURL(String feedURL)
    {
        DownloadData downloadData=new DownloadData();
        downloadData.execute(feedURL);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.feeds_menu,menu);
        if (feedLimit==10)
        {menu.findItem(R.id.mnu10).setChecked(true);}
        else
        {menu.findItem(R.id.mnu25).setChecked(true);}
        return true;
    }

    private class DownloadData extends AsyncTask<String,Void,String>{

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                ParseApplications parseApplications=new ParseApplications();
                parseApplications.parse(s);
//                ArrayAdapter<FeedEntry> arrayAdapter=new ArrayAdapter<FeedEntry>(MainActivity.this,R.layout.list_item,parseApplications.getApplications());
//                listApps.setAdapter(arrayAdapter);
                FeedAdapter feedAdapter=new FeedAdapter(MainActivity.this,R.layout.list_record,parseApplications.getApplications());
                listApps.setAdapter(feedAdapter);
            }

            @Override
            protected String doInBackground(String...strings) {
                String rssfeed=downloadXML(strings[0]);
                if (rssfeed==null)
                {
                    Log.e(TAG, "doInBackground: Error downloading");
                }
                return rssfeed;
            }

            private String downloadXML(String urlpath)
            {
                StringBuilder xmlResult=new StringBuilder();

                try {
                    URL url=new URL(urlpath);
                    HttpURLConnection connection=(HttpURLConnection) url.openConnection();
                    int response=connection.getResponseCode();
                    InputStream inputStream=connection.getInputStream();
                    InputStreamReader inputStreamReader=new InputStreamReader(inputStream);
                    BufferedReader reader=new BufferedReader(inputStreamReader);

                    int charsread;
                    char[] inputBuffer=new char[500];
                    while (true)
                    {
                        charsread=reader.read(inputBuffer);
                        if (charsread<0)
                        {
                            break;
                        }
                        if (charsread>0)
                        {
                            xmlResult.append(String.copyValueOf(inputBuffer,0,charsread));
                        }
                    }
                    reader.close();
                    return xmlResult.toString();
                }
                
                catch (MalformedURLException e)
                {
                    Log.e(TAG, "downloadXML: Invalid URL: "+e.getMessage());
                }
                catch (IOException e)
                {
                    Log.e(TAG, "downloadXML: IO Exception Reading Data"+e.getMessage());
                }
                catch (SecurityException e) {
                    Log.e(TAG, "downloadXML: Security exception" + e.getMessage());
                    e.printStackTrace();
                }
                    return null;

                }
            }
        }

