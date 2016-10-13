package br.com.fiap.musicplayer;

import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Shido on 11/01/2016.
 */
public class LyricsService extends IntentService {



    private InputStream is = null;
    private JSONObject jObj = null;
    private String json = "";
    public final Gson gson = new Gson();

    private String letra = "";


    //Consgtantes se a letra foi trazida

    public static final int LYRICS_OK = 1 ;
    public static final int LYRICS_ERROR = 0;

    public static final String RESULT_RECEIVER = "resultReceiver";


    public LyricsService() {
        super(LyricsService.class.getName());
    }



    @Override
    protected void onHandleIntent(Intent intent) {
        final ResultReceiver receiver = intent.getParcelableExtra(LyricsService.RESULT_RECEIVER);
        String title = intent.getStringExtra(LyricsActivity.TITLE);
        String artist = intent.getStringExtra(LyricsActivity.ARTIST);

        JSONObject json = getResponse(artist, title);
       Log.i("JSON OBJECT JSON", json.toString());

        try{
            JSONObject mus = (JSONObject) json.getJSONArray("mus").get(0);

            Bundle b = new Bundle();
            b.putString("Lyrics", mus.getString("text"));

            receiver.send(LYRICS_OK, b);

        }catch (Exception e) {
            receiver.send(LYRICS_ERROR,Bundle.EMPTY);
            //e.printStackTrace();

        }




    }



    public JSONObject getResponse(String artist, String title) {
        String responsestring = null;
        HttpURLConnection urlConnection = null;

        try {
            OkHttpClient client = new OkHttpClient();
            String uri = Uri.parse("http://api.vagalume.com.br/search.php").buildUpon().appendQueryParameter
                    ("mus", title).appendQueryParameter("art", artist).build().toString();



            URL urla = new URL(uri);
            urlConnection = (HttpURLConnection) urla.openConnection();


            InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());


            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "iso-8859-1"), 8);

            StringBuilder sb = new StringBuilder();

            String line = null;
            while ((line = reader.readLine()) != null) {

                sb.append(line + "n");

            }
             inputStream.close();
            json = sb.toString();


           jObj = new JSONObject(json);

        } catch (Exception j) {
            j.printStackTrace();
            Log.e("Buffer Error", "Error converting result " + j.toString());
        }


        return jObj;


    }





}
