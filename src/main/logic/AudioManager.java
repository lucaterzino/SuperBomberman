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
    private MediaPlayer musicPlayer; // Per la musica (MP3, supporto streaming/lunga durata)
    private Map<String, AudioClip> soundEffects; // Per effetti (WAV, in memoria, bassa latenza)
    
    private boolean isMuted = false;
    private double globalVolume = 0.5; // Volume generale (0.0 a 1.0)

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
        // Carica tutti gli effetti sonori in memoria
        // Assicurati che i file siano in src/audio/ o resources/audio/
        loadClip("cursor", "/logic/cursor.wav");
        loadClip("confirm", "logic/confirm.wav");
        loadClip("bomb_place", "/logic/bomb_place.wav");
        loadClip("explosion", "/logic/explosion.wav");
        loadClip("powerup", "/logic/powerup.wav");
        loadClip("death", "/logic/death.wav");
        loadClip("win", "/logic/win.wav");
        loadClip("spawn", "/logic/spawn.wav");
    }

    private void loadClip(String key, String path) {
        try {
            URL url = getClass().getResource(path);
            if (url != null) {
                soundEffects.put(key, new AudioClip(url.toExternalForm()));
            } else {
                System.out.println("Audio mancante: " + path);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Riproduce un effetto sonoro
    public void playSound(String key) {
        if (isMuted) return;
        AudioClip clip = soundEffects.get(key);
        if (clip != null) {
            // Imposta il volume basato sul volume globale (clip volume è 0.0-1.0)
            // Possiamo alzare leggermente il volume degli effetti rispetto alla musica se necessario
            clip.setVolume(globalVolume); 
            
            // Per esplosioni, vogliamo sovrapposizione. Per cursori, magari no.
            if (key.equals("explosion") || !clip.isPlaying()) {
                clip.play();
            }
        }
    }

    // Avvia una traccia musicale in loop
    public void playMusic(String path) {
        if (isMuted) return;
        stopMusic(); // Ferma la precedente
        
        try {
            URL url = getClass().getResource(path);
            if (url != null) {
                Media media = new Media(url.toExternalForm());
                musicPlayer = new MediaPlayer(media);
                musicPlayer.setCycleCount(MediaPlayer.INDEFINITE); // Loop
                musicPlayer.setVolume(globalVolume * 0.7); // Musica leggermente più bassa
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
        // Clamp tra 0.0 e 1.0
        this.globalVolume = Math.max(0.0, Math.min(1.0, volume));
        
        // Aggiorna musica in tempo reale
        if (musicPlayer != null) {
            musicPlayer.setVolume(globalVolume * 0.7);
        }
    }
    
    public double getVolume() {
        return globalVolume;
    }
}