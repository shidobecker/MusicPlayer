package br.com.fiap.musicplayer;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import br.com.fiap.musicplayer.adapter.MusicaAdapter;
import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;

public class MainActivity extends AppCompatActivity {

    private MusicService musicService;

    private boolean musicBound = false;
    private MediaPlayer player ;
    //Handler para atualizar o tempo de musica.
    private Handler myHandler = new Handler();;


    private LinearLayoutManager layoutManager;

    private List<Musica> musicas;
    private MusicaAdapter adapter;


    private Musica musicaAtual;
    private int positionAtual;


    @Bind(R.id.listMusicas)
    ListView listMusicas;





    @Bind(R.id.tvTitle)
    TextView tvTitle;

    @Bind(R.id.tvArtista)
    TextView tvArtista;

    @Bind(R.id.seekBar)
    SeekBar seekbar;


    //Botões Controladores
    @Bind(R.id.stop)
    ImageButton stop;

    @Bind(R.id.playpause)
    ImageButton playpause;

    @Bind(R.id.forwards)
    ImageButton forwards;

    @Bind(R.id.backwards)
    ImageButton backwards;


    //Tempo de musica
    private double startTime = 0;
    private double finalTime = 0;
    public static int oneTimeOnly = 0;


    @Bind(R.id.tvFinalTime)
    TextView tvFinalTime;

    @Bind(R.id.tvStartTime)
    TextView tvStartTime;





    public void pararServico() {
        Intent i = new Intent(this, MusicService.class);
        stopService(i);
    }


    @Override
    protected void onStart() {
        super.onStart();
        Intent mIntent = new Intent(this, MusicService.class);
        bindService(mIntent, mConnection, BIND_AUTO_CREATE);

    }

    //Conexão com o service para poder acessar os metodos do mesmo
    ServiceConnection mConnection = new ServiceConnection() {



        public void onServiceConnected(ComponentName name, IBinder service) {
            Toast.makeText(MainActivity.this, "Service is Connected", Toast.LENGTH_SHORT).show();
            musicBound = true;
            MusicService.MusicBinder mLocalBinder = (MusicService.MusicBinder)service;
            musicService = mLocalBinder.getServerInstance();

        }

        public void onServiceDisconnected(ComponentName name) {
            Toast.makeText(MainActivity.this, "Service is disconnected", Toast.LENGTH_SHORT).show();
            musicBound = false;
            musicService = null;
        }




    };


