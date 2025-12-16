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

    // Stati del menu per gestire la navigazione
    private enum MenuState { MAIN, OPTIONS }
    private MenuState currentState = MenuState.MAIN;

    // Opzioni Menu Principale
    private String[] options = {"START", "OPTION", "EXIT"};
    private int selectedIndex = 0;
    
    // Opzioni Schermata Options
    // 0 = Slider Volume, 1 = Back
    private int optionIndex = 0; 
    
    private double time = 0;
    private double cloudX = 0;

    public void setMainApp(Gioco mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    public void initialize() {
        renderer = new MenuRenderer(menuCanvas.getGraphicsContext2D(), 1024, 768);

        menuLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                update();
                draw();
            }
        };
        
        startLoop();
    }
    
    public void startLoop() {
        if (menuLoop != null) menuLoop.start();
        
        // Avvia la musica del menu
       // AudioManager.getInstance().playMusic("menu");
        
        draw(); 
        if (menuCanvas != null) menuCanvas.requestFocus();
    }

    public void stopLoop() {
        if (menuLoop != null) menuLoop.stop();
    }

    public void handleKeyPressed(KeyEvent event) {
        KeyCode code = event.getCode();
        
        if (currentState == MenuState.MAIN) {
            handleMainInput(code);
        } else if (currentState == MenuState.OPTIONS) {
            handleOptionsInput(code);
        }
    }
    
    // Gestione input Menu Principale
    private void handleMainInput(KeyCode code) {
        if (code == KeyCode.UP) {
            selectedIndex--;
            if (selectedIndex < 0) selectedIndex = options.length - 1;
            AudioManager.getInstance().playSound("cursor");
        } else if (code == KeyCode.DOWN) {
            selectedIndex++;
            if (selectedIndex >= options.length) selectedIndex = 0;
            AudioManager.getInstance().playSound("cursor");
        } else if (code == KeyCode.ENTER || code == KeyCode.SPACE || code == KeyCode.Z) {
            selectOption();
        }
    }
    
    // Gestione input Menu Opzioni
    private void handleOptionsInput(KeyCode code) {
        // Navigazione Verticale (Tra Volume e Back)
        if (code == KeyCode.UP || code == KeyCode.DOWN) {
            optionIndex = (optionIndex == 0) ? 1 : 0; 
            AudioManager.getInstance().playSound("cursor");
        } 
        // Regolazione Volume (Solo se siamo sulla riga 0 - Volume)
        else if (code == KeyCode.LEFT) {
            if (optionIndex == 0) { 
                double vol = AudioManager.getInstance().getVolume();
                AudioManager.getInstance().setVolume(vol - 0.1);
                AudioManager.getInstance().playSound("cursor");
            }
        } else if (code == KeyCode.RIGHT) {
            if (optionIndex == 0) { 
                double vol = AudioManager.getInstance().getVolume();
                AudioManager.getInstance().setVolume(vol + 0.1);
                AudioManager.getInstance().playSound("cursor");
            }
        } 
        // Selezione / Torna Indietro
        else if (code == KeyCode.ENTER || code == KeyCode.Z || code == KeyCode.ESCAPE) {
            if (optionIndex == 1 || code == KeyCode.ESCAPE) { // Back o ESC
                currentState = MenuState.MAIN; // Torna al main
                AudioManager.getInstance().playSound("confirm");
            }
        }
    }

    private void selectOption() {
        if (selectedIndex == 0) { // START
            AudioManager.getInstance().playSound("confirm");
            if (mainApp != null) {
                mainApp.showLevelMapScreen();
            }
        } else if (selectedIndex == 1) { // OPTION
            currentState = MenuState.OPTIONS;
            optionIndex = 0; // Reset selezione su Volume
            AudioManager.getInstance().playSound("confirm");
        } else if (selectedIndex == 2) { // EXIT
            System.exit(0);
        }
    }

    private void update() {
        time += 0.05; 
        cloudX -= 0.5;
        if (cloudX < -250) cloudX = 0;
    }

    private void draw() {
        if (renderer != null) {
            if (currentState == MenuState.MAIN) {
                renderer.drawMenu(time, cloudX, options, selectedIndex);
            } else {
                renderer.drawOptionsScreen(AudioManager.getInstance().getVolume(), optionIndex);
            }
        }
    }
}