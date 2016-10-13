package br.com.fiap.musicplayer;

/**
 * Created by Shido on 06/01/2016.
 */
public class Musica {

    private long id;

    private String titulo;

    private String artista;

    private String urlCapa;

    private String uri;


    //Duração da Musica
    private double startTime = 0;
    private double finalTime = 0;



    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public Musica(long id, String titulo, String artista) {
        this.id = id;
        this.titulo = titulo;
        this.artista = artista;

    }


    public Musica(){}

    public long getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getArtista() {
        return artista;
    }

    public void setArtista(String artista) {
        this.artista = artista;
    }

    public String getUrlCapa() {
        return urlCapa;
    }

    public void setUrlCapa(String urlCapa) {
        this.urlCapa = urlCapa;
    }


    public double getStartTime() {
        return startTime;
    }

    public void setStartTime(double startTime) {
        this.startTime = startTime;
    }

    public double getFinalTime() {
        return finalTime;
    }

    public void setFinalTime(double finalTime) {
        this.finalTime = finalTime;
    }
}
