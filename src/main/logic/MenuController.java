package main.logic;

import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import main.Gioco;
import main.view.MenuRenderer;

public class MenuController {

    @FXML private Canvas menuCanvas;
    private MenuRenderer renderer;
    private Gioco mainApp;
    private AnimationTimer menuLoop;

    private String[] options = {"NORMAL GAME", "OPTION", "EXIT"};
    private int selectedIndex = 0;
    
    private double time = 0;
    private double cloudX = 0;

    public void setMainApp(Gioco mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    public void initialize() {
        // Inizializza View
        renderer = new MenuRenderer(menuCanvas.getGraphicsContext2D(), 1024, 768);

        // Definisci Loop
        menuLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                update();
                draw();
            }
        };
        
        // Avvia subito
        startLoop();
    }
    
    // Metodo per avviare il loop (chiamato da Gioco.java quando si torna al menu)
    public void startLoop() {
        if (menuLoop != null) {
            menuLoop.start();
        }
        // Forza il focus sul canvas per ricevere input tastiera
        if (menuCanvas != null) {
            menuCanvas.requestFocus();
            // Disegna un frame immediatamente per evitare flash neri/blu
            draw();
        }
    }

    public void stopLoop() {
        if (menuLoop != null) {
            menuLoop.stop();
        }
    }

    public void handleKeyPressed(KeyEvent event) {
        KeyCode code = event.getCode();
        
        if (code == KeyCode.UP) {
            selectedIndex--;
            if (selectedIndex < 0) selectedIndex = options.length - 1;
        } else if (code == KeyCode.DOWN) {
            selectedIndex++;
            if (selectedIndex >= options.length) selectedIndex = 0;
        } else if (code == KeyCode.ENTER || code == KeyCode.SPACE || code == KeyCode.Z) {
            selectOption();
        }
    }

    private void selectOption() {
        if (selectedIndex == 0) { // NORMAL GAME
            stopLoop(); // Ferma l'animazione del menu mentre si gioca
            if (mainApp != null) {
                mainApp.showGameScreen();
            }
        } else if (selectedIndex == 2) { // EXIT
            System.exit(0);
        } else {
            // OPTION
            System.out.println("Opzione non disponibile: " + options[selectedIndex]);
        }
    }

    private void update() {
        time += 0.05; 
        cloudX -= 0.5;
        if (cloudX < -250) cloudX = 0;
    }

    private void draw() {
        if (renderer != null) {
            renderer.drawMenu(time, cloudX, options, selectedIndex);
        }
    }
}