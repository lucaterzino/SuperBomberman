package main.logic;

import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import main.Gioco;
import main.view.LevelMapRenderer;

public class LevelMapController {

    @FXML private Canvas mapCanvas;
    
    private LevelMapRenderer renderer; 
    private Gioco mainApp;
    private AnimationTimer loop;

    private int selectedIndex = 0; // 0 = Livello 1, 1 = Livello 2, 2 = Livello 3
    private int maxUnlockedLevel = 1; // Parte da 1

    public void setMainApp(Gioco mainApp) {
        this.mainApp = mainApp;
    }
    
    // Chiamato da Gioco.java per aggiornare i progressi
    public void setMaxUnlockedLevel(int level) {
        this.maxUnlockedLevel = level;
    }

    public void startLoop() {
        if (renderer == null) {
            renderer = new LevelMapRenderer(mapCanvas.getGraphicsContext2D(), 1024, 768);
        }
        
        loop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                draw();
            }
        };
        loop.start();
        mapCanvas.requestFocus();
        
        // Assicuriamoci che l'input sia collegato
        mapCanvas.setOnKeyPressed(this::handleKeyPressed);
    }

    public void stopLoop() {
        if (loop != null) loop.stop();
    }

    private void handleKeyPressed(KeyEvent event) {
        KeyCode code = event.getCode();
        
        if (code == KeyCode.LEFT) {
            selectedIndex--;
            if (selectedIndex < 0) selectedIndex = 2;
            AudioManager.getInstance().playSound("cursor");
        } else if (code == KeyCode.RIGHT) {
            selectedIndex++;
            if (selectedIndex > 2) selectedIndex = 0;
            AudioManager.getInstance().playSound("cursor");
        } else if (code == KeyCode.UP || code == KeyCode.DOWN) {
            // Navigazione alternativa
            if (selectedIndex == 0) selectedIndex = 1;
            else selectedIndex = 0;
            AudioManager.getInstance().playSound("cursor");
        } else if (code == KeyCode.ENTER || code == KeyCode.Z) {
            tryEnterLevel();
        } else if (code == KeyCode.ESCAPE) {
            // Torna al menu principale
            mainApp.showMenuScreen();
        }
    }
    
    private void tryEnterLevel() {
        int levelToEnter = selectedIndex + 1;
        
        if (levelToEnter <= maxUnlockedLevel) {
            // Livello Sbloccato: Avvia!
            AudioManager.getInstance().playSound("confirm");
            stopLoop();
            mainApp.startGameLevel(levelToEnter);
        } else {
            // Livello Bloccato: Suono errore o nulla
            // (Opzionale: aggiungere suono "denied")
            System.out.println("Livello Bloccato!");
        }
    }

    private void draw() {
        if (renderer != null) {
            renderer.drawMap(selectedIndex, maxUnlockedLevel);
        }
    }
}