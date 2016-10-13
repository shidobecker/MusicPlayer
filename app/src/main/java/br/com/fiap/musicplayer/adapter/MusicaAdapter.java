package br.com.fiap.musicplayer.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.List;
import java.util.concurrent.TimeUnit;

import br.com.fiap.musicplayer.Musica;
import br.com.fiap.musicplayer.R;
import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Shido on 06/01/2016.
 */
public class MusicaAdapter extends BaseAdapter{

    private Context context;
    private LayoutInflater musicaLinf;

    private List<Musica> musicas;


    //Construtor para criar o contexto
    public MusicaAdapter(Context context, List<Musica> musicas) {
        this.context = context;
        this.musicas = musicas;

    }

    @Override
    public int getCount() {
        return musicas.size();
    }

    @Override
    public Object getItem(int position) {
        return musicas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return musicas.get(position).getId();
    }




    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;

        Musica currSong = musicas.get(position);

        //Se nao utilizar o viewholder, toda vez que entrar nesse metodo getView ele vai refazer a instacia, com o view holder a informação é reaproveitada
        if(convertView == null){
            //Significa que não tinha sido criado
            //Inflando a partir do contexto
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            //View que será inflada
            convertView = inflater.inflate(R.layout.musica_row, parent, false);
          //  holder.nomeMusica =(TextView) convertView.findViewById(R.id.nomeMusica);
           // holder.artista= (TextView) convertView.findViewById(R.id.artista);
            holder = new ViewHolder(convertView);

            convertView.setTag(holder);
        }else{
            //Setando a tag do holder caso a view já exista
            holder = (ViewHolder) convertView.getTag();
        }
        //Setando o titulo a partir da posição da lista de musica
        holder.nomeMusica.setText(musicas.get(position).getTitulo());
        holder.artista.setText(musicas.get(position).getArtista());
        holder.duration.setText(convertMusicDuration(musicas.get(position).getFinalTime()));
        return convertView;


    }


    public String convertMusicDuration(double finalTime){
        String duration = String.format("%d:%d ",
                TimeUnit.MILLISECONDS.toMinutes((long) finalTime),
                TimeUnit.MILLISECONDS.toSeconds((long) finalTime) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.
                                toMinutes((long) finalTime)));

        return duration;

    }

    static class ViewHolder {

        @Bind(R.id.nomeMusica)
        TextView nomeMusica;
        @Bind(R.id.artista)
        TextView artista;
        @Bind(R.id.duration)
        TextView duration;


        public ViewHolder(View view) {
            ButterKnife.bind(this, view);

        }


    }



}



