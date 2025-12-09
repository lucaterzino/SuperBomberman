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

    // Stati del menu
    private enum MenuState { MAIN, OPTIONS }
    private MenuState currentState = MenuState.MAIN;

    // Main Menu
    private String[] options = {"NORMAL GAME", "OPTION", "EXIT"};
    private int selectedIndex = 0;
    
    // Options Menu
    private int optionIndex = 0; // 0 = Volume, 1 = Back
    
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
        AudioManager.getInstance().playMusic("/audio/menu_theme.mp3");
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
            handleOptionInput(code);
        }
    }
    
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
    
    private void handleOptionInput(KeyCode code) {
        if (code == KeyCode.UP || code == KeyCode.DOWN) {
            optionIndex = (optionIndex == 0) ? 1 : 0; // Toggle tra Volume e Back
            AudioManager.getInstance().playSound("cursor");
        } else if (code == KeyCode.LEFT) {
            if (optionIndex == 0) { // Abbassa volume
                double vol = AudioManager.getInstance().getVolume();
                AudioManager.getInstance().setVolume(vol - 0.1);
                AudioManager.getInstance().playSound("cursor");
            }
        } else if (code == KeyCode.RIGHT) {
            if (optionIndex == 0) { // Alza volume
                double vol = AudioManager.getInstance().getVolume();
                AudioManager.getInstance().setVolume(vol + 0.1);
                AudioManager.getInstance().playSound("cursor");
            }
        } else if (code == KeyCode.ENTER || code == KeyCode.Z) {
            if (optionIndex == 1) { // BACK
                currentState = MenuState.MAIN;
                AudioManager.getInstance().playSound("confirm");
            }
        } else if (code == KeyCode.ESCAPE) {
            currentState = MenuState.MAIN;
            AudioManager.getInstance().playSound("confirm");
        }
    }

    private void selectOption() {
        if (selectedIndex == 0) { // NORMAL GAME
            AudioManager.getInstance().playSound("confirm");
            AudioManager.getInstance().stopMusic(); // Ferma musica menu
            if (mainApp != null) {
                mainApp.showGameScreen();
            }
        } else if (selectedIndex == 1) { // OPTION
            currentState = MenuState.OPTIONS;
            optionIndex = 0;
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
                renderer.drawOptions(AudioManager.getInstance().getVolume(), optionIndex);
            }
        }
    }
}