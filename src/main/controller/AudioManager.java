package main.controller;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.net.URL;
import java.util.HashMap;

/**
 * Gestore Audio Singleton per JavaFX.
 * Gestisce la riproduzione di effetti sonori (.wav) e musica (.mp3).
 */
public class AudioManager {

    // --- Singleton Pattern ---
    private static AudioManager instance;

    public static AudioManager getInstance() {
        if (instance == null) {
            instance = new AudioManager();
        }
        return instance;
    }
    // --- Fine Singleton Pattern ---

    private MediaPlayer musicPlayer;
    private HashMap<String, AudioClip> soundEffects;

    private AudioManager() {
        soundEffects = new HashMap<>();
    }

    /**
     * Carica un effetto sonoro in memoria per un uso rapido.
     * È buona norma caricarli tutti all'avvio del gioco.
     * @param name Un nome per l'effetto (es. "bomb")
     * @param filename Il percorso al file (es. "/sfx/bomb_place.wav")
     */
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

    /**
     * Riproduce un effetto sonoro precedentemente caricato.
     * @param name Il nome dell'effetto (es. "bomb")
     */
    public void playSoundEffect(String name) {
        AudioClip clip = soundEffects.get(name);
        if (clip != null) {
            clip.play();
        } else {
            System.err.println("Effetto sonoro non trovato: " + name);
        }
    }

    /**
     * Riproduce una musica di sottofondo (file .mp3) in loop.
     * @param filename Il percorso al file .mp3 (es. "/music/stage_theme.mp3")
     */
    public void playMusic(String filename) {
        stopMusic(); // Ferma la musica precedente
        try {
            URL resource = getClass().getResource(filename);
            if (resource != null) {
                Media media = new Media(resource.toExternalForm());
                musicPlayer = new MediaPlayer(media);
                // Imposta il loop
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

    /**
     * Ferma la musica di sottofondo (MP3) attualmente in riproduzione.
     */
    public void stopMusic() {
        if (musicPlayer != null) {
            musicPlayer.stop();
            musicPlayer = null;
        }
    }
}