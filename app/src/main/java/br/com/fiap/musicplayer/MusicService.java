package br.com.fiap.musicplayer;

import android.app.Service;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import br.com.fiap.musicplayer.event.MusicEvent;
import de.greenrobot.event.EventBus;

/**
 * Created by Shido on 06/01/2016.
 */
public class MusicService  extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener,
        MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnInfoListener, MediaPlayer.OnSeekCompleteListener{

    private MediaPlayer player;
    private final IBinder musicBinder = new MusicBinder();

    private static final String TAG = "SERVICE TAG";
    //song list
    private List<Musica> musicas;
    //current position
    private int songPosn;

    private Musica musicaAtual;







    @Override
    public void onCreate() {
        player = new MediaPlayer();
        Log.i(TAG, "On Create Irá Iniciar player");
        //O que acontece quando o Media Player termina de tocar
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
        //Media Player precisar ser preparado antes de tocar, preparando as informações para tocar
        player.setOnPreparedListener(this);
        //Se a musica/video precisa carregar em algum momento
        player.setOnBufferingUpdateListener(this);
        //Seekbar
        player.setOnSeekCompleteListener(this);
        player.setOnInfoListener(this);
        //Para tocar em Standby
        player.setWakeMode(this, PowerManager.PARTIAL_WAKE_LOCK);

        player.reset();

      // iniciarPlayer();
        Log.i(TAG, "ON CREATE INICIOU PLAYER");
        //super.onCreate();

    }





    //Esse Metodo será chamado sempre que o usuário clicar numa musica da lista, então sempre deverá tocar aquela musica.
    public Musica playMusicFromList(Musica selectedMusic, int positionAtual){

        songPosn = positionAtual;
        Uri uri = Uri.parse(selectedMusic.getUri());

        if (player.isPlaying()) {
            player.stop();
            player.reset();
        }else{
            player.reset();
        }



            try {
                player.setDataSource(getApplicationContext(), uri);
                musicaAtual = selectedMusic;

                //Prepara o media player em uma thread separada para não segurar recursos
                player.prepareAsync();

            } catch (IOException e) {
                Log.i(TAG, "IO EXCEPTION");

            } catch (IllegalStateException il) {
                Log.i(TAG, "ILlegal State Exception");
                il.printStackTrace();

            } catch (IllegalArgumentException ia) {
                Log.i(TAG, "Illegal Argument Exception");

            }

        return musicaAtual;

    }



//O retorno será para mudar o botão de play para pause e vice versa.
    public String playOrPauseMusic(){
        String button = "";

        if (player != null) {

            if(player.isPlaying()){
                player.pause();


                button ="Play";
               // playpause.setBackgroundResource(R.drawable.ic_play22);
            }else {
                player.start();
               // playpause.setBackgroundResource(R.drawable.ic_pause2);
               button ="Pause";
            }

        }

        return button;
    }

    public void stopMusica() {
        player.stop();
        player.reset();
        player.release();
        player = null;


    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "ON START COMMAND");

    //Se não colocar ele irá terminar apos terminar o onStartCommand, com Start Sticky ele continua até que explicitamente terminemos o service
        return Service.START_STICKY;
    }





    @Override
    public void onDestroy() {

        super.onDestroy();

        Log.i(TAG, "Serviço destruído");

        if(player != null){
            if(player.isPlaying()){
                player.stop();
            }
            player.release();
        }
    }



    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {

    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {

    }



    @Override
    public void onCompletion(MediaPlayer mp) {
        stopMusic();
        //Ir para proxima musica
        musicas.get(songPosn);
        musicaAtual= musicas.get(songPosn +1);
        playMusicFromList(musicaAtual, songPosn + 1);
        //stopSelf();
    }





    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        switch (what){
            case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
                Toast.makeText(this, "Arquivo não é valido"+extra, Toast.LENGTH_SHORT);

            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                Toast.makeText(this, "O service parou"+extra, Toast.LENGTH_SHORT);

            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                Toast.makeText(this, "Erro desconhecido"+extra, Toast.LENGTH_SHORT);

        }
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        Log.i(TAG, "CHEGOU NO ONPREPARED");
           playMusic();
        //Setando as informações da musica de acordo com o player.
        musicaAtual.setStartTime(player.getCurrentPosition());
        musicaAtual.setFinalTime(player.getDuration());

        //Manda evento com as informações da Musica que está tocando
        EventBus.getDefault().post(musicaAtual);
    }


    public int getPlayerCurrentPosition(){
        return player.getCurrentPosition();
    }

    public void playMusic(){
        if(!player.isPlaying()){
            player.start();
        }
    }

    public void stopMusic(){
        if(player.isPlaying()){
            player.stop();
        }
    }





    //Bind do service com a Activity
    //private final IBinder mBinder = new MyBinder();
    @Nullable
    @Override

    public IBinder onBind(Intent intent) {
        Log.i("BIND SERVICE", "Service Bound");
        return musicBinder;
    }

    public class MusicBinder extends Binder {
        MusicService getServerInstance() {
            return MusicService.this;
        }
    }















    public void setMusicas(ArrayList<Musica> musicas) {
        this.musicas = musicas;
    }



    public void setMusica(int positionMusica){
        songPosn = positionMusica;

    }


    public List<Musica> getMusicas() {
        return musicas;
    }

    public void setMusicas(List<Musica> musicas) {
        this.musicas = musicas;
    }

}
