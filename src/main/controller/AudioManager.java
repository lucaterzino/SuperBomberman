package main.controller;

import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.net.URL;
import java.util.HashMap;

public class AudioManager {

    private static AudioManager instance;

    public static AudioManager getInstance() {
        if (instance == null) {
            instance = new AudioManager();
        }
        return instance;
    }

    private MediaPlayer musicPlayer;
    private HashMap<String, AudioClip> soundEffects;

    private AudioManager() {
        soundEffects = new HashMap<>();
    }

    public void loadSoundEffect(String name, String filename) {
        try {
            URL resource = getClass().getResource(filename);
            if (resource != null) {
                AudioClip clip = new AudioClip(resource.toExternalForm());
                soundEffects.put(name, clip);
            } else {
                System.err.println("Impossibile trovare risorsa audio: " + filename);
            }
        } catch (Exception e) {
            System.err.println("Impossibile caricare effetto sonoro: " + filename);
            e.printStackTrace();
        }
    }

    public void playSoundEffect(String name) {
        AudioClip clip = soundEffects.get(name);
        if (clip != null) {
            clip.play();
        } else {
            System.err.println("Effetto sonoro non trovato: " + name);
        }
    }

    public void playMusic(String filename) {
        stopMusic(); 
        try {
            URL resource = getClass().getResource(filename);
            if (resource != null) {
                Media media = new Media(resource.toExternalForm());
                musicPlayer = new MediaPlayer(media);
                musicPlayer.setOnEndOfMedia(() -> {
                    musicPlayer.seek(Duration.ZERO);
                    musicPlayer.play();
                });
                musicPlayer.play();
            } else {
                System.err.println("Impossibile trovare risorsa musica: " + filename);
            }
        } catch (Exception e) {
            System.err.println("Impossibile riprodurre musica: " + filename);
            e.printStackTrace();
        }
    }

    public void stopMusic() {
        if (musicPlayer != null) {
            musicPlayer.stop();
            musicPlayer = null;
        }
    }
}