package br.com.fiap.musicplayer.event;

/**
 * Created by Shido on 14/01/2016.
 */

//Será responsavel para passar informações da Service para a ActivityMain
public class MusicEvent {

    //Duração da Musica
    private double startTime = 0;
    private double finalTime = 0;

    public static int oneTimeOnly = 0;




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
