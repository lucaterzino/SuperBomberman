package main.logic;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

// Gestore Audio Singleton
public class AudioManager {

    private static AudioManager instance;
    private final Map<String, AudioClip> soundEffects = new HashMap<>();
    private final Map<String, MediaPlayer> musicTracks = new HashMap<>();
    private MediaPlayer currentMusic;
    private final boolean isMuted = false;
    private double globalVolume = 0.5; // Volume default (0.0 - 1.0)

    private AudioManager() {
        preloadSounds();
        preloadMusic();
    }

    public static AudioManager getInstance() {
        if (instance == null) {
            instance = new AudioManager();
        }
        return instance;
    }

    private void preloadSounds() {
        // Carica effetti .wav 
        loadClip("cursor", "/audio/cursor.wav");
        loadClip("confirm", "/audio/confirm.wav");
        loadClip("bomb_place", "/audio/bomb_place.wav");
        loadClip("explosion", "/audio/explosion.wav");
        loadClip("powerup", "/audio/powerup.wav");
        loadClip("death", "/audio/death.wav");
        loadClip("win", "/audio/win.wav");
        loadClip("lose", "/audio/lose.wav");
        loadClip("respawn", "/audio/respawn.wav");
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

    private void preloadMusic() {
        loadMusic("menu", "/audio/song_menu.mp3");
        loadMusic("game", "/audio/song_gamplay.mp3");
    }

    public void loadMusic(String key, String path) {
        try {
            URL url = getClass().getResource(path);
            if (url != null) {
                Media media = new Media(url.toURI().toString());
                MediaPlayer player = new MediaPlayer(media);
                player.setCycleCount(MediaPlayer.INDEFINITE);
                player.setVolume(globalVolume * 0.3);
                musicTracks.put(key, player);
            } else {
                System.out.println("Musica mancante: " + path);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

   public void playMusic(String key) {
        if (isMuted) return;
        stopMusic();

        MediaPlayer player = musicTracks.get(key);
        if (player != null) {
            currentMusic = player;
            currentMusic.play();
        }
    }

    public void stopMusic() {
        if (currentMusic != null) {
            currentMusic.stop();
            currentMusic = null;
        }
    }

    public void pauseMusic() {
        if (currentMusic != null) currentMusic.pause();
    }

    public void resumeMusic() {
        if (currentMusic != null && !isMuted) currentMusic.play();
    }
    // --- GESTIONE VOLUME ---
    
    // --- VOLUME MANAGEMENT ---
    public void setVolume(double volume) {
        globalVolume = Math.max(0.0, Math.min(1.0, volume));
        if (currentMusic != null) currentMusic.setVolume(globalVolume * 0.3);
        soundEffects.values().forEach(clip -> clip.setVolume(globalVolume));
    }

    public double getVolume() {
        return globalVolume;
    }

    public boolean isMuted() {
        return isMuted;
    }
}