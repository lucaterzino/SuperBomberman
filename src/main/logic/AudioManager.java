package main.logic;

import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

// Gestore Audio Singleton
public class AudioManager {

    private static AudioManager instance;
    private MediaPlayer musicPlayer;
    private Map<String, AudioClip> soundEffects;
    private boolean isMuted = false;
    private double globalVolume = 0.5; // Volume default (0.0 - 1.0)

    private AudioManager() {
        soundEffects = new HashMap<>();
        loadSounds();
    }

    public static AudioManager getInstance() {
        if (instance == null) {
            instance = new AudioManager();
        }
        return instance;
    }

    private void loadSounds() {
        // Carica effetti .wav 
        loadClip("cursor", "/main/audio/cursor.wav");
        loadClip("confirm", "/main/audio/confirm.wav");
        loadClip("bomb_place", "/main/audio/bomb_place.wav");
        loadClip("explosion", "/main/audio/explosion.wav");
        loadClip("powerup", "/main/audio/powerup.wav");
        loadClip("death", "/main/audio/death.wav");
        loadClip("win", "/main/audio/win.wav");
        loadClip("win", "/main/audio/lose.wav");
        loadClip("spawn", "/main/audio/spawn.wav");
    }

    private void loadClip(String key, String path) {
        try {
            URL url = getClass().getResource(path);
            if (url != null) {
                AudioClip clip = new AudioClip(url.toExternalForm());
                soundEffects.put(key, clip);
            } else {
                System.out.println("Audio mancante: " + path);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void playSound(String key) {
        if (isMuted) return;
        AudioClip clip = soundEffects.get(key);
        if (clip != null) {
            clip.setVolume(globalVolume); 
            // Per esplosioni permettiamo sovrapposizione, per altri magari no
            if (key.equals("explosion") || !clip.isPlaying()) {
                clip.play();
            }
        }
    }

    public void playMusic(String path) {
        if (isMuted) return;
        stopMusic(); // Ferma eventuale musica precedente
        
        try {
            URL url = getClass().getResource(path);
            if (url != null) {
                Media media = new Media(url.toExternalForm());
                musicPlayer = new MediaPlayer(media);
                musicPlayer.setCycleCount(MediaPlayer.INDEFINITE); // Loop infinito
                musicPlayer.setVolume(globalVolume * 0.7); // Musica leggermente più bassa degli effetti
                musicPlayer.play();
            } else {
                System.out.println("Musica mancante: " + path);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopMusic() {
        if (musicPlayer != null) {
            musicPlayer.stop();
            musicPlayer.dispose();
            musicPlayer = null;
        }
    }
    
    public void pauseMusic() {
        if (musicPlayer != null) musicPlayer.pause();
    }
    
    public void resumeMusic() {
        if (musicPlayer != null && !isMuted) musicPlayer.play();
    }

    // --- GESTIONE VOLUME ---
    
    public void setVolume(double volume) {
        this.globalVolume = Math.max(0.0, Math.min(1.0, volume)); // Clamp tra 0 e 1
        
        // Aggiorna volume musica in tempo reale
        if (musicPlayer != null) {
            musicPlayer.setVolume(globalVolume * 0.7);
        }
    }
    
    public double getVolume() {
        return globalVolume;
    }
}