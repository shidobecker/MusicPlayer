package br.com.fiap.musicplayer;

import android.content.Intent;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import butterknife.Bind;
import butterknife.ButterKnife;

public class LyricsActivity extends AppCompatActivity {


    @Bind(R.id.tvLyTitulo)
    TextView tvLyTitulo;

    @Bind(R.id.tvLyArtista)
    TextView tvLyArtista;

    @Bind(R.id.tvLyrics)
    TextView tvLyrics;

    public static final String TITLE = "TITLE";
    public static final String ARTIST = "ARTISTA";

    private LyricsResultReceiver lyricsReceiver;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lyrics);

        ButterKnife.bind(this);
        tvLyTitulo.setText(getIntent().getStringExtra("Title"));
        tvLyArtista.setText(getIntent().getStringExtra("Artist"));
        lyricsReceiver = new LyricsResultReceiver(new Handler());


        Intent i = new Intent(LyricsActivity.this, LyricsService.class);
        i.putExtra(LyricsService.RESULT_RECEIVER, lyricsReceiver);
        i.putExtra(TITLE,tvLyTitulo.getText());
        i.putExtra(ARTIST, tvLyArtista.getText());

        startService(i);

    }




    //Classe que mandará o resultado para trafegar com a activity o resultado do serviço
    private class LyricsResultReceiver extends ResultReceiver {

        public LyricsResultReceiver(Handler handler) {
            super(handler);
        }






        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            super.onReceiveResult(resultCode, resultData);
            switch (resultCode){
                case (LyricsService.LYRICS_ERROR):
                    Toast.makeText(LyricsActivity.this, "Não foi possivel carregar a letra", Toast.LENGTH_LONG).show();

                    break;

                case(LyricsService.LYRICS_OK):
                    String lyrics = resultData.getString("Lyrics");
                    Toast.makeText(LyricsActivity.this,"Letra Carregada", Toast.LENGTH_LONG).show();
                    tvLyrics.setText(lyrics);
                    break;
            }

        }


    }








}