    protected void onStop(){
        super.onStop();
    /*    if(musicBound) {
            unbindService(mConnection);
            musicBound = false;
        }*/
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        ButterKnife.bind(this);

        musicas = new ArrayList<>();


        playpause.setBackgroundResource(android.R.drawable.ic_media_play);
        stop.setBackgroundResource(R.drawable.ic_stop2);
        forwards.setBackgroundResource(android.R.drawable.ic_media_next);
        backwards.setBackgroundResource(android.R.drawable.ic_media_previous);

        //Pegando as musicas do Cartão
        getListaMusicas();
        //Colocando em ordem alfabetica
        Collections.sort(musicas, new Comparator<Musica>() {
            public int compare(Musica a, Musica b) {
                return a.getTitulo().compareTo(b.getTitulo());
            }
        });

        for (int i = 0; i < musicas.size(); i++){
            Musica atual = musicas.get(i);
            atual.setId(i);
        }


        adapter = new MusicaAdapter(this, musicas);
        listMusicas.setAdapter(adapter);
        musicService = new MusicService();
        musicService.setMusicas(musicas);


        //Registrando para receber os eventos
        EventBus.getDefault().register(this);

        //Metodo que irá tocar a musica selecionada
        listMusicas.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Musica selectedMusic =(Musica)adapter.getItem(position);
                positionAtual = position;

                Uri uri = Uri.parse(selectedMusic.getUri());
                musicaAtual = musicService.playMusicFromList(selectedMusic, positionAtual);
                playpause.setBackgroundResource(android.R.drawable.ic_media_pause);

            }
        });





        playpause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pauseOrPlayMusica();
            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopMusica();
            }
        });


        forwards.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextSong();




            }
        });





        backwards.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                previousSong();
            }
        });


        tvTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                baixarLetra();

            }
        });


        headsetReceiver = new MusicIntentReceiver();


    }

    private MusicIntentReceiver headsetReceiver;
    //Registrando a intent do HeadSetReceiver

    @Override
    public void onResume() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
        registerReceiver(headsetReceiver, filter);
        super.onResume();
    }


    private class MusicIntentReceiver extends BroadcastReceiver {
        @Override public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
                int state = intent.getIntExtra("state", -1);
                switch (state) {
                    case 0:
                        Log.d("Headset Receiver", "Headset is unplugged");
                        if (musicService != null) {
                            Toast.makeText(MainActivity.this, "Headset Desconectado", Toast.LENGTH_SHORT).show();
                            pauseOrPlayMusica();
                        }
                        break;
                    case 1:
                        Log.d("Headset Receiver", "Headset Conectado");
                        break;
                    default:
                }
            }
        }
    }


    public void baixarLetra(){

        //Abre a nova Activity
        Intent i = new Intent(MainActivity.this, LyricsActivity.class);
        i.putExtra("Title", tvTitle.getText());
        i.putExtra("Artist", tvArtista.getText());

        startActivity(i);

        //Intent i = new Intent(MainActivity.this, LyricsService.class);
        //i.putExtra(LyricsService.RESULT_RECEIVER, lyricsReceiver);

       // startService(i);





    }














    public void setArtistAndSong(Musica selectedMusic){
        tvTitle.setText(selectedMusic.getTitulo());
        tvArtista.setText(selectedMusic.getArtista());
    }


    public void setMusicTime(){
        tvFinalTime.setText(String.format("%d min, %d sec",
                        TimeUnit.MILLISECONDS.toMinutes((long) finalTime),
                        TimeUnit.MILLISECONDS.toSeconds((long) finalTime) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) finalTime)))
        );

        tvStartTime.setText(String.format("%d min, %d sec",
                        TimeUnit.MILLISECONDS.toMinutes((long) startTime),
                        TimeUnit.MILLISECONDS.toSeconds((long) startTime) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) startTime)))
        );
    }




    public void pauseOrPlayMusica() {

        if (musicaAtual == null) {
            Toast.makeText(MainActivity.this, "Selecione uma música primeiro", Toast.LENGTH_SHORT).show();
        }else{
            if (musicService != null) {
                String button = musicService.playOrPauseMusic();
                if (button.equalsIgnoreCase("Play")) {
                    playpause.setBackgroundResource(android.R.drawable.ic_media_play);

                } else {
                    playpause.setBackgroundResource(android.R.drawable.ic_media_pause);


                }

            }

        }
    }

    public void stopMusica() {

        if(musicService != null){
            tvArtista.setText("");
            tvTitle.setText("");
            tvStartTime.setText("");
            tvFinalTime.setText("");
            musicService.stopMusic();
            seekbar.setProgress(0);
            playpause.setBackgroundResource(android.R.drawable.ic_media_play);

        }


    }




    public void nextSong() {

        try{
            Musica nextMusic =(Musica)adapter.getItem(positionAtual + 1);
            positionAtual = positionAtual +1;

            musicaAtual = nextMusic;

            musicService.playMusicFromList(nextMusic, positionAtual + 1);

            playpause.setBackgroundResource(android.R.drawable.ic_media_pause);


        } catch (IndexOutOfBoundsException ain){
            Toast.makeText(MainActivity.this, "Ultima Musica da lista", Toast.LENGTH_SHORT).show();
        }


    }



    public void previousSong() {

        try{
            Musica previousMusic =(Musica)adapter.getItem(positionAtual - 1);

            positionAtual = positionAtual -1;
            musicaAtual = previousMusic;

            musicService.playMusicFromList(previousMusic, positionAtual - 1);

            playpause.setBackgroundResource(android.R.drawable.ic_media_pause);

        }catch (IndexOutOfBoundsException ain){
            Toast.makeText(MainActivity.this, "Primeira Musica da lista", Toast.LENGTH_SHORT).show();
        }


    }




    private Runnable UpdateSongTime = new Runnable() {
        public void run() {
            try{
                startTime = musicService.getPlayerCurrentPosition();
                tvStartTime.setText(String.format("%d min, %d sec",

                                TimeUnit.MILLISECONDS.toMinutes((long) startTime),
                                TimeUnit.MILLISECONDS.toSeconds((long) startTime) -
                                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.
                                                toMinutes((long) startTime)))
                );
                seekbar.setProgress((int) startTime);
                myHandler.postDelayed(this, 100);
            }catch (IllegalStateException is){
                seekbar.setProgress(0);
                is.printStackTrace();


            }catch (NullPointerException nu){
                seekbar.setProgress(0);
                nu.printStackTrace();
            }

            }

    };





    //Metodo que receberá o evento de carregar
    public void onEvent(String event) {
        //Chamando o metodo da service de parar a musica
        if(event.equalsIgnoreCase("Headset_unplugged")) {
            if (musicService != null) {
                Toast.makeText(MainActivity.this, "Headset unpluugged", Toast.LENGTH_SHORT).show();
                musicService.stopMusic();
            }
        }
    }






    //Metodo que receberá o evento de musica
    public void onEvent(Musica event) {
        //Recebe a musica que veio do Service pelo Eventbus para acertar parametros de duração, etc
        Log.i("MUSICA EVENT - duration", String.valueOf(event.getFinalTime()));

        //Setando artista
        setArtistAndSong(event);

        //Setando tempo de duração
        finalTime = event.getFinalTime();
        startTime = event.getStartTime();


        if (oneTimeOnly == 0) {
            seekbar.setMax((int) finalTime);
            oneTimeOnly = 1;
        }
        setMusicTime();
        seekbar.setProgress((int) startTime);

        myHandler.postDelayed(UpdateSongTime, 100);

    }






    public void getListaMusicas() {
        String path = Environment.getExternalStorageDirectory().toString() + "/musica/";

        File f = new File(path);

        Log.i("DIR", f.getAbsolutePath());

        File[] files = f.listFiles();

        for (File fa :
                files) {
            Log.i("FIIIIILE", fa.getName());
        }
        //String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";

        ContentResolver cr = getContentResolver();
        //Cursor musicCursor = cr.query(path, null, null, null, null);

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

    //Isso limita a pasta onde o MediaStore irá buscar as musicas utilizando atraves da Query do cursor.
    String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0 AND " +
            MediaStore.Audio.Media.DATA + " LIKE '/storage/emulated/0/musica/%'";
    //String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
    String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";
    Cursor cur = cr.query(uri, null, selection, null, sortOrder);
    int count = 0;

    if(cur != null) {
        count = cur.getCount();
        Log.i("COUNT CURSOR", String.valueOf(count));
        if(count > 0 && cur !=null) {
            cur.moveToFirst();
            while(cur.moveToNext()) {
                Musica m = new Musica();
                String data = cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.DATA));
                m.setTitulo(cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.TITLE)));
                m.setArtista(cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.ARTIST)));
                m.setFinalTime(cur.getDouble(cur.getColumnIndex(MediaStore.Audio.Media.DURATION)));
                m.setUri(data);

                // Save to your list here
                Log.i("MUSICA DATA", data);
                musicas.add(m);
            }

        }
    }

    cur.close();


}


}
