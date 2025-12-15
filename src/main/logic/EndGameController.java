package main.logic;

import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import main.Gioco;
import main.view.EndGameRenderer;

public class EndGameController {

    @FXML private Canvas endCanvas;
    
    private EndGameRenderer renderer; 
    private Gioco mainApp;
    private AnimationTimer loop;
    private double time = 0;

    public void setMainApp(Gioco mainApp) {
        this.mainApp = mainApp;
    }

    public void startLoop() {
        if (renderer == null) {
            renderer = new EndGameRenderer(endCanvas.getGraphicsContext2D(), 1024, 768);
        }
        
        // Musica Vittoria Finale (opzionale, riusa 'win' o 'song_menu')
        AudioManager.getInstance().playMusic("/audio/song_menu.mp3");

        loop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                time += 0.016; // Approx 60fps
                renderer.drawEndScreen(time);
            }
        };
        loop.start();
        endCanvas.requestFocus();
        endCanvas.setOnKeyPressed(this::handleKeyPressed);
    }

    public void stopLoop() {
        if (loop != null) loop.stop();
    }

    private void handleKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.Z || event.getCode() == KeyCode.ESCAPE) {
            // Torna al menu principale e resetta il gioco
            AudioManager.getInstance().playSound("confirm");
            mainApp.resetGameAndShowMenu(); 
        }
    }
}